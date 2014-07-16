/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.parsers.nmea;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.TimeZone;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * TODO
 * AAM
   ALM
   APA
   APB
   BOD
   BWC
   BWR
   BWW
   DBK
   DBS
   DBT
   DPT
   GGA
   GLL
   HDG
   HDM
   HDT
   MTW
   MWV
   R00
   RMA
   RMB
   RMC
   RMM
   ROT
   RPM
   RSA
   RTE
   TXT
   VHW
   VWR
   WCV
   WNC
   WPL
   XTE
   XTR
   ZDA

 * 
 * @author Timo Vesalainen
 */
public class NMEAParserTest
{
    private final NMEAParser parser;
    private final double Epsilon = 0.00001;
    
    public NMEAParserTest()
    {
        parser = NMEAParser.newInstance();
    }

    @Test
    public void rmc()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPRMC,062455,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*78\r\n",
                "$GPRMC,062456,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*7B\r\n",
                "$GPRMC,062457,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*7D\r\n",
                "$GPRMC,062458,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*72\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals("GP", ss.getProperty("talkerId"));
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Clock clock = (Clock) ss.getProperty("clock");
                cal.setTimeInMillis(clock.getTime());
                String hhmmss = nch.getString(1);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                assertEquals(Integer.parseInt(hhmmss.substring(4, 6)), cal.get(Calendar.SECOND));
                assertEquals(nch.getChar(2), ss.getProperty("status"));
                assertEquals(nch.getDegree(3), ss.getProperty("latitude"));
                assertEquals(nch.getDegree(5), ss.getProperty("longitude"));
                assertEquals(nch.getFloat(7), ss.getProperty("speedOverGround"));
                assertEquals(nch.getFloat(8), ss.getProperty("trackMadeGood"));
                String ddmmyy = nch.getString(9);
                assertEquals(Integer.parseInt(ddmmyy.substring(0, 2)), cal.get(Calendar.DAY_OF_MONTH));
                assertEquals(Integer.parseInt(ddmmyy.substring(2, 4)), cal.get(Calendar.MONTH)+1);
                assertEquals(2000+Integer.parseInt(ddmmyy.substring(4, 6)), cal.get(Calendar.YEAR));
                assertEquals(nch.getFloat(10), ss.getProperty("magneticVariation"));
                assertEquals(nch.getChar(12), ss.getProperty("faaModeIndicator"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void hdg()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIHDG,171,,,06,E*13\r\n",
                "$IIHDG,177,,,06,E*15\r\n",
                "$IIHDG,175,,,06,E*17\r\n",
                "$IIHDG,174,,,06,E*16\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals("II", ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getProperty("magneticSensorHeading"));
                assertEquals(nch.getFloat(2), ss.getProperty("magneticDeviation"));
                assertEquals(nch.getFloat(4), ss.getProperty("magneticVariation"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void mwv()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIMWV,282,R,11.6,N,A*1D\r\n",
                "$$IIMWV,284,T,12.3,N,A*1B\r\n",
                "$IIMWV,280,R,11.6,N,A*1F\r\n",
                "$IIMWV,282,T,11.6,N,A*1B\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals("II", ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getProperty("magneticSensorHeading"));
                assertEquals(nch.getFloat(2), ss.getProperty("magneticDeviation"));
                assertEquals(nch.getFloat(4), ss.getProperty("magneticVariation"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

}
