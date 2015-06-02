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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.channels.ByteBufferOutputStream;
import org.vesalainen.parsers.seatalk.SeaTalk2NMEA;

/**
 *
 * @author tkv
 */
public class SeaTalkChannel extends SelectableChannel implements ScatteringByteChannel
{
    private final SerialChannel channel;
    private final RingByteBuffer ring = new RingByteBuffer(100, true);
    private final ByteBufferOutputStream out = new ByteBufferOutputStream();
    private final SeaTalkMatcher matcher = new SeaTalkMatcher();
    private final SeaTalk2NMEA parser = SeaTalk2NMEA.newInstance();
    private boolean mark = true;

    public SeaTalkChannel(String port) throws IOException
    {
        Builder builder = new Builder(port, SerialChannel.Speed.B4800)
                .setParity(SerialChannel.Parity.SPACE)
                .setReplaceError(true);
        this.channel = builder.get();
    }

    public SeaTalkChannel(SerialChannel channel)
    {
        this.channel = channel;
    }

    private int read() throws IOException
    {
        int remaining = out.getRemaining();
        System.err.println("seatalk "+remaining+" "+out.getRemaining());
        ring.read(channel);
        while (ring.hasRemaining() && out.getRemaining() >= 80)
        {
            byte b = ring.get(mark);
            System.err.print(String.format("%02X ", b));
            switch (matcher.match(b))
            {
                case Ok:
                    mark = false;
                    break;
                case Error:
                    mark = true;
                    break;
                case Match:
                    System.err.println();
                    parser.parse(ring, out);
                    mark = true;
                    break;
            }
        }
        System.err.println();
        return remaining - out.getRemaining();
    }
    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        out.set(dst);
        return read();
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        out.set(dsts, offset, length);
        return read();
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException
    {
        out.set(dsts);
        return read();
    }

    @Override
    public int validOps()
    {
        return channel.validOps();
    }

    @Override
    public SelectorProvider provider()
    {
        return channel.provider();
    }

    @Override
    public boolean isRegistered()
    {
        return channel.isRegistered();
    }

    @Override
    public SelectionKey keyFor(Selector sel)
    {
        return channel.keyFor(sel);
    }

    @Override
    public SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException
    {
        return channel.register(sel, ops, att);
    }

    @Override
    public SelectableChannel configureBlocking(boolean block) throws IOException
    {
        return channel.configureBlocking(block);
    }

    @Override
    public boolean isBlocking()
    {
        return channel.isBlocking();
    }

    @Override
    public Object blockingLock()
    {
        return channel.blockingLock();
    }

    @Override
    protected void implCloseChannel() throws IOException
    {
        channel.close();
    }

    @Override
    public String toString()
    {
        return "SeaTalkChannel{" + "channel=" + channel + '}';
    }
    
}
