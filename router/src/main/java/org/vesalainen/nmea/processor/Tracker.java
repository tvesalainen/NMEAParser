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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.io.CompressedOutput;
import org.vesalainen.nmea.jaxb.router.TrackerType;
import org.vesalainen.nmea.util.TrackFilter;
import org.vesalainen.nmea.util.TrackLocation;
import org.vesalainen.parsers.nmea.Clock;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 * TODO file rotation & flush
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
    private double bearingTolerance = 3;
    private double minDistance = 0.1;
    private double maxSpeed = 10;
    private long maxPassive = TimeUnit.MINUTES.toMillis(15);
    private File directory;
    private float longitude;
    private float latitude;
    private long lastUpdate;
    private boolean positionUpdated;
    private final ByteBuffer bb = ByteBuffer.allocateDirect(100);
    private final JavaLogging log = new JavaLogging();
    private final Filter filter;
    private final TrackLocation location;
    private CompressedOutput compressor;
    /**
     * This is for testing
     * @param out
     * @throws IOException 
     */
    Tracker(String directory) throws IOException
    {
        this.directory = new File(directory);
        log.setLogger(this.getClass());
        filter = new Filter(bearingTolerance, minDistance, maxSpeed, maxPassive);
        location = new TrackLocation();
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
        this.directory = new File(trackerType.getDirectory());
        filter = new Filter(bearingTolerance, minDistance, maxSpeed, maxPassive);
        location = new TrackLocation();
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
        if (positionUpdated)
        {
            positionUpdated = false;
            log.fine("location %f %f", latitude, longitude);
            try
            {
                filter.input(calendar.getTimeInMillis(), latitude, longitude);
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
        if (compressor != null)
        {
            compressor.close();
        }
    }

    private class Filter extends TrackFilter
    {

        public Filter(double bearingTolerance, double minDistance, double maxSpeed, long maxPassive)
        {
            super(bearingTolerance, minDistance, maxSpeed, maxPassive);
        }

        @Override
        public void output(long time, float latitude, float longitude) throws IOException
        {
            location.time = time;
            location.latitude = latitude;
            location.longitude = longitude;
            compressor.write();
        }

        @Override
        public void open(long time) throws IOException
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String dstr = sdf.format(new Date(time));
            FileOutputStream out = new FileOutputStream(dstr+".trc");
            compressor = new CompressedOutput<>(out, location);
        }

        @Override
        public void close() throws IOException
        {
            compressor.close();
            compressor = null;
        }
        
    }
}
