/*
 * Copyright (C) 2013 Timo Vesalainen
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
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.RecoverMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.annotation.Terminals;
import org.vesalainen.parser.util.InputReader;

/**
 * @author Timo Vesalainen
 */
@GenClassname("org.vesalainen.parsers.nmea.ais.AISParserImpl")
@GrammarDef //(grammarClass=AISGrammar.class)
@Terminals({
@Terminal(left="increment2_10", expression="[01]{10}", doc="Increment B", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIncrement2_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tminute", expression="[01]{6}", doc="To minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTminute(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="increment2_11", expression="[01]{11}", doc="Increment", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIncrement2_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="idtype", expression="[01]{2}", doc="Identifier type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIdtype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cloudbase", expression="[01]{7}", doc="Height of cloud base", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCloudbase_U2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressure_9", expression="[01]{9}", doc="Air Pressure", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPressure_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="berth_name", expression="[01]{120}", doc="Name of Berth", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBerthName(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lightoil", expression="[01]{2}", doc="Light Fuel Oil Bunkered", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLightoil(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="radio_19", expression="[01]{19}", doc="Radio status", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRadio_19(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swheight", expression="[01]{8}", doc="Swell Height", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwheight_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="navrepair", expression="[01]{2}", doc="Navigation repair", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNavrepair(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sounder_state", expression="[01]{2}", doc="Echo sounder", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSounderState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="right", expression="[01]{9}", doc="Right boundary", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRight(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fminute", expression="[01]{6}", doc="From minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFminute(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cnorth1", expression="[01]{8}", doc="Current Vector component North (u) #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCnorth1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lday", expression="[01]{5}", doc="ETA day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLday(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_min", expression="[01]{6}", doc="To UTC Minute", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisToMin(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="watertemp", expression="[01]{10}", doc="Water Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWatertemp_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lading", expression="[01]{2}", doc="Laden or Ballast", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLading(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shiptype", expression="[01]{8}", doc="Ship Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShiptype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="portname", expression="[01]{120}", doc="Name of Port & Berth", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPortname(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nday", expression="[01]{5}", doc="ETA day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNday(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fwdir", expression="[01]{9}", doc="Forecast Wind Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFwdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_starboard", expression="[01]{6}", doc="Dimension to Starboard", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisToStarboard(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="description_6_966", expression="[01]{6,966}", doc="Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDescription_6_966(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="7", expression="000111", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="6", expression="000110", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="5", expression="000101", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="4", expression="000100", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="heavyoil", expression="[01]{2}", doc="Heavy Fuel Oil Bunkered", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHeavyoil(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="type2_1", expression="[01]{6}", doc="First message type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType21(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="xband_state", expression="[01]{2}", doc="Radar (X-band)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisXbandState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="9", expression="001001", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="8", expression="001000", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cnorth2", expression="[01]{8}", doc="Current Vector component North (u) #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCnorth2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="retransmit", expression="[01]{1}", doc="Retransmit flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRetransmit(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nextport", expression="[01]{30}", doc="Next Port Of Call", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNextport(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="alt_11", expression="[01]{11}", doc="Altitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAlt_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="alt_12", expression="[01]{12}", doc="Altitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAlt_12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="seaice", expression="[01]{5}", doc="Sea Ice Concentration", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSeaice(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ata_state", expression="[01]{2}", doc="Automatic Tracking Aid", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAtaState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid20", expression="010100", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid12", expression="001100", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid13", expression="001101", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nmonth", expression="[01]{4}", doc="ETA month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNmonth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="day_6", expression="[01]{6}", doc="UTC Day", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDay_6(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swperiod", expression="[01]{6}", doc="Swell Period", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwperiod(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="day_5", expression="[01]{5}", doc="Day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDay_5(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="conductivity", expression="[01]{10}", doc="Conductivity", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisConductivity_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid16", expression="010000", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid17", expression="010001", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid14", expression="001110", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid15", expression="001111", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icetype", expression="[01]{4}", doc="Amount and Type of Ice", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIcetype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid18", expression="010010", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="left", expression="[01]{9}", doc="Left boundary", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLeft(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="aid_type", expression="[01]{5}", doc="Aid type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAidType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid19", expression="010011", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="customs", expression="[01]{2}", doc="Customs house", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCustoms(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="status_4", expression="[01]{4}", doc="Navigation Status", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisStatus_4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="status_2", expression="[01]{2}", doc="Status of Signal", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisStatus_2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="duration_8", expression="[01]{8}", doc="Duration", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDuration_8(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shipname", expression="[01]{120}", doc="Vessel Name", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShipname(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="hour", expression="[01]{5}", doc="Hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout2", expression="[01]{3}", doc="Time-out", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTimeout2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout1", expression="[01]{3}", doc="Time-out", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTimeout1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout4", expression="[01]{3}", doc="Time-out", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTimeout4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ais_state", expression="[01]{2}", doc="AIS Class A", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAisState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout3", expression="[01]{3}", doc="Time-out", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTimeout3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed1", expression="[01]{8}", doc="Current Speed #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCspeed1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed3", expression="[01]{8}", doc="Current Speed #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCspeed3_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed2", expression="[01]{8}", doc="Current Speed #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCspeed2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="steer_state", expression="[01]{2}", doc="Emergency steering gear", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSteerState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="hazardouswaste", expression="[01]{2}", doc="Waste disposal (hazardous)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHazardouswaste(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sender", expression="[01]{3}", doc="Sender Class", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSender(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional1", expression="[01]{2}", doc="Regional reserved 1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRegional1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional2", expression="[01]{2}", doc="Regional reserved 2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRegional2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="medical", expression="[01]{2}", doc="Medical facilities", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMedical(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="band", expression="[01]{1}", doc="Band flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBand(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="band_a", expression="[01]{1}", doc="Channel A Band", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBandA(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="speed_10", expression="[01]{10}", doc="Speed Over Ground (SOG)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSpeed_U1_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="speed1", expression="[01]{8}", doc="Current Speed #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSpeed1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="band_b", expression="[01]{1}", doc="Channel B Band", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBandB(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="liquidwaste", expression="[01]{2}", doc="Waste disposal (liquid)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLiquidwaste(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset4", expression="[01]{12}", doc="Offset number 4", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOffset4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset3", expression="[01]{12}", doc="Offset number 3", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOffset3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="vislimit", expression="[01]{1}", doc="Visibility Limit", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVislimit(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="station", expression="[01]{120}", doc="Name of Signal Station", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisStation(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sw_lon", expression="[01]{18}", doc="SW Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwLon_I1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="display", expression="[01]{1}", doc="Display flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDisplay(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="radius_10", expression="[01]{10}", doc="Radius extension", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRadius_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="agent", expression="[01]{2}", doc="Agent", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAgent(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="radius_12", expression="[01]{12}", doc="Radius", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRadius_12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cs", expression="[01]{1}", doc="CS Unit", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCs(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airdraught_13", expression="[01]{13}", doc="Air Draught", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAirdraught_U1_13(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airdraught_11", expression="[01]{11}", doc="Air Draught", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAirdraught_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="name_84", expression="[01]{84}", doc="Name", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisName_84(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wgustdir", expression="[01]{9}", doc="Wind Gust Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWgustdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="txrx_2", expression="[01]{2}", doc="Tx/Rx Mode", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTxrx_2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="txrx_4", expression="[01]{4}", doc="Tx/Rx mode", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTxrx_4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="twinddir", expression="[01]{7}", doc="True Wind Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTwinddir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="increment3", expression="[01]{11}", doc="Increment", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIncrement3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="increment4", expression="[01]{11}", doc="Increment", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIncrement4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nextsignal", expression="[01]{5}", doc="Expected Next Signal", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNextsignal(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="future1", expression="[01]{2}", doc="Reserved for future", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFuture1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nhour", expression="[01]{5}", doc="ETA hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNhour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="future2", expression="[01]{2}", doc="Reserved for future", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFuture2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ice", expression="[01]{2}", doc="Ice", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIce(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icedevel", expression="[01]{5}", doc="Ice Development", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIcedevel(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="iceclass", expression="[01]{4}", doc="Ice Class", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIceclass(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fuel", expression="[01]{2}", doc="Bunker/fuel", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFuel(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_stern", expression="[01]{9}", doc="Dimension to Stern", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisToStern(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swheight2", expression="[01]{6}", doc="Second Swell Height", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwheight2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="berth_depth", expression="[01]{8}", doc="Berth Water Depth", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBerthDepth_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swheight1", expression="[01]{6}", doc="First Swell Height", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwheight1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="precision", expression="[01]{3}", doc="Precision", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPrecision(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="assigned", expression="[01]{1}", doc="Assigned", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAssigned(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="persons", expression="[01]{13}", doc="# persons on board", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPersons(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="solidwaste", expression="[01]{2}", doc="Waste disposal (solid)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSolidwaste(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tonnage", expression="[01]{18}", doc="Gross Tonnage", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTonnage(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="leveltrend", expression="[01]{2}", doc="Water Level Trend", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLeveltrend(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="rwinddir", expression="[01]{7}", doc="Relative Wind Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRwinddir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="reason", expression="[01]{120}", doc="Reason For Closing", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisReason(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waterlog_state", expression="[01]{2}", doc="Speed Log through water", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWaterlogState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth3_5", expression="[01]{5}", doc="Measurement Depth #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdepth3_U1_5(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icecause", expression="[01]{3}", doc="Cause of Ice Accretion", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIcecause(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth3_9", expression="[01]{9}", doc="Measurement Depth #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdepth3_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wdir", expression="[01]{9}", doc="Wind Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sensor", expression="[01]{4}", doc="Sensor Report Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSensor(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="course_12", expression="[01]{12}", doc="Course Over Ground (COG)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCourse_U1_12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="orientation", expression="[01]{9}", doc="Orientation", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOrientation(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="secondport", expression="[01]{30}", doc="Second Port Of Call", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSecondport(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wmo1", expression="1", doc="Variant", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWmo(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth2_9", expression="[01]{9}", doc="Measurement Depth #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdepth2_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wmo0", expression="0", doc="Variant", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWmo(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth2_5", expression="[01]{5}", doc="Measurement Depth #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdepth2_U1_5(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swperiod2", expression="[01]{5}", doc="Second Swell Period", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwperiod2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swperiod1", expression="[01]{5}", doc="First Swell Period", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwperiod1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dewpoint", expression="[01]{10}", doc="Dew Point", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDewpoint(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="month", expression="[01]{4}", doc="Month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMonth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="berth_length", expression="[01]{9}", doc="Berth length", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBerthLength(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="grndlog_state", expression="[01]{2}", doc="Speed Log over ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisGrndlogState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="interval", expression="[01]{4}", doc="Report Interval", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisInterval(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="minute_3", expression="[01]{3}", doc="UTC minute", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMinute_3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="minute_6", expression="[01]{6}", doc="Minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMinute_6(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdir1", expression="[01]{9}", doc="Current Direction #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdir1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdir3", expression="[01]{9}", doc="Current Direction #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdir3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdir2", expression="[01]{9}", doc="Current Direction #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdir2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airtemp_10", expression="[01]{10}", doc="Air Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAirtemp_U1_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airtemp_11", expression="[01]{11}", doc="Air Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAirtemp_U1_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ptend", expression="[01]{4}", doc="Pressure Tendency", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPtend(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="hfradio_state", expression="[01]{2}", doc="Radio HF", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHfradioState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="text_968", expression="[01]{968}", doc="Text", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisText_968(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="distance", expression="[01]{10}", doc="Distance", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDistance(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tmonth", expression="[01]{4}", doc="To month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTmonth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi4", expression="[01]{30}", doc="MMSI number 4", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMmsi4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="water", expression="[01]{2}", doc="Potable water", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWater(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ceast1", expression="[01]{8}", doc="Current Vector component East (v) #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCeast1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lowcloudt", expression="[01]{6}", doc="Cloud type (low)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLowcloudt(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="seastate", expression="[01]{4}", doc="Sea state", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSeastate(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi2", expression="[01]{30}", doc="MMSI number 2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMmsi2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi3", expression="[01]{30}", doc="MMSI number 3", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMmsi3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ceast2", expression="[01]{8}", doc="Current Vector component East (v) #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCeast2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lminute", expression="[01]{6}", doc="ETA minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLminute(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi1", expression="[01]{30}", doc="MMSI number 1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMmsi1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dangerous", expression="[01]{120}", doc="Main Dangerous Good", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDangerous(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="reserved", expression="[01]{8}", doc="Regional Reserved", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisReserved(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ne_lat", expression="[01]{17}", doc="NE Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNeLat_I1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="lowclouda", expression="[01]{4}", doc="Cloud amount (low)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLowclouda(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="additional", expression="[01]{2}", doc="Additional services", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAdditional(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dac001", expression="0000000001", doc="DAC", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDac(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="partno1", expression="01", doc="Part Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPartno(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="partno0", expression="00", doc="Part Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPartno(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="accuracy", expression="[01]{1}", doc="Position Accuracy", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAccuracy(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="imo", expression="[01]{30}", doc="IMO Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisImo(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waveheight", expression="[01]{8}", doc="Wave height", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWaveheight_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="second", expression="[01]{6}", doc="Time Stamp", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSecond(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="location", expression="[01]{120}", doc="Location", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLocation(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="thd_state", expression="[01]{2}", doc="THD", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisThdState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waycount", expression="[01]{5}", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWaycount(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="callsign", expression="[01]{42}", doc="Call Sign", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCallsign(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="arpa_state", expression="[01]{2}", doc="Radar (ARPA)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisArpaState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waveperiod", expression="[01]{6}", doc="Wave period", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWaveperiod(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="provisions", expression="[01]{2}", doc="Provisions", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisProvisions(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wspeed", expression="[01]{7}", doc="Average Wind Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWspeed(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="gnss", expression="[01]{1}", doc="GNSS Position status", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisGnss(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ais_version", expression="[01]{2}", doc="AIS Version", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAisVersion(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="linkage", expression="[01]{10}", doc="Message Linkage ID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLinkage(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi", expression="[01]{30}", doc="MMSI", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMmsi(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="thour", expression="[01]{5}", doc="To hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisThour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset2_1", expression="[01]{12}", doc="First slot offset", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOffset21(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ecdisb_state", expression="[01]{2}", doc="ECDIS Back-up", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisEcdisbState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="owner", expression="[01]{4}", doc="Sensor owner", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOwner(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="station_type", expression="[01]{4}", doc="Station Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisStationType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sband_state", expression="[01]{2}", doc="Radar (S-band)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSbandState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="destination_120", expression="[01]{120}", doc="Destination", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDestination_120(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="epaid_state", expression="[01]{2}", doc="Electronic plotting aid", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisEpaidState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="vdr_state", expression="[01]{2}", doc="VDR/S-VDR", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVdrState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="text_936", expression="[01]{936}", doc="Text", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisText_936(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="text_84", expression="[01]{84}", doc="Text", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisText_84(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="scale", expression="[01]{2}", doc="Scale factor", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisScale(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="description_6_930", expression="[01]{6,930}", doc="Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDescription_6_930(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="rtype", expression="[01]{5}", doc="Route Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRtype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mothership_mmsi", expression="[01]{30}", doc="Mothership MMSI", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMothershipMmsi(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="berth_lon", expression="[01]{25}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBerthLon_I3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="id", expression="[01]{42}", doc="Target identifier", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisId(long,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="direction1", expression="[01]{9}", doc="Current Direction #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDirection1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="east", expression="[01]{8}", doc="E dimension", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisEast(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="visibility_6", expression="[01]{6}", doc="Horiz. Visibility", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVisibility_U2_6(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="salinitytype", expression="[01]{2}", doc="Salinity Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSalinitytype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="visibility_8", expression="[01]{8}", doc="Horiz. Visibility", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVisibility_U1_8(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="visibility_7", expression="[01]{7}", doc="Horiz. Visibility", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVisibility_U1_7(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="zonesize", expression="[01]{3}", doc="Zone size", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisZonesize(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="visgreater", expression="[01]{7}", doc="Max. visibility", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVisgreater(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="extunit", expression="[01]{2}", doc="Unit of extension", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisExtunit(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="electrical", expression="[01]{2}", doc="Electrical", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisElectrical(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dieseloil", expression="[01]{2}", doc="Diesel Oil Bunkered", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDieseloil(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="radio_20", expression="[01]{20}", doc="Radio status", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRadio_20(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout", expression="[01]{3}", doc="Data timeout", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTimeout(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="horsepower", expression="[01]{18}", doc="Shaft Horsepower", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHorsepower(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="gaptrend", expression="[01]{2}", doc="Air Gap Trend", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisGaptrend(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset1_2", expression="[01]{12}", doc="Second slot offset", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOffset12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset1_1", expression="[01]{12}", doc="First slot offset", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOffset11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wwheight", expression="[01]{6}", doc="Height of Wind Waves", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWwheight(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lastport", expression="[01]{30}", doc="Last Port Of Call", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLastport(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="magcomp_state", expression="[01]{2}", doc="Magnetic compass", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMagcompState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="temperature", expression="[01]{11}", doc="Air Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTemperature(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="unid", expression="[01]{13}", doc="UN Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisUnid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dewtype", expression="[01]{3}", doc="Dewpoint Sensor Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDewtype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="number4", expression="[01]{4}", doc="Reserved slots", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNumber4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="number3", expression="[01]{4}", doc="Reserved slots", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNumber3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="number2", expression="[01]{4}", doc="Reserved slots", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNumber2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="number1", expression="[01]{4}", doc="Reserved slots", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNumber1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lon_25", expression="[01]{25}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLon_I3_25(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="lon_28", expression="[01]{28}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLon_I4_28(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="year", expression="[01]{14}", doc="Year (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisYear(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waterlevel_12", expression="[01]{12}", doc="Water Level", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWaterlevel_U2_12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="turn", expression="[01]{8}", doc="Rate of Turn (ROT)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTurn_I3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="weather_9", expression="[01]{9}", doc="Present Weather", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWeather_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="weather_4", expression="[01]{4}", doc="Present Weather", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWeather_4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="vhfchan", expression="[01]{12}", doc="VHF Working Channel", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVhfchan(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="22", expression="010110", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="23", expression="010111", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="heading_9", expression="[01]{9}", doc="True Heading (HDG)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHeading_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="24", expression="011000", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="quiet", expression="[01]{4}", doc="Quiet Time", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisQuiet(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="27", expression="011011", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airgap", expression="[01]{13}", doc="Air Gap", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAirgap_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="heading_7", expression="[01]{7}", doc="Heading of the ship", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHeading_7(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="gyro_state", expression="[01]{2}", doc="Gyro compass", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisGyroState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="increment1_11", expression="[01]{11}", doc="Increment", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIncrement1_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lon_16", expression="[01]{16}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLon_I3_16(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="increment1_10", expression="[01]{10}", doc="Increment A", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIncrement1_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="crane", expression="[01]{2}", doc="Crane(s)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCrane(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lon_18", expression="[01]{18}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLon_I1_18(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="lrit_state", expression="[01]{2}", doc="LRIT", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLritState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="chart_state", expression="[01]{2}", doc="Paper Nautical Chart", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisChartState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="imdcat", expression="[01]{24}", doc="IMD Category", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisImdcat(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="19", expression="010011", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="17", expression="010001", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="data", expression="[01]{736}", doc="Payload", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisData(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="18", expression="010010", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="15", expression="001111", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="16", expression="010000", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="14", expression="001110", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="virtual_aid", expression="[01]{1}", doc="Virtual-aid flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVirtualAid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="11", expression="001011", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="12", expression="001100", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="21", expression="010101", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="20", expression="010100", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wwperiod", expression="[01]{5}", doc="Period of Wind Waves", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWwperiod(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wgust", expression="[01]{7}", doc="Gust Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWgust(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="notice", expression="[01]{7}", doc="Notice Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNotice(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pdelta", expression="[01]{10}", doc="Pressure Change", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPdelta_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="site", expression="[01]{7}", doc="Site ID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSite(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ship_type", expression="[01]{8}", doc="Ship Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShipType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="depthtype", expression="[01]{3}", doc="Depth Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDepthtype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waterlevel_9", expression="[01]{9}", doc="Water Level", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWaterlevel_U1_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fwspeed", expression="[01]{7}", doc="Forecast Wind Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFwspeed(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tday", expression="[01]{5}", doc="To day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTday(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_bow", expression="[01]{9}", doc="Dimension to Bow", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisToBow(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="10", expression="001010", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fday", expression="[01]{5}", doc="From day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFday(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icestate", expression="[01]{5}", doc="Ice Situation", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIcestate(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swelldir2", expression="[01]{6}", doc="Second Swell Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwelldir2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="rwindspeed", expression="[01]{8}", doc="Relative Wind Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRwindspeed_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swelldir1", expression="[01]{6}", doc="First Swell Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwelldir1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ballast", expression="[01]{2}", doc="Reserved ballast exchange", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBallast(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="speed_5", expression="[01]{5}", doc="Speed Over Ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSpeed_U1_5(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="speed_6", expression="[01]{6}", doc="Speed Over Ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSpeed_6(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="amount", expression="[01]{10}", doc="Amount of Cargo", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAmount(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dte", expression="[01]{1}", doc="DTE", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDte(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="twindspeed", expression="[01]{8}", doc="True Wind Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTwindspeed(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cup2", expression="[01]{8}", doc="Current Vector component Up (z) #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCup2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="seqno", expression="[01]{2}", doc="Sequence Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSeqno(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional_8", expression="[01]{8}", doc="Regional reserved", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRegional_8(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cup1", expression="[01]{8}", doc="Current Vector component Up (z) #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCup1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="totalcloud", expression="[01]{4}", doc="Total Cloud Cover", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTotalcloud(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional_2", expression="[01]{2}", doc="Regional reserved", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRegional_2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional_4", expression="[01]{4}", doc="Regional reserved", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRegional_4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="closeto", expression="[01]{120}", doc="Location of Closing To", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCloseto(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icedeposit", expression="[01]{7}", doc="Ice deposit (thickness)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIcedeposit(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="draught", expression="[01]{8}", doc="Draught", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDraught_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="signal", expression="[01]{5}", doc="Signal In Service", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSignal(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="repeat", expression="[01]{2}", doc="Repeat Indicator", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRepeat(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swelltype", expression="[01]{3}", doc="Swell Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwelltype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="distance1", expression="[01]{7}", doc="Current Distance #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDistance1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ne_lon", expression="[01]{18}", doc="NE Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNeLon_I1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="cdir", expression="[01]{9}", doc="Current Dir. Predicted", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="raim", expression="[01]{1}", doc="RAIM flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRaim(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth1", expression="[01]{9}", doc="Measurement Depth #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdepth1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="epfd", expression="[01]{4}", doc="Type of EPFD", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisEpfd(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape2", expression="010", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape1", expression="001", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressure_16", expression="[01]{16}", doc="Water Pressure", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPressure_U1_16(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape4", expression="100", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape3", expression="011", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressure_11", expression="[01]{11}", doc="Pressure at sea level", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPressure_U1_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape5", expression="101", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="msg22", expression="[01]{1}", doc="Message 22 flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMsg22(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="unit", expression="[01]{2}", doc="Unit of Quantity", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisUnit(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shiprepair", expression="[01]{2}", doc="Ship repair", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShiprepair(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mfradio_state", expression="[01]{2}", doc="Radio MF", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMfradioState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape0", expression="000", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="chandler", expression="[01]{2}", doc="Chandler", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisChandler(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="inmarsat_state", expression="[01]{2}", doc="Radio INMARSAT", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisInmarsatState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dsc", expression="[01]{1}", doc="DSC Flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDsc(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_port", expression="[01]{6}", doc="Dimension to Port", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisToPort(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nminute", expression="[01]{6}", doc="ETA minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNminute(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="course_7", expression="[01]{7}", doc="Course Over Ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCourse_7(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="highcloudt", expression="[01]{6}", doc="Cloud type (high)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHighcloudt(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="payload", expression="[01]{85}", doc="Sensor payload", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPayload(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="addressed", expression="[01]{1}", doc="Addressed", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAddressed(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swellheight", expression="[01]{8}", doc="Swell height", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwellheight_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="course_9", expression="[01]{9}", doc="Course Over Ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCourse_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pweather2", expression="[01]{5}", doc="Past Weather 2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPweather2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pweather1", expression="[01]{5}", doc="Past Weather 1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPweather1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fhour", expression="[01]{5}", doc="From hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFhour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="surveyor", expression="[01]{2}", doc="Surveyor", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSurveyor(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="channel_a", expression="[01]{12}", doc="Channel A", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisChannelA(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icebearing", expression="[01]{4}", doc="Bearing of Ice Edge", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIcebearing(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="channel_b", expression="[01]{12}", doc="Channel B", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisChannelB(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="destination_30", expression="[01]{30}", doc="Destination", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDestination_30(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="navtex_state", expression="[01]{2}", doc="NAVTEX", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNavtexState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sw_lat", expression="[01]{17}", doc="SW Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwLat_I1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="fmonth", expression="[01]{4}", doc="From month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFmonth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="humidity", expression="[01]{7}", doc="Relative Humidity", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHumidity(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="vendorid", expression="[01]{42}", doc="Vendor ID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVendorid(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lhour", expression="[01]{5}", doc="ETA hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLhour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="bearing", expression="[01]{10}", doc="Bearing", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBearing(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="1-3", expression="000001|000010|000011", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mgustdir", expression="[01]{7}", doc="Maximum Gust Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMgustdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="position", expression="[01]{3}", doc="Mooring Position", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPosition(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_hour", expression="[01]{5}", doc="To UTC Hour", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisToHour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="depth1", expression="[01]{9}", doc="Measurement Depth #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDepth1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="berth_lat", expression="[01]{24}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBerthLat_I3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="cspeed_7", expression="[01]{7}", doc="Current Speed Predicted", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCspeed_U1_7(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed_8", expression="[01]{8}", doc="Current Speed Predicted", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCspeed_U1_8(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="duration_18", expression="[01]{18}", doc="Duration", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDuration_18(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fairgap", expression="[01]{13}", doc="Forecast Air Gap", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFairgap_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset2", expression="[01]{12}", doc="Offset B", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOffset2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset1", expression="[01]{12}", doc="Offset A", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOffset1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="from_hour", expression="[01]{5}", doc="From UTC Hour", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFromHour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swellperiod", expression="[01]{6}", doc="Swell period", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwellperiod(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="bearing1", expression="[01]{9}", doc="Current Bearing #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBearing1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dest1", expression="[01]{30}", doc="MMSI1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDest1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="from_min", expression="[01]{6}", doc="From UTC Minute", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFromMin(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dest2", expression="[01]{30}", doc="MMSI2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDest2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tugs", expression="[01]{2}", doc="Tugs", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTugs(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="precipitation", expression="[01]{3}", doc="Precipitation", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPrecipitation(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="steam", expression="[01]{2}", doc="Steam", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSteam(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="midcloudt", expression="[01]{6}", doc="Cloud type (middle)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMidcloudt(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lshiptype", expression="[01]{42}", doc="Lloyd's Ship Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLshiptype(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid29", expression="011101", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="bnwas_state", expression="[01]{2}", doc="BNWAS", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBnwasState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid24", expression="011000", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="type1_1", expression="[01]{6}", doc="First message type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid23", expression="010111", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid22", expression="010110", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="preciptype_2", expression="[01]{2}", doc="Precipitation Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPreciptype_2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="preciptype_3", expression="[01]{3}", doc="Precipitation", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPreciptype_3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid21", expression="010101", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="type1_2", expression="[01]{6}", doc="Second message type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid28", expression="011100", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid27", expression="011011", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid26", expression="011010", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid30", expression="011110", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="vhfradio_state", expression="[01]{2}", doc="Radio VHF", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVhfradioState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid31", expression="011111", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="power", expression="[01]{1}", doc="Power", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPower(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lat_27", expression="[01]{27}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLat_I4_27(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="lat_24", expression="[01]{24}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLat_I3_24(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="gnss_state", expression="[01]{2}", doc="GNSS", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisGnssState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="off_position", expression="[01]{1}", doc="Off-Position Indicator", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOffPosition(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wavedir", expression="[01]{9}", doc="Wave direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWavedir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid32", expression="100000", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icerate", expression="[01]{3}", doc="Rate of Ice Accretion", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisIcerate(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tcs_state", expression="[01]{2}", doc="Track control system", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTcsState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="name_120", expression="[01]{120}", doc="Name", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisName_120(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cartage", expression="[01]{2}", doc="Cartage", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCartage(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lift", expression="[01]{2}", doc="Lift(s)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLift(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lat_17", expression="[01]{17}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLat_I1_17(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="lat_15", expression="[01]{15}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLat_I3_15(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="sensortype", expression="[01]{3}", doc="Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSensortype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lmonth", expression="[01]{4}", doc="ETA month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLmonth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressuretend_4", expression="[01]{4}", doc="Pressure Tendency", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPressuretend_4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="north", expression="[01]{8}", doc="N dimension", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNorth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fwgust", expression="[01]{7}", doc="Forecast Wind Gust", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFwgust(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="salinity", expression="[01]{9}", doc="Salinity", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSalinity_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="closefrom", expression="[01]{120}", doc="Location Of Closing From", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisClosefrom(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="stevedore", expression="[01]{2}", doc="Stevedore", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisStevedore(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="availability", expression="[01]{1}", doc="Services Availability", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAvailability(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wavetype", expression="[01]{3}", doc="Wave Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWavetype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mgustspeed", expression="[01]{8}", doc="Maximum Gust Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMgustspeed_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressuretype", expression="[01]{3}", doc="Pressure Sensor Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPressuretype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="surftemp", expression="[01]{9}", doc="Sea Surface Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSurftemp_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dest_mmsi", expression="[01]{30}", doc="Destination MMSI", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDestMmsi(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="totaloil", expression="[01]{14}", doc="Total Bunker Oil", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTotaloil(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressuretend_2", expression="[01]{2}", doc="Pressure Tendency", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPressuretend_2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="maneuver", expression="[01]{2}", doc="Maneuver Indicator", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisManeuver(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swelldir", expression="[01]{9}", doc="Swell direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwelldir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
})
@Rules({
@Rule(left="12Messages", value={"(12Content '0*\n')+"})
,@Rule(left="24Content", value={"Type24StaticDataReportB"})
,@Rule(left="IMO289RouteInformationAddressed", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid28", "linkage", "sender", "rtype", "month", "day_5", "hour", "minute_6", "duration_18", "waycount", "(lon_28 lat_27)+"})
,@Rule(left="24Content", value={"Type24StaticDataReportA"})
,@Rule(left="5Messages", value={"(5Content '0*\n')+"})
,@Rule(left="10Messages", value={"(10Content '0*\n')+"})
,@Rule(left="Polygon", value={"shape4", "scale", "(bearing distance)+"})
,@Rule(left="IMO289MarineTrafficSignal", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid19", "linkage", "station", "lon_25", "lat_24", "status_2", "signal", "hour", "minute_6", "nextsignal", "'[01]{102}'"})
,@Rule(left="Type15Interrogation1", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "type1_1", "offset1_1"})
,@Rule(left="IMO289TextDescriptionBroadcast", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid29", "linkage", "description_6_966"})
,@Rule(left="6Content", value={"IMO236DangerousCargoIndication"})
,@Rule(left="17Messages", value={"(17Content '0*\n')+"})
,@Rule(left="shape", value={"Polygon"})
,@Rule(left="SalinityReportPayload", value={"watertemp", "conductivity", "pressure_16", "salinity", "salinitytype", "sensortype", "'[01]{35}'"})
,@Rule(left="6Content", value={"TidalWindowIMO289"})
,@Rule(left="8Content", value={"IMO289ExtendedShipStaticAndVoyageRelatedData"})
,@Rule(left="8Content", value={"MeteorologicalAndHydrologicalDataIMO236"})
,@Rule(left="TidalWindowIMO289", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid32", "month", "day_5", "(lon_25 lat_24 from_hour from_min to_hour to_min cdir cspeed_8)+"})
,@Rule(left="24Messages", value={"(24Content '0*\n')+"})
,@Rule(left="9Messages", value={"(9Content '0*\n')+"})
,@Rule(left="Polyline", value={"shape3", "scale", "(bearing distance)+"})
,@Rule(left="AssociatedText", value={"shape5", "text_84"})
,@Rule(left="IMO289BerthingDataAddressed", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid20", "linkage", "berth_length", "berth_depth", "position", "month", "day_5", "hour", "minute_6", "availability", "agent", "fuel", "chandler", "stevedore", "electrical", "water", "customs", "cartage", "crane", "lift", "medical", "navrepair", "provisions", "shiprepair", "surveyor", "steam", "tugs", "solidwaste", "liquidwaste", "hazardouswaste", "ballast", "additional", "regional1", "regional2", "future1", "future2", "berth_name", "berth_lon", "berth_lat"})
,@Rule(left="messages", value={"message+"})
,@Rule(left="Type20DataLinkManagementMessage", value={"repeat", "mmsi", "'[01]{2}'", "offset1", "number1", "timeout1", "increment1_11", "offset2", "number2", "timeout2", "increment2_11", "offset3", "number3", "timeout3", "increment3", "offset4", "number4", "timeout4", "increment4"})
,@Rule(left="Type19ExtendedClassBCSPositionReport", value={"repeat", "mmsi", "reserved", "speed_10", "accuracy", "lon_28", "lat_27", "course_12", "heading_9", "second", "regional_4", "shipname", "shiptype", "to_bow", "to_stern", "to_port", "to_starboard", "epfd", "raim", "dte", "assigned", "'[01]{4}'"})
,@Rule(left="IMO236DangerousCargoIndication", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid12", "lastport", "lmonth", "lday", "lhour", "lminute", "nextport", "nmonth", "nday", "nhour", "nminute", "dangerous", "imdcat", "unid", "amount", "unit", "'[01]{3}'"})
,@Rule(left="message", value={"4"})
,@Rule(left="8Content", value={"WeatherObservationReportFromShipWMOVariant"})
,@Rule(left="IMO289RouteInformationBroadcast", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid27", "linkage", "sender", "rtype", "month", "day_5", "hour", "minute_6", "duration_18", "waycount", "(lon_28 lat_27)+"})
,@Rule(left="Type16AssignmentModeCommand", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "offset1", "increment1_10", "mmsi2", "offset2", "increment2_10"})
,@Rule(left="message", value={"9"})
,@Rule(left="message", value={"7"})
,@Rule(left="5Content", value={"Type5StaticAndVoyageRelatedData"})
,@Rule(left="message", value={"8"})
,@Rule(left="message", value={"5"})
,@Rule(left="StationIDPayload", value={"name_84", "'[01]{1}'"})
,@Rule(left="message", value={"6"})
,@Rule(left="16Content", value={"Type16AssignmentModeCommand"})
,@Rule(left="WeatherReportPayload", value={"temperature", "sensortype", "preciptype_2", "visibility_8", "dewpoint", "dewtype", "pressure_9", "pressuretend_2", "pressuretype", "salinity", "'[01]{25}'"})
,@Rule(left="message", value={"1-3"})
,@Rule(left="Type22ChannelManagement", value={"repeat", "mmsi", "'[01]{2}'", "channel_a", "channel_b", "txrx_4", "power", "ne_lon", "ne_lat", "sw_lon", "sw_lat", "dest1", "dest2", "addressed", "band_a", "band_b", "zonesize", "'[01]{23}'"})
,@Rule(left="9Content", value={"Type9StandardSARAircraftPositionReport"})
,@Rule(left="22Content", value={"Type22ChannelManagement"})
,@Rule(left="Type15Interrogation2", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "type1_1", "offset1_1", "'[01]{2}'", "type1_2", "offset1_2", "'[01]{2}'"})
,@Rule(left="27Content", value={"Type27LongRangeAISBroadcastMessage"})
,@Rule(left="1-3Messages", value={"(1-3Content '0*\n')+"})
,@Rule(left="Type27LongRangeAISBroadcastMessage", value={"repeat", "mmsi", "accuracy", "raim", "status_4", "lon_18", "lat_17", "speed_6", "course_9", "gnss", "'[01]{1}'"})
,@Rule(left="23Messages", value={"(23Content '0*\n')+"})
,@Rule(left="8Content", value={"IMO289TextDescriptionBroadcast"})
,@Rule(left="12Content", value={"Type12AddressedSafetyRelatedMessage"})
,@Rule(left="19Messages", value={"(19Content '0*\n')+"})
,@Rule(left="6Content", value={"IMO236TidalWindow"})
,@Rule(left="17Content", value={"Type17DGNSSBroadcastBinaryMessage"})
,@Rule(left="FairwayClosed", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid13", "reason", "closefrom", "closeto", "radius_10", "extunit", "fday", "fmonth", "fhour", "fminute", "tday", "tmonth", "thour", "tminute", "'[01]{4}'"})
,@Rule(left="CurrentFlow2DReportPayload", value={"cspeed1", "cdir1", "cdepth1", "cspeed2", "cdir2", "cdepth2_9", "cspeed3", "cdir3", "cdepth3_9", "sensortype", "'[01]{4}'"})
,@Rule(left="21Content", value={"Type21AidToNavigationReport"})
,@Rule(left="4Messages", value={"(4Content '0*\n')+"})
,@Rule(left="Type4BaseStationReport", value={"repeat", "mmsi", "year", "month", "day_5", "hour", "minute_6", "second", "accuracy", "lon_28", "lat_27", "epfd", "'[01]{10}'", "raim", "radio_19"})
,@Rule(left="6Content", value={"IMO236NumberOfPersonsOnBoard"})
,@Rule(left="AreaNoticeAddressedMessageHeader", value={"repeat", "mmsi", "seqno", "dac001", "fid22", "linkage", "notice", "month", "day_5", "hour", "minute_6", "duration_18", "(shape)+"})
,@Rule(left="Type17DGNSSBroadcastBinaryMessage", value={"repeat", "mmsi", "'[01]{2}'", "lon_18", "lat_17", "'[01]{5}'", "data"})
,@Rule(left="14Content", value={"Type14SafetyRelatedBroadcastMessage"})
,@Rule(left="Sector", value={"shape2", "scale", "lon_25", "lat_24", "precision", "radius_12", "left", "right"})
,@Rule(left="MeteorologicalAndHydrologicalDataIMO289", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid31", "lon_25", "lat_24", "accuracy", "day_5", "hour", "minute_6", "wspeed", "wgust", "wdir", "wgustdir", "airtemp_11", "humidity", "dewpoint", "pressure_9", "pressuretend_2", "visgreater", "visibility_8", "waterlevel_12", "leveltrend", "cspeed_8", "cdir", "cspeed2", "cdir2", "cdepth2_5", "cspeed3", "cdir3", "cdepth3_5", "waveheight", "waveperiod", "wavedir", "swellheight", "swellperiod", "swelldir", "seastate", "watertemp", "precipitation", "salinity", "ice", "'[01]{10}'"})
,@Rule(left="Type5StaticAndVoyageRelatedData", value={"repeat", "mmsi", "ais_version", "imo", "callsign", "shipname", "shiptype", "to_bow", "to_stern", "to_port", "to_starboard", "epfd", "month", "day_5", "hour", "minute_6", "draught", "destination_120", "dte", "'[01]{1}'"})
,@Rule(left="VTSGeneratedSyntheticTargets", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid17", "(idtype id '[01]{4}' lat_24 lon_25 course_9 second speed_10)+"})
,@Rule(left="10Content", value={"Type10UTCDateInquiry"})
,@Rule(left="Type10UTCDateInquiry", value={"repeat", "mmsi", "'[01]{2}'", "dest_mmsi", "'[01]{2}'"})
,@Rule(left="27Messages", value={"(27Content '0*\n')+"})
,@Rule(left="MeteorologicalAndHydrologicalDataIMO236", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid16", "lat_24", "lon_25", "day_5", "hour", "minute_6", "wspeed", "wgust", "wdir", "wgustdir", "temperature", "humidity", "dewpoint", "pressure_9", "pressuretend_2", "visibility_8", "waterlevel_9", "leveltrend", "cspeed_8", "cdir", "cspeed2", "cdir2", "cdepth2_5", "cspeed3", "cdir3", "cdepth3_5", "waveheight", "waveperiod", "wavedir", "swellheight", "swellperiod", "swelldir", "seastate", "watertemp", "preciptype_3", "salinity", "ice", "'[01]{6}'"})
,@Rule(left="IMO236ExtendedShipStaticAndVoyageRelatedData", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid15", "airdraught_11", "'[01]{5}'"})
,@Rule(left="IMO289TextDescriptionAddressed", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid30", "linkage", "description_6_930"})
,@Rule(left="6Content", value={"IMO289RouteInformationAddressed"})
,@Rule(left="15Messages", value={"(15Content '0*\n')+"})
,@Rule(left="SeaStateReportPayload", value={"swheight", "swperiod", "swelldir", "seastate", "swelltype", "watertemp", "distance1", "depthtype", "waveheight", "waveperiod", "wavedir", "wavetype", "salinity"})
,@Rule(left="shape", value={"CircleOrPoint"})
,@Rule(left="6Content", value={"IMO289BerthingDataAddressed"})
,@Rule(left="IMO236NumberOfPersonsOnBoard", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid16", "persons", "'[01]{35}'"})
,@Rule(left="Type12AddressedSafetyRelatedMessage", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "text_936"})
,@Rule(left="Type11UTCDateResponse", value={"repeat", "mmsi", "year", "month", "day_5", "hour", "minute_6", "second", "accuracy", "lon_28", "lat_27", "epfd", "'[01]{10}'", "raim", "radio_19"})
,@Rule(left="Type7BinaryAcknowledge", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "'[01]{2}'", "mmsi2", "'[01]{2}'", "mmsi3", "'[01]{2}'", "mmsi4", "'[01]{2}'"})
,@Rule(left="23Content", value={"Type23GroupAssignmentCommand"})
,@Rule(left="Type15Interrogation3", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "type1_1", "offset1_1", "'[01]{2}'", "type1_2", "offset1_2", "'[01]{2}'", "mmsi2", "type2_1", "offset2_1", "'[01]{2}'"})
,@Rule(left="IMO236NumberOfPersonsOnBoard", value={"repeat", "mmsi", "seqno", "dac001", "fid16", "persons", "'[01]{3}'"})
,@Rule(left="Type23GroupAssignmentCommand", value={"repeat", "mmsi", "'[01]{2}'", "ne_lon", "ne_lat", "sw_lon", "sw_lat", "station_type", "ship_type", "'[01]{22}'", "txrx_2", "interval", "quiet", "'[01]{6}'"})
,@Rule(left="IMO236TidalWindow", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid14", "month", "day_5", "(lat_27 lon_28 from_hour from_min to_hour to_min cdir cspeed_7)+"})
,@Rule(left="6Content", value={"IMO289TextDescriptionAddressed"})
,@Rule(left="8Content", value={"FairwayClosed"})
,@Rule(left="21Messages", value={"(21Content '0*\n')+"})
,@Rule(left="8Content", value={"IMO236ExtendedShipStaticAndVoyageRelatedData"})
,@Rule(left="EnvironmentalMessageHeader", value={"repeat", "mmsi", "seqno", "dac001", "fid26", "(sensor day_5 hour minute_6 site payload)+"})
,@Rule(left="SiteLocationPayload", value={"lon_28", "lat_27", "alt_11", "owner", "timeout", "'[01]{12}'"})
,@Rule(left="message", value={"27"})
,@Rule(left="message", value={"23"})
,@Rule(left="20Content", value={"Type20DataLinkManagementMessage"})
,@Rule(left="message", value={"24"})
,@Rule(left="message", value={"21"})
,@Rule(left="message", value={"22"})
,@Rule(left="1-3Content", value={"CommonNavigationBlock"})
,@Rule(left="message", value={"20"})
,@Rule(left="Type9StandardSARAircraftPositionReport", value={"repeat", "mmsi", "alt_12", "speed_10", "accuracy", "lon_28", "lat_27", "course_12", "second", "regional_8", "dte", "'[01]{3}'", "assigned", "raim", "radio_19"})
,@Rule(left="CommonNavigationBlock", value={"repeat", "mmsi", "status_4", "turn", "speed_10", "accuracy", "lon_28", "lat_27", "course_12", "heading_9", "second", "maneuver", "'[01]{3}'", "raim", "radio_19"})
,@Rule(left="7Content", value={"Type7BinaryAcknowledge"})
,@Rule(left="message", value={"16"})
,@Rule(left="19Content", value={"Type19ExtendedClassBCSPositionReport"})
,@Rule(left="message", value={"17"})
,@Rule(left="message", value={"18"})
,@Rule(left="message", value={"19"})
,@Rule(left="IMO289ExtendedShipStaticAndVoyageRelatedData", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid24", "linkage", "airdraught_13", "lastport", "nextport", "secondport", "ais_state", "ata_state", "bnwas_state", "ecdisb_state", "chart_state", "sounder_state", "epaid_state", "steer_state", "gnss_state", "gyro_state", "lrit_state", "magcomp_state", "navtex_state", "arpa_state", "sband_state", "xband_state", "hfradio_state", "inmarsat_state", "mfradio_state", "vhfradio_state", "grndlog_state", "waterlog_state", "thd_state", "tcs_state", "vdr_state", "'[01]{2}'", "iceclass", "horsepower", "vhfchan", "lshiptype", "tonnage", "lading", "heavyoil", "lightoil", "dieseloil", "totaloil", "persons", "'[01]{10}'"})
,@Rule(left="message", value={"12"})
,@Rule(left="message", value={"14"})
,@Rule(left="message", value={"15"})
,@Rule(left="CurrentFlow3DPayload", value={"cnorth1", "ceast1", "cup1", "cdepth1", "cnorth2", "ceast2", "cup2", "cdepth2_9", "sensortype", "'[01]{16}'"})
,@Rule(left="message", value={"10"})
,@Rule(left="message", value={"11"})
,@Rule(left="Type24StaticDataReportB", value={"repeat", "mmsi", "partno1", "shiptype", "vendorid", "callsign", "to_bow", "to_stern", "to_port", "to_starboard", "mothership_mmsi", "'[01]{6}'"})
,@Rule(left="WindReportPayload", value={"wspeed", "wgust", "wdir", "wgustdir", "sensortype", "fwspeed", "fwgust", "fwdir", "day_5", "hour", "minute_6", "duration_8", "'[01]{3}'"})
,@Rule(left="8Content", value={"IMO289MarineTrafficSignal"})
,@Rule(left="shape", value={"Rectangle"})
,@Rule(left="8Content", value={"MeteorologicalAndHydrologicalDataIMO289"})
,@Rule(left="shape", value={"AssociatedText"})
,@Rule(left="6Messages", value={"(6Content '0*\n')+"})
,@Rule(left="Type18StandardClassBCSPositionReport", value={"repeat", "mmsi", "reserved", "speed_10", "accuracy", "lon_28", "lat_27", "course_12", "heading_9", "second", "regional_2", "cs", "display", "dsc", "band", "msg22", "assigned", "raim", "radio_20"})
,@Rule(left="6Content", value={"EnvironmentalMessageHeader"})
,@Rule(left="14Messages", value={"(14Content '0*\n')+"})
,@Rule(left="Type21AidToNavigationReport", value={"repeat", "mmsi", "aid_type", "name_120", "accuracy", "lon_28", "lat_27", "to_bow", "to_stern", "to_port", "to_starboard", "epfd", "second", "off_position", "regional_8", "raim", "virtual_aid", "assigned", "'[01]{1}'", "'[01]{88}'"})
,@Rule(left="8Content", value={"IMO289RouteInformationBroadcast"})
,@Rule(left="7Messages", value={"(7Content '0*\n')+"})
,@Rule(left="WeatherObservationReportFromShipNonWMOVariant", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid21", "wmo0", "location", "lon_25", "lat_24", "day_5", "hour", "minute_6", "weather_4", "vislimit", "visibility_7", "humidity", "wspeed", "wdir", "pressure_9", "pressuretend_4", "airtemp_11", "watertemp", "waveperiod", "waveheight", "wavedir", "swellheight", "swelldir", "swellperiod", "'[01]{3}'"})
,@Rule(left="8Messages", value={"(8Content '0*\n')+"})
,@Rule(left="8Content", value={"WeatherObservationReportFromShipNonWMOVariant"})
,@Rule(left="WeatherObservationReportFromShipWMOVariant", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid21", "wmo1", "lon_16", "lat_15", "month", "day_6", "hour", "minute_3", "course_7", "speed_5", "heading_7", "pressure_11", "pdelta", "ptend", "twinddir", "twindspeed", "rwinddir", "rwindspeed", "mgustspeed", "mgustdir", "airtemp_10", "humidity", "surftemp", "visibility_6", "weather_9", "pweather1", "pweather2", "totalcloud", "lowclouda", "lowcloudt", "midcloudt", "highcloudt", "cloudbase", "wwperiod", "wwheight", "swelldir1", "swperiod1", "swheight1", "swelldir2", "swperiod2", "swheight2", "icedeposit", "icerate", "icecause", "seaice", "icetype", "icestate", "icedevel", "icebearing"})
,@Rule(left="6Content", value={"AreaNoticeAddressedMessageHeader"})
,@Rule(left="18Content", value={"Type18StandardClassBCSPositionReport"})
,@Rule(left="4Content", value={"Type4BaseStationReport"})
,@Rule(left="Type14SafetyRelatedBroadcastMessage", value={"repeat", "mmsi", "'[01]{2}'", "text_968"})
,@Rule(left="6Content", value={"IMO289ClearanceTimeToEnterPort"})
,@Rule(left="22Messages", value={"(22Content '0*\n')+"})
,@Rule(left="CircleOrPoint", value={"shape0", "scale", "lon_25", "lat_24", "precision", "radius_12", "'[01]{18}'"})
,@Rule(left="11Messages", value={"(11Content '0*\n')+"})
,@Rule(left="11Content", value={"Type11UTCDateResponse"})
,@Rule(left="16Messages", value={"(16Content '0*\n')+"})
,@Rule(left="AirGapAirDraftReportPayload", value={"airdraught_13", "airgap", "gaptrend", "fairgap", "day_5", "hour", "minute_6", "'[01]{28}'"})
,@Rule(left="15Content", value={"Type15Interrogation2"})
,@Rule(left="15Content", value={"Type15Interrogation3"})
,@Rule(left="15Content", value={"Type15Interrogation1"})
,@Rule(left="shape", value={"Polyline"})
,@Rule(left="20Messages", value={"(20Content '0*\n')+"})
,@Rule(left="AreaNoticeAddressedMessageHeader", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid23", "linkage", "notice", "month", "day_5", "hour", "minute_6", "duration_18", "(shape)+"})
,@Rule(left="shape", value={"Sector"})
,@Rule(left="HorizontalCurrentReportPayload", value={"bearing1", "distance1", "speed1", "direction1", "depth1", "bearing1", "distance1", "speed1", "direction1", "depth1", "'[01]{1}'"})
,@Rule(left="Rectangle", value={"shape1", "scale", "lon_25", "lat_24", "precision", "east", "north", "orientation", "'[01]{5}'"})
,@Rule(left="8Content", value={"VTSGeneratedSyntheticTargets"})
,@Rule(left="Type24StaticDataReportA", value={"repeat", "mmsi", "partno0", "shipname", "'[01]{8}'"})
,@Rule(left="IMO289ClearanceTimeToEnterPort", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid18", "linkage", "month", "day_5", "hour", "minute_6", "portname", "destination_30", "lon_25", "lat_24", "'[01]{43}'"})
,@Rule(left="18Messages", value={"(18Content '0*\n')+"})
})
public abstract class AISParser implements ParserInfo
{
protected void aisStatus_4(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisSpeed_U1_10(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisCourse_U1_12(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisHeading_9(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisDay_5(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisMinute_6(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisDestination_120(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisCspeed_U1_7(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisDestination_30(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisDuration_18(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisRadius_12(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisText_84(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisDescription_6_930(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisCspeed_U1_8(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisPressuretend_2(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisVisibility_U1_8(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisWaterlevel_U1_9(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisCdepth2_U1_5(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisCdepth3_U1_5(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisPreciptype_3(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisRadius_10(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisAirdraught_11(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisCourse_9(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisSpeed_10(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisStatus_2(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisWeather_4(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisVisibility_U1_7(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisPressuretend_4(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisAirtemp_U1_11(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisLon_I3_16(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisLat_I3_15(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisDay_6(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisMinute_3(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisCourse_7(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisSpeed_U1_5(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisHeading_7(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisPressure_U1_11(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisAirtemp_U1_10(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisVisibility_U2_6(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisWeather_9(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisCdepth2_9(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisCdepth3_9(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisPreciptype_2(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisAirdraught_U1_13(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisDescription_6_966(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisAirdraught_13(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisWaterlevel_U2_12(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisCdepth3_5(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisText_936(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisText_968(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisLon_I1_18(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisLat_I1_17(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisDuration_8(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisIncrement1_10(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisIncrement2_10(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisIncrement1_11(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisIncrement2_11(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisTxrx_4(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisTxrx_2(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisLon_I4_18(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisLat_I4_17(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisSpeed_6(int arg, @ParserContext("aisData") AISObserver aisData){}

    /**
     * Parses AIS messages decompressed to bits. (represented with characters
     * '0' & '1')
     *
     * <p> Example input:
     * 00010100001111011001011001001011111000000000000110001...
     *
     * @param is
     * @param aisData
     */
    public void parseBits(InputStream is, AISObserver aisData)
    {
        parse(is, aisData);
    }

    public static AISParser newInstance() throws IOException
    {
        return (AISParser) GenClassFactory.getGenInstance(AISParser.class);
    }

    @ParseMethod(start = "messages", size = 1024, wideIndex = true)
    protected abstract void parse(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);

    @ParseMethod(start = "1-3Messages", size = 1024, wideIndex = true)
    protected abstract void parse123Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "10Messages", size = 1024, wideIndex = true)
    protected abstract void parse10Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "11Messages", size = 1024, wideIndex = true)
    protected abstract void parse11Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "12Messages", size = 1024, wideIndex = true)
    protected abstract void parse12Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "14Messages", size = 1024, wideIndex = true)
    protected abstract void parse14Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "15Messages", size = 1024, wideIndex = true)
    protected abstract void parse15Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "16Messages", size = 1024, wideIndex = true)
    protected abstract void parse16Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "17Messages", size = 1024, wideIndex = true)
    protected abstract void parse17Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "18Messages", size = 1024, wideIndex = true)
    protected abstract void parse18Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "19Messages", size = 1024, wideIndex = true)
    protected abstract void parse19Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "20Messages", size = 1024, wideIndex = true)
    protected abstract void parse20Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "21Messages", size = 1024, wideIndex = true)
    protected abstract void parse21Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "22Messages", size = 1024, wideIndex = true)
    protected abstract void parse22Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "23Messages", size = 1024, wideIndex = true)
    protected abstract void parse23Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "24Messages", size = 1024, wideIndex = true)
    protected abstract void parse24Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "27Messages", size = 1024, wideIndex = true)
    protected abstract void parse27Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "4Messages", size = 1024, wideIndex = true)
    protected abstract void parse4Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "5Messages", size = 1024, wideIndex = true)
    protected abstract void parse5Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "6Messages", size = 1024, wideIndex = true)
    protected abstract void parse6Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "7Messages", size = 1024, wideIndex = true)
    protected abstract void parse7Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "8Messages", size = 1024, wideIndex = true)
    protected abstract void parse8Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);
    @ParseMethod(start = "9Messages", size = 1024, wideIndex = true)
    protected abstract void parse9Messages(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);

    @RecoverMethod
    public void recover(
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader,
            @ParserContext(ParserConstants.ExpectedDescription) String expected,
            @ParserContext(ParserConstants.LastToken) String got,
            @ParserContext(ParserConstants.THROWABLE) Throwable thr
            ) throws IOException
    {
        String input = reader.getInput();
        if (input.endsWith("\n"))
        {
            aisData.rollback("skipping " + input.substring(0, input.length()-1)+"^ "+thr);
            reader.clear();
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append(input);
            sb.append('^');
            int cc = reader.read();
            while (cc != '\n' && cc != -1)
            {
                sb.append((char) cc);
                cc = reader.read();
                reader.clear();
            }
            aisData.rollback("skipping " + sb+" "+thr);
        }
        System.err.println(expected);
    }

    protected void aisType(
            int messageType, 
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext
    )
    {
        aisData.setMessageType(MessageTypes.values()[messageType]);
        AISContext.AISThread thread0 = aisContext.getThread(0);
        thread0.getSwitchingInputStream().setSleeping(true);
        AISContext.AISThread thread = aisContext.getThread(messageType);
        thread.getSwitchingInputStream().getSemaphore().release();
    }

    protected void aisRepeat(int repeatIndicator, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRepeatIndicator(repeatIndicator);
    }

    protected void aisMmsi(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMMSI(mmsi);
    }

    protected void aisStatus(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setStatus(NavigationStatus.values()[status]);
    }

    /**
     * <p> Turn rate is encoded as follows: <p> 0 = not turning <p> 1…126 =
     * turning right at up to 708 degrees per minute or higher <p> 1…-126 =
     * turning left at up to 708 degrees per minute or higher <p> 127 = turning
     * right at more than 5deg/30s (No TI available) <p> -127 = turning left at
     * more than 5deg/30s (No TI available) <p> 128 (80 hex) indicates no turn
     * information available (default) <p>Values between 0 and 708 degrees/min
     * coded by ROTAIS=4.733 * SQRT(ROTsensor) degrees/min where ROTsensor is
     * the Rate of Turn as input by an external Rate of Turn Indicator. ROTAIS
     * is rounded to the nearest integer value. Thus, to decode the field value,
     * divide by 4.733 and then square it. Sign of the field value should be
     * preserved when squaring it, otherwise the left/right indication will be
     * lost.
     *
     * @param turn
     * @param aisData
     */
    protected void aisTurn_I3(int turn, @ParserContext("aisData") AISObserver aisData)
    {
        switch (turn)
        {
            case 0:
                aisData.setTurn(0);
                break;
            case 127:
                aisData.setTurn(10);
                break;
            case -127:
                aisData.setTurn(-10);
                break;
            case -128:
                break;
            default:
                float f = turn;
                f = f / 4.733F;
                aisData.setTurn(Math.signum(f) * f * f);
                break;
        }
    }

    protected void aisSpeed_U1(int speed, @ParserContext("aisData") AISObserver aisData)
    {
        if (speed != 1023)
        {
            float f = speed;
            aisData.setSpeed(f / 10F);
        }
    }

    protected void aisAccuracy(int accuracy, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAccuracy(accuracy == 1);
    }

    protected void aisLon_I3_25(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisLon_I3(arg, aisData);
    }
    protected void aisLon_I4_28(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisLon_I4(arg, aisData);
    }
    protected void aisLon_I1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisLon_I3(int lon, @ParserContext("aisData") AISObserver aisData)
    {
        if (lon != 0x6791AC0)
        {
            if (lon <= 180 * 60 * 1000 && lon >= -180 * 60 * 1000)
            {
                float f = lon;
                aisData.setLongitude(f / 60000F);
            }
            else
            {
                System.err.println("longitude I3 = " + lon);
            }
        }
    }

    protected void aisLon_I4(int lon, @ParserContext("aisData") AISObserver aisData)
    {
        if (lon != 0x6791AC0)
        {
            if (lon <= 180 * 60 * 10000 && lon >= -180 * 60 * 10000)
            {
                float f = lon;
                aisData.setLongitude(f / 600000F);
            }
            else
            {
                System.err.println("longitude I4 = " + lon);
            }
        }
    }

    protected void aisLat_I3_24(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisLat_I3(arg, aisData);
    }
    protected void aisLat_I4_27(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisLat_I4(arg, aisData);
    }
    protected void aisLat_I1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisLat_I3(int lat, @ParserContext("aisData") AISObserver aisData)
    {
        if (lat != 0x3412140)
        {
            if (lat <= 90 * 60 * 1000 && lat >= -90 * 60 * 1000)
            {
                float f = lat;
                aisData.setLatitude(f / 60000L);
            }
            else
            {
                System.err.println("latitude I3 = " + lat);
            }
        }
    }

    protected void aisLat_I4(int lat, @ParserContext("aisData") AISObserver aisData)
    {
        if (lat != 0x3412140)
        {
            if (lat <= 90 * 60 * 10000 && lat >= -90 * 60 * 10000)
            {
                float f = lat;
                aisData.setLatitude(f / 600000F);
            }
            else
            {
                System.err.println("latitude I4 = " + lat);
            }
        }
    }

    protected void aisCourse_U1(int course, @ParserContext("aisData") AISObserver aisData)
    {
        if (course != 3600)
        {
            if (course >= 0 && course < 3600)
            {
                float f = course;
                aisData.setCourse(f / 10F);
            }
            else
            {
                System.err.println("course U1 = " + course);
            }
        }
    }

    protected void aisHeading(int heading, @ParserContext("aisData") AISObserver aisData)
    {
        if (heading != 511)
        {
            aisData.setHeading(heading);
        }
    }

    protected void aisSecond(int second, @ParserContext("aisData") AISObserver aisData)
    {
        if (second < 60)
        {
            aisData.setSecond(second);
        }
    }

    protected void aisManeuver(int maneuver, @ParserContext("aisData") AISObserver aisData)
    {
        if (maneuver < 3)
        {
            aisData.setManeuver(ManeuverIndicator.values()[maneuver]);
        }
    }
    /**
     * The RAIM flag indicates whether Receiver Autonomous Integrity Monitoring 
     * is being used to check the performance of the EPFD. 
     * 0 = RAIM not in use(default), 1 = RAIM in use. 
     * See [RAIM] for a detailed description of this flag.
     * @param raim
     * @param aisData 
     */
    protected void aisRaim(int raim, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRAIM(raim == 1);
    }
    /**
     * Bits 149-167 are diagnostic information for the radio system. 
     * Consult [IALA] for detailed description of the latter.
     * @param arg
     * @param aisData 
     */
    protected void aisRadio_19(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisRadio(arg, aisData);
    }
    /**
     * The radio status is 20 bits rather than 19 because an extra first bit 
     * selects whether it should be interpreted as a SOTDMA or ITDMA state.
     * @param arg
     * @param aisData 
     */
    protected void aisRadio_20(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisRadio(arg>>1, aisData);
    }
    protected void aisRadio(int radio, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRadioStatus(radio);
    }

    protected void aisYear(int year, @ParserContext("aisData") AISObserver aisData)
    {
        if (year != 0)
        {
            aisData.setYear(year);
        }
    }

    protected void aisMonth(int month, @ParserContext("aisData") AISObserver aisData)
    {
        if (month != 0)
        {
            aisData.setMonth(month);
        }
    }

    protected void aisDay(int day, @ParserContext("aisData") AISObserver aisData)
    {
        if (day != 0)
        {
            aisData.setDay(day);
        }
    }

    protected void aisHour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setHour(hour);
        }
    }

    protected void aisMinute(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setMinute(minute);
        }
    }

    protected void aisEpfd(int epfd, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setEPFD(EPFDFixTypes.values()[epfd]);
    }

    protected void aisAisVersion(int version, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVersion(version);
    }

    protected void aisImo(int imo, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIMONumber(imo);
    }

    protected void aisCallsign(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setCallSign(fromSixBitCharacters(reader));
    }

    protected void aisShipname(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVesselName(fromSixBitCharacters(reader));
    }

    protected void aisShiptype(int shiptype, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setShipType(CodesForShipType.values()[shiptype]);
    }

    protected void aisToBow(int dimension, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDimensionToBow(dimension);
    }

    protected void aisToStern(int dimension, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDimensionToStern(dimension);
    }

    protected void aisToPort(int dimension, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDimensionToPort(dimension);
    }

    protected void aisToStarboard(int dimension, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDimensionToStarboard(dimension);
    }

    protected void aisDraught_U1(int draught, @ParserContext("aisData") AISObserver aisData)
    {
        float f = draught;
        aisData.setDraught(f / 10F);
    }

    protected void aisDestination(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDestination(fromSixBitCharacters(reader));
    }

    protected void aisDte(int dte, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDTE(dte != 1);
    }

    protected void aisSeqno(int seq, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSequenceNumber(seq);
    }

    protected void aisDestMmsi(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDestinationMMSI(mmsi);
    }

    protected void aisRetransmit(int retransmit, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRetransmit(retransmit == 1);
    }

    protected void aisDac(int dac, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDAC(dac);
    }

    protected void aisFid(int fid, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setFID(fid);
    }

    protected void aisLastport(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLastPort(fromSixBitCharacters(reader));
    }

    protected void aisLmonth(int month, @ParserContext("aisData") AISObserver aisData)
    {
        if (month != 0)
        {
            aisData.setLastPortMonth(month);
        }
    }

    protected void aisLday(int day, @ParserContext("aisData") AISObserver aisData)
    {
        if (day != 0)
        {
            aisData.setLastPortDay(day);
        }
    }

    protected void aisLhour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setLastPortHour(hour);
        }
    }

    protected void aisLminute(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setLastPortMinute(minute);
        }
    }

    protected void aisNextport(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNextPort(fromSixBitCharacters(reader));
    }

    protected void aisNmonth(int month, @ParserContext("aisData") AISObserver aisData)
    {
        if (month != 0)
        {
            aisData.setNextPortMonth(month);
        }
    }

    protected void aisNday(int day, @ParserContext("aisData") AISObserver aisData)
    {
        if (day != 0)
        {
            aisData.setNextPortDay(day);
        }
    }

    protected void aisNhour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setNextPortHour(hour);
        }
    }

    protected void aisNminute(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setNextPortMinute(minute);
        }
    }

    protected void aisDangerous(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMainDangerousGood(fromSixBitCharacters(reader));
    }

    protected void aisImdcat(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIMDCategory(fromSixBitCharacters(reader));
    }

    protected void aisUnid(int unid, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setUNNumber(unid);
    }

    protected void aisAmount(int amount, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAmountOfCargo(amount);
    }

    protected void aisUnit(int unit, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setUnitOfQuantity(CargoUnitCodes.values()[unit]);
    }

    protected void aisFromHour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setFromHour(hour);
        }
    }

    protected void aisFromMin(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setFromMinute(minute);
        }
    }

    protected void aisToHour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setToHour(hour);
        }
    }

    protected void aisToMin(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setToMinute(minute);
        }
    }

    protected void aisCdir(int currentDirection, @ParserContext("aisData") AISObserver aisData)
    {
        if (currentDirection != 360)
        {
            aisData.setCurrentDirection(currentDirection);
        }
    }

    protected void aisCspeed_U1(int currentSpeed, @ParserContext("aisData") AISObserver aisData)
    {
        if (currentSpeed != 127)
        {
            float f = currentSpeed;
            aisData.setCurrentSpeed(f / 10F);
        }
    }

    protected void aisPersons(int persons, @ParserContext("aisData") AISObserver aisData)
    {
        if (persons != 0)
        {
            aisData.setPersonsOnBoard(persons);
        }
    }

    protected void aisLinkage(int id, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLinkage(id);
    }

    protected void aisPortname(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setPortname(fromSixBitCharacters(reader));
    }

    protected void aisNotice(int notice, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAreaNotice(AreaNoticeDescription.values()[notice]);
    }

    protected void aisDuration(int duration, @ParserContext("aisData") AISObserver aisData)
    {
        if (duration != 262143)
        {
            aisData.setDuration(duration);
        }
    }

    protected void aisShape(int shape, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setShape(SubareaType.values()[shape]);
    }

    protected void aisScale(int scale, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setScale(scale);
    }

    protected void aisPrecision(int precision, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setPrecision(precision);
    }

    protected void aisRadius(int radius, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRadius(radius);
    }

    protected void aisEast(int east, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setEast(east);
    }

    protected void aisNorth(int north, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNorth(north);
    }

    protected void aisOrientation(int orientation, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setOrientation(orientation);
    }

    protected void aisLeft(int left, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLeft(left);
    }

    protected void aisRight(int right, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRight(right);
    }

    protected void aisBearing(int bearing, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setBearing(bearing);
    }

    protected void aisDistance(int distance, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDistance(distance);
    }

    protected void aisText(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setText(fromSixBitCharacters(reader));
    }

    protected void aisBerthLength(int meters, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setBerthLength(meters);
    }

    protected void aisBerthDepth_U1(int meters, @ParserContext("aisData") AISObserver aisData)
    {
        float f = meters;
        aisData.setBerthDepth(f / 10F);
    }

    protected void aisPosition(int position, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMooringPosition(MooringPosition.values()[position]);
    }

    protected void aisAvailability(int available, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setServicesAvailability(available == 1);
    }

    protected void aisAgent(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAgentServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisFuel(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setFuelServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisChandler(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setChandlerServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisStevedore(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setStevedoreServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisElectrical(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setElectricalServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisWater(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setWaterServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisCustoms(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setCustomsServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisCartage(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setCartageServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisCrane(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setCraneServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisLift(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLiftServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisMedical(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMedicalServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisNavrepair(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNavrepairServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisProvisions(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setProvisionsServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisShiprepair(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setShiprepairServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisSurveyor(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSurveyorServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisSteam(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSteamServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisTugs(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setTugsServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisSolidwaste(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSolidwasteServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisLiquidwaste(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLiquidwasteServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisHazardouswaste(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setHazardouswasteServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisBallast(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setBallastServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisAdditional(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAdditionalServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisRegional1(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRegional1ServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisRegional2(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRegional2ServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisFuture1(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setFuture1ServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisFuture2(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setFuture2ServiceStatus(ServiceStatus.values()[status]);
    }

    protected void aisBerthName(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setBerthName(fromSixBitCharacters(reader));
    }

    protected void aisBerthLon_I3(int lon, @ParserContext("aisData") AISObserver aisData)
    {
        if (lon != 0x6791AC0)
        {
            float f = lon;
            aisData.setLongitude(f / 60000L);
        }
    }

    protected void aisBerthLat_I3(int lat, @ParserContext("aisData") AISObserver aisData)
    {
        if (lat != 0x3412140)
        {
            float f = lat;
            aisData.setLatitude(f / 60000L);
        }
    }

    protected void aisSender(int sender, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSender(sender);
    }

    protected void aisRtype(int type, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRouteType(RouteTypeCodes.values()[type]);
    }

    protected void aisWaycount(int count, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setWaypointCount(count);
    }

    protected void aisDescription(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDescription(fromSixBitCharacters(reader));
    }

    protected void aisMmsi1(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMMSI1(mmsi);
    }

    protected void aisMmsi2(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMMSI2(mmsi);
    }

    protected void aisMmsi3(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMMSI3(mmsi);
    }

    protected void aisMmsi4(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMMSI4(mmsi);
    }

    protected void aisWspeed(int knots, @ParserContext("aisData") AISObserver aisData)
    {
        if (knots != 127)
        {
            aisData.setAverageWindSpeed(knots);
        }
    }

    protected void aisWgust(int knots, @ParserContext("aisData") AISObserver aisData)
    {
        if (knots != 127)
        {
            aisData.setGustSpeed(knots);
        }
    }

    protected void aisWdir(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees <= 360)
        {
            aisData.setWindDirection(degrees);
        }
    }

    protected void aisWgustdir(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees <= 360)
        {
            aisData.setWindGustDirection(degrees);
        }
    }

    protected void aisTemperature(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        float f = degrees;
        aisData.setAirTemperature((f / 10F) - 60F);
    }

    protected void aisHumidity(int humidity, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRelativeHumidity(humidity);
    }

    protected void aisDewpoint(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        float f = degrees;
        aisData.setDewPoint((f / 10F) - 20F);
    }

    protected void aisPressure_9(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisPressure(arg, aisData);
    }
    protected void aisPressure_U1_16(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisPressure_U1(arg, aisData);
    }
    protected void aisPressure(int pressure, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAirPressure(pressure + 400);
    }

    protected void aisPressure_U1(int pressure, @ParserContext("aisData") AISObserver aisData)
    {
        float f = pressure;
        aisData.setAirPressure((f / 10F) - 900F);   // ???? 90-1100 hPa: P = (value/10)+900 for 0-2000
    }

    protected void aisPressuretend(int tendency, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAirPressureTendency(tendency);
    }

    protected void aisVisibility_U1(int visibility, @ParserContext("aisData") AISObserver aisData)
    {
        float f = visibility;
        aisData.setVisibility(f / 10F);
    }

    protected void aisWaterlevel_U1(int level, @ParserContext("aisData") AISObserver aisData)
    {
        float f = level;
        aisData.setWaterLevel((f / 10F) - 10F);
    }

    protected void aisLeveltrend(int trend, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setWaterLevelTrend(trend);
    }

    protected void aisCspeed2_U1(int speed, @ParserContext("aisData") AISObserver aisData)
    {
        float f = speed;
        aisData.setCurrentSpeed2(f / 10F);
    }

    protected void aisCdir2(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees <= 360)
        {
            aisData.setCurrentDirection2(degrees);
        }
    }

    protected void aisCdepth2_U1(int depth, @ParserContext("aisData") AISObserver aisData)
    {
        float f = depth;
        aisData.setMeasurementDepth2(f / 10F);
    }

    protected void aisCspeed3_U1(int speed, @ParserContext("aisData") AISObserver aisData)
    {
        float f = speed;
        aisData.setCurrentSpeed3(f / 10F);
    }

    protected void aisCdir3(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees <= 360)
        {
            aisData.setCurrentDirection3(degrees);
        }
    }

    protected void aisCdepth3_U1(int depth, @ParserContext("aisData") AISObserver aisData)
    {
        float f = depth;
        aisData.setMeasurementDepth3(f / 10F);
    }

    protected void aisWaveheight_U1(int height, @ParserContext("aisData") AISObserver aisData)
    {
        float f = height;
        aisData.setWaveHeight(f / 10F);
    }

    protected void aisWaveperiod(int seconds, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setWavePeriod(seconds);
    }

    protected void aisWavedir(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setWaveDirection(degrees);
    }

    protected void aisSwellheight_U1(int height, @ParserContext("aisData") AISObserver aisData)
    {
        float f = height;
        aisData.setSwellHeight(f / 10F);
    }

    protected void aisSwellperiod(int seconds, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSwellPeriod(seconds);
    }

    protected void aisSwelldir(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSwellDirection(degrees);
    }

    protected void aisSeastate(int state, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSeaState(BeaufortScale.values()[state]);
    }

    protected void aisWatertemp_U1(int temp, @ParserContext("aisData") AISObserver aisData)
    {
        float f = temp;
        aisData.setWaterTemperature((f / 10F) - 10F);
    }

    protected void aisPreciptype(int type, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setPrecipitation(PrecipitationTypes.values()[type]);
    }

    protected void aisSalinity_U1(int salinity, @ParserContext("aisData") AISObserver aisData)
    {
        float f = salinity;
        aisData.setSalinity(f / 10F);
    }

    protected void aisIce(int ice, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIce(ice);
    }

    protected void aisReason(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setReasonForClosing(fromSixBitCharacters(reader));
    }

    protected void aisClosefrom(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setClosingFrom(fromSixBitCharacters(reader));
    }

    protected void aisCloseto(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setClosingTo(fromSixBitCharacters(reader));
    }

    protected void aisExtunit(int unit, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setUnitOfExtension(ExtensionUnit.values()[unit]);
    }

    protected void aisFmonth(int month, @ParserContext("aisData") AISObserver aisData)
    {
        if (month != 0)
        {
            aisData.setFromMonth(month);
        }
    }

    protected void aisFday(int day, @ParserContext("aisData") AISObserver aisData)
    {
        if (day != 0)
        {
            aisData.setFromDay(day);
        }
    }

    protected void aisFhour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setFromHour(hour);
        }
    }

    protected void aisFminute(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setFromMinute(minute);
        }
    }

    protected void aisTmonth(int month, @ParserContext("aisData") AISObserver aisData)
    {
        if (month != 0)
        {
            aisData.setToMonth(month);
        }
    }

    protected void aisTday(int day, @ParserContext("aisData") AISObserver aisData)
    {
        if (day != 0)
        {
            aisData.setToDay(day);
        }
    }

    protected void aisThour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setToHour(hour);
        }
    }

    protected void aisTminute(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setToMinute(minute);
        }
    }

    protected void aisAirdraught(int meters, @ParserContext("aisData") AISObserver aisData)
    {
        if (meters != 0)
        {
            aisData.setAirDraught(meters);
        }
    }

    protected void aisIdtype(int type, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIdType(TargetIdentifierType.values()[type]);
    }

    protected void aisId(long id, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setId(id);
    }

    protected void aisCourse(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees < 360)
        {
            aisData.setCourse(degrees);
        }
    }

    protected void aisSpeed(int knots, @ParserContext("aisData") AISObserver aisData)
    {
        if (knots < 255)
        {
            aisData.setSpeed(knots);
        }
    }

    protected void aisStation(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setStation(fromSixBitCharacters(reader));
    }

    protected void aisSignal(int signal, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSignal(MarineTrafficSignals.values()[signal]);
    }

    protected void aisNextsignal(int signal, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNextSignal(MarineTrafficSignals.values()[signal]);
    }

    protected void aisWmo(int variant, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVariant(variant);
    }

    protected void aisLocation(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLocation(fromSixBitCharacters(reader));
    }

    protected void aisWeather(int code, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setWeather(WMOCode45501.values()[code]);
    }

    protected void aisVislimit(int reached, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVisibilityLimit(reached != 0);
    }

    protected void aisAirtemp_U1(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        float f = degrees;
        aisData.setAirTemperature((f / 10F) - 60F);
    }

    protected void aisPdelta_U1(int delta, @ParserContext("aisData") AISObserver aisData)
    {
        float f = delta;
        aisData.setAirPressureChange((f / 10F) - 50F);
    }

    protected void aisPtend(int tend, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAirPressureTendency(tend);
    }

    protected void aisTwinddir(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTwindspeed(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisRwinddir(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisRwindspeed_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisMgustspeed_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisMgustdir(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSurftemp_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisVisibility_U2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisPweather1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisPweather2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTotalcloud(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisLowclouda(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisLowcloudt(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisMidcloudt(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisHighcloudt(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCloudbase_U2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisWwperiod(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisWwheight(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwelldir1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwperiod1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwheight1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwelldir2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwperiod2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwheight2_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIcedeposit(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIcerate(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIcecause(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSeaice(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIcetype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIcestate(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIcedevel(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIcebearing(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSensor(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSite(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisPayload(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
    protected void aisAlt_11(int arg, @ParserContext("aisData") AISObserver aisData){}
    protected void aisAlt_12(int arg, @ParserContext("aisData") AISObserver aisData){}
    protected void aisAlt(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisOwner(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTimeout(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
    protected void aisName_84(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisName(arg, aisData);
    }
    protected void aisName_120(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisName(arg, aisData);
    }
    protected void aisName(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setName(arg.getInput());
    }
    protected void aisNameExt(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNameExtension(arg.getInput());
    }
    protected void aisSensortype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisFwspeed(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisFwgust(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisFwdir(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// CurrentFlow2DReportPayload
    protected void aisCspeed1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCdir1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCdepth1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCdepth2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCdepth3(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// CurrentFlow3DPayload
    protected void aisCnorth1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCeast1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCup1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCnorth2_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCeast2_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCup2_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// HorizontalCurrentReportPayload
    protected void aisBearing1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisDistance1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSpeed1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisDirection1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisDepth1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// SeaStateReportPayload
    protected void aisSwheight_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwperiod(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwelltype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisDistance1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisDepthtype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisWavetype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// SalinityReportPayload
    protected void aisConductivity_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSalinitytype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// WeatherReportPayload
    protected void aisDewtype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisPressuretype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// AirGapAirDraftReportPayload
    protected void aisAirdraught_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisAirgap_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisGaptrend(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisFairgap_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// IMO289RouteInformationBroadcast
// IMO289TextDescriptionBroadcast
// IMO289ExtendedShipStaticAndVoyageRelatedData
    protected void aisSecondport(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisAisState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisAtaState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisBnwasState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisEcdisbState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisChartState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSounderState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisEpaidState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSteerState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisGnssState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisGyroState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisLritState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisMagcompState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisNavtexState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisArpaState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSbandState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisXbandState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisHfradioState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisInmarsatState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisMfradioState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisVhfradioState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisGrndlogState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisWaterlogState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisThdState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTcsState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisVdrState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIceclass(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisHorsepower(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisVhfchan(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisLshiptype(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTonnage(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisLading(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisHeavyoil(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisLightoil(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisDieseloil(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTotaloil(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// MeteorologicalAndHydrologicalDataIMO289
    protected void aisDewpoint_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisVisgreater(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisWaterlevel_U2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisPrecipitation(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
    protected void aisRegional_2(int arg, @ParserContext("aisData") AISObserver aisData){}
    protected void aisRegional_4(int arg, @ParserContext("aisData") AISObserver aisData){}
    protected void aisRegional_8(int arg, @ParserContext("aisData") AISObserver aisData){}
    protected void aisRegional(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisAssigned(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type10UTCDateInquiry
// Type12AddressedSafetyRelatedMessage
// Type14SafetyRelatedBroadcastMessage
// Type15Interrogation
    protected void aisType11(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisOffset11(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisType12(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisOffset12(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisType21(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisOffset21(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type16AssignmentModeCommand
    protected void aisOffset1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIncrement1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisOffset2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIncrement2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
    protected void aisData(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type18StandardClassBCSPositionReport
    protected void aisReserved(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisCs(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisDisplay(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisDsc(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisBand(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisMsg22(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type19ExtendedClassBCSPositionReport
    protected void aisShipname(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type20DataLinkManagementMessage
    protected void aisNumber1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTimeout1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisNumber2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTimeout2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisOffset3(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisNumber3(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTimeout3(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIncrement3(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisOffset4(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisNumber4(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTimeout4(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisIncrement4(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type21AidToNavigationReport
    protected void aisAidType(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisOffPosition(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisVirtualAid(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type22ChannelManagement
    protected void aisChannelA(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisChannelB(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTxrx(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisPower(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisNeLon_I1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisNeLat_I1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwLon_I1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwLat_I1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisDest1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisDest2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisAddressed(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisBandA(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisBandB(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisZonesize(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type23GroupAssignmentCommand
    protected void aisNeLon(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisNeLat(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwLon(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisSwLat(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisStationType(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisShipType(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisInterval(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisQuiet(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type24StaticDataReport
    protected void aisPartno(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisVendorid(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisMothershipMmsi(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type25SingleSlotBinaryMessage
    protected void aisStructured(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisGnss(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// ###################################################################################

    private String fromSixBitCharacters(InputReader reader)
    {
        StringBuilder sb = new StringBuilder();
        char[] array = reader.getArray();
        int start = reader.getStart();
        int length = reader.getLength();
        assert length % 6 == 0;
        int bit = 0;
        int cc = 0;
        for (int ii = 0; ii < length; ii++)
        {
            bit++;
            cc <<= 1;
            cc += array[(start + ii) % array.length] - '0';
            if (bit == 6)
            {
                if (cc == 0)    // terminating '@'
                {
                    break;
                }
                if (cc < 32)
                {
                    sb.append((char) (cc + '@'));
                }
                else
                {
                    sb.append((char) cc);
                }
                bit = 0;
                cc = 0;
            }
        }
        return sb.toString().trim();
    }
}
