/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.nmea.util;

import java.util.stream.Stream;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.logging.JavaLogging;

/**
 * Consumer for NMEASample's. Samples are processed in own thread in process
 * method.
 * @author tkv
 */
public abstract class AbstractSampleConsumer extends JavaLogging implements Runnable
{
    protected Stream<NMEASample> stream;
    protected Thread thread;
    /**
     * Creates consumer.
     * @param cls Implementation class for log naming.
     */
    protected AbstractSampleConsumer(Class<? extends AbstractSampleConsumer> cls)
    {
        super(cls);
    }
    /**
     * Starts consumer
     * @param service 
     */
    public void start(NMEAService service)
    {
        init(service.stream(getProperties()));
        thread = new Thread(this, this.getClass().getSimpleName());
        thread.start();
    }
    /**
     * Stops consumer.
     */
    public void stop()
    {
        thread.interrupt();
    }
    /**
     * Initializes sample stream. Default implementation assigns given stream. 
     * Subclasses can put filters, maps and other intermediate methods in place.
     * @param stream 
     */
    protected void init(Stream<NMEASample> stream)
    {
        this.stream = stream;
    }
    /**
     * Returns array of property names we are interested in.
     * @return 
     */
    protected abstract String[] getProperties();
    /**
     * Process samples after intermediate methods in own thread. If processing 
     * takes too much time, some NMEA sentences might be skipped.
     * @param sample 
     */
    protected abstract void process(NMEASample sample);
    
    @Override
    public void run()
    {
        if (stream == null)
        {
            throw new IllegalStateException("stream not initialized");
        }
        stream.forEach(this::process);
    }
    
}
