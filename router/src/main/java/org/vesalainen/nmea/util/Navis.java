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

/**
 *
 * @author tkv
 */
public class Navis
{
    public static final double departure(WayPoint loc1, WayPoint loc2)
    {
        return Math.cos(Math.toRadians((loc2.getLatitude()+loc1.getLatitude())/2));
    }
    /**
     * Return bearing from wp1 to wp2 in degrees
     * @param wp1
     * @param wp2
     * @return Degrees
     */
    public static final double bearing(WayPoint wp1, WayPoint wp2)
    {
        double dep = departure(wp1, wp2);
        double aa = dep*(wp2.getLongitude()-wp1.getLongitude());
        double bb = wp2.getLatitude()-wp1.getLatitude();
        double dd = Math.atan2(aa, bb);
        if (dd < 0)
        {
            dd += 2*Math.PI;
        }
        return Math.toDegrees(dd);
    }
    /**
     * Return distance between wp1 and wp2 in NM
     * @param wp1
     * @param wp2
     * @return NM
     */
    public static final double distance(WayPoint wp1, WayPoint wp2)
    {
        double dep = departure(wp1, wp2);
        return 60*Math.hypot(
                wp1.getLatitude()-wp2.getLatitude(),
                dep*(wp1.getLongitude()-wp2.getLongitude())
                );
    }
    /**
     * Returns the speed needed to move from wp1 to wp2
     * @param wp1
     * @param wp2
     * @return Kts
     */
    public static final double speed(WayPoint wp1, WayPoint wp2)
    {
        double distance = distance(wp1, wp2);
        double duration = wp2.getTime()-wp1.getTime();
        double hours = duration/3600000.0;
        double speed = distance/hours;
        return speed;
    }
    
}
