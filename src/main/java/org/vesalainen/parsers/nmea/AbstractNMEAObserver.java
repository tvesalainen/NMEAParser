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

/**
 * @author Timo Vesalainen
 */
public class AbstractNMEAObserver implements NMEAObserver
{
    protected Clock clock;

    @Override
    public void setClock(Clock clock)
    {
        this.clock = clock;
    }
    
    /**
     * Returns distance in NM from (lat1, lon1) to (lat2, lon2)
     * @param lat1 in degrees
     * @param lon1 in degrees
     * @param lat2 in degrees
     * @param lon2 in degrees
     * @return 
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2)
    {
        // TO DO use GC with long distance
        double dep = departure(lat1, lat2);
        return (double) (60*Math.sqrt(square(lat1-lat2)+square(dep*(lon1-lon2))));
    }
    
    private static double square(double d)
    {
        return d*d;
    }
    /**
     * Returns bearing in degrees from (lat1, lon1) to (lat2, lon2)
     * @param lat1 in degrees
     * @param lon1 in degrees
     * @param lat2 in degrees
     * @param lon2 in degrees
     * @return 
     */
    public static double bearing(double lat1, double lon1, double lat2, double lon2)
    {
        double dep = departure(lat1, lat2);
        double aa = dep*(lon2-lon1);
        double bb = lat2-lat1;
        double dd = (double) Math.atan2(aa, bb);
        if (dd < 0)
        {
            dd += 2*Math.PI;
        }
        return (double) Math.toDegrees(dd);
    }
    
    /**
     * 
     * @param lat1 Latitude in degrees
     * @param lat2 Latitude in degrees
     * @return 
     */
    public static double departure(double lat1, double lat2)
    {
        assert lat1 >= -90 && lat1 <= 90;
        assert lat2 >= -90 && lat2 <= 90;
        return (double) Math.cos(Math.toRadians((lat2+lat1)/2));
    }


    @Override
    public void setTalkerId1(char talkerId)
    {
        
    }

    @Override
    public void setTalkerId2(char talkerId)
    {
        
    }

    @Override
    public void setLatitude(float latitude)
    {
        
    }

    @Override
    public void setLongitude(float longitude)
    {
        
    }

    @Override
    public void setSpeedOverGround(float speedOverGround)
    {
        
    }

    @Override
    public void setTrackMadeGood(float trackMadeGood)
    {
        
    }

    @Override
    public void setMagneticVariation(float magneticVariation)
    {
        
    }

    @Override
    public void setCrossTrackError(float crossTrackError)
    {
        
    }

    @Override
    public void setDestinationWaypointLatitude(float latitude)
    {
        
    }

    @Override
    public void setDestinationWaypointLongitude(float longitude)
    {
        
    }

    @Override
    public void setRangeToDestination(float rangeToDestination)
    {
        
    }

    @Override
    public void setBearingToDestination(float bearingToDestination)
    {
        
    }

    @Override
    public void setDestinationClosingVelocity(float destinationClosingVelocity)
    {
        
    }

    @Override
    public void setGpsQualityIndicator(GPSQualityIndicator gpsQualityIndicator)
    {
        
    }

    @Override
    public void setNumberOfSatellitesInView(int numberOfSatellitesInView)
    {
        
    }

    @Override
    public void setHorizontalDilutionOfPrecision(float horizontalDilutionOfPrecision)
    {
        
    }

    @Override
    public void setAntennaAltitude(float antennaAltitude)
    {
        
    }

    @Override
    public void setGeoidalSeparation(float geoidalSeparation)
    {
        
    }

    @Override
    public void setAgeOfDifferentialGPSData(float ageOfDifferentialGPSData)
    {
        
    }

    @Override
    public void setDifferentialReferenceStationID(int differentialReferenceStationID)
    {
        
    }

    @Override
    public void setStatus(char status)
    {
        
    }

    @Override
    public void setArrivalStatus(char arrivalStatus)
    {
        
    }

    @Override
    public void setTimeDifferenceA(float timeDifferenceA)
    {
        
    }

    @Override
    public void setTimeDifferenceB(float timeDifferenceB)
    {
        
    }

    @Override
    public void setWaypointStatus(char waypointStatus)
    {
        
    }

    @Override
    public void setArrivalCircleRadius(float arrivalCircleRadius)
    {
        
    }

    @Override
    public void setWaypoint(String waypoint)
    {
        
    }

    @Override
    public void setTotalNumberOfMessages(int totalNumberOfMessages)
    {
        
    }

    @Override
    public void setMessageNumber(int messageNumber)
    {
        
    }

    @Override
    public void setSatellitePRNNumber(int satellitePRNNumber)
    {
        
    }

    @Override
    public void setGpsWeekNumber(int gpsWeekNumber)
    {
        
    }

    @Override
    public void setSvHealth(int svHealth)
    {
        
    }

    @Override
    public void setEccentricity(int eccentricity)
    {
        
    }

    @Override
    public void setAlmanacReferenceTime(int almanacReferenceTime)
    {
        
    }

    @Override
    public void setInclinationAngle(int inclinationAngle)
    {
        
    }

    @Override
    public void setRateOfRightAscension(int rateOfRightAscension)
    {
        
    }

    @Override
    public void setRootOfSemiMajorAxis(int rootOfSemiMajorAxis)
    {
        
    }

    @Override
    public void setArgumentOfPerigee(int argumentOfPerigee)
    {
        
    }

    @Override
    public void setLongitudeOfAscensionNode(int longitudeOfAscensionNode)
    {
        
    }

    @Override
    public void setMeanAnomaly(int meanAnomaly)
    {
        
    }

