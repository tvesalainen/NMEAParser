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
                TC tc = new TC();
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals("GP", tc.talkerId);
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(tc.clock.getTime());
                String hhmmss = nch.getString(1);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                assertEquals(Integer.parseInt(hhmmss.substring(4, 6)), cal.get(Calendar.SECOND));
                assertEquals(nch.getChar(2), tc.status);
                assertEquals(nch.getDegree(3), tc.latitude, Epsilon);
                assertEquals(nch.getDegree(5), tc.longitude, Epsilon);
                assertEquals(nch.getFloat(7), tc.speedOverGround, Epsilon);
                assertEquals(nch.getFloat(8), tc.trackMadeGood, Epsilon);
                String ddmmyy = nch.getString(9);
                assertEquals(Integer.parseInt(ddmmyy.substring(0, 2)), cal.get(Calendar.DAY_OF_MONTH));
                assertEquals(Integer.parseInt(ddmmyy.substring(2, 4)), cal.get(Calendar.MONTH)+1);
                assertEquals(2000+Integer.parseInt(ddmmyy.substring(4, 6)), cal.get(Calendar.YEAR));
                assertEquals(nch.getFloat(10), tc.magneticVariation, Epsilon);
                assertEquals(nch.getChar(12), tc.faaModeIndicator);
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
                TC tc = new TC();
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals("II", tc.talkerId);
                assertEquals(nch.getFloat(1), tc.magneticSensorHeading, Epsilon);
                assertEquals(nch.getFloat(2), tc.magneticDeviation, Epsilon);
                assertEquals(nch.getFloat(4), tc.magneticVariation, Epsilon);
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
                TC tc = new TC();
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals("II", tc.talkerId);
                assertEquals(nch.getFloat(1), tc.magneticSensorHeading, Epsilon);
                assertEquals(nch.getFloat(2), tc.magneticDeviation, Epsilon);
                assertEquals(nch.getFloat(4), tc.magneticVariation, Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    private static class TC extends AbstractNMEAObserver
    {
        private String commitReason;
        private String rollbackReason;
        private String talkerId;
        private float latitude = Float.NaN;
        private float longitude = Float.NaN;
        private char status;
        private float speedOverGround = Float.NaN;
        private float trackMadeGood = Float.NaN;
        private float magneticVariation = Float.NaN;
        private char faaModeIndicator;
        private float magneticDeviation = Float.NaN;
        private float magneticSensorHeading = Float.NaN;

        @Override
        public void setWindSpeed(float windSpeed, char unit)
        {
        }

        @Override
        public void setWindAngle(float windAngle, char unit)
        {
        }

        @Override
        public void setMagneticSensorHeading(float magneticSensorHeading)
        {
            this.magneticSensorHeading = magneticSensorHeading;
        }

        @Override
        public void setMagneticDeviation(float magneticDeviation)
        {
            this.magneticDeviation = magneticDeviation;
        }

        @Override
        public void setFAAModeIndicator(char faaModeIndicator)
        {
            this.faaModeIndicator = faaModeIndicator;
        }

        @Override
        public void setMagneticVariation(float magneticVariation)
        {
            this.magneticVariation = magneticVariation;
        }

        @Override
        public void setTrackMadeGood(float trackMadeGood)
        {
            this.trackMadeGood = trackMadeGood;
        }

        @Override
        public void setSpeedOverGround(float speedOverGround)
        {
            this.speedOverGround = speedOverGround;
        }

        @Override
        public void commit(String reason)
        {
            this.commitReason = reason;
        }

        @Override
        public void rollback(String reason)
        {
            this.rollbackReason = reason;
        }

        @Override
        public void setStatus(char status)
        {
            this.status = status;
        }

        @Override
        public void setLocation(float latitude, float longitude)
        {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public void setTalkerId(char c1, char c2)
        {
            this.talkerId = new String(new char[] {c1, c2});
        }

    }

}
