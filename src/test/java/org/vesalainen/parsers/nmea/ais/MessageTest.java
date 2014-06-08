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
 * TODO Test for 
 * Message 17
 * Message 18
 * Message 19
 * Message 20
 * Message 21
 * Message 22
 * Message 24

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
                assertEquals((float)ach.getUInt(116, 128)/10, tc.course, Epsilon);
                int hdg = ach.getUInt(128, 137);
                if (hdg == 511)
                {
                    hdg = -1;
                }
                assertEquals(hdg, tc.heading);
                assertEquals(ach.getUInt(137, 143), tc.second);
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
                "!AIVDM,1,1,,A,802R5Ph0Bk;N6PGM7RGaOwwwwwwwwwwwwk;0AA1Q0@12igwwwwwwwwwwwwu,2*45\r\n",
                "!AIVDM,1,1,,B,802R5Ph0BkEao@DowjGaOwwwwwwwwwwwwwwP8F0I<L1OLwwwwwwwwwwwwwt,2*19\r\n"
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
                else
                {
                    assertTrue(tc.hour > 0 && tc.hour < 23);
                }
                assertEquals(hour, tc.hour);
                assertEquals(ach.getUInt(115, 121), tc.minute);
                assertTrue(tc.minute > 0 && tc.minute < 60);
                int wspeed = ach.getUInt(121, 128);
                if (wspeed == 127)
                {
                    wspeed=-1;
                }
                else
                {
                    assertTrue(tc.wspeed >= 0 && tc.wspeed < 127);
                }
                assertEquals(wspeed, tc.wspeed);
                int wgust = ach.getUInt(128, 135);
                if (wgust == 127)
                {
                    wgust=-1;
                }
                else
                {
                    assertTrue(tc.wgust >= 0 && tc.wgust < 127);
                }
                assertEquals(wgust, tc.wgust);
                int wdir = ach.getUInt(135, 144);
                if (wdir >= 360)
                {
                    wdir=-1;
                }
                else
                {
                    assertTrue(tc.wdir >= 0 && tc.wdir < 360);
                }
                assertEquals(wdir, tc.wdir);
                int wgustdir = ach.getUInt(144, 153);
                if (wgustdir >= 360)
                {
                    wgustdir=-1;
                }
                else
                {
                    assertTrue(tc.wgustdir >= 0 && tc.wgustdir < 360);
                }
                assertEquals(wgustdir, tc.wgustdir);
                float temperature = ach.getUInt(153, 164);
                if (temperature > 1200)
                {
                    temperature=Float.NaN;
                }
                else
                {
                    temperature = temperature/10-60;
                    assertTrue(tc.temperature >= -60 && tc.temperature < 60);
                }
                assertEquals(temperature, tc.temperature, Epsilon);
                int humidity = ach.getUInt(164, 171);
                if (humidity == 127)
                {
                    humidity=-1;
                }
                else
                {
                    assertTrue(tc.humidity >= 0 && tc.humidity <= 100);
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
                    assertTrue(tc.dewpoint >= -20 && tc.dewpoint <= 50);
                }
                assertEquals(dewpoint, tc.dewpoint, Epsilon);
                int pressure = ach.getUInt(181, 190);
                if (pressure >= 403)
                {
                    pressure=-1;
                }
                else
                {
                    pressure+=800;
                    assertTrue(tc.pressure >= 800 && tc.pressure <= 1200);
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
                    assertTrue(tc.visibility >= 0 && tc.visibility <= 25);
                }
                assertEquals(visibility, tc.visibility, Epsilon);
                float waterlevel = ach.getUInt(200, 209);
                if (waterlevel >= 511)
                {
                    waterlevel=Float.NaN;
                }
                else
                {
                    waterlevel = waterlevel/10-10;
                    assertTrue(tc.waterlevel >= -10 && tc.waterlevel <= 30);
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
                    assertTrue(tc.cspeed >= 0 && tc.cspeed <= 25);
                }
                assertEquals(cspeed, tc.cspeed, Epsilon);
                int cdir = ach.getUInt(219, 228);
                if (cdir >= 360)
                {
                    cdir=-1;
                }
                else
                {
                    assertTrue(tc.cdir >= 0 && tc.cdir < 360);
                }
                assertEquals(cdir, tc.cdir);
                float cspeed2 = ach.getUInt(228, 236);
                if (cspeed2 >= 251)
                {
                    cspeed2=Float.NaN;
                }
                else
                {
                    cspeed2 = cspeed2/10;
                    assertTrue(tc.cspeed2 >= 0 && tc.cspeed2 <= 25);
                }
                assertEquals(cspeed2, tc.cspeed2, Epsilon);
                int cdir2 = ach.getUInt(236, 245);
                if (cdir2 >= 360)
                {
                    cdir2=-1;
                }
                else
                {
                    assertTrue(tc.cdir2 >= 0 && tc.cdir2 < 360);
                }
                assertEquals(cdir2, tc.cdir2);
                int cdepth2 = ach.getUInt(245, 250);
                if (cdepth2 >= 31)
                {
                    cdepth2=-1;
                }
                else
                {
                    assertTrue(tc.cdepth2 >= 0 && tc.cdepth2 < 30);
                }
                assertEquals(cdepth2, tc.cdepth2);
                float cspeed3 = ach.getUInt(250, 258);
                if (cspeed3 >= 251)
                {
                    cspeed3=Float.NaN;
                }
                else
                {
                    cspeed3 = cspeed3/10;
                    assertTrue(tc.cspeed3 >= 0 && tc.cspeed3 <= 25);
                }
                assertEquals(cspeed3, tc.cspeed3, Epsilon);
                int cdir3 = ach.getUInt(258, 267);
                if (cdir3 >= 360)
                {
                    cdir3=-1;
                }
                else
                {
                    assertTrue(tc.cdir3 >= 0 && tc.cdir3 < 360);
                }
                assertEquals(cdir3, tc.cdir3);
                int cdepth3 = ach.getUInt(267, 272);
                if (cdepth3 >= 31)
                {
                    cdepth3=-1;
                }
                else
                {
                    assertTrue(tc.cdepth3 >= 0 && tc.cdepth3 < 30);
                }
                assertEquals(cdepth3, tc.cdepth3);
                float waveheight = ach.getUInt(272, 280);
                if (waveheight >= 251)
                {
                    waveheight=Float.NaN;
                }
                else
                {
                    waveheight = waveheight/10;
                    assertTrue(tc.waveheight >= 0 && tc.waveheight < 25);
                }
                assertEquals(waveheight, tc.waveheight, Epsilon);
                int waveperiod = ach.getUInt(280, 286);
                if (waveperiod >= 61)
                {
                    waveperiod=-1;
                }
                else
                {
                    assertTrue(tc.waveperiod >= 0 && tc.waveperiod <= 60);
                }
                assertEquals(waveperiod, tc.waveperiod);
                int wavedir = ach.getUInt(286, 295);
                if (wavedir >= 360)
                {
                    wavedir=-1;
                }
                else
                {
                    assertTrue(tc.wavedir >= 0 && tc.wavedir < 360);
                }
                assertEquals(wavedir, tc.wavedir);
                float swellheight = ach.getUInt(295, 303);
                if (swellheight >= 251)
                {
                    swellheight=Float.NaN;
                }
                else
                {
                    swellheight = swellheight/10;
                    assertTrue(tc.swellheight >= 0 && tc.swellheight < 25);
                }
                assertEquals(swellheight, tc.swellheight, Epsilon);
                int swellperiod = ach.getUInt(303, 309);
                if (swellperiod >= 61)
                {
                    swellperiod=-1;
                }
                else
                {
                    assertTrue(tc.swellperiod >= 0 && tc.swellperiod <= 60);
                }
                assertEquals(swellperiod, tc.swellperiod);
                int swelldir = ach.getUInt(309, 318);
                if (swelldir >= 360)
                {
                    swelldir=-1;
                }
                else
                {
                    assertTrue(tc.swelldir >= 0 && tc.swelldir < 360);
                }
                assertEquals(swelldir, tc.swelldir);
                assertEquals(BeaufortScale.values()[ach.getUInt(318, 322)], tc.seastate);
                float watertemp = ach.getUInt(322, 332);
                if (watertemp >= 601)
                {
                    watertemp=Float.NaN;
                }
                else
                {
                    watertemp = watertemp/10-10;
                    assertTrue(tc.watertemp >= -10 && tc.watertemp <= 50);
                }
                assertEquals(watertemp, tc.watertemp, Epsilon);
                assertEquals(PrecipitationTypes.values()[ach.getUInt(332, 335)], tc.preciptype);
                float salinity = ach.getUInt(335, 344);
                if (salinity >= 500)
                {
                    salinity=Float.NaN;
                }
                else
                {
                    salinity = salinity/10;
                    assertTrue(tc.salinity >= 0 && tc.salinity <= 50);
                }
                assertEquals(salinity, tc.salinity, Epsilon);
                assertEquals(ach.getUInt(344, 346), tc.ice);
                assertNull(tc.error);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type9()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,B,91b55vRAirOn<94M097lV@@20<6=,0*5D\r\n",
                "!AIVDM,1,1,,B,91b55vRAivOnAWTM05?CNUP20<6F,0*67\r\n",
                "!AIVDM,1,1,,A,91b55vRAQwOnDE<M05ICOp0208CM,0*6A\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AisContentHelper ach = new AisContentHelper(nmea);
                assertEquals(MessageTypes.StandardSARAircraftPositionReport, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                MMSIEntry mmsiEntry = mmsiParser.parse(tc.mmsi);
                assertEquals(MMSIType.SarAircraft, mmsiEntry.getType());
                int alt = ach.getInt(38, 50);
                if (alt > 4095)
                {
                    alt = -1;
                }
                else
                {
                    assertTrue(alt >= 0 && alt <= 4094);
                }
                assertEquals(alt, tc.alt);
                assertEquals((float)ach.getUInt(50, 60), tc.speed, Epsilon);
                assertEquals((float)ach.getInt(61, 89)/600000.0 , tc.longitude, Epsilon);
                assertEquals((float)ach.getInt(89, 116)/600000.0 , tc.latitude, Epsilon);
                assertEquals((float)ach.getUInt(116, 128)/10, tc.course, Epsilon);
                assertEquals(ach.getUInt(128, 134), tc.second);
                assertNull(tc.error);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type10()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,A,:81:Jf1D02J0,0*0E\r\n",
                "!AIVDM,1,1,,B,:02Au11EB6G0,0*6F\r\n",
                "!AIVDM,1,1,,B,:81:Jf0qKjvP,0*46\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AisContentHelper ach = new AisContentHelper(nmea);
                assertEquals(MessageTypes.UTCAndDateInquiry, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals(ach.getUInt(40, 70), tc.dest_mmsi);
                assertNull(tc.error);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type11()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,B,;5E8IL1uho;NQ1d4sJEW<Ci00000,0*58\r\n",
                "!AIVDM,1,1,,A,;03t=31uho;NS`e;KLBDS0o00000,0*02\r\n",
                "!AIVDM,1,1,,A,;028j:Quho;N>OvPkdFl:VG00d2v,0*58\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AisContentHelper ach = new AisContentHelper(nmea);
                assertEquals(MessageTypes.UTCAndDateResponse, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals(ach.getUInt(38, 52), tc.year);
                assertEquals(ach.getUInt(52, 56), tc.month);
                assertEquals(ach.getUInt(56, 61), tc.day);
                int hour = ach.getUInt(61, 66);
                if (hour == 24)
                {
                    hour = -1;
                }
                assertEquals(hour, tc.hour);
                assertEquals(ach.getUInt(66, 72), tc.minute);
                assertEquals(ach.getUInt(72, 78), tc.second);
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
    public void type16()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,B,@6STUk004lQ206bCKNOBAb6SJ@5s,0*74\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AisContentHelper ach = new AisContentHelper(nmea);
                assertEquals(MessageTypes.AssignmentModeCommand, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals(ach.getUInt(40, 70), tc.mmsi1);
                assertEquals(ach.getUInt(70, 82), tc.offset1);
                assertEquals(ach.getUInt(82, 92), tc.increment1);
                assertEquals(ach.getUInt(92, 122), tc.mmsi2);
                assertEquals(ach.getUInt(122, 134), tc.offset2);
                assertEquals(ach.getUInt(134, 144), tc.increment2);
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
        private float course = Float.NaN;
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
        private int cdepth3=-1;
        private int cdir3=-1;
        private float cspeed3=Float.NaN;
        private int cdepth2=-1;
        private float cspeed2=Float.NaN;
        private int cdir2=-1;
        private int swelldir=-1;
        private int swellperiod=-1;
        private float swellheight=Float.NaN;
        private int wavedir=-1;
        private int waveperiod=-1;
        private float waveheight=Float.NaN;
        private BeaufortScale seastate;
        private float watertemp=Float.NaN;
        private PrecipitationTypes preciptype;
        private float salinity=Float.NaN;
        private int ice=-1;
        private int alt=-1;
        private int dest_mmsi=-1;
        private int year=-1;
        private int mmsi1=-1;
        private int mmsi2=-1;
        private int mmsi3=-1;
        private int mmsi4=-1;
        private int increment2=-1;
        private int increment1=-1;
        private int offset2=-1;
        private int offset1=-1;

        @Override
        public void setIncrementB(int arg)
        {
            this.increment2 = arg;
        }

        @Override
        public void setIncrementA(int arg)
        {
            this.increment1 = arg;
        }

        @Override
        public void setOffsetB(int arg)
        {
            this.offset2 = arg;
        }

        @Override
        public void setOffsetA(int arg)
        {
            this.offset1 = arg;
        }

        @Override
        public void setMMSI4(int mmsi)
        {
            this.mmsi4 = mmsi;
        }

        @Override
        public void setMMSI3(int mmsi)
        {
            this.mmsi3 = mmsi;
        }

        @Override
        public void setMMSI2(int mmsi)
        {
            this.mmsi2 = mmsi;
        }

        @Override
        public void setMMSI1(int mmsi)
        {
            this.mmsi1 = mmsi;
        }

        @Override
        public void setYear(int year)
        {
            this.year = year;
        }

        @Override
        public void setDestinationMMSI(int mmsi)
        {
            this.dest_mmsi = mmsi;
        }

        @Override
        public void setAltitude(int meters)
        {
            this.alt = meters;
        }

        @Override
        public void setIce(int ice)
        {
            this.ice = ice;
        }

        @Override
        public void setSalinity(float f)
        {
            this.salinity = f;
        }

        @Override
        public void setPrecipitation(PrecipitationTypes precipitationTypes)
        {
            this.preciptype = precipitationTypes;
        }

        @Override
        public void setWaterTemperature(float degrees)
        {
            this.watertemp = degrees;
        }

        @Override
        public void setSeaState(BeaufortScale beaufortScale)
        {
            this.seastate = beaufortScale;
        }

        @Override
        public void setSwellDirection(int degrees)
        {
            this.swelldir = degrees;
        }

        @Override
        public void setSwellPeriod(int seconds)
        {
            this.swellperiod = seconds;
        }

        @Override
        public void setSwellHeight(float meters)
        {
            this.swellheight = meters;
        }

        @Override
        public void setWaveDirection(int degrees)
        {
            this.wavedir = degrees;
        }

        @Override
        public void setWavePeriod(int seconds)
        {
            this.waveperiod = seconds;
        }

        @Override
        public void setWaveHeight(float meters)
        {
            this.waveheight = meters;
        }

        @Override
        public void setMeasurementDepth3(int meters)
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
        public void setMeasurementDepth2(int meters)
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
            this.course = cog;
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
