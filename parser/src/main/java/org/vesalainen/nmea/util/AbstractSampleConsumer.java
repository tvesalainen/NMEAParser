/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.logging.JavaLogging;

/**
 * Consumer for NMEASample's. Samples are processed in own thread in process
 * method.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractSampleConsumer extends JavaLogging implements Runnable, Stoppable
{
    protected Stream<NMEASample> stream;
    protected ScheduledExecutorService executor;
    private Future<?> future;
    /**
     * Creates consumer.
     * @param cls Implementation class for log naming.
     */
    protected AbstractSampleConsumer(Class<? extends AbstractSampleConsumer> cls, ScheduledExecutorService executor)
    {
        super(cls);
        this.executor = executor;
    }
    /**
     * Starts consumer
     * @param service 
     */
    public void start(NMEAService service)
    {
        init(service.stream(getProperties()));
        future = executor.submit(this);
    }
    /**
     * Stops consumer.
     */
    @Override
    public void stop()
    {
        future.cancel(true);
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
