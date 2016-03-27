/*
 * Copyright (C) 2015 tkv
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
import java.nio.channels.DatagramChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.code.BackgroundPropertySetterDispatcher;
import org.vesalainen.code.PropertySetterDispatcher;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.parsers.nmea.ais.AISDispatcher;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class NMEAService extends JavaLogging implements Runnable, AutoCloseable
{
    protected ScatteringByteChannel in;
    protected GatheringByteChannel out;
    private final NMEADispatcher nmeaDispatcher;
    private AISDispatcher aisDispatcher;
    private final List<AutoCloseable> autoCloseables = new ArrayList<>();
    private Thread thread;
    private BackgroundPropertySetterDispatcher dispatcher;

    public NMEAService(String address, int port) throws IOException
    {
        this(UnconnectedDatagramChannel.open(address, port, 100, true, true));
    }

    public NMEAService(UnconnectedDatagramChannel channel) throws IOException
    {
        this(channel, channel);
    }

    public NMEAService(DatagramChannel channel) throws IOException
    {
        this(channel, channel);
    }

    public NMEAService(ScatteringByteChannel in, GatheringByteChannel out) throws IOException
    {
        setLogger(this.getClass());
        this.in = in;
        this.out = out;
        dispatcher = new BackgroundPropertySetterDispatcher(1024);
        nmeaDispatcher = NMEADispatcher.getInstance(NMEADispatcher.class, dispatcher);
    }
    
    public void start()
    {
        dispatcher.start();
        thread = new Thread(this, NMEAService.class.getSimpleName());
        thread.start();
    }
    
    public void stop()
    {
        thread.interrupt();
        dispatcher.stop();
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
            parser.parse(in, nmeaDispatcher, aisDispatcher);
        }
        catch (Exception ex)
        {
            log(Level.SEVERE, "", ex);
        }
        log(Level.SEVERE, "Processor dies!!!");
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
