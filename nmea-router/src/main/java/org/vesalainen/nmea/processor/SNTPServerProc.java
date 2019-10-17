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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Clock;
import java.util.logging.Level;
import static org.apache.commons.net.ntp.NtpV3Packet.*;
import org.apache.commons.net.ntp.TimeStamp;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.net.sntp.NtpV4Packet;
import org.vesalainen.net.sntp.ReferenceClock;
import org.vesalainen.net.sntp.SNTPServer;
import org.vesalainen.nmea.jaxb.router.SntpServerType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.parsers.nmea.time.GPSClock;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SNTPServerProc extends AnnotatedPropertyStore implements Stoppable
{
    @Property private Clock clock;
    
    private SNTPServer server;
    private final CachedScheduledThreadPool executor;
    private boolean started;
    private long rootDelay;
    private final SntpServerType sntpServerType;
    
    public SNTPServerProc(SntpServerType sntpServerType, CachedScheduledThreadPool executor) throws SocketException, UnknownHostException
    {
        super(MethodHandles.lookup());
        this.sntpServerType = sntpServerType;
        this.executor = executor;
        if (sntpServerType.getRootDelay() != null)
        {
            this.rootDelay = sntpServerType.getRootDelay().longValue();
        }
    }
    
    @Override
    public void start(String reason)
    {
        started = true;
    }

    @Override
    public void commit(String reason)
    {
        if (started && server == null && clock != null && ((NMEAClock)clock).isReady())
        {
            config("SNTPServer started");
            server = new SNTPServer(6, rootDelay, ReferenceClock.GPS, clock, executor);
            if (sntpServerType.getServer() != null)
            {
                server.setServer(sntpServerType.getServer());
            }
            server.start();
        }
    }
    
    @Override
    public void stop()
    {
        if (server != null)
        {
            sntpServerType.setRootDelay(server.offset());
            server.stop();
            server = null;
            config("SNTP stopped");
        }
    }

}
