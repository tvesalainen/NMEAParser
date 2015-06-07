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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 *
 * @author tkv
 */
public class SeaTalk2NMEAT
{
    
    public SeaTalk2NMEAT()
    {
    }

    //@Test
    public void test1()
    {
        try
        {
            URL url = SeaTalk2NMEAT.class.getClassLoader().getResource("seatalk");
            Path path = Paths.get(url.toURI());
            FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
            SeaTalk2NMEA s2n = SeaTalk2NMEA.newInstance();
            assertNotNull(s2n);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            s2n.parse(fc, baos);
            String nmea = baos.toString("US-ASCII");
            String[] nmeas = nmea.split("\n");
            NMEAParser parser = NMEAParser.newInstance();
            for (String s : nmeas)
            {
                System.err.println(s);
                parser.parse(s+"\n", null, null);
            }
        }
        catch (URISyntaxException | IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
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
                    SeaTalk2NMEA s2n = SeaTalk2NMEA.newInstance();
                    s2n.parse(sc, System.err);
                }
            }
            catch (IOException ex)
            {
                fail(ex.getMessage());
            }
        }
    }
    
}
