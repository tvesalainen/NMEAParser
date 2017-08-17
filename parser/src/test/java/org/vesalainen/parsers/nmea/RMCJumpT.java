/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.nmea.util.NMEAFilters;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.nmea.util.NMEAStats;
import org.vesalainen.nmea.util.NMEAStream;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RMCJumpT
{
    //@Test
    public void jumpTest()
    {
        JavaLogging.setConsoleHandler("org.vesalainen.nmea", Level.ALL);
        try(FileInputStream is = new FileInputStream("nmeadataa"))
        {
            Stream<NMEASample> stream = NMEAStream.parse(is, 5, 5, TimeUnit.SECONDS, ()->{return "/sample.nmea";}, "latitude", "longitude")
                .filter(NMEAFilters.locationFilter(1));
            NMEAStats stats = NMEAStats.stats(stream);
            System.err.println(stats);
        }
        catch (IOException ex)
        {
            Logger.getLogger(RMCJumpT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Test
    public void netTest()
    {
        try
        {
            JavaLogging.setConsoleHandler("org.vesalainen.nmea", Level.ALL);
            NMEAService service = new NMEAService("224.0.0.3", 10110);
            service.start();
            List<String> list = NMEAProperties.getInstance().stream(float.class).collect(Collectors.toList());
            Stream<NMEASample> stream = service.stream(list.toArray(new String[list.size()]));
            NMEAStats stats = NMEAStats.stats(stream.limit(1000));
            System.err.println(stats);
            service.stop();
        }
        catch (IOException ex)
        {
            Logger.getLogger(RMCJumpT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
