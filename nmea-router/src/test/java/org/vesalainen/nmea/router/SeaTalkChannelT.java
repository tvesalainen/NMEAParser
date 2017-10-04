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
package org.vesalainen.nmea.router;

import org.vesalainen.nmea.router.seatalk.SeaTalkChannel;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.parsers.nmea.NMEAObserver;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.NMEATracer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeaTalkChannelT
{
    
    public SeaTalkChannelT()
    {
    }

    @Test
    public void test1()
    {
        try
        {
            List<String> ports = SerialChannel.getFreePorts();
            assertTrue(ports.size() >= 1);
            try (SeaTalkChannel stc = new SeaTalkChannel(ports.get(0)))
            {
                NMEAParser parser = NMEAParser.newInstance();
                NMEAObserver obs = NMEATracer.getTracer(System.err);
                parser.parse(stc, obs, null);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    
}
