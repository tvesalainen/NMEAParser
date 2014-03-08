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
import java.io.InputStream;
import java.util.concurrent.Semaphore;

/**
 * @author Timo Vesalainen
 */
public class SwitchingInputStream extends InputStream
{
    private Semaphore mainSemaphore;
    private Semaphore sideSemaphore;
    private boolean sleeping = true;
    private int count;
    private InputStream is;

    public SwitchingInputStream(InputStream is)
    {
        this.is = is;
        mainSemaphore = new Semaphore(0);
        sideSemaphore = new Semaphore(0);
    }

    public Semaphore getMainSemaphore()
    {
        return mainSemaphore;
    }

    public Semaphore getSideSemaphore()
    {
        return sideSemaphore;
    }

    public void setNumberOfSentences(int count) throws IOException
    {
        this.count = count;
    }

    @Override
    public int read() throws IOException
    {
        try
        {
            if (sleeping)
            {
                sleeping = false;
                sideSemaphore.acquire();
            }
            int cc = is.read();
            if (cc == ',')
            {
                mainSemaphore.release();
                count--;
                if (count == 0)
                {
                    sleeping = true;
                    return '\n';
                }
                // continue sentence
                sideSemaphore.acquire();
                cc = is.read();
            }
            return cc;
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex);
        }
    }
}
