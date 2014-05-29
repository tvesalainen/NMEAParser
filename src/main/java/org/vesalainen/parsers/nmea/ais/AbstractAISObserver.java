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

package org.vesalainen.parsers.nmea.ais;

import org.vesalainen.parser.util.InputReader;

/**
 * @author Timo Vesalainen
 */
public class AbstractAISObserver implements AISObserver
{
    protected int numberOfSentences;
    protected int sentenceNumber;
    protected int sequentialMessageID;
    protected char channel;

    @Override
    public void setPrefix(int numberOfSentences, int sentenceNumber, int sequentialMessageID, char channel)
    {
        this.numberOfSentences = numberOfSentences;
        this.sentenceNumber = sentenceNumber;
        this.sequentialMessageID = sequentialMessageID;
        this.channel = channel;
    }

    @Override
    public void setMessageType(MessageTypes messageTypes)
    {
        
    }

    @Override
    public void setNumberOfSentences(int numberOfSentences)
    {
        
    }

    @Override
    public void setSentenceNumber(int sentenceNumber)
    {
        
    }

    @Override
    public void setSequenceMessageId(int sequentialMessageId)
    {
        
    }

    @Override
    public void setChannel(char channel)
    {
        
    }

    @Override
    public void setRepeatIndicator(int repeatIndicator)
    {
        
    }

    @Override
    public void setMMSI(int mmsi)
    {
        
    }

    @Override
    public void setNavigationStatus(NavigationStatus navigationStatus)
    {
        
    }

    @Override
    public void setTurn(float degreesPerMinute)
    {
        
    }

    @Override
    public void setSpeed(float knots)
    {
        
    }

    @Override
    public void setAccuracy(boolean accuracy)
    {
        
    }

    @Override
    public void setLongitude(float degrees)
    {
        
    }

    @Override
    public void setLatitude(float degrees)
    {
        
    }

    @Override
    public void setCourse(float cog)
    {
        
    }

    @Override
    public void setHeading(int heading)
    {
        
    }

    @Override
    public void setSecond(int second)
    {
        
    }

    @Override
    public void setManeuver(ManeuverIndicator maneuverIndicator)
    {
        
    }

    @Override
    public void setRAIM(boolean raim)
    {
        
    }

    @Override
    public void setRadioStatus(int radio)
    {
        
    }

    @Override
    public void setYear(int year)
    {
        
    }

    @Override
    public void setMonth(int month)
    {
        
    }

    @Override
    public void setDay(int day)
    {
        
    }

    @Override
    public void setHour(int hour)
    {
        
    }

    @Override
    public void setMinute(int minute)
    {
        
    }

    @Override
    public void setEPFD(EPFDFixTypes epfdFixTypes)
    {
        
    }

    @Override
    public void setVersion(int version)
    {
        
    }

    @Override
    public void setIMONumber(int imo)
    {
        
    }

