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

import org.vesalainen.navi.Navis;
import org.vesalainen.navi.WayPoint;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class NavisTest
{
    private static final double Epsilon = 1e-10;
    public NavisTest()
    {
    }

    @Test
    public void test1()
    {
        WP wp1 = new WP(0, 60, 25);
        WP wp2 = new WP(0, 60, 24);
        assertEquals(270, Navis.bearing(wp1, wp2), Epsilon);
        assertEquals(90, Navis.bearing(wp2, wp1), Epsilon);
    }
    
    @Test
    public void test2()
    {
        WP wp1 = new WP(0, -0.5F, 24);
        WP wp2 = new WP(0, 0.5F, 25);
        assertEquals(45, Navis.bearing(wp1, wp2), Epsilon);
        assertEquals(225, Navis.bearing(wp2, wp1), Epsilon);
    }
    
    @Test
    public void test3()
    {
        WP wp1 = new WP(0, 59.5F, 24);
        WP wp2 = new WP(0, 60.5F, 26);
        assertEquals(45, Navis.bearing(wp1, wp2), Epsilon);
        assertEquals(225, Navis.bearing(wp2, wp1), Epsilon);
    }
    
    private class WP implements WayPoint
    {
        private long time;
        private float latitude;
        private float longitude;

        public WP(long time, float latitude, float longitude)
        {
            this.time = time;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public long getTime()
        {
            return time;
        }

        @Override
        public double getLatitude()
        {
            return latitude;
        }

        @Override
        public double getLongitude()
        {
            return longitude;
        }

        @Override
        public String toString()
        {
            return "WayPoint{" + "time=" + time + ", latitude=" + latitude + ", longitude=" + longitude + '}';
        }
        
    }
}
