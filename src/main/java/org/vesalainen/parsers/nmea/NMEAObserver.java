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
    void talkerId(char c1, char c2);
    /**
     * Location in degrees. BWC, BWR, GGA, GLL, RMA, RMC 
     * @param latitude Latitude. South is negative.
     * @param longitude Longitude West is negative.
     */
    void setLocation(double latitude, double longitude);
    /**
     * RMA, RMC
     * @param speedOverGround 
     */
    void setSpeedOverGround(float speedOverGround);
    /**
     * RMA, RMC
     * @param trackMadeGood 
     */
    void setTrackMadeGood(float trackMadeGood);
    /**
     * HDG, RMA, RMC
     * @param magneticVariation 
     */
    void setMagneticVariation(float magneticVariation);
    /**
     * RMB, APA, APB, XTR
     * @param crossTrackError
     * @param directionToSteer
     * @param units 
     */
    void setCrossTrackError(float crossTrackError, char directionToSteer, char units);
    /**
     * BOD, BWW, RMB, WNC
     * @param toWaypoint
     * @param fromWaypoint 
     */
    void setWaypointToWaypoint(InputReader toWaypoint, InputReader fromWaypoint);
    /**
     * RMB, WPL
     * @param latitude
     * @param longitude 
     */
    void setDestinationWaypointLocation(double latitude, double longitude);
    /**
     * RMB
     * @param rangeToDestination 
     */
    void setRangeToDestination(float rangeToDestination);
    /**
     * RMB
     * @param bearingToDestination 
     */
    void setBearingToDestination(float bearingToDestination);
    /**
     * RMB
     * @param destinationClosingVelocity 
     */
    void setDestinationClosingVelocity(float destinationClosingVelocity);
    /**
     * GGA
     * @param gpsQualityIndicator 
     */
    void setGpsQualityIndicator(int gpsQualityIndicator);
    /**
     * GGA
     * @param numberOfSatellitesInView 
     */
    void setNumberOfSatellitesInView(int numberOfSatellitesInView);
    /**
     * GGA
     * @param horizontalDilutionOfPrecision 
     */
    void setHorizontalDilutionOfPrecision(float horizontalDilutionOfPrecision);
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
     * @param waypoint 
     */
    void setWaypoint(InputReader waypoint);
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
     * @param horizontalDatum 
     */
    void setHorizontalDatum(InputReader horizontalDatum);
    /**
     * RTE
     * @param messageMode 
     */
    void setMessageMode(char messageMode);
    /**
     * R00, RTE
     * @param list 
     */
    void setWaypoints(List<String> list);
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
     * @param name Target name
     */
    void setTargetName(InputReader name);
    /**
     * TXT
     * @param message 
     */
    void setMessage(InputReader message);

}