    @Override
    public void setCallSign(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setVesselName(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setDimensionToBow(int dimension)
    {
        
    }

    @Override
    public void setDimensionToStern(int dimension)
    {
        
    }

    @Override
    public void setDimensionToPort(int dimension)
    {
        
    }

    @Override
    public void setDimensionToStarboard(int dimension)
    {
        
    }

    @Override
    public void setDraught(float meters)
    {
        
    }

    @Override
    public void setDestination(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setDTE(boolean ready)
    {
        
    }

    @Override
    public void setShipType(CodesForShipType codesForShipType)
    {
        
    }

    @Override
    public void setSequenceNumber(int seq)
    {
        
    }

    @Override
    public void setDestinationMMSI(int mmsi)
    {
        
    }

    @Override
    public void setRetransmit(boolean retransmit)
    {
        
    }

    @Override
    public void setDAC(int dac)
    {
        
    }

    @Override
    public void setFID(int fid)
    {
        
    }

    @Override
    public void setLastPort(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setLastPortMonth(int month)
    {
        
    }

    @Override
    public void setLastPortDay(int day)
    {
        
    }

    @Override
    public void setLastPortHour(int hour)
    {
        
    }

    @Override
    public void setLastPortMinute(int minute)
    {
        
    }

    @Override
    public void setNextPort(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setNextPortMonth(int month)
    {
        
    }

    @Override
    public void setNextPortDay(int day)
    {
        
    }

    @Override
    public void setNextPortHour(int hour)
    {
        
    }

    @Override
    public void setNextPortMinute(int minute)
    {
        
    }

    @Override
    public void setMainDangerousGood(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setIMDCategory(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setUNNumber(int unid)
    {
        
    }

    @Override
    public void setAmountOfCargo(int amount)
    {
        
    }

    @Override
    public void setUnitOfQuantity(CargoUnitCodes cargoUnitCodes)
    {
        
    }

    @Override
    public void setFromHour(int hour)
    {
        
    }

    @Override
    public void setFromMinute(int minute)
    {
        
    }

    @Override
    public void setToHour(int hour)
    {
        
    }

    @Override
    public void setToMinute(int minute)
    {
        
    }

    @Override
    public void setCurrentDirection(int currentDirection)
    {
        
    }

    @Override
    public void setCurrentSpeed(float knots)
    {
        
    }

    @Override
    public void setPersonsOnBoard(int persons)
    {
        
    }

    @Override
    public void setLinkage(int id)
    {
        
    }

    @Override
    public void setPortname(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setAreaNotice(AreaNoticeDescription areaNoticeDescription)
    {
        
    }

    @Override
    public void setDuration(int duration)
    {
        
    }

    @Override
    public void setShape(SubareaType subareaType)
    {
        
    }

    @Override
    public void setScale(int scale)
    {
        
    }

    @Override
    public void setPrecision(int precision)
    {
        
    }

    @Override
    public void setRadius(int radius)
    {
        
    }

    @Override
    public void setEast(int east)
    {
        
    }

    @Override
    public void setNorth(int north)
    {
        
    }

    @Override
    public void setOrientation(int orientation)
    {
        
    }

    @Override
    public void setLeft(int left)
    {
        
    }

    @Override
    public void setRight(int right)
    {
        
    }

    @Override
    public void setBearing(int bearing)
    {
        
    }

    @Override
    public void setDistance(int distance)
    {
        
    }

    @Override
    public void setText(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setBerthLength(int meters)
    {
        
    }

    @Override
    public void setBerthDepth(float meters)
    {
        
    }

    @Override
    public void setServicesAvailability(boolean available)
    {
        
    }

    @Override
    public void setBerthName(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setMooringPosition(MooringPosition mooringPosition)
    {
        
    }

    @Override
    public void setAgentServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setFuelServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setChandlerServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setStevedoreServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setElectricalServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setWaterServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setCustomsServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setCartageServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setCraneServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setLiftServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setMedicalServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setNavrepairServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setProvisionsServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setShiprepairServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setSurveyorServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setSteamServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setTugsServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setSolidwasteServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setLiquidwasteServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setHazardouswasteServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setBallastServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setAdditionalServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setRegional1ServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setRegional2ServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setFuture1ServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setFuture2ServiceStatus(ServiceStatus serviceStatus)
    {
        
    }

    @Override
    public void setSender(int sender)
    {
        
    }

    @Override
    public void setWaypointCount(int count)
    {
        
    }

    @Override
    public void setRouteType(RouteTypeCodes routeTypeCodes)
    {
        
    }

    @Override
    public void setDescription(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setMMSI1(int mmsi)
    {
        
    }

    @Override
    public void setMMSI2(int mmsi)
    {
        
    }

    @Override
    public void setMMSI3(int mmsi)
    {
        
    }

    @Override
    public void setMMSI4(int mmsi)
    {
        
    }

    @Override
    public void setAverageWindSpeed(int knots)
    {
        
    }

    @Override
    public void setGustSpeed(int knots)
    {
        
    }

    @Override
    public void setWindDirection(int degrees)
    {
        
    }

    @Override
    public void setWindGustDirection(int degrees)
    {
        
    }

    @Override
    public void setAirTemperature(float degrees)
    {
        
    }

    @Override
    public void setRelativeHumidity(int humidity)
    {
        
    }

    @Override
    public void setDewPoint(float degrees)
    {
        
    }

    @Override
    public void setAirPressure(int pressure)
    {
        
    }

    @Override
    public void setAirPressureTendency(int tendency)
    {
        
    }

    @Override
    public void setVisibility(float nm)
    {
        
    }

    @Override
    public void setWaterLevel(float meters)
    {
        
    }

    @Override
    public void setWaterLevelTrend(int trend)
    {
        
    }

    @Override
    public void setSurfaceCurrentSpeed(float knots)
    {
        
    }

    @Override
    public void setCurrentSpeed2(float knots)
    {
        
    }

    @Override
    public void setCurrentDirection2(int degrees)
    {
        
    }

    @Override
    public void setMeasurementDepth2(float meters)
    {
        
    }

    @Override
    public void setCurrentSpeed3(float knots)
    {
        
    }

    @Override
    public void setCurrentDirection3(int degrees)
    {
        
    }

    @Override
    public void setMeasurementDepth3(float meters)
    {
        
    }

    @Override
    public void setWaveHeight(float meters)
    {
        
    }

    @Override
    public void setWavePeriod(int seconds)
    {
        
    }

    @Override
    public void setWaveDirection(int degrees)
    {
        
    }

    @Override
    public void setSwellHeight(float meters)
    {
        
    }

    @Override
    public void setSwellPeriod(int seconds)
    {
        
    }

    @Override
    public void setSwellDirection(int degrees)
    {
        
    }

    @Override
    public void setWaterTemperature(float degrees)
    {
        
    }

    @Override
    public void setSalinity(float f)
    {
        
    }

    @Override
    public void setIce(int ice)
    {
        
    }

    @Override
    public void setPrecipitation(PrecipitationTypes precipitationTypes)
    {
        
    }

    @Override
    public void setSeaState(BeaufortScale beaufortScale)
    {
        
    }

    @Override
    public void setReasonForClosing(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setClosingFrom(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setClosingTo(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setUnitOfExtension(ExtensionUnit unit)
    {
        
    }

    @Override
    public void setFromMonth(int month)
    {
        
    }

    @Override
    public void setFromDay(int day)
    {
        
    }

    @Override
    public void setToMonth(int month)
    {
        
    }

    @Override
    public void setToDay(int day)
    {
        
    }

    @Override
    public void setAirDraught(int meters)
    {
        
    }

    @Override
    public void setIdType(TargetIdentifierType targetIdentifierType)
    {
        
    }

    @Override
    public void setId(long id)
    {
        
    }

    @Override
    public void setStation(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setSignal(MarineTrafficSignals marineTrafficSignals)
    {
        
    }

    @Override
    public void setNextSignal(MarineTrafficSignals marineTrafficSignals)
    {
        
    }

    @Override
    public void setVariant(int variant)
    {
        
    }

    @Override
    public void setLocation(InputReader reader, int fieldRef)
    {
        
    }

    @Override
    public void setWeather(WMOCode45501 wmoCode45501)
    {
        
    }

    @Override
    public void setVisibilityLimit(boolean reached)
    {
        
    }

    @Override
    public void setAirPressure(float pressure)
    {
        
    }

    @Override
    public void setAirPressureChange(float delta)
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
    public void setOwnMessage(boolean ownMessage)
    {
    }

    @Override
    public void setName(InputReader reader, int fieldRef)
    {
    }

    @Override
    public void setNameExtension(InputReader reader, int fieldRef)
    {
    }

    @Override
    public void setVendorId(InputReader reader, int fieldRef)
    {
    }

    @Override
    public void setNavaidType(NavaidTypes navaidTypes)
    {
    }

    @Override
    public void setError(String string)
    {
    }

}
