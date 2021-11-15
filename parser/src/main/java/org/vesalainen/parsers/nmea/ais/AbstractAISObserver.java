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

import org.vesalainen.parsers.mmsi.MMSIType;
import org.vesalainen.parsers.nmea.NMEAClock;

/**
 * @author Timo Vesalainen
 */
public class AbstractAISObserver implements AISObserver
{
    protected int numberOfSentences;
    protected int sentenceNumber;
    protected int sequentialMessageID;
    protected char channel;
    protected MessageTypes messageType;
    protected int mmsi;

    @Override
    public void setMessageType(MessageTypes messageType)
    {
        this.messageType = messageType;
    }

    @Override
    public void setRepeatIndicator(int repeatIndicator)
    {
        
    }

    @Override
    public void setMmsi(int mmsi)
    {
        this.mmsi = mmsi;
    }

    @Override
    public void setChannel(char channel)
    {
    }

    @Override
    public void setNavigationStatus(NavigationStatus navigationStatus)
    {
        
    }

    @Override
    public void setRateOfTurn(float degreesPerMinute)
    {
        
    }

    @Override
    public void setSpeed(float knots)
    {
        
    }

    @Override
    public void setPositionAccuracy(boolean accuracy)
    {
        
    }

    @Override
    public void setLongitude(double degrees)
    {
        
    }

    @Override
    public void setLatitude(double degrees)
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
    public void setRaim(boolean raim)
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
    public void setEpfd(EPFDFixTypes epfdFixTypes)
    {
        
    }

    @Override
    public void setVersion(int version)
    {
        
    }

    @Override
    public void setImoNumber(int imo)
    {
        
    }

    @Override
    public void setCallSign(String str)
    {
        
    }

    @Override
    public void setVesselName(String str)
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
    public void setDestination(String str)
    {
        
    }

    @Override
    public void setDte(boolean ready)
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
    public void setDac(int dac)
    {
        
    }

    @Override
    public void setFid(int fid)
    {
        
    }

    @Override
    public void setLastPort(String str)
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
    public void setNextPort(String str)
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
    public void setMainDangerousGood(String str)
    {
        
    }

    @Override
    public void setImdCategory(String str)
    {
        
    }

    @Override
    public void setUnNumber(int unid)
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
    public void setSurfaceCurrentDirection(int currentDirection)
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
    public void setPortname(String str)
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
    public void setBerthName(String str)
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
    public void setDescription(String str)
    {
        
    }

    @Override
    public void setMmsi1(int mmsi)
    {
        
    }

    @Override
    public void setMmsi2(int mmsi)
    {
        
    }

    @Override
    public void setMmsi3(int mmsi)
    {
        
    }

    @Override
    public void setMmsi4(int mmsi)
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
    public void setAirPressureTendency(Tendency tendency)
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
    public void setWaterLevelTrend(Tendency trend)
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
    public void setMeasurementDepth2(int meters)
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
    public void setMeasurementDepth3(int meters)
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
    public void setReasonForClosing(String str)
    {
        
    }

    @Override
    public void setClosingFrom(String str)
    {
        
    }

    @Override
    public void setClosingTo(String str)
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
    public void setStation(String str)
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
    public void setLocation(String str)
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
    public void start(String reason)
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
    public void setName(String str)
    {
    }

    @Override
    public void setNameExtension(String str)
    {
    }

    @Override
    public void setVendorId(String str)
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

    @Override
    public void setClock(NMEAClock clock)
    {
    }

    @Override
    public void setAisVersion(int arg)
    {
    }

    @Override
    public void setWaterPressure(float decibar)
    {
    }

    @Override
    public void setAltitude(int meters)
    {
    }

    @Override
    public void setSensorAltitude(float meters)
    {
    }

    @Override
    public void setOffset1(int arg)
    {
    }

    @Override
    public void setOffset2(int arg)
    {
    }

    @Override
    public void setIncrement1(int arg)
    {
    }

    @Override
    public void setIncrement2(int arg)
    {
    }

    @Override
    public void setCsUnit(boolean cs)
    {
    }

    @Override
    public void setDisplay(boolean hasDisplay)
    {
    }

    @Override
    public void setDsc(boolean dsc)
    {
    }

    @Override
    public void setBand(boolean flag)
    {
    }

    @Override
    public void setMsg22(boolean b)
    {
    }

    @Override
    public void setAssignedMode(boolean b)
    {
    }

    @Override
    public void setReservedSlots1(int arg)
    {
    }

    @Override
    public void setReservedSlots2(int arg)
    {
    }

    @Override
    public void setReservedSlots3(int arg)
    {
    }

    @Override
    public void setReservedSlots4(int arg)
    {
    }

    @Override
    public void setTimeout1(int arg)
    {
    }

    @Override
    public void setTimeout2(int arg)
    {
    }

    @Override
    public void setTimeout3(int arg)
    {
    }

    @Override
    public void setTimeout4(int arg)
    {
    }

    @Override
    public void setOffset3(int arg)
    {
    }

    @Override
    public void setOffset4(int arg)
    {
    }

    @Override
    public void setIncrement3(int arg)
    {
    }

    @Override
    public void setIncrement4(int arg)
    {
    }

    @Override
    public void setOffPosition(boolean off)
    {
    }

    @Override
    public void setVirtualAid(boolean virtual)
    {
    }

    @Override
    public void setChannelA(int arg)
    {
    }

    @Override
    public void setChannelB(int arg)
    {
    }

    @Override
    public void setTransceiverMode(TransceiverModes transmitModes)
    {
    }

    @Override
    public void setPower(boolean high)
    {
    }

    @Override
    public void setNeLongitude(float f)
    {
    }

    @Override
    public void setSwLongitude(float f)
    {
    }

    @Override
    public void setNeLatitude(float f)
    {
    }

    @Override
    public void setSwLatitude(float f)
    {
    }

    @Override
    public void setAddressed(boolean addressed)
    {
    }

    @Override
    public void setChannelABand(boolean band)
    {
    }

    @Override
    public void setChannelBBand(boolean band)
    {
    }

    @Override
    public void setZoneSize(int arg)
    {
    }

    @Override
    public void setPartNumber(int arg)
    {
    }

    @Override
    public void setMothershipMMSI(int arg)
    {
    }

    @Override
    public void setUnitModelCode(int arg)
    {
    }

    @Override
    public void setSerialNumber(int arg)
    {
    }

    @Override
    public void setEtaMonth(int month)
    {
    }

    @Override
    public void setEtaDay(int day)
    {
    }

    @Override
    public void setEtaHour(int hour)
    {
    }

    @Override
    public void setEtaMinute(int minute)
    {
    }

    @Override
    public void setSubarea(CharSequence seq)
    {
    }

    @Override
    public void setRegional(int arg)
    {
    }

}
