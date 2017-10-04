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

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import static java.util.logging.Level.*;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.nio.RingBuffer;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.router.NMEAMatcher;
import org.vesalainen.nmea.router.seatalk.SeaTalkChannel;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.Matcher;
import org.vesalainen.util.RepeatingIterator;
import org.vesalainen.util.function.IOFunction;
import org.vesalainen.util.logging.JavaLogging;
import static org.vesalainen.nmea.router.ThreadPool.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PortScanner extends JavaLogging
{
    private static final int BUF_SIZE = 128;
    private List<IOFunction<String,ScatteringByteChannel>> channelSuppliers = new ArrayList<>();
    private long checkPeriod = 5000;
    private long closeDelay = 1000;
    private long fingerPrintPeriod = 10000;
    private List<String> freePorts;
    private Map<String,Iterator<IOFunction<String,ScatteringByteChannel>>> channelIterators = new HashMap<>();
    private Map<String,Future<Throwable>> futures = new HashMap<>();
    private Map<String,Scanner> scanners = new HashMap<>();
    private ScheduledFuture<?> scanFuture;
    private Consumer<ScanResult> consumer;

    public PortScanner()
    {
        super(PortScanner.class);
    }

    public void waitScanner() throws IOException
    {
        waitScanner(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    public void waitScanner(long time, TimeUnit unit) throws IOException
    {
        try
        {
            scanFuture.get(time, unit);
        }
        catch (CancellationException ex)
        {
        }
        catch (InterruptedException | ExecutionException | TimeoutException ex)
        {
            throw new IOException(ex);
        }
    }
    public PortScanner addChannelSuppliers(IOFunction<String,ScatteringByteChannel>... suppliers)
    {
        for (IOFunction<String,ScatteringByteChannel> supplier : suppliers)
        {
            channelSuppliers.add(supplier);
        }
        return this;
    }

    public PortScanner setCheckPeriod(long checkPeriod)
    {
        this.checkPeriod = checkPeriod;
        return this;
    }

    public PortScanner setCloseDelay(long closeDelay)
    {
        this.closeDelay = closeDelay;
        return this;
    }

    public PortScanner setFingerPrintPeriod(long fingerPrintPeriod)
    {
        this.fingerPrintPeriod = fingerPrintPeriod;
        return this;
    }
    private void checkDelays()
    {
        if (!(closeDelay < checkPeriod && checkPeriod < fingerPrintPeriod))
        {
            throw new IllegalArgumentException("closeDelay < checkPeriod < fingerPrintPeriod");
        }
    }
    public static ScatteringByteChannel nmea4800(String port) throws IOException
    {
        JavaLogging.getLogger(PortScanner.class).fine("created NMEA4800 channel for %s", port);
        return new SerialChannel.Builder(port, SerialChannel.Speed.B4800).get();
    }
    public static ScatteringByteChannel nmea38400(String port) throws IOException
    {
        JavaLogging.getLogger(PortScanner.class).fine("created NMEA38400 channel for %s", port);
        return new SerialChannel.Builder(port, SerialChannel.Speed.B38400).get();
    }
    public static ScatteringByteChannel seaTalk(String port) throws IOException
    {
        JavaLogging.getLogger(PortScanner.class).fine("created SeaTalk channel for %s", port);
        return new SeaTalkChannel(port);
    }
    public void scan(Consumer<ScanResult> consumer) throws IOException
    {
        if (scanFuture != null)
        {
            throw new IllegalStateException("scan is already running");
        }
        checkDelays();
        freePorts = SerialChannel.getFreePorts();
        if (freePorts.isEmpty())
        {
            warning("no free ports");
            return;
        }
        if (channelSuppliers.isEmpty())
        {
            warning("no channel suppliers");
            return;
        }
        this.consumer = consumer;
        for (String port : freePorts)
        {
            RepeatingIterator<IOFunction<String, ScatteringByteChannel>> it = new RepeatingIterator<>(channelSuppliers);
            channelIterators.put(port, it);
            startScanner(port, 0);
        }
        Monitor monitor = new Monitor();
        scanFuture = POOL.scheduleWithFixedDelay(monitor, checkPeriod, checkPeriod, TimeUnit.MILLISECONDS);
    }
    private void startScanner(String port, long delayMillis) throws IOException
    {
        config("starting scanner for %s after %d millis", port, delayMillis);
        Iterator<IOFunction<String, ScatteringByteChannel>> it = channelIterators.get(port);
        if (it.hasNext())
        {
            IOFunction<String, ScatteringByteChannel> channelSupplier = it.next();
            Scanner scanner = new Scanner(port, channelSupplier);
            scanners.put(port, scanner);
            Future<Throwable> future = POOL.schedule(scanner, delayMillis, TimeUnit.MILLISECONDS);
            futures.put(port, future);
        }
        else
        {
            throw new IllegalArgumentException("should not happen");
        }
    }
    private class Monitor implements Runnable
    {

        @Override
        public void run()
        {
            Iterator<String> iterator = freePorts.iterator();
            while (iterator.hasNext())
            {
                String port = iterator.next();
                boolean rescan = false;
                Scanner scanner = scanners.get(port);
                Future<Throwable> future = futures.get(port);
                if (future.isDone())
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
                else
                {
                    if (scanner.getElapsedTime() >= checkPeriod && scanner.getFingerPrint().isEmpty())
                    {
                        fine("rescanning %s because no finger print after %d millis", port, checkPeriod);
                        rescan = true;
                    }
                    else
                    {
                        if (scanner.getElapsedTime() >= fingerPrintPeriod)
                        {
                            fine("finger print for %s after %d millis %s", port, fingerPrintPeriod, scanner.getFingerPrint());
                            ScanResult sr = new ScanResult(scanner);
                            consumer.accept(sr);
                            iterator.remove();
                            future.cancel(true);
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
            if (freePorts.isEmpty())
            {
                scanFuture.cancel(false);
            }
        }
        
    }
    private class Scanner implements Callable<Throwable>
    {
        private String port;
        private IOFunction<String, ScatteringByteChannel> supplier;
        private RingByteBuffer ring = new RingByteBuffer(BUF_SIZE, true);
        private NMEAMatcher<Boolean> matcher;
        private Set<String> fingerPrint = new HashSet<>();
        private int count;
        private int matched;
        private boolean mark = true;
        private Matcher.Status match;
        private long time;

        public Scanner(String port, IOFunction<String, ScatteringByteChannel> supplier) throws IOException
        {
            this.port = port;
            this.supplier = supplier;
            matcher = new NMEAMatcher<>();
            matcher.addExpression("$", true);
            matcher.addExpression("!", true);
            matcher.compile();
            time = System.currentTimeMillis();
        }

        @Override
        public Throwable call() throws Exception
        {
            try (ScatteringByteChannel channel = supplier.apply(port))
            {
                config("started scanner for %s %s", port, channel);
                while (true)
                {
                    int cnt = ring.read(channel);
                    if (cnt == -1)
                    {
                        return new EOFException(channel.toString());
                    }
                    count += cnt;
                    while (ring.hasRemaining())
                    {
                        byte b = ring.get(mark);
                        match = matcher.match(b);
                        switch (match)
                        {
                            case Error:
                                finest("drop: '%1$c' %1$d 0x%1$02X %2$s", b & 0xff, (RingBuffer)ring);
                                mark = true;
                                break;
                            case Ok:
                            case WillMatch:
                                mark = false;
                                break;
                            case Match:
                                finer("read: %s", ring);
                                int idx = CharSequences.indexOf(ring, ',');
                                if (idx != -1)
                                {
                                    fingerPrint.add(ring.subSequence(0, idx).toString());
                                }
                                matched += ring.length();
                                mark = true;
                                break;
                        }
                    }
                }
            }
            catch (Throwable ex)
            {
                return ex;
            }
            finally
            {
                fine("scanner %s exit", port);
            }
        }

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

        @Override
        public String toString()
        {
            return "Scanner{" + "port=" + port + ", fingerPrint=" + fingerPrint + ", count=" + count + ", matched=" + matched + '}';
        }
        
    }
    public static class ScanResult
    {
        private String port;
        private IOFunction<String, ScatteringByteChannel> supplier;
        private Set<String> fingerPrint;

        public ScanResult(Scanner scanner)
        {
            this.port = scanner.port;
            this.supplier = scanner.supplier;
            this.fingerPrint = scanner.fingerPrint;
        }

        public String getPort()
        {
            return port;
        }

        public IOFunction<String, ScatteringByteChannel> getSupplier()
        {
            return supplier;
        }

        public Set<String> getFingerPrint()
        {
            return fingerPrint;
        }

        @Override
        public String toString()
        {
            return "ScanResult{" + "port=" + port + ", fingerPrint=" + fingerPrint + '}';
        }
        
    }
}