    @Override
    public void setF0ClockParameter(int f0ClockParameter)
    {
        
    }

    @Override
    public void setF1ClockParameter(int f1ClockParameter)
    {
        
    }

    @Override
    public void setStatus2(char status)
    {
        
    }

    @Override
    public void setFaaModeIndicator(char faaModeIndicator)
    {
        
    }

    @Override
    public void setMessageMode(char messageMode)
    {
        
    }

    @Override
    public void setWaypoints(List<String> list)
    {
        
    }

    @Override
    public void setDistanceToWaypoint(float distanceToWaypoint)
    {
        
    }

    @Override
    public void setDepthBelowKeel(float depth)
    {
        
    }

    @Override
    public void setDepthBelowSurface(float depth)
    {
        
    }

    @Override
    public void setDepthBelowTransducer(float depth)
    {
        
    }

    @Override
    public void setMagneticDeviation(float magneticDeviation)
    {
        
    }

    @Override
    public void setMagneticSensorHeading(float magneticSensorHeading)
    {
        
    }

    @Override
    public void setWaterTemperature(float waterTemperature)
    {
        
    }

    @Override
    public void setRateOfTurn(float rateOfTurn)
    {
        
    }

    @Override
    public void setRpmSource(char rpmSource)
    {
        
    }

    @Override
    public void setRpmSourceNumber(int rpmSourceNumber)
    {
        
    }

    @Override
    public void setRpm(float rpm)
    {
        
    }

    @Override
    public void setPropellerPitch(float propellerPitch)
    {
        
    }

    @Override
    public void setStarboardRudderSensor(float starboardRudderSensor)
    {
        
    }

    @Override
    public void setPortRudderSensor(float portRudderSensor)
    {
        
    }

    @Override
    public void setVelocityToWaypoint(float velocityToWaypoint)
    {
        
    }

    @Override
    public void rollback(String reason)
    {
        
    }

    @Override
    public void commit(String reason)
    {
        
    }

    @Override
    public void setTargetName(String name)
    {
    }

    @Override
    public void setMessage(String message)
    {
    }

    @Override
    public void setProprietaryType(String type)
    {
    }

    @Override
    public void setProprietaryData(List<String> data)
    {
    }

    @Override
    public void setToWaypoint(String toWaypoint)
    {
    }

    @Override
    public void setFromWaypoint(String fromWaypoint)
    {
    }

    @Override
    public void setMagneticBearingOriginToDestination(float bearingOriginToDestination)
    {
    }

    @Override
    public void setTrueBearingOriginToDestination(float bearingOriginToDestination)
    {
    }

    @Override
    public void setMagneticBearingPresentPositionToDestination(float bearingPresentPositionToDestination)
    {
    }

    @Override
    public void setTrueBearingPresentPositionToDestination(float bearingPresentPositionToDestination)
    {
    }

    @Override
    public void setMagneticHeadingToSteerToDestination(float headingToSteerToDestination)
    {
    }

    @Override
    public void setTrueHeadingToSteerToDestination(float headingToSteerToDestination)
    {
    }

    @Override
    public void setTrueBearing(float degrees)
    {
    }

    @Override
    public void setMagneticBearing(float degrees)
    {
    }

    @Override
    public void setDepthOfWater(float meters)
    {
    }

    @Override
    public void setDepthOffsetOfWater(float meters)
    {
    }

    @Override
    public void setTrueHeading(float degrees)
    {
    }

    @Override
    public void setMagneticHeading(float degrees)
    {
    }

    @Override
    public void setRelativeWindAngle(float windAngle)
    {
    }

    @Override
    public void setTrueWindAngle(float windAngle)
    {
    }

    @Override
    public void setWindSpeed(float metersInSecond)
    {
    }

    @Override
    public void setTrueWaterHeading(float degrees)
    {
    }

    @Override
    public void setMagneticWaterHeading(float degrees)
    {
    }

    @Override
    public void setWaterSpeed(float knots)
    {
    }

    @Override
    public void setWindDirection(float windDirection)
    {
    }

    @Override
    public void setSelectionMode(char mode)
    {
    }

    @Override
    public void setMode(char mode)
    {
    }

    @Override
    public void setSatelliteId1(int id)
    {
    }

    @Override
    public void setSatelliteId2(int id)
    {
    }

    @Override
    public void setSatelliteId3(int id)
    {
    }

    @Override
    public void setSatelliteId4(int id)
    {
    }

    @Override
    public void setSatelliteId5(int id)
    {
    }

    @Override
    public void setSatelliteId6(int id)
    {
    }

    @Override
    public void setSatelliteId7(int id)
    {
    }

    @Override
    public void setSatelliteId8(int id)
    {
    }

    @Override
    public void setSatelliteId9(int id)
    {
    }

    @Override
    public void setSatelliteId10(int id)
    {
    }

    @Override
    public void setSatelliteId11(int id)
    {
    }

    @Override
    public void setSatelliteId12(int id)
    {
    }

    @Override
    public void setPdop(float value)
    {
    }

    @Override
    public void setHdop(float value)
    {
    }

    @Override
    public void setVdop(float value)
    {
    }

    @Override
    public void setTotalNumberOfSatellitesInView(int count)
    {
    }

    @Override
    public void setPrn(int prn)
    {
    }

    @Override
    public void setElevation(int elevation)
    {
    }

    @Override
    public void setAzimuth(int azimuth)
    {
    }

    @Override
    public void setSnr(int snr)
    {
    }

    @Override
    public void setTrueTrackMadeGood(float track)
    {
    }

    @Override
    public void setMagneticTrackMadeGood(float track)
    {
    }

    @Override
    public void setRoute(String route)
    {
    }

}
