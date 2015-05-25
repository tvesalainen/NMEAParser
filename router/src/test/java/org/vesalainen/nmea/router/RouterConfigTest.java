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
import org.vesalainen.nmea.jaxb.router.ChannelType;
import org.vesalainen.nmea.jaxb.router.EndpointType;

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
            assertEquals(3, channels.size());
            ChannelType c1 = channels.get(0);
            EndpointType et = c1.getBroadcastOrBroadcastNmeaOrDatagram();
            assertEquals("SeaTalk", et.getName());
        }
        catch(IOException | JAXBException ex)
        {
            fail(ex.getMessage());
        }
    }
    
}
