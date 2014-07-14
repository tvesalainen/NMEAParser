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
    public static float distance(float lat1, float lon1, float lat2, float lon2)
    {
        // TO DO use GC with long distance
        float dep = departure(lat1, lat2);
        return (float) (60*Math.sqrt(square(lat1-lat2)+square(dep*(lon1-lon2))));
    }
    
    private static float square(float d)
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
    public static float bearing(float lat1, float lon1, float lat2, float lon2)
    {
        float dep = departure(lat1, lat2);
        float aa = dep*(lon2-lon1);
        float bb = lat2-lat1;
        float dd = (float) Math.atan2(aa, bb);
        if (dd < 0)
        {
            dd += 2*Math.PI;
        }
        return (float) Math.toDegrees(dd);
    }
    
    /**
     * 
     * @param lat1 Latitude in degrees
     * @param lat2 Latitude in degrees
     * @return 
     */
    public static float departure(float lat1, float lat2)
    {
        assert lat1 >= -90 && lat1 <= 90;
        assert lat2 >= -90 && lat2 <= 90;
        return (float) Math.cos(Math.toRadians((lat2+lat1)/2));
    }


    @Override
    public void setTalkerId(char c1, char c2)
    {
        
    }

    @Override
    public void setLocation(float latitude, float longitude)
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
    public void setCrossTrackError(float crossTrackError, char directionToSteer, char units)
    {
        
    }

    @Override
    public void setWaypointToWaypoint(InputReader input, int toWaypoint, int fromWaypoint)
    {
        
    }

    @Override
    public void setDestinationWaypointLocation(float latitude, float longitude)
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
    public void setAntennaAltitude(float antennaAltitude, char unitsOfAntennaAltitude)
    {
        
    }

    @Override
    public void setGeoidalSeparation(float geoidalSeparation, char unitsOfGeoidalSeparation)
    {
        
    }

    @Override
    public void setAgeOfDifferentialGPSData(int ageOfDifferentialGPSData)
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
    public void setTimeDifference(float timeDifferenceA, float timeDifferenceB)
    {
        
    }

    @Override
    public void setWaypointStatus(char waypointStatus)
    {
        
    }

    @Override
    public void setArrivalCircleRadius(float arrivalCircleRadius, char units)
    {
        
    }

    @Override
    public void setWaypoint(InputReader input, int waypoint)
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
    public void setEccentricity(float eccentricity)
    {
        
    }

    @Override
    public void setAlmanacReferenceTime(float almanacReferenceTime)
    {
        
    }

    @Override
    public void setInclinationAngle(float inclinationAngle)
    {
        
    }

    @Override
    public void setRateOfRightAscension(float rateOfRightAscension)
    {
        
    }

    @Override
    public void setRootOfSemiMajorAxis(float rootOfSemiMajorAxis)
    {
        
    }

    @Override
    public void setArgumentOfPerigee(float argumentOfPerigee)
    {
        
    }

    @Override
    public void setLongitudeOfAscensionNode(float longitudeOfAscensionNode)
    {
        
    }

    @Override
    public void setMeanAnomaly(float meanAnomaly)
    {
        
    }

    @Override
    public void setF0ClockParameter(float f0ClockParameter)
    {
        
    }

    @Override
    public void setF1ClockParameter(float f1ClockParameter)
    {
        
    }

    @Override
    public void setStatus2(char status)
    {
        
    }

    @Override
    public void setBearingOriginToDestination(float bearingOriginToDestination, char mOrT)
    {
        
    }

    @Override
    public void setBearingPresentPositionToDestination(float bearingPresentPositionToDestination, char mOrT)
    {
        
    }

    @Override
    public void setHeadingToSteerToDestination(float headingToSteerToDestination, char mOrT)
    {
        
    }

    @Override
    public void setFAAModeIndicator(char faaModeIndicator)
    {
        
    }

    @Override
    public void setHorizontalDatum(InputReader reader, int horizontalDatum)
    {
        
    }

    @Override
    public void setMessageMode(char messageMode)
    {
        
    }

    @Override
    public void setWaypoints(InputReader input, List<Integer> list)
    {
        
    }

    @Override
    public void setDistanceToWaypoint(float distanceToWaypoint, char units)
    {
        
    }

    @Override
    public void setDepthBelowKeel(float depth, char unit)
    {
        
    }

    @Override
    public void setDepthBelowSurface(float depth, char unit)
    {
        
    }

    @Override
    public void setDepthBelowTransducer(float depth, char unit)
    {
        
    }

    @Override
    public void setBearing(float bearing, char unit)
    {
        
    }

    @Override
    public void setDepthOfWater(float depth, float offset)
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
    public void setHeading(float heading, char unit)
    {
        
    }

    @Override
    public void setWaterTemperature(float waterTemperature, char unit)
    {
        
    }

    @Override
    public void setWindAngle(float windAngle, char unit)
    {
        
    }

    @Override
    public void setWindSpeed(float windSpeed, char unit)
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
    public void setRpm(int rpm)
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
    public void setWaterHeading(float waterHeading, char unit)
    {
        
    }

    @Override
    public void setWaterSpeed(float waterSpeed, char unit)
    {
        
    }

    @Override
    public void setWindDirection(float windDirection, char unit)
    {
        
    }

    @Override
    public void setVelocityToWaypoint(float velocityToWaypoint, char unit)
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
    public void setTargetName(InputReader input, int name)
    {
    }

    @Override
    public void setMessage(InputReader input, int message)
    {
    }

    @Override
    public void setProprietaryType(InputReader reader, int fieldRef)
    {
    }

    @Override
    public void setProprietaryData(InputReader reader, int fieldRef)
    {
    }

}
