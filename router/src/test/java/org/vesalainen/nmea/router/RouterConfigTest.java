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
            List<EndpointType> endpoints = rc.getEndpoints();
            assertNotNull(endpoints);
            assertEquals(4, endpoints.size());
            
            EndpointType et = endpoints.get(0);
            assertEquals("SeaTalk", et.getName());
            SeatalkType stt = (SeatalkType) et;
            List<RouteType> rl1 = et.getRoute();
            assertEquals(2, rl1.size());
            RouteType r1 = rl1.get(0);
            assertEquals("$??MTW", r1.getPrefix());
            List<String> target = r1.getTarget();
            assertEquals(1, target.size());
            assertEquals("Net", target.get(0));
            
            et = endpoints.get(1);
            assertEquals("Furuno", et.getName());
            NmeaType nt = (NmeaType) et;

            et = endpoints.get(2);
            assertEquals("AIS", et.getName());
            NmeaHsType hst = (NmeaHsType) et;
            
            et = endpoints.get(3);
            assertEquals("Net", et.getName());
            BroadcastNMEAType bnt = (BroadcastNMEAType) et;
        }
        catch(IOException | JAXBException ex)
        {
            fail(ex.getMessage());
        }
    }
    
}
