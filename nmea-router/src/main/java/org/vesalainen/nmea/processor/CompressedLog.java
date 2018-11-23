/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.processor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.io.CompressedOutput;
import org.vesalainen.io.ConcurrentCompressedOutput;
import org.vesalainen.nio.PathHelp;
import org.vesalainen.nmea.jaxb.router.CompressedLogType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.logging.AttachedLogger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompressedLog extends AbstractPropertySetter implements AttachedLogger, Stoppable
{
    private final String[] properties;
    private CompressedOutput compressor;
    private Clock clock;
    private final ScheduledFuture<?> future;
    
    public CompressedLog(CompressedLogType type, ScheduledExecutorService executor) throws IOException
    {
        List<String> props = type.getProperties();
        Path dir = Paths.get(type.getDirectory());
        Path file = PathHelp.getTimePath(dir, ".mea");
        Long bufferSize = type.getBufferSize();
        int bufSize = bufferSize != null ? bufferSize.intValue() : 0;
        OutputStream os = Files.newOutputStream(file);
        if (bufSize > 0)
        {
            os = new BufferedOutputStream(os, bufSize);
        }
        compressor = new ConcurrentCompressedOutput(os, CompressedLog.class.getName());
        compressor.addLong("time", false);
        for (String prop : props)
        {
            switch (prop)
            {
                case "latitude":
                case "longitude":
                    compressor.addDouble(prop);
                    break;
                default:
                    compressor.addFloat(prop);
                    break;
            }
        }
        compressor.ready();
        Set<String> pset = new HashSet<>(props);
        pset.add("clock");
        properties = CollectionHelp.toArray(pset, String.class);
        Long updateSeconds = type.getUpdateSeconds();
        long delay = updateSeconds != null ? updateSeconds.longValue() : 1;
        future = executor.scheduleWithFixedDelay(this::update, delay, delay, TimeUnit.SECONDS);
    }

    @Override
    public void stop()
    {
        future.cancel(false);
        try
        {
            compressor.close();
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, ex, "stop %s", ex.getMessage());
        }
    }
    
    private void update()
    {
        try
        {
            compressor.write();
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, ex, "update %s", ex.getMessage());
        }
    }

    @Override
    public void commit(String reason)
    {
        if (clock != null)
        {
            compressor.setLong("time", clock.millis());
        }
    }
    
    @Override
    public void set(String property, Object arg)
    {
        switch (property)
        {
            case "clock":
                clock = (Clock) arg;
                break;
        }
    }

    @Override
    public void set(String property, float arg)
    {
        compressor.setFloat(property, arg);
    }

    @Override
    public void set(String property, double arg)
    {
        compressor.setDouble(property, arg);
    }

    @Override
    public String[] getPrefixes()
    {
        return properties;
    }
    
}
