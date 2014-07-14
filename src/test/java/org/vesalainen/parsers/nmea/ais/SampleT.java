/*
 * Copyright (C) 2012 Timo Vesalainen
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
package org.vesalainen.parsers.nmea.ais;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vesalainen.parsers.nmea.AbstractNMEAObserver;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 *
 * @author Timo Vesalainen
 */
public class SampleT
{
    private BufferedWriter out;
    
    public SampleT() throws IOException
    {
        out = new BufferedWriter(new FileWriter("aisdump.txt"));
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     */
    @Test
    public void testSample() throws Exception
    {
        System.out.println("sample");
        URL url = SampleT.class.getClassLoader().getResource("ais-sample.nmea");
        NMEAParser parser = NMEAParser.newInstance();
        try
        {
            parser.parse(url, new AbstractNMEAObserver(), AISTracer.getTracer(out));
        }
        catch (IOException | IllegalArgumentException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    public class IS extends InputStream
    {
        private final InputStream in;
        private final StringBuilder sb = new StringBuilder();

        public IS(InputStream in)
        {
            this.in = in;
        }
        
        @Override
        public int read() throws IOException
        {
            int rc = in.read();
            if (rc == '\n')
            {
                sb.append((char)rc);
                out.append(sb.toString());
                sb.setLength(0);
            }
            else
            {
                sb.append((char)rc);
            }
            return rc;
        }

        @Override
        public String toString()
        {
            return sb.toString();
        }

    }
}
