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

import org.vesalainen.navi.WayPoint;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import static org.vesalainen.navi.Navis.*;
import org.vesalainen.util.logging.JavaLogging;

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
    private WP last;
    private final List<WP> buffer = new ArrayList<>();
    private final Deque<WP> pool = new ArrayDeque<>();
    private boolean open;
    private long active;
    protected final JavaLogging log = new JavaLogging();

    public TrackFilter()
    {
        log.setLogger(this.getClass());
    }

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
        WP wp = create(time, latitude, longitude);
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
                    log.warning("%s skipped because of speed (1)", buffer.get(0));
                    log.warning("%s", wp);
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
                        log.warning("%s skipped because of speed (2)", buffer.get(0));
                        log.warning("%s", wp);
                        buffer.clear();
                    }
                    else
                    {
                        log.warning("%s skipped because of speed (3)", buffer.get(1));
                        log.warning("%s", wp);
                        recycle(wp);
                        recycle(buffer);
                    }
                }
                break;
        }
    }
    private void doInput(WP wp) throws IOException
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
                        output(last.time, last.latitude, last.longitude);
                    }
                    recycle(last);
                    last = wp;
                    lastBearing = bearing;
                    output(wp.time, wp.latitude, wp.longitude);
                    active = wp.time;
                }
                else
                {
                    if (log.isLoggable(Level.FINEST))
                    {
                        if (Math.abs(bearing-lastBearing) <= bearingTolerance)
                        {
                            log.finest("%s skipped because of bearing", wp);
                        }
                        if (distance <= minDistance)
                        {
                            log.finest("%s skipped because of distance", wp);
                        }
                    }
                    if (open && distance < minDistance && (active + maxPassive) < wp.time)
                    {
                        output(wp.time, wp.latitude, wp.longitude);
                        close();
                    }
                    recycle(wp);
                }
            }
        }
    }

    private WP create(long time, float latitude, float longitude)
    {
        WP wp;
        if (pool.isEmpty())
        {
            wp = new WP(time, latitude, longitude);
        }
        else
        {
            wp = pool.pop();
            wp.time = time;
            wp.latitude = latitude;
            wp.longitude = longitude;
        }
        return wp;
    }
    private void recycle(WP wp)
    {
        assert(!pool.contains(wp));
        pool.add(wp);
    }
    private void recycle(List<WP> buf)
    {
        for (WP wp : buf)
        {
            recycle(wp);
        }
        buf.clear();
    }
    protected void flush()
    {

    }
    protected void open(long time) throws IOException
    {
        open = true;
    }
    public void close() throws IOException
    {
        open = false;
    }
    protected abstract void output(long time, float latitude, float longitude) throws IOException;

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
