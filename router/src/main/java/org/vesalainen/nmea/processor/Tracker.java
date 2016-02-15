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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.nmea.jaxb.router.TrackerType;
import org.vesalainen.nmea.util.TrackOutput;
import org.vesalainen.parsers.nmea.Clock;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 * @author tkv
 */
public class Tracker implements PropertySetter, Transactional, AutoCloseable
{
    private static final String[] Prefixes = new String[]{
        "latitude",
        "longitude",
        "clock"
            };
    private GregorianCalendar calendar;
    private int dayOfMonth;
    private double bearingTolerance = 3;
    private double minDistance = 0.1;
    private double maxSpeed = 10;
    private long maxPassive = TimeUnit.MINUTES.toMillis(15);
    private boolean buffered;
    private File directory;
    private float longitude;
    private float latitude;
    private boolean positionUpdated;
    private final ByteBuffer bb = ByteBuffer.allocateDirect(100);
    private final JavaLogging log = new JavaLogging();
    private final TrackOutput track;
    /**
     * This is for testing
     * @param out
     * @throws IOException 
     */
    Tracker(String dirStr) throws IOException
    {
        this.directory = new File(dirStr);
        log.setLogger(this.getClass());
        track = new TrackOutput(directory)
                .setBearingTolerance(bearingTolerance)
                .setMinDistance(minDistance)
                .setMaxSpeed(maxSpeed)
                .setMaxPassive(maxPassive)
                .setBuffered(buffered);
    }

    public Tracker(TrackerType trackerType) throws FileNotFoundException, IOException
    {
        log.setLogger(this.getClass());
        Long bt = trackerType.getBearingTolerance();
        if (bt != null)
        {
            bearingTolerance = bt;
        }
        BigDecimal md = trackerType.getMinDistance();
        if (md != null)
        {
            minDistance = md.doubleValue();
        }
        BigDecimal ms = trackerType.getMaxSpeed();
        if (ms != null)
        {
            maxSpeed = ms.doubleValue();
        }
        Long mp = trackerType.getMaxPassive();
        if (mp != null)
        {
            maxPassive = mp.longValue();
        }
        Boolean isBuffered = trackerType.isBuffered();
        if (isBuffered != null)
        {
            buffered = isBuffered;
        }
        this.directory = new File(trackerType.getDirectory());
        track = new TrackOutput(directory)
                .setBearingTolerance(bearingTolerance)
                .setMinDistance(minDistance)
                .setMaxSpeed(maxSpeed)
                .setMaxPassive(maxPassive)
                .setBuffered(buffered);
    }
    
    @Override
    public String[] getPrefixes()
    {
        return Prefixes;
    }

    @Override
    public void start(String reason)
    {
    }

    @Override
    public void rollback(String reason)
    {
        log.warning("rollback(%s)", reason);
    }

    @Override
    public void commit(String reason)
    {
        if (positionUpdated)
        {
            positionUpdated = false;
            log.finest("location %f %f", latitude, longitude);
            try
            {
                if (dayOfMonth == 0)
                {
                    dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                }
                else
                {
                    int dom = calendar.get(Calendar.DAY_OF_MONTH);
                    if (dayOfMonth != dom)
                    {
                        track.close();
                        dayOfMonth = dom;
                    }
                }
                track.input(calendar.getTimeInMillis(), latitude, longitude);
                log.finest("input %d %f %f", calendar.getTimeInMillis(), latitude, longitude);
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
    }
    
    @Override
    public void set(String property, boolean arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, byte arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, char arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, short arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, int arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, long arg)
    {
        switch (property)
        {
            
        }
    }

    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
            case "latitude":
                latitude = arg;
                positionUpdated = true;
                break;
            case "longitude":
                longitude = arg;
                break;
        }
    }

    @Override
    public void set(String property, double arg)
    {
        switch (property)
        {
            
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

    @Override
    public void close() throws Exception
    {
        if (track != null)
        {
            track.close();
        }
    }

}
