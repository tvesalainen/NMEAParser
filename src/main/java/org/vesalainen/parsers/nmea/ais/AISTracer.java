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
import org.vesalainen.parsers.nmea.Clock;
import org.vesalainen.util.AppendablePrinter;

/**
 * @author Timo Vesalainen
 */
public class AISTracer implements AISObserver
{
    private AppendablePrinter printer;

    public AISTracer()
    {
        this(System.err);
    }

    public AISTracer(Appendable appendable)
    {
        this.printer = new AppendablePrinter(appendable);
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
    public void setError(String error)
    {
        printer.println("setError("+error+")");
    }

    @Override
    public void setNumberOfSentences(int numberOfSentences)
    {
        printer.println("setNumberOfSentences("+numberOfSentences+")");
    }

    @Override
    public void setSentenceNumber(int sentenceNumber)
    {
        printer.println("setSentenceNumber("+sentenceNumber+")");
    }

    @Override
    public void setSequenceMessageId(int sequentialMessageId)
    {
        printer.println("setSequenceMessageId("+sequentialMessageId+")");
    }

    @Override
    public void setChannel(char channel)
    {
        printer.println("setChannel("+channel+")");
    }

    @Override
    public void setMessageType(MessageTypes messageTypes)
    {
        printer.println("setMessageType("+messageTypes+")");
    }

    @Override
    public void setRepeatIndicator(int repeatIndicator)
    {
        printer.println("setRepeatIndicator("+repeatIndicator+")");
    }

    @Override
    public void setMMSI(int mmsi)
    {
        printer.format("setMMSI(%09d)\n", mmsi);
    }

    @Override
    public void setNavigationStatus(NavigationStatus navigationStatus)
    {
        printer.println("setStatus("+navigationStatus+")");
    }

    @Override
    public void setRateOfTurn(float degreesPerMinute)
    {
        printer.println("setTurn("+degreesPerMinute+")");
    }

    @Override
    public void setSpeed(float knots)
    {
        printer.println("setSpeed("+knots+")");
    }

    @Override
    public void setAccuracy(boolean accuracy)
    {
        printer.println("setAccuracy("+accuracy+")");
    }

    @Override
    public void setLongitude(float degrees)
    {
        printer.println("setLongitude("+degrees+")");
    }

    @Override
    public void setLatitude(float latitude)
    {
        printer.println("setLatitude("+latitude+")");
    }

    @Override
    public void setCourse(float course)
    {
        printer.println("setCourse("+course+")");
    }

    @Override
    public void setHeading(int heading)
    {
        printer.println("setHeading("+heading+")");
    }

    @Override
    public void setSecond(int second)
    {
        printer.println("setSecond("+second+")");
    }

    @Override
    public void setManeuver(ManeuverIndicator maneuverIndicator)
    {
        printer.println("setManeuver("+maneuverIndicator+")");
    }

    @Override
    public void setRAIM(boolean raim)
    {
        printer.println("setRAIM("+raim+")");
    }

    @Override
    public void setRadioStatus(int radio)
    {
        printer.println("setRadioStatus("+radio+")");
    }

    @Override
    public void setYear(int year)
    {
        printer.println("setYear("+year+")");
    }

    @Override
    public void setMonth(int month)
    {
        printer.println("setMonth("+month+")");
    }

    @Override
    public void setDay(int day)
    {
        printer.println("setDay("+day+")");
    }

    @Override
    public void setHour(int hour)
    {
        printer.println("setHour("+hour+")");
    }

    @Override
    public void setMinute(int minute)
    {
        printer.println("setMinute("+minute+")");
    }

    @Override
    public void setEPFD(EPFDFixTypes epfdFixTypes)
    {
        printer.println("setEPFD("+epfdFixTypes+")");
    }

    @Override
    public void setVersion(int version)
    {
        printer.println("setVersion("+version+")");
    }

    @Override
    public void setIMONumber(int setIMONumber)
    {
        printer.println("setIMONumber("+setIMONumber+")");
    }

    @Override
    public void setCallSign(InputReader reader, int fieldRef)
    {
        printer.println("setCallSign("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setVesselName(InputReader reader, int fieldRef)
    {
        printer.println("setVesselName("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setDimensionToBow(int dimension)
    {
        printer.println("setDimensionToBow("+dimension+")");
    }

    @Override
    public void setDimensionToStern(int dimension)
    {
        printer.println("setDimensionToStern("+dimension+")");
    }

    @Override
    public void setDimensionToPort(int dimension)
    {
        printer.println("setDimensionToPort("+dimension+")");
    }

    @Override
    public void setDimensionToStarboard(int dimension)
    {
        printer.println("setDimensionToStarboard("+dimension+")");
    }

    @Override
    public void setDraught(float meters)
    {
        printer.println("setDraught("+meters+")");
    }

    @Override
    public void setDestination(InputReader reader, int fieldRef)
    {
        printer.println("setDestination("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setDTE(boolean ready)
    {
        printer.println("setDTE("+ready+")");
    }

    @Override
    public void setShipType(CodesForShipType codesForShipType)
    {
        printer.println("setShipType("+codesForShipType+")");
    }

    @Override
    public void setSequenceNumber(int seq)
    {
        printer.println("setSequenceNumber("+seq+")");
    }

    @Override
    public void setDestinationMMSI(int mmsi)
    {
        printer.format("setDestinationMMSI(%09d)\n", mmsi);
    }

    @Override
    public void setRetransmit(boolean retransmit)
    {
        printer.println("setRetransmit("+retransmit+")");
    }

    @Override
    public void setDAC(int dac)
    {
        printer.println("setDAC("+dac+")");
    }

    @Override
    public void setFID(int fid)
    {
        printer.println("setFID("+fid+")");
    }

    @Override
    public void setLastPort(InputReader reader, int fieldRef)
    {
        printer.println("setLastPort("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setLastPortMonth(int month)
    {
        printer.println("setLastPortMonth("+month+")");
    }

    @Override
    public void setLastPortDay(int day)
    {
        printer.println("setLastPortDay("+day+")");
    }

    @Override
    public void setLastPortHour(int hour)
    {
        printer.println("setLastPortHour("+hour+")");
    }

    @Override
    public void setLastPortMinute(int minute)
    {
        printer.println("setLastPortMinute("+minute+")");
    }

    @Override
    public void setNextPort(InputReader reader, int fieldRef)
    {
        printer.println("setNextPort("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setNextPortMonth(int month)
    {
        printer.println("setNextPortMonth("+month+")");
    }

    @Override
    public void setNextPortDay(int day)
    {
        printer.println("setNextPortDay("+day+")");
    }

    @Override
    public void setNextPortHour(int hour)
    {
        printer.println("setNextPortHour("+hour+")");
    }

    @Override
    public void setNextPortMinute(int minute)
    {
        printer.println("setNextPortMinute("+minute+")");
    }

    @Override
    public void setMainDangerousGood(InputReader reader, int fieldRef)
    {
        printer.println("setMainDangerousGood("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setIMDCategory(InputReader reader, int fieldRef)
    {
        printer.println("setIMDCategory("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setUNNumber(int unid)
    {
        printer.println("UNNumber("+unid+")");
    }

    @Override
    public void setAmountOfCargo(int amount)
    {
        printer.println("AmountOfCargo("+amount+")");
    }

    @Override
    public void setUnitOfQuantity(CargoUnitCodes cargoUnitCodes)
    {
        printer.println("setUnitOfQuantity("+cargoUnitCodes+")");
    }

    @Override
    public void setFromHour(int hour)
    {
        printer.println("setFromHour("+hour+")");
    }

    @Override
    public void setFromMinute(int minute)
    {
        printer.println("setFromMinute("+minute+")");
    }

    @Override
    public void setToHour(int hour)
    {
        printer.println("setToHour("+hour+")");
    }

    @Override
    public void setToMinute(int minute)
    {
        printer.println("setToMinute("+minute+")");
    }

    @Override
    public void setCurrentDirection(int currentDirection)
    {
        printer.println("setCurrentDirection("+currentDirection+")");
    }

    @Override
    public void setCurrentSpeed(float knots)
    {
        printer.println("setCurrentSpeed("+knots+")");
    }

    @Override
    public void setPersonsOnBoard(int persons)
    {
        printer.println("setPersonsOnBoard("+persons+")");
    }

    @Override
    public void setLinkage(int id)
    {
        printer.println("setLinkage("+id+")");
    }

    @Override
    public void setPortname(InputReader reader, int fieldRef)
    {
        printer.println("setPortname("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setAreaNotice(AreaNoticeDescription areaNoticeDescription)
    {
        printer.println("setAreaNotice("+areaNoticeDescription+")");
    }

    @Override
    public void setDuration(int duration)
    {
        printer.println("setDuration("+duration+")");
    }

    @Override
    public void setShape(SubareaType subareaType)
    {
        printer.println("setShape("+subareaType+")");
    }

    @Override
    public void setScale(int scale)
    {
        printer.println("setScale("+scale+")");
    }

    @Override
    public void setPrecision(int precision)
    {
        printer.println("setPrecision("+precision+")");
    }

    @Override
    public void setRadius(int radius)
    {
        printer.println("setRadius("+radius+")");
    }

    @Override
    public void setEast(int east)
    {
        printer.println("setEast("+east+")");
    }

    @Override
    public void setNorth(int north)
    {
        printer.println("setNorth("+north+")");
    }

    @Override
    public void setOrientation(int orientation)
    {
        printer.println("setOrientation("+orientation+")");
    }

    @Override
    public void setLeft(int left)
    {
        printer.println("setLeft("+left+")");
    }

    @Override
    public void setRight(int right)
    {
        printer.println("setRight("+right+")");
    }

    @Override
    public void setBearing(int bearing)
    {
        printer.println("setBearing("+bearing+")");
    }

    @Override
    public void setDistance(int distance)
    {
        printer.println("setDistance("+distance+")");
    }

    @Override
    public void setText(InputReader reader, int fieldRef)
    {
        printer.println("setText("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setBerthLength(int meters)
    {
        printer.println("setBerthLength("+meters+")");
    }

    @Override
    public void setBerthDepth(float meters)
    {
        printer.println("setBerthDepth("+meters+")");
    }

    @Override
    public void setServicesAvailability(boolean available)
    {
        printer.println("setServicesAvailability("+available+")");
    }

    @Override
    public void setBerthName(InputReader reader, int fieldRef)
    {
        printer.println("setBerthName("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setMooringPosition(MooringPosition mooringPosition)
    {
        printer.println("setMooringPosition("+mooringPosition+")");
    }

    @Override
    public void setAgentServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setAgentServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setFuelServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setFuelServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setChandlerServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setChandlerServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setStevedoreServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setStevedoreServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setElectricalServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setElectricalServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setWaterServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setWaterServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setCustomsServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setCustomsServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setCartageServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setCartageServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setCraneServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setCraneServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setLiftServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setLiftServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setMedicalServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setMedicalServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setNavrepairServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setNavrepairServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setProvisionsServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setProvisionsServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setShiprepairServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setShiprepairServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setSurveyorServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setSurveyorServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setSteamServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setSteamServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setTugsServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setTugsServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setSolidwasteServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setSolidwasteServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setLiquidwasteServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setLiquidwasteServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setHazardouswasteServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setHazardouswasteServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setBallastServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setBallastServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setAdditionalServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setAdditionalServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setRegional1ServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setRegional1ServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setRegional2ServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setRegional2ServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setFuture1ServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setFuture1ServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setFuture2ServiceStatus(ServiceStatus serviceStatus)
    {
        printer.println("setFuture2ServiceStatus("+serviceStatus+")");
    }

    @Override
    public void setSender(int sender)
    {
        printer.println("setSender("+sender+")");
    }

    @Override
    public void setWaypointCount(int count)
    {
        printer.println("setWaypointCount("+count+")");
    }

    @Override
    public void setRouteType(RouteTypeCodes routeTypeCodes)
    {
        printer.println("setRouteType("+routeTypeCodes+")");
    }

    @Override
    public void setDescription(InputReader reader, int fieldRef)
    {
        printer.println("setDescription("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setMMSI1(int mmsi)
    {
        printer.format("setMMSI1(%09d)\n", mmsi);
    }

    @Override
    public void setMMSI2(int mmsi)
    {
        printer.format("setMMSI2(%09d)\n", mmsi);
    }

    @Override
    public void setMMSI3(int mmsi)
    {
        printer.format("setMMSI3(%09d)\n", mmsi);
    }

    @Override
    public void setMMSI4(int mmsi)
    {
        printer.format("setMMSI4(%09d)\n", mmsi);
    }

    @Override
    public void setAverageWindSpeed(int knots)
    {
        printer.println("setAverageWindSpeed("+knots+")");
    }

    @Override
    public void setGustSpeed(int knots)
    {
        printer.println("setGustSpeed("+knots+")");
    }

    @Override
    public void setWindDirection(int degrees)
    {
        printer.println("setWindDirection("+degrees+")");
    }

    @Override
    public void setWindGustDirection(int degrees)
    {
        printer.println("setWindGustDirection("+degrees+")");
    }

    @Override
    public void setAirTemperature(float degrees)
    {
        printer.println("setAirTemperature("+degrees+")");
    }

    @Override
    public void setRelativeHumidity(int humidity)
    {
        printer.println("setRelativeHumidity("+humidity+")");
    }

    @Override
    public void setDewPoint(float degrees)
    {
        printer.println("setDewPoint("+degrees+")");
    }

    @Override
    public void setAirPressure(int pressure)
    {
        printer.println("setAirPressure("+pressure+")");
    }

    @Override
    public void setAirPressureTendency(int tendency)
    {
        printer.println("setAirPressureTendency("+tendency+")");
    }

    @Override
    public void setVisibility(float nm)
    {
        printer.println("setVisibility("+nm+")");
    }

    @Override
    public void setWaterLevel(float meters)
    {
        printer.println("setWaterLevel("+meters+")");
    }

    @Override
    public void setWaterLevelTrend(int trend)
    {
        printer.println("setWaterLevelTrend("+trend+")");
    }

    @Override
    public void setSurfaceCurrentSpeed(float knots)
    {
        printer.println("setSurfaceCurrentSpeed("+knots+")");
    }

    @Override
    public void setCurrentSpeed2(float knots)
    {
        printer.println("setCurrentSpeed2("+knots+")");
    }

    @Override
    public void setCurrentDirection2(int degrees)
    {
        printer.println("setCurrentDirection2("+degrees+")");
    }

    @Override
    public void setMeasurementDepth2(float meters)
    {
        printer.println("setMeasurementDepth2("+meters+")");
    }

    @Override
    public void setCurrentSpeed3(float knots)
    {
        printer.println("setCurrentSpeed3("+knots+")");
    }

    @Override
    public void setCurrentDirection3(int degrees)
    {
        printer.println("setCurrentDirection3("+degrees+")");
    }

    @Override
    public void setMeasurementDepth3(float meters)
    {
        printer.println("setMeasurementDepth3("+meters+")");
    }

    @Override
    public void setWaveHeight(float meters)
    {
        printer.println("setWaveHeight("+meters+")");
    }

    @Override
    public void setWavePeriod(int seconds)
    {
        printer.println("setWavePeriod("+seconds+")");
    }

    @Override
    public void setWaveDirection(int degrees)
    {
        printer.println("setWaveDirection("+degrees+")");
    }

    @Override
    public void setSwellHeight(float meters)
    {
        printer.println("setSwellHeight("+meters+")");
    }

    @Override
    public void setSwellPeriod(int seconds)
    {
        printer.println("setSwellPeriod("+seconds+")");
    }

    @Override
    public void setSwellDirection(int degrees)
    {
        printer.println("setSwellDirection("+degrees+")");
    }

    @Override
    public void setWaterTemperature(float degrees)
    {
        printer.println("setWaterTemperature("+degrees+")");
    }

    @Override
    public void setSalinity(float f)
    {
        printer.println("setSalinity("+f+")");
    }

    @Override
    public void setIce(int ice)
    {
        printer.println("setIce("+ice+")");
    }

    @Override
    public void setPrecipitation(PrecipitationTypes precipitationTypes)
    {
        printer.println("setPrecipitation("+precipitationTypes+")");
    }

    @Override
    public void setSeaState(BeaufortScale beaufortScale)
    {
        printer.println("setSeaState("+beaufortScale+")");
    }

    @Override
    public void setReasonForClosing(InputReader reader, int fieldRef)
    {
        printer.println("setReasonForClosing("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setClosingFrom(InputReader reader, int fieldRef)
    {
        printer.println("setClosingFrom("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setClosingTo(InputReader reader, int fieldRef)
    {
        printer.println("setClosingTo("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setUnitOfExtension(ExtensionUnit unit)
    {
        printer.println("setUnitOfExtension("+unit+")");
    }


    @Override
    public void setFromMonth(int month)
    {
        printer.println("setFromMonth("+month+")");
    }

    @Override
    public void setFromDay(int day)
    {
        printer.println("setFromDay("+day+")");
    }

    @Override
    public void setToMonth(int month)
    {
        printer.println("setToMonth("+month+")");
    }

    @Override
    public void setToDay(int day)
    {
        printer.println("setToDay("+day+")");
    }

    @Override
    public void setAirDraught(int meters)
    {
        printer.println("setAirDraught("+meters+")");
    }

    @Override
    public void setIdType(TargetIdentifierType targetIdentifierType)
    {
        printer.println("setIdType("+targetIdentifierType+")");
    }

    @Override
    public void setId(long id)
    {
        printer.println("setId("+id+")");
    }

    @Override
    public void setStation(InputReader reader, int fieldRef)
    {
        printer.println("setStation("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setSignal(MarineTrafficSignals marineTrafficSignals)
    {
        printer.println("setSignal("+marineTrafficSignals+")");
    }

    @Override
    public void setNextSignal(MarineTrafficSignals marineTrafficSignals)
    {
        printer.println("setNextSignal("+marineTrafficSignals+")");
    }

    @Override
    public void setVariant(int variant)
    {
        printer.println("setVariant("+variant+")");
    }

    @Override
    public void setLocation(InputReader reader, int fieldRef)
    {
        printer.println("setLocation("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setWeather(WMOCode45501 wmoCode45501)
    {
        printer.println("setWeather("+wmoCode45501+")");
    }

    @Override
    public void setVisibilityLimit(boolean reached)
    {
        printer.println("setVisibilityLimit("+reached+")");
    }

    @Override
    public void setAirPressure(float pressure)
    {
        printer.println("setAirPressure("+pressure+")");
    }

    @Override
    public void setAirPressureChange(float delta)
    {
        printer.println("setAirPressureChange("+delta+")");
    }

    @Override
    public void setPrefix(int numberOfSentences, int sentenceNumber, int sequentialMessageID, char channel)
    {
        printer.println("setPrefix("+numberOfSentences+", "+sentenceNumber+", "+sequentialMessageID+", "+channel+")");
    }

    @Override
    public void setOwnMessage(boolean ownMessage)
    {
        printer.println("setOwnMessage("+ownMessage+")");
    }

    @Override
    public void setName(InputReader reader, int fieldRef)
    {
        printer.println("setName("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setNameExtension(InputReader reader, int fieldRef)
    {
        printer.println("setNameExtension("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setVendorId(InputReader reader, int fieldRef)
    {
        printer.println("setVendorId("+AisUtil.makeString(reader.getCharSequence(fieldRef))+")");
    }

    @Override
    public void setNavaidType(NavaidTypes navaidType)
    {
        printer.println("setNavaidType("+navaidType+")");
    }

    @Override
    public void setClock(Clock clock)
    {
        printer.println("setClock()");
    }

    @Override
    public void setAisVersion(int arg)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
