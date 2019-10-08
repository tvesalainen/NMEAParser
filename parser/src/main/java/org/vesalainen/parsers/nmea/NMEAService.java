/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Stream;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.nmea.util.NMEASampler;
import org.vesalainen.parsers.nmea.ais.AISDispatcher;
import org.vesalainen.parsers.nmea.time.GPSClock;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEAService extends JavaLogging implements Runnable, AutoCloseable
{
    protected ScatteringByteChannel in;
    protected GatheringByteChannel out;
    protected CachedScheduledThreadPool executor;
    private final NMEADispatcher nmeaDispatcher;
    private AISDispatcher aisDispatcher;
    private final List<AutoCloseable> autoCloseables = new ArrayList<>();
    private Thread thread;
    private boolean liveClock = true;
    private Future<?> future;
    protected GPSClock clock;
    protected CountDownLatch running = new CountDownLatch(1);

    public NMEAService(String address, int port) throws IOException
    {
        this(address, port, new CachedScheduledThreadPool());
    }
    public NMEAService(String address, int port, CachedScheduledThreadPool executor) throws IOException
    {
        this(UnconnectedDatagramChannel.open(address, port, 100, true, true), executor);
    }

    public NMEAService(UnconnectedDatagramChannel channel) throws IOException
    {
        this(channel, new CachedScheduledThreadPool());
    }
    public NMEAService(UnconnectedDatagramChannel channel, CachedScheduledThreadPool executor) throws IOException
    {
        this(channel, channel, executor);
    }

    public NMEAService(DatagramChannel channel) throws IOException
    {
        this(channel, new CachedScheduledThreadPool());
    }
    public NMEAService(DatagramChannel channel, CachedScheduledThreadPool executor) throws IOException
    {
        this(channel, channel, executor);
    }

    public NMEAService(ScatteringByteChannel in, GatheringByteChannel out) throws IOException
    {
        this(in, out, new CachedScheduledThreadPool());
    }
    public NMEAService(ScatteringByteChannel in, GatheringByteChannel out, CachedScheduledThreadPool executor) throws IOException
    {
        setLogger(this.getClass());
        this.in = in;
        this.out = out;
        this.executor = executor;
        nmeaDispatcher = NMEADispatcher.newInstance();
    }
    
    public void start()
    {
        future = executor.submit(this);
    }
    
    public void stop()
    {
        future.cancel(true);
    }

    public Stream<NMEASample> stream(String... properties)
    {
        return sampler(properties).stream();
    }

    public NMEASampler sampler(String... properties)
    {
        return new NMEASampler(nmeaDispatcher, properties);
    }

    public boolean isLiveClock()
    {
        return liveClock;
    }

    public void setLiveClock(boolean liveClock)
    {
        this.liveClock = liveClock;
    }
    
    public void addNMEAObserver(PropertySetter propertySetter)
    {
        addNMEAObserver(propertySetter, true);
    }
    public void addNMEAObserver(PropertySetter propertySetter, boolean reportMissingProperties)
    {
        addClock(propertySetter);
        nmeaDispatcher.addObserver(propertySetter, reportMissingProperties);
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.add(ac);
        }
    }
    
    public void addAISObserver(PropertySetter propertySetter)
    {
        addAISObserver(propertySetter, true);
    }
    public void addAISObserver(PropertySetter propertySetter, boolean reportMissingProperties)
    {
        if (aisDispatcher == null)
        {
            aisDispatcher = AISDispatcher.newInstance();
        }
        addClock(propertySetter);
        aisDispatcher.addObserver(propertySetter, reportMissingProperties);
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.add(ac);
        }
    }
    
    public void addNMEAObserver(AnnotatedPropertyStore propertyStore)
    {
        addNMEAObserver(propertyStore, true);
    }
    public void addNMEAObserver(AnnotatedPropertyStore propertyStore, boolean reportMissingProperties)
    {
        addClock(propertyStore);
        nmeaDispatcher.addObserver(propertyStore, reportMissingProperties);
        if (propertyStore instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertyStore;
            autoCloseables.add(ac);
        }
    }
    
    public void addAISObserver(AnnotatedPropertyStore propertyStore)
    {
        addAISObserver(propertyStore, true);
    }
    public void addAISObserver(AnnotatedPropertyStore propertyStore, boolean reportMissingProperties)
    {
        if (aisDispatcher == null)
        {
            aisDispatcher = AISDispatcher.newInstance();
        }
        addClock(propertyStore);
        aisDispatcher.addObserver(propertyStore, reportMissingProperties);
        if (propertyStore instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertyStore;
            autoCloseables.add(ac);
        }
    }
    
    public void removeNMEAObserver(PropertySetter propertySetter)
    {
        nmeaDispatcher.removeObserver(propertySetter);
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.remove(ac);
        }
    }
    
    public void removeAISObserver(PropertySetter propertySetter)
    {
        aisDispatcher.removeObserver(propertySetter);
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.remove(ac);
        }
    }
    private void addClock(PropertySetter propertySetter)
    {
        if (clock != null && propertySetter.wantsProperty("clock"))
        {
            Transactional transactional = null;
            if (propertySetter instanceof Transactional)
            {
                transactional = (Transactional) propertySetter;
                transactional.start("add clock");
            }
            propertySetter.set("clock", clock); // supply clock is attached on the fly
            if (transactional != null)
            {
                transactional.commit("add clock");
            }
        }
    }
    public boolean hasObservers()
    {
        return nmeaDispatcher.hasObservers() ||
                (aisDispatcher != null && aisDispatcher.hasObservers());
    }
    
    @Override
    public void run()
    {
        try
        {
            NMEAParser parser = NMEAParser.newInstance();
            Supplier<InetSocketAddress> origin = ()->null;
            if (in instanceof UnconnectedDatagramChannel)
            {
                UnconnectedDatagramChannel udc = (UnconnectedDatagramChannel) in;
                origin = ()->{return udc.getFromAddress();};
            }
            clock = GPSClock.getInstance(liveClock);
            setClockSupplier(()->clock);
            running.countDown();
            running = null;
            parser.parse(in, clock, origin, nmeaDispatcher, aisDispatcher, executor);
        }
        catch (Exception ex)
        {
            log(Level.SEVERE, ex, "");
        }
        log(Level.SEVERE, "NMEA Service dies!!!");
    }

    @Override
    public void close() throws Exception
    {
        for (AutoCloseable ac : autoCloseables)
        {
            ac.close();
        }
    }

}
