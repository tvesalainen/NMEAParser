/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.channels.ScatteringByteChannel;
import java.util.Objects;
import java.util.function.Supplier;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.util.Matcher;
import org.vesalainen.util.function.IOConsumer;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEAReader extends JavaLogging
{
    private String name;
    private NMEAMatcher matcher;
    private ScatteringByteChannel channel;
    private int bufferSize;
    private int maxRead;
    private IOConsumer<RingByteBuffer> onOk;
    private IOConsumer<Supplier<byte[]>> onError;

    public NMEAReader(String name, NMEAMatcher matcher, ScatteringByteChannel channel, int bufSize, int maxRead, IOConsumer<RingByteBuffer> onOk, IOConsumer<Supplier<byte[]>> onError)
    {
        super(NMEAReader.class, name);
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(onOk, "onOk");
        Objects.requireNonNull(onError, "onError");
        this.name = name;
        this.matcher = matcher;
        this.channel = channel;
        this.bufferSize = bufSize;
        this.maxRead = maxRead;
        this.onOk = onOk;
        this.onError = onError;
    }
    
    public void read() throws IOException
    {
        boolean mark = true;
        RingByteBuffer ring = new RingByteBuffer(bufferSize, maxRead, true);
        ByteArrayOutputStream errInput = new ByteArrayOutputStream();
        while (true)
        {
            if (ring.isFull())
            {
                severe("%s buffer is too small %d", name, bufferSize);
                throw new BufferUnderflowException();
            }
            int count = ring.read(channel);
            finest("handle %s read %d bytes", name, count);
            if (count == -1)
            {
                throw new EOFException(name);
            }
            Matcher.Status match = null;
            while (ring.hasRemaining())
            {
                byte b = ring.get(mark);
                match = matcher.match(b);
                switch (match)
                {
                    case Error:
                        if (ring.length() > 1)
                        {
                            ring.chars().forEach(errInput::write);
                        }
                        else
                        {
                            errInput.write(b);
                        }
                        mark = true;
                        break;
                    case Ok:
                    case WillMatch:
                        if (mark && errInput.size()> 0)
                        {
                            onError.apply(errInput::toByteArray);
                            errInput.reset();
                        }
                        mark = false;
                        break;
                    case Match:
                        onOk.apply(ring);
                        mark = true;
                        break;
                }
            }
        }
        
    }
}
