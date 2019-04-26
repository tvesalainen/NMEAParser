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

import java.time.Clock;
import org.vesalainen.util.Transactional;
import java.util.List;
import org.vesalainen.math.Unit;
import static org.vesalainen.math.UnitType.*;
import static org.vesalainen.parsers.nmea.NMEACategory.*;

/**
 * NMEAObserver is observer class for NMEA data. NMEAParser calls methods of this 
 * interface.
 * 
 * <p>User of NMEAParser is probably only interested in a small subset of the data.
 For this reason creation of Strings is avoided because of performance and GC
 overhead. CharSequence arguments should not be stored. They are mostly valid
 only during the observer method call. Use toString method to convertTo to string.
 
 <p>It is mostly easier to derive your class from AbstractNMEAObserver class.
 * AbstractNMEAObserver has empty methods for all NMEAObserver methods.
 * 
 * <p>Observer methods are called as soon they are found in input. Parsing might
 * cause syntax error or NMEA sentence checksum might fail. In that case rollback
 * is called. Critical application should store the values and use them only after
 * commit. 
 * @author Timo Vesalainen
 * @see org.vesalainen.parsers.nmea.NMEADispatcher
 */
public interface NMEAObserver extends Transactional
{
    /**
     * Set the origin of the data. Like inet address or port name.
     * @param origin 
     */
    void setOrigin(Object origin);
    /**
     * Set the clock
     * @param clock 
     */
    void setClock(Clock clock);
    /**
     * Set taker id of sentence
     * @param talkerId 
     */
    public void setTalkerId(TalkerId talkerId);
    /**
     * Latitude in degrees. BWC, BWR, GGA, GLL, RMA, RMC 
     * @param latitude Latitude. South is negative.
     */
    @NMEACat(Coordinate)
    @Unit(value=DegMin, min=-90, max=90)
    void setLatitude(double latitude);
    /**
     * Longitude in degrees. BWC, BWR, GGA, GLL, RMA, RMC 
     * @param longitude Longitude West is negative.
     */
    @NMEACat(Coordinate)
    @Unit(value=DegMin, min=-180, max=180)
    void setLongitude(double longitude);
    /**
     * RMA, RMC
     * @param knots 
     */
    @NMEACat(Speed)
    @Unit(value=Knot, min=0, max=50)
    void setSpeedOverGround(float knots);
    /**
     * RMA, RMC
     * @param degrees
     */
    @NMEACat(Bearing)
    @Unit(value=Degree, min=0, max=360)
    void setTrackMadeGood(float degrees);
    /**
     * HDG, RMA, RMC
     * @param degrees West is minus
     */
    @NMEACat(Bearing)
    @Unit(value=Degree, min=0, max=360)
    void setMagneticVariation(float degrees);
    /**
     * DirectionToSteer - = left + = right
     * APA, APB, XTR
     * @param nm
     */
    @NMEACat(Bearing)
    @Unit(value=NM, min=-10, max=10)
    void setCrossTrackError(float nm);
    /**
     * BOD, BWW, RMB, WNC
     * @param toWaypoint 
     */
    @NMEACat(Waypoint)
    void setToWaypoint(CharSequence toWaypoint);
    /**
     * BOD, BWW, RMB, WNC
     * @param fromWaypoint 
     */
    @NMEACat(Waypoint)
    void setFromWaypoint(CharSequence fromWaypoint);
    /**
     * RMB, WPL
     * @param latitude 
     */
    @NMEACat(Waypoint)
    @Unit(value=DegMin, min=-90, max=90)
    void setDestinationWaypointLatitude(double latitude);
    /**
     * RMB, WPL
     * @param longitude 
     */
    @NMEACat(Waypoint)
    @Unit(value=DegMin, min=-180, max=180)
    void setDestinationWaypointLongitude(double longitude);
    /**
     * RMB
     * @param nm 
     */
    @NMEACat(Waypoint)
    @Unit(value=NM, min=0, max=10000)
    void setRangeToDestination(float nm);
    /**
     * RMB
     * @param degrees True
     */
    @NMEACat(Waypoint)
    @Unit(value=Degree, min=0, max=360)
    void setBearingToDestination(float degrees);
    /**
     * RMB
     * @param knots 
     */
    @NMEACat(Waypoint)
    @Unit(value=Knot, min=0, max=50)
    void setDestinationClosingVelocity(float knots);
    /**
     * GGA
     * @param gpsQualityIndicator 
     */
    @NMEACat(GPS)
    void setGpsQualityIndicator(GPSQualityIndicator gpsQualityIndicator);
    /**
     * GGA
     * @param numberOfSatellitesInView 
     */
    @NMEACat(GPS)
    void setNumberOfSatellitesInView(int numberOfSatellitesInView);
    /**
     * GGA
     * @param meters 
     */
    @NMEACat(GPS)
    @Unit(value=Meter, min=0, max=50)
    void setHorizontalDilutionOfPrecision(float meters);
    /**
     * GGA
     * @param meters
     */
    @NMEACat(GPS)
    @Unit(value=Meter, min=0, max=10000)
    void setAntennaAltitude(float meters);
    /**
     * GGA
     * @param meters
     */
    @NMEACat(GPS)
    @Unit(Meter)
    void setGeoidalSeparation(float meters);
    /**
     * GGA
     * @param ageOfDifferentialGPSData 
     */
    @NMEACat(GPS)
    void setAgeOfDifferentialGPSData(float ageOfDifferentialGPSData);
    /**
     * GGA
     * @param differentialReferenceStationID 
     */
    @NMEACat(GPS)
    void setDifferentialReferenceStationID(int differentialReferenceStationID);
    /**
     * APA, APB, GLL, MWV, RMA, RMB, RMC, ROT, RSA, XTE
     * @param status 
     */
    @NMEACat(GPS)
    void setStatus(char status);
    /**
     * AAM, APA, APB
     * @param arrivalStatus 
     */
    @NMEACat(Waypoint)
    void setArrivalStatus(char arrivalStatus);
    /**
     * RMA
     * @param timeDifferenceA 
     */
    @NMEACat(GPS)
    void setTimeDifferenceA(float timeDifferenceA);
    /**
     * RMA
     * @param timeDifferenceB 
     */
    @NMEACat(GPS)
    void setTimeDifferenceB(float timeDifferenceB);
    /**
     * AAM, APA, APB
     * @param waypointStatus 
     */
    @NMEACat(Waypoint)
    void setWaypointStatus(char waypointStatus);
    /**
     * AAM
     * @param nm
     */
    @NMEACat(Waypoint)
    @Unit(NM)
    void setArrivalCircleRadius(float nm);
    /**
     * AAM, APA, APB, BWC, BWR, R00, WCV, WPL
     * @param waypoint 
     */
    void setWaypoint(CharSequence waypoint);
    /**
     * ALM, RTE
     * @param totalNumberOfMessages 
     */
    void setTotalNumberOfMessages(int totalNumberOfMessages);
    /**
     * ALM, RTE
     * @param messageNumber 
     */
    void setMessageNumber(int messageNumber);
    /**
     * ALM
     * @param satellitePRNNumber 
     */
    @NMEACat(GPS)
    void setSatellitePRNNumber(int satellitePRNNumber);
    /**
     * ALM
     * @param gpsWeekNumber 
     */
    @NMEACat(GPS)
    void setGpsWeekNumber(int gpsWeekNumber);
    /**
     * ALM
     * @param svHealth 
     */
    @NMEACat(GPS)
    void setSvHealth(int svHealth);
    /**
     * ALM
     * @param eccentricity 
     */
    @NMEACat(GPS)
    void setEccentricity(int eccentricity);
    /**
     * ALM
     * @param almanacReferenceTime 
     */
    @NMEACat(GPS)
    void setAlmanacReferenceTime(int almanacReferenceTime);
    /**
     * ALM
     * @param inclinationAngle 
     */
    @NMEACat(GPS)
    void setInclinationAngle(int inclinationAngle);
    /**
     * ALM
     * @param rateOfRightAscension 
     */
    @NMEACat(GPS)
    void setRateOfRightAscension(int rateOfRightAscension);
    /**
     * ALM
     * @param rootOfSemiMajorAxis 
     */
    @NMEACat(GPS)
    void setRootOfSemiMajorAxis(int rootOfSemiMajorAxis);
    /**
     * ALM
     * @param argumentOfPerigee 
     */
    @NMEACat(GPS)
    void setArgumentOfPerigee(int argumentOfPerigee);
    /**
     * ALM
     * @param longitudeOfAscensionNode 
     */
    @NMEACat(GPS)
    void setLongitudeOfAscensionNode(int longitudeOfAscensionNode);
    /**
     * ALM
     * @param meanAnomaly 
     */
    @NMEACat(GPS)
    void setMeanAnomaly(int meanAnomaly);
    /**
     * ALM
     * @param f0ClockParameter 
     */
    @NMEACat(GPS)
    void setF0ClockParameter(int f0ClockParameter);
    /**
     * ALM
     * @param f1ClockParameter 
     */
    @NMEACat(GPS)
    void setF1ClockParameter(int f1ClockParameter);
    /**
     * APA, APB, RSA, XTE
     * @param status 
     */
    void setStatus2(char status);
    /**
     * APA, APB
     * @param bearingOriginToDestination 
     */
    @NMEACat(Waypoint)
    @Unit(value=Degree, min=0, max=360)
    void setMagneticBearingOriginToDestination(float bearingOriginToDestination);
    /**
     * APA, APB
     * @param bearingOriginToDestination 
     */
    @NMEACat(Waypoint)
    @Unit(value=Degree, min=0, max=360)
    void setTrueBearingOriginToDestination(float bearingOriginToDestination);
    /**
     * APB
     * @param bearingPresentPositionToDestination 
     */
    @NMEACat(Waypoint)
    @Unit(value=Degree, min=0, max=360)
    void setMagneticBearingPresentPositionToDestination(float bearingPresentPositionToDestination);
    /**
     * APB
     * @param bearingPresentPositionToDestination 
     */
    @NMEACat(Waypoint)
    @Unit(value=Degree, min=0, max=360)
    void setTrueBearingPresentPositionToDestination(float bearingPresentPositionToDestination);
    /**
     * APB
     * @param headingToSteerToDestination 
     */
    @NMEACat(Waypoint)
    @Unit(value=Degree, min=0, max=360)
    void setMagneticHeadingToSteerToDestination(float headingToSteerToDestination);
    /**
     * APB
     * @param headingToSteerToDestination 
     */
    @NMEACat(Waypoint)
    @Unit(value=Degree, min=0, max=360)
    void setTrueHeadingToSteerToDestination(float headingToSteerToDestination);
    /**
     * BWC, GLL, XTE
     * @param faaModeIndicator 
     */
    void setFaaModeIndicator(char faaModeIndicator);
    /**
     * RTE
     * @param messageMode 
     */
    void setMessageMode(char messageMode);
    /**
     * R00, RTE
     * @param list 
     */
    void setWaypoints(List<CharSequence> list);
    /**
     * BWC, BWR WNC
     * @param nm
     */
    @NMEACat(Waypoint)
    @Unit(value=NM, min=0, max=500)
    void setDistanceToWaypoint(float nm);
    /**
     * DBK
     * @param meters
     */
    @NMEACat(Depth)
    @Unit(value=Meter, min=0, max=100)
    void setDepthBelowKeel(float meters);
    /**
     * DBS
     * @param meters
     */
    @NMEACat(Depth)
    @Unit(value=Meter, min=0, max=100)
    void setDepthBelowSurface(float meters);
    /**
     * DBT
     * @param meters
     */
    @NMEACat(Depth)
    @Unit(value=Meter, min=0, max=100)
    void setDepthBelowTransducer(float meters);
    /**
     * BOD, BWC, BWR, BWW
     * @param degrees
     */
    @NMEACat(Bearing)
    @Unit(value=Degree, min=0, max=360)
    void setTrueBearing(float degrees);
    /**
     * BOD, BWC, BWR, BWW
     * @param degrees
     */
    @NMEACat(Bearing)
    @Unit(value=Degree, min=0, max=360)
    void setMagneticBearing(float degrees);
    /**
     * DBT
     * @param meters
     */
    @NMEACat(Depth)
    @Unit(value=Meter, min=0, max=100)
    void setDepthOfWater(float meters);
    /**
     * DBT
     * @param meters
     */
    @NMEACat(Depth)
    @Unit(value=Meter, min=0, max=10)
    void setDepthOffsetOfWater(float meters);
    /**
     * HDG
     * @param magneticDeviation 
     */
    @NMEACat(Bearing)
    @Unit(value=Degree, min=-30, max=30)
    void setMagneticDeviation(float magneticDeviation);
    /**
     * HDT
     * @param degrees
     */
    @NMEACat(Bearing)
    @Unit(value=Degree, min=0, max=360)
    void setTrueHeading(float degrees);
    /**
     * HDM, HDG
     * @param degrees
     */
    @NMEACat(Bearing)
    @Unit(value=Degree, min=0, max=360)
    void setMagneticHeading(float degrees);
    /**
     * MTW
     * @param celcius
     */
    @NMEACat(Temperature)
    @Unit(value=Celsius, min=0, max=40)
    void setWaterTemperature(float celcius);
    /**
     * MWV
     * @param windAngle Wind Angle, 0 to 360 degrees
     */
    @NMEACat(Wind)
    @Unit(value=Degree, min=0, max=360)
    void setRelativeWindAngle(float windAngle);
    /**
     * MWV
     * @param windAngle Wind Angle, 0 to 360 degrees
     */
    @NMEACat(Wind)
    @Unit(value=Degree, min=0, max=360)
    void setTrueWindAngle(float windAngle);
    /**
     * Returns the wind speed.
     * <p>Note! Is it relative or true depends on which setXXXWindAngle was called
     * in same transaction. (before commit)
     * MWV, VWR
     * @param metersInSecond
     */
    @NMEACat(Wind)
    @Unit(value=MS, min=0, max=100)
    void setWindSpeed(float metersInSecond);
    /**
     * ROT
     * Rate Of Turn, degrees per minute, "-" means bow turns to port
     * @param rateOfTurn 
     */
    @NMEACat(Turn)
    void setRateOfTurn(float rateOfTurn);
    /**
     * RPM
     * Sourse, S = Shaft, E = Engine
     * @param rpmSource 
     */
    @NMEACat(RPM)
    void setRpmSource(char rpmSource);
    /**
     * RPM
     * Engine or shaft number
     * @param rpmSourceNumber 
     */
    @NMEACat(RPM)
    void setRpmSourceNumber(int rpmSourceNumber);
    /**
     * RPM
     * Speed, Revolutions per minute
     * @param rpm 
     */
    @NMEACat(RPM)
    void setRpm(float rpm);
    /**
     * RPM
     * Propeller pitch, % of maximum, "-" means astern
     * @param propellerPitch 
     */
    void setPropellerPitch(float propellerPitch);
    /**
     * RSA
     * Starboard (or single) rudder sensor, "-" means Turn To Port
     * @param starboardRudderSensor 
     */
    void setStarboardRudderSensor(float starboardRudderSensor);
    /**
     * RSA
     * Port rudder sensor
     * @param portRudderSensor 
     */
    void setPortRudderSensor(float portRudderSensor);
    /**
     * VHW
     * @param degrees
     */
    @NMEACat(Bearing)
    @Unit(value=Degree, min=0, max=360)
    void setTrueWaterHeading(float degrees);
    /**
     * VHW
     * @param degrees
     */
    @NMEACat(Bearing)
    @Unit(value=Degree, min=0, max=360)
    void setMagneticWaterHeading(float degrees);
    /**
     * VHW
     * @param knots
     */
    @NMEACat(Speed)
    @Unit(value=Knot, min=0, max=50)
    void setWaterSpeed(float knots);
    /**
     * @deprecated This method will no longer be called. Use setRelativeWindAngle.
     * VWR
     * Relative wind angle - = left + = right
     * @param windDirection Wind direction magnitude in degrees
     * @see org.vesalainen.parsers.nmea.NMEAObserver#setRelativeWindAngle(float) 
     */
    void setWindDirection(float windDirection);
    /**
     * WCV
     * @param knots
     */
    @NMEACat(Waypoint)
    @Unit(value=Knot, min=0, max=50)
    void setVelocityToWaypoint(float knots);
    /**
     * TXT
     * @param name Target name
     */
    void setTargetName(CharSequence name);
    /**
     * TXT
     * @param message 
     */
    void setMessage(CharSequence message);
    /**
     * Proprietary sentences start with $P. Proprietary type is the string
     * following that prefix. E.g. $PGRMI,... GRMI is the type.
     * @param type
     */
    void setProprietaryType(CharSequence type);
    /**
     * Proprietary data. Comma separated proprietary data fields.
     * @param data
     */
    void setProprietaryData(List<CharSequence> data);
    /**
     * GSA
     * Selection mode: M=Manual, forced to operate in 2D or 3D, A=Automatic, 3D/2D
     * @param mode 
     */
    public void setSelectionMode(char mode);
    /**
     * GSA
     * Mode (1 = no fix, 2 = 2D fix, 3 = 3D fix)
     * @param mode 
     */
    public void setMode(char mode);
    /**
     * GSA
     * ID of 1st satellite used for fix
     * @param id 
     */
    @NMEACat(GPS)
    public void setSatelliteId1(int id);

