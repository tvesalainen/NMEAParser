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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import java.util.EnumSet;
import java.util.zip.CheckedOutputStream;
import static org.vesalainen.parser.ParserFeature.SingleThread;
import org.vesalainen.parser.util.Input;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.parsers.seatalk.SeaTalk2NMEA;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeaTalkInputStream extends InputStream
{
    private final SeaTalk2NMEA parser = SeaTalk2NMEA.newInstance();
    private InputReader input;
    private byte[] buffer = new byte[128];
    private int pos;
    private int limit;
    private CheckedOutputStream out;

    public SeaTalkInputStream(InputStream in) throws IOException
    {
        input = Input.getInstance(in, 16, ISO_8859_1, EnumSet.of(SingleThread));
        input.setEof(()->true);
        this.out = new CheckedOutputStream(new BufferOutputStream(), new NMEAChecksum());
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException
    {
        while (pos == limit)
        {
            fill();
        }
        int count = Math.min(len, limit-pos);
        System.arraycopy(buffer, pos, b, off, count);
        pos += count;
        return count;
    }

    @Override
    public synchronized int read() throws IOException
    {
        while (pos == limit)
        {
            fill();
        }
        return buffer[pos++];
    }

    private void fill() throws IOException
    {
        pos = 0;
        limit = 0;
        parser.parse(input, out);
    }
    
    private class BufferOutputStream extends OutputStream
    {

        @Override
        public void write(int b) throws IOException
        {
            buffer[limit++] = (byte) b;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            System.arraycopy(b, off, buffer, limit, len);
            limit += len;
        }
        
    }
}
