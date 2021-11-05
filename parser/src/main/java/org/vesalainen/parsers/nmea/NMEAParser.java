
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
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.zip.Checksum;
import org.vesalainen.lang.Primitives;
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
import static org.vesalainen.parsers.nmea.Converter.*;
import org.vesalainen.parsers.nmea.ais.AISBridge;
import org.vesalainen.parsers.nmea.ais.AISObserver;
import org.vesalainen.parsers.nmea.time.GPSClock;
import org.vesalainen.util.CharSequences;

/**
 * @author Timo Vesalainen
 * @see <a href="http://catb.org/gpsd/NMEA.html">NMEA Revealed</a>
 * @see <a href="http://catb.org/gpsd/AIVDM.html">AIVDM/AIVDO protocol decoding</a>
 * @see <a href="http://www.eye4software.com/hydromagic/documentation/nmea0183/">Professional hydrographic survey software</a>
 * @see <a href="doc-files/NMEAParser-statements.html#BNF">BNF Syntax for NMEA</a>
 */
// TODO grammar size is nearing 64k
@GenClassname("org.vesalainen.parsers.nmea.NMEAParserImpl")
@GrammarDef()
@Rules(
{
    @Rule(left = "statements", value = "statement*"),
    @Rule(left = "statement", value = "nmeaStatement"),
    @Rule(left = "nmeaStatement", value = "'\\$' talkerId nmeaSentence '[\\,]*\\*' checksum '\r\n'"),
    @Rule(left = "nmeaStatement", value = "'\\$P' proprietaryType c proprietaryData '[\\,]*\\*' checksum '\r\n'"),
    @Rule(left = "nmeaStatement", value = "aisMessage"),
    @Rule(left = "nmeaSentence", value = "aam c arrivalStatus c waypointStatus c arrivalCircleRadius c waypoint"),
    @Rule(left = "nmeaSentence", value = "alm c totalNumberOfMessages c messageNumber c satellitePRNNumber c gpsWeekNumber c svHealth c eccentricity c almanacReferenceTime c inclinationAngle c rateOfRightAscension c rootOfSemiMajorAxis c argumentOfPerigee c longitudeOfAscensionNode c meanAnomaly c f0ClockParameter c f1ClockParameter"),
    @Rule(left = "nmeaSentence", value = "apa apaapb"),
    @Rule(left = "nmeaSentence", value = "apb apaapb c bearingPresentPositionToDestination c headingToSteerToDestination"),
    @Rule(left = "apaapb", value = "c status c status2 c crossTrackError c arrivalStatus c waypointStatus c bearingOriginToDestination c waypoint"),
    @Rule(left = "nmeaSentence", value = "bod c bearing c bearing c waypointToWaypoint"),
    @Rule(left = "nmeaSentence", value = "bec becbwcbwr"),
    @Rule(left = "nmeaSentence", value = "bwc becbwcbwr faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "bwr becbwcbwr"),
    @Rule(left = "becbwcbwr", value = "c utc c location c bearing c bearing c distanceToWaypoint c waypoint"),
    @Rule(left = "nmeaSentence", value = "bww c bearing c bearing c waypointToWaypoint"),
    @Rule(left = "nmeaSentence", value = "dbk c depthBelowKeel"),
    @Rule(left = "nmeaSentence", value = "dbs c depthBelowSurface"),
    @Rule(left = "nmeaSentence", value = "dbt c depthBelowTransducer"),
    @Rule(left = "nmeaSentence", value = "dpt c depthOfWater"),
    @Rule(left = "nmeaSentence", value = "gga c utc c location c gpsQualityIndicator c numberOfSatellitesInView c horizontalDilutionOfPrecision c antennaAltitude c geoidalSeparation c ageOfDifferentialGPSData c differentialReferenceStationID"),
    @Rule(left = "nmeaSentence", value = "gll c location c utc c status faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "gsa c selectionMode c mode c sat1? c sat2? c sat3? c sat4? c sat5? c sat6? c sat7? c sat8? c sat9? c sat10? c sat11? c sat12? c pdop c hdop c vdop"),
    @Rule(left = "nmeaSentence", value = "gsv c totalNumberOfMessages c messageNumber c totalNumberOfSatellitesInView (c prn c elevation c azimuth c snr)+"),
    @Rule(left = "nmeaSentence", value = "hdg c magneticHeading c magneticDeviation c magneticVariation"),
    @Rule(left = "nmeaSentence", value = "hdm c heading"),
    @Rule(left = "nmeaSentence", value = "hdt c heading"),
    @Rule(left = "nmeaSentence", value = "mtw c waterTemperature"),
    @Rule(left = "nmeaSentence", value = "mwv c windAngleSpeed c status"),
    @Rule(left = "nmeaSentence", value = "r00 c waypoints"),
    @Rule(left = "nmeaSentence", value = "rma c status c location c timeDifference c speedOverGround c trackMadeGood c magneticVariation"),
    @Rule(left = "nmeaSentence", value = "rmb c status c crossTrackErrorNM c waypointToWaypoint c destinationWaypointLocation c rangeToDestination c bearingToDestination c destinationClosingVelocity c arrivalStatus faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "rmc c utc c status c location c speedOverGround c trackMadeGood c date c magneticVariation faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "rot c rateOfTurn c status"),
    @Rule(left = "nmeaSentence", value = "rpm c rpmSource c rpmSourceNumber c rpm c propellerPitch c status"),
    @Rule(left = "nmeaSentence", value = "rsa c starboardRudderSensor c status c portRudderSensor c status2"),
    @Rule(left = "nmeaSentence", value = "rte c totalNumberOfMessages c messageNumber c messageMode c route c waypoints"),
    @Rule(left = "nmeaSentence", value = "ths c trueHeading c status"),
    @Rule(left = "nmeaSentence", value = "tll c targetNumber c destinationWaypointLocation c targetName c targetTime c targetStatus c referenceTarget"),
    @Rule(left = "nmeaSentence", value = "ttm c targetNumber c targetDistance c bearingFromOwnShip c bearingUnit c targetSpeed c targetCourse c courseUnit c distanceOfCPA c timeToCPA c distanceUnit c targetName c targetStatus c referenceTarget"),
    @Rule(left = "nmeaSentence", value = "txt c totalNumberOfMessages c messageNumber c targetName c message"),
    @Rule(left = "nmeaSentence", value = "vhw c waterHeading c waterHeading c waterSpeed c waterSpeed"),
    @Rule(left = "nmeaSentence", value = "vlw c waterDistance c waterDistanceSinceReset"),
    @Rule(left = "nmeaSentence", value = "vtg c track c track c speed c speed faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "vtg c trueCourseOverGround c magneticCourseOverGround c speedOverGroundKnots c speedOverGroundKilometers"),
    @Rule(left = "nmeaSentence", value = "vwr c relativeWindDirection c relativeWindSpeed c relativeWindSpeed c relativeWindSpeed"),
    @Rule(left = "nmeaSentence", value = "wcv c velocityToWaypoint c waypoint"),
    @Rule(left = "nmeaSentence", value = "wnc c distanceToWaypoint c distanceToWaypoint c waypointToWaypoint"),
    @Rule(left = "nmeaSentence", value = "wpl c destinationWaypointLocation c waypoint"),
    @Rule(left = "nmeaSentence", value = "xdr xdrGroup+"),
    @Rule(left = "nmeaSentence", value = "xte c status c status2 c crossTrackError faaModeIndicator"),
    @Rule(left = "nmeaSentence", value = "xtr c crossTrackError"),
    @Rule(left = "nmeaSentence", value = "zda c utc c day c month c year c localZoneHours c localZoneMinutes"),
    @Rule(left = "xdrGroup", value = "ptch"),
    @Rule(left = "xdrGroup", value = "roll"),
    @Rule(left = "xdrGroup", value = "xacc"),
    @Rule(left = "xdrGroup", value = "yacc"),
    @Rule(left = "xdrGroup", value = "zacc"),
    @Rule(left = "xdrGroup", value = "rrat"),
    @Rule(left = "xdrGroup", value = "prat"),
    @Rule(left = "xdrGroup", value = "yrat"),
    @Rule(left = "xdrGroup", value = "rrtr"),
    @Rule(left = "xdrGroup", value = "prtr"),
    @Rule(left = "xdrGroup", value = "yrtr"),
    @Rule(left = "trueHeading"),
    @Rule(left = "rateOfTurn"),
    @Rule(left = "waterTemperature"),
    @Rule(left = "heading"),
    @Rule(left = "magneticHeading"),
    @Rule(left = "magneticDeviation", value="c skip?"),
    @Rule(left = "faaModeIndicator"),
    @Rule(left = "messageMode"),
    @Rule(left = "distanceToWaypoint", value="c skip?"),
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
    @Rule(left = "relativeWindSpeed"),
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
    @Rule(left = "relativeWindDirection", value = "c skip?"),
    @Rule(left = "waterHeading", value = "c skip?"),
    @Rule(left = "waterDistance", value = "c skip?"),
    @Rule(left = "waterDistanceSinceReset", value = "c skip?"),
    @Rule(left = "waterSpeed", value = "c skip?"),
    @Rule(left = "track", value = "c skip?"),
    @Rule(left = "speed", value = "c skip?"),
    @Rule(left = "bearingUnit"),
    @Rule(left = "windAngle")
})
public abstract class NMEAParser extends NMEATalkerIds implements ParserInfo, ChecksumProvider
{
    public NMEAParser()
    {
        setLogger(this.getClass());
    }
    
    @Rule("c letter c decimal c letter c 'PTCH'")
    protected void ptch(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setPitch(value);
    }
    @Rule("c letter c decimal c letter c 'ROLL'")
    protected void roll(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setRoll(value);
    }
    @Rule("c letter c decimal c letter c 'XACC'")
    protected void xacc(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setXAcceleration(value);
    }
    @Rule("c letter c decimal c letter c 'YACC'")
    protected void yacc(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setYAcceleration(value);
    }
    @Rule("c letter c decimal c letter c 'ZACC'")
    protected void zacc(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setZAcceleration(value);
    }
    @Rule("c letter c decimal c letter c 'RRAT'")
    protected void rrat(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setRRat(value);
    }
    @Rule("c letter c decimal c letter c 'PRAT'")
    protected void prat(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setPRat(value);
    }
    @Rule("c letter c decimal c letter c 'YRAT'")
    protected void yrat(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setYRat(value);
    }
    @Rule("c letter c decimal c letter c 'RRTR'")
    protected void rrtr(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setRRtr(value);
    }
    @Rule("c letter c decimal c letter c 'PRTR'")
    protected void prtr(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setPRtr(value);
    }
    @Rule("c letter c decimal c letter c 'YRTR'")
    protected void yrtr(
            char type,
            float value,
            char unit,
            @ParserContext("data") NMEAObserver data
    )
    {
        data.setYRtr(value);
    }
    @Rule("aivdm")
    @Rule("aivdo")
    protected abstract boolean aisOwnMessage(boolean ownMessage);

    @Rule("'!AIVDM'")
    protected boolean aivdm()
    {
        return false;
    }

    @Rule("'!AIVDO'")
    protected boolean aivdo()
    {
        return true;
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
            List<CharSequence> pdata,
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
        return '-';
    }

    @Rule("letter")
    protected char channel(char cc)
    {
        return cc;
    }

    @Terminal(expression="[0-W`-w]+")
    protected CharSequence aisPayload(CharSequence data)
    {
        return new StringBuilder(data);
    }

    @Rule("aisOwnMessage c integer c integer c sequentialMessageID c channel c aisPayload c integer '\\*' hex '\r\n'")
    protected void aisMessage(
            boolean ownMessage,
            int numberOfSentences,
            int sentenceNumber,
            int sequentialMessageID,
            char channel,
            CharSequence payload,
            int padding,
            int checksum,
            @ParserContext(ParserConstants.InputReader) InputReader input,
            @ParserContext("aisBridge") AISBridge aisBridge
            ) throws IOException
    {
        if (aisBridge != null)
        {
            aisBridge.newSentence(
                    ownMessage, 
                    numberOfSentences, 
                    sentenceNumber,
                    sequentialMessageID,
                    channel,
                    payload,
                    padding,
                    checksum,
                    input.getChecksum().getValue()
            );
        }
    }

    @Rule("integer")
    protected void day(
            int day,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("clock") NMEAClock clock)
    {
        clock.setDay(day);
        data.setDay(day);
    }

    @Rule("integer")
    protected void month(
            int month,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("clock") NMEAClock clock)
    {
        clock.setMonth(month);
        data.setMonth(month);
    }

    @Rule("integer")
    protected void year(
            int year,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("clock") NMEAClock clock)
    {
        clock.setYear(year);
        data.setYear(year);
    }

    @Rule("integer")
    protected void localZoneHours(
            int localZoneHours,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("clock") NMEAClock clock)
    {
        data.setLocalZoneHours(localZoneHours);
    }

    @Rule("integer")
    protected void localZoneMinutes(
            int localZoneMinutes,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("clock") NMEAClock clock)
    {
        data.setLocalZoneMinutes(localZoneMinutes);
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
    protected void relativeWindDirection(
            float windDirection,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        float wd = leftOrRight(windDirection, unit);
        if (wd >= 0)
        {
            data.setRelativeWindAngle(wd);
        }
        else
        {
            data.setRelativeWindAngle(wd+360);
        }
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

    @Rule("decimal c letter")
    protected void waterDistance(
            float waterDistance,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaterDistance(toNauticalMiles(waterDistance, unit));
    }

    @Rule("decimal c letter")
    protected void waterDistanceSinceReset(
            float waterDistanceSinceReset,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaterDistanceSinceReset(toNauticalMiles(waterDistanceSinceReset, unit));
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

    @Rule("decimal c letter c decimal c letter")
    protected void windAngleSpeed(
            float windAngle,
            char tr,
            float windSpeed,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        switch (tr)
        {
            case 'T':
                data.setTrueWindAngle(windAngle);
                data.setTrueWindSpeed(toKnots(windSpeed, unit));
                break;
            case 'R':
                data.setRelativeWindAngle(windAngle);
                data.setRelativeWindSpeed(toKnots(windSpeed, unit));
                break;
            default:
                throw new IllegalArgumentException(tr+ "expected T/R");
        }
    }

    @Rule("decimal c letter")
    protected void relativeWindSpeed(
            float windSpeed,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setRelativeWindSpeed(toKnots(windSpeed, unit));
    }

    @Rule("decimal c letter")
    protected void waterTemperature(
            float waterTemperature,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setWaterTemperature(toCelsius(waterTemperature, unit));
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
                throw new IllegalArgumentException(unit+ " expected T/M");
        }
    }

    @Rule("decimal")
    protected void trueHeading(
            float heading,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTrueHeading(heading);
    }

    @Rule("decimal")
    protected void magneticHeading(
            float magneticHeading,
            @ParserContext("data") NMEAObserver data)
    {
        data.setMagneticHeading(magneticHeading);
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

    @Rule("decimal c decimal c decimal")
    protected void depthOfWater(
            float depth,
            float offset,
            float scale,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthOfWater(depth);
        data.setDepthOffsetOfWater(offset);
        data.setMaximumRangeScale(scale);
    }

    @Rule("stringList")
    protected void waypoints(
            List<CharSequence> list,
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
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDistanceToWaypoint(toKnots(distanceToWaypoint, unit));
    }

    @Rule("depthBelowTransducerValue c depthBelowTransducerValue c depthBelowTransducerValue")
    protected void depthBelowTransducer(
            float d1,
            float d2,
            float d3,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthBelowTransducer(firstValid(d1, d2, d3));
    }

    @Rule("decimal c letter")
    protected float depthBelowTransducerValue(
            float depth,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        return toMeters(depth, unit);
    }

    @Rule("c skip?")
    protected float depthBelowSurfaceValue()
    {
        return Float.NaN;
    }

    @Rule("c skip?")
    protected float depthBelowKeelValue()
    {
        return Float.NaN;
    }

    @Rule("c skip?")
    protected float depthBelowTransducerValue()
    {
        return Float.NaN;
    }

    @Rule("depthBelowSurfaceValue c depthBelowSurfaceValue c depthBelowSurfaceValue")
    protected void depthBelowSurface(
            float d1,
            float d2,
            float d3,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthBelowSurface(firstValid(d1, d2, d3));
    }

    @Rule("decimal c letter")
    protected float depthBelowSurfaceValue(
            float depth,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        return toMeters(depth, unit);
    }

    @Rule("depthBelowKeelValue c depthBelowKeelValue c depthBelowKeelValue")
    protected void depthBelowKeel(
            float d1,
            float d2,
            float d3,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDepthBelowKeel(firstValid(d1, d2, d3));
    }
    @Rule("decimal c letter")
    protected float depthBelowKeelValue(
            float depth,
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        return toMeters(depth, unit);
    }
    private float firstValid(float... values)
    {
        for (float v : values)
        {
            if (!Float.isNaN(v))
            {
                return v;
            }
        }
        return 0;
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
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setArrivalCircleRadius(toNauticalMiles(arrivalCircleRadius, unit));
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

    @Rule("decimal")
    protected void targetDistance(
            float distance,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTargetDistance(distance);
    }

    @Rule("decimal")
    protected void bearingFromOwnShip(
            float bearing,
            @ParserContext("data") NMEAObserver data)
    {
        data.setBearingFromOwnShip(bearing);
    }

    @Rule("letter")
    protected void bearingUnit(
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setBearingUnit(unit);
    }

    @Rule("decimal")
    protected void targetSpeed(
            float speed,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTargetSpeed(speed);
    }

    @Rule("letter")
    protected void targetStatus(
            char status,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTargetStatus(status);
    }
    
    @Rule("decimal")
    protected void targetCourse(
            float course,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTargetCourse(course);
    }

    @Rule("letter")
    protected void courseUnit(
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setCourseUnit(unit);
    }
    
    @Rule("decimal")
    protected void distanceOfCPA(
            float distance,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDistanceOfCPA(distance);
    }

    @Rule("decimal")
    protected void timeToCPA(
            float time,
            @ParserContext("data") NMEAObserver data)
    {
        data.setTimeToCPA(time);
    }

    @Rule("wsp")
    protected void distanceOfCPA()
    {   // OpenCPN send ' '
    }

    @Rule("wsp")
    protected void timeToCPA()
    {   // OpenCPN send ' '
    }

    @Rule("letter")
    protected void distanceUnit(
            char unit,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDistanceUnit(unit);
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
            @ParserContext("data") NMEAObserver data,
            @ParserContext("clock") NMEAClock clock)
    {
        int s = (int) second;
        clock.setTime(hour, minute, s, (int)((second - (float)s)*1000));
        data.setEpochMillis(((Clock)clock).millis());
        data.setHour(hour);
        data.setMinute(minute);
        data.setSecond(second);
    }

    @Rule("digit2 digit2 digit2")
    protected void date(
            int day,
            int month,
            int year,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("clock") NMEAClock clock)
    {
        clock.setDate(year, month, day);
        data.setDay(day);
        data.setMonth(month);
        data.setYear(year);
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
            float trackMadeGood, // degrees
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

    @Rule("coordinate c ns c coordinate c ew")
    protected void location(
            double latitude,
            int ns,
            double longitude,
            int ew,
            @ParserContext("data") NMEAObserver data)
    {
        data.setLatitude(ns * latitude);
        data.setLongitude(ew * longitude);
    }

    @Rule("coordinate c ns c coordinate c ew")
    protected void destinationWaypointLocation(
            double latitude,
            int ns,
            double longitude,
            int ew,
            @ParserContext("data") NMEAObserver data)
    {
        data.setDestinationWaypointLatitude(ns * latitude);
        data.setDestinationWaypointLongitude(ew * longitude);
    }

    @Rule("hex")
    protected void checksum(
            int sum,
            @ParserContext(ParserConstants.InputReader) InputReader input,
            @ParserContext("clock") NMEAClock clock,
            @ParserContext("origin") Supplier origin,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("aisBridge") AISBridge aisBridge
            )
    {
        Checksum checksum = input.getChecksum();
        if (sum != checksum.getValue())
        {
            clock.rollback("checksum");
            Object org = origin != null ? origin.get() : null;
            String reason = org+" "+input.getLineNumber()+": checksum " + Integer.toHexString(sum) + " != " + Integer.toHexString((int) checksum.getValue());
            data.rollback(reason);
            warning(reason);
        }
        else
        {
            clock.commit("ok");
            //String reason = input.getLineNumber()+": "+Integer.toHexString(sum);
            if (origin != null)
            {
                data.setOrigin(origin.get());
            }
            data.commit("ok");
        }
    }

    @Terminal(expression = "[0-9]+\\.[0-9]+")
    protected double coordinate(CharSequence seq)
    {
        int ipnt = CharSequences.indexOf(seq, '.');
        if (ipnt == -1)
        {
            throw new IllegalArgumentException(seq+" illegal coordinate");
        }
        double degrees = Primitives.parseDouble(seq, 0, ipnt-2);
        double minutes = Primitives.parseDouble(seq, ipnt-2, seq.length());
        double latitude = degrees + minutes / 60.0;
        return latitude;
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

    @Terminal(expression = "[a-zA-Z0-9 \\.\\-\\(\\)\\^]+")
    protected String string(CharSequence seq)
    {
        StringBuilder sb = new StringBuilder();
        int len = seq.length();
        for (int ii=0;ii<len;ii++)
        {
            char cc = seq.charAt(ii);
            if (cc == '^')
            {
                if (len-ii < 3)
                {
                    throw new IllegalArgumentException("illegal escape");
                }
                int d1 = Character.digit(seq.charAt(ii+1), 16);
                int d2 = Character.digit(seq.charAt(ii+2), 16);
                sb.append((char)((d1<<4)+d2));
                ii+=2;
            }
            else
            {
                sb.append(cc);
            }
        }
        return sb.toString();
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

    @Terminal(expression = "[ \t]+")
    protected abstract void wsp();

    @RecoverMethod
    public void recover(
            @ParserContext("data") NMEAObserver data,
            @ParserContext("aisBridge") AISBridge aisBridge,
            @ParserContext(ParserConstants.ExpectedDescription) String expected,
            @ParserContext(ParserConstants.INPUTREADER) InputReader reader,
            @ParserContext(ParserConstants.THROWABLE) Throwable thr
            ) throws IOException
    {
        if (thr != null)
        {
            log(Level.SEVERE, thr, "recover exp=%s", expected);
        }
        else
        {
            log(Level.SEVERE, "recover exp=%s", expected);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(reader.getInput());
        sb.append('^');
        int cc = reader.read();
        while (cc != '\n' && cc != -1)
        {
            sb.append((char) cc);
            cc = reader.read();
        }
        String reason = "skipping " + sb+"\nexpected:"+expected;
        data.rollback(reason);
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
        parse(input, true, null, data, aisData);
    }
    public <I> void parse(I input, boolean liveClock, Supplier origin, NMEAObserver data, AISObserver aisData) throws IOException
    {
        parse(input, GPSClock.getInstance(liveClock), origin, data, aisData);
    }
    public <I> void parse(I input, GPSClock gpsClock, Supplier origin, NMEAObserver data, AISObserver aisData) throws IOException
    {
        parse(input, gpsClock, origin, data, aisData, Executors.newCachedThreadPool());
    }
    public <I> void parse(I input, GPSClock gpsClock, Supplier origin, NMEAObserver data, AISObserver aisData, ExecutorService executor) throws IOException
    {
        if (data == null)
        {
            data = new AbstractNMEAObserver();
        }
        data.start(null);
        data.setClock(gpsClock);
        gpsClock.start(null);
        data.commit("Set clock");
        AISBridge aisBridge = null;
        if (aisData != null)
        {
            aisData.start(null);
            aisData.setClock(gpsClock);
            aisData.commit("Set clock");
            aisBridge = new AISBridge(aisData, executor);
        }
        try
        {
            parse(input, gpsClock, origin, data, aisBridge);
        }
        finally
        {
            if (aisBridge != null)
            {
                aisBridge.waitAndStopThreads();
            }
        }
    }
    @ParseMethod(start = "statements", size = 1024, charSet = "US-ASCII",
            features={WideIndex, UseChecksum, UseDirectBuffer}
    )
    protected abstract <I> void parse(
            I input,
            @ParserContext("clock") NMEAClock clock,
            @ParserContext("origin") Supplier origin,
            @ParserContext("data") NMEAObserver data,
            @ParserContext("aisBridge") AISBridge aisBridge
            ) throws IOException;

    public static NMEAParser newInstance()
    {
        return (NMEAParser) GenClassFactory.loadGenInstance(NMEAParser.class);
    }

    @Override
    public int lookaheadLength()
    {
        return 16;
    }

    @Override
    public Checksum createChecksum()
    {
        return new NMEAChecksum();
    }

}
