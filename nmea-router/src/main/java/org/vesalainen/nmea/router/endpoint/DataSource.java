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
package org.vesalainen.nmea.router.endpoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.router.DataSourceMXBean;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class DataSource extends JavaLogging implements Runnable, DataSourceMXBean
{
    private static final Map<String,DataSource> map = new HashMap<>();
    protected final String name;
    protected long readCount;
    protected long readBytes;
    protected long writeCount;
    protected long writeBytes;

    public DataSource(String name)
    {
        this.name = name;
        if (map.containsKey(name))
        {
            throw new IllegalArgumentException(name+" DataSource exists already");
        }
        map.put(name, this);
        setLogger(Logger.getLogger(this.getClass().getName().replace('$', '.') + "." + name));
    }

    public static DataSource get(String name)
    {
        return map.get(name);
    }

    public abstract int write(ByteBuffer readBuffer) throws IOException;

    public abstract int write(RingByteBuffer ring) throws IOException;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public long getReadCount()
    {
        return readCount;
    }

    @Override
    public long getReadBytes()
    {
        return readBytes;
    }

    @Override
    public long getWriteCount()
    {
        return writeCount;
    }

    @Override
    public long getWriteBytes()
    {
        return writeBytes;
    }

}
