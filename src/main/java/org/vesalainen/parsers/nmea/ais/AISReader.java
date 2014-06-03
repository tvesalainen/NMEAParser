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
    private Reader in;
    private int cc;
    private int bit;
    
    public AISReader(Reader in)
    {
        this.in = in;
    }

    @Override
    public int read() throws IOException
    {
        if (bit == 0)
        {
            cc = in.read();
            if ((cc >= '0' && cc <= 'W') || (cc >= '`' && cc <= 'w'))
            {
                if (cc == -1)
                {
                    throw new IOException("unexpected EOF in AIS Data");
                }
                cc -= '0';
                if (cc > 40)
                {
                    cc -= 8;
                }
                bit = 6;
            }
            else
            {
                if (cc != '\n')
                {
                    throw new IOException(cc+" unexpected");
                }
                return cc;
            }
        }
        bit--;
        if ((cc & (1<<bit)) == 0)
        {
            return '0';
        }
        else
        {
            return '1';
        }
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
        // Because of thread switching, we cannot use bulk reading
        int rc = read();
        if (rc == -1)
        {
            return -1;
        }
        chars[off] = (char) rc;
        return 1;
    }

    @Override
    public void close() throws IOException
    {
        in.close();
    }
}
