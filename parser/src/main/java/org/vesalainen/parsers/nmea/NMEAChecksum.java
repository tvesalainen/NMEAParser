/*
 * Copyright (C) 2013 Timo Vesalainen
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

package org.vesalainen.parsers.nmea;

import java.util.zip.Checksum;

/**
 * @author Timo Vesalainen
 */
public class NMEAChecksum implements Checksum
{
    private boolean on;
    private int value;
    @Override
    public void update(int b)
    {
        doUpdate(b);
    }
    private void doUpdate(int b)
    {
        if (b == '*')
        {
            on = false;
        }
        if (on)
        {
            value ^= b;
        }
        if (b == '$' || b == '!')
        {
            value = 0;
            on = true;
        }
    }

    public void update(CharSequence seq)
    {
        int length = seq.length();
        for (int ii=0;ii<length;ii++)
        {
            doUpdate(seq.charAt(ii));
        }
    }
    @Override
    public void update(byte[] b, int off, int len)
    {
        for (int ii=0;ii<len;ii++)
        {
            doUpdate(b[ii+off]);
        }
    }

    @Override
    public long getValue()
    {
        return value;
    }

    @Override
    public void reset()
    {
        value = 0;
    }
    /**
     * Fills 5 byte length array with * checksum and crlf
     * @param arr 
     */
    public void fillSuffix(byte[] arr)
    {
        fillSuffix(arr, 0, arr.length);
    }
    /**
     * Fills 5 byte length array with * checksum and crlf
     * @param arr
     * @param offset
     * @param length 
     */
    public void fillSuffix(byte[] arr, int offset, int length)
    {
        if (length != 5)
        {
            throw new IllegalArgumentException("NMEA suffix length must be 5");
        }
        arr[0] = '*';
        arr[1] = toHex(value>>4);
        arr[2] = toHex(value&0xf);
        arr[3] = '\r';
        arr[4] = '\n';
    }
    /**
     * returns 5 char suffix
     * @return 
     */
    public String getSuffix()
    {
        return "*"+(char)toHex(value>>4)+(char)toHex(value&0xf)+"\r\n";
    }
    private byte toHex(int v)
    {
        if (v < 10)
        {
            return (byte) ('0'+v);
        }
        else
        {
            return (byte) ('A'+v-10);
        }
    }
}
