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
package org.vesalainen.parsers.seatalk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.parsers.nmea.NMEAObserver;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.SimpleStorage;

/**
 *
 * @author tkv
 */
public class SeaTalk2NMEATest
{
    
    public SeaTalk2NMEATest()
    {
    }

    //@Test
    public void test1()
    {
        try
        {
            URL url = SeaTalk2NMEATest.class.getClassLoader().getResource("seatalk");
            Path path = Paths.get(url.toURI());
            FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
            SeaTalk2NMEA s2n = SeaTalk2NMEA.newInstance();
            assertNotNull(s2n);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            WritableByteChannel channel = Channels.newChannel(baos);
            s2n.parse(fc, channel);
            String nmea = baos.toString("US-ASCII");
            String[] nmeas = nmea.split("\n");
            NMEAParser parser = NMEAParser.newInstance();
            for (String s : nmeas)
            {
                System.err.println(s);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(s+"\n", tc, null);
                assertNull(ss.getRollbackReason());
            }
        }
        catch (URISyntaxException | IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    //@Test
    public void testSerial()
    {
        List<String> allPorts = SerialChannel.getFreePorts();
        assertNotNull(allPorts);
        for (String port : allPorts)
        {
            try 
            {
                Builder builder = new Builder(port, 4800)
                        .setReplaceError(true)
                        .setParity(SerialChannel.Parity.SPACE);
                try (SerialChannel sc = builder.get())
                {
                    WritableByteChannel channel = Channels.newChannel(System.err);
                    SeaTalk2NMEA s2n = SeaTalk2NMEA.newInstance();
                    s2n.parse(sc, channel);
                }
            }
            catch (IOException ex)
            {
                fail(ex.getMessage());
            }
        }
    }
    
}
