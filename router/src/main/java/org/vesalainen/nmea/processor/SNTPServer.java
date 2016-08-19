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
package org.vesalainen.nmea.processor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import static org.apache.commons.net.ntp.NtpV3Packet.*;
import org.apache.commons.net.ntp.TimeStamp;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.net.sntp.NtpV4Impl;
import org.vesalainen.net.sntp.ReferenceIdentifier;
import org.vesalainen.nmea.jaxb.router.SntpServerType;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class SNTPServer implements PropertySetter, Transactional, Runnable
{
    private static final String[] Prefixes = new String[]{
        "clock"
            };
    private final JavaLogging log = new JavaLogging();
    private NMEAClock clock;
    private DatagramSocket socket;
    private NtpV4Impl ntpMessage;
    private Thread thread;

    public SNTPServer(SntpServerType sntpServerType) throws SocketException, UnknownHostException
    {
        log.setLogger(this.getClass());
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
        if (thread == null && clock != null && clock.isCommitted())
        {
            log.config("SNTPServer started");
            thread = new Thread(this);
            thread.start();
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            socket = new DatagramSocket(NTP_PORT    );
            ntpMessage = new NtpV4Impl();
            DatagramPacket datagramPacket = ntpMessage.getDatagramPacket();
            
            while (true)
            {
                socket.receive(datagramPacket);
                log.info("NTP: received %s from %s %d", ntpMessage, datagramPacket.getAddress(), datagramPacket.getPort());
                TimeStamp transmitTimeStamp = ntpMessage.getTransmitTimeStamp();
                ntpMessage.setLeapIndicator(LI_NO_WARNING);
                ntpMessage.setMode(MODE_SERVER);
                ntpMessage.setStratum(1);
                ntpMessage.setPrecision(-20);
                ntpMessage.setRootDelay(0);
                ntpMessage.setRootDispersion(0);
                ntpMessage.setReferenceId(ReferenceIdentifier.GPS);
                ntpMessage.setReferenceTime(TimeStamp.getNtpTime(clock.getZonedDateTime().toEpochSecond()));
                ntpMessage.setOriginateTimeStamp(transmitTimeStamp);
                long time = clock.millis();
                TimeStamp timeStamp = TimeStamp.getNtpTime(time);
                ntpMessage.setReceiveTimeStamp(timeStamp);
                ntpMessage.setTransmitTime(timeStamp);
                socket.send(datagramPacket);
                log.info("NTP: sent %s to %s %d diff=%d", ntpMessage, datagramPacket.getAddress(), datagramPacket.getPort(), time-transmitTimeStamp.getTime());
            }
        }
        catch (Exception ex)
        {
            log.log(Level.SEVERE, ex, "");
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
                clock = (NMEAClock) arg;
                break;
        }
    }

}
