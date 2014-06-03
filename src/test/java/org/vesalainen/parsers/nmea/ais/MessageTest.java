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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 *
 * @author Timo Vesalainen
 */
public class MessageTest
{
    private final NMEAParser parser;

    public MessageTest()
    {
        parser = NMEAParser.newInstance();
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
                assertEquals(rot, tc.rateOfTurn, 0.00001);
                assertEquals((float)ach.getUInt(50, 60)/10, tc.speed, 0.0001);
                assertEquals((float)ach.getInt(61, 89)/600000.0 , tc.longitude, 0.0001);
                assertEquals((float)ach.getInt(89, 116)/600000.0 , tc.latitude, 0.0001);
                assertEquals((float)ach.getUInt(116, 128)/10, tc.cog, 0.0001);
                int hdg = ach.getUInt(128, 137);
                if (hdg == 511)
                {
                    hdg = -1;
                }
                assertEquals(hdg, tc.heading);
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
                assertEquals((float)ach.getInt(79, 107)/600000.0 , tc.longitude, 0.0001);
                assertEquals((float)ach.getInt(107, 134)/600000.0 , tc.latitude, 0.0001);
                assertEquals(EPFDFixTypes.values()[ach.getUInt(134, 138)], tc.epfd);
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
                "!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09\r\n!AIVDM,2,2,9,B,888888888888880,2*2E\r\n",
                "!AIVDM,2,1,6,B,56:fS:D0000000000008v0<QD4r0`T4v3400000t0`D147?ps1P00000,0*3D\r\n!AIVDM,2,2,6,B,000000000000008,2*29\r\n",
                "!AIVDM,2,1,8,A,53Q6SR02=21U`@H?800l4E9<f1HTLt000000001?BhL<@4q30Glm841E,0*7C\r\n!AIVDM,2,2,8,A,1DThUDQh0000000,2*4D\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AisContentHelper ach = new AisContentHelper(nmea);
                assertEquals(MessageTypes.StaticAndVoyageRelatedData, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
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
                assertEquals((float)ach.getUInt(294, 302)/10, tc.draught, 0.00001);
                assertEquals(ach.getString(302, 422), tc.destination);
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
            this.destination = AisUtil.makeString(reader.getCharSequence());
        }

        @Override
        public void setDraught(float meters)
        {
            this.draught = meters;
        }

        @Override
        public void setVesselName(InputReader reader, int fieldRef)
        {
            this.vesselName = AisUtil.makeString(reader.getCharSequence());
        }

        @Override
        public void setCallSign(InputReader reader, int fieldRef)
        {
            this.callSign = AisUtil.makeString(reader.getCharSequence());
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
