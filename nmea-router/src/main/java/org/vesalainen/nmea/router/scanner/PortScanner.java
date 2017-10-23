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
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ScatteringByteChannel;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
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
import org.vesalainen.util.ConditionalSet;
import org.vesalainen.util.HexDump;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PortScanner extends JavaLogging
{
    static final int BUF_SIZE = 4096;
    private ScheduledExecutorService pool;
    private long monitorPeriod = 10000;
    private long closeDelay = 1000;
    private long fingerPrintDelay = 20000;
    private Map<String,Iterator<PortType>> channelIterators = new HashMap<>();
    private Map<Scanner,Future<?>> futures = new ConcurrentHashMap<>();
    private Map<String,Scanner> scanners = new ConcurrentHashMap<>();
    private ScheduledFuture<?> monitorFuture;
    private Consumer<ScanResult> consumer;
    private Map<PortType,SymmetricDifferenceMatcher<String,SerialType>> portMatcher;
    private Set<PortType> portTypes;
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
        scan(consumer, null);
    }
    public void scan(Consumer<ScanResult> consumer, Map<PortType,SymmetricDifferenceMatcher<String,SerialType>> portMatcher) throws IOException
    {
        Objects.requireNonNull(consumer, "consumer");
        this.consumer = consumer;
        if (portMatcher != null)
        {
            this.portMatcher = portMatcher;
            this.portTypes = new ConditionalSet<>(portMatcher.keySet(), (PortType k)->
            {   // only unresolved port types
                SymmetricDifferenceMatcher<String,SerialType> m = portMatcher.get(k);
                return m!=null && !m.getUnresolved().isEmpty();
            });
        }
        else
        {
            this.portMatcher = Collections.EMPTY_MAP;
            this.portTypes = EnumSet.allOf(PortType.class);
        }
        config("portTypes %s - %s", portTypes, portMatcher.keySet());
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
        ports.addAll(SerialChannel.getFreePorts());
        config("scanning %s", ports);
        portMonitor = new PortMonitor(pool, monitorPeriod, TimeUnit.MILLISECONDS);
        portMonitor.addNewFreePortConsumer(this::addPort);
        portMonitor.addRemoveFreePortConsumer(this::removePort);
        monitorFuture = pool.scheduleWithFixedDelay(this::monitor, 0, monitorPeriod, TimeUnit.MILLISECONDS);
    }
    private void addPort(String port)
    {
        ports.add(port);
        config("added new free port %s", port);
    }
    private void removePort(String port)
    {
        ports.remove(port);
        config("removed free port %s", port);
    }
    private void initialStartScanner(String port, long delayMillis) throws IOException
    {
        config("initialStartScanner(%s, %d)", port, delayMillis);
        RepeatingIterator<PortType> it = new RepeatingIterator<>(portTypes);
        channelIterators.put(port, it);
        startScanner(port, delayMillis);
    }
    private void startScanner(String port, long delayMillis) throws IOException
    {
        config("starting scanner for %s after %d millis", port, delayMillis);
        config("scanning port types %s", portTypes);
        Iterator<PortType> it = channelIterators.get(port);
        if (it.hasNext())
        {
            PortType portType = it.next();
            Scanner scanner = new Scanner(port, portType);
            scanners.put(port, scanner);
            Future<?> future = pool.schedule(scanner, delayMillis, TimeUnit.MILLISECONDS);
            futures.put(scanner, future);
        }
        else
        {
            severe("no port types");
            throw new IllegalArgumentException("should not happen");
        }
    }
    public void monitor()
    {
        List<String> freePorts = SerialChannel.getFreePorts();
        fine("monitor free ports=%s", freePorts);
        for (String port : freePorts)
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
            }
        }
        for (Scanner scanner : futures.keySet())
        {
            String port = scanner.port;
            boolean rescan = false;
            Future<?> future = futures.get(scanner);
            if (future.isDone())
            {
                if (!future.isCancelled() && scanner.getSerialType() != null)
                {
                    fine("finger print for %s because unique hit", port, fingerPrintDelay, scanner.getFingerPrint());
                    ScanResult sr = new ScanResult(scanner);
                    consumer.accept(sr);
                }
                scanners.remove(port);
                ports.remove(port);
                futures.remove(scanner);
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
                        futures.remove(scanner);
                    }
                }
            }
            if (rescan)
            {
                future.cancel(true);
                try
                {
                    startScanner(port, closeDelay);
                }
                catch (IOException ex)
                {
                    log(SEVERE, ex, "startScanner %s", ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    private class Scanner extends BaseScanner implements Runnable
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
        public void run()
        {
            try (ScatteringByteChannel channel = portType.getChannelFactory().apply(port))
            {
                config("started scanner for %s %s", port, channel);
                NMEAReader reader = new NMEAReader(port, matcher, channel, 128, 10, this::onOk, this::onError);
                reader.read();
            }
            catch (PortFoundException ex)
            {
                fine("%s port found", port);
            }
            catch (ClosedByInterruptException ex)
            {
                fine("%s port interrupted");
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "%s stopped %s", port, ex.getMessage());
            }
            finally
            {
                fine("scanner %s exit", port);
            }
        }

        private void onOk(RingByteBuffer ring, long timestamp)
        {
            finer("read: %s", ring);
            int idx = CharSequences.indexOf(ring, ',');
            if (idx != -1)
            {
                String prefix = ring.subSequence(0, idx).toString();
                fingerPrint.add(prefix);
                SymmetricDifferenceMatcher<String, SerialType> sdm = portMatcher.get(portType);
                if (sdm != null)
                {
                    serialType = sdm.match(fingerPrint);
                    if (serialType != null)
                    {
                        throw new PortFoundException();
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
    private class PortFoundException extends RuntimeException
    {
        
    }
}
