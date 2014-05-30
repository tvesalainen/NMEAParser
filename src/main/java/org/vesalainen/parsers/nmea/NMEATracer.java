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

import java.util.ArrayList;
import java.util.List;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.util.AppendablePrinter;

/**
 * @author Timo Vesalainen
 */
public class NMEATracer implements NMEAObserver
{
    private AppendablePrinter printer;

    public NMEATracer()
    {
        this(System.err);
    }

    public NMEATracer(Appendable appendable)
    {
        this.printer = new AppendablePrinter(appendable);
    }
    
    @Override
    public void talkerId(char c1, char c2)
    {
        printer.println("talkerId="+c1+c2);
    }

    @Override
    public void commit(String reason)
    {
        printer.println("commit("+reason+")");
    }

    @Override
    public void rollback(String reason)
    {
        printer.println("rollback("+reason+")");
    }

    @Override
    public void setSpeedOverGround(float speedOverGround)
    {
        printer.println("setSpeedOverGround("+speedOverGround+")");
    }

    @Override
    public void setTrackMadeGood(float trackMadeGood)
    {
        printer.println("setTrackMadeGood("+trackMadeGood+")");
    }

    @Override
    public void setMagneticVariation(float magneticVariation)
    {
        printer.println("setMagneticVariation("+magneticVariation+")");
    }

    @Override
    public void setCrossTrackError(float crossTrackError, char directionToSteer, char units)
    {
        printer.println("setCrossTrackError("+crossTrackError+", "+directionToSteer+", "+units+")");
    }

    @Override
    public void setWaypointToWaypoint(InputReader reader, int toWaypoint, int fromWaypoint)
    {
        printer.println("setWaypointToWaypoint("+reader.getString(toWaypoint)+", "+reader.getString(fromWaypoint)+")");
    }

    @Override
    public void setRangeToDestination(float rangeToDestination)
    {
        printer.println("setRangeToDestination("+rangeToDestination+")");
    }

    @Override
    public void setBearingToDestination(float bearingToDestination)
    {
        printer.println("setBearingToDestination("+bearingToDestination+")");
    }

    @Override
    public void setDestinationClosingVelocity(float destinationClosingVelocity)
    {
        printer.println("setDestinationClosingVelocity("+destinationClosingVelocity+")");
    }

    @Override
    public void setGpsQualityIndicator(GPSQualityIndicator gpsQualityIndicator)
    {
        printer.println("setGpsQualityIndicator("+gpsQualityIndicator+")");
    }

    @Override
    public void setNumberOfSatellitesInView(int numberOfSatellitesInView)
    {
        printer.println("setNumberOfSatellitesInView("+numberOfSatellitesInView+")");
    }

    @Override
    public void setHorizontalDilutionOfPrecision(float horizontalDilutionOfPrecision)
    {
        printer.println("setHorizontalDilutionOfPrecision("+horizontalDilutionOfPrecision+")");
    }

    @Override
    public void setAntennaAltitude(float antennaAltitude, char unitsOfAntennaAltitude)
    {
        printer.println("setAntennaAltitude("+antennaAltitude+", "+unitsOfAntennaAltitude+")");
    }

    @Override
    public void setGeoidalSeparation(float geoidalSeparation, char unitsOfGeoidalSeparation)
    {
        printer.println("setGeoidalSeparation("+geoidalSeparation+", "+unitsOfGeoidalSeparation+")");
    }

    @Override
    public void setAgeOfDifferentialGPSData(int ageOfDifferentialGPSData)
    {
        printer.println("setAgeOfDifferentialGPSData("+ageOfDifferentialGPSData+")");
    }

    @Override
    public void setDifferentialReferenceStationID(int differentialReferenceStationID)
    {
        printer.println("setDifferentialReferenceStationID("+differentialReferenceStationID+")");
    }

    @Override
    public void setStatus(char status)
    {
        printer.println("setStatus("+status+")");
    }

    @Override
    public void setArrivalStatus(char arrivalStatus)
    {
        printer.println("setArrivalStatus("+arrivalStatus+")");
    }

    @Override
    public void setTimeDifference(float timeDifferenceA, float timeDifferenceB)
    {
        printer.println("setTimeDifference("+timeDifferenceA+", "+timeDifferenceB+")");
    }

    @Override
    public void setWaypointStatus(char waypointStatus)
    {
        printer.println("setWaypointStatus("+waypointStatus+")");
    }

    @Override
    public void setArrivalCircleRadius(float arrivalCircleRadius, char units)
    {
        printer.println("setArrivalCircleRadius("+arrivalCircleRadius+", "+units+")");
}

