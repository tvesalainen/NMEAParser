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

import org.vesalainen.nmea.util.AbstractSampleConsumer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Stream;
import org.vesalainen.nmea.jaxb.router.TrackerType;
import org.vesalainen.nmea.util.NMEAFilters;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.nmea.util.TrackOutput;
import org.vesalainen.parsers.nmea.NMEAService;

/**
 * @author tkv
 */
public class Tracker extends AbstractSampleConsumer implements AutoCloseable
{
    private static final long SecondInMillis = 1000;
    private static final long MinuteInMillis = 60*SecondInMillis;
    private static final long HourInMillis = 60*MinuteInMillis;
    private static final long DayInMillis = 24*HourInMillis;
    private static final String[] Prefixes = new String[]{
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
    private Timer timer;
    private WatchDog closer;
    private boolean active;
    /**
     * This is for testing
     * @param out
     * @throws IOException 
     */
    Tracker(String dirStr) throws IOException
    {
        super(Tracker.class);
        this.directory = new File(dirStr);
        track = new TrackOutput(directory)
                .setBuffered(buffered);
    }

    public Tracker(TrackerType trackerType) throws FileNotFoundException, IOException
    {
        super(Tracker.class);
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
        return Prefixes;
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
        closer = new WatchDog();
        timer = new Timer();
        timer.scheduleAtFixedRate(closer, maxPassive, maxPassive);
    }

    @Override
    public void stop()
    {
        timer.cancel();
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
                nextDayMillis = time + DayInMillis - (
                        HourInMillis*zdt.getHour() +
                        MinuteInMillis*zdt.getMinute() +
                        SecondInMillis*zdt.getSecond()
                        );
            }
            else
            {
                if (nextDayMillis < time)
                {
                    track.close();
                    nextDayMillis = time + DayInMillis;
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

    private class WatchDog extends TimerTask
    {

        @Override
        public void run()
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
}