    @NMEACat(GPS)
    public void setSatelliteId2(int id);

    @NMEACat(GPS)
    public void setSatelliteId3(int id);

    @NMEACat(GPS)
    public void setSatelliteId4(int id);

    @NMEACat(GPS)
    public void setSatelliteId5(int id);

    @NMEACat(GPS)
    public void setSatelliteId6(int id);

    @NMEACat(GPS)
    public void setSatelliteId7(int id);

    @NMEACat(GPS)
    public void setSatelliteId8(int id);

    @NMEACat(GPS)
    public void setSatelliteId9(int id);

    @NMEACat(GPS)
    public void setSatelliteId10(int id);

    @NMEACat(GPS)
    public void setSatelliteId11(int id);

    @NMEACat(GPS)
    public void setSatelliteId12(int id);

    @NMEACat(GPS)
    public void setPdop(float value);

    @NMEACat(GPS)
    public void setHdop(float value);

    @NMEACat(GPS)
    public void setVdop(float value);
    /**
     * Total number of satellites in view
     * @param count 
     */
    @NMEACat(GPS)
    public void setTotalNumberOfSatellitesInView(int count);
    /**
     * Satellite PRN number
     * @param prn 
     */
    @NMEACat(GPS)
    public void setPrn(int prn);
    /**
     * Elevation in degrees
     * @param elevation 
     */
    @NMEACat(GPS)
    @Unit(Degree)
    public void setElevation(int elevation);
    /**
     * Azimuth in degrees to true north
     * @param azimuth 
     */
    @NMEACat(GPS)
    @Unit(Degree)
    public void setAzimuth(int azimuth);
    /**
     * SNR in dB
     * @param snr 
     */
    @NMEACat(GPS)
    public void setSnr(int snr);

