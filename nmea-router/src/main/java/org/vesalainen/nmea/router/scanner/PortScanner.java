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
import java.util.Collection;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import static java.util.logging.Level.*;
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
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PortScanner extends JavaLogging
{
    static final int BUF_SIZE = 4096;
    private CachedScheduledThreadPool pool;
    private long monitorPeriod = 10000;
    private long closeDelay = 1000;
    private long fingerPrintDelay = 20000;
    private Map<String,Iterator<PortType>> channelIterators = new HashMap<>();
    private Map<Scanner,Future<?>> futures = new ConcurrentHashMap<>();
    private ScheduledFuture<?> monitorFuture;
    private Consumer<ScanResult> consumer;
    private Map<PortType,SymmetricDifferenceMatcher<String,SerialType>> portMatcher;
    private Set<PortType> portTypes;
    private Set<String> dontScan;
    private Map<String,PortType> lastPortType;

    public PortScanner(CachedScheduledThreadPool pool)
    {
        this(pool, Collections.EMPTY_SET, Collections.EMPTY_MAP);
    }

    public PortScanner(CachedScheduledThreadPool pool, Collection<String> dontScan, Map<String,PortType> lastPortType)
    {
        super(PortScanner.class);
        this.pool = pool;
        this.dontScan = new HashSet<>(dontScan);
        this.lastPortType = lastPortType;
    }

    public void stop()
    {
        if (monitorFuture != null )
        {
            monitorFuture.cancel(false);
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
    public void scan(Consumer<ScanResult> consumer, Map<PortType,SymmetricDifferenceMatcher<String,SerialType>> prtMtchr) throws IOException
    {
        Objects.requireNonNull(consumer, "consumer");
        this.consumer = consumer;
        if (prtMtchr != null)
        {
            this.portMatcher = prtMtchr;
            this.portTypes = new ConditionalSet<>(prtMtchr.keySet(), (PortType k)->
            {   // only unresolved port types
                SymmetricDifferenceMatcher<String,SerialType> m = prtMtchr.get(k);
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
        monitorFuture = pool.scheduleWithFixedDelay(this::monitor, 0, monitorPeriod, TimeUnit.MILLISECONDS);
    }
    private void initialStartScanner(String port) throws IOException
    {
        PortType portType = lastPortType.get(port);
        config("initialStartScanner(%s) starting with %s", port, portType);
        RepeatingIterator<PortType> it = new RepeatingIterator<>(portTypes, portType);
        channelIterators.put(port, it);
        startScannerAfter(port, null);
    }
    private void startScannerAfter(String port, Scanner after) throws IOException
    {
        config("starting scanner for %s after %s", port, after);
        config("scanning port types %s", portTypes);
        Iterator<PortType> it = channelIterators.get(port);
        if (it.hasNext())
        {
            PortType portType = it.next();
            Scanner scanner = new Scanner(port, portType);
            Future<?> future;
            if (after != null)
            {
                future = pool.submitAfter(after::waitClose, scanner);
            }
            else
            {
                future = pool.submit(scanner);
            }
            futures.put(scanner, future);
        }
        else
        {
            severe("NO PORT TYPES!!!");
            throw new IllegalArgumentException("should not happen");
        }
    }
    public void monitor()
    {
        try
        {
            futures.values().removeIf((f)->f.isDone());
            List<String> freePorts = SerialChannel.getFreePorts();
            fine("monitor free ports=%s exclude=%s", freePorts, dontScan);
            for (String port : freePorts)
            {
                if (!dontScan.contains(port))
                {
                    if (!futures.keySet().stream().anyMatch((s)->s.port.equals(port)))
                    {
                        try
                        {
                            initialStartScanner(port);
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
                config("%s", scanner);
                String port = scanner.port;
                Future<?> future = futures.get(scanner);
                if (scanner.getElapsedTime() >= monitorPeriod && scanner.getFingerPrint().isEmpty())
                {
                    fine("rescanning %s because no finger print after %d millis", port, monitorPeriod);
                    boolean cancelled = future.cancel(true);
                    fine("%s cancel=%s", port, cancelled);
                    startScannerAfter(port, scanner);
                }
                else
                {
                    if (scanner.getElapsedTime() >= fingerPrintDelay && portMatcher.isEmpty())  // initial scanning
                    {
                        fine("finger print for %s after %d millis %s", port, fingerPrintDelay, scanner.getFingerPrint());
                        ScanResult sr = new ScanResult(scanner);
                        pool.submitAfter(scanner::waitClose, ()->consumer.accept(sr));
                        future.cancel(true);
                    }
                }
            }
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "startScanner %s", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
    private class Scanner extends BaseScanner implements Runnable
    {
        private String port;
        private PortType portType;
        private NMEAMatcher<Boolean> matcher;
        private SerialType serialType;
        private CountDownLatch latch = new CountDownLatch(1);

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

        public boolean waitClose(long timeout, TimeUnit unit) throws InterruptedException
        {
            return latch.await(timeout, unit);
        }

        @Override
        public void run()
        {
            try (ScatteringByteChannel channel = portType.getChannelFactory().apply(port))
            {
                config("started scanner for %s %s", port, channel);
                NMEAReader reader = new NMEAReader(port, matcher, channel, 128, this::onOk, this::onError);
                reader.read();
            }
            catch (PortFoundException ex)
            {
                fine("finger print for %s because unique hit", port, fingerPrintDelay, getFingerPrint());
                ScanResult sr = new ScanResult(this);
                consumer.accept(sr);
            }
            catch (ClosedByInterruptException ex)
            {
                fine("%s interrupted", port);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "%s stopped %s", port, ex.getMessage());
            }
            finally
            {
                fine("scanner %s exit", port);
                latch.countDown();
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
            return "Scanner{" + "port=" + port + ", portType=" + portType + '}';
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
