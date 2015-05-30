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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.comm.channel.SerialChannel.Configuration;
import org.vesalainen.parsers.nmea.NMEAObserver;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.NMEATracer;
import org.vesalainen.parsers.seatalk.SeaTalk2NMEA;
import org.vesalainen.parsers.seatalk.SeaTalk2NMEATest;

/**
 *
 * @author tkv
 */
public class SeaTalkChannelTest
{
    
    public SeaTalkChannelTest()
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
