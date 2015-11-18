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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 *
 * @author tkv
 */
public abstract class TrackFilter
{
    private double bearingTolerance;
    private double minDistance;
    private double maxSpeed;
    private long maxPassive;
    private double lastBearing = Double.NaN;
    private WayPoint last;
    private final List<WayPoint> buffer = new ArrayList<>();
    private final Deque<WayPoint> pool = new ArrayDeque<>();
    private boolean open;
    private long active;

    public TrackFilter setBearingTolerance(double bearingTolerance)
    {
        this.bearingTolerance = bearingTolerance;
        return this;
    }

    public TrackFilter setMinDistance(double minDistance)
    {
        this.minDistance = minDistance;
        return this;
    }
    /**
     * 
     * @param maxSpeed Knots. If waypoints distance implies greater speed the waypoint is dropped.
     * @return 
     */
    public TrackFilter setMaxSpeed(double maxSpeed)
    {
        this.maxSpeed = maxSpeed;
        return this;
    }

    public TrackFilter setMaxPassive(long maxPassive)
    {
        this.maxPassive = maxPassive;
        return this;
    }

    public void input(long time, float latitude, float longitude) throws IOException
    {
        WayPoint wp = create(time, latitude, longitude);
        switch (buffer.size())
        {
            case 0:
                buffer.add(wp);
                break;
            case 1:
                if (speed(buffer.get(0), wp) <= maxSpeed)
                {
                    doInput(buffer.get(0));
                    doInput(wp);
                    buffer.clear();
                }
                else
                {
                    buffer.add(wp);
                }
                break;
            case 2:
                if (speed(buffer.get(0), wp) <= maxSpeed)
                {
                    doInput(buffer.get(0));
                    doInput(wp);
                    recycle(buffer.get(1));
                    buffer.clear();
                }
                else
                {
                    if (speed(buffer.get(1), wp) <= maxSpeed)
                    {
                        doInput(buffer.get(1));
                        doInput(wp);
                        recycle(buffer.get(0));
                        buffer.clear();
                    }
                    else
                    {
                        recycle(wp);
                        recycle(buffer);
                    }
                }
                break;
        }
    }
    private void doInput(WayPoint wp) throws IOException
    {
        if (last == null)
        {
            last = wp;
        }
        else
        {
            if (Double.isNaN(lastBearing))
            {
                lastBearing = bearing(last, wp);
                recycle(wp);
            }
            else
            {
                double bearing = bearing(last, wp);
                double distance = distance(last, wp);
                if (
                        Math.abs(bearing-lastBearing) > bearingTolerance &&
                        distance > minDistance
                        )
                {
                    if (!open)
                    {
                        open(last.time);
                        open = true;
                        output(last.time, (float)last.latitude, (float)last.longitude);
                    }
                    recycle(last);
                    last = wp;
                    lastBearing = bearing;
                    output(wp.time, (float)wp.latitude, (float)wp.longitude);
                    active = wp.time;
                }
                else
                {
                    if (open && distance < minDistance && (active + maxPassive) < wp.time)
                    {
                        output(wp.time, (float)wp.latitude, (float)wp.longitude);
                        close();
                        open = false;
                    }
                    recycle(wp);
                }
            }
        }
    }

    private WayPoint create(long time, float latitude, float longitude)
    {
        WayPoint wp;
        if (pool.isEmpty())
        {
            wp = new WayPoint();
        }
        else
        {
            wp = pool.pop();
        }
        wp.time = time;
        wp.latitude = latitude;
        wp.longitude = longitude;
        return wp;
    }
    private void recycle(WayPoint wp)
    {
        assert(!pool.contains(wp));
        pool.add(wp);
    }
    private void recycle(List<WayPoint> buf)
    {
        for (WayPoint wp : buf)
        {
            recycle(wp);
        }
        buf.clear();
    }
    protected void flush()
    {

    }
    protected abstract void open(long time) throws IOException;
    public abstract void close() throws IOException;
    protected abstract void output(long time, float latitude, float longitude) throws IOException;

    private static double departure(WayPoint loc1, WayPoint loc2)
    {
        return Math.cos(Math.toRadians((loc2.latitude+loc1.latitude)/2));
    }
    /**
     * Return bearing from wp1 to wp2 in degrees
     * @param wp1
     * @param wp2
     * @return 
     */
    private static double bearing(WayPoint wp1, WayPoint wp2)
    {
        double dep = departure(wp1, wp2);
        double aa = dep*(wp2.longitude-wp1.longitude);
        double bb = wp2.latitude-wp1.latitude;
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
     * @return 
     */
    private static double distance(WayPoint wp1, WayPoint wp2)
    {
        double dep = departure(wp1, wp2);
        return 60*Math.hypot(
                wp1.latitude-wp2.latitude,
                dep*(wp1.longitude-wp2.longitude)
                );
    }
    private static double speed(WayPoint wp1, WayPoint wp2)
    {
        double distance = distance(wp1, wp2);
        double duration = wp2.time-wp1.time;
        double hours = duration/3600000.0;
        double speed = distance/hours;
        return speed;
    }
    
    private class WayPoint
    {
        private long time;
        private float latitude;
        private float longitude;
        
    }
}
