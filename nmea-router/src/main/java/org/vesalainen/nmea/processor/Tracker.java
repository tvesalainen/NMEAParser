/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.nmea.util.AbstractSampleConsumer;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Stream;
import org.vesalainen.nmea.jaxb.router.TrackerType;
import org.vesalainen.nmea.util.NMEAFilters;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.nmea.util.TrackOutput;
import org.vesalainen.parsers.nmea.NMEAService;

/**
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Tracker extends AbstractSampleConsumer implements AutoCloseable
{
    private static final long SECOND_IN_MILLIS = 1000;
    private static final long MINUTE_IN_MILLIS = 60*SECOND_IN_MILLIS;
    private static final long HOUR_IN_MILLIS = 60*MINUTE_IN_MILLIS;
    private static final long DAY_IN_MILLIS = 24*HOUR_IN_MILLIS;
    private static final String[] PREFIXES = new String[]{
        "latitude",
        "longitude"
            };
    private long nextDayMillis;
    private double bearingTolerance = 3;
    private double minDistance = 0.1;
    private float maxSpeedAcceleration = 1;
    private long maxPassive = TimeUnit.MINUTES.toMillis(15);
    private boolean buffered;
    private File directory;
    private final TrackOutput track;
    private int count;
    private boolean active;
    private ScheduledFuture<?> scheduledfuture;
    /**
     * This is for testing
     * @param out
     * @throws IOException 
     */
    Tracker(String dirStr) throws IOException
    {
        this(dirStr, Executors.newScheduledThreadPool(3));
    }
    Tracker(String dirStr, ScheduledExecutorService executor) throws IOException
    {
        super(Tracker.class, executor);
        this.directory = new File(dirStr);
        track = new TrackOutput(directory)
                .setBuffered(buffered);
    }

    public Tracker(TrackerType trackerType, ScheduledExecutorService executor) throws IOException
    {
        super(Tracker.class, executor);
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
        BigDecimal ms = trackerType.getMaxSpeedAcceleration();
        if (ms != null)
        {
            maxSpeedAcceleration = ms.floatValue();
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
                .setBuffered(buffered);
    }
    
    @Override
    public String[] getProperties()
    {
        return PREFIXES;
    }

    @Override
    public void init(Stream<NMEASample> stream)
    {
        Stream<NMEASample> accFilter = stream
                .filter(NMEAFilters.locationFilter(maxSpeedAcceleration))
                .filter(NMEAFilters.minDistanceFilter(minDistance));
        this.stream = NMEAFilters.bearingToleranceFilter(accFilter, bearingTolerance);
    }

    @Override
    public void start(NMEAService service)
    {
        super.start(service);
        scheduledfuture = executor.scheduleAtFixedRate(this::closer, maxPassive, maxPassive, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop()
    {
        scheduledfuture.cancel(false);
        super.stop();
    }

    @Override
    protected void process(NMEASample sample)
    {
        output(sample.getTime(), sample.getProperty("latitude"), sample.getProperty("longitude"));
    }
    public void output(long time, float latitude, float longitude)
    {
        active = true;
        count++;
        try
        {
            if (nextDayMillis == 0)
            {
                ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
                nextDayMillis = time + DAY_IN_MILLIS - (
                        HOUR_IN_MILLIS*zdt.getHour() +
                        MINUTE_IN_MILLIS*zdt.getMinute() +
                        SECOND_IN_MILLIS*zdt.getSecond()
                        );
            }
            else
            {
                if (nextDayMillis < time)
                {
                    track.close();
                    nextDayMillis = time + DAY_IN_MILLIS;
                }
            }
            track.output(time, latitude, longitude);
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, ex, ex.getMessage());
        }
    }

    int getCount()
    {
        return count;
    }

    @Override
    public void close() throws Exception
    {
        if (track != null)
        {
            track.close();
            count = 0;
        }
    }

    public void closer()
    {
        try
        {
            if (!active)
            {
                close();
            }
            active = false;
        }
        catch (Exception ex)
        {
            log(Level.WARNING, ex, "timed close failed %s", ex.getMessage());
        }
    }
}
