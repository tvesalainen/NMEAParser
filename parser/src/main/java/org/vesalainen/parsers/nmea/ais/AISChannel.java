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
import static org.vesalainen.parsers.nmea.ais.ThreadMessage.*;
import org.vesalainen.regex.SyntaxErrorException;

/**
 * AISChannel converts ais content to binary input.
 * @author Timo Vesalainen
 */
public class AISChannel implements ScatteringByteChannel
{
    private static final byte CommitC = (byte)'C';
    private static final byte RollbackC = (byte)'R';
    
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
    public void close() throws IOException
    {
        in.close();
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        int count = 0;
        for (int ii=offset;ii<length;ii++)
        {
            ByteBuffer bb = dsts[ii];
            while (bb.hasRemaining())
            {
                if (underflow)
                {
                    ThreadMessage rc = context.join(); // wait for nmea thread
                    switch (rc)
                    {
                        case Commit:
                            bb.put(CommitC);
                            context.fork(-1, Go);
                            return 1;
                        case Rollback:
                            bb.put(RollbackC);
                            context.fork(-1, Go);
                            return 1;
                    }
                    underflow = false;
                }
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
                        try
                        {
                            underflow = true;
                            if (cc == ',')
                            {
                                int p = in.read();
                                if (p<'0' || p>'5')
                                {
                                    bb.put(RollbackC);
                                    return count+1;
                                    //throw new SyntaxErrorException("expected padding, got "+(char)p);
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
                            else
                            {
                                bb.put(RollbackC);
                                return count+1;
                                //throw new SyntaxErrorException("expected ',' got '"+(char)cc+"'");
                            }
                        }
                        finally
                        {
                            context.fork(-1, Go);   // let nmea thread run
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
