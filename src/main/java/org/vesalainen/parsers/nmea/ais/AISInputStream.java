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
import java.io.InputStream;

/**
 * AISInputStream converts ais content to binary input.
 * @author Timo Vesalainen
 */
public class AISInputStream extends InputStream
{
    private InputStream in;
    private int cc;
    private int bit;
    
    public AISInputStream(InputStream in)
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
}
