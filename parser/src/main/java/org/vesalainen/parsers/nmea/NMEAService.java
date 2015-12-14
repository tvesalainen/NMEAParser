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
    private final NMEADispatcher nmeaObserver = NMEADispatcher.getInstance(NMEADispatcher.class);
    private AISDispatcher aisObserver;
    private final List<AutoCloseable> autoCloseables = new ArrayList<>();
    private Thread thread;

    public NMEAService()
    {
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
    }
    
    public void start()
    {
        thread = new Thread(this, NMEAService.class.getSimpleName());
        thread.start();
    }
    
    public void stop()
    {
        thread.interrupt();
    }
    
    public void addNMEAObserver(PropertySetter propertySetter)
    {
        nmeaObserver.addObserver(propertySetter, propertySetter.getPrefixes());
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.add(ac);
        }
    }
    
    public void addAISObserver(PropertySetter propertySetter)
    {
        if (aisObserver == null)
        {
            aisObserver = AISDispatcher.getInstance(AISDispatcher.class);
        }
        aisObserver.addObserver(propertySetter, propertySetter.getPrefixes());
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.add(ac);
        }
    }
    
    public void removeNMEAObserver(PropertySetter propertySetter)
    {
        nmeaObserver.removeObserver(propertySetter, propertySetter.getPrefixes());
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.remove(ac);
        }
    }
    
    public void removeAISObserver(PropertySetter propertySetter)
    {
        aisObserver.removeObserver(propertySetter, propertySetter.getPrefixes());
        if (propertySetter instanceof AutoCloseable)
        {
            AutoCloseable ac = (AutoCloseable) propertySetter;
            autoCloseables.remove(ac);
        }
    }
    
    public boolean hasObservers()
    {
        return nmeaObserver.hasObservers() ||
                (aisObserver != null && aisObserver.hasObservers());
    }
    @Override
    public void run()
    {
        try
        {
            NMEAParser parser = NMEAParser.newInstance();
            parser.parse(in, nmeaObserver, aisObserver);
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
