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

import java.util.List;
import org.vesalainen.parser.util.InputReader;

/**
 * NMEAObserver is observer class for NMEA data. NMEAParser calls methods of this 
 * interface.
 * 
 * <p>User of NMEAParser is probably only interested in a small subset of the data.
 * For this reason creation of Strings is avoided because of performance and GC
 * overhead. Use InputReader getString(fieldRef) to create strings. Use fieldRefs
 * before commit.
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
     * Talker Id of sending device.
     * @param c1
     * @param c2 
     */
    void setTalkerId(char c1, char c2);
    /**
     * Location in degrees. BWC, BWR, GGA, GLL, RMA, RMC 
     * @param latitude Latitude. South is negative.
     * @param longitude Longitude West is negative.
     */
    void setLocation(float latitude, float longitude);
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
     * APA, APB, XTR
     * @param crossTrackError In unit
     * @param directionToSteer L or R
     * @param unit N = NM, K=Km
     */
    void setCrossTrackError(float crossTrackError, char directionToSteer, char unit);
    /**
     * BOD, BWW, RMB, WNC
     * @param input
     * @param toWaypoint
     * @param fromWaypoint 
     */
    void setWaypointToWaypoint(InputReader input, int toWaypoint, int fromWaypoint);
    /**
     * RMB, WPL
     * @param latitude
     * @param longitude 
     */
    void setDestinationWaypointLocation(float latitude, float longitude);
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
     * @param antennaAltitude
     * @param unitsOfAntennaAltitude 
     */
    void setAntennaAltitude(float antennaAltitude, char unitsOfAntennaAltitude);
    /**
     * GGA
     * @param geoidalSeparation
     * @param unitsOfGeoidalSeparation 
     */
    void setGeoidalSeparation(float geoidalSeparation, char unitsOfGeoidalSeparation);
    /**
     * GGA
     * @param ageOfDifferentialGPSData 
     */
    void setAgeOfDifferentialGPSData(int ageOfDifferentialGPSData);
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
     * @param timeDifferenceB 
     */
    void setTimeDifference(float timeDifferenceA, float timeDifferenceB);
    /**
     * AAM, APA, APB
     * @param waypointStatus 
     */
    void setWaypointStatus(char waypointStatus);
    /**
     * AAM
     * @param arrivalCircleRadius
     * @param units 
     */
    void setArrivalCircleRadius(float arrivalCircleRadius, char units);
    /**
     * AAM, APA, APB, BWC, BWR, R00, WCV, WPL
     * @param input
     * @param waypoint 
     */
    void setWaypoint(InputReader input, int waypoint);
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
    void setEccentricity(float eccentricity);
    /**
     * ALM
     * @param almanacReferenceTime 
     */
    void setAlmanacReferenceTime(float almanacReferenceTime);
    /**
     * ALM
     * @param inclinationAngle 
     */
    void setInclinationAngle(float inclinationAngle);
    /**
     * ALM
     * @param rateOfRightAscension 
     */
    void setRateOfRightAscension(float rateOfRightAscension);
    /**
     * ALM
     * @param rootOfSemiMajorAxis 
     */
    void setRootOfSemiMajorAxis(float rootOfSemiMajorAxis);
    /**
     * ALM
     * @param argumentOfPerigee 
     */
    void setArgumentOfPerigee(float argumentOfPerigee);
    /**
     * ALM
     * @param longitudeOfAscensionNode 
     */
    void setLongitudeOfAscensionNode(float longitudeOfAscensionNode);
    /**
     * ALM
     * @param meanAnomaly 
     */
    void setMeanAnomaly(float meanAnomaly);
    /**
     * ALM
     * @param f0ClockParameter 
     */
    void setF0ClockParameter(float f0ClockParameter);
    /**
     * ALM
     * @param f1ClockParameter 
     */
    void setF1ClockParameter(float f1ClockParameter);
    /**
     * APA, APB, RSA, XTE
     * @param status 
     */
    void setStatus2(char status);
    /**
     * APA, APB
     * @param bearingOriginToDestination
     * @param mOrT 
     */
    void setBearingOriginToDestination(float bearingOriginToDestination, char mOrT);
    /**
     * APB
     * @param bearingPresentPositionToDestination
     * @param mOrT 
     */
    void setBearingPresentPositionToDestination(float bearingPresentPositionToDestination, char mOrT);
    /**
     * APB
     * @param headingToSteerToDestination
     * @param mOrT 
     */
    void setHeadingToSteerToDestination(float headingToSteerToDestination, char mOrT);
    /**
     * BWC, GLL, XTE
     * @param faaModeIndicator 
     */
    void setFAAModeIndicator(char faaModeIndicator);
    /**
     * RMM
     * @param input
     * @param horizontalDatum 
     */
    void setHorizontalDatum(InputReader input, int horizontalDatum);
    /**
     * RTE
     * @param messageMode 
     */
    void setMessageMode(char messageMode);
    /**
     * R00, RTE
     * @param input
     * @param list 
     */
    void setWaypoints(InputReader input, List<Integer> list);
    /**
     * BWC, BWR WNC
     * @param distanceToWaypoint
     * @param units 
     */
    void setDistanceToWaypoint(float distanceToWaypoint, char units);
    /**
     * DBK
     * @param depth
     * @param unit 
     */
    void setDepthBelowKeel(float depth, char unit);
    /**
     * DBS
     * @param depth
     * @param unit 
     */
    void setDepthBelowSurface(float depth, char unit);
    /**
     * DBT
     * @param depth
     * @param unit 
     */
    void setDepthBelowTransducer(float depth, char unit);
    /**
     * BOD, BWC, BWR, BWW
     * @param bearing
     * @param unit 
     */
    void setBearing(float bearing, char unit);
    /**
     * DBT
     * @param depth
     * @param offset 
     */
    void setDepthOfWater(float depth, float offset);
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
     * @param heading
     * @param unit 
     */
    void setHeading(float heading, char unit);
    /**
     * MTW
     * @param waterTemperature
     * @param unit 
     */
    void setWaterTemperature(float waterTemperature, char unit);
    /**
     * MWV
     * @param windAngle Wind Angle, 0 to 360 degrees
     * @param unit Reference, R = Relative, T = True
     */
    void setWindAngle(float windAngle, char unit);
    /**
     * MWV, VWR
     * @param windSpeed
     * @param unit Wind Speed Units, K/M/N
     */
    void setWindSpeed(float windSpeed, char unit);
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
    void setRpm(int rpm);
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
     * @param waterHeading
     * @param unit 
     */
    void setWaterHeading(float waterHeading, char unit);
    /**
     * VHW
     * @param waterSpeed
     * @param unit 
     */
    void setWaterSpeed(float waterSpeed, char unit);
    /**
     * VWR
     * Relative wind angle
     * @param windDirection Wind direction magnitude in degrees
     * @param unit Wind direction Left/Right of bow
     */
    void setWindDirection(float windDirection, char unit);
    /**
     * WCV
     * @param velocityToWaypoint
     * @param unit 
     */
    void setVelocityToWaypoint(float velocityToWaypoint, char unit);
    /**
     * TXT
     * @param input
     * @param name Target name
     */
    void setTargetName(InputReader input, int name);
    /**
     * TXT
     * @param input
     * @param message 
     */
    void setMessage(InputReader input, int message);
    /**
     * Proprietary sentences start with $P. Proprietary type is the string
     * following that prefix. E.g. $PGRMI,... GRMI is the type.
     * @param reader
     * @param fieldRef FieldRef for type.
     */
    void setProprietaryType(InputReader reader, int fieldRef);
    /**
     * Proprietary data. Comma separated proprietary data fields.
     * @param reader
     * @param fieldRef 
     */
    void setProprietaryData(InputReader reader, int fieldRef);

}