    @Override
    public void setWaypoint(InputReader reader, int waypoint)
    {
        printer.println("setWaypoint("+reader.getString(waypoint)+")");
    }

    @Override
    public void setTotalNumberOfMessages(int totalNumberOfMessages)
    {
        printer.println("setTotalNumberOfMessages("+totalNumberOfMessages+")");
    }

    @Override
    public void setMessageNumber(int messageNumber)
    {
        printer.println("setMessageNumber("+messageNumber+")");
    }

    @Override
    public void setSatellitePRNNumber(int satellitePRNNumber)
    {
        printer.println("setSatellitePRNNumber("+satellitePRNNumber+")");
    }

    @Override
    public void setGpsWeekNumber(int gpsWeekNumber)
    {
        printer.println("setGpsWeekNumber("+gpsWeekNumber+")");
    }

    @Override
    public void setSvHealth(int svHealth)
    {
        printer.println("setSvHealth("+svHealth+")");
    }

    @Override
    public void setEccentricity(float eccentricity)
    {
        printer.println("setEccentricity("+eccentricity+")");
    }

    @Override
    public void setAlmanacReferenceTime(float almanacReferenceTime)
    {
        printer.println("setAlmanacReferenceTime("+almanacReferenceTime+")");
    }

    @Override
    public void setInclinationAngle(float inclinationAngle)
    {
        printer.println("setInclinationAngle("+inclinationAngle+")");
    }

    @Override
    public void setRateOfRightAscension(float rateOfRightAscension)
    {
        printer.println("setRateOfRightAscension("+rateOfRightAscension+")");
    }

    @Override
    public void setRootOfSemiMajorAxis(float rootOfSemiMajorAxis)
    {
        printer.println("setRootOfSemiMajorAxis("+rootOfSemiMajorAxis+")");
    }

    @Override
    public void setArgumentOfPerigee(float argumentOfPerigee)
    {
        printer.println("setArgumentOfPerigee("+argumentOfPerigee+")");
    }

    @Override
    public void setLongitudeOfAscensionNode(float longitudeOfAscensionNode)
    {
        printer.println("setLongitudeOfAscensionNode("+longitudeOfAscensionNode+")");
    }

    @Override
    public void setMeanAnomaly(float meanAnomaly)
    {
        printer.println("setMeanAnomaly("+meanAnomaly+")");
    }

    @Override
    public void setF0ClockParameter(float f0ClockParameter)
    {
        printer.println("setF0ClockParameter("+f0ClockParameter+")");
    }

    @Override
    public void setF1ClockParameter(float f1ClockParameter)
    {
        printer.println("setF1ClockParameter("+f1ClockParameter+")");
    }

    @Override
    public void setStatus2(char status)
    {
        printer.println("setStatus2("+status+")");
    }

    @Override
    public void setBearingOriginToDestination(float bearingOriginToDestination, char mOrT)
    {
        printer.println("setBearingOriginToDestination("+bearingOriginToDestination+", "+mOrT+")");
    }

    @Override
    public void setBearingPresentPositionToDestination(float bearingPresentPositionToDestination, char mOrT)
    {
        printer.println("setBearingPresentPositionToDestination("+bearingPresentPositionToDestination+", "+mOrT+")");
    }

    @Override
    public void setHeadingToSteerToDestination(float headingToSteerToDestination, char mOrT)
    {
        printer.println("setHeadingToSteerToDestination("+headingToSteerToDestination+", "+mOrT+")");
    }

    @Override
    public void setFAAModeIndicator(char setFAAModeIndicator)
    {
        printer.println("setFAAModeIndicator("+setFAAModeIndicator+")");
    }

    @Override
    public void setHorizontalDatum(InputReader reader, int horizontalDatum)
    {
        printer.println("setHorizontalDatum("+reader.getString(horizontalDatum)+")");
    }

    @Override
    public void setMessageMode(char messageMode)
    {
        printer.println("setMessageMode("+messageMode+")");
    }

    @Override
    public void setWaypoints(InputReader reader, List<Integer> list)
    {
        List<String> sl = new ArrayList<>();
        for (int fieldRef : list)
        {
            sl.add(reader.getString(fieldRef));
        }
        printer.println("setWaypointList("+sl+")");
    }

    @Override
    public void setDistanceToWaypoint(float distanceToWaypoint, char units)
    {
        printer.println("setDistanceToWaypoint("+distanceToWaypoint+", "+units+")");
    }

