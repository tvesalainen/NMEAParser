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
package org.vesalainen.nmea.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.io.CompressedInput;
import org.vesalainen.io.PushbackReadable;
import org.vesalainen.util.navi.Degree;
import org.vesalainen.util.navi.Knots;
import org.vesalainen.util.navi.Location;
import org.vesalainen.util.navi.TimeSpan;
import org.vesalainen.util.navi.Velocity;

/**
 *
 * @author tkv
 */
public class TrackLocationTest
{
    
    public TrackLocationTest()
    {
    }

    @Test
    public void test1()
    {
        try (InputStream is = TrackLocationTest.class.getClassLoader().getResourceAsStream("nmea.trc"))
        {
            TrackLocation tl = new TrackLocation();
            CompressedInput<TrackLocation> ci = new CompressedInput<>(is, tl);
            ci.read();
            ci.read();
            
        }
        catch (IOException ex)
        {
            
        }
    }
    @Test
    public void test2()
    {
        long time = System.currentTimeMillis();
        Location loc = new Location(28, -16);
        Velocity kn5 = new Knots(5.0);
        TimeSpan span = new TimeSpan(1000, TimeUnit.MILLISECONDS);
        Degree sw = new Degree(225);
        for (int ii=0;ii<1000;ii++)
        {
            System.err.println(time);
            System.err.println(loc.getLatitude());
            System.err.println(loc.getLongitude());
            loc = loc.move(sw, kn5, span);
            time += 1000;
        }
    }
    
}
