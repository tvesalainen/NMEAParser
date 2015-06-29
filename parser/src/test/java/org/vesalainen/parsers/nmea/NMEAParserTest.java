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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.util.navi.Knots;

/**
 * TODO
   RMM
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
    public void aam()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPAAM,A,A,0.10,N,WPTNME*32\r\n",
                "$GPAAM,A,A,0.50,N,WPT0001*71\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.AAM, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getChar(1), ss.getProperty("arrivalStatus"));
                assertEquals(nch.getChar(2), ss.getProperty("waypointStatus"));
                assertEquals(nch.getFloat(3), ss.getFloat("arrivalCircleRadius"), Epsilon);
                assertEquals(nch.getString(5), ss.getProperty("waypoint"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void alm()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPALM,1,1,15,1159,00,441d,4e,16be,fd5e,a10c9f,4a2da4,686e81,58cbe1,0a4,001*77\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.ALM, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getInt(1), ss.getProperty("totalNumberOfMessages"));
                assertEquals(nch.getInt(2), ss.getProperty("messageNumber"));
                assertEquals(nch.getInt(3), ss.getProperty("satellitePRNNumber"));
                assertEquals(nch.getInt(4), ss.getProperty("gpsWeekNumber"));
                assertEquals(nch.getHex(5), ss.getProperty("svHealth"));
                assertEquals(nch.getHex(6), ss.getProperty("eccentricity"));
                assertEquals(nch.getHex(7), ss.getProperty("almanacReferenceTime"));
                assertEquals(nch.getHex(8), ss.getProperty("inclinationAngle"));
                assertEquals(nch.getHex(9), ss.getProperty("rateOfRightAscension"));
                assertEquals(nch.getHex(10), ss.getProperty("rootOfSemiMajorAxis"));
                assertEquals(nch.getHex(11), ss.getProperty("argumentOfPerigee"));
                assertEquals(nch.getHex(12), ss.getProperty("longitudeOfAscensionNode"));
                assertEquals(nch.getHex(13), ss.getProperty("meanAnomaly"));
                assertEquals(nch.getHex(14), ss.getProperty("f0ClockParameter"));
                assertEquals(nch.getHex(15), ss.getProperty("f1ClockParameter"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void apa()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPAPA,A,A,0.10,R,N,V,V,011,M,DEST*3f\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.APA, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getChar(1), ss.getProperty("status"));
                assertEquals(nch.getChar(2), ss.getProperty("status2"));
                assertEquals(nch.getFloat(3), ss.getProperty("crossTrackError"));
                assertEquals(nch.getChar(6), ss.getProperty("arrivalStatus"));
                assertEquals(nch.getChar(7), ss.getProperty("waypointStatus"));
                assertEquals(nch.getFloat(8), ss.getProperty(nch.getPrefix(9)+"BearingOriginToDestination"));
                assertEquals(nch.getString(10), ss.getProperty("waypoint"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void apb()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPAPB,A,A,0.10,R,N,V,V,011,M,DEST,011,M,011,M*3c\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.APB, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getChar(1), ss.getProperty("status"));
                assertEquals(nch.getChar(2), ss.getProperty("status2"));
                assertEquals(nch.getFloat(3), ss.getProperty("crossTrackError"));
                assertEquals(nch.getChar(6), ss.getProperty("arrivalStatus"));
                assertEquals(nch.getChar(7), ss.getProperty("waypointStatus"));
                assertEquals(nch.getFloat(8), ss.getProperty(nch.getPrefix(9)+"BearingOriginToDestination"));
                assertEquals(nch.getString(10), ss.getProperty("waypoint"));
                assertEquals(nch.getFloat(11), ss.getProperty(nch.getPrefix(12)+"BearingPresentPositionToDestination"));
                assertEquals(nch.getFloat(13), ss.getProperty(nch.getPrefix(14)+"HeadingToSteerToDestination"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void bec()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPBEC,220516,5130.02,N,00046.34,W,213.8,T,218.0,M,0004.6,N,EGLM*33\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.BEC, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Clock clock = (Clock) ss.getProperty("clock");
                cal.setTimeInMillis(clock.getTime());
                String hhmmss = nch.getString(1);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                assertEquals(Integer.parseInt(hhmmss.substring(4, 6)), cal.get(Calendar.SECOND));
                assertEquals(nch.getDegree(2), ss.getFloat("latitude"), Epsilon);
                assertEquals(nch.getDegree(4), ss.getFloat("longitude"), Epsilon);
                assertEquals(nch.getFloat(6), ss.getFloat(nch.getPrefix(7)+"Bearing"), Epsilon);
                assertEquals(nch.getFloat(8), ss.getFloat(nch.getPrefix(9)+"Bearing"), Epsilon);
                assertEquals(nch.getFloat(10), ss.getFloat("distanceToWaypoint"), Epsilon);
                assertEquals(nch.getString(12), ss.getProperty("waypoint"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void bod()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPBOD,099.3,T,105.6,M,POINTB,*48\r\n",
                "$GPBOD,097.0,T,103.2,M,POINTB,POINTA*4a\r\n",
                "$GPBOD,164.3,T,164.5,M,De Volmer,De Volmer*41\r\n",
                "$GPBOD,345.6,T,8.6,M,BA01,AIC*04\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.BOD, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getProperty(nch.getPrefix(2)+"Bearing"));
                assertEquals(nch.getFloat(3), ss.getProperty(nch.getPrefix(4)+"Bearing"));
                assertEquals(nch.getString(5), ss.getProperty("toWaypoint"));
                assertEquals(nch.getString(6), ss.getProperty("fromWaypoint"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void bwc()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPBWC,081837,,,,,,T,,M,,N,*13\r\n",
                "$GPBWC,220516,5130.02,N,00046.34,W,213.8,T,218.0,M,0004.6,N,EGLM*21\r\n",
                "$GPBWC,010003,1248.4128,S,03827.6978,W,338.4,T,1.5,M,0.314,N,BA01,A*61\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.BWC, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Clock clock = (Clock) ss.getProperty("clock");
                cal.setTimeInMillis(clock.getTime());
                String hhmmss = nch.getString(1);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                assertEquals(Integer.parseInt(hhmmss.substring(4, 6)), cal.get(Calendar.SECOND));
                assertEquals(nch.getDegree(2), ss.getFloat("latitude"), Epsilon);
                assertEquals(nch.getDegree(4), ss.getFloat("longitude"), Epsilon);
                assertEquals(nch.getFloat(6), ss.getFloat(nch.getPrefix(7)+"Bearing"), Epsilon);
                assertEquals(nch.getFloat(8), ss.getFloat(nch.getPrefix(9)+"Bearing"), Epsilon);
                assertEquals(nch.getFloat(10), ss.getFloat("distanceToWaypoint"), Epsilon);
                assertEquals(nch.getString(12), ss.getProperty("waypoint"));
                if (nch.getSize() > 14)
                {
                    assertEquals(nch.getChar(13), ss.getProperty("faaModeIndicator"));
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void bwr()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPBWR,081837,,,,,,T,,M,,N,*02\r\n",
                "$GPBWR,220516,5130.02,N,00046.34,W,213.8,T,218.0,M,0004.6,N,EGLM*30\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.BWR, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Clock clock = (Clock) ss.getProperty("clock");
                cal.setTimeInMillis(clock.getTime());
                String hhmmss = nch.getString(1);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                assertEquals(Integer.parseInt(hhmmss.substring(4, 6)), cal.get(Calendar.SECOND));
                assertEquals(nch.getDegree(2), ss.getFloat("latitude"), Epsilon);
                assertEquals(nch.getDegree(4), ss.getFloat("longitude"), Epsilon);
                assertEquals(nch.getFloat(6), ss.getFloat(nch.getPrefix(7)+"Bearing"), Epsilon);
                assertEquals(nch.getFloat(8), ss.getFloat(nch.getPrefix(9)+"Bearing"), Epsilon);
                assertEquals(nch.getFloat(10), ss.getFloat("distanceToWaypoint"), Epsilon);
                assertEquals(nch.getString(12), ss.getProperty("waypoint"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void bww()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPBWW,099.3,T,105.6,M,POINTB,*43\r\n",
                "$GPBWW,097.0,T,103.2,M,POINTB,POINTA*41\r\n",
                "$GPBWW,164.3,T,164.5,M,De Volmer,De Volmer*4a\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.BWW, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getProperty(nch.getPrefix(2)+"Bearing"));
                assertEquals(nch.getFloat(3), ss.getProperty(nch.getPrefix(4)+"Bearing"));
                assertEquals(nch.getString(5), ss.getProperty("toWaypoint"));
                assertEquals(nch.getString(6), ss.getProperty("fromWaypoint"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void dbk()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIDBK,007.4,f,,M,,F,*21\r\n",
                "$IIDBK,007.4,f,002.3,M,,F,*0e\r\n",
                "$IIDBK,007.4,f,002.3,M,001.3,F,*22\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.DBK, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.II, ss.getProperty("talkerId"));
                assertEquals(2.3, ss.getFloat("depthBelowKeel"), 0.1);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void dbs()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIDBS,007.4,f,,M,,F,*39\r\n",
                "$IIDBS,007.4,f,002.3,M,,F,*16\r\n",
                "$IIDBS,007.4,f,002.3,M,001.3,F,*3a\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.DBS, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.II, ss.getProperty("talkerId"));
                assertEquals(2.3, ss.getFloat("depthBelowSurface"), 0.1);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void dbt()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIDBT,007.4,f,,M,,F,*3e\r\n",
                "$IIDBT,007.4,f,002.3,M,,F,*11\r\n",
                "$IIDBT,007.4,f,002.3,M,001.3,F,*3d\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.DBT, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.II, ss.getProperty("talkerId"));
                assertEquals(2.3, ss.getFloat("depthBelowTransducer"), 0.1);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void dpt()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIDPT,007.4,+0.3,*47\r\n",
                "$IIDPT,025.6,+0.3,*45\r\n",
                "$IIDPT,016.5,+0.3,*46\r\n",
                "$IIDPT,014.9,+0.3,*48\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.DPT, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.II, ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getFloat("depthOfWater"), Epsilon);
                assertEquals(nch.getFloat(2), ss.getFloat("depthOffsetOfWater"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void gga()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPGGA,172814.0,3723.46587704,N,12202.26957864,W,2,6,1.2,18.893,M,-25.669,M,2.0,0031*4F\r\n",
                "$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47\r\n",
                "$GPGGA,181703.200,5209.6815,N,00643.0724,E,1,08,01,+0025,M,+0047,M,00,0425*46\r\n",
                "$GPGGA,010003,1248.7047,S,03827.5797,W,1,11,0.8,1.0,M,-10.5,M,,*66\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.GGA, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Clock clock = (Clock) ss.getProperty("clock");
                cal.setTimeInMillis(clock.getTime());
                String hhmmss = nch.getString(1);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                assertEquals(Integer.parseInt(hhmmss.substring(4, 6)), cal.get(Calendar.SECOND));
                assertEquals(nch.getDegree(2), ss.getFloat("latitude"), Epsilon);
                assertEquals(nch.getDegree(4), ss.getFloat("longitude"), Epsilon);
                assertEquals(GPSQualityIndicator.values()[nch.getInt(6)], ss.getProperty("gpsQualityIndicator"));
                assertEquals(nch.getInt(7), ss.getProperty("numberOfSatellitesInView"));
                assertEquals(nch.getFloat(8), ss.getFloat("horizontalDilutionOfPrecision"), 0.1);
                assertEquals(nch.getFloat(9), ss.getFloat("antennaAltitude"), 0.1);
                assertEquals(nch.getFloat(11), ss.getFloat("geoidalSeparation"), 0.1);
                assertEquals(nch.getFloat(13), ss.getFloat("ageOfDifferentialGPSData"), 0.1);
                assertEquals(nch.getInt(14), ss.getProperty("differentialReferenceStationID"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void gll()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPGLL,4916.45,N,12311.12,W,225444,A,*1D\r\n",
                "$GPGLL,5209.6815,N,00643.0724,E,181703.00,A*0C\r\n",
                "$GPGLL,1248.7047,S,03827.5797,W,010003,A,A*43\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.GLL, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getDegree(1), ss.getFloat("latitude"), Epsilon);
                assertEquals(nch.getDegree(3), ss.getFloat("longitude"), Epsilon);
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Clock clock = (Clock) ss.getProperty("clock");
                cal.setTimeInMillis(clock.getTime());
                String hhmmss = nch.getString(5);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                assertEquals(Integer.parseInt(hhmmss.substring(4, 6)), cal.get(Calendar.SECOND));
                assertEquals(nch.getChar(6), ss.getProperty("status"));
                if (nch.getSize() > 8)
                {
                    assertEquals(nch.getChar(7), ss.getProperty("faaModeIndicator"));
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void gsa()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPGSA,A,3,01,03,06,,14,15,16,18,19,21,22,25,1.5,0.8,1.3*3B\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.GSA, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getChar(1), ss.getProperty("selectionMode"));
                assertEquals(nch.getChar(2), ss.getProperty("mode"));
                for (int ii=1;ii<=12;ii++)
                {
                    assertEquals(nch.getInt(2+ii), ss.getProperty("satelliteId"+ii));
                }
                assertEquals(nch.getFloat(15), ss.getFloat("pdop"), Epsilon);
                assertEquals(nch.getFloat(16), ss.getFloat("hdop"), Epsilon);
                assertEquals(nch.getFloat(17), ss.getFloat("vdop"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void gsv()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPGSV,3,1,12,01,43,333,54,03,43,237,49,06,05,030,43,09,02,120,00*7C\r\n",
                "$GPGSV,3,2,12,14,86,032,47,15,46,140,45,16,18,315,47,18,19,143,49*70\r\n",
                "$GPGSV,3,3,12,19,21,221,35,21,29,091,49,22,39,174,43,25,17,000,50*70\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.GSV, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getInt(1), ss.getProperty("totalNumberOfMessages"));
                assertEquals(nch.getInt(2), ss.getProperty("messageNumber"));
                assertEquals(nch.getInt(3), ss.getProperty("totalNumberOfSatellitesInView"));
                int count = (nch.getSize()-5)/4;
                int index = 4+4*(count-1);
                assertEquals(nch.getInt(index++), ss.getProperty("prn"));
                assertEquals(nch.getInt(index++), ss.getProperty("elevation"));
                assertEquals(nch.getInt(index++), ss.getProperty("azimuth"));
                assertEquals(nch.getInt(index++), ss.getProperty("snr"));
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
                "$IIHDG,174,,,06,E*16\r\n",
                "$IIHDG,98.3,0.0,E,12.6,W*5C\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.HDG, ss.getProperty("messageType"));
                assertEquals(TalkerId.II, ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getFloat("magneticSensorHeading"), Epsilon);
                assertEquals(nch.getFloat(2), ss.getFloat("magneticDeviation"), Epsilon);
                assertEquals(nch.getSign(5)*nch.getFloat(4), ss.getFloat("magneticVariation"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void hdm()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIHDM,171,M*3b\r\n",
                "$IIHDM,177,M*3d\r\n",
                "$IIHDM,175,M*3f\r\n",
                "$IIHDM,174,M*3e\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.HDM, ss.getProperty("messageType"));
                assertEquals(TalkerId.II, ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getFloat("magneticHeading"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void hdt()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIHDT,171,T*3b\r\n",
                "$IIHDT,177,T*3d\r\n",
                "$IIHDT,175,T*3f\r\n",
                "$IIHDT,174,T*3e\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.HDT, ss.getProperty("messageType"));
                assertEquals(TalkerId.II, ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getFloat("trueHeading"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void mtw()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIMTW,23.5,C*17\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.MTW, ss.getProperty("messageType"));
                assertEquals(TalkerId.II, ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getFloat("waterTemperature"), Epsilon);
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
                "$IIMWV,284,T,12.3,N,A*1B\r\n",
                "$IIMWV,280,R,11.6,N,A*1F\r\n",
                "$IIMWV,282,T,11.6,N,A*1B\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.MWV, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.II, ss.getProperty("talkerId"));
                String rt = nch.getPrefix(2);
                assertEquals(nch.getFloat(1), ss.getFloat(rt+"WindAngle"), Epsilon);
                assertEquals(Knots.toMetersPerSecond(nch.getFloat(3)), ss.getFloat("windSpeed"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void r00()
    {
        try
        {
            String[] nmeas = new String[] {
                "$IIR00,WP1,WP2,WP3,WP4*56\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.R00, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.II, ss.getProperty("talkerId"));
                assertEquals(nch.getList(1, 4), ss.getProperty("waypoints"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void rma()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPRMA,A,6009.2054,N,02453.6493,E,12.3,23.4,6.7,265.3,6.7,E*77\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.RMA, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getChar(1), ss.getProperty("status"));
                assertEquals(nch.getDegree(2), ss.getFloat("latitude"), Epsilon);
                assertEquals(nch.getDegree(4), ss.getFloat("longitude"), Epsilon);
                assertEquals(nch.getFloat(6), ss.getFloat("timeDifferenceA"), Epsilon);
                assertEquals(nch.getFloat(7), ss.getFloat("timeDifferenceB"), Epsilon);
                assertEquals(nch.getFloat(8), ss.getFloat("speedOverGround"), Epsilon);
                assertEquals(nch.getFloat(9), ss.getFloat("trackMadeGood"), Epsilon);
                assertEquals(nch.getSign(11)*nch.getFloat(10), ss.getFloat("magneticVariation"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void rmb()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPRMB,A,0.66,L,003,004,4917.24,N,12309.57,W,001.3,052.5,000.5,V*20\r\n",
                "$GPRMB,A,4.08,L,EGLL,EGLM,5130.02,N,00046.34,W,004.6,213.9,122.9,A*3D\r\n",
                "$GPRMB,A,0.04,L,AIC,BA01,1248.4128,S,03827.6978,W,0.314,338.4,,V,A*33\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.RMB, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getChar(1), ss.getProperty("status"));
                assertEquals(nch.getSign(3)*nch.getFloat(2), ss.getFloat("crossTrackError"), Epsilon);
                assertEquals(nch.getString(4), ss.getProperty("toWaypoint"));
                assertEquals(nch.getString(5), ss.getProperty("fromWaypoint"));
                assertEquals(nch.getDegree(6), ss.getFloat("destinationWaypointLatitude"), Epsilon);
                assertEquals(nch.getDegree(8), ss.getFloat("destinationWaypointLongitude"), Epsilon);
                assertEquals(nch.getFloat(10), ss.getFloat("rangeToDestination"), Epsilon);
                assertEquals(nch.getFloat(11), ss.getFloat("bearingToDestination"), Epsilon);
                assertEquals(nch.getFloat(12), ss.getFloat("destinationClosingVelocity"), Epsilon);
                assertEquals(nch.getChar(13), ss.getProperty("arrivalStatus"));
                if (nch.getSize() > 15)
                {
                    assertEquals(nch.getChar(14), ss.getProperty("faaModeIndicator"));
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
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
                "$GPRMC,062458,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*72\r\n",
                "$GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62\r\n",
                "$GPRMC,225446,A,4916.45,N,12311.12,W,000.5,054.7,191194,020.3,E*68\r\n",
                "$GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70\r\n",
                "$GPRMC,010003,A,1248.7047,S,03827.5797,W,0.0,94.9,290505,23.0,W,A*03\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.RMC, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Clock clock = (Clock) ss.getProperty("clock");
                cal.setTimeInMillis(clock.getTime());
                String hhmmss = nch.getString(1);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                assertEquals(Integer.parseInt(hhmmss.substring(4, 6)), cal.get(Calendar.SECOND));
                assertEquals(nch.getChar(2), ss.getProperty("status"));
                assertEquals(nch.getDegree(3), ss.getFloat("latitude"), Epsilon);
                assertEquals(nch.getDegree(5), ss.getFloat("longitude"), Epsilon);
                assertEquals(nch.getFloat(7), ss.getFloat("speedOverGround"), Epsilon);
                assertEquals(nch.getFloat(8), ss.getFloat("trackMadeGood"), Epsilon);
                String ddmmyy = nch.getString(9);
                assertEquals(Integer.parseInt(ddmmyy.substring(0, 2)), cal.get(Calendar.DAY_OF_MONTH));
                assertEquals(Integer.parseInt(ddmmyy.substring(2, 4)), cal.get(Calendar.MONTH)+1);
                assertEquals(nch.parseYear(ddmmyy.substring(4, 6)), cal.get(Calendar.YEAR));
                assertEquals(nch.getSign(11)*nch.getFloat(10), ss.getFloat("magneticVariation"), Epsilon);
                if (nch.getSize() > 13)
                {
                    assertEquals(nch.getChar(12), ss.getProperty("faaModeIndicator"));
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void pgrme()
    {
        try
        {
            String[] nmeas = new String[] {
                "$PGRME,6.7,M,9.5,M,11.6,M*15\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals("GRME", ss.getProperty("proprietaryType"));
                assertEquals(nch.getList(1, 6), ss.getProperty("proprietaryData"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void pgrmm()
    {
        try
        {
            String[] nmeas = new String[] {
                "$PGRMM,WGS 84*06\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals("GRMM", ss.getProperty("proprietaryType"));
                assertEquals(nch.getList(1, 1), ss.getProperty("proprietaryData"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void pgrmz()
    {
        try
        {
            String[] nmeas = new String[] {
                "$PGRMZ,3,f,3*18\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals("GRMZ", ss.getProperty("proprietaryType"));
                assertEquals(nch.getList(1, 3), ss.getProperty("proprietaryData"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void rot()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPROT,35.6,A*01\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.ROT, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getProperty("rateOfTurn"));
                assertEquals(nch.getChar(2), ss.getProperty("status"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void rpm()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPRPM,S,1,3500.6,5.6,A*64\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.RPM, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getChar(1), ss.getProperty("rpmSource"));
                assertEquals(nch.getInt(2), ss.getProperty("rpmSourceNumber"));
                assertEquals(nch.getFloat(3), ss.getFloat("rpm"), Epsilon);
                assertEquals(nch.getFloat(4), ss.getFloat("propellerPitch"), Epsilon);
                assertEquals(nch.getChar(5), ss.getProperty("status"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void rsa()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPRSA,-4.2,A,-4.0,A*55\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.RSA, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getFloat(1), ss.getFloat("starboardRudderSensor"), Epsilon);
                assertEquals(nch.getChar(2), ss.getProperty("status"));
                assertEquals(nch.getFloat(3), ss.getFloat("portRudderSensor"), Epsilon);
                assertEquals(nch.getChar(4), ss.getProperty("status2"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void rte()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPRTE,2,1,c,0,PBRCPK,PBRTO,PTELGR,PPLAND,PYAMBU,PPFAIR,PWARRN,PMORTL,PLISMR*73\r\n",
                "$GPRTE,2,2,c,0,PCRESY,GRYRIE,GCORIO,GWERR,GWESTG,7FED*34\r\n",
                "$GPRTE,2,1,c,0,W3IWI,DRIVWY,32CEDR,32-29,32BKLD,32-I95,32-US1,BW-32,BW-198*69\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.RTE, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getInt(1), ss.getProperty("totalNumberOfMessages"));
                assertEquals(nch.getInt(2), ss.getProperty("messageNumber"));
                assertEquals(nch.getChar(3), ss.getProperty("messageMode"));
                assertEquals(nch.getString(4), ss.getProperty("route"));
                int no = nch.getSize()-6;
                assertEquals(nch.getList(5, no), ss.getProperty("waypoints"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void tll()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPTLL,01,4305.4281,N,07147.3170,W,TARG1,123456.21,T,R*57\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.TLL, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getInt(1), ss.getProperty("targetNumber"));
                assertEquals(nch.getDegree(2), ss.getFloat("destinationWaypointLatitude"), Epsilon);
                assertEquals(nch.getDegree(4), ss.getFloat("destinationWaypointLongitude"), Epsilon);
                assertEquals(nch.getString(6), ss.getProperty("targetName"));
                String hhmmss = nch.getString(7);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), ss.getInt("targetHour"));
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), ss.getInt("targetMinute"));
                assertEquals(Float.parseFloat(hhmmss.substring(4)), ss.getFloat("targetSecond"), Epsilon);
                assertEquals(nch.getChar(8), ss.getProperty("targetStatus"));
                assertEquals(nch.getString(9), ss.getProperty("referenceTarget"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void txt()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPTXT,01,01,TARG1,Message*35\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.TXT, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getInt(1), ss.getProperty("totalNumberOfMessages"));
                assertEquals(nch.getInt(2), ss.getProperty("messageNumber"));
                assertEquals(nch.getString(3), ss.getProperty("targetName"));
                assertEquals(nch.getString(4), ss.getProperty("message"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void vtg()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPVTG,94.9,T,117.9,M,0.0,N,0.0,K,A*4d\r\n",
                "$GPVTG,94.9,117.9,0.0,0.0*48\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.VTG, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(94.9, ss.getFloat("trueTrackMadeGood"), Epsilon);
                assertEquals(117.9, ss.getFloat("magneticTrackMadeGood"), Epsilon);
                assertEquals(0, ss.getFloat("speedOverGround"), Epsilon);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void wpl()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPWPL,1249.4946,S,03830.9732,W,USIB*4F\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.WPL, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getDegree(1), ss.getFloat("destinationWaypointLatitude"), Epsilon);
                assertEquals(nch.getDegree(3), ss.getFloat("destinationWaypointLongitude"), Epsilon);
                assertEquals(nch.getString(5), ss.getProperty("waypoint"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void xte()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPXTE,A,A,0.04,L,N,A*07\r\n",
                "$GPXTE,A,A,0.67,L,N*6F\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.XTE, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getChar(1), ss.getProperty("status"));
                assertEquals(nch.getChar(2), ss.getProperty("status2"));
                assertEquals(nch.getFloat(3)*nch.getSign(4), ss.getProperty("crossTrackError"));
                if (nch.getSize() > 7)
                {
                    assertEquals(nch.getChar(6), ss.getProperty("faaModeIndicator"));
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void zda()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPZDA,201530.00,04,07,2002,00,00*60\r\n",
                "$GPZDA,160012.71,11,03,2004,-1,00*7D\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.ZDA, ss.getProperty("messageType"));
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                int hd = nch.getInt(5);
                int md = nch.getInt(6);
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Clock clock = (Clock) ss.getProperty("clock");
                cal.setTimeInMillis(clock.getTime());
                String hhmmss = nch.getString(1);
                assertEquals(Integer.parseInt(hhmmss.substring(0, 2)), cal.get(Calendar.HOUR_OF_DAY)+hd);
                assertEquals(Integer.parseInt(hhmmss.substring(2, 4)), cal.get(Calendar.MINUTE));
                // note that clock is running!
                assertEquals(Float.parseFloat(hhmmss.substring(4)), (double)cal.get(Calendar.SECOND)+(double)cal.get(Calendar.MILLISECOND)/1000.0, 0.1);  
                assertEquals(nch.getInt(2), Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                assertEquals(nch.getInt(3), Integer.valueOf(cal.get(Calendar.MONTH)+1));
                assertEquals(nch.getInt(4), Integer.valueOf(cal.get(Calendar.YEAR)));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void err0()
    {
        try
        {
            String nmea = 
                "$GPRMC,062455,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*78\r\n"+
                "$GPRMC,062456,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*7B\r\n"+
                "$GPRMC,062457,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*7D\r\n"+
                "$GPRMC,062458,A,6009.2053,N,02453.6493,E,000.0,001.3,171009,,,A*72\r\n"+
                "$GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62\r\n"+
                "$GPRMC,225446,A,4916.45,N,12311.12,W,000.5,054.7,191194,020.3,E*68\r\n"+
                "$GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70\r\n"+
                "$GPRMC,010003,A,1248.7047,S,03827.5797,W,0.0,94.9,290505,23.0,W,A*03\r\n"
            ;
            System.err.println(nmea);
            List<Object> list = new ArrayList<>();
            list.add(1.3F);
            list.add(1.3F);
            list.add(1.3F);
            list.add(1.3F);
            list.add(360F);
            list.add(54.7F);
            list.add(231.8F);
            list.add(94.9F);
            ListStorage ls = new ListStorage();
            NMEAObserver tc = ls.getStorage(NMEAObserver.class);
            parser.parse(nmea, tc, null);
            assertEquals(list, ls.getProperty("trackMadeGood"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void err1()
    {
        try
        {
            String nmea = 
                "@GPRMC,062455,A,6009.2054,N,02453.6493,E,000.0,001.7,171009,,,A*78\r\n"+   // err
                "$GPRMC,062456,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*7B\r\n"+
                "$GPRMC,062457,A,6009.2053,N,02453.6493,E,000.0,001.9,171009,,,A*70\r\n"+   // err
                "$GPRMC,062458,A,6009.2053,N,02453.6493,E,000.0,001.5,171009,,,A*72\r\n"+   // err
                "$GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62\r\n"+
                "$GPRMC,225446,A,4916.45,N,12311.12,W,000.5,054.7,191194,020.3,E*68\r\n"+
                "$GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70\r\n"+
                "$GPRMC,A010003,A,1248.7047,S,03827.5797,W,0.0,94.9,290505,23.0,W,A*03\r\n" // err
            ;
            System.err.println(nmea);
            List<Object> list = new ArrayList<>();
            list.add(1.3F);
            list.add(360F);
            list.add(54.7F);
            list.add(231.8F);
            ListStorage ls = new ListStorage();
            NMEAObserver tc = ls.getStorage(NMEAObserver.class);
            parser.parse(nmea, tc, null);
            assertEquals(list, ls.getProperty("trackMadeGood"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void escape()
    {
        try
        {
            String[] nmeas = new String[] {
                "$GPTXT,01,01,TARG1,H^D6LM^D6*37\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                SimpleStorage ss = new SimpleStorage();
                NMEAObserver tc = ss.getStorage(NMEAObserver.class);
                parser.parse(nmea, tc, null);
                assertNull(ss.getRollbackReason());
                assertEquals(MessageType.TXT, ss.getProperty("messageType"));
                NMEAContentHelper nch = new NMEAContentHelper(nmea);
                assertEquals(TalkerId.GP, ss.getProperty("talkerId"));
                assertEquals(nch.getInt(1), ss.getProperty("totalNumberOfMessages"));
                assertEquals(nch.getInt(2), ss.getProperty("messageNumber"));
                assertEquals(nch.getString(3), ss.getProperty("targetName"));
                assertEquals("HÖLMÖ", ss.getProperty("message"));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

}
