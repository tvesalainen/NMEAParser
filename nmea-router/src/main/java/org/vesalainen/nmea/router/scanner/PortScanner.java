/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.nmea.router.scanner;

import java.io.IOException;
import java.nio.channels.ScatteringByteChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import static java.util.logging.Level.*;
import org.vesalainen.comm.channel.PortMonitor;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.math.SymmetricDifferenceMatcher;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.nmea.router.NMEAMatcher;
import org.vesalainen.nmea.router.NMEAReader;
import org.vesalainen.nmea.router.PortType;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.RepeatingIterator;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.AbstractProvisioner.Setting;
import org.vesalainen.util.HexDump;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PortScanner extends JavaLogging
{
    static final int BUF_SIZE = 4096;
    private ScheduledExecutorService pool;
    private Set<PortType> portTypes;
    private long monitorPeriod = 10000;
    private long closeDelay = 1000;
    private long fingerPrintDelay = 20000;
    private Map<String,Iterator<PortType>> channelIterators = new HashMap<>();
    private Map<String,Future<Throwable>> futures = new HashMap<>();
    private Map<String,Scanner> scanners = new HashMap<>();
    private ScheduledFuture<?> monitorFuture;
    private Consumer<ScanResult> consumer;
    private SymmetricDifferenceMatcher<String,SerialType> portMatcher;
    private Set<String> dontScan;
    private PortMonitor portMonitor;
    private Set<String> ports = new HashSet<>();

    public PortScanner(ScheduledExecutorService pool)
    {
        this(pool, Collections.EMPTY_SET);
    }

    public PortScanner(ScheduledExecutorService pool, Set<String> dontScan)
    {
        super(PortScanner.class);
        this.pool = pool;
        this.dontScan = dontScan;
    }

    public void stop()
    {
        if (monitorFuture != null )
        {
            monitorFuture.cancel(false);
            portMonitor.stop();
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        stop();
        super.finalize();
    }
    
    public boolean isScanning()
    {
        return monitorFuture != null && !monitorFuture.isDone();
    }
    public void waitScanner() throws IOException
    {
        waitScanner(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    public void waitScanner(long time, TimeUnit unit) throws IOException
    {
        if (monitorFuture == null)
        {
            return;
        }
        try
        {
            monitorFuture.get(time, unit);
        }
        catch (CancellationException | TimeoutException ex)
        {
        }
        catch (InterruptedException | ExecutionException ex)
        {
            throw new IOException(ex);
        }
    }
    public PortScanner setChannelSuppliers(Set<PortType> portTypes)
    {
        this.portTypes = portTypes;
        return this;
    }
    @Setting
    public PortScanner setCheckDelay(long checkDelay)
    {
        this.monitorPeriod = checkDelay;
        return this;
    }
    @Setting
    public PortScanner setCloseDelay(long closeDelay)
    {
        this.closeDelay = closeDelay;
        return this;
    }
    @Setting
    public PortScanner setFingerPrintDelay(long fingerPrintDelay)
    {
        this.fingerPrintDelay = fingerPrintDelay;
        return this;
    }
    private void checkDelays()
    {
        if (!(closeDelay < monitorPeriod && monitorPeriod < fingerPrintDelay))
        {
            throw new IllegalArgumentException("closeDelay < checkPeriod < fingerPrintPeriod");
        }
    }
    public void scan(Consumer<ScanResult> consumer) throws IOException
    {
        scan(consumer, SymmetricDifferenceMatcher.EMPTY_MATCHER);
    }
    public void scan(Consumer<ScanResult> consumer, SymmetricDifferenceMatcher<String,SerialType> portMatcher) throws IOException
    {
        config("started port scanner");
        if (isScanning())
        {
            throw new IllegalStateException("scan is already running");
        }
        checkDelays();
        if (portTypes == null || portTypes.isEmpty())
        {
            warning("no channel suppliers");
        }
        Objects.requireNonNull(consumer, "consumer");
        Objects.requireNonNull(portMatcher, "portMatcher");
        this.consumer = consumer;
        this.portMatcher = portMatcher;
        ports.addAll(SerialChannel.getFreePorts());
        config("scanning %s", ports);
        portMonitor = new PortMonitor(pool, monitorPeriod, TimeUnit.MILLISECONDS);
        portMonitor.addNewFreePortConsumer((p)->ports.add(p));
        portMonitor.addRemovePortConsumer((p)->ports.remove(p));
        monitorFuture = pool.scheduleWithFixedDelay(this::monitor, 0, monitorPeriod, TimeUnit.MILLISECONDS);
    }
    private void initialStartScanner(String port, long delayMillis) throws IOException
    {
        RepeatingIterator<PortType> it = new RepeatingIterator<>(portTypes);
        channelIterators.put(port, it);
        startScanner(port, 0);
    }
    private void startScanner(String port, long delayMillis) throws IOException
    {
        config("starting scanner for %s after %d millis", port, delayMillis);
        config("scanned port types %s", portTypes);
        Iterator<PortType> it = channelIterators.get(port);
        if (it.hasNext())
        {
            PortType portType = it.next();
            Scanner scanner = new Scanner(port, portType);
            scanners.put(port, scanner);
            Future<Throwable> future = pool.schedule(scanner, delayMillis, TimeUnit.MILLISECONDS);
            futures.put(port, future);
        }
        else
        {
            throw new IllegalArgumentException("should not happen");
        }
    }
    public void monitor()
    {
        for (String port : ports)
        {
            if (!dontScan.contains(port))
            {
                if (!scanners.containsKey(port))
                {
                    try
                    {
                        initialStartScanner(port, 0);
                        config("started scanner for new port %s", port);
                    }
                    catch (IOException ex)
                    {
                        log(SEVERE, ex, "initialStartScanner %s", ex.getMessage());
                    }
                }
                else
                {
                    boolean rescan = false;
                    Scanner scanner = scanners.get(port);
                    Future<Throwable> future = futures.get(port);
                    if (future.isDone())
                    {
                        if (!future.isCancelled() && scanner.getSerialType() != null)
                        {
                            fine("finger print for %s because unique hit", port, fingerPrintDelay, scanner.getFingerPrint());
                            ScanResult sr = new ScanResult(scanner);
                            consumer.accept(sr);
                            scanners.remove(port);
                            futures.remove(port);
                        }
                        else
                        {
                            try
                            {
                                log(SEVERE, future.get(), "rescanning %s because stopped", port);
                            }
                            catch (InterruptedException | ExecutionException ex)
                            {
                                log(SEVERE, ex, "%s", ex.getMessage());
                            }
                            rescan = true;
                        }
                    }
                    else
                    {
                        if (scanner.getElapsedTime() >= monitorPeriod && scanner.getFingerPrint().isEmpty())
                        {
                            fine("rescanning %s because no finger print after %d millis", port, monitorPeriod);
                            rescan = true;
                        }
                        else
                        {
                            if (scanner.getElapsedTime() >= fingerPrintDelay)
                            {
                                fine("finger print for %s after %d millis %s", port, fingerPrintDelay, scanner.getFingerPrint());
                                ScanResult sr = new ScanResult(scanner);
                                consumer.accept(sr);
                                future.cancel(true);
                                scanners.remove(port);
                                futures.remove(port);
                            }
                        }
                    }
                    if (rescan)
                    {
                        future.cancel(true);
                        try
                        {
                            future.get();
                        }
                        catch (CancellationException | InterruptedException | ExecutionException ex)
                        {
                            fine("cancelled %s", scanner);
                        }
                        try
                        {
                            startScanner(port, closeDelay);
                        }
                        catch (IOException ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        }
    }
    private class Scanner extends BaseScanner implements Callable<Throwable>
    {
        private String port;
        private PortType portType;
        private NMEAMatcher<Boolean> matcher;
        protected SerialType serialType;

        public Scanner(String port, PortType portType) throws IOException
        {
            this.port = port;
            this.portType = portType;
            matcher = new NMEAMatcher<>();
            matcher.addExpression("$", true);
            matcher.addExpression("!", true);
            matcher.compile();
            time = System.currentTimeMillis();
        }

        @Override
        public Throwable call() throws Exception
        {
            try (ScatteringByteChannel channel = portType.getChannelFactory().apply(port))
            {
                config("started scanner for %s %s", port, channel);
                NMEAReader reader = new NMEAReader(port, matcher, channel, 128, 10, this::onOk, this::onError);
                reader.read();
            }
            catch (WrongPortTypeException ex)
            {
            }
            catch (Throwable ex)
            {
                return ex;
            }
            finally
            {
                fine("scanner %s exit", port);
            }
            return null;
        }

        private void onOk(RingByteBuffer ring)
        {
            finer("read: %s", ring);
            int idx = CharSequences.indexOf(ring, ',');
            if (idx != -1)
            {
                String prefix = ring.subSequence(0, idx).toString();
                fingerPrint.add(prefix);
                serialType = portMatcher.match(fingerPrint);
                if (serialType != null)
                {
                    if (portType == PortType.getPortType(serialType))
                    {
                        throw new WrongPortTypeException();
                    }
                }
            }
            matched += ring.length();
        }
        private void onError(Supplier<byte[]> errInput)
        {
            fine(()->HexDump.toHex(errInput));
        }
        public SerialType getSerialType()
        {
            return serialType;
        }

        @Override
        public String toString()
        {
            return "Scanner{" + "port=" + port + ", fingerPrint=" + fingerPrint + ", count=" + count + ", matched=" + matched + '}';
        }
        
    }
    private class BaseScanner 
    {
        protected Set<String> fingerPrint = new HashSet<>();
        protected int count;
        protected long time;
        protected int matched;
        
        public long getElapsedTime()
        {
            return System.currentTimeMillis() - time;
        }
        
        public Set<String> getFingerPrint()
        {
            return fingerPrint;
        }

        public int getCount()
        {
            return count;
        }

        public int getMatched()
        {
            return matched;
        }
        
        public int getErrors()
        {
            return count - matched;
        }

    }
    public static class ScanResult
    {
        private String port;
        private PortType portType;
        private SerialType serialType;
        private Set<String> fingerPrint;

        public ScanResult(Scanner scanner)
        {
            this.port = scanner.port;
            this.portType = scanner.portType;
            this.fingerPrint = scanner.getFingerPrint();
            this.serialType = scanner.getSerialType();
        }

        public String getPort()
        {
            return port;
        }

        public PortType getPortType()
        {
            return portType;
        }

        public Set<String> getFingerPrint()
        {
            return fingerPrint;
        }

        public SerialType getSerialType()
        {
            return serialType;
        }

        @Override
        public String toString()
        {
            return "ScanResult{" + "port=" + port + ", portType=" + portType + ", fingerPrint=" + fingerPrint + '}';
        }

    }
    private class WrongPortTypeException extends RuntimeException
    {
        
    }
}
