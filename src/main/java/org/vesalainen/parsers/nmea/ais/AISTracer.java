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

import java.util.ArrayList;
import java.util.List;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parsers.nmea.Clock;

/**
 * @author Timo Vesalainen
 */
public class AISTracer implements AISObserver
{
    @Override
    public void commit(String reason)
    {
        System.err.println("commit("+reason+")");
    }

    @Override
    public void rollback(String reason)
    {
        System.err.println("rollback("+reason+")");
    }

    @Override
    public void setNumberOfSentences(int numberOfSentences)
    {
        System.err.println("setNumberOfSentences("+numberOfSentences+")");
    }

    @Override
    public void setSentenceNumber(int sentenceNumber)
    {
        System.err.println("setSentenceNumber("+sentenceNumber+")");
    }

    @Override
    public void setSequenceMessageId(int sequentialMessageId)
    {
        System.err.println("setSequenceMessageId("+sequentialMessageId+")");
    }

    @Override
    public void setChannel(char channel)
    {
        System.err.println("setChannel("+channel+")");
    }

    @Override
    public void setMessageType(MessageTypes messageTypes)
    {
        System.err.println("setMessageType("+messageTypes+")");
    }

    @Override
    public void setRepeatIndicator(int repeatIndicator)
    {
        System.err.println("setRepeatIndicator("+repeatIndicator+")");
    }

    @Override
    public void setMMSI(int mmsi)
    {
        System.err.println("setMMSI("+mmsi+")");
        System.err.println("country("+MMSIMIDs.getCountry(mmsi)+")");
    }

    @Override
    public void setNavigationStatus(NavigationStatus navigationStatus)
    {
        System.err.println("setStatus("+navigationStatus+")");
    }

    @Override
    public void setTurn(float degreesPerMinute)
    {
        System.err.println("setTurn("+degreesPerMinute+")");
    }

    @Override
    public void setSpeed(float knots)
    {
        System.err.println("setSpeed("+knots+")");
    }

    @Override
    public void setAccuracy(boolean accuracy)
    {
        System.err.println("setAccuracy("+accuracy+")");
    }

    @Override
    public void setLongitude(double longitude)
    {
        System.err.println("setLongitude("+longitude+")");
    }

    @Override
    public void setLatitude(float latitude)
    {
        System.err.println("setLatitude("+latitude+")");
    }

    @Override
    public void setCourse(float course)
    {
        System.err.println("setCourse("+course+")");
    }

    @Override
    public void setHeading(int heading)
    {
        System.err.println("setHeading("+heading+")");
    }

    @Override
    public void setSecond(int second)
    {
        System.err.println("setSecond("+second+")");
    }

    @Override
    public void setManeuver(ManeuverIndicator maneuverIndicator)
    {
        System.err.println("setManeuver("+maneuverIndicator+")");
    }

    @Override
    public void setRAIM(boolean raim)
    {
        System.err.println("setRAIM("+raim+")");
    }

    @Override
    public void setRadioStatus(int radio)
    {
        System.err.println("setRadioStatus("+radio+")");
    }

    @Override
    public void setYear(int year)
    {
        System.err.println("setYear("+year+")");
    }

    @Override
    public void setMonth(int month)
    {
        System.err.println("setMonth("+month+")");
    }

    @Override
    public void setDay(int day)
    {
        System.err.println("setDay("+day+")");
    }

    @Override
    public void setHour(int hour)
    {
        System.err.println("setHour("+hour+")");
    }

    @Override
    public void setMinute(int minute)
    {
        System.err.println("setMinute("+minute+")");
    }

    @Override
    public void setEPFD(EPFDFixTypes epfdFixTypes)
    {
        System.err.println("setEPFD("+epfdFixTypes+")");
    }

    @Override
    public void setVersion(int version)
    {
        System.err.println("setVersion("+version+")");
    }

    @Override
    public void setIMONumber(int setIMONumber)
    {
        System.err.println("setIMONumber("+setIMONumber+")");
    }

