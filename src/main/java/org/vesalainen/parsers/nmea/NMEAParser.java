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

import java.io.FileInputStream;
import org.vesalainen.parsers.nmea.ais.AISObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
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
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.nmea.ais.AISContext;
import org.vesalainen.parsers.nmea.ais.AISParser;
import org.vesalainen.parsers.nmea.ais.VesselMonitor;

/**
 * @author Timo Vesalainen
 * @see <a href="http://catb.org/gpsd/NMEA.html">NMEA Revealed</a>
 * @see <a href="http://gpsd.berlios.de/AIVDM.html">AIVDM/AIVDO protocol decoding</a>
 * @see <a href="doc-files/NMEAParser-statements.html#BNF">BNF Syntax for NMEA</a>
 */
@GenClassname("org.vesalainen.parsers.nmea.NMEAParserImpl")
@GrammarDef()
@Rules(
{
    @Rule(left = "statements", value = "statement*"),
    @Rule(left = "statement", value = "nmeaStatement"),
    @Rule(left = "nmeaStatement", value = "'\\$' talkerId nmeaSentence '[\\,]*\\*' checksum '\r\n'"),
    @Rule(left = "nmeaStatement", value = "'\\$P' proprietaryType ('[\\,]' proprietaryData)* '\\*' checksum '\r\n'"),
    @Rule(left = "nmeaStatement", value = "aivdm aisPrefix '[0-5]+\\*' checksum '\r\n'"),
    @Rule(left = "nmeaStatement", value = "aivdo aisPrefix '[0-5]+\\*' checksum '\r\n'"),
    @Rule(left = "nmeaSentence", value = "'AAM' c arrivalStatus c waypointStatus c arrivalCircleRadius c waypoint"),
    @Rule(left = "nmeaSentence", value = "'ALM' c totalNumberOfMessages c messageNumber c satellitePRNNumber c gpsWeekNumber c svHealth c eccentricity c almanacReferenceTime c inclinationAngle c rateOfRightAscension c rootOfSemiMajorAxis c argumentOfPerigee c longitudeOfAscensionNode c meanAnomaly c f0ClockParameter c f1ClockParameter"),
    @Rule(left = "nmeaSentence", value = "'APA' c status c status2 c crossTrackError c arrivalStatus c waypointStatus c bearingOriginToDestination c waypoint"),
    @Rule(left = "nmeaSentence", value = "'APB' c status c status2 c crossTrackError c arrivalStatus c waypointStatus c bearingOriginToDestination c waypoint c bearingPresentPositionToDestination c headingToSteerToDestination"),
    @Rule(left = "nmeaSentence", value = "'BOD' c bearing c bearing c waypointToWaypoint"),
    @Rule(left = "nmeaSentence", value = "'BWC' c utc c location c bearing c bearing c distanceToWaypoint c waypoint faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "'BWR' c utc c location c bearing c bearing c distanceToWaypoint c waypoint"),
    @Rule(left = "nmeaSentence", value = "'BWW' c bearing c bearing c waypointToWaypoint"),
    @Rule(left = "nmeaSentence", value = "'DBK' c depthBelowKeel c depthBelowKeel c depthBelowKeel"),
    @Rule(left = "nmeaSentence", value = "'DBS' c depthBelowSurface c depthBelowSurface c depthBelowSurface"),
    @Rule(left = "nmeaSentence", value = "'DBT' c depthBelowTransducer c depthBelowTransducer c depthBelowTransducer"),
    @Rule(left = "nmeaSentence", value = "'DPT' c depthOfWater"),
    @Rule(left = "nmeaSentence", value = "'GGA' c utc c location c gpsQualityIndicator c numberOfSatellitesInView c horizontalDilutionOfPrecision c antennaAltitude c geoidalSeparation c ageOfDifferentialGPSData c differentialReferenceStationID"),
    @Rule(left = "nmeaSentence", value = "'GLL' c location c utc c status faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "'HDG' c magneticSensorHeading c magneticDeviation c magneticVariation"),
    @Rule(left = "nmeaSentence", value = "'HDM' c heading"),
    @Rule(left = "nmeaSentence", value = "'HDT' c heading"),
    @Rule(left = "nmeaSentence", value = "'MTW' c waterTemperature"),
    @Rule(left = "nmeaSentence", value = "'MWV' c windAngle c windSpeed c status"),
    @Rule(left = "nmeaSentence", value = "'R00' c waypoints"),
    @Rule(left = "nmeaSentence", value = "'RMA' c status c location c timeDifference c speedOverGround c trackMadeGood c magneticVariation"),
    @Rule(left = "nmeaSentence", value = "'RMB' c status c crossTrackErrorNM c waypointToWaypoint c destinationWaypointLocation c rangeToDestination c bearingToDestination c destinationClosingVelocity c arrivalStatus"),
    @Rule(left = "nmeaSentence", value = "'RMC' c utc c status c location c speedOverGround c trackMadeGood c date c magneticVariation faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "'RMM' c horizontalDatum"),
    @Rule(left = "nmeaSentence", value = "'ROT' c rateOfTurn c status"),
    @Rule(left = "nmeaSentence", value = "'RPM' c rpmSource c rpmSourceNumber c rpm c propellerPitch c status"),
    @Rule(left = "nmeaSentence", value = "'RSA' c starboardRudderSensor c status c portRudderSensor c status2"),
    @Rule(left = "nmeaSentence", value = "'RTE' c totalNumberOfMessages c messageNumber c messageMode c waypoints"),
    @Rule(left = "nmeaSentence", value = "'TXT' c totalNumberOfMessages c messageNumber c targetName c message"),
    @Rule(left = "nmeaSentence", value = "'VHW' c waterHeading c waterHeading c waterSpeed c waterSpeed"),
    @Rule(left = "nmeaSentence", value = "'VWR' c windDirection c windSpeed c windSpeed c windSpeed"),
    @Rule(left = "nmeaSentence", value = "'WCV' c velocityToWaypoint c waypoint"),
    @Rule(left = "nmeaSentence", value = "'WNC' c distanceToWaypoint c distanceToWaypoint c waypointToWaypoint"),
    @Rule(left = "nmeaSentence", value = "'WPL' c destinationWaypointLocation c waypoint"),
    @Rule(left = "nmeaSentence", value = "'XTE' c status c status2 c crossTrackError faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "'XTR' c crossTrackError"),
    @Rule(left = "nmeaSentence", value = "'ZDA' c utc c day c month c year c localZoneHours c localZoneMinutes"),
    @Rule(left = "rateOfTurn"),
    @Rule(left = "waterTemperature"),
    @Rule(left = "heading"),
    @Rule(left = "magneticSensorHeading"),
    @Rule(left = "magneticDeviation"),
    @Rule(left = "horizontalDatum"),
    @Rule(left = "faaModeIndicator"),
    @Rule(left = "messageMode"),
    @Rule(left = "distanceToWaypoint"),
    @Rule(left = "depthBelowTransducer"),
    @Rule(left = "depthBelowSurface"),
    @Rule(left = "depthBelowKeel"),
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
    @Rule(left = "geoidalSeparation", value = "c"),
    @Rule(left = "ageOfDifferentialGPSData"),
    @Rule(left = "differentialReferenceStationID"),
    @Rule(left = "status"),
    @Rule(left = "status2"),
    @Rule(left = "waypointStatus"),
    @Rule(left = "arrivalStatus"),
    @Rule(left = "date"),
    @Rule(left = "utc"),
    @Rule(left = "waypoint"),
    @Rule(left = "timeDifference", value = "c"),
    @Rule(left = "arrivalCircleRadius", value = "c"),
    @Rule(left = "depthOfWater", value = "c"),
    @Rule(left = "windSpeed"),
    @Rule(left = "destinationWaypointLocation", value = "c c c"),
    @Rule(left = "location", value = "c c c"),
    @Rule(left = "trackMadeGood"),
    @Rule(left = "speedOverGround"),
    @Rule(left = "magneticVariation", value = "c"),
    @Rule(left = "crossTrackErrorNM", value = "c"),
    @Rule(left = "crossTrackError", value = "c c"),
    @Rule(left = "waypointToWaypoint", value = "c"),
    @Rule(left = "rangeToDestination"),
    @Rule(left = "headingToSteerToDestination", value = "c"),
    @Rule(left = "bearingPresentPositionToDestination", value = "c"),
    @Rule(left = "bearingOriginToDestination", value = "c"),
    @Rule(left = "bearingToDestination"),
    @Rule(left = "bearing", value = "c"),
    @Rule(left = "destinationClosingVelocity"),
    @Rule(left = "gpsQualityIndicator"),
    @Rule(left = "numberOfSatellitesInView"),
    @Rule(left = "horizontalDilutionOfPrecision"),
    @Rule(left = "antennaAltitude", value = "c"),
    @Rule(left = "starboardRudderSensor"),
    @Rule(left = "portRudderSensor"),
    @Rule(left = "rpmSource"),
    @Rule(left = "rpmSourceNumber"),
    @Rule(left = "rpm"),
    @Rule(left = "propellerPitch"),
    @Rule(left = "localZoneHours", value = "c"),
    @Rule(left = "localZoneMinutes", value = "c"),
    @Rule(left = "windDirection", value = "c"),
    @Rule(left = "waterHeading", value = "c"),
    @Rule(left = "waterSpeed", value = "c"),
    @Rule(left = "windAngle")
})
public abstract class NMEAParser implements ParserInfo
{
    @Rule("'!AIVDM'")
    protected void aivdm(@ParserContext("aisContext") AISContext aisContext)
    {
        AISObserver aisData = aisContext.getAisData();
        aisData.setOwnMessage(false);
    }

    @Rule("'!AIVDO'")
    protected void aivdo(@ParserContext("aisContext") AISContext aisContext)
    {
        AISObserver aisData = aisContext.getAisData();
        aisData.setOwnMessage(true);
    }

    @Rule("string")
    protected void proprietaryType(
            int fieldRef,
            @ParserContext("data") NMEAObserver data,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        data.setProprietaryType(reader, fieldRef);
    }
    @Rule
    protected void proprietaryData()
    {
    }
    @Rule("string")
    protected void proprietaryData(
            int fieldRef,
            @ParserContext("data") NMEAObserver data,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader)
    {
        data.setProprietaryData(reader, fieldRef);
    }
    @Rule
    protected void targetName()
    {
    }
    @Rule("string")
    protected void targetName(
            int name, 
            @ParserContext("data") NMEAObserver data,
            @ParserContext(ParserConstants.INPUTREADER) InputReader input)
    {
        data.setTargetName(input, name);
    }
    @Rule
    protected void message()
    {
    }
    @Rule("string")
    protected void message(
            int message, 
            @ParserContext("data") NMEAObserver data,
            @ParserContext(ParserConstants.INPUTREADER) InputReader input)
    {
        data.setMessage(input, message);
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
            @ParserContext("aisContext") AISContext aisContext
            ) throws IOException
    {
        AISObserver aisData = aisContext.getAisData();
        aisData.setPrefix(
            numberOfSentences,
            sentenceNumber,
            sequentialMessageID,
            channel
                );
        if (sentenceNumber == 1)
        {
            aisContext.setNumberOfMessages(numberOfSentences);
            aisContext.switchTo(0);
        }
        else
        {
            aisContext.switchTo(aisContext.getLast());
        }
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
        data.setVelocityToWaypoint(velocityToWaypoint, unit);
    }

    @Rule("decimal c letter")
    protected void windDirection(
            float windDirection,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWindDirection(windDirection, unit);
    }

    @Rule("decimal c letter")
    protected void waterHeading(
            float waterHeading,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaterHeading(waterHeading, unit);
    }

    @Rule("decimal c letter")
    protected void waterSpeed(
            float waterSpeed,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaterSpeed(waterSpeed, unit);
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

    @Rule("integer")
    protected void rpm(
            int rpm,
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
        data.setWindAngle(windAngle, unit);
    }

    @Rule("decimal c letter")
    protected void windSpeed(
            float windSpeed,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWindSpeed(windSpeed, unit);
    }

    @Rule("decimal c letter")
    protected void waterTemperature(
            float waterTemperature,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaterTemperature(waterTemperature, unit);
    }

    @Rule("decimal c letter")
    protected void heading(
            float heading,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setHeading(heading, unit);
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
        data.setDepthOfWater(depth, offset);
    }

    @Rule("stringList")
    protected void waypoints(
            List<Integer> list,
            @ParserContext("data") NMEAObserver data,
            @ParserContext(ParserConstants.INPUTREADER) InputReader input)
    {
        data.setWaypoints(input, list);
    }

    @Rule("string")
    protected List<Integer> stringList(int fieldRef)
    {
        List<Integer> list = new ArrayList<>();
        list.add(fieldRef);
        return list;
    }

    @Rule("stringList c string")
    protected List<Integer> stringList(List<Integer> list, int fieldRef)
    {
        list.add(fieldRef);
        return list;
    }

    @Rule("string")
    protected void horizontalDatum(
            int horizontalDatum,
            @ParserContext("data") NMEAObserver data,
            @ParserContext(ParserConstants.INPUTREADER) InputReader input)
    {
        data.setHorizontalDatum(input, horizontalDatum);
    }

    @Rule("c letter")
    protected void faaModeIndicator(
            char faaModeIndicator,
            @ParserContext("data") NMEAObserver data)
    {
        data.setFAAModeIndicator(faaModeIndicator);
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
        data.setDistanceToWaypoint(distanceToWaypoint, units);
    }

    @Rule("decimal c letter")
    protected void depthBelowTransducer(
            float depth,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthBelowTransducer(depth, unit);
    }

    @Rule("decimal c letter")
    protected void depthBelowSurface(
            float depth,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthBelowSurface(depth, unit);
    }

    @Rule("decimal c letter")
    protected void depthBelowKeel(
            float depth,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthBelowKeel(depth, unit);
    }

    @Rule("decimal")
    protected void f1ClockParameter(
            float f1ClockParameter,
            @ParserContext("data") NMEAObserver data)
    {
        data.setF1ClockParameter(f1ClockParameter);
    }

    @Rule("decimal")
    protected void f0ClockParameter(
            float f0ClockParameter,
            @ParserContext("data") NMEAObserver data)
    {
        data.setF0ClockParameter(f0ClockParameter);
    }

    @Rule("decimal")
    protected void meanAnomaly(
            float meanAnomaly,
            @ParserContext("data") NMEAObserver data)
    {
        data.setMeanAnomaly(meanAnomaly);
    }

    @Rule("decimal")
    protected void longitudeOfAscensionNode(
            float longitudeOfAscensionNode,
            @ParserContext("data") NMEAObserver data)
    {
        data.setLongitudeOfAscensionNode(longitudeOfAscensionNode);
    }

    @Rule("decimal")
    protected void argumentOfPerigee(
            float argumentOfPerigee,
            @ParserContext("data") NMEAObserver data)
    {
        data.setArgumentOfPerigee(argumentOfPerigee);
    }

    @Rule("decimal")
    protected void rootOfSemiMajorAxis(
            float rootOfSemiMajorAxis,
            @ParserContext("data") NMEAObserver data)
    {
        data.setRootOfSemiMajorAxis(rootOfSemiMajorAxis);
    }

    @Rule("decimal")
    protected void rateOfRightAscension(
            float rateOfRightAscension,
            @ParserContext("data") NMEAObserver data)
    {
        data.setRateOfRightAscension(rateOfRightAscension);
    }

    @Rule("decimal")
    protected void inclinationAngle(
            float inclinationAngle,
            @ParserContext("data") NMEAObserver data)
    {
        data.setInclinationAngle(inclinationAngle);
    }

    @Rule("decimal")
    protected void almanacReferenceTime(
            float almanacReferenceTime,
            @ParserContext("data") NMEAObserver data)
    {
        data.setAlmanacReferenceTime(almanacReferenceTime);
    }

    @Rule("decimal")
    protected void eccentricity(
            float eccentricity,
            @ParserContext("data") NMEAObserver data)
    {
        data.setEccentricity(eccentricity);
    }

    @Rule("integer")
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
        data.setArrivalCircleRadius(arrivalCircleRadius, units);
    }

    @Rule("decimal c decimal")
    protected void timeDifference(
            float timeDifferenceA, // uS
            float timeDifferenceB, // uS
            @ParserContext("data") NMEAObserver data)
    {
        data.setTimeDifference(timeDifferenceA, timeDifferenceB);
    }

    @Rule("string")
    protected void waypoint(
            int waypoint,
            @ParserContext("data") NMEAObserver data,
            @ParserContext(ParserConstants.INPUTREADER) InputReader input)
    {
        data.setWaypoint(input, waypoint);
    }

    @Rule("decimal")
    protected void utc(
            float utc, // hhmmss.ss
            @ParserContext("clock") Clock clock)
    {
        clock.setTime(utc);
    }

    @Rule("integer")
    protected void date(
            int date, // ddmmyy
            @ParserContext("clock") Clock clock)
    {
        clock.setDate(date);
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

    @Rule("integer")
    protected void ageOfDifferentialGPSData(
            int ageOfDifferentialGPSData, //time in seconds since last SC104 type 1 or 9 update, null field when DGPS is not used
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
        data.setGeoidalSeparation(geoidalSeparation, unitsOfGeoidalSeparation);
    }

    @Rule("decimal c letter")
    protected void antennaAltitude(
            float antennaAltitude, // above/below mean-sea-level (geoid) (in meters)
            char unitsOfAntennaAltitude, //meters
            @ParserContext("data") NMEAObserver data)
    {
        data.setAntennaAltitude(antennaAltitude, unitsOfAntennaAltitude);
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
        data.setBearing(bearing, unit);
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
        data.setBearingOriginToDestination(bearingOriginToDestination, mOrT);
    }

    @Rule("decimal c letter")
    protected void bearingPresentPositionToDestination(
            float bearingPresentPositionToDestination, // degrees
            char mOrT, // M = Magnetic, T = True
            @ParserContext("data") NMEAObserver data)
    {
        data.setBearingPresentPositionToDestination(bearingPresentPositionToDestination, mOrT);
    }

    @Rule("decimal c letter")
    protected void headingToSteerToDestination(
            float headingToSteerToDestination, // degrees
            char mOrT, // M = Magnetic, T = True
            @ParserContext("data") NMEAObserver data)
    {
        data.setHeadingToSteerToDestination(headingToSteerToDestination, mOrT);
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
            int toWaypoint,
            int fromWaypoint,
            @ParserContext("data") NMEAObserver data,
            @ParserContext(ParserConstants.INPUTREADER) InputReader input)
    {
        data.setWaypointToWaypoint(input, toWaypoint, fromWaypoint);
    }

    @Rule("decimal c letter c letter")
    protected void crossTrackError(
            float crossTrackError, // NM
            char directionToSteer,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setCrossTrackError(crossTrackError, directionToSteer, unit);
    }

    @Rule("decimal c letter")
    protected void crossTrackErrorNM(
            float crossTrackError, // NM
            char directionToSteer,
            @ParserContext("data") NMEAObserver data)
    {
        data.setCrossTrackError(crossTrackError, directionToSteer, 'N');
    }

    @Rule("c")
    protected void magneticVariation()
    {
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

    @Rule("latitude c ns c longitude c ew")
    protected void location(
            double latitude,
            int ns,
            double longitude,
            int ew,
            @ParserContext("data") NMEAObserver data)
    {
        data.setLocation(ns * latitude, ew * longitude);
    }

    @Rule("latitude c ns c longitude c ew")
    protected void destinationWaypointLocation(
            double latitude,
            int ns,
            double longitude,
            int ew,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDestinationWaypointLocation(ns * latitude, ew * longitude);
    }

    @Rule("letterNotP letter")
    protected void talkerId(char c1, char c2, @ParserContext("data") NMEAObserver data)
    {
        data.talkerId(c1, c2);
    }

    @Rule("hexAlpha hexAlpha")
    protected void checksum(
            char x1,
            char x2,
            @ParserContext("checksum") Checksum checksum,
            @ParserContext("clock") Clock clock,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("aisContext") AISContext aisContext
            )
    {
        AISObserver aisData = aisContext.getAisData();
        int sum = 16 * parseHex(x1) + parseHex(x2);
        if (sum != checksum.getValue())
        {
            clock.rollback();
            String reason = "checksum " + Integer.toHexString(sum) + " != " + Integer.toHexString((int) checksum.getValue());
            data.rollback(reason);
            aisData.rollback(reason);
        }
        else
        {
            clock.commit();
            String reason = Integer.toHexString(sum);
            data.commit(reason);
            aisData.commit(reason);
        }
    }

    @Terminal(expression = "[0-9]+\\.[0-9]+")
    protected double latitude(double lat)
    {
        double degrees = (double) Math.floor(lat / 100);
        double minutes = lat - 100F * degrees;
        double latitude = degrees + minutes / 60F;
        assert latitude >= 0;
        assert latitude <= 90;
        return latitude;
    }

    @Terminal(expression = "[0-9]+\\.[0-9]+")
    protected double longitude(double lat)
    {
        double degrees = (double) Math.floor(lat / 100);
        double minutes = lat - 100F * degrees;
        double longitude = degrees + minutes / 60F;
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

    @Terminal(expression = "[a-zA-OQ-Z]")
    protected abstract char letterNotP(char c);

    @Terminal(expression = "[0-9A-Fa-f]")
    protected abstract char hexAlpha(char x);

    @Terminal(expression = "[a-zA-Z0-9 \\.\\-\\(\\)]+")
    protected int string(InputReader input)
    {
        return input.getFieldRef();
    }

    @Terminal(expression = "[\\+\\-]?[0-9]+")
    protected abstract int integer(int i);

    @Terminal(expression = "[\\+\\-]?[0-9]+(\\.[0-9]+)*")
    protected abstract float decimal(float f);

    @Terminal(expression = "[\\,]")
    protected abstract void c();

    private static final int parseHex(char x)
    {
        switch (x)
        {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return x - '0';
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return x - 'A' + 10;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return x - 'a' + 10;
            default:
                throw new IllegalArgumentException(x + " is not hex digit");
        }
    }

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

    public void parse(InputStream is, NMEAObserver data, AISObserver aisData) throws IOException
    {
        Checksum checksum = new NMEAChecksum();
        Clock clock = new GPSClock();
        data.setClock(clock);
        CheckedInputStream checkedInputStream = new CheckedInputStream(is, checksum);
        AISContext aisContext = new AISContext(checkedInputStream, aisData);
        parse(checkedInputStream, checksum, clock, data, aisContext);
        aisContext.stopThreads();
    }

    @ParseMethod(start = "statements", size = 1024, charSet="ASCII", wideIndex=true)
    protected abstract void parse(
            InputStream is,
            @ParserContext("checksum") Checksum checksum,
            @ParserContext("clock") Clock clock,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("aisContext") AISContext aisContext
            ) throws IOException;

    public static NMEAParser newInstance() throws NoSuchMethodException, IOException, NoSuchFieldException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        return (NMEAParser) GenClassFactory.getGenInstance(NMEAParser.class);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            String pck = AISParser.class.getPackage().getName().replace('.', '/') + "/";
            InputStream is = AISParser.class.getClassLoader().getResourceAsStream(pck + "nmea-sample");
            FileInputStream fis = new FileInputStream("C:\\Users\\tkv\\Documents\\NetBeansProjects\\Parsers\\src\\org\\vesalainen\\parsers\\nmea\\ais\\nmea-sample_1");
            NMEAParser p = NMEAParser.newInstance();
            p.parse(fis, new AbstractNMEAObserver(), new VesselMonitor());
            System.err.println("nmea finished");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
