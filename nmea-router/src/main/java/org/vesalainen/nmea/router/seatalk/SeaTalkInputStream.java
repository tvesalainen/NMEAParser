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
package org.vesalainen.nmea.router.seatalk;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.vesalainen.util.OperatingSystem;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeaTalkInputStream extends FilterInputStream
{
    private enum State {PRE1, PRE2, COM, AT, DAT}; 
    private byte[] buffer = new byte[128];
    private int pos;
    private int limit;
    private OutputStream out;
    private int prefix1 = 0xff;
    private int prefix2;
    public SeaTalkInputStream(InputStream in)
    {
        super(in);
        this.out = new BufferOutputStream();
        switch (OperatingSystem.getOperatingSystem())
        {
            case Windows:
                prefix2 = 0xff;
            case Linux:
                prefix2 = 0x00;
            default:
                throw new UnsupportedOperationException(OperatingSystem.getOperatingSystem()+" not supported");
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (pos == limit)
        {
            fill();
        }
        if (pos == limit)
        {
            return -1;
        }
        int count = Math.min(len, limit-pos);
        System.arraycopy(buffer, pos, b, off, count);
        pos += count;
        return count;
    }

    @Override
    public int read() throws IOException
    {
        if (pos == limit)
        {
            fill();
        }
        if (pos == limit)
        {
            return -1;
        }
        return buffer[pos++];
    }

    private void fill() throws IOException
    {
        State state = State.PRE1;
        int cc = in.read();
        while (cc != 0xff)
        {
            switch (state)
            {
                case PRE1:
                    if (cc == prefix1)
                    {
                        state = State.PRE2;
                    }
                    break;
                case PRE2:
                    if (cc == prefix2)
                    {
                        state = State.COM;
                    }
                    break;
                case COM:
                    switch (cc)
                    {
                        
                    }
            }
            cc = in.read();
        }
    }
    
    private class BufferOutputStream extends OutputStream
    {

        @Override
        public void write(int b) throws IOException
        {
            buffer[limit++] = (byte) b;
        }
        
    }
}