    @Override
    public void setCallSign(InputReader reader, int fieldRef)
    {
        System.err.println("setCallSign("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setVesselName(InputReader reader, int fieldRef)
    {
        System.err.println("setVesselName("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setDimensionToBow(int dimension)
    {
        System.err.println("setDimensionToBow("+dimension+")");
    }

    @Override
    public void setDimensionToStern(int dimension)
    {
        System.err.println("setDimensionToStern("+dimension+")");
    }

    @Override
    public void setDimensionToPort(int dimension)
    {
        System.err.println("setDimensionToPort("+dimension+")");
    }

    @Override
    public void setDimensionToStarboard(int dimension)
    {
        System.err.println("setDimensionToStarboard("+dimension+")");
    }

    @Override
    public void setDraught(float meters)
    {
        System.err.println("setDraught("+meters+")");
    }

    @Override
    public void setDestination(InputReader reader, int fieldRef)
    {
        System.err.println("setDestination("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setDTE(boolean ready)
    {
        System.err.println("setDTE("+ready+")");
    }

    @Override
    public void setShipType(CodesForShipType codesForShipType)
    {
        System.err.println("setShipType("+codesForShipType+")");
    }

    @Override
    public void setSequenceNumber(int seq)
    {
        System.err.println("setSequenceNumber("+seq+")");
    }

    @Override
    public void setDestinationMMSI(int mmsi)
    {
        System.err.println("setDestinationMMSI("+mmsi+")");
    }

    @Override
    public void setRetransmit(boolean retransmit)
    {
        System.err.println("setRetransmit("+retransmit+")");
    }

    @Override
    public void setDAC(int dac)
    {
        System.err.println("setDAC("+dac+")");
    }

    @Override
    public void setFID(int fid)
    {
        System.err.println("setFID("+fid+")");
    }

    @Override
    public void setLastPort(InputReader reader, int fieldRef)
    {
        System.err.println("setLastPort("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setLastPortMonth(int month)
    {
        System.err.println("setLastPortMonth("+month+")");
    }

    @Override
    public void setLastPortDay(int day)
    {
        System.err.println("setLastPortDay("+day+")");
    }

    @Override
    public void setLastPortHour(int hour)
    {
        System.err.println("setLastPortHour("+hour+")");
    }

    @Override
    public void setLastPortMinute(int minute)
    {
        System.err.println("setLastPortMinute("+minute+")");
    }

    @Override
    public void setNextPort(InputReader reader, int fieldRef)
    {
        System.err.println("setNextPort("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setNextPortMonth(int month)
    {
        System.err.println("setNextPortMonth("+month+")");
    }

    @Override
    public void setNextPortDay(int day)
    {
        System.err.println("setNextPortDay("+day+")");
    }

    @Override
    public void setNextPortHour(int hour)
    {
        System.err.println("setNextPortHour("+hour+")");
    }

    @Override
    public void setNextPortMinute(int minute)
    {
        System.err.println("setNextPortMinute("+minute+")");
    }

    @Override
    public void setMainDangerousGood(InputReader reader, int fieldRef)
    {
        System.err.println("setMainDangerousGood("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setIMDCategory(InputReader reader, int fieldRef)
    {
        System.err.println("setIMDCategory("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setUNNumber(int unid)
    {
        System.err.println("UNNumber("+unid+")");
    }

    @Override
    public void setAmountOfCargo(int amount)
    {
        System.err.println("AmountOfCargo("+amount+")");
    }

    @Override
    public void setUnitOfQuantity(CargoUnitCodes cargoUnitCodes)
    {
        System.err.println("setUnitOfQuantity("+cargoUnitCodes+")");
    }

    @Override
    public void setFromHour(int hour)
    {
        System.err.println("setFromHour("+hour+")");
    }

    @Override
    public void setFromMinute(int minute)
    {
        System.err.println("setFromMinute("+minute+")");
    }

    @Override
    public void setToHour(int hour)
    {
        System.err.println("setToHour("+hour+")");
    }

    @Override
    public void setToMinute(int minute)
    {
        System.err.println("setToMinute("+minute+")");
    }

    @Override
    public void setCurrentDirection(int currentDirection)
    {
        System.err.println("setCurrentDirection("+currentDirection+")");
    }

    @Override
    public void setCurrentSpeed(float knots)
    {
        System.err.println("setCurrentSpeed("+knots+")");
    }

    @Override
    public void setPersonsOnBoard(int persons)
    {
        System.err.println("setPersonsOnBoard("+persons+")");
    }

    @Override
    public void setLinkage(int id)
    {
        System.err.println("setLinkage("+id+")");
    }

    @Override
    public void setPortname(InputReader reader, int fieldRef)
    {
        System.err.println("setPortname("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setAreaNotice(AreaNoticeDescription areaNoticeDescription)
    {
        System.err.println("setAreaNotice("+areaNoticeDescription+")");
    }

    @Override
    public void setDuration(int duration)
    {
        System.err.println("setDuration("+duration+")");
    }

    @Override
    public void setShape(SubareaType subareaType)
    {
        System.err.println("setShape("+subareaType+")");
    }

    @Override
    public void setScale(int scale)
    {
        System.err.println("setScale("+scale+")");
    }

    @Override
    public void setPrecision(int precision)
    {
        System.err.println("setPrecision("+precision+")");
    }

    @Override
    public void setRadius(int radius)
    {
        System.err.println("setRadius("+radius+")");
    }

    @Override
    public void setEast(int east)
    {
        System.err.println("setEast("+east+")");
    }

    @Override
    public void setNorth(int north)
    {
        System.err.println("setNorth("+north+")");
    }

    @Override
    public void setOrientation(int orientation)
    {
        System.err.println("setOrientation("+orientation+")");
    }

    @Override
    public void setLeft(int left)
    {
        System.err.println("setLeft("+left+")");
    }

    @Override
    public void setRight(int right)
    {
        System.err.println("setRight("+right+")");
    }

    @Override
    public void setBearing(int bearing)
    {
        System.err.println("setBearing("+bearing+")");
    }

    @Override
    public void setDistance(int distance)
    {
        System.err.println("setDistance("+distance+")");
    }

    @Override
    public void setText(InputReader reader, int fieldRef)
    {
        System.err.println("setText("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setBerthLength(int meters)
    {
        System.err.println("setBerthLength("+meters+")");
    }

    @Override
    public void setBerthDepth(float meters)
    {
        System.err.println("setBerthDepth("+meters+")");
    }

    @Override
    public void setServicesAvailability(boolean available)
    {
        System.err.println("setServicesAvailability("+available+")");
    }

    @Override
    public void setBerthName(InputReader reader, int fieldRef)
    {
        System.err.println("setBerthName("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setMooringPosition(MooringPosition mooringPosition)
    {
        System.err.println("setMooringPosition("+mooringPosition+")");
    }

    @Override
    public void setAgentServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setAgentServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setFuelServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setFuelServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setChandlerServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setChandlerServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setStevedoreServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setStevedoreServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setElectricalServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setElectricalServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setWaterServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setWaterServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setCustomsServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setCustomsServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setCartageServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setCartageServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setCraneServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setCraneServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setLiftServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setLiftServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setMedicalServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setMedicalServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setNavrepairServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setNavrepairServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setProvisionsServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setProvisionsServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setShiprepairServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setShiprepairServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setSurveyorServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setSurveyorServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setSteamServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setSteamServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setTugsServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setTugsServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setSolidwasteServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setSolidwasteServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setLiquidwasteServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setLiquidwasteServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setHazardouswasteServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setHazardouswasteServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setBallastServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setBallastServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setAdditionalServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setAdditionalServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setRegional1ServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setRegional1ServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setRegional2ServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setRegional2ServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setFuture1ServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setFuture1ServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setFuture2ServiceStatus(ServiceStatus serviceStatus)
    {
        System.err.println("setFuture2ServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setSender(int sender)
    {
        System.err.println("setSender("+sender+")");
    }

    @Override
    public void setWaypointCount(int count)
    {
        System.err.println("setWaypointCount("+count+")");
    }

    @Override
    public void setRouteType(RouteTypeCodes routeTypeCodes)
    {
        System.err.println("setRouteType("+routeTypeCodes+")");
    }

    @Override
    public void setDescription(InputReader reader, int fieldRef)
    {
        System.err.println("setDescription("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setMMSI1(int mmsi)
    {
        System.err.println("setMMSI1("+mmsi+")");
    }

    @Override
    public void setMMSI2(int mmsi)
    {
        System.err.println("setMMSI2("+mmsi+")");
    }

    @Override
    public void setMMSI3(int mmsi)
    {
        System.err.println("setMMSI3("+mmsi+")");
    }

    @Override
    public void setMMSI4(int mmsi)
    {
        System.err.println("setMMSI4("+mmsi+")");
    }

    @Override
    public void setAverageWindSpeed(int knots)
    {
        System.err.println("setAverageWindSpeed("+knots+")");
    }

    @Override
    public void setGustSpeed(int knots)
    {
        System.err.println("setGustSpeed("+knots+")");
    }

    @Override
    public void setWindDirection(int degrees)
    {
        System.err.println("setWindDirection("+degrees+")");
    }

    @Override
    public void setWindGustDirection(int degrees)
    {
        System.err.println("setWindGustDirection("+degrees+")");
    }

    @Override
    public void setAirTemperature(float degrees)
    {
        System.err.println("setAirTemperature("+degrees+")");
    }

    @Override
    public void setRelativeHumidity(int humidity)
    {
        System.err.println("setRelativeHumidity("+humidity+")");
    }

    @Override
    public void setDewPoint(float degrees)
    {
        System.err.println("setDewPoint("+degrees+")");
    }

    @Override
    public void setAirPressure(int pressure)
    {
        System.err.println("setAirPressure("+pressure+")");
    }

    @Override
    public void setAirPressureTendency(int tendency)
    {
        System.err.println("setAirPressureTendency("+tendency+")");
    }

    @Override
    public void setVisibility(float nm)
    {
        System.err.println("setVisibility("+nm+")");
    }

    @Override
    public void setWaterLevel(float meters)
    {
        System.err.println("setWaterLevel("+meters+")");
    }

    @Override
    public void setWaterLevelTrend(int trend)
    {
        System.err.println("setWaterLevelTrend("+trend+")");
    }

    @Override
    public void setSurfaceCurrentSpeed(float knots)
    {
        System.err.println("setSurfaceCurrentSpeed("+knots+")");
    }

    @Override
    public void setCurrentSpeed2(float knots)
    {
        System.err.println("setCurrentSpeed2("+knots+")");
    }

    @Override
    public void setCurrentDirection2(int degrees)
    {
        System.err.println("setCurrentDirection2("+degrees+")");
    }

    @Override
    public void setMeasurementDepth2(float meters)
    {
        System.err.println("setMeasurementDepth2("+meters+")");
    }

    @Override
    public void setCurrentSpeed3(float knots)
    {
        System.err.println("setCurrentSpeed3("+knots+")");
    }

    @Override
    public void setCurrentDirection3(int degrees)
    {
        System.err.println("setCurrentDirection3("+degrees+")");
    }

    @Override
    public void setMeasurementDepth3(float meters)
    {
        System.err.println("setMeasurementDepth3("+meters+")");
    }

    @Override
    public void setWaveHeight(float meters)
    {
        System.err.println("setWaveHeight("+meters+")");
    }

    @Override
    public void setWavePeriod(int seconds)
    {
        System.err.println("setWavePeriod("+seconds+")");
    }

    @Override
    public void setWaveDirection(int degrees)
    {
        System.err.println("setWaveDirection("+degrees+")");
    }

    @Override
    public void setSwellHeight(float meters)
    {
        System.err.println("setSwellHeight("+meters+")");
    }

    @Override
    public void setSwellPeriod(int seconds)
    {
        System.err.println("setSwellPeriod("+seconds+")");
    }

    @Override
    public void setSwellDirection(int degrees)
    {
        System.err.println("setSwellDirection("+degrees+")");
    }

    @Override
    public void setWaterTemperature(float degrees)
    {
        System.err.println("setWaterTemperature("+degrees+")");
    }

    @Override
    public void setSalinity(float f)
    {
        System.err.println("setSalinity("+f+")");
    }

    @Override
    public void setIce(int ice)
    {
        System.err.println("setIce("+ice+")");
    }

    @Override
    public void setPrecipitation(PrecipitationTypes precipitationTypes)
    {
        System.err.println("setPrecipitation("+precipitationTypes+")");
    }

    @Override
    public void setSeaState(BeaufortScale beaufortScale)
    {
        System.err.println("setSeaState("+beaufortScale+")");
    }

    @Override
    public void setReasonForClosing(InputReader reader, int fieldRef)
    {
        System.err.println("setReasonForClosing("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setClosingFrom(InputReader reader, int fieldRef)
    {
        System.err.println("setClosingFrom("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setClosingTo(InputReader reader, int fieldRef)
    {
        System.err.println("setClosingTo("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setUnitOfExtension(ExtensionUnit unit)
    {
        System.err.println("setUnitOfExtension("+unit+")");
    }


    @Override
    public void setFromMonth(int month)
    {
        System.err.println("setFromMonth("+month+")");
    }

    @Override
    public void setFromDay(int day)
    {
        System.err.println("setFromDay("+day+")");
    }

    @Override
    public void setToMonth(int month)
    {
        System.err.println("setToMonth("+month+")");
    }

    @Override
    public void setToDay(int day)
    {
        System.err.println("setToDay("+day+")");
    }

    @Override
    public void setAirDraught(int meters)
    {
        System.err.println("setAirDraught("+meters+")");
    }

    @Override
    public void setIdType(TargetIdentifierType targetIdentifierType)
    {
        System.err.println("setIdType("+targetIdentifierType+")");
    }

    @Override
    public void setId(long id)
    {
        System.err.println("setId("+id+")");
    }

    @Override
    public void setStation(InputReader reader, int fieldRef)
    {
        System.err.println("setStation("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setSignal(MarineTrafficSignals marineTrafficSignals)
    {
        System.err.println("setSignal("+marineTrafficSignals+")");
    }

    @Override
    public void setNextSignal(MarineTrafficSignals marineTrafficSignals)
    {
        System.err.println("setNextSignal("+marineTrafficSignals+")");
    }

    @Override
    public void setVariant(int variant)
    {
        System.err.println("setVariant("+variant+")");
    }

    @Override
    public void setLocation(InputReader reader, int fieldRef)
    {
        System.err.println("setLocation("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setWeather(WMOCode45501 wmoCode45501)
    {
        System.err.println("setWeather("+wmoCode45501+")");
    }

    @Override
    public void setVisibilityLimit(boolean reached)
    {
        System.err.println("setVisibilityLimit("+reached+")");
    }

    @Override
    public void setAirPressure(float pressure)
    {
        System.err.println("setAirPressure("+pressure+")");
    }

    @Override
    public void setAirPressureChange(float delta)
    {
        System.err.println("setAirPressureChange("+delta+")");
    }

    @Override
    public void setPrefix(int numberOfSentences, int sentenceNumber, int sequentialMessageID, char channel)
    {
        System.err.println("setPrefix("+numberOfSentences+", "+sentenceNumber+", "+sequentialMessageID+", "+channel+")");
    }

    @Override
    public void setOwnMessage(boolean ownMessage)
    {
        System.err.println("setOwnMessage("+ownMessage+")");
    }

    @Override
    public void setName(InputReader reader, int fieldRef)
    {
        System.err.println("setName("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setNameExtension(InputReader reader, int fieldRef)
    {
        System.err.println("setNameExtension("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setVendorId(InputReader reader, int fieldRef)
    {
        System.err.println("setVendorId("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setNavaidType(NavaidTypes navaidType)
    {
        System.err.println("setNavaidType("+navaidType+")");
    }

}
