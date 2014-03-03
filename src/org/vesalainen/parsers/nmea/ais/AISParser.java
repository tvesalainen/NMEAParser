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
@Terminal(left="type1-3", expression="000001|000010|000011", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="repeat", expression="[01]{2}", doc="Repeat Indicator", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRepeat(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="mmsi", expression="[01]{30}", doc="MMSI", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMmsi(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="status", expression="[01]{4}", doc="Navigation Status", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisStatus(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="turn", expression="[01]{8}", doc="Rate of Turn (ROT)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTurn_I3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="speed", expression="[01]{10}", doc="Speed Over Ground (SOG)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSpeed_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="accuracy", expression="[01]{1}", doc="Position Accuracy", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAccuracy(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="lon", expression="[01]{28}", doc="Longitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLon_I4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="lat", expression="[01]{27}", doc="Latitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLat_I4(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="course", expression="[01]{12}", doc="Course Over Ground (COG)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCourse_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="heading", expression="[01]{9}", doc="True Heading (HDG)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHeading(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="second", expression="[01]{6}", doc="Time Stamp", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSecond(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="maneuver", expression="[01]{2}", doc="Maneuver Indicator", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisManeuver(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="raim", expression="[01]{1}", doc="RAIM flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRaim(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="radio", expression="[01]{19}", doc="Radio status", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRadio(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="type4", expression="000100", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="year", expression="[01]{14}", doc="Year (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisYear(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="month", expression="[01]{4}", doc="Month (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMonth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="day", expression="[01]{5}", doc="Day (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDay(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="hour", expression="[01]{5}", doc="Hour (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisHour(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="minute", expression="[01]{6}", doc="Minute (UTC)", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMinute(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="epfd", expression="[01]{4}", doc="Type of EPFD", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisEpfd(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="type5", expression="000101", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ais_version", expression="[01]{2}", doc="AIS Version", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAisVersion(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="imo", expression="[01]{30}", doc="IMO Number", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisImo(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="callsign", expression="[01]{42}", doc="Call Sign", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCallsign(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shipname", expression="[01]{120}", doc="Vessel Name", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShipname(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shiptype", expression="[01]{8}", doc="Ship Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShiptype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_bow", expression="[01]{9}", doc="Dimension to Bow", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisToBow(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_stern", expression="[01]{9}", doc="Dimension to Stern", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisToStern(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_port", expression="[01]{6}", doc="Dimension to Port", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisToPort(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="to_starboard", expression="[01]{6}", doc="Dimension to Starboard", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisToStarboard(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="draught", expression="[01]{8}", doc="Draught", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDraught_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="destination", expression="[01]{120}", doc="Destination", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDestination(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dte", expression="[01]{1}", doc="DTE", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDte(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape0", expression="000", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="scale", expression="[01]{2}", doc="Scale factor", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisScale(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="precision", expression="[01]{3}", doc="Precision", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPrecision(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="radius", expression="[01]{12}", doc="Radius", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRadius(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape1", expression="001", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="east", expression="[01]{8}", doc="E dimension", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisEast(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="north", expression="[01]{8}", doc="N dimension", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisNorth(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="orientation", expression="[01]{9}", doc="Orientation", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOrientation(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape2", expression="010", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="left", expression="[01]{9}", doc="Left boundary", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisLeft(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="right", expression="[01]{9}", doc="Right boundary", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRight(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape3", expression="011", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="bearing", expression="[01]{10}", doc="Bearing", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBearing(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="distance", expression="[01]{10}", doc="Distance", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDistance(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape4", expression="100", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="shape5", expression="101", doc="Shape of area", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisShape(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="text", expression="[01]{84}", doc="Text", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisText(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="alt", expression="[01]{11}", doc="Altitude", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAlt(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="owner", expression="[01]{4}", doc="Sensor owner", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisOwner(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="timeout", expression="[01]{3}", doc="Data timeout", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTimeout(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="name", expression="[01]{84}", doc="Name", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisName(org.vesalainen.parser.util.InputReader,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wspeed", expression="[01]{7}", doc="Average Wind Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWspeed(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wgust", expression="[01]{7}", doc="Wind Gust", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWgust(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wdir", expression="[01]{9}", doc="Wind Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wgustdir", expression="[01]{9}", doc="Wind Gust Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWgustdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="sensortype", expression="[01]{3}", doc="Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSensortype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fwspeed", expression="[01]{7}", doc="Forecast Wind Speed", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFwspeed(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fwgust", expression="[01]{7}", doc="Forecast Wind Gust", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFwgust(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fwdir", expression="[01]{9}", doc="Forecast Wind Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFwdir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="duration", expression="[01]{8}", doc="Duration", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDuration(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed1", expression="[01]{8}", doc="Current Speed #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCspeed1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdir1", expression="[01]{9}", doc="Current Direction #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdir1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth1", expression="[01]{9}", doc="Measurement Depth #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdepth1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed2", expression="[01]{8}", doc="Current Speed #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCspeed2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdir2", expression="[01]{9}", doc="Current Direction #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdir2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth2", expression="[01]{9}", doc="Measurement Depth #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdepth2(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cspeed3", expression="[01]{8}", doc="Current Speed #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCspeed3_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdir3", expression="[01]{9}", doc="Current Direction #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdir3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cdepth3", expression="[01]{9}", doc="Measurement Depth #3", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCdepth3(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cnorth1", expression="[01]{8}", doc="Current Vector component North (u) #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCnorth1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ceast1", expression="[01]{8}", doc="Current Vector component East (v) #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCeast1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cup1", expression="[01]{8}", doc="Current Vector component Up (z) #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCup1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cnorth2", expression="[01]{8}", doc="Current Vector component North (u) #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCnorth2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="ceast2", expression="[01]{8}", doc="Current Vector component East (v) #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCeast2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cup2", expression="[01]{8}", doc="Current Vector component Up (z) #2", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCup2_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="bearing1", expression="[01]{9}", doc="Current Bearing #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBearing1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="distance1", expression="[01]{7}", doc="Current Distance #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDistance1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="speed1", expression="[01]{8}", doc="Current Speed #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSpeed1_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="direction1", expression="[01]{9}", doc="Current Direction #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDirection1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="depth1", expression="[01]{9}", doc="Measurement Depth #1", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDepth1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swheight", expression="[01]{8}", doc="Swell Height", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwheight_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swperiod", expression="[01]{6}", doc="Swell Period", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwperiod(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swelldir", expression="[01]{9}", doc="Swell Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwelldir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="seastate", expression="[01]{4}", doc="Sea State", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSeastate(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="swelltype", expression="[01]{3}", doc="Swell Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSwelltype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="watertemp", expression="[01]{10}", doc="Water Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWatertemp_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="depthtype", expression="[01]{3}", doc="Depth Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDepthtype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waveheight", expression="[01]{8}", doc="Wave Height", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWaveheight_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="waveperiod", expression="[01]{6}", doc="Wave Period", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWaveperiod(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wavedir", expression="[01]{9}", doc="Wave Direction", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWavedir(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="wavetype", expression="[01]{3}", doc="Wave Sensor Description", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisWavetype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="salinity", expression="[01]{9}", doc="Salinity", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSalinity_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="conductivity", expression="[01]{10}", doc="Conductivity", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisConductivity_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressure", expression="[01]{16}", doc="Water Pressure", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPressure_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="salinitytype", expression="[01]{2}", doc="Salinity Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisSalinitytype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="temperature", expression="[01]{11}", doc="Air Temperature", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisTemperature(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="preciptype", expression="[01]{2}", doc="Precipitation Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPreciptype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="visibility", expression="[01]{8}", doc="Horiz. Visibility", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisVisibility_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dewpoint", expression="[01]{10}", doc="Dew Point", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDewpoint(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=-2)
,@Terminal(left="dewtype", expression="[01]{3}", doc="Dewpoint Sensor Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDewtype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressuretend", expression="[01]{2}", doc="Pressure Tendency", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPressuretend(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="pressuretype", expression="[01]{3}", doc="Pressure Sensor Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisPressuretype(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airdraught", expression="[01]{13}", doc="Air Draught", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAirdraught_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="airgap", expression="[01]{13}", doc="Air Gap", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAirgap_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="gaptrend", expression="[01]{2}", doc="Air Gap Trend", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisGaptrend(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="fairgap", expression="[01]{13}", doc="Forecast Air Gap", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisFairgap_U1(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="type18", expression="010010", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="reserved", expression="[01]{8}", doc="Regional Reserved", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisReserved(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="regional", expression="[01]{2}", doc="Regional reserved", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisRegional(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="cs", expression="[01]{1}", doc="CS Unit", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisCs(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="display", expression="[01]{1}", doc="Display flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDisplay(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="dsc", expression="[01]{1}", doc="DSC Flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisDsc(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="band", expression="[01]{1}", doc="Band flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisBand(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="msg22", expression="[01]{1}", doc="Message 22 flag", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisMsg22(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="assigned", expression="[01]{1}", doc="Assigned", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisAssigned(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
,@Terminal(left="type19", expression="010011", doc="Message Type", reducer="org.vesalainen.parsers.nmea.ais.AISParser aisType(int,org.vesalainen.parsers.nmea.ais.AISObserver)", radix=2)
})
@Rules({
@Rule(left="Accept", value={"messages"})
,@Rule(left="messages", value={"(message '0*\n')+"})
,@Rule(left="message", value={"Type19ExtendedClassBCSPositionReport"})
,@Rule(left="message", value={"Type18StandardClassBCSPositionReport"})
,@Rule(left="message", value={"Type5StaticAndVoyageRelatedData"})
,@Rule(left="message", value={"Type4BaseStationReport"})
,@Rule(left="message", value={"CommonNavigationBlock"})
,@Rule(left="Type19ExtendedClassBCSPositionReport", value={"type19", "repeat", "mmsi", "reserved", "speed", "accuracy", "lon", "lat", "course", "heading", "second", "regional", "shipname", "shiptype", "to_bow", "to_stern", "to_port", "to_starboard", "epfd", "raim", "dte", "assigned", "'[01]{4}'"})
,@Rule(left="Type18StandardClassBCSPositionReport", value={"type18", "repeat", "mmsi", "reserved", "speed", "accuracy", "lon", "lat", "course", "heading", "second", "regional", "cs", "display", "dsc", "band", "msg22", "assigned", "raim", "radio"})
,@Rule(left="Type5StaticAndVoyageRelatedData", value={"type5", "repeat", "mmsi", "ais_version", "imo", "callsign", "shipname", "shiptype", "to_bow", "to_stern", "to_port", "to_starboard", "epfd", "month", "day", "hour", "minute", "draught", "destination", "dte", "'[01]{1}'"})
,@Rule(left="Type4BaseStationReport", value={"type4", "repeat", "mmsi", "year", "month", "day", "hour", "minute", "second", "accuracy", "lon", "lat", "epfd", "'[01]{10}'", "raim", "radio"})
,@Rule(left="CommonNavigationBlock", value={"type1-3", "repeat", "mmsi", "status", "turn", "speed", "accuracy", "lon", "lat", "course", "heading", "second", "maneuver", "'[01]{3}'", "raim", "radio"})
})
public abstract class AISParser
{
    /**
     * Parses AIS messages encoded in NMEA message <p> Example input:
     * 53nFBv01SJ...
     *
     * @param is
     * @param aisData
     */
    public void parse(InputStream is, AISObserver aisData)
    {
        AISInputStream aisInputStream = new AISInputStream(is);
        doParse(aisInputStream, aisData);
    }

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
        doParse(is, aisData);
    }

    public static AISParser newInstance() throws IOException
    {
        return (AISParser) GenClassFactory.getGenInstance(AISParser.class);
    }

    @ParseMethod(start = "messages", size = 1024, wideIndex = true)
    protected abstract void doParse(
            InputStream is,
            @ParserContext("aisData") AISObserver aisData);

    @RecoverMethod
    public void recover(
            @ParserContext("aisData") AISObserver aisData,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader,
            @ParserContext(ParserConstants.THROWABLE) Throwable thr
            ) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        int cc = reader.read();
        while (cc != '\n' && cc != -1)
        {
            sb.append((char) cc);
            cc = reader.read();
            reader.clear();
        }
        aisData.rollback("skipping " + sb+" "+thr);
    }

    protected void aisType(int messageType, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setMessageType(MessageTypes.values()[messageType]);
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

    protected void aisRaim(int raim, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setRAIM(raim == 1);
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

    protected void aisPressure(int pressure, @ParserContext("aisData") AISObserver aisData)
    {
        aisData.setAirPressure(pressure + 400);
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

    protected void aisPressure_U1(int pressure, @ParserContext("aisData") AISObserver aisData)
    {
        float f = pressure;
        aisData.setAirPressure((f / 10F) - 900F);   // ???? 90-1100 hPa: P = (value/10)+900 for 0-2000
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
// SiteLocationPayload
    protected void aisAlt(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisOwner(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisTimeout(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// StationIDPayload
    protected void aisName(InputReader arg, @ParserContext("aisData") AISObserver aisData)
    {
    }
// WindReportPayload
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
// Type9StandardSARAircraftPositionReport
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
// Type17DGNSSBroadcastBinaryMessage
    protected void aisLon_I1(int arg, @ParserContext("aisData") AISObserver aisData)
    {
    }

    protected void aisLat_I1(int arg, @ParserContext("aisData") AISObserver aisData)
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
