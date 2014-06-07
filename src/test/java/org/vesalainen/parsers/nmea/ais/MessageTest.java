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
package org.vesalainen.parsers.nmea.ais;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.mmsi.MMSIEntry;
import org.vesalainen.parsers.mmsi.MMSIParser;
import org.vesalainen.parsers.mmsi.MMSIType;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 *
 * @author Timo Vesalainen
 */
public class MessageTest
{
    private final NMEAParser parser;
    private final MMSIParser mmsiParser;
    private double Epsilon = 0.00001;

    public MessageTest()
    {
        parser = NMEAParser.newInstance();
        mmsiParser = MMSIParser.getInstance();
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

    @Test
    public void types1_2_3()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n",
                "!AIVDM,1,1,,A,133sVfPP00PD>hRMDH@jNOvN20S8,0*7F\r\n",
                "!AIVDM,1,1,,A,13aDr=PP00PGIljMhwO3F?wN20RJ,0*62\r\n",
                "!AIVDM,1,1,,B,16:@?m001o85tmL<SbP5OlHN25Ip,0*7F\r\n",
                "!AIVDM,1,1,,A,133w;`PP00PCqghMcqNqdOvPR5Ip,0*65\r\n",
                "!AIVDM,1,1,,B,139eb:PP00PIHDNMdd6@0?vN2D2s,0*43\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AisContentHelper ach = new AisContentHelper(nmea);
                assertEquals(MessageTypes.values()[ach.getUInt(0, 6)], tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                MMSIEntry mmsiEntry = mmsiParser.parse(tc.mmsi);
                assertEquals(MMSIType.ShipStation, mmsiEntry.getType());
                assertEquals(NavigationStatus.values()[ach.getUInt(38, 42)], tc.navigationStatus);
                int roti = ach.getInt(42, 50);
                float rot;
                switch (roti)
                {
                    case 127:
                        rot = 10;
                        break;
                    case -127:
                        rot = -10;
                        break;
                    case -128:  // 0x80
                        rot = Float.NaN;
                        break;
                    default:
                        rot = (float) ((float) Math.signum(roti)*Math.pow((double)Math.abs(roti)/4.733, 2));
                        break;
                }
                assertEquals(rot, tc.rateOfTurn, Epsilon);
                assertEquals((float)ach.getUInt(50, 60)/10, tc.speed, Epsilon);
                assertEquals((float)ach.getInt(61, 89)/600000.0 , tc.longitude, Epsilon);
                assertEquals((float)ach.getInt(89, 116)/600000.0 , tc.latitude, Epsilon);
                assertEquals((float)ach.getUInt(116, 128)/10, tc.cog, Epsilon);
                int hdg = ach.getUInt(128, 137);
                if (hdg == 511)
                {
                    hdg = -1;
                }
                assertEquals(hdg, tc.heading);
                assertNull(tc.error);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type4()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,B,4020ssAuho;N?PeNwjOAp<70089A,0*09\r\n",
                "!AIVDM,1,1,,B,4028j=1uho;N>Npi9j?wtk700@8a,0*09\r\n",
                "!AIVDM,1,1,,B,402FhaQuho6Nj0dsn4I=k<i004ip,0*77\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AisContentHelper ach = new AisContentHelper(nmea);
                assertEquals(MessageTypes.BaseStationReport, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                MMSIEntry mmsiEntry = mmsiParser.parse(tc.mmsi);
                assertEquals(MMSIType.CoastStation, mmsiEntry.getType());
                assertEquals((float)ach.getInt(79, 107)/600000.0 , tc.longitude, Epsilon);
                assertEquals((float)ach.getInt(107, 134)/600000.0 , tc.latitude, Epsilon);
                assertEquals(EPFDFixTypes.values()[ach.getUInt(134, 138)], tc.epfd);
                assertNull(tc.error);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type5()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09\r\n"+
                    "!AIVDM,2,2,9,B,888888888888880,2*2E\r\n",
                "!AIVDM,2,1,6,B,56:fS:D0000000000008v0<QD4r0`T4v3400000t0`D147?ps1P00000,0*3D\r\n"+
                    "!AIVDM,2,2,6,B,000000000000008,2*29\r\n",
                "!AIVDM,2,1,8,A,53Q6SR02=21U`@H?800l4E9<f1HTLt000000001?BhL<@4q30Glm841E,0*7C\r\n"+
                    "!AIVDM,2,2,8,A,1DThUDQh0000000,2*4D\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AisContentHelper ach = new AisContentHelper(nmea);
                assertEquals(MessageTypes.StaticAndVoyageRelatedData, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                MMSIEntry mmsiEntry = mmsiParser.parse(tc.mmsi);
                assertEquals(MMSIType.ShipStation, mmsiEntry.getType());
                assertEquals(ach.getUInt(38, 40), tc.aisVersion);
                assertEquals(ach.getUInt(40, 70), tc.imoNumber);
                assertEquals(ach.getString(70, 112), tc.callSign);
                assertEquals(ach.getString(112, 232), tc.vesselName);
                assertEquals(CodesForShipType.values()[ach.getUInt(232, 240)], tc.shipType);
                assertEquals(ach.getUInt(240, 249), tc.dimensionToBow);
                assertEquals(ach.getUInt(249, 258), tc.dimensionToStern);
                assertEquals(ach.getUInt(258, 264), tc.dimensionToPort);
                assertEquals(ach.getUInt(264, 270), tc.dimensionToStarboard);
                assertEquals(EPFDFixTypes.values()[ach.getUInt(270, 274)], tc.epfd);
                assertEquals(ach.getUInt(274, 278), tc.month);
                assertEquals(ach.getUInt(278, 283), tc.day);
                int hour = ach.getUInt(283, 288);
                if (hour == 24)
                {
                    hour = -1;
                }
                assertEquals(hour, tc.hour);
                assertEquals(ach.getUInt(288, 294), tc.minute);
                assertEquals((float)ach.getUInt(294, 302)/10, tc.draught, Epsilon);
                assertEquals(ach.getString(302, 422), tc.destination);
                assertNull(tc.error);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type6()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,A,6h2E:p66B2SR04<0@00000000000,0*4C\r\n",
                "!AIVDM,1,1,,A,601uEO@oWh>0048100,4*79\r\n",
                "!AIVDM,1,1,,A,602E3U0rFKsn<P<j07,4*5A\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AisContentHelper ach = new AisContentHelper(nmea);
                assertEquals(MessageTypes.BinaryAddressedMessage, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertNull(tc.error);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type8DAC1FID11()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,A,802R5Ph0BkEachFWA2GaOwwwwwwwwwwwwkBwwwwwwwwwwwwwwwwwwwwwwwu,2*57\r\n",
                "!AIVDM,1,1,,A,8@2<HVh0BkOrj0W0I3UfUk=<abEqowejwwwwwwwwwwwwwwwwwwwwwwwwwt0,2*58\r\n",
                "!AIVDM,2,1,0,A,802R5Ph0BkHgL@PCQ:GaOwwwwwwwwwww2k8wwwwwwwwwwwwwwwwwwwww,0*3A\r\n"+
                "!AIVDM,2,2,0,A,wwt,2*60\r\n",
                "!AIVDM,1,1,,A,802R5Ph0Bk@Ch@Fln:Ga10k`M7wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwt,2*2F\r\n",
                "!AIVDM,1,1,,A,802R5Ph0Bk;N6PGM7RGaOwwwwwwwwwwwwk;0AA1Q0@12igwwwwwwwwwwwwu,2*45\r\n"    
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                AisContentHelper ach = new AisContentHelper(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                assertEquals(MessageTypes.BinaryBroadcastMessage, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals(ach.getUInt(40, 50), tc.dac);
                assertEquals(ach.getUInt(50, 56), tc.fid);
                assertEquals(1, tc.dac);
                assertEquals(11, tc.fid);
                assertEquals((float)ach.getInt(56, 80)/60000.0 , tc.latitude, Epsilon);
                assertEquals((float)ach.getInt(80, 105)/60000.0 , tc.longitude, Epsilon);
                assertEquals(ach.getUInt(105, 110), tc.day);
                int hour = ach.getUInt(110, 115);
                if (hour == 24)
                {
                    hour = -1;
                }
                assertEquals(hour, tc.hour);
                assertEquals(ach.getUInt(115, 121), tc.minute);
                int wspeed = ach.getUInt(121, 128);
                if (wspeed == 127)
                {
                    wspeed=-1;
                }
                assertEquals(wspeed, tc.wspeed);
                int wgust = ach.getUInt(128, 135);
                if (wgust == 127)
                {
                    wgust=-1;
                }
                assertEquals(wgust, tc.wgust);
                int wdir = ach.getUInt(135, 144);
                if (wdir >= 360)
                {
                    wdir=-1;
                }
                assertEquals(wdir, tc.wdir);
                int wgustdir = ach.getUInt(144, 153);
                if (wgustdir >= 360)
                {
                    wgustdir=-1;
                }
                assertEquals(wgustdir, tc.wgustdir);
                float temperature = ach.getUInt(153, 164);
                if (temperature == -1024)
                {
                    temperature=-1;
                }
                else
                {
                    temperature = temperature/10-60;
                }
                assertEquals(temperature, tc.temperature, Epsilon);
                int humidity = ach.getUInt(164, 171);
                if (humidity == 127)
                {
                    humidity=-1;
                }
                assertEquals(humidity, tc.humidity);
                float dewpoint = ach.getUInt(171, 181);
                if (dewpoint == 1023)
                {
                    dewpoint=Float.NaN;
                }
                else
                {
                    dewpoint = dewpoint/10-20;
                }
                assertEquals(dewpoint, tc.dewpoint, Epsilon);
                int pressure = ach.getUInt(181, 190);
                if (pressure == 403)
                {
                    pressure=-1;
                }
                else
                {
                    pressure+=400;
                }
                assertEquals(pressure, tc.pressure);
                assertEquals(Tendency.values()[ach.getUInt(190, 192)], tc.pressuretend);
                float visibility = ach.getUInt(192, 200);
                if (visibility >= 250)
                {
                    visibility=Float.NaN;
                }
                else
                {
                    visibility = visibility/10;
                }
                assertEquals(visibility, tc.visibility, Epsilon);
                float waterlevel = ach.getUInt(200, 209);
                if (waterlevel == 1923)
                {
                    waterlevel=Float.NaN;
                }
                else
                {
                    waterlevel = waterlevel/10-10;
                }
                assertEquals(waterlevel, tc.waterlevel, Epsilon);
                assertEquals(Tendency.values()[ach.getUInt(209, 211)], tc.leveltrend);
                float cspeed = ach.getUInt(211, 219);
                if (cspeed >= 251)
                {
                    cspeed=Float.NaN;
                }
                else
                {
                    cspeed = cspeed/10;
                }
                assertEquals(cspeed, tc.cspeed, Epsilon);
                int cdir = ach.getUInt(219, 228);
                if (cdir >= 360)
                {
                    cdir=-1;
                }
                else
                {
                    cdir = cdir/10;
                }
                assertEquals(cdir, tc.cdir, Epsilon);
                float cspeed2 = ach.getUInt(228, 236);
                if (cspeed2 >= 251)
                {
                    cspeed2=Float.NaN;
                }
                else
                {
                    cspeed2 = cspeed2/10;
                }
                assertEquals(cspeed2, tc.cspeed2, Epsilon);
                int cdir2 = ach.getUInt(236, 245);
                if (cdir2 >= 360)
                {
                    cdir2=-1;
                }
                else
                {
                    cdir2 = cdir2/10;
                }
                assertEquals(cdir2, tc.cdir2, Epsilon);
                float cdepth2 = ach.getUInt(245, 250);
                if (cdepth2 < 31)
                {
                    cdepth2=Float.NaN;
                }
                else
                {
                    cdepth2 = cdepth2/10;
                }
                assertEquals(cdepth2, tc.cdepth2, Epsilon);
                float cspeed3 = ach.getUInt(250, 258);
                if (cspeed3 >= 251)
                {
                    cspeed3=Float.NaN;
                }
                else
                {
                    cspeed3 = cspeed3/10;
                }
                assertEquals(cspeed3, tc.cspeed3, Epsilon);
                int cdir3 = ach.getUInt(258, 267);
                if (cdir3 >= 360)
                {
                    cdir3=-1;
                }
                else
                {
                    cdir3 = cdir3/10;
                }
                assertEquals(cdir3, tc.cdir3, Epsilon);
                float cdepth3 = ach.getUInt(267, 272);
                if (cdepth3 < 31)
                {
                    cdepth3=Float.NaN;
                }
                else
                {
                    cdepth3 = cdepth3/10;
                }
                assertEquals(cdepth3, tc.cdepth3, Epsilon);
                assertNull(tc.error);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    public class TC extends AbstractAISObserver
    {
        private boolean ownMessage;
        private String commitReason;
        private String rollbackReason;
        private int seq;
        private int second;
        private int heading=-1;
        private float cog = Float.NaN;
        private float latitude = Float.NaN;
        private float longitude = Float.NaN;
        private float speed = Float.NaN;
        private float rateOfTurn = Float.NaN;
        private NavigationStatus navigationStatus;
        private int mmsi = -1;
        private int sequentialMessageId;
        private MessageTypes messageType;
        private EPFDFixTypes epfd;
        private int aisVersion=-1;
        private CodesForShipType shipType;
        private String destination;
        private float draught = Float.NaN;
        private String vesselName;
        private String callSign;
        private int imoNumber=-1;
        private int dimensionToStarboard=-1;
        private int dimensionToPort=-1;
        private int dimensionToStern=-1;
        private int dimensionToBow=-1;
        private int minute=-1;
        private int hour=-1;
        private int day=-1;
        private int month=-1;
        private int fid=-1;
        private int dac=-1;
        private String error;
        private int wspeed=-1;
        private int wgust=-1;
        private int wdir=-1;
        private int wgustdir=-1;
        private float temperature=Float.NaN;
        private int humidity=-1;
        private float dewpoint=Float.NaN;
        private int pressure=-1;
        private Tendency pressuretend;
        private float visibility=Float.NaN;
        private Tendency leveltrend;
        private float waterlevel=Float.NaN;
        private float cspeed=Float.NaN;
        private int cdir=-1;
        private float cdepth3=Float.NaN;
        private int cdir3=-1;
        private float cspeed3=Float.NaN;
        private float cdepth2=Float.NaN;
        private float cspeed2=Float.NaN;
        private int cdir2=-1;

        @Override
        public void setMeasurementDepth3(float meters)
        {
            this.cdepth3 = meters;
        }

        @Override
        public void setCurrentDirection3(int degrees)
        {
            this.cdir3 = degrees;
        }

        @Override
        public void setCurrentSpeed3(float knots)
        {
            this.cspeed3 = knots;
        }

        @Override
        public void setMeasurementDepth2(float meters)
        {
            this.cdepth2 = meters;
        }

        @Override
        public void setCurrentDirection2(int degrees)
        {
            this.cdir2 = degrees;
        }

        @Override
        public void setCurrentSpeed2(float knots)
        {
            this.cspeed2 = knots;
        }

        @Override
        public void setSurfaceCurrentDirection(int currentDirection)
        {
            this.cdir = currentDirection;
        }

        @Override
        public void setSurfaceCurrentSpeed(float knots)
        {
            this.cspeed = knots;
        }

        @Override
        public void setWaterLevelTrend(Tendency trend)
        {
            this.leveltrend = trend;
        }

        @Override
        public void setWaterLevel(float meters)
        {
            this.waterlevel = meters;
        }

        @Override
        public void setAirPressureTendency(Tendency tendency)
        {
            this.pressuretend = tendency;
        }

        @Override
        public void setVisibility(float nm)
        {
            this.visibility = nm;
        }

        @Override
        public void setAirPressure(int pressure)
        {
            this.pressure = pressure;
        }

        @Override
        public void setDewPoint(float degrees)
        {
            this.dewpoint = degrees;
        }

        @Override
        public void setRelativeHumidity(int humidity)
        {
            this.humidity = humidity;
        }

        @Override
        public void setAirTemperature(float degrees)
        {
            this.temperature = degrees;
        }

        @Override
        public void setWindGustDirection(int degrees)
        {
            this.wgustdir = degrees;
        }

        @Override
        public void setWindDirection(int degrees)
        {
            this.wdir = degrees;
        }

        @Override
        public void setGustSpeed(int knots)
        {
            this.wgust = knots;
        }

        @Override
        public void setAverageWindSpeed(int knots)
        {
            this.wspeed = knots;
        }

        @Override
        public void setError(String string)
        {
            this.error = string;
        }

        @Override
        public void setFID(int fid)
        {
            this.fid = fid;
        }

        @Override
        public void setDAC(int dac)
        {
            this.dac = dac;
        }

        @Override
        public void setMinute(int minute)
        {
            this.minute = minute;
        }

        @Override
        public void setHour(int hour)
        {
            this.hour = hour;
        }

        @Override
        public void setDay(int day)
        {
            this.day = day;
        }

        @Override
        public void setMonth(int month)
        {
            this.month = month;
        }

        @Override
        public void setDimensionToStarboard(int dimension)
        {
            this.dimensionToStarboard = dimension;
        }

        @Override
        public void setDimensionToPort(int dimension)
        {
            this.dimensionToPort = dimension;
        }

        @Override
        public void setDimensionToStern(int dimension)
        {
            this.dimensionToStern = dimension;
        }

        @Override
        public void setDimensionToBow(int dimension)
        {
            this.dimensionToBow = dimension;
        }

        @Override
        public void setShipType(CodesForShipType codesForShipType)
        {
            this.shipType = codesForShipType;
        }

        @Override
        public void setDestination(InputReader reader, int fieldRef)
        {
            this.destination = AisUtil.makeString(reader);
        }

        @Override
        public void setDraught(float meters)
        {
            this.draught = meters;
        }

        @Override
        public void setVesselName(InputReader reader, int fieldRef)
        {
            this.vesselName = AisUtil.makeString(reader);
        }

        @Override
        public void setCallSign(InputReader reader, int fieldRef)
        {
            this.callSign = AisUtil.makeString(reader);
        }

        @Override
        public void setIMONumber(int imo)
        {
            this.imoNumber = imo;
        }

        @Override
        public void setAisVersion(int arg)
        {
            this.aisVersion = arg;
        }

        @Override
        public void setEPFD(EPFDFixTypes epfdFixTypes)
        {
            this.epfd = epfdFixTypes;
        }

        @Override
        public void setOwnMessage(boolean ownMessage)
        {
            this.ownMessage = ownMessage;
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
        public void setSequenceNumber(int seq)
        {
            this.seq = seq;
        }

        @Override
        public void setSecond(int second)
        {
            this.second = second;
        }

        @Override
        public void setHeading(int heading)
        {
            this.heading = heading;
        }

        @Override
        public void setCourse(float cog)
        {
            this.cog = cog;
        }

        @Override
        public void setLatitude(float degrees)
        {
            this.latitude = degrees;
        }

        @Override
        public void setLongitude(float degrees)
        {
            this.longitude = degrees;
        }

        @Override
        public void setSpeed(float knots)
        {
            this.speed = knots;
        }

        @Override
        public void setRateOfTurn(float degreesPerMinute)
        {
            this.rateOfTurn = degreesPerMinute;
        }

        @Override
        public void setNavigationStatus(NavigationStatus navigationStatus)
        {
            this.navigationStatus = navigationStatus;
        }

        @Override
        public void setMMSI(int mmsi)
        {
            this.mmsi = mmsi;
        }

        @Override
        public void setMessageType(MessageTypes messageType)
        {
            this.messageType = messageType;
        }
        
    }
}
