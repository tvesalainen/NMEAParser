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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;
import java.util.Set;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialSelectorProvider;
import org.vesalainen.nio.RingByteBuffer;

/**
 *
 * @author tkv
 */
public class SeaTalkChannel extends AbstractSelectableChannel implements ScatteringByteChannel
{
    private final SerialChannel channel;
    private final RingByteBuffer ring = new RingByteBuffer(100, true);

    public SeaTalkChannel(SerialChannel channel)
    {
        super(SerialSelectorProvider.provider());
        this.channel = channel;
    }

    private final ByteBuffer[] readArray = new ByteBuffer[1];
    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        readArray[0] = dst;
        return (int) read(readArray);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        return channel.read(dsts, offset, length);
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException
    {
        return read(dsts, 0, dsts.length);
    }

    @Override
    public int validOps()
    {
        return channel.validOps();
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException
    {
        channel.close();
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException
    {
        channel.configureBlocking(block);
    }
    
}
