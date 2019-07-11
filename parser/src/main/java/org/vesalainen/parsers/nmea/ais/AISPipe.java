/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea.ais;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISPipe implements ScatteringByteChannel
{
    private static final byte COMMIT_MARK = (byte)'C';
    private static final byte ROLLBACK_MARK = (byte)'R';
    private ByteBuffer bb;
    private SinkChannel sink;
    private SourceChannel source;

    public AISPipe() throws IOException
    {
        bb = ByteBuffer.allocate(1024);
        Pipe pipe = Pipe.open();
        this.sink = pipe.sink();
        this.source = pipe.source();
    }
    public void start(boolean ownMessage, byte channel) throws IOException
    {
        bb.clear();
        bb.put((byte) (ownMessage ? 'O' : 'M'));
        bb.put(channel);
        bb.flip();
        sink.write(bb);
    }
    public void commit() throws IOException
    {
        bb.clear();
        bb.put(COMMIT_MARK);
        bb.flip();
        sink.write(bb);
    }
    public void rollback() throws IOException
    {
        bb.clear();
        bb.put(ROLLBACK_MARK);
        bb.flip();
        sink.write(bb);
    }
    public void add(CharSequence data, int padding) throws IOException
    {
        bb.clear();
        int length = data.length();
        for (int ii=0;ii<length;ii++)
        {
            int cc = data.charAt(ii);
            if ((cc >= '0' && cc <= 'W') || (cc >= '`' && cc <= 'w'))
            {
                cc -= '0';
                if (cc > 40)
                {
                    cc -= 8;
                }
            }
            else
            {
                throw new IllegalArgumentException(data.toString());
            }
            for (int bit=5;bit>=0;bit--)
            {
                if ((cc & (1<<bit)) == 0)
                {
                    bb.put((byte)'0');
                }
                else
                {
                    bb.put((byte)'1');
                }
            }
        }
        bb.position(bb.position()-padding);
        bb.flip();
        while (bb.hasRemaining())
        {
            sink.write(bb);
        }
    }
    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        return source.read(dsts, offset, length);
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException
    {
        return source.read(dsts);
    }

    @Override
    public final void close() throws IOException
    {
        source.close();
    }

    @Override
    public final boolean isOpen()
    {
        return source.isOpen();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        return source.read(dst);
    }
    
}
