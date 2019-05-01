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
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Stream;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.code.PropertySetterDispatcher;
import org.vesalainen.code.SimplePropertySetterDispatcher;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.nmea.util.NMEASampler;
import org.vesalainen.parsers.nmea.ais.AISDispatcher;
import org.vesalainen.parsers.nmea.time.GPSClock;
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
    private PropertySetterDispatcher dispatcher;
    private boolean liveClock = true;
    private Future<?> future;
    private GPSClock clock;

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
        dispatcher = new SimplePropertySetterDispatcher(/*new WeakMapSet<>()*/);
        nmeaDispatcher = NMEADispatcher.getInstance(NMEADispatcher.class, dispatcher);
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
    
    public PropertySetterDispatcher getDispatcher()
    {
        return dispatcher;
    }

    public void addNMEAObserver(PropertySetter propertySetter)
    {
        addNMEAObserver(propertySetter, propertySetter.getPrefixes());
    }
    public void addNMEAObserver(PropertySetter propertySetter, String... prefixes)
    {
        nmeaDispatcher.addObserver(propertySetter, prefixes);
        if (clock != null)
        {
            propertySetter.set("clock", clock); // supply clock if attached on the fly
        }
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.add(ac);
        }
    }
    
    public void addAISObserver(PropertySetter propertySetter)
    {
        addAISObserver(propertySetter, propertySetter.getPrefixes());
    }
    public void addAISObserver(PropertySetter propertySetter, String... prefixes)
    {
        if (aisDispatcher == null)
        {
            aisDispatcher = AISDispatcher.getInstance(AISDispatcher.class);
        }
        aisDispatcher.addObserver(propertySetter, prefixes);
        if (clock != null)
        {
            propertySetter.set("clock", clock); // supply clock if attached on the fly
        }
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.add(ac);
        }
    }
    
    public void removeNMEAObserver(PropertySetter propertySetter)
    {
        removeNMEAObserver(propertySetter, propertySetter.getPrefixes());
    }
    public void removeNMEAObserver(PropertySetter propertySetter, String... prefixes)
    {
        nmeaDispatcher.removeObserver(propertySetter, prefixes);
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.remove(ac);
        }
    }
    
    public void removeAISObserver(PropertySetter propertySetter)
    {
        removeAISObserver(propertySetter, propertySetter.getPrefixes());
    }
    public void removeAISObserver(PropertySetter propertySetter, String... prefixes)
    {
        aisDispatcher.removeObserver(propertySetter, prefixes);
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.remove(ac);
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
            parser.parse(in, clock, origin, nmeaDispatcher, aisDispatcher);
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
