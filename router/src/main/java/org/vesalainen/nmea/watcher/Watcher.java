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
package org.vesalainen.nmea.watcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import static java.net.StandardSocketOptions.SO_BROADCAST;
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import java.nio.channels.Channels;
import java.nio.channels.DatagramChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.parsers.nmea.AbstractNMEAObserver;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 *
 * @author tkv
 */
public class Watcher extends AbstractNMEAObserver
{
    private float trackMadeGood;
    private float speedOverGround;
    private float longitude;
    private float latitude;
    private String message;

    public Watcher() throws IOException
    {
    }
    private void run() throws IOException
    {
        try (UnconnectedDatagramChannel channel = UnconnectedDatagramChannel.open("255.255.255.255", 10110))
        {
            NMEAParser parser = NMEAParser.newInstance();
            InputStream is = Channels.newInputStream(channel);
            parser.parse(is, this, null);
        }
    }

    @Override
    public void commit(String reason)
    {
        System.err.println();
    }

    @Override
    public void setMessage(String message)
    {
        this.message = message;
    }

    @Override
    public void setTrackMadeGood(float trackMadeGood)
    {
        this.trackMadeGood = trackMadeGood;
    }

    @Override
    public void setSpeedOverGround(float speedOverGround)
    {
        this.speedOverGround = speedOverGround;
    }

    @Override
    public void setLongitude(float longitude)
    {
        this.longitude = longitude;
    }

    @Override
    public void setLatitude(float latitude)
    {
        this.latitude = latitude;
    }
    
    public static void main(String... args)
    {
        try
        {
            Watcher watcher = new Watcher();
            watcher.run();
        }
        catch (IOException ex)
        {
            Logger.getLogger(Watcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
