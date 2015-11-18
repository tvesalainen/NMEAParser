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
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nio.channels.ReadableByteChannelFactory;
import org.vesalainen.nmea.util.TrackInput;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 *
 * @author tkv
 */
public class TrackerTest
{
    static final float Epsilon = 1e-10F;
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
                assertEquals(toFloat(60, 09.2031F), trackInput.getLatitude(), Epsilon);
                assertEquals(toFloat(24, 53.6519F), trackInput.getLongitude(), Epsilon);
                while (trackInput.read())
                {
                    
                }
                assertEquals("20100515120656", sdf.format(new Date(trackInput.getTime())));
                //assertEquals(toFloat(60, 09.2038F), trackInput.getLatitude(), Epsilon);
                assertEquals(toFloat(24, 53.6586F), trackInput.getLongitude(), Epsilon);
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
    
    private float toFloat(int deg, float min)
    {
        return deg+min/60.0F;
    }
}
