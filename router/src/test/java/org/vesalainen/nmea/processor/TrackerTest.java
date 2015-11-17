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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nio.channels.ReadableByteChannelFactory;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 *
 * @author tkv
 */
public class TrackerTest
{
    
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
            FileOutputStream fis = new FileOutputStream("src/test/resources/sample.trc");
            Tracker tracker = new Tracker(fis);
            observer.addObserver(tracker, tracker.getPrefixes());
            try (ReadableByteChannel rbc = ReadableByteChannelFactory.getInstance(file))
            {
                NMEAParser parser = NMEAParser.newInstance();
                parser.parse(rbc, observer, null);
            }
            catch (IOException ex)
            {
                fail(ex.getMessage());
                Logger.getLogger(TrackerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
            Logger.getLogger(TrackerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
