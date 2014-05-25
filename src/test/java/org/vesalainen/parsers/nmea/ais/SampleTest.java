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

import java.io.IOException;
import java.io.InputStream;
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
public class SampleTest
{
    
    public SampleTest()
    {
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
        InputStream in = SampleTest.class.getClassLoader().getResourceAsStream("nmea-sample");
        IS is = new IS(in);
        NMEAParser parser = NMEAParser.newInstance();
        try
        {
            AD ad = new AD();
            parser.parse(is, new AbstractNMEAObserver(), ad);
            assertEquals(0, ad.getRollbacks());
        }
        catch (IOException | IllegalArgumentException ex)
        {
            fail(ex.getMessage());
        }
    }

    public class AD extends AISTracer
    {
        private int rollbacks;
        @Override
        public void rollback(String reason)
        {
            super.rollback(reason);
            rollbacks++;
        }

        public int getRollbacks()
        {
            return rollbacks;
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
                System.err.println(sb.toString());
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
