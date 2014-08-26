
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
package org.vesalainen.parsers.nmea;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Checksum;
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
import org.vesalainen.parser.util.ChecksumProvider;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.nmea.ais.AISContext;
import org.vesalainen.parsers.nmea.ais.AISObserver;
import org.vesalainen.parsers.nmea.ais.AbstractAISObserver;
import org.vesalainen.util.navi.Fathom;
import org.vesalainen.util.navi.Feet;
import org.vesalainen.util.navi.KilometersInHour;
import org.vesalainen.util.navi.Knots;
import org.vesalainen.util.navi.Velocity;

/**
 * @author Timo Vesalainen
 * @see <a href="http://catb.org/gpsd/NMEA.html">NMEA Revealed</a>
 * @see <a href="http://catb.org/gpsd/AIVDM.html">AIVDM/AIVDO protocol decoding</a>
 * @see <a href="http://www.eye4software.com/hydromagic/documentation/nmea0183/">Professional hydrographic survey software</a>
 * @see <a href="doc-files/NMEAParser-statements.html#BNF">BNF Syntax for NMEA</a>
 */
@GenClassname("org.vesalainen.parsers.nmea.NMEAParserImpl")
@GrammarDef()
@Rules(
{
    @Rule(left = "statements", value = "statement*"),
    @Rule(left = "statement", value = "nmeaStatement"),
    @Rule(left = "nmeaStatement", value = "'\\$' talkerId nmeaSentence '[\\,]*\\*' checksum '\r\n'"),
    @Rule(left = "nmeaStatement", value = "'\\$P' proprietaryType c proprietaryData '\\*' checksum '\r\n'"),
    @Rule(left = "nmeaStatement", value = "aivdm aisPrefix '\\*' checksum '\r\n'"),
    @Rule(left = "nmeaStatement", value = "aivdo aisPrefix '\\*' checksum '\r\n'"),
    @Rule(left = "nmeaSentence", value = "aam c arrivalStatus c waypointStatus c arrivalCircleRadius c waypoint"),
    @Rule(left = "nmeaSentence", value = "alm c totalNumberOfMessages c messageNumber c satellitePRNNumber c gpsWeekNumber c svHealth c eccentricity c almanacReferenceTime c inclinationAngle c rateOfRightAscension c rootOfSemiMajorAxis c argumentOfPerigee c longitudeOfAscensionNode c meanAnomaly c f0ClockParameter c f1ClockParameter"),
    @Rule(left = "nmeaSentence", value = "apa c status c status2 c crossTrackError c arrivalStatus c waypointStatus c bearingOriginToDestination c waypoint"),
    @Rule(left = "nmeaSentence", value = "apb c status c status2 c crossTrackError c arrivalStatus c waypointStatus c bearingOriginToDestination c waypoint c bearingPresentPositionToDestination c headingToSteerToDestination"),
    @Rule(left = "nmeaSentence", value = "bod c bearing c bearing c waypointToWaypoint"),
    @Rule(left = "nmeaSentence", value = "bec c utc c location c bearing c bearing c distanceToWaypoint c waypoint"),
    @Rule(left = "nmeaSentence", value = "bwc c utc c location c bearing c bearing c distanceToWaypoint c waypoint faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "bwr c utc c location c bearing c bearing c distanceToWaypoint c waypoint"),
    @Rule(left = "nmeaSentence", value = "bww c bearing c bearing c waypointToWaypoint"),
    @Rule(left = "nmeaSentence", value = "dbk c depthBelowKeel c depthBelowKeel c depthBelowKeel"),
    @Rule(left = "nmeaSentence", value = "dbs c depthBelowSurface c depthBelowSurface c depthBelowSurface"),
    @Rule(left = "nmeaSentence", value = "dbt c depthBelowTransducer c depthBelowTransducer c depthBelowTransducer"),
    @Rule(left = "nmeaSentence", value = "dpt c depthOfWater"),
    @Rule(left = "nmeaSentence", value = "gga c utc c location c gpsQualityIndicator c numberOfSatellitesInView c horizontalDilutionOfPrecision c antennaAltitude c geoidalSeparation c ageOfDifferentialGPSData c differentialReferenceStationID"),
    @Rule(left = "nmeaSentence", value = "gll c location c utc c status faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "gsa c selectionMode c mode c sat1? c sat2? c sat3? c sat4? c sat5? c sat6? c sat7? c sat8? c sat9? c sat10? c sat11? c sat12? c pdop c hdop c vdop"),
    @Rule(left = "nmeaSentence", value = "gsv c totalNumberOfMessages c messageNumber c totalNumberOfSatellitesInView (c prn c elevation c azimuth c snr)+"),
    @Rule(left = "nmeaSentence", value = "hdg c magneticSensorHeading c magneticDeviation c magneticVariation"),
    @Rule(left = "nmeaSentence", value = "hdm c heading"),
    @Rule(left = "nmeaSentence", value = "hdt c heading"),
    @Rule(left = "nmeaSentence", value = "mtw c waterTemperature"),
    @Rule(left = "nmeaSentence", value = "mwv c windAngle c windSpeed c status"),
    @Rule(left = "nmeaSentence", value = "r00 c waypoints"),
    @Rule(left = "nmeaSentence", value = "rma c status c location c timeDifference c speedOverGround c trackMadeGood c magneticVariation"),
    @Rule(left = "nmeaSentence", value = "rmb c status c crossTrackErrorNM c waypointToWaypoint c destinationWaypointLocation c rangeToDestination c bearingToDestination c destinationClosingVelocity c arrivalStatus faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "rmc c utc c status c location c speedOverGround c trackMadeGood c date c magneticVariation faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "rot c rateOfTurn c status"),
    @Rule(left = "nmeaSentence", value = "rpm c rpmSource c rpmSourceNumber c rpm c propellerPitch c status"),
    @Rule(left = "nmeaSentence", value = "rsa c starboardRudderSensor c status c portRudderSensor c status2"),
    @Rule(left = "nmeaSentence", value = "rte c totalNumberOfMessages c messageNumber c messageMode c route c waypoints"),
    @Rule(left = "nmeaSentence", value = "tll c targetNumber c destinationWaypointLocation c targetName c targetTime c targetStatus c referenceTarget"),
    @Rule(left = "nmeaSentence", value = "txt c totalNumberOfMessages c messageNumber c targetName c message"),
    @Rule(left = "nmeaSentence", value = "vhw c waterHeading c waterHeading c waterSpeed c waterSpeed"),
    @Rule(left = "nmeaSentence", value = "vtg c track c track c speed c speed faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "vtg c trueCourseOverGround c magneticCourseOverGround c speedOverGroundKnots c speedOverGroundKilometers"),
    @Rule(left = "nmeaSentence", value = "vwr c windDirection c windSpeed c windSpeed c windSpeed"),
    @Rule(left = "nmeaSentence", value = "wcv' c velocityToWaypoint c waypoint"),
    @Rule(left = "nmeaSentence", value = "wnc c distanceToWaypoint c distanceToWaypoint c waypointToWaypoint"),
    @Rule(left = "nmeaSentence", value = "wpl c destinationWaypointLocation c waypoint"),
    @Rule(left = "nmeaSentence", value = "xte c status c status2 c crossTrackError faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "xtr c crossTrackError"),
    @Rule(left = "nmeaSentence", value = "zda c utc c day c month c year c localZoneHours c localZoneMinutes"),
    @Rule(left = "rateOfTurn"),
    @Rule(left = "waterTemperature"),
    @Rule(left = "heading"),
    @Rule(left = "magneticSensorHeading"),
    @Rule(left = "magneticDeviation", value="c skip?"),
    @Rule(left = "faaModeIndicator"),
    @Rule(left = "messageMode"),
    @Rule(left = "distanceToWaypoint", value="c skip?"),
    @Rule(left = "depthBelowTransducer", value="c skip?"),
    @Rule(left = "depthBelowSurface", value="c skip?"),
    @Rule(left = "depthBelowKeel", value="c skip?"),
    @Rule(left = "f0ClockParameter"),
    @Rule(left = "f1ClockParameter"),
    @Rule(left = "meanAnomaly"),
    @Rule(left = "longitudeOfAscensionNode"),
    @Rule(left = "argumentOfPerigee"),
    @Rule(left = "rootOfSemiMajorAxis"),
    @Rule(left = "rateOfRightAscension"),
    @Rule(left = "inclinationAngle"),
    @Rule(left = "almanacReferenceTime"),
    @Rule(left = "eccentricity"),
    @Rule(left = "svHealth"),
    @Rule(left = "gpsWeekNumber"),
    @Rule(left = "satellitePRNNumber"),
    @Rule(left = "messageNumber"),
    @Rule(left = "totalNumberOfMessages"),
    @Rule(left = "geoidalSeparation", value = "c skip?"),
    @Rule(left = "ageOfDifferentialGPSData"),
    @Rule(left = "differentialReferenceStationID"),
    @Rule(left = "status"),
    @Rule(left = "status2"),
    @Rule(left = "waypointStatus"),
    @Rule(left = "arrivalStatus"),
    @Rule(left = "date"),
    @Rule(left = "utc"),
    @Rule(left = "waypoint"),
    @Rule(left = "timeDifference", value = "c skip?"),
    @Rule(left = "arrivalCircleRadius", value = "c skip?"),
    @Rule(left = "depthOfWater", value = "c skip?"),
    @Rule(left = "windSpeed"),
    @Rule(left = "destinationWaypointLocation", value = "c c c"),
    @Rule(left = "location", value = "c c c"),
    @Rule(left = "trackMadeGood"),
    @Rule(left = "speedOverGround"),
    @Rule(left = "magneticVariation", value = "c skip?"),
    @Rule(left = "crossTrackErrorNM", value = "c skip?"),
    @Rule(left = "crossTrackError", value = "c c"),
    @Rule(left = "waypointToWaypoint", value = "c skip?"),
    @Rule(left = "rangeToDestination"),
    @Rule(left = "headingToSteerToDestination", value = "c skip?"),
    @Rule(left = "bearingPresentPositionToDestination", value = "c skip?"),
    @Rule(left = "bearingOriginToDestination", value = "c skip?"),
    @Rule(left = "bearingToDestination"),
    @Rule(left = "bearing", value = "c skip?"),
    @Rule(left = "destinationClosingVelocity"),
    @Rule(left = "gpsQualityIndicator"),
    @Rule(left = "numberOfSatellitesInView"),
    @Rule(left = "horizontalDilutionOfPrecision"),
    @Rule(left = "antennaAltitude", value = "c skip?"),
    @Rule(left = "starboardRudderSensor"),
    @Rule(left = "portRudderSensor"),
    @Rule(left = "rpmSource"),
    @Rule(left = "rpmSourceNumber"),
    @Rule(left = "rpm"),
    @Rule(left = "propellerPitch"),
    @Rule(left = "localZoneHours", value = "c skip?"),
    @Rule(left = "localZoneMinutes", value = "c skip?"),
    @Rule(left = "windDirection", value = "c skip?"),
    @Rule(left = "waterHeading", value = "c skip?"),
    @Rule(left = "waterSpeed", value = "c skip?"),
    @Rule(left = "track", value = "c skip?"),
    @Rule(left = "speed", value = "c skip?"),
    @Rule(left = "windAngle")
})
public abstract class NMEAParser extends NMEASentences implements ParserInfo, ChecksumProvider
{
    private static final LocalNMEAChecksum localChecksum = new LocalNMEAChecksum();
    
    @Rule("'!AIVDM'")
    protected void aivdm(@ParserContext("aisContext") AISContext aisContext)
    {
        aisContext.setOwnMessage(false);
    }

    @Rule("'!AIVDO'")
    protected void aivdo(@ParserContext("aisContext") AISContext aisContext)
    {
        aisContext.setOwnMessage(true);
    }

    @Rule("letter")
    protected void selectionMode(char mode, @ParserContext("data") NMEAObserver data)
    {
        data.setSelectionMode(mode);
    }
    
    @Rule("alphaNum")
    protected void mode(char mode, @ParserContext("data") NMEAObserver data)
    {
        data.setMode(mode);
    }
    
    @Rule("integer")
    protected void sat1(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId1(id);
    }
    
    @Rule("integer")
    protected void sat2(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId2(id);
    }
    
    @Rule("integer")
    protected void sat3(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId3(id);
    }
    
    @Rule("integer")
    protected void sat4(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId4(id);
    }
    
    @Rule("integer")
    protected void sat5(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId5(id);
    }
    
    @Rule("integer")
    protected void sat6(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId6(id);
    }
    
    @Rule("integer")
    protected void sat7(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId7(id);
    }
    
    @Rule("integer")
    protected void sat8(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId8(id);
    }
    
    @Rule("integer")
    protected void sat9(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId9(id);
    }
    
    @Rule("integer")
    protected void sat10(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId10(id);
    }
    
    @Rule("integer")
    protected void sat11(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId11(id);
    }
    
    @Rule("integer")
    protected void sat12(int id, @ParserContext("data") NMEAObserver data)
    {
        data.setSatelliteId12(id);
    }
    
    @Rule("decimal")
    protected void pdop(float value, @ParserContext("data") NMEAObserver data)
    {
        data.setPdop(value);
    }
    
    @Rule("decimal")
    protected void hdop(float value, @ParserContext("data") NMEAObserver data)
    {
        data.setHdop(value);
    }
    
    @Rule("decimal")
    protected void vdop(float value, @ParserContext("data") NMEAObserver data)
    {
        data.setVdop(value);
    }
    
    @Rule("integer")
    protected void totalNumberOfSatellitesInView(int count, @ParserContext("data") NMEAObserver data)
    {
        data.setTotalNumberOfSatellitesInView(count);
    }
    
    @Rule("integer")
    protected void prn(int prn, @ParserContext("data") NMEAObserver data)
    {
        data.setPrn(prn);
    }
    
    @Rule("integer")
    protected void elevation(int elevation, @ParserContext("data") NMEAObserver data)
    {
        data.setElevation(elevation);
    }
    
    @Rule("integer")
    protected void azimuth(int azimuth, @ParserContext("data") NMEAObserver data)
    {
        data.setAzimuth(azimuth);
    }
    
    @Rule("integer")
    protected void snr(int snr, @ParserContext("data") NMEAObserver data)
    {
        data.setSnr(snr);
    }
    
    @Rule("string")
    protected void proprietaryType(
            String type,
            @ParserContext("data") NMEAObserver data)
    {
        data.setProprietaryType(type);
    }
    @Rule("stringList")
    protected void proprietaryData(
            List<String> pdata,
            @ParserContext("data") NMEAObserver data)
    {
        data.setProprietaryData(pdata);
    }
    @Rule
    protected void targetName()
    {
    }
    @Rule("string")
    protected void targetName(
            String name, 
            @ParserContext("data") NMEAObserver data)
    {
        data.setTargetName(name);
    }
    @Rule
    protected void message()
    {
    }
    @Rule("string")
    protected void message(
            String message, 
            @ParserContext("data") NMEAObserver data)
    {
        data.setMessage(message);
    }
    @Rule
    protected int sequentialMessageID()
    {
        return 0;
    }

    @Rule("integer")
    protected int sequentialMessageID(int id)
    {
        return id;
    }

    @Rule
    protected char channel()
    {
        return 0;
    }

    @Rule("letter")
    protected char channel(char cc)
    {
        return cc;
    }

    @Rule("c integer c integer c sequentialMessageID c channel c")
    protected void aisPrefix(
            int numberOfSentences,
            int sentenceNumber,
            int sequentialMessageID,
            char channel,
            @ParserContext(ParserConstants.InputReader) InputReader input,
            @ParserContext("aisContext") AISContext aisContext
            )
    {
        aisContext.startOfSentence(
                input, 
                numberOfSentences, 
                sentenceNumber,
                sequentialMessageID,
                channel
        );
    }

    @Rule("integer")
    protected void day(
            int day,
            @ParserContext("clock") Clock clock)
    {
        clock.setDay(day);
    }

    @Rule("integer")
    protected void month(
            int month,
            @ParserContext("clock") Clock clock)
    {
        clock.setMonth(month);
    }

    @Rule("integer")
    protected void year(
            int year,
            @ParserContext("clock") Clock clock)
    {
        clock.setYear(year);
    }

    @Rule("integer")
    protected void localZoneHours(
            int localZoneHours,
            @ParserContext("clock") Clock clock)
    {
        clock.setZoneHours(localZoneHours);
    }

    @Rule("integer")
    protected void localZoneMinutes(
            int localZoneMinutes,
            @ParserContext("clock") Clock clock)
    {
        clock.setZoneMinutes(localZoneMinutes);
    }

    @Rule("decimal c letter")
    protected void velocityToWaypoint(
            float velocityToWaypoint,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setVelocityToWaypoint(toKnots(velocityToWaypoint, unit));
    }

    @Rule("decimal c letter")
    protected void windDirection(
            float windDirection,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWindDirection(leftOrRight(windDirection, unit));
    }

    @Rule("decimal c letter")
    protected void waterHeading(
            float waterHeading,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        switch (unit)
        {
            case 'T':
                data.setTrueWaterHeading(waterHeading);
                break;
            case 'M':
                data.setMagneticWaterHeading(waterHeading);
                break;
            default:
                throw new IllegalArgumentException(unit+" expected T/M");
        }
    }

    @Rule("decimal c letter")
    protected void waterSpeed(
            float waterSpeed,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaterSpeed(toKnots(waterSpeed, unit));
    }

    @Rule("decimal")
    protected void starboardRudderSensor(
            float starboardRudderSensor,
            @ParserContext("data") NMEAObserver data)
    {
        data.setStarboardRudderSensor(starboardRudderSensor);
    }

    @Rule("decimal")
    protected void portRudderSensor(
            float portRudderSensor,
            @ParserContext("data") NMEAObserver data)
    {
        data.setPortRudderSensor(portRudderSensor);
    }

    @Rule("letter")
    protected void rpmSource(
            char rpmSource,
            @ParserContext("data") NMEAObserver data)
    {
        data.setRpmSource(rpmSource);
    }

    @Rule("integer")
    protected void rpmSourceNumber(
            int rpmSourceNumber,
            @ParserContext("data") NMEAObserver data)
    {
        data.setRpmSourceNumber(rpmSourceNumber);
    }

    @Rule("decimal")
    protected void rpm(
            float rpm,
            @ParserContext("data") NMEAObserver data)
    {
        data.setRpm(rpm);
    }

    @Rule("decimal")
    protected void propellerPitch(
            float propellerPitch,
            @ParserContext("data") NMEAObserver data)
    {
        data.setPropellerPitch(propellerPitch);
    }

    @Rule("decimal")
    protected void rateOfTurn(
            float rateOfTurn,
            @ParserContext("data") NMEAObserver data)
    {
        data.setRateOfTurn(rateOfTurn);
    }

    @Rule("decimal c letter")
    protected void windAngle(
            float windAngle,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        switch (unit)
        {
            case 'T':
                data.setTrueWindAngle(windAngle);
                break;
            case 'R':
                data.setRelativeWindAngle(windAngle);
                break;
            default:
                throw new IllegalArgumentException(unit+ "expected T/R");
        }
    }

    @Rule("decimal c letter")
    protected void windSpeed(
            float windSpeed,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWindSpeed(toMetersPerSecond(windSpeed, unit));
    }

    @Rule("decimal c letter")
    protected void waterTemperature(
            float waterTemperature,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaterTemperature(toCelcius(waterTemperature, unit));
    }

    @Rule("decimal c letter")
    protected void heading(
            float heading,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        switch (unit)
        {
            case 'T':
                data.setTrueHeading(heading);
                break;
            case 'M':
                data.setMagneticHeading(heading);
                break;
            default:
                throw new IllegalArgumentException(unit+ "expected T/M");
        }
    }

    @Rule("decimal")
    protected void magneticSensorHeading(
            float magneticSensorHeading,
            @ParserContext("data") NMEAObserver data)
    {
        data.setMagneticSensorHeading(magneticSensorHeading);
    }

    @Rule("decimal c ew")
    protected void magneticDeviation(
            float magneticDeviation,
            float sign,
            @ParserContext("data") NMEAObserver data)
    {
        data.setMagneticDeviation(sign * magneticDeviation);
    }

    @Rule("decimal c decimal")
    protected void depthOfWater(
            float depth,
            float offset,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthOfWater(depth);
        data.setDepthOffsetOfWater(offset);
    }

    @Rule("stringList")
    protected void waypoints(
            List<String> list,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaypoints(list);
    }

    @Rule("string")
    protected List<String> stringList(String waypoint)
    {
        List<String> list = new ArrayList<>();
        list.add(waypoint);
        return list;
    }

    @Rule("stringList c string")
    protected List<String> stringList(List<String> list, String waypoint)
    {
        list.add(waypoint);
        return list;
    }

    @Rule("c letter")
    protected void faaModeIndicator(
            char faaModeIndicator,
            @ParserContext("data") NMEAObserver data)
    {
        data.setFaaModeIndicator(faaModeIndicator);
    }

    @Rule("letter")
    protected void messageMode(
            char messageMode,
            @ParserContext("data") NMEAObserver data)
    {
        data.setMessageMode(messageMode);
    }

    @Rule("decimal c letter")
    protected void distanceToWaypoint(
            float distanceToWaypoint,
            char units,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDistanceToWaypoint(toKnots(distanceToWaypoint, units));
    }

    @Rule("decimal c letter")
    protected void depthBelowTransducer(
            float depth,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthBelowTransducer(toMeters(depth, unit));
    }

    @Rule("decimal c letter")
    protected void depthBelowSurface(
            float depth,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthBelowSurface(toMeters(depth, unit));
    }

    @Rule("decimal c letter")
    protected void depthBelowKeel(
            float depth,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthBelowKeel(toMeters(depth, unit));
    }

    @Rule("hex")
    protected void f1ClockParameter(
            int f1ClockParameter,
            @ParserContext("data") NMEAObserver data)
    {
        data.setF1ClockParameter(f1ClockParameter);
    }

    @Rule("hex")
    protected void f0ClockParameter(
            int f0ClockParameter,
            @ParserContext("data") NMEAObserver data)
    {
        data.setF0ClockParameter(f0ClockParameter);
    }

    @Rule("hex")
    protected void meanAnomaly(
            int meanAnomaly,
            @ParserContext("data") NMEAObserver data)
    {
        data.setMeanAnomaly(meanAnomaly);
    }

    @Rule("hex")
    protected void longitudeOfAscensionNode(
            int longitudeOfAscensionNode,
            @ParserContext("data") NMEAObserver data)
    {
        data.setLongitudeOfAscensionNode(longitudeOfAscensionNode);
    }

    @Rule("hex")
    protected void argumentOfPerigee(
            int argumentOfPerigee,
            @ParserContext("data") NMEAObserver data)
    {
        data.setArgumentOfPerigee(argumentOfPerigee);
    }

    @Rule("hex")
    protected void rootOfSemiMajorAxis(
            int rootOfSemiMajorAxis,
            @ParserContext("data") NMEAObserver data)
    {
        data.setRootOfSemiMajorAxis(rootOfSemiMajorAxis);
    }

    @Rule("hex")
    protected void rateOfRightAscension(
            int rateOfRightAscension,
            @ParserContext("data") NMEAObserver data)
    {
        data.setRateOfRightAscension(rateOfRightAscension);
    }

    @Rule("hex")
    protected void inclinationAngle(
            int inclinationAngle,
            @ParserContext("data") NMEAObserver data)
    {
        data.setInclinationAngle(inclinationAngle);
    }

    @Rule("hex")
    protected void almanacReferenceTime(
            int almanacReferenceTime,
            @ParserContext("data") NMEAObserver data)
    {
        data.setAlmanacReferenceTime(almanacReferenceTime);
    }

    @Rule("hex")
    protected void eccentricity(
            int eccentricity,
            @ParserContext("data") NMEAObserver data)
    {
        data.setEccentricity(eccentricity);
    }

    @Rule("hex")
    protected void svHealth(
            int svHealth,
            @ParserContext("data") NMEAObserver data)
    {
        data.setSvHealth(svHealth);
    }

    @Rule("integer")
    protected void gpsWeekNumber(
            int gpsWeekNumber,
            @ParserContext("data") NMEAObserver data)
    {
        data.setGpsWeekNumber(gpsWeekNumber);
    }

    @Rule("integer")
    protected void satellitePRNNumber(
            int satellitePRNNumber,
            @ParserContext("data") NMEAObserver data)
    {
        data.setSatellitePRNNumber(satellitePRNNumber);
    }

    @Rule("integer")
    protected void messageNumber(
            int messageNumber,
            @ParserContext("data") NMEAObserver data)
    {
        data.setMessageNumber(messageNumber);
    }

    @Rule("integer")
    protected void totalNumberOfMessages(
            int totalNumberOfMessages,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTotalNumberOfMessages(totalNumberOfMessages);
    }

    @Rule("decimal c letter")
    protected void arrivalCircleRadius(
            float arrivalCircleRadius,
            char units,
            @ParserContext("data") NMEAObserver data)
    {
        data.setArrivalCircleRadius(distance(arrivalCircleRadius, units));
    }

    @Rule("decimal c decimal")
    protected void timeDifference(
            float timeDifferenceA, // uS
            float timeDifferenceB, // uS
            @ParserContext("data") NMEAObserver data)
    {
        data.setTimeDifferenceA(timeDifferenceA);
        data.setTimeDifferenceB(timeDifferenceB);
    }

    @Rule("string")
    protected void waypoint(
            String waypoint,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaypoint(waypoint);
    }

    @Rule("string")
    protected void route(
            String route,
            @ParserContext("data") NMEAObserver data)
    {
        data.setRoute(route);
    }

    @Rule("integer")
    protected void targetNumber(
            int target,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTargetNumber(target);
    }

    @Rule("letter")
    protected void targetStatus(
            char status,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTargetStatus(status);
    }
    
    @Rule("string")
    protected void referenceTarget(
            String referenceTarget,
            @ParserContext("data") NMEAObserver data)
    {
        data.setReferenceTarget(referenceTarget);
    }

    @Rule("digit2 digit2 decimal")
    protected void targetTime(
            int hour,
            int minute,
            float second,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTargetHour(hour);
        data.setTargetMinute(minute);
        data.setTargetSecond(second);
    }

    @Rule("digit2 digit2 decimal")
    protected void utc(
            int hour,
            int minute,
            float second,
            @ParserContext("clock") Clock clock)
    {
        clock.setTime(hour, minute, second);
    }

    @Rule("digit2 digit2 digit2")
    protected void date(
            int day,
            int month,
            int year,
            @ParserContext("clock") Clock clock)
    {
        clock.setDate(year, month, day);
    }

    @Rule("letter")
    protected void arrivalStatus(
            char arrivalStatus,
            @ParserContext("data") NMEAObserver data)
    {
        data.setArrivalStatus(arrivalStatus);
    }

    @Rule("letter")
    protected void waypointStatus(
            char waypointStatus,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaypointStatus(waypointStatus);
    }

    @Rule("letter")
    protected void status(
            char status,
            @ParserContext("data") NMEAObserver data)
    {
        data.setStatus(status);
    }

    @Rule("letter")
    protected void status2(
            char status,
            @ParserContext("data") NMEAObserver data)
    {
        data.setStatus2(status);
    }

    @Rule("integer")
    protected void differentialReferenceStationID(
            int differentialReferenceStationID, //0000-1023            
            @ParserContext("data") NMEAObserver data)
    {
        data.setDifferentialReferenceStationID(differentialReferenceStationID);
    }

    @Rule("decimal")
    protected void ageOfDifferentialGPSData(
            float ageOfDifferentialGPSData, //time in seconds since last SC104 type 1 or 9 update, null field when DGPS is not used
            @ParserContext("data") NMEAObserver data)
    {
        data.setAgeOfDifferentialGPSData(ageOfDifferentialGPSData);
    }

    @Rule("decimal c letter")
    protected void geoidalSeparation(
            float geoidalSeparation, //the difference between the WGS-84 earth ellipsoid and mean-sea-level (geoid), "-" means mean-sea-level below ellipsoid
            char unitsOfGeoidalSeparation, // meters
            @ParserContext("data") NMEAObserver data)
    {
        data.setGeoidalSeparation(toMeters(geoidalSeparation, unitsOfGeoidalSeparation));
    }

    @Rule("decimal c letter")
    protected void antennaAltitude(
            float antennaAltitude, // above/below mean-sea-level (geoid) (in meters)
            char unitsOfAntennaAltitude, //meters
            @ParserContext("data") NMEAObserver data)
    {
        data.setAntennaAltitude(toMeters(antennaAltitude, unitsOfAntennaAltitude));
    }

    @Rule("decimal")
    protected void horizontalDilutionOfPrecision(
            float horizontalDilutionOfPrecision, // (meters)
            @ParserContext("data") NMEAObserver data)
    {
        data.setHorizontalDilutionOfPrecision(horizontalDilutionOfPrecision);
    }

    @Rule("integer")
    protected void numberOfSatellitesInView(
            int numberOfSatellitesInView,
            @ParserContext("data") NMEAObserver data)
    {
        data.setNumberOfSatellitesInView(numberOfSatellitesInView);
    }

    @Rule("integer")
    protected void gpsQualityIndicator(
            int gpsQualityIndicator,
            @ParserContext("data") NMEAObserver data)
    {
        data.setGpsQualityIndicator(GPSQualityIndicator.values()[gpsQualityIndicator]);
    }

    @Rule("decimal")
    protected void destinationClosingVelocity(
            float destinationClosingVelocity, // knots
            @ParserContext("data") NMEAObserver data)
    {
        data.setDestinationClosingVelocity(destinationClosingVelocity);
    }

    @Rule("decimal c letter")
    protected void bearing(
            float bearing, // degrees
            char unit, // M = Magnetic, T = True
            @ParserContext("data") NMEAObserver data)
    {
        switch (unit)
        {
            case 'T':
                data.setTrueBearing(bearing);
                break;
            case 'M':
                data.setMagneticBearing(bearing);
                break;
            default:
                throw new IllegalArgumentException(unit+" expected T/M");
        }
    }

    @Rule("decimal")
    protected void bearingToDestination(
            float bearingToDestination, // degrees
            @ParserContext("data") NMEAObserver data)
    {
        data.setBearingToDestination(bearingToDestination);
    }

    @Rule("decimal c letter")
    protected void bearingOriginToDestination(
            float bearingOriginToDestination, // degrees
            char mOrT, // M = Magnetic, T = True
            @ParserContext("data") NMEAObserver data)
    {
        switch (mOrT)
        {
            case 'T':
                data.setTrueBearingOriginToDestination(bearingOriginToDestination);
                break;
            case 'M':
                data.setMagneticBearingOriginToDestination(bearingOriginToDestination);
                break;
            default:
                throw new IllegalArgumentException(mOrT+" expected T/M");
        }
    }

    @Rule("decimal c letter")
    protected void bearingPresentPositionToDestination(
            float bearingPresentPositionToDestination, // degrees
            char mOrT, // M = Magnetic, T = True
            @ParserContext("data") NMEAObserver data)
    {
        switch (mOrT)
        {
            case 'T':
                data.setTrueBearingPresentPositionToDestination(bearingPresentPositionToDestination);
                break;
            case 'M':
                data.setMagneticBearingPresentPositionToDestination(bearingPresentPositionToDestination);
                break;
            default:
                throw new IllegalArgumentException(mOrT+" expected T/M");
        }
    }

    @Rule("decimal c letter")
    protected void headingToSteerToDestination(
            float headingToSteerToDestination, // degrees
            char mOrT, // M = Magnetic, T = True
            @ParserContext("data") NMEAObserver data)
    {
        switch (mOrT)
        {
            case 'T':
                data.setTrueHeadingToSteerToDestination(headingToSteerToDestination);
                break;
            case 'M':
                data.setMagneticHeadingToSteerToDestination(headingToSteerToDestination);
                break;
            default:
                throw new IllegalArgumentException(mOrT+" expected T/M");
        }
    }

    @Rule("decimal")
    protected void rangeToDestination(
            float rangeToDestination, // NM
            @ParserContext("data") NMEAObserver data)
    {
        data.setRangeToDestination(rangeToDestination);
    }

    @Rule("string c string")
    protected void waypointToWaypoint(
            String toWaypoint,
            String fromWaypoint,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setToWaypoint(toWaypoint);
        data.setFromWaypoint(fromWaypoint);
    }

    @Rule("string c")
    protected void waypointToWaypoint(
            String toWaypoint,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setToWaypoint(toWaypoint);
    }

    @Rule("decimal c letter c letter")
    protected void crossTrackError(
            float crossTrackError, // NM
            char directionToSteer,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setCrossTrackError(leftOrRight(toKnots(crossTrackError, unit), directionToSteer));
    }

    @Rule("decimal c letter")
    protected void crossTrackErrorNM(
            float crossTrackError, // NM
            char directionToSteer,
            @ParserContext("data") NMEAObserver data)
    {
        data.setCrossTrackError(leftOrRight(crossTrackError, directionToSteer));
    }

    @Rule("decimal c ew")
    protected void magneticVariation(
            float magneticVariation, // degrees
            float mew,
            @ParserContext("data") NMEAObserver data)
    {
        data.setMagneticVariation(mew * magneticVariation);
    }

    @Rule("decimal")
    protected void speedOverGround(
            float speedOverGround, // knots
            @ParserContext("data") NMEAObserver data)
    {
        data.setSpeedOverGround(speedOverGround);
    }

    @Rule("decimal")
    protected void trackMadeGood(
            float trackMadeGood, // knots
            @ParserContext("data") NMEAObserver data)
    {
        data.setTrackMadeGood(trackMadeGood);
    }

    @Rule("decimal c letter")
    protected void track(
            float degrees,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        switch (unit)
        {
            case 'T':
                data.setTrueTrackMadeGood(degrees);
                break;
            case 'M':
                data.setMagneticTrackMadeGood(degrees);
                break;
            default:
                throw new IllegalArgumentException(unit+" expected T/M");
        }
    }

    @Rule("decimal")
    protected void trueCourseOverGround(
            float degrees,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTrueTrackMadeGood(degrees);
    }

    @Rule("decimal")
    protected void magneticCourseOverGround(
            float degrees,
            @ParserContext("data") NMEAObserver data)
    {
        data.setMagneticTrackMadeGood(degrees);
    }

    @Rule("decimal c letter")
    protected void speed(
            float speed,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setSpeedOverGround(toKnots(speed, unit));
    }

    @Rule("decimal")
    protected void speedOverGroundKnots(
            float speed,
            @ParserContext("data") NMEAObserver data)
    {
        data.setSpeedOverGround(speed);
    }

    @Rule("decimal")
    protected void speedOverGroundKilometers(
            float speed,
            @ParserContext("data") NMEAObserver data)
    {
        data.setSpeedOverGround(toKnots(speed, 'K'));
    }

    @Rule("latitude c ns c longitude c ew")
    protected void location(
            float latitude,
            int ns,
            float longitude,
            int ew,
            @ParserContext("data") NMEAObserver data)
    {
        data.setLatitude(ns * latitude);
        data.setLongitude(ew * longitude);
    }

    @Rule("latitude c ns c longitude c ew")
    protected void destinationWaypointLocation(
            float latitude,
            int ns,
            float longitude,
            int ew,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDestinationWaypointLatitude(ns * latitude);
        data.setDestinationWaypointLongitude(ew * longitude);
    }

    @Rule("letterNotP letter")
    protected void talkerId(char c1, char c2, @ParserContext("data") NMEAObserver data)
    {
        data.setTalkerId1(c1);
        data.setTalkerId2(c2);
    }

    @Rule("hex")
    protected void checksum(
            int sum,
            @ParserContext(ParserConstants.InputReader) InputReader input,
            @ParserContext("clock") Clock clock,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("aisContext") AISContext aisContext
            )
    {
        NMEAChecksum checksum = (NMEAChecksum) getChecksum();
        if (sum != checksum.getValue())
        {
            clock.rollback();
            String reason = input.getLineNumber()+": checksum " + Integer.toHexString(sum) + " != " + Integer.toHexString((int) checksum.getValue());
            data.rollback(reason);
            if (aisContext.isAisMessage())
            {
                aisContext.afterChecksum(false, reason);
            }
        }
        else
        {
            clock.commit();
            String reason = input.getLineNumber()+": "+Integer.toHexString(sum);
            data.commit(reason);
            if (aisContext.isAisMessage())
            {
                aisContext.afterChecksum(true, reason);
            }
        }
        aisContext.setAisMessage(false);
    }

    @Terminal(expression = "[0-9]+\\.[0-9]+")
    protected float latitude(float lat)
    {
        float degrees = (float) Math.floor(lat / 100);
        float minutes = lat - 100F * degrees;
        float latitude = degrees + minutes / 60F;
        assert latitude >= 0;
        assert latitude <= 90;
        return latitude;
    }

    @Terminal(expression = "[0-9]+\\.[0-9]+")
    protected float longitude(float lat)
    {
        float degrees = (float) Math.floor(lat / 100);
        float minutes = lat - 100F * degrees;
        float longitude = degrees + minutes / 60F;
        assert longitude >= 0;
        assert longitude <= 180;
        return longitude;
    }

    @Terminal(expression = "[NS]")
    protected int ns(char c)
    {
        return 'N' == c ? 1 : -1;
    }

    @Terminal(expression = "[WE]")
    protected int ew(char c)
    {
        return 'E' == c ? 1 : -1;
    }

    @Terminal(expression = "[a-zA-Z]")
    protected abstract char letter(char c);

    @Terminal(expression = "[a-zA-Z0-9]")
    protected abstract char alphaNum(char c);

    @Terminal(expression = "[a-zA-OQ-Z]")
    protected abstract char letterNotP(char c);

    @Terminal(expression = "[0-9A-Fa-f]")
    protected abstract char hexAlpha(char x);

    @Terminal(expression = "[a-zA-Z0-9 \\.\\-\\(\\)]+")
    protected String string(String input)
    {
        return input;
    }

    @Terminal(expression = "[\\+\\-]?[0-9]+")
    protected abstract int integer(int i);

    @Terminal(expression = "[0-9]{2}")
    protected abstract int digit2(int i);

    @Terminal(expression = "[0-9a-fA-F]+", radix=16)
    protected abstract int hex(int i);

    @Terminal(expression = "[\\+\\-]?[0-9]+(\\.[0-9]+)?")
    protected abstract float decimal(float f);

    @Terminal(expression = "[\\,]")
    protected abstract void c();

    @Terminal(expression = "[a-zA-Z0-9 \\.\\-\\(\\)]+")
    protected abstract void skip();

    @RecoverMethod
    public void recover(
            @ParserContext("data") NMEAObserver data,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader,
            @ParserContext(ParserConstants.THROWABLE) Throwable thr
            ) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(reader.getInput());
        sb.append('^');
        int cc = reader.read();
        while (cc != '\n' && cc != -1)
        {
            sb.append((char) cc);
            cc = reader.read();
        }
        data.rollback("skipping " + sb);
        reader.clear();
    }
    /**
     * Parse NMEA
     * @param <I>
     * @param input
     * @param data NMEAObserver can be null
     * @param aisData AISObserver can be null
     * @throws IOException 
     */
    public <I> void parse(I input, NMEAObserver data, AISObserver aisData) throws IOException
    {
        if (data == null)
        {
            data = new AbstractNMEAObserver();
        }
        if (aisData == null)
        {
            aisData = new AbstractAISObserver();
        }
        Clock clock = new GPSClock();
        data.setClock(clock);
        aisData.setClock(clock);
        AISContext aisContext = new AISContext(aisData);
        try
        {
            if (input instanceof ScatteringByteChannel)
            {
                ScatteringByteChannel sbc = (ScatteringByteChannel) input;
                parse(sbc, clock, data, aisContext);
            }
            else
            {
                if (input instanceof URL)
                {
                    URL url = (URL) input;
                    parse(url, clock, data, aisContext);
                }
                else
                {
                    if (input instanceof String)
                    {
                        String str = (String) input;
                        parse(str, clock, data, aisContext);
                    }
                    else
                    {
                        if (input instanceof InputStream)
                        {
                            InputStream is = (InputStream) input;
                            parse(is, clock, data, aisContext);
                        }
                        else
                        {
                            throw new UnsupportedOperationException(input+" not supported as input");
                        }
                    }
                }
            }
        }
        finally
        {
            aisContext.waitAndStopThreads();
        }
    }
    @ParseMethod(start = "statements", size = 1024, charSet = "US-ASCII",
            features={WideIndex, UseChecksum, UseDirectBuffer}
    )
    protected abstract void parse(
            URL url,
            @ParserContext("clock") Clock clock,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("aisContext") AISContext aisContext
            ) throws IOException;

    @ParseMethod(start = "statements", size = 1024, charSet = "US-ASCII",
            features={WideIndex, UseChecksum, UseDirectBuffer}
    )
    protected abstract void parse(
            ScatteringByteChannel channel,
            @ParserContext("clock") Clock clock,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("aisContext") AISContext aisContext
            ) throws IOException;

    @ParseMethod(start = "statements", size = 1024, 
            features={WideIndex, UseChecksum}
    )
    protected abstract void parse(
            String text,
            @ParserContext("clock") Clock clock,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("aisContext") AISContext aisContext
            ) throws IOException;

    @ParseMethod(start = "statements", size = 8192, charSet = "US-ASCII",
            features={WideIndex, UseChecksum}
    )
    protected abstract void parse(
            InputStream is,
            @ParserContext("clock") Clock clock,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("aisContext") AISContext aisContext
            ) throws IOException;

    public static NMEAParser newInstance()
    {
        return (NMEAParser) GenClassFactory.loadGenInstance(NMEAParser.class);
    }

    @Override
    public Checksum getChecksum()
    {
        return localChecksum.get();
    }

    private float toKnots(float velocity, char unit)
    {
        switch (unit)
        {
            case 'N':
                return velocity;
            case 'M':
                return (float) Velocity.toKnots(velocity);
            case 'K':
                return (float) KilometersInHour.toKnots(velocity);
            default:
                throw new IllegalArgumentException(unit+" unknown expected N/M/K");
        }
    }

    private float toMetersPerSecond(float velocity, char unit)
    {
        switch (unit)
        {
            case 'N':
                return (float) Knots.toMetersPerSecond(velocity);
            case 'M':
                return velocity;
            case 'K':
                return (float) KilometersInHour.toMetersPerSecond(velocity);
            default:
                throw new IllegalArgumentException(unit+" unknown expected N/M/K");
        }
    }

    private float leftOrRight(float dir, char unit)
    {
        switch (unit)
        {
            case 'L':
                return -dir;
            case 'R':
                return dir;
            default:
                throw new IllegalArgumentException(unit+" unknown expected L/R");
        }
    }

    private float toCelcius(float temp, char unit)
    {
        switch (unit)
        {
            case 'C':
                return temp;
            default:
                throw new IllegalArgumentException(unit+" unknown expected C");
        }
    }

    private float toMeters(float depth, char unit)
    {
        switch (unit)
        {
            case 'F':
                return (float) Fathom.toMeters(depth);
            case 'M':
                return depth;
            case 'f':
                return (float) Feet.toMeters(depth);
            default:
                throw new IllegalArgumentException(unit+" unknown expected f/M/F");
        }
    }

    private float distance(float dist, char unit)
    {
        switch (unit)
        {
            case 'N':
                return dist;
            default:
                throw new IllegalArgumentException(unit+" unknown expected N");
        }
    }

}
