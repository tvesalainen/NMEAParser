/*
 * Copyright (C) 2012 Timo Vesalainen
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
import java.nio.channels.ScatteringByteChannel;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parser.util.Recoverable;
import org.vesalainen.regex.SyntaxErrorException;

/**
 * AISChannel converts ais content to binary input.
 * @author Timo Vesalainen
 */
public class AISChannel implements ScatteringByteChannel, Recoverable
{
    private final InputReader in;
    private int cc;
    private int bit;
    private final AISContext context;
    private boolean underflow;

    AISChannel(InputReader in, AISContext context)
    {
        this.in = in;
        this.context = context;
    }

    @Override
    public boolean recover(String msg, String source, int line, int column) throws IOException
    {
        try
        {
            context.afterSyntaxError(msg);
            if (!underflow)
            {
                int c = in.read();
                while (c != ',')
                {
                    c = in.read();
                }
                c = in.read();  // padding
            }
            underflow = true;
            return true;
        }
        finally
        {
            context.fork(-1);   // let nmea thread run
        }
    }

    @Override
    public void close() throws IOException
    {
        in.close();
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        if (underflow)
        {
            context.join();// wait for nmea thread
            if (context.isEnded() && context.isCommitted())
            {
                try
                {
                    ByteBuffer bb = dsts[offset];
                    bb.put((byte)'C');
                    return 1;
                }
                finally
                {
                    context.fork(-1);   // let nmea thread run
                }
            }
            underflow = false;
        }
        if (context.isEnded() && !context.isCommitted())
        {
            throw new SyntaxErrorException("NMEA failure");
        }
        int count = 0;
        for (int ii=offset;ii<length;ii++)
        {
            ByteBuffer bb = dsts[ii];
            while (bb.hasRemaining())
            {
                if (bit == 0)
                {
                    cc = in.read();
                    if ((cc >= '0' && cc <= 'W') || (cc >= '`' && cc <= 'w'))
                    {
                        cc -= '0';
                        if (cc > 40)
                        {
                            cc -= 8;
                        }
                        bit = 6;
                    }
                    else
                    {
                        if (cc == ',')
                        {
                            underflow = true;
                            try
                            {
                                int p = in.read();
                                if (p<'0' || p>'5')
                                {
                                    throw new IOException("expected padding, got "+(char)p);
                                }
                                int padding = p-'0';
                                if (padding <= bb.position())
                                {
                                    bb.position(bb.position()-padding);
                                }
                                else
                                {
                                    padding -= bb.position();
                                    bb = dsts[ii-1];
                                    bb.position(bb.position()-padding);
                                }
                                return count-padding;
                            }
                            finally
                            {
                                context.fork(-1);   // let nmea thread run
                            }
                        }
                        else
                        {
                            throw new SyntaxErrorException("expected ',' got '"+(char)cc+"'");
                        }
                    }
                }
                bit--;
                if ((cc & (1<<bit)) == 0)
                {
                    bb.put((byte)'0');
                }
                else
                {
                    bb.put((byte)'1');
                }
                count++;
            }
        }
        return count;
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException
    {
        return read(dsts, 0, dsts.length);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        return (int) read(new ByteBuffer[] {dst});
    }

    @Override
    public boolean isOpen()
    {
        return true;
    }
}
