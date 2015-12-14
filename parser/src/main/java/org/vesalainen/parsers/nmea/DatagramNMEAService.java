/*
 * Copyright (C) 2015 tkv
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
import java.nio.channels.DatagramChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;

/**
 *
 * @author tkv
 */
public class DatagramNMEAService extends NMEAService
{
    private static DatagramNMEAService service;
    private boolean started;
    
    private DatagramNMEAService()
    {
    }

    private DatagramNMEAService(DatagramChannel channel) throws IOException
    {
        super(channel);
    }

    private DatagramNMEAService(ScatteringByteChannel in, GatheringByteChannel out) throws IOException
    {
        super(in, out);
    }

    public static DatagramNMEAService getInstance()
    {
        if (service == null)
        {
            service = new DatagramNMEAService();
        }
        return service;
    }
    @Override
    public void stop()
    {
        try
        {
            super.stop();
            in.close();
            in = null;
            out = null;
            started = false;
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public void start()
    {
        try
        {
            UnconnectedDatagramChannel channel = UnconnectedDatagramChannel.open("224.0.0.3", 10110, 100, true, false);
            in = channel;
            out = channel;
            
            super.start();
            started = true;
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public boolean isStarted()
    {
        return started;
    }

}