    public void setTrueTrackMadeGood(float track);

    public void setMagneticTrackMadeGood(float track);
    /**
     * Route id
     * @param route 
     */
    public void setRoute(CharSequence route);

    public void setTargetNumber(int target);

    public void setTargetHour(int hour);
    public void setTargetMinute(int minute);
    public void setTargetSecond(float second);
    /**
     * S = SOS
     * @param status 
     */
    public void setTargetStatus(char status);

    public void setReferenceTarget(CharSequence referenceTarget);

    public void setMessageType(MessageType messageType);
    /**
     * Pitch: oscillation of vessel about its latitudinal axis. Bow moving up is
     * positive. Value reported to the nearest 0.1 degree.
     * @param value 
     */
    @NMEACat(Attitude)
    @Unit(value=DegreeNeg, min=-60, max=60)
    public void setPitch(float value);
    /**
     * Roll: oscillation of vessel about its longitudinal axis. Roll to the
     * starboard is positive. Value reported to the nearest 0.1 degree.
     * @param value 
     */
    @NMEACat(Attitude)
    @Unit(value=DegreeNeg, min=-100, max=100)
    public void setRoll(float value);
    /**
     * X-Acceleration. Unit is gravity.
     * @param value 
     */
    @NMEACat(Acceleration)
    @Unit(value=GForceEarth, min=-1.1, max=1.1)
    public void setXAcceleration(float value);
    /**
     * Y-Acceleration. Unit is gravity.
     * @param value 
     */
    @NMEACat(Acceleration)
    @Unit(value=GForceEarth, min=-1.1, max=1.1)
    public void setYAcceleration(float value);
    /**
     * Z-Acceleration. Unit is gravity.
     * @param value 
     */
    @NMEACat(Acceleration)
    @Unit(value=GForceEarth, min=-1.1, max=1.1)
    public void setZAcceleration(float value);

    @NMEACat(Acceleration)
    public void setRRat(float value);

    @NMEACat(Acceleration)
    public void setPRat(float value);

    @NMEACat(Acceleration)
    public void setYRat(float value);

    @NMEACat(Acceleration)
    public void setRRtr(float value);

    @NMEACat(Acceleration)
    public void setPRtr(float value);

    @NMEACat(Acceleration)
    public void setYRtr(float value);

    @NMEACat(Distance)
    public void setTargetDistance(float distance);

    @NMEACat(Bearing)
    public void setBearingFromOwnShip(float bearing);

    public void setBearingUnit(char units);

    @NMEACat(Speed)
    public void setTargetSpeed(float speed);

    @NMEACat(Bearing)
    public void setTargetCourse(float course);

    public void setCourseUnit(char units);

    @NMEACat(Distance)
    public void setDistanceOfCPA(float distance);

    public void setTimeToCPA(float time);

    public void setDistanceUnit(char units);
}
