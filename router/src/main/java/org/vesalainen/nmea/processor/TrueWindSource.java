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
package org.vesalainen.nmea.processor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.zip.CheckedOutputStream;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.nio.channels.ByteBufferOutputStream;
import org.vesalainen.nmea.jaxb.router.TrueWindSourceType;
import org.vesalainen.nmea.util.Navis;
import org.vesalainen.nmea.util.TrueWind;
import org.vesalainen.nmea.util.WayPoint;
import org.vesalainen.parsers.nmea.Clock;
import org.vesalainen.parsers.nmea.Converter;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.parsers.nmea.NMEAGen;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.navi.Velocity;

/**
 *
 * @author tkv
 */
public class TrueWindSource extends AbstractPropertySetter implements Transactional
{
    private static final String[] Prefixes = new String[]{
        "relativeWindAngle",
        "windSpeed",
        "latitude",
        "longitude",
        "clock"
            };
    private final GatheringByteChannel channel;
    private final TrueWind trueWind = new TrueWind();
    private WayPointImpl prev;
    private final WayPointImpl current = new WayPointImpl();
    private boolean relativeUpdated;
    private final ByteBuffer bb = ByteBuffer.allocateDirect(100);
    private final ByteBufferOutputStream out = new ByteBufferOutputStream(bb);
    private final CheckedOutputStream cout = new CheckedOutputStream(out, new NMEAChecksum());
    private final JavaLogging log = new JavaLogging();
    private GregorianCalendar calendar;

    public TrueWindSource(GatheringByteChannel channel, TrueWindSourceType trueWindSourceType)
    {
        log.setLogger(this.getClass());
        this.channel = channel;
    }
    
    @Override
    public String[] getPrefixes()
    {
        return Prefixes;
    }

    @Override
    public void rollback(String reason)
    {
        log.warning("rollback(%s)", reason);
    }

    @Override
    public void commit(String reason)
    {
        if (relativeUpdated)
        {
            relativeUpdated = false;
            current.setTime(calendar.getTimeInMillis());
            if (prev != null)
            {
                bb.clear();
                try
                {
                    trueWind.setBoatSpeed(Navis.speed(prev, current));
                    trueWind.calc();
                    int trueAngle = (int) trueWind.getTrueAngle();
                    float trueSpeed = (float) trueWind.getTrueSpeed();
                    NMEAGen.mwv(cout, trueAngle, trueSpeed, true);
                    bb.flip();
                    channel.write(bb);
                    log.finest("send MWV trueAngle=%d trueSpeed=%f", trueAngle, trueSpeed);
                }
                catch (IOException ex)
                {
                    log.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
            else
            {
                prev = new WayPointImpl();
            }
            prev.copy(current);
        }
    }
    
    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
            case "relativeWindAngle":
                trueWind.setRelativeAngle(arg);
                relativeUpdated = true;
                break;
            case "windSpeed":
                trueWind.setRelativeSpeed(Velocity.toKnots(arg));
                break;
            case "latitude":
                current.setLatitude(arg);
                break;
            case "longitude":
                current.setLongitude(arg);
                break;
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        switch (property)
        {
            case "clock":
                Clock clock = (Clock) arg;
                calendar = clock.getCalendar();
                break;
        }
    }

    private class WayPointImpl implements WayPoint
    {
        private long time;
        private double latitude;
        private double longitude;

        public void copy(WayPoint oth)
        {
            this.time = oth.getTime();
            this.latitude = oth.getLatitude();
            this.longitude = oth.getLongitude();
        }

        @Override
        public long getTime()
        {
            return time;
        }

        public void setTime(long time)
        {
            this.time = time;
        }

        @Override
        public double getLatitude()
        {
            return latitude;
        }

        public void setLatitude(double latitude)
        {
            this.latitude = latitude;
        }

        @Override
        public double getLongitude()
        {
            return longitude;
        }

        public void setLongitude(double longitude)
        {
            this.longitude = longitude;
        }

    }
}
