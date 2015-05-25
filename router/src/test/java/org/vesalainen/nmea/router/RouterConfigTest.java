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
package org.vesalainen.nmea.router;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nmea.jaxb.router.BroadcastNMEAType;
import org.vesalainen.nmea.jaxb.router.ChannelType;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.NmeaHsType;
import org.vesalainen.nmea.jaxb.router.NmeaType;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;

/**
 *
 * @author tkv
 */
public class RouterConfigTest
{
    
    public RouterConfigTest()
    {
    }

    @Test
    public void test1()
    {
        try (InputStream is = RouterConfigTest.class.getClassLoader().getResourceAsStream("router.xml");)
        {
            RouterConfig rc = new RouterConfig(is);
            List<ChannelType> channels = rc.getChannels();
            assertNotNull(channels);
            assertEquals(4, channels.size());
            
            ChannelType c1 = channels.get(0);
            EndpointType et = c1.getBroadcastOrBroadcastNmeaOrDatagram();
            assertEquals("SeaTalk", et.getName());
            SeatalkType stt = (SeatalkType) et;
            assertEquals(4800, stt.getSpeed());
            assertEquals(8, stt.getBits());
            assertEquals(1, stt.getStops());
            assertEquals("SPACE", stt.getParity().name());
            List<RouteType> rl1 = c1.getRoute();
            assertEquals(2, rl1.size());
            RouteType r1 = rl1.get(0);
            assertEquals("$??MTW", r1.getPrefix());
            List<String> target = r1.getTarget();
            assertEquals(1, target.size());
            assertEquals("Net", target.get(0));
            
            ChannelType c2 = channels.get(1);
            et = c2.getBroadcastOrBroadcastNmeaOrDatagram();
            assertEquals("Furuno", et.getName());
            NmeaType nt = (NmeaType) et;
            assertEquals(4800, nt.getSpeed());
            assertEquals(8, nt.getBits());
            assertEquals(1, nt.getStops());
            assertEquals("NONE", nt.getParity().name());

            ChannelType c3 = channels.get(2);
            et = c3.getBroadcastOrBroadcastNmeaOrDatagram();
            assertEquals("AIS", et.getName());
            NmeaHsType hst = (NmeaHsType) et;
            assertEquals(38400, hst.getSpeed());
            assertEquals(8, hst.getBits());
            assertEquals(1, hst.getStops());
            assertEquals("NONE", hst.getParity().name());
            
            ChannelType c4 = channels.get(3);
            et = c4.getBroadcastOrBroadcastNmeaOrDatagram();
            assertEquals("Net", et.getName());
            BroadcastNMEAType bnt = (BroadcastNMEAType) et;
            assertEquals("255.255.255.255", bnt.getAddress());
            assertEquals(10110, bnt.getPort());
        }
        catch(IOException | JAXBException ex)
        {
            fail(ex.getMessage());
        }
    }
    
}
