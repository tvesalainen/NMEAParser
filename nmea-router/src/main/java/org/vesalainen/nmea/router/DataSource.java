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
package org.vesalainen.nmea.router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;
import org.vesalainen.util.WeakMapList;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class DataSource extends JavaLogging
{
    private static final MapList<String,DataSource> map = new HashMapList<>();
    protected final String name;
    protected DataSource attached;
    protected boolean isSink;
    protected boolean isSingleSink;
    protected long readCount;
    protected long readBytes;
    protected long writeCount;
    protected long writeBytes;

    public DataSource(String name)
    {
        this.name = name;
        if (map.contains(name, this))
        {
            throw new IllegalArgumentException(name+" DataSource exists already");
        }
        map.add(name, this);
        setLogger(Logger.getLogger(this.getClass().getName().replace('$', '.') + "." + name));
    }

    public static List<DataSource> get(String name)
    {
        return map.get(name);
    }
    public static DataSource getSingle(String name)
    {
        List<DataSource> list = map.get(name);
        switch (list.size())
        {
            case 0:
                return null;
            case 1:
                return list.get(0);
            default:
                throw new IllegalArgumentException("many data sources");
        }
    }
    protected abstract int read(RingByteBuffer ring) throws IOException;

    protected abstract void handle(SelectionKey sk) throws IOException;

    protected abstract int write(ByteBuffer readBuffer) throws IOException;

    protected abstract int write(RingByteBuffer ring) throws IOException;

    protected int writePartial(RingByteBuffer ring, int lastPosition) throws IOException
    {
        return 0;
    }

    protected int attachedWrite(RingByteBuffer ring) throws IOException
    {
        return 0;
    }

    public boolean isSink()
    {
        return isSink;
    }

    /**
     * Returns true if only one endpoint writes to target and matched
     * input is written to only one target.
     * @return
     */
    protected boolean isSingleSink()
    {
        return isSingleSink;
    }

    protected void attach(DataSource ds)
    {
        if (attached != null)
        {
            throw new BadInputException(name + " is already attached");
        }
        attached = ds;
    }

    protected void detach()
    {
        attached = null;
    }

    protected boolean isAttached()
    {
        return attached != null;
    }

    protected void updateStatus()
    {
    }

    public static Collection<DataSource> getDataSources()
    {
        Set<DataSource> set = new HashSet<>();
        map.values().stream().forEach((l) ->
        {
            set.addAll(l);
        });
        return set;
    }
    
}
