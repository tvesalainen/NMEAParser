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
import java.util.logging.Level;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserConstants;
import static org.vesalainen.parser.ParserFeature.*;
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
import org.vesalainen.parsers.mmsi.MMSIType;
import static org.vesalainen.parsers.mmsi.MMSIType.*;
import org.vesalainen.regex.SyntaxErrorException;
import org.vesalainen.util.concurrent.SimpleWorkflow.ContextAccess;
import org.vesalainen.util.concurrent.ThreadStoppedException;
import org.vesalainen.util.logging.JavaLogging;

/**
 * @author Timo Vesalainen
 * @see <a href="http://catb.org/gpsd/AIVDM.html">AIVDM/AIVDO protocol decoding</a>
 */
@GenClassname("org.vesalainen.parsers.nmea.ais.AISParserImpl")
@GrammarDef(traceLevel=0)
@Terminals({
@Terminal(left="year", expression="[01]{14}", doc="Year (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser year(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sw_lon", expression="[01]{18}", doc="SW Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser swLon_I1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="accuracy", expression="[01]{1}", doc="Position Accuracy", reducer="org.vesalainen.parsers.nmea.ais.AISParser accuracy(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressuretype", expression="[01]{3}", doc="Pressure Sensor Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser pressuretype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="gaptrend", expression="[01]{2}", doc="Air Gap Trend", reducer="org.vesalainen.parsers.nmea.ais.AISParser gaptrend(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="weather_9", expression="[01]{9}", doc="Present Weather", reducer="org.vesalainen.parsers.nmea.ais.AISParser weather_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="band_a", expression="[01]{1}", doc="Channel A Band", reducer="org.vesalainen.parsers.nmea.ais.AISParser bandA(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressuretend_2", expression="[01]{2}", doc="Pressure Tendency", reducer="org.vesalainen.parsers.nmea.ais.AISParser pressuretend_2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="weather_4", expression="[01]{4}", doc="Present Weather", reducer="org.vesalainen.parsers.nmea.ais.AISParser weather_4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="customs", expression="[01]{2}", doc="Customs house", reducer="org.vesalainen.parsers.nmea.ais.AISParser customs(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="humidity", expression="[01]{7}", doc="Relative Humidity", reducer="org.vesalainen.parsers.nmea.ais.AISParser humidity(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cloudbase", expression="[01]{7}", doc="Height of cloud base", reducer="org.vesalainen.parsers.nmea.ais.AISParser cloudbase_U2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth3_5", expression="[01]{5}", doc="Measurement Depth #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser cdepth3_5(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_starboard", expression="[01]{6}", doc="Dimension to Starboard", reducer="org.vesalainen.parsers.nmea.ais.AISParser toStarboard(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="band_b", expression="[01]{1}", doc="Channel B Band", reducer="org.vesalainen.parsers.nmea.ais.AISParser bandB(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth3_9", expression="[01]{9}", doc="Measurement Depth #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser cdepth3_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swellperiod", expression="[01]{6}", doc="Swell period", reducer="org.vesalainen.parsers.nmea.ais.AISParser swellperiod(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="vhfchan", expression="[01]{12}", doc="VHF Working Channel", reducer="org.vesalainen.parsers.nmea.ais.AISParser vhfchan(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid18", expression="010010", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="berth_lon", expression="[01]{25}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser berthLon_I3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="fid19", expression="010011", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid16", expression="010000", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid17", expression="010001", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressuretend_4", expression="[01]{4}", doc="Pressure Tendency", reducer="org.vesalainen.parsers.nmea.ais.AISParser pressuretend_4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid14", expression="001110", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid15", expression="001111", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid12", expression="001100", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid13", expression="001101", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid11", expression="001011", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="persons", expression="[01]{13}", doc="# persons on board", reducer="org.vesalainen.parsers.nmea.ais.AISParser persons(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid20", expression="010100", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="text_84", expression="[01]{84}", doc="Text", reducer="org.vesalainen.parsers.nmea.ais.AISParser text_84(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="4", expression="000100", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="5", expression="000101", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="left", expression="[01]{9}", doc="Left boundary", reducer="org.vesalainen.parsers.nmea.ais.AISParser left(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sender", expression="[01]{3}", doc="Sender Class", reducer="org.vesalainen.parsers.nmea.ais.AISParser sender(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="reserved", expression="[01]{8}", doc="Regional Reserved", reducer="org.vesalainen.parsers.nmea.ais.AISParser reserved(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="6", expression="000110", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="7", expression="000111", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="8", expression="001000", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="sounder_state", expression="[01]{2}", doc="Echo sounder", reducer="org.vesalainen.parsers.nmea.ais.AISParser sounderState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="9", expression="001001", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="assigned", expression="[01]{1}", doc="Assigned", reducer="org.vesalainen.parsers.nmea.ais.AISParser assigned(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="txrx_4", expression="[01]{4}", doc="Tx/Rx mode", reducer="org.vesalainen.parsers.nmea.ais.AISParser txrx_4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="txrx_2", expression="[01]{2}", doc="Tx/Rx Mode", reducer="org.vesalainen.parsers.nmea.ais.AISParser txrx_2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="turn", expression="[01]{8}", doc="Rate of Turn (ROT)", reducer="org.vesalainen.parsers.nmea.ais.AISParser turn_I3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="linkage", expression="[01]{10}", doc="Message Linkage ID", reducer="org.vesalainen.parsers.nmea.ais.AISParser linkage(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="maneuver", expression="[01]{2}", doc="Maneuver Indicator", reducer="org.vesalainen.parsers.nmea.ais.AISParser maneuver(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swperiod1", expression="[01]{5}", doc="First Swell Period", reducer="org.vesalainen.parsers.nmea.ais.AISParser swperiod1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swperiod2", expression="[01]{5}", doc="Second Swell Period", reducer="org.vesalainen.parsers.nmea.ais.AISParser swperiod2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="quiet", expression="[01]{4}", doc="Quiet Time", reducer="org.vesalainen.parsers.nmea.ais.AISParser quiet(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nday", expression="[01]{5}", doc="ETA day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser nday(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="rwinddir", expression="[01]{7}", doc="Relative Wind Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser rwinddir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="right", expression="[01]{9}", doc="Right boundary", reducer="org.vesalainen.parsers.nmea.ais.AISParser right(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waveperiod", expression="[01]{6}", doc="Wave period", reducer="org.vesalainen.parsers.nmea.ais.AISParser waveperiod(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cs", expression="[01]{1}", doc="CS Unit", reducer="org.vesalainen.parsers.nmea.ais.AISParser cs(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ata_state", expression="[01]{2}", doc="Automatic Tracking Aid", reducer="org.vesalainen.parsers.nmea.ais.AISParser ataState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressure_9", expression="[01]{9}", doc="Air Pressure", reducer="org.vesalainen.parsers.nmea.ais.AISParser pressure_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ecdisb_state", expression="[01]{2}", doc="ECDIS Back-up", reducer="org.vesalainen.parsers.nmea.ais.AISParser ecdisbState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wdir", expression="[01]{9}", doc="Wind Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser wdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lat_I4_17", expression="[01]{17}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser lat_I4_17(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="destination_120", expression="[01]{120}", doc="Destination", reducer="org.vesalainen.parsers.nmea.ais.AISParser destination_120(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mgustspeed", expression="[01]{8}", doc="Maximum Gust Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser mgustspeed_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="payload", expression="[01]{85}", doc="Sensor payload", reducer="org.vesalainen.parsers.nmea.ais.AISParser payload(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="berth_depth", expression="[01]{8}", doc="Berth Water Depth", reducer="org.vesalainen.parsers.nmea.ais.AISParser berthDepth_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lminute", expression="[01]{6}", doc="ETA minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser lminute(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="signal", expression="[01]{5}", doc="Signal In Service", reducer="org.vesalainen.parsers.nmea.ais.AISParser signal(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="vhfradio_state", expression="[01]{2}", doc="Radio VHF", reducer="org.vesalainen.parsers.nmea.ais.AISParser vhfradioState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="midcloudt", expression="[01]{6}", doc="Cloud type (middle)", reducer="org.vesalainen.parsers.nmea.ais.AISParser midcloudt(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="horsepower", expression="[01]{18}", doc="Shaft Horsepower", reducer="org.vesalainen.parsers.nmea.ais.AISParser horsepower(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sband_state", expression="[01]{2}", doc="Radar (S-band)", reducer="org.vesalainen.parsers.nmea.ais.AISParser sbandState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icedeposit", expression="[01]{7}", doc="Ice deposit (thickness)", reducer="org.vesalainen.parsers.nmea.ais.AISParser icedeposit(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="hazardouswaste", expression="[01]{2}", doc="Waste disposal (hazardous)", reducer="org.vesalainen.parsers.nmea.ais.AISParser hazardouswaste(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lon_I4_28", expression="[01]{28}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser lon_I4_28(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="seastate", expression="[01]{4}", doc="Sea state", reducer="org.vesalainen.parsers.nmea.ais.AISParser seastate(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lowcloudt", expression="[01]{6}", doc="Cloud type (low)", reducer="org.vesalainen.parsers.nmea.ais.AISParser lowcloudt(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="month", expression="[01]{4}", doc="Month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser month(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icebearing", expression="[01]{4}", doc="Bearing of Ice Edge", reducer="org.vesalainen.parsers.nmea.ais.AISParser icebearing(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lowclouda", expression="[01]{4}", doc="Cloud amount (low)", reducer="org.vesalainen.parsers.nmea.ais.AISParser lowclouda(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dac001", expression="0000000001", doc="DAC", reducer="org.vesalainen.parsers.nmea.ais.AISParser dac(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="additional", expression="[01]{2}", doc="Additional services", reducer="org.vesalainen.parsers.nmea.ais.AISParser additional(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi1", expression="[01]{30}", doc="MMSI number 1", reducer="org.vesalainen.parsers.nmea.ais.AISParser mmsi1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icerate", expression="[01]{3}", doc="Rate of Ice Accretion", reducer="org.vesalainen.parsers.nmea.ais.AISParser icerate(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="type2_1", expression="[01]{6}", doc="First message type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type21(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi4", expression="[01]{30}", doc="MMSI number 4", reducer="org.vesalainen.parsers.nmea.ais.AISParser mmsi4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi3", expression="[01]{30}", doc="MMSI number 3", reducer="org.vesalainen.parsers.nmea.ais.AISParser mmsi3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout", expression="[01]{3}", doc="Data timeout", reducer="org.vesalainen.parsers.nmea.ais.AISParser timeout(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi2", expression="[01]{30}", doc="MMSI number 2", reducer="org.vesalainen.parsers.nmea.ais.AISParser mmsi2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="rtype", expression="[01]{5}", doc="Route Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser rtype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cartage", expression="[01]{2}", doc="Cartage", reducer="org.vesalainen.parsers.nmea.ais.AISParser cartage(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="callsign", expression="[01]{42}", doc="Call Sign", reducer="org.vesalainen.parsers.nmea.ais.AISParser callsign(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="chandler", expression="[01]{2}", doc="Chandler", reducer="org.vesalainen.parsers.nmea.ais.AISParser chandler(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="notice", expression="[01]{7}", doc="Notice Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser notice(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="owner", expression="[01]{4}", doc="Sensor owner", reducer="org.vesalainen.parsers.nmea.ais.AISParser owner(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mgustdir", expression="[01]{7}", doc="Maximum Gust Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser mgustdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lat_I4_27", expression="[01]{27}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser lat_I4_27(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="seqno", expression="[01]{2}", doc="Sequence Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser seqno(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="stevedore", expression="[01]{2}", doc="Stevedore", reducer="org.vesalainen.parsers.nmea.ais.AISParser stevedore(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_port", expression="[01]{6}", doc="Dimension to Port", reducer="org.vesalainen.parsers.nmea.ais.AISParser toPort(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fwgust", expression="[01]{7}", doc="Forecast Wind Gust", reducer="org.vesalainen.parsers.nmea.ais.AISParser fwgust(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ais_version", expression="[01]{2}", doc="AIS Version", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVersion(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swheight", expression="[01]{8}", doc="Swell Height", reducer="org.vesalainen.parsers.nmea.ais.AISParser swheight_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="station_type", expression="[01]{4}", doc="Station Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser stationType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="partno0", expression="00", doc="Part Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser partno(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="partno1", expression="01", doc="Part Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser partno(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="vislimit", expression="[01]{1}", doc="Visibility Limit", reducer="org.vesalainen.parsers.nmea.ais.AISParser vislimit(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nextport", expression="[01]{30}", doc="Next Port Of Call", reducer="org.vesalainen.parsers.nmea.ais.AISParser nextport(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="thd_state", expression="[01]{2}", doc="THD", reducer="org.vesalainen.parsers.nmea.ais.AISParser thdState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset1_2", expression="[01]{12}", doc="Second slot offset", reducer="org.vesalainen.parsers.nmea.ais.AISParser offset12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="extunit", expression="[01]{2}", doc="Unit of extension", reducer="org.vesalainen.parsers.nmea.ais.AISParser extunit(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset1_1", expression="[01]{12}", doc="First slot offset", reducer="org.vesalainen.parsers.nmea.ais.AISParser offset11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lastport", expression="[01]{30}", doc="Last Port Of Call", reducer="org.vesalainen.parsers.nmea.ais.AISParser lastport(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="model", expression="[01]{4}", doc="Unit Model Code", reducer="org.vesalainen.parsers.nmea.ais.AISParser model(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="id", expression="[01]{42}", doc="Target identifier", reducer="org.vesalainen.parsers.nmea.ais.AISParser id(long,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="heavyoil", expression="[01]{2}", doc="Heavy Fuel Oil Bunkered", reducer="org.vesalainen.parsers.nmea.ais.AISParser heavyoil(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="electrical", expression="[01]{2}", doc="Electrical", reducer="org.vesalainen.parsers.nmea.ais.AISParser electrical(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="duration_8", expression="[01]{8}", doc="Duration", reducer="org.vesalainen.parsers.nmea.ais.AISParser duration_8(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swelldir2", expression="[01]{6}", doc="Second Swell Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser swelldir2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swelldir1", expression="[01]{6}", doc="First Swell Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser swelldir1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="draught", expression="[01]{8}", doc="Draught", reducer="org.vesalainen.parsers.nmea.ais.AISParser draught_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wwperiod", expression="[01]{5}", doc="Period of Wind Waves", reducer="org.vesalainen.parsers.nmea.ais.AISParser wwperiod(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="interval", expression="[01]{4}", doc="Report Interval", reducer="org.vesalainen.parsers.nmea.ais.AISParser interval(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="unid", expression="[01]{13}", doc="UN Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser unid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swheight2", expression="[01]{6}", doc="Second Swell Height", reducer="org.vesalainen.parsers.nmea.ais.AISParser swheight2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swheight1", expression="[01]{6}", doc="First Swell Height", reducer="org.vesalainen.parsers.nmea.ais.AISParser swheight1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="bearing1", expression="[01]{9}", doc="Current Bearing #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser bearing1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lat_I3_24", expression="[01]{24}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser lat_I3_24(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="imdcat", expression="[01]{24}", doc="IMD Category", reducer="org.vesalainen.parsers.nmea.ais.AISParser imdcat(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dieseloil", expression="[01]{2}", doc="Diesel Oil Bunkered", reducer="org.vesalainen.parsers.nmea.ais.AISParser dieseloil(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fday", expression="[01]{5}", doc="From day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser fday(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="totaloil", expression="[01]{14}", doc="Total Bunker Oil", reducer="org.vesalainen.parsers.nmea.ais.AISParser totaloil(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dewtype", expression="[01]{3}", doc="Dewpoint Sensor Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser dewtype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdir", expression="[01]{9}", doc="Current Dir. Predicted", reducer="org.vesalainen.parsers.nmea.ais.AISParser cdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lat_I3_15", expression="[01]{15}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser lat_I3_15(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="course_U1_12", expression="[01]{12}", doc="Course Over Ground (COG)", reducer="org.vesalainen.parsers.nmea.ais.AISParser course_U1_12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icetype", expression="[01]{4}", doc="Amount and Type of Ice", reducer="org.vesalainen.parsers.nmea.ais.AISParser icetype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waterlog_state", expression="[01]{2}", doc="Speed Log through water", reducer="org.vesalainen.parsers.nmea.ais.AISParser waterlogState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nhour", expression="[01]{5}", doc="ETA hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser nhour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed3", expression="[01]{8}", doc="Current Speed #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser cspeed3_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed2", expression="[01]{8}", doc="Current Speed #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser cspeed2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed1", expression="[01]{8}", doc="Current Speed #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser cspeed1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airdraught_11", expression="[01]{11}", doc="Air Draught", reducer="org.vesalainen.parsers.nmea.ais.AISParser airdraught_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airdraught_13", expression="[01]{13}", doc="Air Draught", reducer="org.vesalainen.parsers.nmea.ais.AISParser airdraught_13(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdir1", expression="[01]{9}", doc="Current Direction #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser cdir1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fuel", expression="[01]{2}", doc="Bunker/fuel", reducer="org.vesalainen.parsers.nmea.ais.AISParser fuel(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="precision", expression="[01]{3}", doc="Precision", reducer="org.vesalainen.parsers.nmea.ais.AISParser precision(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pdelta", expression="[01]{10}", doc="Pressure Change", reducer="org.vesalainen.parsers.nmea.ais.AISParser pdelta_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="berth_length", expression="[01]{9}", doc="Berth length", reducer="org.vesalainen.parsers.nmea.ais.AISParser berthLength(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdir2", expression="[01]{9}", doc="Current Direction #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser cdir2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdir3", expression="[01]{9}", doc="Current Direction #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser cdir3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="speed_U1_5", expression="[01]{5}", doc="Speed Over Ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser speed_U1_5(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="second", expression="[01]{6}", doc="Time Stamp", reducer="org.vesalainen.parsers.nmea.ais.AISParser second(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="from_hour", expression="[01]{5}", doc="From UTC Hour", reducer="org.vesalainen.parsers.nmea.ais.AISParser fromHour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="preciptype_3", expression="[01]{3}", doc="Precipitation", reducer="org.vesalainen.parsers.nmea.ais.AISParser preciptype_3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="preciptype_2", expression="[01]{2}", doc="Precipitation Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser preciptype_2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="1-3", expression="000001|000010|000011", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="fmonth", expression="[01]{4}", doc="From month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser fmonth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="off_position", expression="[01]{1}", doc="Off-Position Indicator", reducer="org.vesalainen.parsers.nmea.ais.AISParser offPosition(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cup2", expression="[01]{8}", doc="Current Vector component Up (z) #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser cup2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional1", expression="[01]{2}", doc="Regional reserved 1", reducer="org.vesalainen.parsers.nmea.ais.AISParser regional1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional2", expression="[01]{2}", doc="Regional reserved 2", reducer="org.vesalainen.parsers.nmea.ais.AISParser regional2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cup1", expression="[01]{8}", doc="Current Vector component Up (z) #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser cup1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lat_I1_17", expression="[01]{17}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser lat_I1_17(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="pweather2", expression="[01]{5}", doc="Past Weather 2", reducer="org.vesalainen.parsers.nmea.ais.AISParser pweather2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="bearing", expression="[01]{10}", doc="Bearing", reducer="org.vesalainen.parsers.nmea.ais.AISParser bearing(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pweather1", expression="[01]{5}", doc="Past Weather 1", reducer="org.vesalainen.parsers.nmea.ais.AISParser pweather1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="depth1", expression="[01]{9}", doc="Measurement Depth #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser depth1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="visibility_U1_7", expression="[01]{7}", doc="Horiz. Visibility", reducer="org.vesalainen.parsers.nmea.ais.AISParser visibility_U1_7(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="visibility_U1_8", expression="[01]{8}", doc="Horiz. Visibility", reducer="org.vesalainen.parsers.nmea.ais.AISParser visibility_U1_8(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="hfradio_state", expression="[01]{2}", doc="Radio HF", reducer="org.vesalainen.parsers.nmea.ais.AISParser hfradioState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="unit", expression="[01]{2}", doc="Unit of Quantity", reducer="org.vesalainen.parsers.nmea.ais.AISParser unit(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ne_lon", expression="[01]{18}", doc="NE Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser neLon_I1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="offset4", expression="[01]{12}", doc="Offset number 4", reducer="org.vesalainen.parsers.nmea.ais.AISParser offset4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dsc", expression="[01]{1}", doc="DSC Flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser dsc(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset3", expression="[01]{12}", doc="Offset number 3", reducer="org.vesalainen.parsers.nmea.ais.AISParser offset3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="gnss", expression="[01]{1}", doc="GNSS Position status", reducer="org.vesalainen.parsers.nmea.ais.AISParser gnss(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="epaid_state", expression="[01]{2}", doc="Electronic plotting aid", reducer="org.vesalainen.parsers.nmea.ais.AISParser epaidState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sensor", expression="[01]{4}", doc="Sensor Report Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser sensor(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wavetype", expression="[01]{3}", doc="Wave Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser wavetype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="position", expression="[01]{3}", doc="Mooring Position", reducer="org.vesalainen.parsers.nmea.ais.AISParser position(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="status_2", expression="[01]{2}", doc="Status of Signal", reducer="org.vesalainen.parsers.nmea.ais.AISParser status_2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="status_4", expression="[01]{4}", doc="Navigation Status", reducer="org.vesalainen.parsers.nmea.ais.AISParser status_4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icedevel", expression="[01]{5}", doc="Ice Development", reducer="org.vesalainen.parsers.nmea.ais.AISParser icedevel(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ais_state", expression="[01]{2}", doc="AIS Class A", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dangerous", expression="[01]{120}", doc="Main Dangerous Good", reducer="org.vesalainen.parsers.nmea.ais.AISParser dangerous(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lrit_state", expression="[01]{2}", doc="LRIT", reducer="org.vesalainen.parsers.nmea.ais.AISParser lritState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wavedir", expression="[01]{9}", doc="Wave direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser wavedir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="hour", expression="[01]{5}", doc="Hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser hour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dte", expression="[01]{1}", doc="DTE", reducer="org.vesalainen.parsers.nmea.ais.AISParser dte(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="closeto", expression="[01]{120}", doc="Location of Closing To", reducer="org.vesalainen.parsers.nmea.ais.AISParser closeto(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="repeat", expression="[01]{2}", doc="Repeat Indicator", reducer="org.vesalainen.parsers.nmea.ais.AISParser repeat(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="liquidwaste", expression="[01]{2}", doc="Waste disposal (liquid)", reducer="org.vesalainen.parsers.nmea.ais.AISParser liquidwaste(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth2_5", expression="[01]{5}", doc="Measurement Depth #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser cdepth2_5(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth2_9", expression="[01]{9}", doc="Measurement Depth #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser cdepth2_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shiprepair", expression="[01]{2}", doc="Ship repair", reducer="org.vesalainen.parsers.nmea.ais.AISParser shiprepair(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="leveltrend", expression="[01]{2}", doc="Water Level Trend", reducer="org.vesalainen.parsers.nmea.ais.AISParser leveltrend(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="from_min", expression="[01]{6}", doc="From UTC Minute", reducer="org.vesalainen.parsers.nmea.ais.AISParser fromMin(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="visibility_U2_6", expression="[01]{6}", doc="Horiz. Visibility", reducer="org.vesalainen.parsers.nmea.ais.AISParser visibility_U2_6(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airdraught_U1_13", expression="[01]{13}", doc="Air Draught", reducer="org.vesalainen.parsers.nmea.ais.AISParser airdraught_U1_13(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="retransmit", expression="[01]{1}", doc="Retransmit flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser retransmit(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="speed_10", expression="[01]{10}", doc="Speed Over Ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser speed_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="direction1", expression="[01]{9}", doc="Current Direction #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser direction1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nextsignal", expression="[01]{5}", doc="Expected Next Signal", reducer="org.vesalainen.parsers.nmea.ais.AISParser nextsignal(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="portname", expression="[01]{120}", doc="Name of Port & Berth", reducer="org.vesalainen.parsers.nmea.ais.AISParser portname(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lshiptype", expression="[01]{42}", doc="Lloyd's Ship Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser lshiptype(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="day_6", expression="[01]{6}", doc="UTC Day", reducer="org.vesalainen.parsers.nmea.ais.AISParser day_6(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="day_5", expression="[01]{5}", doc="Day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser day_5(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_hour", expression="[01]{5}", doc="To UTC Hour", reducer="org.vesalainen.parsers.nmea.ais.AISParser toHour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="reason", expression="[01]{120}", doc="Reason For Closing", reducer="org.vesalainen.parsers.nmea.ais.AISParser reason(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fairgap", expression="[01]{13}", doc="Forecast Air Gap", reducer="org.vesalainen.parsers.nmea.ais.AISParser fairgap_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="increment3", expression="[01]{11}", doc="Increment", reducer="org.vesalainen.parsers.nmea.ais.AISParser increment3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="increment4", expression="[01]{11}", doc="Increment", reducer="org.vesalainen.parsers.nmea.ais.AISParser increment4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="precipitation", expression="[01]{3}", doc="Precipitation", reducer="org.vesalainen.parsers.nmea.ais.AISParser precipitation(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="band", expression="[01]{1}", doc="Band flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser band(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_stern", expression="[01]{9}", doc="Dimension to Stern", reducer="org.vesalainen.parsers.nmea.ais.AISParser toStern(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="10", expression="001010", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="11", expression="001011", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="watertemp", expression="[01]{10}", doc="Water Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser watertemp_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="12", expression="001100", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="lading", expression="[01]{2}", doc="Laden or Ballast", reducer="org.vesalainen.parsers.nmea.ais.AISParser lading(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="distance1", expression="[01]{7}", doc="Current Distance #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser distance1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="14", expression="001110", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="15", expression="001111", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="16", expression="010000", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="seaice", expression="[01]{5}", doc="Sea Ice Concentration", reducer="org.vesalainen.parsers.nmea.ais.AISParser seaice(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="17", expression="010001", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="18", expression="010010", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="19", expression="010011", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="waterlevel_U2_12", expression="[01]{12}", doc="Water Level", reducer="org.vesalainen.parsers.nmea.ais.AISParser waterlevel_U2_12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="surveyor", expression="[01]{2}", doc="Surveyor", reducer="org.vesalainen.parsers.nmea.ais.AISParser surveyor(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dest_mmsi", expression="[01]{30}", doc="Destination MMSI", reducer="org.vesalainen.parsers.nmea.ais.AISParser destMmsi(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="idtype", expression="[01]{2}", doc="Identifier type", reducer="org.vesalainen.parsers.nmea.ais.AISParser idtype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ptend", expression="[01]{4}", doc="Pressure Tendency", reducer="org.vesalainen.parsers.nmea.ais.AISParser ptend(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="solidwaste", expression="[01]{2}", doc="Waste disposal (solid)", reducer="org.vesalainen.parsers.nmea.ais.AISParser solidwaste(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_bow", expression="[01]{9}", doc="Dimension to Bow", reducer="org.vesalainen.parsers.nmea.ais.AISParser toBow(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="20", expression="010100", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="21", expression="010101", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="22", expression="010110", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="23", expression="010111", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="24", expression="011000", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="destination_30", expression="[01]{30}", doc="Destination", reducer="org.vesalainen.parsers.nmea.ais.AISParser destination_30(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="27", expression="011011", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type(int,org.vesalainen.parsers.nmea.ais.AISObserver,org.vesalainen.parsers.nmea.ais.AISContext)", radix=2)
,@Terminal(left="shiptype", expression="[01]{8}", doc="Ship Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser shiptype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset2", expression="[01]{12}", doc="Offset B", reducer="org.vesalainen.parsers.nmea.ais.AISParser offset2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="availability", expression="[01]{1}", doc="Services Availability", reducer="org.vesalainen.parsers.nmea.ais.AISParser availability(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset1", expression="[01]{12}", doc="Offset A", reducer="org.vesalainen.parsers.nmea.ais.AISParser offset1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fminute", expression="[01]{6}", doc="From minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser fminute(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="speed_U1_10", expression="[01]{10}", doc="Speed Over Ground (SOG)", reducer="org.vesalainen.parsers.nmea.ais.AISParser speed_U1_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="navrepair", expression="[01]{2}", doc="Navigation repair", reducer="org.vesalainen.parsers.nmea.ais.AISParser navrepair(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="temperature", expression="[01]{11}", doc="Air Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser temperature(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tonnage", expression="[01]{18}", doc="Gross Tonnage", reducer="org.vesalainen.parsers.nmea.ais.AISParser tonnage(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="speed1", expression="[01]{8}", doc="Current Speed #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser speed1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="radius_10", expression="[01]{10}", doc="Radius extension", reducer="org.vesalainen.parsers.nmea.ais.AISParser radius_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="radius_12", expression="[01]{12}", doc="Radius", reducer="org.vesalainen.parsers.nmea.ais.AISParser radius_12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ne_lat", expression="[01]{17}", doc="NE Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser neLat_I1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="vendorid", expression="[01]{18}", doc="Vendor ID", reducer="org.vesalainen.parsers.nmea.ais.AISParser vendorid(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ship_type", expression="[01]{8}", doc="Ship Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser shipType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lon_I1_18", expression="[01]{18}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser lon_I1_18(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="lhour", expression="[01]{5}", doc="ETA hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser lhour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="speed_6", expression="[01]{6}", doc="Speed Over Ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser speed_6(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wgustdir", expression="[01]{9}", doc="Wind Gust Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser wgustdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wgust", expression="[01]{7}", doc="Gust Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser wgust(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="text_936", expression="[01]{936}", doc="Text", reducer="org.vesalainen.parsers.nmea.ais.AISParser text_936(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth1", expression="[01]{9}", doc="Measurement Depth #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser cdepth1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="agent", expression="[01]{2}", doc="Agent", reducer="org.vesalainen.parsers.nmea.ais.AISParser agent(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="medical", expression="[01]{2}", doc="Medical facilities", reducer="org.vesalainen.parsers.nmea.ais.AISParser medical(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fhour", expression="[01]{5}", doc="From hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser fhour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="magcomp_state", expression="[01]{2}", doc="Magnetic compass", reducer="org.vesalainen.parsers.nmea.ais.AISParser magcompState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="epfd", expression="[01]{4}", doc="Type of EPFD", reducer="org.vesalainen.parsers.nmea.ais.AISParser epfd(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swelldir", expression="[01]{9}", doc="Swell direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser swelldir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="salinitytype", expression="[01]{2}", doc="Salinity Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser salinitytype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="description_6_966", expression="[01]{6,966}", doc="Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser description_6_966(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="grndlog_state", expression="[01]{2}", doc="Speed Log over ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser grndlogState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waycount", expression="[01]{5}", reducer="org.vesalainen.parsers.nmea.ais.AISParser waycount(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waveheight", expression="[01]{8}", doc="Wave height", reducer="org.vesalainen.parsers.nmea.ais.AISParser waveheight_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ice", expression="[01]{2}", doc="Ice", reducer="org.vesalainen.parsers.nmea.ais.AISParser ice(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape0", expression="000", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser shape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="aid_type", expression="[01]{5}", doc="Aid type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aidType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape5", expression="101", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser shape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape1", expression="001", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser shape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape2", expression="010", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser shape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape3", expression="011", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser shape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape4", expression="100", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser shape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="text_968", expression="[01]{968}", doc="Text", reducer="org.vesalainen.parsers.nmea.ais.AISParser text_968(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="berth_name", expression="[01]{120}", doc="Name of Berth", reducer="org.vesalainen.parsers.nmea.ais.AISParser berthName(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tday", expression="[01]{5}", doc="To day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser tday(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="box_dest", expression="[01]{70}", doc="BoxOrDest", reducer="org.vesalainen.parsers.nmea.ais.AISParser boxDest(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="crane", expression="[01]{2}", doc="Crane(s)", reducer="org.vesalainen.parsers.nmea.ais.AISParser crane(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="steer_state", expression="[01]{2}", doc="Emergency steering gear", reducer="org.vesalainen.parsers.nmea.ais.AISParser steerState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tmonth", expression="[01]{4}", doc="To month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser tmonth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="inmarsat_state", expression="[01]{2}", doc="Radio INMARSAT", reducer="org.vesalainen.parsers.nmea.ais.AISParser inmarsatState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_min", expression="[01]{6}", doc="To UTC Minute", reducer="org.vesalainen.parsers.nmea.ais.AISParser toMin(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="vdr_state", expression="[01]{2}", doc="VDR/S-VDR", reducer="org.vesalainen.parsers.nmea.ais.AISParser vdrState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="heading_7", expression="[01]{7}", doc="Heading of the ship", reducer="org.vesalainen.parsers.nmea.ais.AISParser heading_7(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="heading_9", expression="[01]{9}", doc="True Heading (HDG)", reducer="org.vesalainen.parsers.nmea.ais.AISParser heading_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wmo0", expression="0", doc="Variant", reducer="org.vesalainen.parsers.nmea.ais.AISParser wmo(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="increment1_10", expression="[01]{10}", doc="Increment A", reducer="org.vesalainen.parsers.nmea.ais.AISParser increment1_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="increment1_11", expression="[01]{11}", doc="Increment", reducer="org.vesalainen.parsers.nmea.ais.AISParser increment1_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed_U1_8", expression="[01]{8}", doc="Current Speed Predicted", reducer="org.vesalainen.parsers.nmea.ais.AISParser cspeed_U1_8(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="distance", expression="[01]{10}", doc="Distance", reducer="org.vesalainen.parsers.nmea.ais.AISParser distance(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="number3", expression="[01]{4}", doc="Reserved slots", reducer="org.vesalainen.parsers.nmea.ais.AISParser number3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout3", expression="[01]{3}", doc="Time-out", reducer="org.vesalainen.parsers.nmea.ais.AISParser timeout3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="number4", expression="[01]{4}", doc="Reserved slots", reducer="org.vesalainen.parsers.nmea.ais.AISParser number4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout4", expression="[01]{3}", doc="Time-out", reducer="org.vesalainen.parsers.nmea.ais.AISParser timeout4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="number1", expression="[01]{4}", doc="Reserved slots", reducer="org.vesalainen.parsers.nmea.ais.AISParser number1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout1", expression="[01]{3}", doc="Time-out", reducer="org.vesalainen.parsers.nmea.ais.AISParser timeout1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="berth_lat", expression="[01]{24}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser berthLat_I3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="thour", expression="[01]{5}", doc="To hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser thour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="number2", expression="[01]{4}", doc="Reserved slots", reducer="org.vesalainen.parsers.nmea.ais.AISParser number2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout2", expression="[01]{3}", doc="Time-out", reducer="org.vesalainen.parsers.nmea.ais.AISParser timeout2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="radio_19", expression="[01]{19}", doc="Radio status", reducer="org.vesalainen.parsers.nmea.ais.AISParser radio_19(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wmo1", expression="1", doc="Variant", reducer="org.vesalainen.parsers.nmea.ais.AISParser wmo(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airgap", expression="[01]{13}", doc="Air Gap", reducer="org.vesalainen.parsers.nmea.ais.AISParser airgap_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="offset2_1", expression="[01]{12}", doc="First slot offset", reducer="org.vesalainen.parsers.nmea.ais.AISParser offset21(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mfradio_state", expression="[01]{2}", doc="Radio MF", reducer="org.vesalainen.parsers.nmea.ais.AISParser mfradioState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed_U1_7", expression="[01]{7}", doc="Current Speed Predicted", reducer="org.vesalainen.parsers.nmea.ais.AISParser cspeed_U1_7(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icecause", expression="[01]{3}", doc="Cause of Ice Accretion", reducer="org.vesalainen.parsers.nmea.ais.AISParser icecause(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cnorth1", expression="[01]{8}", doc="Current Vector component North (u) #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser cnorth1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="virtual_aid", expression="[01]{1}", doc="Virtual-aid flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser virtualAid(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="closefrom", expression="[01]{120}", doc="Location Of Closing From", reducer="org.vesalainen.parsers.nmea.ais.AISParser closefrom(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="twinddir", expression="[01]{7}", doc="True Wind Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser twinddir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="xband_state", expression="[01]{2}", doc="Radar (X-band)", reducer="org.vesalainen.parsers.nmea.ais.AISParser xbandState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nminute", expression="[01]{6}", doc="ETA minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser nminute(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="navtex_state", expression="[01]{2}", doc="NAVTEX", reducer="org.vesalainen.parsers.nmea.ais.AISParser navtexState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="gnss_state", expression="[01]{2}", doc="GNSS", reducer="org.vesalainen.parsers.nmea.ais.AISParser gnssState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="rwindspeed", expression="[01]{8}", doc="Relative Wind Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser rwindspeed_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="location", expression="[01]{120}", doc="Location", reducer="org.vesalainen.parsers.nmea.ais.AISParser location(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cnorth2", expression="[01]{8}", doc="Current Vector component North (u) #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser cnorth2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dewpoint", expression="[01]{10}", doc="Dew Point", reducer="org.vesalainen.parsers.nmea.ais.AISParser dewpoint(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="channel_a", expression="[01]{12}", doc="Channel A", reducer="org.vesalainen.parsers.nmea.ais.AISParser channelA(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="channel_b", expression="[01]{12}", doc="Channel B", reducer="org.vesalainen.parsers.nmea.ais.AISParser channelB(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lon_I3_25", expression="[01]{25}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser lon_I3_25(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="type1_2", expression="[01]{6}", doc="Second message type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="type1_1", expression="[01]{6}", doc="First message type", reducer="org.vesalainen.parsers.nmea.ais.AISParser type11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swellheight", expression="[01]{8}", doc="Swell height", reducer="org.vesalainen.parsers.nmea.ais.AISParser swellheight_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="course_7", expression="[01]{7}", doc="Course Over Ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser course_7(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tugs", expression="[01]{2}", doc="Tugs", reducer="org.vesalainen.parsers.nmea.ais.AISParser tugs(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="course_9", expression="[01]{9}", doc="Course Over Ground", reducer="org.vesalainen.parsers.nmea.ais.AISParser course_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="highcloudt", expression="[01]{6}", doc="Cloud type (high)", reducer="org.vesalainen.parsers.nmea.ais.AISParser highcloudt(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="visgreater", expression="[01]{7}", doc="Max. visibility", reducer="org.vesalainen.parsers.nmea.ais.AISParser visgreater(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="radio_20", expression="[01]{20}", doc="Radio status", reducer="org.vesalainen.parsers.nmea.ais.AISParser radio_20(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="increment2_11", expression="[01]{11}", doc="Increment", reducer="org.vesalainen.parsers.nmea.ais.AISParser increment2_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="increment2_10", expression="[01]{10}", doc="Increment B", reducer="org.vesalainen.parsers.nmea.ais.AISParser increment2_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lon_I3_16", expression="[01]{16}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser lon_I3_16(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="zonesize", expression="[01]{3}", doc="Zone size", reducer="org.vesalainen.parsers.nmea.ais.AISParser zonesize(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="provisions", expression="[01]{2}", doc="Provisions", reducer="org.vesalainen.parsers.nmea.ais.AISParser provisions(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="future1", expression="[01]{2}", doc="Reserved for future", reducer="org.vesalainen.parsers.nmea.ais.AISParser future1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="duration_18", expression="[01]{18}", doc="Duration", reducer="org.vesalainen.parsers.nmea.ais.AISParser duration_18(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="future2", expression="[01]{2}", doc="Reserved for future", reducer="org.vesalainen.parsers.nmea.ais.AISParser future2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swperiod", expression="[01]{6}", doc="Swell Period", reducer="org.vesalainen.parsers.nmea.ais.AISParser swperiod(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lift", expression="[01]{2}", doc="Lift(s)", reducer="org.vesalainen.parsers.nmea.ais.AISParser lift(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="nmonth", expression="[01]{4}", doc="ETA month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser nmonth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressure_U1_16", expression="[01]{16}", doc="Water Pressure", reducer="org.vesalainen.parsers.nmea.ais.AISParser pressure_U1_16(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressure_U1_11", expression="[01]{11}", doc="Pressure at sea level", reducer="org.vesalainen.parsers.nmea.ais.AISParser pressure_U1_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="arpa_state", expression="[01]{2}", doc="Radar (ARPA)", reducer="org.vesalainen.parsers.nmea.ais.AISParser arpaState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fwspeed", expression="[01]{7}", doc="Forecast Wind Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser fwspeed(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="north", expression="[01]{8}", doc="N dimension", reducer="org.vesalainen.parsers.nmea.ais.AISParser north(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lon_I4_18", expression="[01]{18}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser lon_I4_18(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="depthtype", expression="[01]{3}", doc="Depth Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser depthtype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="surftemp", expression="[01]{9}", doc="Sea Surface Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser surftemp_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="totalcloud", expression="[01]{4}", doc="Total Cloud Cover", reducer="org.vesalainen.parsers.nmea.ais.AISParser totalcloud(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="power", expression="[01]{1}", doc="Power", reducer="org.vesalainen.parsers.nmea.ais.AISParser power(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tcs_state", expression="[01]{2}", doc="Track control system", reducer="org.vesalainen.parsers.nmea.ais.AISParser tcsState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="description_6_930", expression="[01]{6,930}", doc="Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser description_6_930(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="minute_6", expression="[01]{6}", doc="Minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser minute_6(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="minute_3", expression="[01]{3}", doc="UTC minute", reducer="org.vesalainen.parsers.nmea.ais.AISParser minute_3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="display", expression="[01]{1}", doc="Display flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser display(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="site", expression="[01]{7}", doc="Site ID", reducer="org.vesalainen.parsers.nmea.ais.AISParser site(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional_2", expression="[01]{2}", doc="Regional reserved", reducer="org.vesalainen.parsers.nmea.ais.AISParser regional_2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional_4", expression="[01]{4}", doc="Regional reserved", reducer="org.vesalainen.parsers.nmea.ais.AISParser regional_4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional_8", expression="[01]{8}", doc="Regional reserved", reducer="org.vesalainen.parsers.nmea.ais.AISParser regional_8(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airtemp_U1_10", expression="[01]{10}", doc="Air Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser airtemp_U1_10(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airtemp_U1_11", expression="[01]{11}", doc="Air Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser airtemp_U1_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lday", expression="[01]{5}", doc="ETA day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser lday(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="data", expression="[01]{1,736}", doc="Payload", reducer="org.vesalainen.parsers.nmea.ais.AISParser data(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mothership_dim", expression="[01]{1,36}", doc="Mothership or Dim", reducer="org.vesalainen.parsers.nmea.ais.AISParser mothershipDim(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="chart_state", expression="[01]{2}", doc="Paper Nautical Chart", reducer="org.vesalainen.parsers.nmea.ais.AISParser chartState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="name_84", expression="[01]{84}", doc="Name", reducer="org.vesalainen.parsers.nmea.ais.AISParser name_84(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="conductivity", expression="[01]{10}", doc="Conductivity", reducer="org.vesalainen.parsers.nmea.ais.AISParser conductivity_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="gyro_state", expression="[01]{2}", doc="Gyro compass", reducer="org.vesalainen.parsers.nmea.ais.AISParser gyroState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="secondport", expression="[01]{30}", doc="Second Port Of Call", reducer="org.vesalainen.parsers.nmea.ais.AISParser secondport(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="raim", expression="[01]{1}", doc="RAIM flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser raim(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wspeed", expression="[01]{7}", doc="Average Wind Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser wspeed(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="water", expression="[01]{2}", doc="Potable water", reducer="org.vesalainen.parsers.nmea.ais.AISParser water(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="icestate", expression="[01]{5}", doc="Ice Situation", reducer="org.vesalainen.parsers.nmea.ais.AISParser icestate(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="iceclass", expression="[01]{4}", doc="Ice Class", reducer="org.vesalainen.parsers.nmea.ais.AISParser iceclass(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="addressed", expression="[01]{1}", doc="Addressed", reducer="org.vesalainen.parsers.nmea.ais.AISParser addressed(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sw_lat", expression="[01]{17}", doc="SW Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser swLat_I1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="msg22", expression="[01]{1}", doc="Message 22 flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser msg22(boolean,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="bnwas_state", expression="[01]{2}", doc="BNWAS", reducer="org.vesalainen.parsers.nmea.ais.AISParser bnwasState(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="name_120", expression="[01]{120}", doc="Name", reducer="org.vesalainen.parsers.nmea.ais.AISParser name_120(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi", expression="[01]{30}", doc="MMSI", reducer="org.vesalainen.parsers.nmea.ais.AISParser mmsi(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="tminute", expression="[01]{6}", doc="To minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser tminute(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="twindspeed", expression="[01]{8}", doc="True Wind Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser twindspeed(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid29", expression="011101", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fwdir", expression="[01]{9}", doc="Forecast Wind Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser fwdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid27", expression="011011", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="scale", expression="[01]{2}", doc="Scale factor", reducer="org.vesalainen.parsers.nmea.ais.AISParser scale(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid28", expression="011100", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swelltype", expression="[01]{3}", doc="Swell Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser swelltype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="imo", expression="[01]{30}", doc="IMO Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser imo(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid26", expression="011010", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid23", expression="010111", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid24", expression="011000", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid21", expression="010101", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid22", expression="010110", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid30", expression="011110", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid31", expression="011111", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="east", expression="[01]{8}", doc="E dimension", reducer="org.vesalainen.parsers.nmea.ais.AISParser east(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="name_ext", expression="[01]{1,88}", doc="Name Extension", reducer="org.vesalainen.parsers.nmea.ais.AISParser nameExt(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="station", expression="[01]{120}", doc="Name of Signal Station", reducer="org.vesalainen.parsers.nmea.ais.AISParser station(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="alt_11", expression="[01]{11}", doc="Altitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser alt_11(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="alt_12", expression="[01]{12}", doc="Altitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser alt_12(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="steam", expression="[01]{2}", doc="Steam", reducer="org.vesalainen.parsers.nmea.ais.AISParser steam(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lightoil", expression="[01]{2}", doc="Light Fuel Oil Bunkered", reducer="org.vesalainen.parsers.nmea.ais.AISParser lightoil(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="salinity", expression="[01]{9}", doc="Salinity", reducer="org.vesalainen.parsers.nmea.ais.AISParser salinity_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="amount", expression="[01]{10}", doc="Amount of Cargo", reducer="org.vesalainen.parsers.nmea.ais.AISParser amount(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="orientation", expression="[01]{9}", doc="Orientation", reducer="org.vesalainen.parsers.nmea.ais.AISParser orientation(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sensortype", expression="[01]{3}", doc="Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser sensortype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lmonth", expression="[01]{4}", doc="ETA month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser lmonth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ballast", expression="[01]{2}", doc="Reserved ballast exchange", reducer="org.vesalainen.parsers.nmea.ais.AISParser ballast(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fid32", expression="100000", doc="FID", reducer="org.vesalainen.parsers.nmea.ais.AISParser fid(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wwheight", expression="[01]{6}", doc="Height of Wind Waves", reducer="org.vesalainen.parsers.nmea.ais.AISParser wwheight(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waterlevel_U1_9", expression="[01]{9}", doc="Water Level", reducer="org.vesalainen.parsers.nmea.ais.AISParser waterlevel_U1_9(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="serial", expression="[01]{20}", doc="Serial Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser serial(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ceast1", expression="[01]{8}", doc="Current Vector component East (v) #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser ceast1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shipname", expression="[01]{120}", doc="Vessel Name", reducer="org.vesalainen.parsers.nmea.ais.AISParser shipname(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ceast2", expression="[01]{8}", doc="Current Vector component East (v) #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser ceast2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
})
@Rules({
@Rule(left="23Messages", value={"(23Content end)+"})
,@Rule(left="shape", value={"Sector"})
,@Rule(left="6Content", value={"IMO289BerthingDataAddressed"})
,@Rule(left="Sector", value={"shape2", "scale", "lon_I3_25", "lat_I3_24", "precision", "radius_12", "left", "right"})
,@Rule(left="8Content", value={"IMO289RouteInformationBroadcast"})
,@Rule(left="Type16AssignmentModeCommandB", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "offset1", "increment1_10", "mmsi2", "offset2", "increment2_10"})
,@Rule(left="22Messages", value={"(22Content end)+"})
,@Rule(left="17Content", value={"Type17DGNSSBroadcastBinaryMessage"})
,@Rule(left="8Content", value={"WeatherObservationReportFromShipNonWMOVariant"})
,@Rule(left="Type20DataLinkManagementMessage2", value={"repeat", "mmsi", "'[01]{2}'", "offset1", "number1", "timeout1", "increment1_11", "offset2", "number2", "timeout2", "increment2_11"})
,@Rule(left="11Content", value={"Type11UTCDateResponse"})
,@Rule(left="12Content", value={"Type12AddressedSafetyRelatedMessage"})
,@Rule(left="Type23GroupAssignmentCommand", value={"repeat", "mmsi", "'[01]{2}'", "ne_lon", "ne_lat", "sw_lon", "sw_lat", "station_type", "ship_type", "'[01]{22}'", "txrx_2", "interval", "quiet", "('[01]{6}')?"})
,@Rule(left="20Messages", value={"(20Content end)+"})
,@Rule(left="8Content", value={"MeteorologicalAndHydrologicalDataIMO236"})
,@Rule(left="AreaNoticeAddressedMessageHeader", value={"repeat", "mmsi", "seqno", "dac001", "fid22", "linkage", "notice", "month", "day_5", "hour", "minute_6", "duration_18", "(shape)+"})
,@Rule(left="24Messages", value={"(24Content end)+"})
,@Rule(left="21Content", value={"Type21AidToNavigationReport1"})
,@Rule(left="Type15Interrogation3", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "type1_1", "offset1_1", "'[01]{2}'", "type1_2", "offset1_2", "'[01]{2}'", "mmsi2", "type2_1", "offset2_1", "('[01]{2}')?"})
,@Rule(left="6Content", value={"TidalWindowIMO289"})
,@Rule(left="Type18StandardClassBCSPositionReport", value={"repeat", "mmsi", "reserved", "speed_U1_10", "accuracy", "lon_I4_28", "lat_I4_27", "course_U1_12", "heading_9", "second", "regional_2", "cs", "display", "dsc", "band", "msg22", "assigned", "raim", "radio_20"})
,@Rule(left="IMO289ClearanceTimeToEnterPort", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid18", "linkage", "month", "day_5", "hour", "minute_6", "portname", "destination_30", "lon_I3_25", "lat_I3_24", "('[01]{43}')?"})
,@Rule(left="21Content", value={"Type21AidToNavigationReport2"})
,@Rule(left="Type5StaticAndVoyageRelatedData", value={"repeat", "mmsi", "ais_version", "imo", "callsign", "shipname", "shiptype", "to_bow", "to_stern", "to_port", "to_starboard", "epfd", "month", "day_5", "hour", "minute_6", "draught", "destination_120", "dte", "('[01]{1}')?"})
,@Rule(left="Type15Interrogation2", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "type1_1", "offset1_1", "'[01]{2}'", "type1_2", "offset1_2", "('[01]{2}')?"})
,@Rule(left="message", value={"27"})
,@Rule(left="Type20DataLinkManagementMessage4", value={"repeat", "mmsi", "'[01]{2}'", "offset1", "number1", "timeout1", "increment1_11", "offset2", "number2", "timeout2", "increment2_11", "offset3", "number3", "timeout3", "increment3", "offset4", "number4", "timeout4", "increment4"})
,@Rule(left="message", value={"24"})
,@Rule(left="message", value={"23"})
,@Rule(left="message", value={"20"})
,@Rule(left="message", value={"22"})
,@Rule(left="AreaNoticeAddressedMessageHeader", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid23", "linkage", "notice", "month", "day_5", "hour", "minute_6", "duration_18", "(shape)+"})
,@Rule(left="message", value={"21"})
,@Rule(left="WindReportPayload", value={"wspeed", "wgust", "wdir", "wgustdir", "sensortype", "fwspeed", "fwgust", "fwdir", "day_5", "hour", "minute_6", "duration_8", "('[01]{3}')?"})
,@Rule(left="Type20DataLinkManagementMessage3", value={"repeat", "mmsi", "'[01]{2}'", "offset1", "number1", "timeout1", "increment1_11", "offset2", "number2", "timeout2", "increment2_11", "offset3", "number3", "timeout3", "increment3"})
,@Rule(left="23Content", value={"Type23GroupAssignmentCommand"})
,@Rule(left="messages", value={"message+"})
,@Rule(left="Type4BaseStationReport", value={"repeat", "mmsi", "year", "month", "day_5", "hour", "minute_6", "second", "accuracy", "lon_I4_28", "lat_I4_27", "epfd", "'[01]{10}'", "raim", "radio_19"})
,@Rule(left="message", value={"17"})
,@Rule(left="message", value={"16"})
,@Rule(left="message", value={"19"})
,@Rule(left="message", value={"18"})
,@Rule(left="Type27LongRangeAISBroadcastMessage", value={"repeat", "mmsi", "accuracy", "raim", "status_4", "lon_I4_18", "lat_I4_17", "speed_6", "course_9", "gnss", "('[01]{1}')?"})
,@Rule(left="IMO236DangerousCargoIndication", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid12", "lastport", "lmonth", "lday", "lhour", "lminute", "nextport", "nmonth", "nday", "nhour", "nminute", "dangerous", "imdcat", "unid", "amount", "unit", "('[01]{3}')?"})
,@Rule(left="message", value={"12"})
,@Rule(left="message", value={"15"})
,@Rule(left="message", value={"14"})
,@Rule(left="SeaStateReportPayload", value={"swheight", "swperiod", "swelldir", "seastate", "swelltype", "watertemp", "distance1", "depthtype", "waveheight", "waveperiod", "wavedir", "wavetype", "salinity"})
,@Rule(left="message", value={"11"})
,@Rule(left="message", value={"10"})
,@Rule(left="1-3Messages", value={"(1-3Content end)+"})
,@Rule(left="message", value={"9"})
,@Rule(left="IMO236ExtendedShipStaticAndVoyageRelatedData", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid15", "airdraught_11", "('[01]{5}')?"})
,@Rule(left="18Content", value={"Type18StandardClassBCSPositionReport"})
,@Rule(left="message", value={"6"})
,@Rule(left="HorizontalCurrentReportPayload", value={"bearing1", "distance1", "speed1", "direction1", "depth1", "bearing1", "distance1", "speed1", "direction1", "depth1", "('[01]{1}')?"})
,@Rule(left="message", value={"5"})
,@Rule(left="message", value={"8"})
,@Rule(left="shape", value={"AssociatedText"})
,@Rule(left="message", value={"7"})
,@Rule(left="message", value={"4"})
,@Rule(left="27Messages", value={"(27Content end)+"})
,@Rule(left="8Content", value={"VTSGeneratedSyntheticTargets"})
,@Rule(left="6Content", value={"AreaNoticeAddressedMessageHeader"})
,@Rule(left="16Content", value={"Type16AssignmentModeCommandA"})
,@Rule(left="16Content", value={"Type16AssignmentModeCommandB"})
,@Rule(left="6Content", value={"IMO289RouteInformationAddressed"})
,@Rule(left="SiteLocationPayload", value={"lon_I4_28", "lat_I4_27", "alt_11", "owner", "timeout", "('[01]{12}')?"})
,@Rule(left="IMO289RouteInformationBroadcast", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid27", "linkage", "sender", "rtype", "month", "day_5", "hour", "minute_6", "duration_18", "waycount", "(lon_I4_28 lat_I4_27)+"})
,@Rule(left="18Messages", value={"(18Content end)+"})
,@Rule(left="7Messages", value={"(7Content end)+"})
,@Rule(left="IMO289BerthingDataAddressed", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid20", "linkage", "berth_length", "berth_depth", "position", "month", "day_5", "hour", "minute_6", "availability", "agent", "fuel", "chandler", "stevedore", "electrical", "water", "customs", "cartage", "crane", "lift", "medical", "navrepair", "provisions", "shiprepair", "surveyor", "steam", "tugs", "solidwaste", "liquidwaste", "hazardouswaste", "ballast", "additional", "regional1", "regional2", "future1", "future2", "berth_name", "berth_lon", "berth_lat"})
,@Rule(left="8Content", value={"IMO236ExtendedShipStaticAndVoyageRelatedData"})
,@Rule(left="12Messages", value={"(12Content end)+"})
,@Rule(left="15Content", value={"Type15Interrogation1"})
,@Rule(left="15Content", value={"Type15Interrogation2"})
,@Rule(left="15Content", value={"Type15Interrogation3"})
,@Rule(left="10Messages", value={"(10Content end)+"})
,@Rule(left="CommonNavigationBlock", value={"repeat", "mmsi", "status_4", "turn", "speed_U1_10", "accuracy", "lon_I4_28", "lat_I4_27", "course_U1_12", "heading_9", "second", "maneuver", "'[01]{3}'", "raim", "radio_19"})
,@Rule(left="6Content", value={"IMO236TidalWindow"})
,@Rule(left="19Messages", value={"(19Content end)+"})
,@Rule(left="6Messages", value={"(6Content end)+"})
,@Rule(left="MeteorologicalAndHydrologicalDataIMO289", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid31", "lon_I3_25", "lat_I3_24", "accuracy", "day_5", "hour", "minute_6", "wspeed", "wgust", "wdir", "wgustdir", "airtemp_U1_11", "humidity", "dewpoint", "pressure_9", "pressuretend_2", "visgreater", "visibility_U1_8", "waterlevel_U2_12", "leveltrend", "cspeed_U1_8", "cdir", "cspeed2", "cdir2", "cdepth2_5", "cspeed3", "cdir3", "cdepth3_5", "waveheight", "waveperiod", "wavedir", "swellheight", "swellperiod", "swelldir", "seastate", "watertemp", "precipitation", "salinity", "ice", "('[01]{10}')?"})
,@Rule(left="Type24StaticDataReportB", value={"repeat", "mmsi", "partno1", "shiptype", "vendorid", "model", "serial", "callsign", "mothership_dim"})
,@Rule(left="Type24StaticDataReportA", value={"repeat", "mmsi", "partno0", "shipname", "('[01]{1,8}')?"})
,@Rule(left="shape", value={"Polyline"})
,@Rule(left="21Messages", value={"(21Content end)+"})
,@Rule(left="IMO289TextDescriptionAddressed", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid30", "linkage", "description_6_930"})
,@Rule(left="IMO289MarineTrafficSignal", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid19", "linkage", "station", "lon_I3_25", "lat_I3_24", "status_2", "signal", "hour", "minute_6", "nextsignal", "('[01]{102}')?"})
,@Rule(left="CircleOrPoint", value={"shape0", "scale", "lon_I3_25", "lat_I3_24", "precision", "radius_12", "('[01]{18}')?"})
,@Rule(left="11Messages", value={"(11Content end)+"})
,@Rule(left="6Content", value={"IMO236NumberOfPersonsOnBoard"})
,@Rule(left="24Content", value={"Type24StaticDataReportB"})
,@Rule(left="Type14SafetyRelatedBroadcastMessage", value={"repeat", "mmsi", "'[01]{2}'", "text_968"})
,@Rule(left="24Content", value={"Type24StaticDataReportA"})
,@Rule(left="TidalWindowIMO289", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid32", "month", "day_5", "(lon_I3_25 lat_I3_24 from_hour from_min to_hour to_min cdir cspeed_U1_8)+"})
,@Rule(left="FairwayClosed", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid13", "reason", "closefrom", "closeto", "radius_10", "extunit", "fday", "fmonth", "fhour", "fminute", "tday", "tmonth", "thour", "tminute", "('[01]{4}')?"})
,@Rule(left="6Content", value={"IMO289ClearanceTimeToEnterPort"})
,@Rule(left="IMO289RouteInformationAddressed", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid28", "linkage", "sender", "rtype", "month", "day_5", "hour", "minute_6", "duration_18", "waycount", "(lon_I4_28 lat_I4_27)+"})
,@Rule(left="8Content", value={"FairwayClosed"})
,@Rule(left="IMO236NumberOfPersonsOnBoard", value={"repeat", "mmsi", "seqno", "dac001", "fid16", "persons", "('[01]{3}')?"})
,@Rule(left="Type19ExtendedClassBCSPositionReport", value={"repeat", "mmsi", "reserved", "speed_U1_10", "accuracy", "lon_I4_28", "lat_I4_27", "course_U1_12", "heading_9", "second", "regional_4", "shipname", "shiptype", "to_bow", "to_stern", "to_port", "to_starboard", "epfd", "raim", "dte", "assigned", "('[01]{4}')?"})
,@Rule(left="4Content", value={"Type4BaseStationReport"})
,@Rule(left="WeatherReportPayload", value={"temperature", "sensortype", "preciptype_2", "visibility_U1_8", "dewpoint", "dewtype", "pressure_9", "pressuretend_2", "pressuretype", "salinity", "('[01]{25}')?"})
,@Rule(left="CurrentFlow2DReportPayload", value={"cspeed1", "cdir1", "cdepth1", "cspeed2", "cdir2", "cdepth2_9", "cspeed3", "cdir3", "cdepth3_9", "sensortype", "('[01]{4}')?"})
,@Rule(left="Type22ChannelManagement", value={"repeat", "mmsi", "'[01]{2}'", "channel_a", "channel_b", "txrx_4", "power", "box_dest", "addressed", "band_a", "band_b", "zonesize", "('[01]{23}')?"})
,@Rule(left="6Content", value={"IMO236DangerousCargoIndication"})
,@Rule(left="IMO289TextDescriptionBroadcast", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid29", "linkage", "description_6_966"})
,@Rule(left="Type12AddressedSafetyRelatedMessage", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "text_936"})
,@Rule(left="Type16AssignmentModeCommandA", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "offset1", "increment1_10"})
,@Rule(left="EnvironmentalMessageHeader", value={"repeat", "mmsi", "seqno", "dac001", "fid26", "(sensor day_5 hour minute_6 site payload)+"})
,@Rule(left="VTSGeneratedSyntheticTargets", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid17", "(idtype id '[01]{4}' lat_I3_24 lon_I3_25 course_9 second speed_10)+"})
,@Rule(left="Polygon", value={"shape4", "scale", "(bearing distance)+"})
,@Rule(left="5Messages", value={"(5Content end)+"})
,@Rule(left="Type21AidToNavigationReport1", value={"repeat", "mmsi", "aid_type", "name_120", "accuracy", "lon_I4_28", "lat_I4_27", "to_bow", "to_stern", "to_port", "to_starboard", "epfd", "second", "off_position", "regional_8", "raim", "virtual_aid", "assigned", "('[01]{1}')?"})
,@Rule(left="Type9StandardSARAircraftPositionReport", value={"repeat", "mmsi", "alt_12", "speed_10", "accuracy", "lon_I4_28", "lat_I4_27", "course_U1_12", "second", "regional_8", "dte", "'[01]{3}'", "assigned", "raim", "radio_20"})
,@Rule(left="9Content", value={"Type9StandardSARAircraftPositionReport"})
,@Rule(left="8Content", value={"WeatherObservationReportFromShipWMOVariant"})
,@Rule(left="7Content", value={"Type7BinaryAcknowledge"})
,@Rule(left="22Content", value={"Type22ChannelManagement"})
,@Rule(left="4Messages", value={"(4Content end)+"})
,@Rule(left="Polyline", value={"shape3", "scale", "(bearing distance)+"})
,@Rule(left="Type21AidToNavigationReport2", value={"repeat", "mmsi", "aid_type", "name_120", "accuracy", "lon_I4_28", "lat_I4_27", "to_bow", "to_stern", "to_port", "to_starboard", "epfd", "second", "off_position", "regional_8", "raim", "virtual_aid", "assigned", "'[01]{1}'", "name_ext"})
,@Rule(left="shape", value={"Rectangle"})
,@Rule(left="Type11UTCDateResponse", value={"repeat", "mmsi", "year", "month", "day_5", "hour", "minute_6", "second", "accuracy", "lon_I4_28", "lat_I4_27", "epfd", "'[01]{10}'", "raim", "radio_19"})
,@Rule(left="8Messages", value={"(8Content end)+"})
,@Rule(left="17Messages", value={"(17Content end)+"})
,@Rule(left="shape", value={"CircleOrPoint"})
,@Rule(left="shape", value={"Polygon"})
,@Rule(left="5Content", value={"Type5StaticAndVoyageRelatedData"})
,@Rule(left="6Content", value={"IMO289TextDescriptionAddressed"})
,@Rule(left="Type10UTCDateInquiry", value={"repeat", "mmsi", "'[01]{2}'", "dest_mmsi", "('[01]{2}')?"})
,@Rule(left="CurrentFlow3DPayload", value={"cnorth1", "ceast1", "cup1", "cdepth1", "cnorth2", "ceast2", "cup2", "cdepth2_9", "sensortype", "('[01]{16}')?"})
,@Rule(left="20Content", value={"Type20DataLinkManagementMessage4"})
,@Rule(left="20Content", value={"Type20DataLinkManagementMessage3"})
,@Rule(left="20Content", value={"Type20DataLinkManagementMessage2"})
,@Rule(left="10Content", value={"Type10UTCDateInquiry"})
,@Rule(left="20Content", value={"Type20DataLinkManagementMessage1"})
,@Rule(left="StationIDPayload", value={"name_84", "('[01]{1}')?"})
,@Rule(left="15Messages", value={"(15Content end)+"})
,@Rule(left="9Messages", value={"(9Content end)+"})
,@Rule(left="AssociatedText", value={"shape5", "text_84"})
,@Rule(left="16Messages", value={"(16Content end)+"})
,@Rule(left="Type15Interrogation1", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "type1_1", "offset1_1"})
,@Rule(left="8Content", value={"IMO289MarineTrafficSignal"})
,@Rule(left="Type17DGNSSBroadcastBinaryMessage", value={"repeat", "mmsi", "'[01]{2}'", "lon_I1_18", "lat_I1_17", "'[01]{5}'", "data"})
,@Rule(left="8Content", value={"MeteorologicalAndHydrologicalDataIMO289"})
,@Rule(left="14Content", value={"Type14SafetyRelatedBroadcastMessage"})
,@Rule(left="message", value={"1-3"})
,@Rule(left="8Content", value={"IMO289TextDescriptionBroadcast"})
,@Rule(left="Type20DataLinkManagementMessage1", value={"repeat", "mmsi", "'[01]{2}'", "offset1", "number1", "timeout1", "increment1_11"})
,@Rule(left="IMO289ExtendedShipStaticAndVoyageRelatedData", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid24", "linkage", "airdraught_13", "lastport", "nextport", "secondport", "ais_state", "ata_state", "bnwas_state", "ecdisb_state", "chart_state", "sounder_state", "epaid_state", "steer_state", "gnss_state", "gyro_state", "lrit_state", "magcomp_state", "navtex_state", "arpa_state", "sband_state", "xband_state", "hfradio_state", "inmarsat_state", "mfradio_state", "vhfradio_state", "grndlog_state", "waterlog_state", "thd_state", "tcs_state", "vdr_state", "'[01]{2}'", "iceclass", "horsepower", "vhfchan", "lshiptype", "tonnage", "lading", "heavyoil", "lightoil", "dieseloil", "totaloil", "persons", "('[01]{10}')?"})
,@Rule(left="14Messages", value={"(14Content end)+"})
,@Rule(left="1-3Content", value={"CommonNavigationBlock"})
,@Rule(left="MeteorologicalAndHydrologicalDataIMO236", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid11", "lat_I3_24", "lon_I3_25", "day_5", "hour", "minute_6", "wspeed", "wgust", "wdir", "wgustdir", "temperature", "humidity", "dewpoint", "pressure_9", "pressuretend_2", "visibility_U1_8", "waterlevel_U1_9", "leveltrend", "cspeed_U1_8", "cdir", "cspeed2", "cdir2", "cdepth2_5", "cspeed3", "cdir3", "cdepth3_5", "waveheight", "waveperiod", "wavedir", "swellheight", "swellperiod", "swelldir", "seastate", "watertemp", "preciptype_3", "salinity", "ice", "('[01]{6}')?"})
,@Rule(left="Rectangle", value={"shape1", "scale", "lon_I3_25", "lat_I3_24", "precision", "east", "north", "orientation", "('[01]{5}')?"})
,@Rule(left="Type7BinaryAcknowledge", value={"repeat", "mmsi", "'[01]{2}'", "mmsi1", "'[01]{2}'", "mmsi2", "'[01]{2}'", "mmsi3", "'[01]{2}'", "mmsi4", "('[01]{2}')?"})
,@Rule(left="19Content", value={"Type19ExtendedClassBCSPositionReport"})
,@Rule(left="IMO236TidalWindow", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid14", "month", "day_5", "(lat_I4_27 lon_I4_28 from_hour from_min to_hour to_min cdir cspeed_U1_7)+"})
,@Rule(left="6Content", value={"EnvironmentalMessageHeader"})
,@Rule(left="AirGapAirDraftReportPayload", value={"airdraught_U1_13", "airgap", "gaptrend", "fairgap", "day_5", "hour", "minute_6", "('[01]{28}')?"})
,@Rule(left="SalinityReportPayload", value={"watertemp", "conductivity", "pressure_U1_16", "salinity", "salinitytype", "sensortype", "('[01]{35}')?"})
,@Rule(left="8Content", value={"IMO289ExtendedShipStaticAndVoyageRelatedData"})
,@Rule(left="WeatherObservationReportFromShipNonWMOVariant", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid21", "wmo0", "location", "lon_I3_25", "lat_I3_24", "day_5", "hour", "minute_6", "weather_4", "vislimit", "visibility_U1_7", "humidity", "wspeed", "wdir", "pressure_9", "pressuretend_4", "airtemp_U1_11", "watertemp", "waveperiod", "waveheight", "wavedir", "swellheight", "swelldir", "swellperiod", "('[01]{3}')?"})
,@Rule(left="WeatherObservationReportFromShipWMOVariant", value={"repeat", "mmsi", "'[01]{2}'", "dac001", "fid21", "wmo1", "lon_I3_16", "lat_I3_15", "month", "day_6", "hour", "minute_3", "course_7", "speed_U1_5", "heading_7", "pressure_U1_11", "pdelta", "ptend", "twinddir", "twindspeed", "rwinddir", "rwindspeed", "mgustspeed", "mgustdir", "airtemp_U1_10", "humidity", "surftemp", "visibility_U2_6", "weather_9", "pweather1", "pweather2", "totalcloud", "lowclouda", "lowcloudt", "midcloudt", "highcloudt", "cloudbase", "wwperiod", "wwheight", "swelldir1", "swperiod1", "swheight1", "swelldir2", "swperiod2", "swheight2", "icedeposit", "icerate", "icecause", "seaice", "icetype", "icestate", "icedevel", "icebearing"})
,@Rule(left="IMO236NumberOfPersonsOnBoard", value={"repeat", "mmsi", "seqno", "dest_mmsi", "retransmit", "'[01]{1}'", "dac001", "fid16", "persons", "('[01]{35}')?"})
,@Rule(left="27Content", value={"Type27LongRangeAISBroadcastMessage"})
})
public abstract class AISParser extends JavaLogging implements ParserInfo
{
    private final ThreadLocal<Integer> mmsiStore = new ThreadLocal<>();

    public AISParser()
    {
        super(AISParser.class);
    }
    
protected void payload(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void aisState(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void radius_12(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void text_84(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void description_6_930(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void radius_10(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void airdraught_11(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void status_2(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void weather_4(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void visibility_U1_7(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void pressuretend_4(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void airtemp_U1_11(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void airtemp_U1_10(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void visibility_U2_6(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void weather_9(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void airdraught_U1_13(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void description_6_966(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void airdraught_13(int arg, @ParserContext("aisData") AISObserver aisData){}
protected void text_936(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void text_968(InputReader arg, @ParserContext("aisData") AISObserver aisData){}
protected void duration_8(int arg, @ParserContext("aisData") AISObserver aisData){}

    @Terminal(expression="[CR]")
    protected void end(char end, @ParserContext("aisData") AISObserver aisData, @ParserContext("aisContext") AISContext aisContext)
    {
        if (end == 'C')
        {
            commit(aisContext, aisData, "Commit");
        }
        else
        {
            rollback(aisContext, aisData, "Rollback");
        }
    }
    public static AISParser newInstance() throws IOException
    {
        return (AISParser) GenClassFactory.getGenInstance(AISParser.class);
    }
    @ParseMethod(start = "messages", size=6, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;

    @ParseMethod(start = "1-3Messages", size=168, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse123Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "4Messages", size=168, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse4Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "5Messages", size=422, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse5Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "6Messages", size=1008, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse6Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "7Messages", size=168, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse7Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "8Messages", size=1008, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse8Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "9Messages", size=168, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse9Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "10Messages", size=72, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse10Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "11Messages", size=168, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse11Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "12Messages", size=1008, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse12Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "14Messages", size=1008, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse14Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "15Messages", size=160, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse15Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "16Messages", size=144, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse16Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "17Messages", size=816, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse17Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "18Messages", size=168, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse18Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "19Messages", size=312, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse19Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "20Messages", size=160, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse20Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "21Messages", size=360, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse21Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "22Messages", size=168, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse22Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "23Messages", size=160, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse23Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "24Messages", size=168, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse24Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;
    @ParseMethod(start = "27Messages", size=168, charSet = "US-ASCII", features={WideIndex})
    protected abstract void parse27Messages(
            AISChannel channel,
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext) throws ThreadStoppedException;

    @RecoverMethod
    public void recover(
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext,
            @ParserContext(ParserConstants.InputReader) InputReader reader,
            @ParserContext(ParserConstants.ExpectedDescription) String expected,
            @ParserContext(ParserConstants.LastToken) String got,
            @ParserContext(ParserConstants.Exception) Throwable thr
            ) throws IOException
    {
        if (thr != null && !(thr instanceof SyntaxErrorException))
        {
            this.log(Level.SEVERE, thr, "recover exp=%s", expected);
            throw new IOException(thr);
        }
        warning("recover exp=%s", expected);
        StringBuilder sb = new StringBuilder();
        String input = reader.getInput();
        sb.append(input);
        sb.append('^');
        int myKey = aisContext.getCurrentKey();
        if (myKey == 0)
        {
            for (int ii=input.length();ii<6;ii++)
            {
                int cc = reader.read();
                sb.append((char) cc);
            }
        }
        else
        {
            if (skip(input))
            {
                int cc = reader.read();
                while (cc == '0' || cc == '1')
                {
                    sb.append((char) cc);
                    cc = reader.read();
                    reader.clear();
                }
            }
        }
        rollback(aisContext, aisData, "skipping: "+sb+"\nexpected:"+expected);
        reader.clear();
    }
    private boolean skip(String input)
    {
        if (input.isEmpty())
        {
            return true;
        }
        char cc = input.charAt(input.length()-1);
        return cc == '0' || cc == '1';
    }

    private void commit(final AISContext aisContext, final AISObserver aisData, final String comment)
    {
        ContextAccess<Void,Void> ca = new ContextAccess<Void,Void>() 
        {
            @Override
            public Void access(Void context)
            {
                Integer currentKey = aisContext.getCurrentKey();
                aisData.setMessageType(MessageTypes.values()[currentKey]);
                aisData.commit(comment);
                return null;
            }
        };
        aisContext.accessContext(ca);
    }
    private void rollback(AISContext aisContext, final AISObserver aisData, final String comment)
    {
        ContextAccess<Void,Void> ca = new ContextAccess<Void,Void>() 
        {
            @Override
            public Void access(Void context)
            {
                aisData.rollback(comment);
                return null;
            }
        };
        aisContext.accessContext(ca);
    }
    protected void type(
            int messageType, 
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext("aisContext") AISContext aisContext
    )
    {
        aisContext.setMessageType(messageType);
    }

    protected void repeat(int repeatIndicator, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRepeatIndicator(repeatIndicator);
    }

    protected void mmsi(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMmsi(mmsi);
        mmsiStore.set(mmsi);
    }

    protected void aisVersion(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAisVersion(arg);
    }
    protected void status_4(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        navigationStatus(arg, aisData);
    }
    protected void navigationStatus(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNavigationStatus(NavigationStatus.values()[status]);
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
    protected void turn_I3(int turn, @ParserContext("aisData") AISObserver aisData)
    {
        switch (turn)
        {
            case 0:
                aisData.setRateOfTurn(0);
                break;
            case 127:
                aisData.setRateOfTurn(10);
                break;
            case -127:
                aisData.setRateOfTurn(-10);
                break;
            case -128:  // 0x80
                break;
            default:
                float f = turn;
                f = f / 4.733F;
                aisData.setRateOfTurn(Math.signum(f) * f * f);
                break;
        }
    }

    protected void accuracy(boolean accuracy, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setPositionAccuracy(accuracy);
    }

    protected void accuracy(int accuracy, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setPositionAccuracy(accuracy != 0);
    }

    protected void lon_I3_25(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg != 181000)
        {
            float lon = arg;
            lon = lon / 60000;
            if (lon <= 180 && lon >= -180)
            {
                aisData.setLongitude(lon);
            }
            else
            {
                aisData.setError("lon = "+lon);
            }
        }
    }
    protected void lon_I1_18(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg != 0x1a838)
        {
            lon_I4_18(arg, aisData);
        }
    }

    protected void lon_I3_16(int lon, @ParserContext("aisData") AISObserver aisData)
    {
        if (lon != 65536)
        {
            float f = lon;
            f = (f / 100) - 180;
            if (lon <= 180 && lon >= -180)
            {
                aisData.setLongitude(f);
            }
            else
            {
                aisData.setError("longitude I3 = " + lon);
            }
        }
    }

    protected void lon_I4_18(int lon, @ParserContext("aisData") AISObserver aisData)
    {
        if (lon != 181000)
        {
            if (lon <= 180 * 60 * 10 && lon >= -180 * 60 * 10)
            {
                float f = lon;
                aisData.setLongitude(f / 600F);
            }
            else
            {
                aisData.setError("longitude I4 = " + lon);
            }
        }
    }

    protected void lon_I4_28(int lon, @ParserContext("aisData") AISObserver aisData)
    {
        if (lon != 108600000)
        {
            if (lon <= 108000000 && lon >= -108000000)
            {
                float f = lon;
                aisData.setLongitude(f / 600000F);
            }
            else
            {
                aisData.setError("longitude I4 = " + lon);
            }
        }
    }

    protected void lat_I3_24(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg != 91000)
        {
            float lat = arg;
            lat = lat / 60000;
            if (lat <= 90 && lat >= -90)
            {
                aisData.setLatitude(lat);
            }
            else
            {
                aisData.setError("lat = "+lat);
            }
        }
    }
    protected void lat_I1_17(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg != 0xd548)
        {
            lat_I4_17(arg, aisData);
        }
    }

    protected void lat_I3_15(int lat, @ParserContext("aisData") AISObserver aisData)
    {
        if (lat != 32767)
        {
            float f = lat;
            f = f / 100 - 90;
            if (f <= 90 && lat >= -90)
            {
                aisData.setLatitude(f);
            }
            else
            {
                aisData.setError("latitude I3 = " + lat);
            }
        }
    }

    protected void lat_I4_17(int lat, @ParserContext("aisData") AISObserver aisData)
    {
        if (lat != 91000)
        {
            if (lat <= 90 * 60 * 10 && lat >= -90 * 60 * 10)
            {
                float f = lat;
                aisData.setLatitude(f / 600F);
            }
            else
            {
                aisData.setError("latitude I4 = " + lat);
            }
        }
    }

    protected void lat_I4_27(int lat, @ParserContext("aisData") AISObserver aisData)
    {
        if (lat != 54600000)
        {
            if (lat <= 90 * 60 * 10000 && lat >= -90 * 60 * 10000)
            {
                float f = lat;
                aisData.setLatitude(f / 600000F);
            }
            else
            {
                aisData.setError("latitude I4 = " + lat);
            }
        }
    }

    protected void heading_7(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 127)
        {
            aisData.setHeading(5*arg);
        }
    }
    protected void heading_9(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 360)
        {
            aisData.setHeading(arg);
        }
    }

    protected void second(int second, @ParserContext("aisData") AISObserver aisData)
    {
        if (second < 60)
        {
            aisData.setSecond(second);
        }
    }

    protected void maneuver(int maneuver, @ParserContext("aisData") AISObserver aisData)
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
    protected void raim(boolean raim, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRaim(raim);
    }
    protected void raim(int raim, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRaim(raim == 1);
    }
    /**
     * Bits 149-167 are diagnostic information for the radio system. 
     * Consult [IALA] for detailed description of the latter.
     * @param arg
     * @param aisData 
     */
    protected void radio_19(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRadioStatus(arg);
    }
    /**
     * The radio status is 20 bits rather than 19 because an extra first bit 
     * selects whether it should be interpreted as a SOTDMA or ITDMA state.
     * @param arg
     * @param aisData 
     */
    protected void radio_20(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRadioStatus(arg & 0b1111111111111111111);
    }

    protected void year(int year, @ParserContext("aisData") AISObserver aisData)
    {
        if (year != 0)
        {
            aisData.setYear(year);
        }
    }

    protected void month(int month, @ParserContext("aisData") AISObserver aisData)
    {
        if (month != 0)
        {
            aisData.setMonth(month);
        }
    }

    protected void day_5(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        day(arg, aisData);
    }
    protected void day_6(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        day(arg, aisData);
    }
    protected void day(int day, @ParserContext("aisData") AISObserver aisData)
    {
        if (day != 0)
        {
            aisData.setDay(day);
        }
    }

    protected void hour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setHour(hour);
        }
    }

    protected void minute_3(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMinute(arg*10);
    }

    protected void minute_6(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setMinute(minute);
        }
    }

    protected void epfd(int epfd, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setEpfd(EPFDFixTypes.values()[epfd]);
    }

    protected void version(int version, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVersion(version);
    }

    protected void imo(int imo, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setImoNumber(imo);
    }

    protected void callsign(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setCallSign(AISUtil.makeString(reader));
    }

    protected void shipname(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVesselName(AISUtil.makeString(reader));
    }

    protected void shiptype(int shiptype, @ParserContext("aisData") AISObserver aisData)
    {
        if (shiptype < CodesForShipType.values().length)
        {
            aisData.setShipType(CodesForShipType.values()[shiptype]);
        }
    }

    protected void toBow(int dimension, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDimensionToBow(dimension);
    }

    protected void toStern(int dimension, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDimensionToStern(dimension);
    }

    protected void toPort(int dimension, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDimensionToPort(dimension);
    }

    protected void toStarboard(int dimension, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDimensionToStarboard(dimension);
    }

    protected void draught_U1(int draught, @ParserContext("aisData") AISObserver aisData)
    {
        float f = draught;
        aisData.setDraught(f / 10F);
    }

    protected void destination_120(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDestination(AISUtil.makeString(reader));
    }
    protected void destination_30(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDestination(AISUtil.makeString(reader));
    }

    protected void dte(boolean dte, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDte(!dte);
    }

    protected void seqno(int seq, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSequenceNumber(seq);
    }

    protected void destMmsi(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDestinationMMSI(mmsi);
    }

    protected void retransmit(int retransmit, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRetransmit(retransmit != 1);
    }

    protected void retransmit(boolean retransmit, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRetransmit(!retransmit);
    }

    protected void dac(int dac, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDac(dac);
    }

    protected void fid(int fid, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setFid(fid);
    }

    protected void lastport(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLastPort(AISUtil.makeString(reader));
    }

    protected void lmonth(int month, @ParserContext("aisData") AISObserver aisData)
    {
        if (month != 0)
        {
            aisData.setLastPortMonth(month);
        }
    }

    protected void lday(int day, @ParserContext("aisData") AISObserver aisData)
    {
        if (day != 0)
        {
            aisData.setLastPortDay(day);
        }
    }

    protected void lhour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setLastPortHour(hour);
        }
    }

    protected void lminute(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setLastPortMinute(minute);
        }
    }

    protected void nextport(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNextPort(AISUtil.makeString(reader));
    }

    protected void nmonth(int month, @ParserContext("aisData") AISObserver aisData)
    {
        if (month != 0)
        {
            aisData.setNextPortMonth(month);
        }
    }

    protected void nday(int day, @ParserContext("aisData") AISObserver aisData)
    {
        if (day != 0)
        {
            aisData.setNextPortDay(day);
        }
    }

    protected void nhour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setNextPortHour(hour);
        }
    }

    protected void nminute(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setNextPortMinute(minute);
        }
    }

    protected void dangerous(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMainDangerousGood(AISUtil.makeString(reader));
    }

    protected void imdcat(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setImdCategory(AISUtil.makeString(reader));
    }

    protected void unid(int unid, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setUnNumber(unid);
    }

    protected void amount(int amount, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAmountOfCargo(amount);
    }

    protected void unit(int unit, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setUnitOfQuantity(CargoUnitCodes.values()[unit]);
    }

    protected void fromHour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setFromHour(hour);
        }
    }

    protected void fromMin(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setFromMinute(minute);
        }
    }

    protected void toHour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setToHour(hour);
        }
    }

    protected void toMin(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setToMinute(minute);
        }
    }

    protected void cdir(int currentDirection, @ParserContext("aisData") AISObserver aisData)
    {
        if (currentDirection < 360)
        {
            aisData.setSurfaceCurrentDirection(currentDirection);
        }
    }

    protected void cspeed_U1_7(int currentSpeed, @ParserContext("aisData") AISObserver aisData)
    {
        if (currentSpeed != 127)
        {
            float f = currentSpeed;
            aisData.setCurrentSpeed(f / 10F);
        }
    }

    protected void cspeed_U1_8(int currentSpeed, @ParserContext("aisData") AISObserver aisData)
    {
        if (currentSpeed < 255)
        {
            float f = currentSpeed;
            aisData.setSurfaceCurrentSpeed(f / 10F);
        }
    }

    protected void persons(int persons, @ParserContext("aisData") AISObserver aisData)
    {
        if (persons != 0)
        {
            aisData.setPersonsOnBoard(persons);
        }
    }

    protected void linkage(int id, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLinkage(id);
    }

    protected void portname(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setPortname(AISUtil.makeString(reader));
    }

    protected void notice(int notice, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAreaNotice(AreaNoticeDescription.values()[notice]);
    }

    protected void duration_18(int duration, @ParserContext("aisData") AISObserver aisData)
    {
        if (duration != 262143)
        {
            aisData.setDuration(duration);
        }
    }

    protected void shape(int shape, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setShape(SubareaType.values()[shape]);
    }

    protected void scale(int scale, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setScale(scale);
    }

    protected void precision(int precision, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setPrecision(precision);
    }

    protected void radius(int radius, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRadius(radius);
    }

    protected void east(int east, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setEast(east);
    }

    protected void north(int north, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNorth(north);
    }

    protected void orientation(int orientation, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setOrientation(orientation);
    }

    protected void left(int left, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLeft(left);
    }

    protected void right(int right, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRight(right);
    }

    protected void bearing(int bearing, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setBearing(bearing);
    }

    protected void distance(int distance, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDistance(distance);
    }

    protected void text(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setText(AISUtil.makeString(reader));
    }

    protected void berthLength(int meters, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setBerthLength(meters);
    }

    protected void berthDepth_U1(int meters, @ParserContext("aisData") AISObserver aisData)
    {
        float f = meters;
        aisData.setBerthDepth(f / 10F);
    }

    protected void position(int position, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMooringPosition(MooringPosition.values()[position]);
    }

    protected void availability(boolean available, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setServicesAvailability(available);
    }

    protected void agent(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAgentServiceStatus(ServiceStatus.values()[status]);
    }

    protected void fuel(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setFuelServiceStatus(ServiceStatus.values()[status]);
    }

    protected void chandler(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setChandlerServiceStatus(ServiceStatus.values()[status]);
    }

    protected void stevedore(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setStevedoreServiceStatus(ServiceStatus.values()[status]);
    }

    protected void electrical(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setElectricalServiceStatus(ServiceStatus.values()[status]);
    }

    protected void water(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setWaterServiceStatus(ServiceStatus.values()[status]);
    }

    protected void customs(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setCustomsServiceStatus(ServiceStatus.values()[status]);
    }

    protected void cartage(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setCartageServiceStatus(ServiceStatus.values()[status]);
    }

    protected void crane(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setCraneServiceStatus(ServiceStatus.values()[status]);
    }

    protected void lift(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLiftServiceStatus(ServiceStatus.values()[status]);
    }

    protected void medical(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMedicalServiceStatus(ServiceStatus.values()[status]);
    }

    protected void navrepair(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNavrepairServiceStatus(ServiceStatus.values()[status]);
    }

    protected void provisions(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setProvisionsServiceStatus(ServiceStatus.values()[status]);
    }

    protected void shiprepair(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setShiprepairServiceStatus(ServiceStatus.values()[status]);
    }

    protected void surveyor(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSurveyorServiceStatus(ServiceStatus.values()[status]);
    }

    protected void steam(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSteamServiceStatus(ServiceStatus.values()[status]);
    }

    protected void tugs(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setTugsServiceStatus(ServiceStatus.values()[status]);
    }

    protected void solidwaste(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSolidwasteServiceStatus(ServiceStatus.values()[status]);
    }

    protected void liquidwaste(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLiquidwasteServiceStatus(ServiceStatus.values()[status]);
    }

    protected void hazardouswaste(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setHazardouswasteServiceStatus(ServiceStatus.values()[status]);
    }

    protected void ballast(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setBallastServiceStatus(ServiceStatus.values()[status]);
    }

    protected void additional(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAdditionalServiceStatus(ServiceStatus.values()[status]);
    }

    protected void regional1(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRegional1ServiceStatus(ServiceStatus.values()[status]);
    }

    protected void regional2(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRegional2ServiceStatus(ServiceStatus.values()[status]);
    }

    protected void future1(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setFuture1ServiceStatus(ServiceStatus.values()[status]);
    }

    protected void future2(int status, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setFuture2ServiceStatus(ServiceStatus.values()[status]);
    }

    protected void berthName(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setBerthName(AISUtil.makeString(reader));
    }

    protected void berthLon_I3(int lon, @ParserContext("aisData") AISObserver aisData)
    {
        if (lon != 0x6791AC0)
        {
            float f = lon;
            aisData.setLongitude(f / 60000L);
        }
    }

    protected void berthLat_I3(int lat, @ParserContext("aisData") AISObserver aisData)
    {
        if (lat != 54600000)
        {
            float f = lat;
            aisData.setLatitude(f / 60000L);
        }
    }

    protected void sender(int sender, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSender(sender);
    }

    protected void rtype(int type, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRouteType(RouteTypeCodes.values()[type]);
    }

    protected void waycount(int count, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setWaypointCount(count);
    }

    protected void description(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDescription(AISUtil.makeString(reader));
    }

    protected void mmsi1(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMmsi1(mmsi);
    }

    protected void mmsi2(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMmsi2(mmsi);
    }

    protected void mmsi3(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMmsi3(mmsi);
    }

    protected void mmsi4(int mmsi, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMmsi4(mmsi);
    }

    protected void wspeed(int knots, @ParserContext("aisData") AISObserver aisData)
    {
        if (knots != 127)
        {
            aisData.setAverageWindSpeed(knots);
        }
    }

    protected void wgust(int knots, @ParserContext("aisData") AISObserver aisData)
    {
        if (knots != 127)
        {
            aisData.setGustSpeed(knots);
        }
    }

    protected void wdir(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees <= 360)
        {
            aisData.setWindDirection(degrees);
        }
    }

    protected void wgustdir(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees <= 360)
        {
            aisData.setWindGustDirection(degrees);
        }
    }

    protected void temperature(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees <= 1200)
        {
            float f = degrees;
            aisData.setAirTemperature((f / 10F) - 60F);
        }
    }

    protected void humidity(int humidity, @ParserContext("aisData") AISObserver aisData)
    {
        if (humidity < 127)
        {
            aisData.setRelativeHumidity(humidity);
        }
    }

    protected void dewpoint(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees < 1023)
        {
            float f = degrees;
            aisData.setDewPoint((f / 10F) - 20F);
        }
    }

    protected void pressure_9(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 403)
        {
            aisData.setAirPressure(arg + 800);
        }
    }
    protected void pressure_U1_11(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        float f = arg;
        aisData.setAirPressure((f / 10F) + 900F);
    }
    protected void pressure_U1_16(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 60002)
        {
            float f = arg;
            aisData.setWaterPressure(f / 10F);
        }
    }

    protected void pressuretend_2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAirPressureTendency(Tendency.values()[arg]);
    }
    /**
     * 
     * @param visibility
     * @param aisData 
     */
    protected void visibility_U1_8(int visibility, @ParserContext("aisData") AISObserver aisData)
    {
        if (visibility < 250)
        {
            float f = visibility;
            aisData.setVisibility(f / 10F);
        }
    }

    protected void waterlevel_U1_9(int level, @ParserContext("aisData") AISObserver aisData)
    {
        if (level < 511)
        {
            float f = level;
            aisData.setWaterLevel((f / 10F) - 10F);
        }
    }
    protected void waterlevel_U2_12(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 40001)
        {
            float f = arg;
            aisData.setWaterLevel((f / 100F) - 10F);
        }
    }

    protected void leveltrend(int trend, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setWaterLevelTrend(Tendency.values()[trend]);
    }

    protected void cspeed2_U1(int speed, @ParserContext("aisData") AISObserver aisData)
    {
        if (speed < 255)
        {
            float f = speed;
            aisData.setCurrentSpeed2(f / 10F);
        }
    }

    protected void cdir2(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees <= 360)
        {
            aisData.setCurrentDirection2(degrees);
        }
    }

    protected void cdepth2_5(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 31)
        {
            aisData.setMeasurementDepth2(arg);
        }
    }
    protected void cdepth3_5(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 31)
        {
            aisData.setMeasurementDepth3(arg);
        }
    }
    protected void cdepth2_9(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 362)
        {
            aisData.setMeasurementDepth2(arg);
        }
    }
    protected void cdepth3_9(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 362)
        {
            aisData.setMeasurementDepth3(arg);
        }
    }
    protected void cspeed3_U1(int speed, @ParserContext("aisData") AISObserver aisData)
    {
        if (speed < 255)
        {
            float f = speed;
            aisData.setCurrentSpeed3(f / 10F);
        }
    }

    protected void cdir3(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees <= 360)
        {
            aisData.setCurrentDirection3(degrees);
        }
    }

    protected void waveheight_U1(int height, @ParserContext("aisData") AISObserver aisData)
    {
        if (height < 251)
        {
            float f = height;
            aisData.setWaveHeight(f / 10F);
        }
    }

    protected void waveperiod(int seconds, @ParserContext("aisData") AISObserver aisData)
    {
        if (seconds < 61)
        {
            aisData.setWavePeriod(seconds);
        }
    }

    protected void wavedir(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees < 360)
        {
            aisData.setWaveDirection(degrees);
        }
    }

    protected void swellheight_U1(int height, @ParserContext("aisData") AISObserver aisData)
    {
        if (height < 251)
        {
            float f = height;
            aisData.setSwellHeight(f / 10F);
        }
    }

    protected void swellperiod(int seconds, @ParserContext("aisData") AISObserver aisData)
    {
        if (seconds < 61)
        {
            aisData.setSwellPeriod(seconds);
        }
    }

    protected void swelldir(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees < 360)
        {
            aisData.setSwellDirection(degrees);
        }
    }

    protected void seastate(int state, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSeaState(BeaufortScale.values()[state]);
    }

    protected void watertemp_U1(int temp, @ParserContext("aisData") AISObserver aisData)
    {
        if (temp < 601)
        {
            float f = temp;
            aisData.setWaterTemperature((f / 10F) - 10F);
        }
    }
    /**
     * From Weather report payload
     * 0 = rain,
     * 1 = rain and snow,
     * 2 = rain and snow,
     * 3 = other.
     * 
     * <p> This doesn't make any sense!!!! 
     * @param type
     * @param aisData 
     */
    protected void preciptype_2(int type, @ParserContext("aisData") AISObserver aisData)
    {
        switch (type)
        {
            case 0:
                aisData.setPrecipitation(PrecipitationTypes.Rain);
                break;
            case 1:
                aisData.setPrecipitation(PrecipitationTypes.Snow);
                break;
            case 2:
                aisData.setPrecipitation(PrecipitationTypes.Snow);
                break;
            case 3:
                aisData.setPrecipitation(PrecipitationTypes.NADefault);
                break;
        }
    }

    protected void preciptype_3(int type, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setPrecipitation(PrecipitationTypes.values()[type]);
    }

    protected void salinity_U1(int salinity, @ParserContext("aisData") AISObserver aisData)
    {
        if (salinity < 500)
        {
            float f = salinity;
            aisData.setSalinity(f / 10F);
        }
    }

    protected void ice(int ice, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIce(ice);
    }

    protected void reason(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setReasonForClosing(AISUtil.makeString(reader));
    }

    protected void closefrom(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setClosingFrom(AISUtil.makeString(reader));
    }

    protected void closeto(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setClosingTo(AISUtil.makeString(reader));
    }

    protected void extunit(int unit, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setUnitOfExtension(ExtensionUnit.values()[unit]);
    }

    protected void fmonth(int month, @ParserContext("aisData") AISObserver aisData)
    {
        if (month != 0)
        {
            aisData.setFromMonth(month);
        }
    }

    protected void fday(int day, @ParserContext("aisData") AISObserver aisData)
    {
        if (day != 0)
        {
            aisData.setFromDay(day);
        }
    }

    protected void fhour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setFromHour(hour);
        }
    }

    protected void fminute(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setFromMinute(minute);
        }
    }

    protected void tmonth(int month, @ParserContext("aisData") AISObserver aisData)
    {
        if (month != 0)
        {
            aisData.setToMonth(month);
        }
    }

    protected void tday(int day, @ParserContext("aisData") AISObserver aisData)
    {
        if (day != 0)
        {
            aisData.setToDay(day);
        }
    }

    protected void thour(int hour, @ParserContext("aisData") AISObserver aisData)
    {
        if (hour != 24)
        {
            aisData.setToHour(hour);
        }
    }

    protected void tminute(int minute, @ParserContext("aisData") AISObserver aisData)
    {
        if (minute != 60)
        {
            aisData.setToMinute(minute);
        }
    }

    protected void airdraught(int meters, @ParserContext("aisData") AISObserver aisData)
    {
        if (meters != 0)
        {
            aisData.setAirDraught(meters);
        }
    }

    protected void idtype(int type, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIdType(TargetIdentifierType.values()[type]);
    }

    protected void id(long id, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setId(id);
    }
    /**
     * 0.1 degrees from true north
     * @param arg
     * @param aisData 
     */
    protected void course_U1_12(int course, @ParserContext("aisData") AISObserver aisData)
    {
        if (course < 3600)
        {
            if (course >= 0 && course < 3600)
            {
                float f = course;
                aisData.setCourse(f / 10F);
            }
            else
            {
                aisData.setError("course U1 = " + course);
            }
        }
    }
    /**
     * 
     * @param arg
     * @param aisData 
     */
    protected void course_7(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 359)
        {
            aisData.setCourse(arg);
        }
    }
    protected void course_9(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 359)
        {
            aisData.setCourse(arg);
        }
    }


    /**
     * 0-14.5m/s: SOG = (value * 0.5) for 0-29, 30 = 15 m/s and more, average 
     * over last 10 minutes. 31 = N/A (default)
     * @param arg
     * @param aisData 
     */
    protected void speed_U1_5(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSpeed((float)arg/20F);
    }

    /**
     * Knots (0-62); 63 = N/A (default)
     * @param arg
     * @param aisData 
     */
    protected void speed_6(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 63)
        {
            aisData.setSpeed(arg);
        }
    }

    /**
     * Speed over ground is in knots, not deciknots as in the common navigation 
     * block; planes go faster. The special value 1023 indicates speed not 
     * available, 1022 indicates 1022 knots or higher.
     * @param arg
     * @param aisData 
     */
    protected void speed_10(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 1023)
        {
            aisData.setSpeed(arg);
        }
    }
    /**
     * Speed over ground is in 0.1-knot resolution from 0 to 102 knots. 
     * value 1023 indicates speed is not available, value 1022 indicates 102.2 
     * knots or higher.
     */
    protected void speed_U1_10(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 1023)
        {
            aisData.setSpeed((float)arg/10F);
        }
    }

    protected void station(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setStation(AISUtil.makeString(reader));
    }

    protected void signal(int signal, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSignal(MarineTrafficSignals.values()[signal]);
    }

    protected void nextsignal(int signal, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNextSignal(MarineTrafficSignals.values()[signal]);
    }

    protected void wmo(int variant, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVariant(variant);
    }

    protected void location(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setLocation(AISUtil.makeString(reader));
    }

    protected void weather(int code, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setWeather(WMOCode45501.values()[code]);
    }

    protected void vislimit(boolean reached, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVisibilityLimit(!reached);
    }

    protected void airtemp_U1(int degrees, @ParserContext("aisData") AISObserver aisData)
    {
        if (degrees <= 1200)
        {
            float f = degrees;
            aisData.setAirTemperature((f / 10F) - 60F);
        }
    }

    protected void pdelta_U1(int delta, @ParserContext("aisData") AISObserver aisData)
    {
        float f = delta;
        aisData.setAirPressureChange((f / 10F) - 50F);
    }

    protected void ptend(int tend, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAirPressureTendency(Tendency.values()[tend]);
    }

    protected void twinddir(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void twindspeed(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void rwinddir(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void rwindspeed_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void mgustspeed_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void mgustdir(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void surftemp_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void visibility_U2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void pweather1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void pweather2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void totalcloud(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void lowclouda(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void lowcloudt(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void midcloudt(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void highcloudt(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void cloudbase_U2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void wwperiod(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void wwheight(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void swelldir1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void swperiod1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void swheight1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void swelldir2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void swperiod2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void swheight2_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void icedeposit(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void icerate(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void icecause(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void seaice(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void icetype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void icestate(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void icedevel(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void icebearing(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void sensor(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void site(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void payload(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
    protected void alt_11(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 2002)
        {
            float sensorAltitude = arg;
            aisData.setSensorAltitude(sensorAltitude/10);
        }
    }
    protected void alt_12(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg < 4095)
        {
            aisData.setAltitude(arg);
        }
    }

    protected void owner(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void timeout(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
    protected void name_84(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
        name(arg, aisData);
    }
    protected void name_120(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
        name(arg, aisData);
    }
    protected void name(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setName(AISUtil.makeString(reader));
    }
    protected void nameExt(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNameExtension(AISUtil.makeString(reader));
    }
    protected void sensortype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void fwspeed(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void fwgust(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void fwdir(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// CurrentFlow2DReportPayload
    protected void cspeed1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void cdir1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void cdepth1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

// CurrentFlow3DPayload
    protected void cnorth1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void ceast1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void cup1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void cnorth2_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void ceast2_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void cup2_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// HorizontalCurrentReportPayload
    protected void bearing1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void distance1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void speed1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void direction1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void depth1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// SeaStateReportPayload
    protected void swheight_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void swperiod(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void swelltype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void distance1_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void depthtype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void wavetype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// SalinityReportPayload
    protected void conductivity_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void salinitytype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// WeatherReportPayload
    protected void dewtype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void pressuretype(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// AirGapAirDraftReportPayload
    protected void airdraught_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void airgap_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void gaptrend(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void fairgap_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// IMO289RouteInformationBroadcast
// IMO289TextDescriptionBroadcast
// IMO289ExtendedShipStaticAndVoyageRelatedData
    protected void secondport(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void state(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void ataState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void bnwasState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void ecdisbState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void chartState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void sounderState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void epaidState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void steerState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void gnssState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void gyroState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void lritState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void magcompState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void navtexState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void arpaState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void sbandState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void xbandState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void hfradioState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void inmarsatState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void mfradioState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void vhfradioState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void grndlogState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void waterlogState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void thdState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void tcsState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void vdrState(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void iceclass(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void horsepower(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void vhfchan(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void lshiptype(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void tonnage(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void lading(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void heavyoil(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void lightoil(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void dieseloil(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void totaloil(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// MeteorologicalAndHydrologicalDataIMO289
    protected void dewpoint_U1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void visgreater(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void precipitation(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
    protected void regional_2(int arg, @ParserContext("aisData") AISObserver aisData){}
    protected void regional_4(int arg, @ParserContext("aisData") AISObserver aisData){}
    protected void regional_8(int arg, @ParserContext("aisData") AISObserver aisData){}
    protected void regional(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void assigned(boolean assigned, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAssignedMode(assigned);
    }
    protected void assigned(int assigned, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAssignedMode(assigned != 0);
    }
// Type10UTCDateInquiry
// Type12AddressedSafetyRelatedMessage
// Type14SafetyRelatedBroadcastMessage
// Type15Interrogation
    protected void type11(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void offset11(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void type12(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void offset12(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void type21(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void offset21(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type16AssignmentModeCommand
    protected void offset1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setOffset1(arg);
    }

    protected void increment1_10(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIncrement1(arg);
    }

    protected void offset2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setOffset2(arg);
    }

    protected void increment2_10(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIncrement2(arg);
    }
    /**
     * DGNSS correction data
     * @param input
     * @param aisData 
     */
    protected void data(InputReader input, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type18StandardClassBCSPositionReport
    protected void reserved(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void cs(boolean cs, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setCsUnit(cs);
    }

    protected void display(boolean arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDisplay(arg);
    }

    protected void dsc(boolean arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setDsc(arg);
    }

    protected void band(boolean arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setBand(arg);
    }

    protected void msg22(boolean arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMsg22(arg);
    }
// Type20DataLinkManagementMessage
    protected void number1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setReservedSlots1(arg);
    }

    protected void timeout1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setTimeout1(arg);
    }

    protected void number2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setReservedSlots2(arg);
    }

    protected void timeout2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setTimeout2(arg);
    }

    protected void offset3(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setOffset3(arg);
    }

    protected void number3(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setReservedSlots3(arg);
    }

    protected void timeout3(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setTimeout3(arg);
    }

    protected void increment1_11(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIncrement1(arg);
    }
    protected void increment2_11(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIncrement2(arg);
    }
    protected void increment3(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIncrement3(arg);
    }

    protected void offset4(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setOffset4(arg);
    }

    protected void number4(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setReservedSlots4(arg);
    }

    protected void timeout4(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setTimeout4(arg);
    }

    protected void increment4(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setIncrement4(arg);
    }
// Type21AidToNavigationReport
    protected void aidType(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setNavaidType(NavaidTypes.values()[arg]);
    }

    protected void offPosition(boolean arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setOffPosition(arg);
    }

    protected void virtualAid(boolean arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVirtualAid(arg);
    }
    protected void virtualAid(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVirtualAid(arg == 1);
    }
// Type22ChannelManagement
    protected void channelA(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setChannelA(arg);
    }

    protected void channelB(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setChannelB(arg);
    }
    /**
     * The txrx field encodes the same information as the 2-bit field txrx field 
     * in message type 23; only the two low bits are used.
     */
    protected void txrx_4(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (arg > 3)
        {
            warning("txrx_4(%d)", arg);
        }
        aisData.setTransceiverMode(TransceiverModes.values()[arg & 0b11]);
    }
    protected void txrx_2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setTransceiverMode(TransceiverModes.values()[arg]);
    }

    protected void power(boolean arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setPower(arg);
    }

    protected void neLon_I1(int lon, @ParserContext("aisData") AISObserver aisData)
    {
        if (lon != 0x1a838)
        {
            if (lon <= 180 * 60 * 10 && lon >= -180 * 60 * 10)
            {
                float f = lon;
                aisData.setNeLongitude(f / 600F);
            }
            else
            {
                aisData.setError(" nelongitude I4 = " + lon);
            }
        }
    }

    protected void neLat_I1(int lat, @ParserContext("aisData") AISObserver aisData)
    {
        if (lat != 0xd548)
        {
            if (lat <= 90 * 60 * 10 && lat >= -90 * 60 * 10)
            {
                float f = lat;
                aisData.setNeLatitude(f / 600F);
            }
            else
            {
                aisData.setError("latitude I4 = " + lat);
            }
        }
    }

    protected void swLon_I1(int lon, @ParserContext("aisData") AISObserver aisData)
    {
        if (lon != 0x1a838)
        {
            if (lon <= 180 * 60 * 10 && lon >= -180 * 60 * 10)
            {
                float f = lon;
                aisData.setSwLongitude(f / 600F);
            }
            else
            {
                aisData.setError("sw longitude I4 = " + lon);
            }
        }
    }

    protected void swLat_I1(int lat, @ParserContext("aisData") AISObserver aisData)
    {
        if (lat != 0xd548)
        {
            if (lat <= 90 * 60 * 10 && lat >= -90 * 60 * 10)
            {
                float f = lat;
                aisData.setSwLatitude(f / 600F);
            }
            else
            {
                aisData.setError("latitude I4 = " + lat);
            }
        }
    }

    protected void dest1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMmsi1(arg);
    }

    protected void dest2(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMmsi2(arg);
    }
    protected void boxDest(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void addressed(
            InputReader input, 
            @ParserContext("aisData") AISObserver aisData
    )
    {
        int arg = input.parseIntRadix2();
        boolean addressed = arg != 0;
        int offset = input.getStart()-70;
        if (addressed)
        {
            aisData.setMmsi1(input.parseInt(offset, 30, 2));
            offset+=30;
            aisData.setMmsi2(input.parseInt(offset, 30, 2));
        }
        else
        {
            neLon_I1(input.parseInt(offset, 18, -2), aisData);
            offset+=18;
            neLat_I1(input.parseInt(offset, 17, -2), aisData);
            offset+=17;
            swLon_I1(input.parseInt(offset, 18, -2), aisData);
            offset+=18;
            swLat_I1(input.parseInt(offset, 17, -2), aisData);
        }
        aisData.setAddressed(addressed);
    }

    protected void bandA(boolean arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setChannelABand(arg);
    }

    protected void bandB(boolean arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setChannelBBand(arg);
    }

    protected void zonesize(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setZoneSize(arg);
    }
// Type23GroupAssignmentCommand
    protected void neLon(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void neLat(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void swLon(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void swLat(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void stationType(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void shipType(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void interval(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void quiet(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// Type24StaticDataReport
    protected void partno(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setPartNumber(arg);
    }

    protected void vendorid(InputReader reader, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setVendorId(AISUtil.makeString(reader));
    }

    protected void model(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setUnitModelCode(arg);
    }

    protected void serial(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setSerialNumber(arg);
    }

    protected void mothershipMmsi(int arg, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMotherShipMMSI(arg);
    }
    protected void mothershipDim(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
        if (MMSIType.getType(mmsiStore.get()) == CraftAssociatedWithParentShip)
        {
            aisData.setMotherShipMMSI(arg.parseIntRadix2());
        }
        else
        {
            int off = arg.getStart();
            aisData.setDimensionToBow(arg.parseInt(off, 9, 2));
            off += 9;
            aisData.setDimensionToStern(arg.parseInt(off, 9, 2));
            off += 9;
            aisData.setDimensionToPort(arg.parseInt(off, 6, 2));
            off += 6;
            aisData.setDimensionToStarboard(arg.parseInt(off, 6, 2));
        }
    }

// Type25SingleSlotBinaryMessage
    protected void structured(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void gnss(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

}
