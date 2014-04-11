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

/**
 * @author Timo Vesalainen
 */
public class AISContext
{
    private SwitchingInputStream switchingInputStream;
    private AISObserver aisData;
    private AISParser aisParser;
    private AISInputStream aisInputStream;
    private InputStream in;
    private AISThread[] threads = new AISThread[0b111111];

    public AISContext(InputStream in, AISObserver aisData) throws IOException
    {
        this.aisData = aisData;
        this.in = in;
        aisParser = AISParser.newInstance();
    }
    
    public AISObserver getAisData()
    {
        return aisData;
    }

    public AISThread getThread(int message)
    {
        AISThread thr = threads[message];
        if (thr == null)
        {
            thr = new AISThread(message);
            threads[message] = thr;
            thr.start();
        }
        return thr;
    }

    public void stopThreads()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public class AISThread implements Runnable
    {
        private Thread thread;
        private int message;

        public AISThread(int message)
        {
            this.message = message;
            switchingInputStream = new SwitchingInputStream(in);
            aisInputStream = new AISInputStream(switchingInputStream);
            thread = new Thread(this, "AIS Thread "+message);
        }
        
        public void start()
        {
            thread.start();
        }
        public void reStart(int numberOfSentences) throws IOException
        {
            switchingInputStream.setNumberOfSentences(numberOfSentences);
        }

        @Override
        public void run()
        {
            aisParser.parse(aisInputStream, aisData);
        }

        public void stop()
        {
            if (thread != null)
            {
                thread.interrupt();
            }
        }

        public void goOn()
        {
            switchingInputStream.getSideSemaphore().release();
            try
            {
                switchingInputStream.getMainSemaphore().acquire();
            }
            catch (InterruptedException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
    }
}
