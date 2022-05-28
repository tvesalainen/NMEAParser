/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import static org.vesalainen.math.Stats.min;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PayloadBuilder
{
    private StringBuilder sb = new StringBuilder();
    private int rem;
    private int remBits;
    
    public void add(int bits, int v)
    {
        int m1 = -1<<(bits);
        int m2 = -1<<(bits-1);
        if (((v>=0)&&(v&m1)!=0) || ((v<0)&&(v&m2)!=m2))
        {
            throw new IllegalArgumentException(v+" doesn't fit to "+bits+" bits");
        }
        while (bits > 0)
        {
            int sht = min(6, 6-remBits, bits);
            rem = (rem<<sht) | (v>>>(bits-sht) & -1>>>(32-sht));
            remBits += sht;
            bits -= sht;
            if (remBits == 6)
            {
                sixBit(rem);
                rem = 0;
                remBits = 0;
            }
        }
    }
    public void add(byte[] data, int offset, int length)
    {
        for (int ii=0;ii<length;ii++)
        {
            add(8, data[ii+offset]&0xff);
        }
    }
    public int length()
    {
        return 6*sb.length()+remBits;
    }
    public String build()
    {
        if (remBits > 0)
        {
            rem <<= 6-remBits;
            sixBit(rem);
            rem = 0;
            remBits = 0;
        }
        return sb.toString();
    }
    private void sixBit(int bits)
    {
        char sixBit = (char) (bits + 48);
        if (bits >= 40)
        {
            sixBit += 8;
        }
        sb.append(sixBit);
    }
}
