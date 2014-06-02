/*
 * Copyright (C) 2014 Timo Vesalainen
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.zip.Checksum;
import org.vesalainen.parser.util.Recoverable;

/**
 *
 * @author Timo Vesalainen
 */
class CheckedReader extends Reader implements Recoverable
{
    private Reader in;
    private Checksum checkSum;

    public CheckedReader(String in, Checksum chcksm)
    {
        this(new StringReader(in), chcksm);
    }

    public CheckedReader(InputStream in, Checksum chcksm)
    {
        this(new AsciiReader(in), chcksm);
    }

    public CheckedReader(Reader in, Checksum chcksm)
    {
        this.in = in;
        this.checkSum = chcksm;
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
    public int read() throws IOException
    {
        int rc = in.read();
        checkSum.update(rc);
        return rc;
    }

    @Override
    public int read(char[] chars, int off, int len) throws IOException
    {
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

    public Checksum getCheckSum()
    {
        return checkSum;
    }
    
}
