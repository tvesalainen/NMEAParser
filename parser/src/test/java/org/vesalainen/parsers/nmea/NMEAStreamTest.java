/*
 * Copyright (C) 2016 tkv
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

import org.vesalainen.nmea.util.NMEAStream;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nmea.util.NMEAFilters;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.nmea.util.NMEAStream.Builder;
import org.vesalainen.svg.SVGPlotter;
import org.vesalainen.test.DebugHelper;
import org.vesalainen.ui.Plotter;
import org.vesalainen.ui.Plotter.Polyline;
import org.vesalainen.util.stream.Streams;

/**
 *
 * @author tkv
 */
public class NMEAStreamTest
{
    
    public NMEAStreamTest()
    {
    }

    @Test
    public void testBearingToleranceFilter()
    {
        Builder builder = new NMEAStream.Builder();
        builder.addWaypoint(0, 60, 25);
        builder.addWaypoint(10000, 60.1F, 25);
        builder.addWaypoint(20000, 60.2F, 25);
        builder.addWaypoint(30000, 60.3F, 25.1F);
        builder.addWaypoint(40000, 60.4F, 25.2F);
        List<NMEASample> list = NMEAFilters.BearingToleranceFilter(builder.build(), 3.0).collect(Collectors.toList());
        assertEquals(3, list.size());
        assertEquals(0, list.get(0).getTime());
        assertEquals(20000, list.get(1).getTime());
        assertEquals(40000, list.get(2).getTime());
    }
    @Test
    public void testMinDistanceFilter()
    {
        Builder builder = new NMEAStream.Builder();
        builder.addWaypoint(0, 60, 25);
        builder.addWaypoint(10000, 60F, 25);
        builder.addWaypoint(20000, 60F, 25);
        builder.addWaypoint(30000, 60F, 25.1F);
        builder.addWaypoint(40000, 60.4F, 25.2F);
        List<NMEASample> list = builder.build().filter(NMEAFilters.minDistanceFilter(0.01)).collect(Collectors.toList());
        assertEquals(3, list.size());
        assertEquals(0, list.get(0).getTime());
        assertEquals(30000, list.get(1).getTime());
        assertEquals(40000, list.get(2).getTime());
    }
    //@Test
    public void plot()
    {
        long offerTimeout = DebugHelper.guessDebugging() ? 600 : 0;
        long takeTimeout = DebugHelper.guessDebugging() ? 600 : 5;
        SVGPlotter plotter = new SVGPlotter(1000, 1000);
        Polyline pl1 = plotter.polyline(Color.BLACK);
        Polyline pl2 = plotter.polyline(Color.BLUE);
        Polyline pl3 = plotter.polyline(Color.RED);
        
        try(InputStream is = NMEAStreamTest.class.getResourceAsStream("/sample.nmea"))
        {
            Stream<NMEASample> peek = NMEAStream.parse(is, offerTimeout, takeTimeout, TimeUnit.SECONDS, ()->{return "/sample.nmea";}, "latitude", "longitude")
                    .peek((s)->{pl1.lineTo(s.getLongitude(), s.getLatitude());})
                    .filter(NMEAFilters.minDistanceFilter(0.01))
                    .peek((s)->{pl2.lineTo(s.getLongitude(), s.getLatitude());});
            NMEAFilters.BearingToleranceFilter(peek, 3.0)
                    .forEach(Streams.recyclingConsumer((s)->{pl3.lineTo(s.getLongitude(), s.getLatitude());}));
            plotter.drawPolyline(pl1);
            plotter.drawPolyline(pl2);
            plotter.drawPolyline(pl3);
            plotter.plot("sample", "svg");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
}   
