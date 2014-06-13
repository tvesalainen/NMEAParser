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
import java.io.Reader;
import org.vesalainen.parser.util.Recoverable;

/**
 * AISReader converts ais content to binary input.
 * @author Timo Vesalainen
 */
public class AISReader extends Reader implements Recoverable
{
    private final Reader in;
    private int cc;
    private int bit;
    private final AISContext context;
    private boolean nextSentence;

    AISReader(Reader in, AISContext context)
    {
        this.in = in;
        this.context = context;
    }

    @Override
    public boolean recover()
    {
        if (in instanceof Recoverable)
        {
            Recoverable recoverable = (Recoverable) in;
            return recoverable.recover();
        }
        return false;
    }

    @Override
    public int read(char[] chars, int off, int len) throws IOException
    {
        if (nextSentence)
        {
            context.switchTo(-1);
            nextSentence = false;
        }
        int lim = off+len;
        for (int ii=off;ii<lim;ii++)
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
                        nextSentence = true;
                        int p = in.read();
                        if (p<'0' || p>'9')
                        {
                            throw new IOException("expected padding, got "+(char)p);
                        }
                        int padding = p-'0';
                        if (context.isLastMessage())
                        {
                            chars[ii-padding] = '\n';
                            return ii-padding+1;
                        }
                        else
                        {
                            return ii-padding;
                        }
                    }
                    else
                    {
                        throw new IOException("expected (,) got "+(char)cc);
                    }
                }
            }
            bit--;
            if ((cc & (1<<bit)) == 0)
            {
                chars[ii] = '0';
            }
            else
            {
                chars[ii] = '1';
            }
        }
        return lim;
    }

    @Override
    public void close() throws IOException
    {
        in.close();
    }
}
