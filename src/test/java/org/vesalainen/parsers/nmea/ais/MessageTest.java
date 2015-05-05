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

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vesalainen.code.MapListPropertySetter;
import org.vesalainen.parsers.mmsi.MMSIEntry;
import org.vesalainen.parsers.mmsi.MMSIParser;
import org.vesalainen.parsers.mmsi.MMSIType;
import static org.vesalainen.parsers.mmsi.MMSIType.CraftAssociatedWithParentShip;
import org.vesalainen.parsers.nmea.ListStorage;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 * TODO Test for 
 * 7
 * 8 other than dac 1 fid 11
 * 12
 * 14
 * 15
 * 23
 * 27
 * 
 * @author Timo Vesalainen
 */
public class MessageTest
{
    private NMEAParser parser;
    private MMSIParser mmsiParser;
    private final double Epsilon = 0.00001;

    public MessageTest()
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
        parser = NMEAParser.newInstance();
        mmsiParser = MMSIParser.getInstance();
    }

    @After
    public void tearDown()
    {
    }
/*
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
                AISContentHelper ach = new AISContentHelper(nmea);
                assertNull(tc.rollbackReason);
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
        catch (Exception ex)
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
                AISContentHelper ach = new AISContentHelper(nmea);
                assertNull(tc.rollbackReason);
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
        catch (Exception ex)
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
                AISContentHelper ach = new AISContentHelper(nmea);
                assertEquals(MessageTypes.StaticAndVoyageRelatedData, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                MMSIEntry mmsiEntry = mmsiParser.parse(tc.mmsi);
                assertNull(tc.rollbackReason);
                assertEquals(MMSIType.ShipStation, mmsiEntry.getType());
                assertEquals(ach.getUInt(38, 40), tc.aisVersion);
                assertEquals(ach.getUInt(40, 70), tc.imoNumber);
                assertEquals(ach.getString(70, 112), tc.callSign);
                assertEquals(ach.getString(112, 232), tc.shipname);
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
        catch (Exception ex)
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
//                "!AIVDM,1,1,,A,6h2E:p66B2SR04<0@00000000000,0*4C\r\n",
//                "!AIVDM,1,1,,A,601uEO@oWh>0048100,4*79\r\n",
//                "!AIVDM,1,1,,A,602E3U0rFKsn<P<j07,4*5A\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                AISContentHelper ach = new AISContentHelper(nmea);
                int dac = ach.getUInt(72, 82);
                int fid = ach.getUInt(82, 88);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                assertNull(tc.rollbackReason);
                assertEquals(MessageTypes.BinaryAddressedMessage, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertNull(tc.error);
            }
        }
        catch (Exception ex)
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
                AISContentHelper ach = new AISContentHelper(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                assertNull(tc.rollbackReason);
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
        catch (Exception ex)
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
                AISContentHelper ach = new AISContentHelper(nmea);
                int bits = ach.getBits();
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                //assertNull(tc.rollbackReason);
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
                assertEquals(!ach.getBoolean(142), tc.dte);
                assertEquals(ach.getBoolean(146), tc.assigned);
                assertEquals(ach.getBoolean(147), tc.raim);
                assertEquals(ach.getUInt(148, 168), tc.radio);
                assertNull(tc.error);
            }
        }
        catch (Exception ex)
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
                AISContentHelper ach = new AISContentHelper(nmea);
                int bits = ach.getBits();
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                assertNull(tc.rollbackReason);
                assertEquals(MessageTypes.UTCAndDateInquiry, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals(ach.getUInt(40, 70), tc.dest_mmsi);
                assertNull(tc.error);
            }
        }
        catch (Exception ex)
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
                AISContentHelper ach = new AISContentHelper(nmea);
                assertNull(tc.rollbackReason);
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
        catch (Exception ex)
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
                "!AIVDM,1,1,,B,@6STUk004lQ206bCKNOBAb6S,0*38\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                AISContentHelper ach = new AISContentHelper(nmea);
                int bits = ach.getBits();
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                assertNull(tc.rollbackReason);
                assertNull(tc.error);
                assertEquals(MessageTypes.AssignmentModeCommand, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals(ach.getUInt(40, 70), tc.mmsi1);
                assertEquals(ach.getUInt(70, 82), tc.offset1);
                assertEquals(ach.getUInt(82, 92), tc.increment1);
                if (bits > 92)
                {
                    assertEquals(ach.getUInt(92, 122), tc.mmsi2);
                    assertEquals(ach.getUInt(122, 134), tc.offset2);
                    assertEquals(ach.getUInt(134, 144), tc.increment2);
                }
            }
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type17()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,A,A@4<g>i:7Tcip2KBGm@`;gvG040h01h04EOw8@0L,0*28\r\n",
                "!AIVDM,1,1,,A,A04757QAv0agH2JdGn``7wrs1540vocuF@?s301G,0*0A\r\n",
                "!AIVDM,1,1,,A,A04757QAv0agH2JdGodP7Oqc4@TGw9`B70,4*2C\r\n",
                "!AIVDM,1,1,,A,A04757QAv0agH2JdGph`6OlR3Dh6wL<>IA3v<0dA,0*5E\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AISContentHelper ach = new AISContentHelper(nmea);
                assertNull(tc.rollbackReason);
                assertEquals(MessageTypes.DGNSSBinaryBroadcastMessage, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals((float)ach.getInt(40, 58)/600.0 , tc.longitude, Epsilon);
                assertEquals((float)ach.getInt(58, 75)/600.0 , tc.latitude, Epsilon);
                assertNull(tc.error);
            }
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type18()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,B,B6:fOUh0=R1oRQSC=jo9Gwb61P06,0*6F\r\n",
                "!AIVDM,1,1,,A,B43JHF0000V@sC6H3t803wbT3P06,0*27\r\n",
                "!AIVDM,1,1,,B,B43NbT0008VGWDVHNs0000N021Mk,0*6A\r\n",
                "!AIVDM,1,1,,B,B3P<0@P00GtiTD`MfuKAKwbUoP06,0*54\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AISContentHelper ach = new AISContentHelper(nmea);
                assertNull(tc.rollbackReason);
                assertEquals(MessageTypes.StandardClassBCSPositionReport, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals((float)ach.getUInt(46, 56)/10, tc.speed, Epsilon);
                assertEquals((float)ach.getInt(57, 85)/600000.0 , tc.longitude, Epsilon);
                assertEquals((float)ach.getInt(85, 112)/600000.0 , tc.latitude, Epsilon);
                assertEquals((float)ach.getUInt(112, 124)/10, tc.course, Epsilon);
                int hdg = ach.getUInt(124, 133);
                if (hdg == 511)
                {
                    hdg = -1;
                }
                else
                {
                    assertTrue(hdg >= 0 && hdg < 360);
                }
                assertEquals(hdg, tc.heading);
                int second = ach.getUInt(133, 139);
                if (second >= 60)
                {
                    second = -1;
                }
                else
                {
                    assertTrue(second >= 0 && second < 60);
                }
                assertEquals(second, tc.second);
                assertEquals(ach.getBoolean(141), tc.cs);
                assertEquals(ach.getBoolean(142), tc.display);
                assertEquals(ach.getBoolean(143), tc.dsc);
                assertEquals(ach.getBoolean(144), tc.band);
                assertEquals(ach.getBoolean(145), tc.msg22);
                assertEquals(ach.getBoolean(146), tc.assigned);
                assertEquals(ach.getBoolean(147), tc.raim);
                assertEquals(ach.getUInt(149, 168), tc.radio);
                assertNull(tc.error);
            }
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type19()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,A,C6:Vo:00@R;51>TORgH2owc6@b30jb2M111111111110S0hS440P,0*0F\r\n",
                "!AIVDM,1,1,,B,C6:fQe@0021vEpSBPJ0<sweRjb:ThL>>b2L6@bC1QeUhS3d:4707,0*07\r\n",
                "!AIVDM,1,1,,B,C>q000@0026mfP5U9isO3wgPHC0hBMh0000000000000WS86VQPP,0*70\r\n",
                "!AIVDM,1,1,,B,C6:V4mh0021mgg3CJD4gSwf2DBM0HNL?1WkU11111110S0<FRTP7,0*11\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AISContentHelper ach = new AISContentHelper(nmea);
                assertNull(tc.rollbackReason);
                assertEquals(MessageTypes.ExtendedClassBEquipmentPositionReport, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals((float)ach.getUInt(46, 56)/10, tc.speed, Epsilon);
                assertEquals((float)ach.getInt(57, 85)/600000.0 , tc.longitude, Epsilon);
                assertEquals((float)ach.getInt(85, 112)/600000.0 , tc.latitude, Epsilon);
                assertEquals((float)ach.getUInt(112, 124)/10, tc.course, Epsilon);
                int hdg = ach.getUInt(124, 133);
                if (hdg == 511)
                {
                    hdg = -1;
                }
                else
                {
                    assertTrue(hdg >= 0 && hdg < 360);
                }
                assertEquals(hdg, tc.heading);
                int second = ach.getUInt(133, 139);
                if (second >= 60)
                {
                    second = -1;
                }
                else
                {
                    assertTrue(second >= 0 && second < 60);
                }
                assertEquals(second, tc.second);
                assertEquals(ach.getString(143, 263), tc.shipname);
                assertEquals(CodesForShipType.values()[ach.getUInt(263, 271)], tc.shipType);
                assertEquals(ach.getUInt(271, 280), tc.dimensionToBow);
                assertEquals(ach.getUInt(280, 289), tc.dimensionToStern);
                assertEquals(ach.getUInt(289, 295), tc.dimensionToPort);
                assertEquals(ach.getUInt(295, 301), tc.dimensionToStarboard);
                assertEquals(EPFDFixTypes.values()[ach.getUInt(301, 305)], tc.epfd);
                assertEquals(ach.getBoolean(305), tc.raim);
                assertEquals(ach.getBoolean(306), !tc.dte);
                assertEquals(ach.getBoolean(307), tc.assigned);
                assertNull(tc.error);
            }
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type20()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,A,D02M4IiOpNfr<`N000000000000,2*4C\r\n",
                "!AIVDM,1,1,,A,D025bvQP4Dfr<`D01qlT0000001,2*47\r\n",
                "!AIVDM,1,1,,B,D02200AdhBfp00C6EGe@1qG0R9I,2*0D\r\n",
                "!AIVDM,1,1,,B,D02VqLQe@Jfp00K6EFAJ>5FUK6E,2*77\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AISContentHelper ach = new AISContentHelper(nmea);
                assertEquals(160, ach.getBits());
                assertNull(tc.rollbackReason);
                assertEquals(MessageTypes.DataLinkManagement, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals(ach.getUInt(40, 52), tc.offset1);
                assertEquals(ach.getUInt(52, 56), tc.number1);
                assertEquals(ach.getUInt(56, 59), tc.timeout1);
                assertEquals(ach.getUInt(59, 70), tc.increment1);
                assertEquals(ach.getUInt(70, 82), tc.offset2);
                assertEquals(ach.getUInt(82, 86), tc.number2);
                assertEquals(ach.getUInt(86, 89), tc.timeout2);
                assertEquals(ach.getUInt(89, 100), tc.increment2);
                assertEquals(ach.getUInt(100, 112), tc.offset3);
                assertEquals(ach.getUInt(112, 116), tc.number3);
                assertEquals(ach.getUInt(116, 119), tc.timeout3);
                assertEquals(ach.getUInt(119, 130), tc.increment3);
                assertEquals(ach.getUInt(130, 142), tc.offset4);
                assertEquals(ach.getUInt(142, 146), tc.number4);
                assertEquals(ach.getUInt(146, 149), tc.timeout4);
                assertEquals(ach.getUInt(149, 160), tc.increment4);
                assertNull(tc.error);
            }
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type21()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,A,E04<o5AaWdPnaGaP00000000000DPmHl:aCUp00000Qh20,4*64\r\n",
                "!AIVDM,1,1,,A,E02E36i`60b37a6h2HrS0ph@@@@@6eow?Rekp00003v000,4*6D\r\n",
                "!AIVDM,1,1,,A,E000`D2S0a7h22h1bV62a0P0000OwP=h;=Q`@1088;SP2P0,2*53\r\n",
                "!AIVDM,1,1,,B,E028ishVa1Qh:2W2a4S2h@@@@@@OJm<:89QcH00003v0100,2*75\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AISContentHelper ach = new AISContentHelper(nmea);
                assertNull(tc.rollbackReason);
                assertEquals(MessageTypes.AidToNavigationReport, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals(NavaidTypes.values()[ach.getUInt(38, 43)], tc.aid_type);
                assertEquals(ach.getString(43, 163), tc.name);
                assertEquals(ach.getBoolean(163), tc.accuracy);
                assertEquals((float)ach.getInt(164, 192)/600000.0 , tc.longitude, Epsilon);
                assertEquals((float)ach.getInt(192, 219)/600000.0 , tc.latitude, Epsilon);
                assertEquals(ach.getUInt(219, 228), tc.dimensionToBow);
                assertEquals(ach.getUInt(228, 237), tc.dimensionToStern);
                assertEquals(ach.getUInt(237, 243), tc.dimensionToPort);
                assertEquals(ach.getUInt(243, 249), tc.dimensionToStarboard);
                assertEquals(EPFDFixTypes.values()[ach.getUInt(249, 253)], tc.epfd);
                int second = ach.getUInt(253, 259);
                if (second >= 60)
                {
                    second = -1;
                }
                else
                {
                    assertTrue(second >= 0 && second < 60);
                }
                assertEquals(second, tc.second);
                assertEquals(ach.getBoolean(259), tc.off_position);
                assertEquals(ach.getBoolean(268), tc.raim);
                assertEquals(ach.getBoolean(269), tc.virtual_aid);
                assertEquals(ach.getBoolean(270), tc.assigned);
                int bits = ach.getBits();
                if (bits > 272)
                {
                    assertEquals(ach.getString(272, bits), tc.name_ext);
                }
                assertNull(tc.error);
            }
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void type22()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,B,F030p?j2N2P73FiiNesU3FR10000,0*32\r\n",
                "!AIVDM,1,1,,B,F030p2j2N2P6Ubib@=4q35b1P000,0*61\r\n",
                "!AIVDM,1,1,,A,F030owj2N2P6Ubib@=4q35b10000,0*58\r\n",
                "!AIVDM,1,1,,B,F030pCB2N2P5iQAoR;H6SQ01P000,0*68\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                AISContentHelper ach = new AISContentHelper(nmea);
                assertNull(tc.rollbackReason);
                assertEquals(MessageTypes.ChannelManagement, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals(ach.getUInt(40, 52), tc.channelA);
                assertEquals(ach.getUInt(52, 64), tc.channelB);
                assertEquals(TransceiverModes.values()[ach.getUInt(64, 68)], tc.transceiverMode);
                assertEquals(ach.getBoolean(68), tc.power);
                assertEquals(ach.getBoolean(139), tc.addressed);
                if (tc.addressed)
                {
                    assertEquals(ach.getUInt(69, 104), tc.mmsi1);
                    assertEquals(ach.getUInt(104, 134), tc.mmsi2);
                }
                else
                {
                    assertEquals((float)ach.getInt(69, 87)/600.0 , tc.neLongitude, Epsilon);
                    assertEquals((float)ach.getInt(87, 104)/600.0 , tc.neLatitude, Epsilon);
                    assertEquals((float)ach.getInt(104, 122)/600.0 , tc.swLongitude, Epsilon);
                    assertEquals((float)ach.getInt(122, 139)/600.0 , tc.swLatitude, Epsilon);
                }
                assertEquals(ach.getBoolean(140), tc.channelABand);
                assertEquals(ach.getBoolean(141), tc.channelBBand);
                assertEquals(ach.getUInt(142, 145), tc.zoneSize);
                assertNull(tc.error);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    @Test
    public void type24()
    {
        try
        {
            String[] nmeas = new String[] {
                "!AIVDM,1,1,,B,H>DQ@04N6DeihhlPPPPPPP000000,0*0E\r\n",
                "!AIVDM,1,1,,A,H7P<1>1LPU@D8U8A<0000000000,2*6C\r\n",
                "!AIVDM,1,1,,A,H7P<1>4UB1I0000F=Aqpoo2P2220,0*3A\r\n",
                "!AIVDM,1,1,,B,H0HN<8QLTdTpN22222222222223,2*1B\r\n"
            };
            for (String nmea : nmeas)
            {
                System.err.println(nmea);
                AISContentHelper ach = new AISContentHelper(nmea);
                int bits = ach.getBits();
                TC tc = new TC();
                parser.parse(nmea, null, tc);
                assertNull(tc.rollbackReason);
                assertEquals(MessageTypes.StaticDataReport, tc.messageType);
                assertEquals(ach.getUInt(8, 38), tc.mmsi);
                assertEquals(ach.getUInt(38, 40), tc.partno);
                assertTrue( tc.partno >= 0 && tc.partno <= 1);
                if (tc.partno == 0)
                {
                    assertEquals(ach.getString(40, 160), tc.shipname);
                }
                else
                {
                    assertEquals(CodesForShipType.values()[ach.getUInt(40, 48)], tc.shipType);
                    assertEquals(ach.getString(48, 66), tc.vendorid);
                    assertEquals(ach.getUInt(66, 70), tc.model);
                    assertEquals(ach.getUInt(70, 90), tc.serial);
                    assertEquals(ach.getString(90, 132), tc.callSign);
                    if (MMSIType.getType(tc.mmsi) == CraftAssociatedWithParentShip)
                    {
                        assertEquals(ach.getUInt(132, 162), tc.mothershipMMSI);
                    }
                    else
                    {
                        assertEquals(ach.getUInt(132, 141), tc.dimensionToBow);
                        assertEquals(ach.getUInt(141, 150), tc.dimensionToStern);
                        assertEquals(ach.getUInt(150, 156), tc.dimensionToPort);
                        assertEquals(ach.getUInt(156, 162), tc.dimensionToStarboard);
                    }
                }
                assertNull(tc.error);
            }
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    @Test
    public void err0()
    {
        try
        {
            List<Object> list = getExpected();
            String nmea = 
                "$GPRMC,062455,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*78\r\n"+
                "!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"+
                "!AIVDM,1,1,,A,133sVfPP00PD>hRMDH@jNOvN20S8,0*7F\r\n"+
                "!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09\r\n"+
                    "!AIVDM,2,2,9,B,888888888888880,2*2E\r\n"+
                "$GPRMC,062457,A,6009.2053,N,02453.6493,E,012.0,001.3,171009,,,A*7D\r\n"+   // err
                "!AIVDM,1,1,,A,13aDr=PP00PGIljMhwO3F?wN20RJ,0*62\r\n"+
                "!AIVDM,2,1,0,A,802R5Ph0BkHgL@PCQ:GaOwwwwwwwwwww2k8wwwwwwwwwwwwwwwwwwwww,0*3A\r\n"+
                "!AIVDM,2,2,0,A,wwt,2*60\r\n"+
                "!AIVDM,1,1,,B,16:@?m001o85tmL<SbP5OlHN25Ip,0*7F\r\n"+
                "$GPRTE,2,1,c,0,W3IWI,DRIVWY,32CEDR,32-29,32BKLD,32-I95,32-US1,BW-32,BW-198*69\r\n"+
                "!AIVDM,1,1,,A,133w;`PP00PCqghMcqNqdOvPR5Ip,0*65\r\n"+
                "!AIVDM,1,1,,B,139eb:PP00PIHDNMdd6@0?vN2D2s,0*43\r\n"
            ;
            ListStorage ls = new ListStorage();
            AISObserver tc = ls.getStorage(AISObserver.class);
            parser.parse(nmea, null, tc);
            assertEquals(list, ls.getProperty("mmsi"));
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    */
    @Test
    public void err1()
    {
        try
        {
            List<Object> list = getExpected();
            String nmea = 
                "$GPRMC,062455,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*78\r\n"+
      //          "!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"+
        //        "!AIVDM,1,1,,A,133sVfPP00PD>hRMDH@jNOvN20S8,0*7F\r\n"+
                "!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*02\r\n"+ // wrong checksum
                    "!AIVDM,2,2,9,B,888888888888880,2*2E\r\n"+
                "$GPRMC,062457,A,6009.2053,N,02453.6493,E,012.0,001.3,171009,,,A*7D\r\n"+   // err
                "!AIVDM,1,1,,A,13aDr=PP00PGIljMhwO3F?wN20RJ,0*62\r\n"+
                "!AIVDM,2,1,0,A,802R5Ph0BkHgL@PCQ:GaOwwwwwwwwwww2k8wwwwwwwwwwwwwwwwwwwww,0*3A\r\n"+
                "!AIVDM,2,2,0,A,wwt,2*60\r\n"+
                "!AIVDM,1,1,,B,16:@?m001o85tmL<SbP5OlHN25Ip,0*7F\r\n"+
                "$GPRTE,2,1,c,0,W3IWI,DRIVWY,32CEDR,32-29,32BKLD,32-I95,32-US1,BW-32,BW-198*69\r\n"+
                "!AIVDM,1,1,,A,133w;`PP00PCqghMcqNqdOvPR5Ip,0*60\r\n"+  // err
                "!AIVDM,1,1,,B,139eb:PP00PIHDNMdd6@0?vN2D2s,0*43\r\n"
            ;
            list.remove(6);
            list.remove(2);
            ListStorage ls = new ListStorage();
            AISObserver tc = ls.getStorage(AISObserver.class);
            parser.parse(nmea, null, tc);
            assertEquals(list, ls.getProperty("mmsi"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    /*
    @Test
    public void err2()
    {
        try
        {
            List<Object> list = getExpected();
            String nmea = 
                "$GPRMC,062455,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*78\r\n"+
                "!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"+
                "!AIVDM,1,1,,A,133sVfPP00PD>hRMDH@jNOvN20S8,0*7F\r\n"+
                "!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09\r\n"+
                    //"!AIVDM,2,2,9,B,888888888888880,2*2E\r\n"+ missing
                "$GPRMC,062457,A,6009.2053,N,02453.6493,E,012.0,001.3,171009,,,A*7D\r\n"+   // err
                "!AIVDM,1,1,,A,13aDr=PP00PGIljMhwO3F?wN20RJ,0*62\r\n"+
                "!AIVDM,2,1,0,A,802R5Ph0BkHgL@PCQ:GaOwwwwwwwwwww2k8wwwwwwwwwwwwwwwwwwwww,0*3A\r\n"+
                "!AIVDM,2,2,0,A,wwt,0*60\r\n"+  // syntax
                "!AIVDM,1,1,,B,16:@?m001o85tmL<SbP5OlHN25Ip,0*7F\r\n"+
                "$GPRTE,2,1,c,0,W3IWI,DRIVWY,32CEDR,32-29,32BKLD,32-I95,32-US1,BW-32,BW-198*69\r\n"+
                "!AIVDM,1,1,,A,133w;`PP00PCqghMcqNqdOvPR5Ip,0*65\r\n"+
                "!AIVDM,1,1,,B,139eb:PP00PIHDNMdd6@0?vN2D2s,0*43\r\n"
            ;
            list.remove(4);
            list.remove(2);
            ListStorage ls = new ListStorage();
            AISObserver tc = ls.getStorage(AISObserver.class);
            parser.parse(nmea, null, tc);
            assertEquals(list, ls.getProperty("mmsi"));
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }

    @Test
    public void err3()
    {
        try
        {
            List<Object> list = getExpected();
            String nmea = 
                "$GPRMC,062455,A,6009.2054,N,02453.6493,E,000.0,001.3,171009,,,A*78\r\n"+
                "!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"+
                "!AIVDM,1,1,,A,N33sVfPP00PD>hRMDH@jNOvN20S8,0*7F\r\n"+  // err
                //"!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09\r\n"+ missing
                    "!AIVDM,2,2,9,B,888888888888880,2*2E\r\n"+
                "$GPRMC,062457,A,6009.2053,N,02453.6493,E,012.0,001.3,171009,,,A*7D\r\n"+   // err
                "!AIVDM,1,1,,A,13aDr=PP00PGIljMhwO3F?wN20RJ,0*62\r\n"+
                //"!AIVDM,2,1,0,A,802R5Ph0BkHgL@PCQ:GaOwwwwwwwwwww2k8wwwwwwwwwwwwwwwwwwwww,0*3A\r\n"+ missing
                "!AIVDM,2,2,0,A,wwt,2*60\r\n"+
                "!AIVDM,1,1,,B,16:@?m001o85tmL<SbP5OlHN25Ip,0*7F\r\n"+
                "$GPRTE,2,1,c,0,W3IWI,DRIVWY,32CEDR,32-29,32BKLD,32-I95,32-US1,BW-32,BW-198*69\r\n"+
                "!AIVDM,1,1,,A,133w;`PP00PCqghMcqNqdOvPR5Ip,0*65\r\n"+
                "!AIVDM,1,1,,B,139eb:PP00PIHDNMdd6@0?vN2D2s,0*43\r\n"
            ;
            list.remove(4);
            list.remove(2);
            list.remove(1);
            MapListPropertySetter ls = new MapListPropertySetter();
            AISObserverImpl tc = AISObserverImpl.getInstance(AISObserverImpl.class);
            tc.addObserver(ls, "");
            parser.parse(nmea, null, tc);
            assertEquals(list, ls.getProperty("mmsi"));
        }
        catch (Exception ex)
        {
            fail(ex.getMessage());
        }
    }
    */
    private List<Object> getExpected()
    {
        String[] nmeas = new String[] {
            "!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n",
            "!AIVDM,1,1,,A,133sVfPP00PD>hRMDH@jNOvN20S8,0*7F\r\n",
            "!AIVDM,2,1,9,B,53nFBv01SJ<thHp6220H4heHTf2222222222221?50:454o<`9QSlUDp,0*09\r\n"+
                "!AIVDM,2,2,9,B,888888888888880,2*2E\r\n",
            "!AIVDM,1,1,,A,13aDr=PP00PGIljMhwO3F?wN20RJ,0*62\r\n",
            "!AIVDM,2,1,0,A,802R5Ph0BkHgL@PCQ:GaOwwwwwwwwwww2k8wwwwwwwwwwwwwwwwwwwww,0*3A\r\n"+
            "!AIVDM,2,2,0,A,wwt,2*60\r\n",
            "!AIVDM,1,1,,B,16:@?m001o85tmL<SbP5OlHN25Ip,0*7F\r\n",
            "!AIVDM,1,1,,A,133w;`PP00PCqghMcqNqdOvPR5Ip,0*65\r\n",
            "!AIVDM,1,1,,B,139eb:PP00PIHDNMdd6@0?vN2D2s,0*43\r\n"
        };
        List<Object> list = new ArrayList<>();
        for (String nmea : nmeas)
        {
            AISContentHelper ach = new AISContentHelper(nmea);
            list.add(ach.getUInt(8, 38));
        }
        return list;
    }
}
