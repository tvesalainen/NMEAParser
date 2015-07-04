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
package org.vesalainen.nmea.router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.logging.Logger;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public abstract class DataSource extends JavaLogging
{
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
        setLogger(Logger.getLogger(this.getClass().getName().replace('$', '.') + "." + name));
    }

    protected abstract void handle(SelectionKey sk) throws IOException;

    protected abstract int write(ByteBuffer readBuffer) throws IOException;

    protected abstract int write(RingByteBuffer ring) throws IOException;

    protected int writePartial(RingByteBuffer ring) throws IOException
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
    
}