    @Override
    public void setDepthBelowKeel(float depth, char unit)
    {
        printer.println("setDepthBelowKeel("+depth+", "+unit+")");
    }

    @Override
    public void setDepthBelowSurface(float depth, char unit)
    {
        printer.println("setDepthBelowSurface("+depth+", "+unit+")");
    }

    @Override
    public void setDepthBelowTransducer(float depth, char unit)
    {
        printer.println("setDepthBelowTransducer("+depth+", "+unit+")");
    }

    @Override
    public void setBearing(float bearing, char unit)
    {
        printer.println("setBearing("+bearing+", "+unit+")");
    }

    @Override
    public void setDepthOfWater(float depth, float offset)
    {
        printer.println("setDepthOfWater("+depth+", "+offset+")");
    }

    @Override
    public void setMagneticDeviation(float magneticDeviation)
    {
        printer.println("setMagneticDeviation("+magneticDeviation+")");
    }

    @Override
    public void setMagneticSensorHeading(float magneticSensorHeading)
    {
        printer.println("setMagneticSensorHeading("+magneticSensorHeading+")");
    }

    @Override
    public void setHeading(float heading, char unit)
    {
        printer.println("setHeading("+heading+", "+unit+")");
    }

    @Override
    public void setWaterTemperature(float waterTemperature, char unit)
    {
        printer.println("setWaterTemperature("+waterTemperature+", "+unit+")");
    }

    @Override
    public void setWindAngle(float windAngle, char unit)
    {
        printer.println("setWindAngle("+windAngle+", "+unit+")");
    }

    @Override
    public void setWindSpeed(float windSpeed, char unit)
    {
        printer.println("setWindSpeed("+windSpeed+", "+unit+")");
    }

    @Override
    public void setRateOfTurn(float rateOfTurn)
    {
        printer.println("setRateOfTurn("+rateOfTurn+")");
    }

    @Override
    public void setRpmSource(char setRpmSource)
    {
        printer.println("setRpmSource("+setRpmSource+")");
    }

    @Override
    public void setRpmSourceNumber(int rpmSourceNumber)
    {
        printer.println("setRpmSourceNumber("+rpmSourceNumber+")");
    }

    @Override
    public void setRpm(int rpm)
    {
        printer.println("setRpmS("+rpm+")");
    }

    @Override
    public void setPropellerPitch(float propellerPitch)
    {
        printer.println("setPropellerPitch("+propellerPitch+")");
    }

    @Override
    public void setStarboardRudderSensor(float starboardRudderSensor)
    {
        printer.println("setStarboardRudderSensor("+starboardRudderSensor+")");
    }

    @Override
    public void setPortRudderSensor(float portRudderSensor)
    {
        printer.println("setPortRudderSensor("+portRudderSensor+")");
    }

    @Override
    public void setWaterHeading(float waterHeading, char unit)
    {
        printer.println("setWaterHeading("+waterHeading+", "+unit+")");
    }

    @Override
    public void setWaterSpeed(float waterSpeed, char unit)
    {
        printer.println("setWaterSpeed("+waterSpeed+", "+unit+")");
    }

    @Override
    public void setWindDirection(float windDirection, char unit)
    {
        printer.println("setWindDirection("+windDirection+", "+unit+")");
    }

    @Override
    public void setVelocityToWaypoint(float velocityToWaypoint, char unit)
    {
        printer.println("setVelocityToWaypoint("+velocityToWaypoint+", "+unit+")");
    }

    @Override
    public void setClock(Clock clock)
    {
        printer.println("setClock(...)");
    }

    @Override
    public void setLocation(double latitude, double longitude)
    {
        printer.println("setLocation("+latitude+", "+longitude+")");
    }

    @Override
    public void setDestinationWaypointLocation(double latitude, double longitude)
    {
        printer.println("setDestinationWaypointLocation("+latitude+", "+longitude+")");
    }

    @Override
    public void setTargetName(InputReader reader, int name)
    {
        printer.println("setTargetName("+reader.getString(name)+")");
    }

    @Override
    public void setMessage(InputReader reader, int message)
    {
        printer.println("setMessage("+reader.getString(message)+")");
    }

    @Override
    public void setProprietaryType(InputReader reader, int fieldRef)
    {
        printer.println("setProprietaryType("+reader.getString(fieldRef)+")");
    }

    @Override
    public void setProprietaryData(InputReader reader, int fieldRef)
    {
        printer.println("setProprietaryData("+reader.getString(fieldRef)+")");
    }

}
