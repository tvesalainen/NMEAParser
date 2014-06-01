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

package org.vesalainen.parsers.nmea.ais;

import java.io.IOException;
import java.io.Reader;
import org.vesalainen.parser.util.Recoverable;

/**
 * @author Timo Vesalainen
 */
public class SwitchingReader extends Reader implements Recoverable
{
    private final Reader in;
    private final AISContext context;
    private int count;

    public SwitchingReader(Reader is, AISContext context)
    {
        this.in = is;
        this.context = context;
    }

    public void setNumberOfSentences(int count)
    {
        this.count = count;
    }

    @Override
    public int read() throws IOException
    {
        int cc = in.read();
        if (cc == ',')
        {
            try
            {
                count--;
                if (count == 0)
                {
                    return '\n';
                }
            }
            finally
            {
                context.switchTo(-1);
            }
            // continue sentence
            cc = in.read();
        }
        return cc;
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
    public int read(char[] chars, int i, int i1) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException
    {
        in.close();
    }
}
