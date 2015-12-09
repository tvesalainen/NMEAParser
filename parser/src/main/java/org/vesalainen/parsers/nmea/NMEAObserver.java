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

import org.vesalainen.util.Transactional;
import java.util.List;

/**
 * NMEAObserver is observer class for NMEA data. NMEAParser calls methods of this 
 * interface.
 * 
 * <p>User of NMEAParser is probably only interested in a small subset of the data.
 * For this reason creation of Strings is avoided because of performance and GC
 * overhead. CharSequence arguments should not be stored. There are mostly valid
 * only during the observer method call. Use toString method to convert to string.
 * 
 * <p>It is mostly easier to derive your class from AbstractNMEAObserver class.
 * AbstractNMEAObserver has empty methods for all NMEAObserver methods.
 * 
 * <p>Observer methods are called as soon they are found in input. Parsing might
 * cause syntax error or NMEA sentence checksum might fail. In that case rollback
 * is called. Critical application should store the values and use them only after
 * commit. 
 * @author Timo Vesalainen
 */
public interface NMEAObserver extends Transactional
{
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
    void setLatitude(float latitude);
    /**
     * Longitude in degrees. BWC, BWR, GGA, GLL, RMA, RMC 
     * @param longitude Longitude West is negative.
     */
    void setLongitude(float longitude);
    /**
     * RMA, RMC
     * @param knots 
     */
    void setSpeedOverGround(float knots);
    /**
     * RMA, RMC
     * @param knots 
     */
    void setTrackMadeGood(float knots);
    /**
     * HDG, RMA, RMC
     * @param degrees West is minus
     */
    void setMagneticVariation(float degrees);
    /**
     * DirectionToSteer - = left + = right
     * APA, APB, XTR
     * @param nm
     */
    void setCrossTrackError(float nm);
    /**
     * BOD, BWW, RMB, WNC
     * @param toWaypoint 
     */
    void setToWaypoint(CharSequence toWaypoint);
    /**
     * BOD, BWW, RMB, WNC
     * @param fromWaypoint 
     */
    void setFromWaypoint(CharSequence fromWaypoint);
    /**
     * RMB, WPL
     * @param latitude 
     */
    void setDestinationWaypointLatitude(float latitude);
    /**
     * RMB, WPL
     * @param longitude 
     */
    void setDestinationWaypointLongitude(float longitude);
    /**
     * RMB
     * @param nm 
     */
    void setRangeToDestination(float nm);
    /**
     * RMB
     * @param degrees True
     */
    void setBearingToDestination(float degrees);
    /**
     * RMB
     * @param knots 
     */
    void setDestinationClosingVelocity(float knots);
    /**
     * GGA
     * @param gpsQualityIndicator 
     */
    void setGpsQualityIndicator(GPSQualityIndicator gpsQualityIndicator);
    /**
     * GGA
     * @param numberOfSatellitesInView 
     */
    void setNumberOfSatellitesInView(int numberOfSatellitesInView);
    /**
     * GGA
     * @param meters 
     */
    void setHorizontalDilutionOfPrecision(float meters);
    /**
     * GGA
     * @param meters
     */
    void setAntennaAltitude(float meters);
    /**
     * GGA
     * @param meters
     */
    void setGeoidalSeparation(float meters);
    /**
     * GGA
     * @param ageOfDifferentialGPSData 
     */
    void setAgeOfDifferentialGPSData(float ageOfDifferentialGPSData);
    /**
     * GGA
     * @param differentialReferenceStationID 
     */
    void setDifferentialReferenceStationID(int differentialReferenceStationID);
    /**
     * APA, APB, GLL, MWV, RMA, RMB, RMC, ROT, RSA, XTE
     * @param status 
     */
    void setStatus(char status);
    /**
     * AAM, APA, APB
     * @param arrivalStatus 
     */
    void setArrivalStatus(char arrivalStatus);
    /**
     * RMA
     * @param timeDifferenceA 
     */
    void setTimeDifferenceA(float timeDifferenceA);
    /**
     * RMA
     * @param timeDifferenceB 
     */
    void setTimeDifferenceB(float timeDifferenceB);
    /**
     * AAM, APA, APB
     * @param waypointStatus 
     */
    void setWaypointStatus(char waypointStatus);
    /**
     * AAM
     * @param nm
     */
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
    void setSatellitePRNNumber(int satellitePRNNumber);
    /**
     * ALM
     * @param gpsWeekNumber 
     */
    void setGpsWeekNumber(int gpsWeekNumber);
    /**
     * ALM
     * @param svHealth 
     */
    void setSvHealth(int svHealth);
    /**
     * ALM
     * @param eccentricity 
     */
    void setEccentricity(int eccentricity);
    /**
     * ALM
     * @param almanacReferenceTime 
     */
    void setAlmanacReferenceTime(int almanacReferenceTime);
    /**
     * ALM
     * @param inclinationAngle 
     */
    void setInclinationAngle(int inclinationAngle);
    /**
     * ALM
     * @param rateOfRightAscension 
     */
    void setRateOfRightAscension(int rateOfRightAscension);
    /**
     * ALM
     * @param rootOfSemiMajorAxis 
     */
    void setRootOfSemiMajorAxis(int rootOfSemiMajorAxis);
    /**
     * ALM
     * @param argumentOfPerigee 
     */
    void setArgumentOfPerigee(int argumentOfPerigee);
    /**
     * ALM
     * @param longitudeOfAscensionNode 
     */
    void setLongitudeOfAscensionNode(int longitudeOfAscensionNode);
    /**
     * ALM
     * @param meanAnomaly 
     */
    void setMeanAnomaly(int meanAnomaly);
    /**
     * ALM
     * @param f0ClockParameter 
     */
    void setF0ClockParameter(int f0ClockParameter);
    /**
     * ALM
     * @param f1ClockParameter 
     */
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
    void setMagneticBearingOriginToDestination(float bearingOriginToDestination);
    /**
     * APA, APB
     * @param bearingOriginToDestination 
     */
    void setTrueBearingOriginToDestination(float bearingOriginToDestination);
    /**
     * APB
     * @param bearingPresentPositionToDestination 
     */
    void setMagneticBearingPresentPositionToDestination(float bearingPresentPositionToDestination);
    /**
     * APB
     * @param bearingPresentPositionToDestination 
     */
    void setTrueBearingPresentPositionToDestination(float bearingPresentPositionToDestination);
    /**
     * APB
     * @param headingToSteerToDestination 
     */
    void setMagneticHeadingToSteerToDestination(float headingToSteerToDestination);
    /**
     * APB
     * @param headingToSteerToDestination 
     */
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
    void setDistanceToWaypoint(float nm);
    /**
     * DBK
     * @param meters
     */
    void setDepthBelowKeel(float meters);
    /**
     * DBS
     * @param meters
     */
    void setDepthBelowSurface(float meters);
    /**
     * DBT
     * @param meters
     */
    void setDepthBelowTransducer(float meters);
    /**
     * BOD, BWC, BWR, BWW
     * @param degrees
     */
    void setTrueBearing(float degrees);
    /**
     * BOD, BWC, BWR, BWW
     * @param degrees
     */
    void setMagneticBearing(float degrees);
    /**
     * DBT
     * @param meters
     */
    void setDepthOfWater(float meters);
    /**
     * DBT
     * @param meters
     */
    void setDepthOffsetOfWater(float meters);
    /**
     * HDG
     * @param magneticDeviation 
     */
    void setMagneticDeviation(float magneticDeviation);
    /**
     * HDG
     * @param magneticSensorHeading 
     */
    void setMagneticSensorHeading(float magneticSensorHeading);
    /**
     * HDM, HDT
     * @param degrees
     */
    void setTrueHeading(float degrees);
    /**
     * HDM, HDT
     * @param degrees
     */
    void setMagneticHeading(float degrees);
    /**
     * MTW
     * @param celcius
     */
    void setWaterTemperature(float celcius);
    /**
     * MWV
     * @param windAngle Wind Angle, 0 to 360 degrees
     */
    void setRelativeWindAngle(float windAngle);
    /**
     * MWV
     * @param windAngle Wind Angle, 0 to 360 degrees
     */
    void setTrueWindAngle(float windAngle);
    /**
     * MWV, VWR
     * @param metersInSecond
     */
    void setWindSpeed(float metersInSecond);
    /**
     * ROT
     * Rate Of Turn, degrees per minute, "-" means bow turns to port
     * @param rateOfTurn 
     */
    void setRateOfTurn(float rateOfTurn);
    /**
     * RPM
     * Sourse, S = Shaft, E = Engine
     * @param rpmSource 
     */
    void setRpmSource(char rpmSource);
    /**
     * RPM
     * Engine or shaft number
     * @param rpmSourceNumber 
     */
    void setRpmSourceNumber(int rpmSourceNumber);
    /**
     * RPM
     * Speed, Revolutions per minute
     * @param rpm 
     */
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
    void setTrueWaterHeading(float degrees);
    /**
     * VHW
     * @param degrees
     */
    void setMagneticWaterHeading(float degrees);
    /**
     * VHW
     * @param knots
     */
    void setWaterSpeed(float knots);
    /**
     * VWR
     * Relative wind angle - = left + = right
     * @param windDirection Wind direction magnitude in degrees
     */
    void setWindDirection(float windDirection);
    /**
     * WCV
     * @param knots
     */
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
    public void setSatelliteId1(int id);

