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
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.SelectorProvider;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.comm.channel.SerialChannel.Configuration;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.channels.ByteBufferOutputStream;
import org.vesalainen.parsers.seatalk.SeaTalk2NMEA;
import org.vesalainen.util.OrMatcher;
import org.vesalainen.util.SimpleMatcher;

/**
 *
 * @author tkv
 */
public class SeaTalkChannel extends SelectableChannel implements ScatteringByteChannel, WritableByteChannel
{
    private final SerialChannel channel;
    private final RingByteBuffer readRing = new RingByteBuffer(100, true);
    private final RingByteBuffer writeRing = new RingByteBuffer(100, true);
    private final ByteBufferOutputStream out = new ByteBufferOutputStream();
    private final SeaTalkMatcher matcher = new SeaTalkMatcher();
    private final SeaTalk2NMEA parser = SeaTalk2NMEA.newInstance();
    private boolean mark = true;
    private ByteBuffer writeBuffer;
    private boolean matched;
    private int lamp = -1;
    private String proprietaryPrefix;
    private final OrMatcher nmeaMatcher = new OrMatcher();

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

    public void setProprietaryPrefix(String proprietaryPrefix)
    {
        this.proprietaryPrefix = proprietaryPrefix;
        nmeaMatcher.add(new SimpleMatcher("$P"+proprietaryPrefix+",ST,LAMP,?*\n"));
    }
    
    @Override
    public int write(ByteBuffer src) throws IOException
    {
        int res = src.remaining();
        nmeaMatcher.clear();
        while (src.hasRemaining())
        {
            byte b = src.get();
            switch (nmeaMatcher.match(b))
            {
                case Error:
                    return res;
                case Ok:
                case WillMatch:
                    break;
                case Match:
                    lamp = src.get(11+proprietaryPrefix.length())-'0';
                    return res;
            }
        }
        return res;
    }

    private void write() throws IOException
    {
        if (matched && lamp != -1)
        {
            if (writeBuffer == null)
            {
                writeBuffer = ByteBuffer.allocateDirect(8);
            }
            Configuration configuration = channel.getConfiguration();
            configuration.setParity(SerialChannel.Parity.MARK);
            channel.configure(configuration);
            writeBuffer.clear();
            writeBuffer.put((byte)0x30);
            writeBuffer.flip();
            channel.write(writeBuffer);
            configuration.setParity(SerialChannel.Parity.SPACE);
            channel.configure(configuration);
            writeBuffer.clear();
            writeBuffer.put((byte)0x00);
            switch (lamp)
            {
                case 0:
                    writeBuffer.put((byte)0x00);
                    break;
                case 1:
                    writeBuffer.put((byte)0x04);
                    break;
                case 2:
                    writeBuffer.put((byte)0x08);
                    break;
                case 3:
                    writeBuffer.put((byte)0x0C);
                    break;
            }
            writeBuffer.flip();
            channel.write(writeBuffer);
            lamp = -1;
        }
    }
    
    private int read() throws IOException
    {
        int remaining = out.getRemaining();
        readRing.read(channel);
        boolean canWrite = false;
        if (!readRing.isFull())
        {
            canWrite = true;
        }
        while (readRing.hasRemaining() && out.getRemaining() >= 80)
        {
            byte b = readRing.get(mark);
            switch (matcher.match(b))
            {
                case Ok:
                    mark = false;
                    break;
                case Error:
                    mark = true;
                    break;
                case Match:
                    if (canWrite)
                    {
                        write();
                    }
                    parser.parse(readRing, out);
                    mark = true;
                    matched = true;
                    break;
            }
        }
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
