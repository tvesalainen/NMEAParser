/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.processor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Clock;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import static org.apache.commons.net.ntp.NtpV3Packet.*;
import org.apache.commons.net.ntp.TimeStamp;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.net.sntp.NtpV4Impl;
import org.vesalainen.net.sntp.ReferenceIdentifier;
import org.vesalainen.nmea.jaxb.router.SntpMulticasterType;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 * Experimental
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SNTPMulticaster extends TimerTask implements PropertySetter, Transactional
{
    private static final String[] Prefixes = new String[]{
        "clock"
            };
    private long period = 4000;
    private Timer timer;
    private JavaLogging log = new JavaLogging();
    private Clock clock;
    private final MulticastSocket socket;
    private InetAddress group;
    private final NtpV4Impl ntpMessage;

    public SNTPMulticaster(SntpMulticasterType sntpMulticasterType) throws SocketException, UnknownHostException, IOException
    {
        log.setLogger(this.getClass());
        int pollInterval = 6;
        Integer interval = sntpMulticasterType.getPollInterval();
        if (interval != null)
        {
            pollInterval = interval;
        }
        period = (long) Math.pow(2, pollInterval)*1000;
        socket = new MulticastSocket();
        group = InetAddress.getByName("224.0.1.1");
        socket.joinGroup(group);
        ntpMessage = new NtpV4Impl();
        ntpMessage.setMode(MODE_BROADCAST);
        ntpMessage.setPoll(pollInterval);
        ntpMessage.setPrecision(-20);
        ntpMessage.setStratum(1);
        ntpMessage.setReferenceId(ReferenceIdentifier.GPS);
        
    }
    
    @Override
    public String[] getPrefixes()
    {
        return Prefixes;
    }

    @Override
    public void start(String reason)
    {
    }

    @Override
    public void rollback(String reason)
    {
        log.warning("rollback(%s)", reason);
    }

    @Override
    public void commit(String reason)
    {
        if (timer  == null)
        {
            log.config("SNTPMulticaster started period=%d", period);
            timer = new Timer();
            timer.scheduleAtFixedRate(this, 0, period);
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            long millis = clock.millis();
            TimeStamp timestamp = TimeStamp.getNtpTime(millis);
            ntpMessage.setReferenceTime(timestamp);
            ntpMessage.setTransmitTime(timestamp);
            DatagramPacket datagramPacket = ntpMessage.getDatagramPacket();
            datagramPacket.setAddress(group);
            socket.send(datagramPacket);
        }
        catch (Exception ex)
        {
            log.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public void set(String property, boolean arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, byte arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, char arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, short arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, int arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, long arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
        }
    }

    @Override
    public void set(String property, double arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        switch (property)
        {
            case "clock":
                clock = (Clock) arg;
                break;
        }
    }

}