    public void setSatelliteId2(int id);

    public void setSatelliteId3(int id);

    public void setSatelliteId4(int id);

    public void setSatelliteId5(int id);

    public void setSatelliteId6(int id);

    public void setSatelliteId7(int id);

    public void setSatelliteId8(int id);

    public void setSatelliteId9(int id);

    public void setSatelliteId10(int id);

    public void setSatelliteId11(int id);

    public void setSatelliteId12(int id);

    public void setPdop(float value);

    public void setHdop(float value);

    public void setVdop(float value);
    /**
     * Total number of satellites in view
     * @param count 
     */
    public void setTotalNumberOfSatellitesInView(int count);
    /**
     * Satellite PRN number
     * @param prn 
     */
    public void setPrn(int prn);
    /**
     * Elevation in degrees
     * @param elevation 
     */
    public void setElevation(int elevation);
    /**
     * Azimuth in degrees to true north
     * @param azimuth 
     */
    public void setAzimuth(int azimuth);
    /**
     * SNR in dB
     * @param snr 
     */
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
    public void setPitch(float value);
    /**
     * Roll: oscillation of vessel about its longitudinal axis. Roll to the
     * starboard is positive. Value reported to the nearest 0.1 degree.
     * @param value 
     */
    public void setRoll(float value);
    /**
     * X-Acceleration. Unit is gravity.
     * @param value 
     */
    public void setXAcceleration(float value);
    /**
     * Y-Acceleration. Unit is gravity.
     * @param value 
     */
    public void setYAcceleration(float value);
    /**
     * Z-Acceleration. Unit is gravity.
     * @param value 
     */
    public void setZAcceleration(float value);

    public void setRRat(float value);

    public void setPRat(float value);

    public void setYRat(float value);

    public void setRRtr(float value);

    public void setPRtr(float value);

    public void setYRtr(float value);
}
