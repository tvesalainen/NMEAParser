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
public class AISContext implements Runnable
{
    private Thread thread;
    private SwitchingInputStream switchingInputStream;
    private AISObserver aisData;
    private AISParser aisParser;
    private AISInputStream aisInputStream;

    public AISContext(InputStream is, AISObserver aisData)
    {
        switchingInputStream = new SwitchingInputStream(is);
        this.aisData = aisData;
    }
    
    public void ensureStarted(boolean ownMessage)
    {
        if (thread == null)
        {
            thread = new Thread(this, "AIS Parser");
            thread.start();
        }
        aisData.setOwnMessage(ownMessage);
    }
    
    public SwitchingInputStream getSwitchingInputStream()
    {
        return switchingInputStream;
    }

    public AISObserver getAisData()
    {
        return aisData;
    }

    public void reStart() throws IOException
    {
        if (aisInputStream != null)
        {
            aisInputStream.reStart();
        }
    }
    @Override
    public void run()
    {
        try
        {
            aisParser = AISParser.newInstance();
            aisInputStream = new AISInputStream(switchingInputStream);
            aisParser.parse(aisInputStream, aisData);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void stop()
    {
        if (thread != null)
        {
            thread.interrupt();
        }
    }
    
}
