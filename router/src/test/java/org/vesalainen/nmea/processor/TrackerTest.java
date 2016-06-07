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

import org.vesalainen.parsers.nmea.NMEADispatcher;
import java.io.File;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.gpx.GPX;
import org.vesalainen.gpx.TrackHandler;
import org.vesalainen.nio.channels.ReadableByteChannelFactory;
import org.vesalainen.nmea.util.TrackInput;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.time.GPSClock;

/**
 *
 * @author tkv
 */
public class TrackerTest
{
    static final float Epsilon = 1e-5F;
    public TrackerTest()
    {
    }

    @Test
    public void test1()
    {
        try
        {
            NMEADispatcher observer = NMEADispatcher.getInstance(NMEADispatcher.class);
            File file = new File("../parser/src/test/resources/sample.nmea");
            try (ReadableByteChannel rbc = ReadableByteChannelFactory.getInstance(file);
                    Tracker tracker = new Tracker(".");)
            {
                observer.addObserver(tracker, tracker.getPrefixes());
                NMEAParser parser = NMEAParser.newInstance();
                parser.parse(rbc, observer, null);
            }
            catch (IOException ex)
            {
                fail(ex.getMessage());
                Logger.getLogger(TrackerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (TrackInput trackInput = new TrackInput("20100515070534.trc"))
            {
                boolean rc = trackInput.read();
                assertTrue(rc);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                assertEquals("20100515070534", sdf.format(new Date(trackInput.getTime())));
                assertEquals(toFloat(6009.2031), trackInput.getLatitude(), Epsilon);
                assertEquals(toFloat(2453.6519), trackInput.getLongitude(), Epsilon);
                while (trackInput.read())
                {
                    
                }
                //assertEquals("20100515120656", sdf.format(new Date(trackInput.getTime())));   TODO
                assertEquals(toFloat(6009.2038), trackInput.getLatitude(), Epsilon);
                assertEquals(toFloat(2453.6586), trackInput.getLongitude(), Epsilon);
            }
            catch (IOException ex)
            {
                fail(ex.getMessage());
                Logger.getLogger(TrackerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
            Logger.getLogger(TrackerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void test2()
    {
        try
        {
            File file = new File("src/test/resources/Brest - La Coruna.gpx");//new File("src/test/resources/gomera-galletas.gpx");
            GPX gpx = new GPX(file);
            TH th = new TH();
            gpx.browse(0.00003, 0.00001, 10, th);
        }
        catch (Exception ex)
        {
            Logger.getLogger(TrackerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    protected String toString(float c)
    {
        return String.format("%s %f", (int)c, 60F*(c-(int)c));
    }
    protected float toFloat(double lat)
    {
        double degrees = Math.floor(lat / 100);
        double minutes = lat - 100.0 * degrees;
        double latitude = degrees + minutes / 60.0;
        return (float) latitude;
    }

    private class TH implements TrackHandler
    {
        private Tracker tracker;
        private final NMEAClock clock = new GPSClock();
        private Calendar calendar = clock.getGregorianCalendar();
        
        @Override
        public boolean startTrack(String name, Collection<Object> extensions)
        {
            try
            {
                tracker = new Tracker(".");
                tracker.set("clock", clock);
            }
            catch (IOException ex)
            {
                Logger.getLogger(TrackerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }

        @Override
        public void endTrack()
        {
            try
            {
                tracker.close();
                tracker = null;
            }
            catch (Exception ex)
            {
                Logger.getLogger(TrackerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void startTrackSeq()
        {
        }

        @Override
        public void endTrackSeq()
        {
        }

        @Override
        public void trackPoint(double latitude, double longitude, long time)
        {
            calendar.setTimeInMillis(time);
            tracker.set("latitude", (float)latitude);
            tracker.set("longitude", (float)longitude);
            tracker.commit("");
        }
        
    }
}
