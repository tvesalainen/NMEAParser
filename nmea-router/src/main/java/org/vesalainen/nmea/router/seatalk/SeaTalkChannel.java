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
package org.vesalainen.nmea.router.seatalk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Logger;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.comm.channel.SerialChannel.Configuration;
import org.vesalainen.nio.RingBuffer;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.ByteBufferOutputStream;
import org.vesalainen.parsers.seatalk.SeaTalk2NMEA;
import org.vesalainen.util.OrMatcher;
import org.vesalainen.util.SimpleMatcher;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeaTalkChannel extends SelectableChannel implements ScatteringByteChannel, GatheringByteChannel
{
    private final SerialChannel channel;
    private final RingByteBuffer readRing = new RingByteBuffer(100, true);
    private final ByteBufferOutputStream out = new ByteBufferOutputStream();
    private final SeaTalkMatcher matcher = new SeaTalkMatcher();
    private final SeaTalk2NMEA parser = SeaTalk2NMEA.newInstance();
    private boolean mark = true;
    private ByteBuffer writeBuffer;
    private boolean matched;
    private int lamp = -1;
    private String proprietaryPrefix;
    private final OrMatcher nmeaMatcher = new OrMatcher();
    private final JavaLogging log = new JavaLogging();

    public SeaTalkChannel(String port) throws IOException
    {
        Builder builder = new Builder(port, SerialChannel.Speed.B4800)
                .setParity(SerialChannel.Parity.SPACE)
                .setReplaceError(true);
        this.channel = builder.get();
        log.setLogger(Logger.getLogger(this.getClass().getName()));
    }

    public SeaTalkChannel(SerialChannel channel)
    {
        this.channel = channel;
        log.setLogger(Logger.getLogger(this.getClass().getName()));
    }

    public void setProprietaryPrefix(String proprietaryPrefix)
    {
        this.proprietaryPrefix = proprietaryPrefix;
        nmeaMatcher.add(new SimpleMatcher("$P"+proprietaryPrefix+",ST,LAMP,?*\n"));
    }
    
    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
                    log.finer("lamp=%d", lamp);
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
            log.finer("wrote lamp=%s", lamp);
            lamp = -1;
        }
    }
    
    private int read() throws IOException
    {
        int remaining = out.getRemaining();
        log.finest("out remaining = %d", remaining);
        readRing.fill(channel);
        log.finest("ring remaining = %d", readRing.remaining());
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
                    log.finest("drop: '%1$c' %1$d 0x%1$02X %2$s", b & 0xff, (RingBuffer)readRing);
                    mark = true;
                    break;
                case Match:
                    if (canWrite)
                    {
                        write();
                    }
                    log.finer("read: %s", readRing);
                    parser.parse(readRing, out);
                    mark = true;
                    matched = true;
                    break;
            }
        }
        int res = remaining - out.getRemaining();
        log.finest("read= %d", res);
        return res;
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
        log.info("register(%s)", sel);
        return channel.register(sel, ops, att);
    }

    @Override
    public SelectableChannel configureBlocking(boolean block) throws IOException
    {
        log.info("configureBlocking(%b)", block);
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
        log.info("close");
        channel.close();
    }

    @Override
    public String toString()
    {
        return "SeaTalkChannel{" + "channel=" + channel + '}';
    }

}
