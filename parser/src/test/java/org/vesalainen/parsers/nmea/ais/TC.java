/*
 * Copyright (C) 2014 Timo Vesalainen
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

/**
 *
 * @author Timo Vesalainen
 */
public class TC extends AbstractAISObserver
{
    boolean ownMessage;
    String commitReason;
    String rollbackReason;
    int seq;
    int second = -1;
    int heading = -1;
    float course = Float.NaN;
    double latitude = Double.NaN;
    double longitude = Double.NaN;
    float speed = Float.NaN;
    float rateOfTurn = Float.NaN;
    NavigationStatus navigationStatus;
    int mmsi = -1;
    int sequentialMessageId;
    MessageTypes messageType;
    EPFDFixTypes epfd;
    int aisVersion = -1;
    CodesForShipType shipType;
    String destination;
    float draught = Float.NaN;
    String shipname;
    String callSign;
    int imoNumber = -1;
    int dimensionToStarboard = -1;
    int dimensionToPort = -1;
    int dimensionToStern = -1;
    int dimensionToBow = -1;
    int eta_minute = -1;
    int eta_hour = -1;
    int eta_day = -1;
    int eta_month = -1;
    int minute = -1;
    int hour = -1;
    int day = -1;
    int month = -1;
    int fid = -1;
    int dac = -1;
    String error;
    int wspeed = -1;
    int wgust = -1;
    int wdir = -1;
    int wgustdir = -1;
    float temperature = Float.NaN;
    int humidity = -1;
    float dewpoint = Float.NaN;
    float pressure = Float.NaN;
    Tendency pressuretend;
    float visibility = Float.NaN;
    Tendency leveltrend;
    float waterlevel = Float.NaN;
    float cspeed = Float.NaN;
    int cdir = -1;
    int cdepth3 = -1;
    int cdir3 = -1;
    float cspeed3 = Float.NaN;
    int cdepth2 = -1;
    float cspeed2 = Float.NaN;
    int cdir2 = -1;
    int swelldir = -1;
    int swellperiod = -1;
    float swellheight = Float.NaN;
    int wavedir = -1;
    int waveperiod = -1;
    float waveheight = Float.NaN;
    BeaufortScale seastate;
    float watertemp = Float.NaN;
    PrecipitationTypes preciptype;
    float salinity = Float.NaN;
    int ice = -1;
    int alt = -1;
    int dest_mmsi = -1;
    int year = -1;
    int mmsi1 = -1;
    int mmsi2 = -1;
    private int mmsi3 = -1;
    private int mmsi4 = -1;
    int increment2 = -1;
    int increment1 = -1;
    int offset2 = -1;
    int offset1 = -1;
    Boolean assigned;
    Boolean msg22;
    Boolean band;
    Boolean dsc;
    Boolean display;
    Boolean cs;
    int radio = -1;
    Boolean raim;
    Boolean dte;
    int increment3 = -1;
    int increment4 = -1;
    int offset3 = -1;
    int offset4 = -1;
    int timeout1 = -1;
    int timeout2 = -1;
    int timeout3 = -1;
    int timeout4 = -1;
    int number1 = -1;
    int number2 = -1;
    int number3 = -1;
    int number4 = -1;
    NavaidTypes aid_type;
    String name;
    Boolean accuracy;
    Boolean virtual_aid;
    Boolean off_position;
    String name_ext;
    int channelA = -1;
    int channelB = -1;
    TransceiverModes transceiverMode;
    float neLongitude = Float.NaN;
    float swLongitude = Float.NaN;
    float neLatitude = Float.NaN;
    float swLatitude = Float.NaN;
    Boolean addressed;
    Boolean channelABand;
    Boolean channelBBand;
    int zoneSize = -1;
    Boolean power;
    String vendorid;
    int partno = -1;
    int mothershipMMSI = -1;
    int model = -1;
    int serial = -1;
    ManeuverIndicator maneuver;

    @Override
    public void setManeuver(ManeuverIndicator maneuverIndicator)
    {
        maneuver = maneuverIndicator;
    }

    @Override
    public void setEtaMinute(int minute)
    {
        eta_minute = minute;
    }

    @Override
    public void setEtaHour(int hour)
    {
        eta_hour = hour;
    }

    @Override
    public void setEtaDay(int day)
    {
        eta_day = day;
    }

    @Override
    public void setEtaMonth(int month)
    {
        eta_month = month;
    }
    
    @Override
    public void setSerialNumber(int arg)
    {
        this.serial = arg;
    }

    @Override
    public void setUnitModelCode(int arg)
    {
        this.model = arg;
    }

    @Override
    public void setMothershipMMSI(int arg)
    {
        this.mothershipMMSI = arg;
    }

    @Override
    public void setPartNumber(int arg)
    {
        this.partno = arg;
    }

    @Override
    public void setVendorId(String vendorId)
    {
        this.vendorid = vendorId;
    }

    @Override
    public void setPower(boolean high)
    {
        this.power = high;
    }

    @Override
    public void setZoneSize(int arg)
    {
        this.zoneSize = arg;
    }

    @Override
    public void setChannelBBand(boolean band)
    {
        this.channelBBand = band;
    }

    @Override
    public void setChannelABand(boolean band)
    {
        this.channelABand = band;
    }

    @Override
    public void setAddressed(boolean addressed)
    {
        this.addressed = addressed;
    }

    @Override
    public void setSwLatitude(float f)
    {
        this.swLatitude = f;
    }

    @Override
    public void setNeLatitude(float f)
    {
        this.neLatitude = f;
    }

    @Override
    public void setSwLongitude(float f)
    {
        this.swLongitude = f;
    }

    @Override
    public void setNeLongitude(float f)
    {
        this.neLongitude = f;
    }

    @Override
    public void setTransceiverMode(TransceiverModes transceiverMode)
    {
        this.transceiverMode = transceiverMode;
    }

    @Override
    public void setChannelB(int arg)
    {
        this.channelB = arg;
    }

    @Override
    public void setChannelA(int arg)
    {
        this.channelA = arg;
    }

    @Override
    public void setNameExtension(String name_ext)
    {
        this.name_ext = name_ext;
    }

    @Override
    public void setVirtualAid(boolean virtual)
    {
        this.virtual_aid = virtual;
    }

    @Override
    public void setOffPosition(boolean off)
    {
        this.off_position = off;
    }

    @Override
    public void setPositionAccuracy(boolean accuracy)
    {
        this.accuracy = accuracy;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public void setNavaidType(NavaidTypes navaidTypes)
    {
        this.aid_type = navaidTypes;
    }

    @Override
    public void setIncrement4(int arg)
    {
        this.increment4 = arg;
    }

    @Override
    public void setIncrement3(int arg)
    {
        this.increment3 = arg;
    }

    @Override
    public void setOffset4(int arg)
    {
        this.offset4 = arg;
    }

    @Override
    public void setOffset3(int arg)
    {
        this.offset3 = arg;
    }

    @Override
    public void setTimeout4(int arg)
    {
        this.timeout4 = arg;
    }

    @Override
    public void setTimeout3(int arg)
    {
        this.timeout3 = arg;
    }

    @Override
    public void setTimeout2(int arg)
    {
        this.timeout2 = arg;
    }

    @Override
    public void setTimeout1(int arg)
    {
        this.timeout1 = arg;
    }

    @Override
    public void setReservedSlots4(int arg)
    {
        this.number4 = arg;
    }

    @Override
    public void setReservedSlots3(int arg)
    {
        this.number3 = arg;
    }

    @Override
    public void setReservedSlots2(int arg)
    {
        this.number2 = arg;
    }

    @Override
    public void setReservedSlots1(int arg)
    {
        this.number1 = arg;
    }

    @Override
    public void setDte(boolean ready)
    {
        this.dte = ready;
    }

    @Override
    public void setAssignedMode(boolean b)
    {
        this.assigned = b;
    }

    @Override
    public void setMsg22(boolean b)
    {
        this.msg22 = b;
    }

    @Override
    public void setBand(boolean flag)
    {
        this.band = flag;
    }

    @Override
    public void setDsc(boolean dsc)
    {
        this.dsc = dsc;
    }

    @Override
    public void setDisplay(boolean hasDisplay)
    {
        this.display = hasDisplay;
    }

    @Override
    public void setCsUnit(boolean cs)
    {
        this.cs = cs;
    }

    @Override
    public void setRadioStatus(int radio)
    {
        this.radio = radio;
    }

    @Override
    public void setRaim(boolean raim)
    {
        this.raim = raim;
    }

    @Override
    public void setIncrement2(int arg)
    {
        this.increment2 = arg;
    }

    @Override
    public void setIncrement1(int arg)
    {
        this.increment1 = arg;
    }

    @Override
    public void setOffset2(int arg)
    {
        this.offset2 = arg;
    }

    @Override
    public void setOffset1(int arg)
    {
        this.offset1 = arg;
    }

    @Override
    public void setMmsi4(int mmsi)
    {
        this.mmsi4 = mmsi;
    }

    @Override
    public void setMmsi3(int mmsi)
    {
        this.mmsi3 = mmsi;
    }

    @Override
    public void setMmsi2(int mmsi)
    {
        this.mmsi2 = mmsi;
    }

    @Override
    public void setMmsi1(int mmsi)
    {
        this.mmsi1 = mmsi;
    }

    @Override
    public void setYear(int year)
    {
        this.year = year;
    }

    @Override
    public void setDestinationMMSI(int mmsi)
    {
        this.dest_mmsi = mmsi;
    }

    @Override
    public void setAltitude(int meters)
    {
        this.alt = meters;
    }

    @Override
    public void setIce(int ice)
    {
        this.ice = ice;
    }

    @Override
    public void setSalinity(float f)
    {
        this.salinity = f;
    }

    @Override
    public void setPrecipitation(PrecipitationTypes precipitationTypes)
    {
        this.preciptype = precipitationTypes;
    }

    @Override
    public void setWaterTemperature(float degrees)
    {
        this.watertemp = degrees;
    }

    @Override
    public void setSeaState(BeaufortScale beaufortScale)
    {
        this.seastate = beaufortScale;
    }

    @Override
    public void setSwellDirection(int degrees)
    {
        this.swelldir = degrees;
    }

    @Override
    public void setSwellPeriod(int seconds)
    {
        this.swellperiod = seconds;
    }

    @Override
    public void setSwellHeight(float meters)
    {
        this.swellheight = meters;
    }

    @Override
    public void setWaveDirection(int degrees)
    {
        this.wavedir = degrees;
    }

    @Override
    public void setWavePeriod(int seconds)
    {
        this.waveperiod = seconds;
    }

    @Override
    public void setWaveHeight(float meters)
    {
        this.waveheight = meters;
    }

    @Override
    public void setMeasurementDepth3(int meters)
    {
        this.cdepth3 = meters;
    }

    @Override
    public void setCurrentDirection3(int degrees)
    {
        this.cdir3 = degrees;
    }

    @Override
    public void setCurrentSpeed3(float knots)
    {
        this.cspeed3 = knots;
    }

    @Override
    public void setMeasurementDepth2(int meters)
    {
        this.cdepth2 = meters;
    }

    @Override
    public void setCurrentDirection2(int degrees)
    {
        this.cdir2 = degrees;
    }

    @Override
    public void setCurrentSpeed2(float knots)
    {
        this.cspeed2 = knots;
    }

    @Override
    public void setSurfaceCurrentDirection(int currentDirection)
    {
        this.cdir = currentDirection;
    }

    @Override
    public void setSurfaceCurrentSpeed(float knots)
    {
        this.cspeed = knots;
    }

    @Override
    public void setWaterLevelTrend(Tendency trend)
    {
        this.leveltrend = trend;
    }

    @Override
    public void setWaterLevel(float meters)
    {
        this.waterlevel = meters;
    }

    @Override
    public void setAirPressureTendency(Tendency tendency)
    {
        this.pressuretend = tendency;
    }

    @Override
    public void setVisibility(float nm)
    {
        this.visibility = nm;
    }

    @Override
    public void setAirPressure(float pressure)
    {
        this.pressure = pressure;
    }

    @Override
    public void setDewPoint(float degrees)
    {
        this.dewpoint = degrees;
    }

    @Override
    public void setRelativeHumidity(int humidity)
    {
        this.humidity = humidity;
    }

    @Override
    public void setAirTemperature(float degrees)
    {
        this.temperature = degrees;
    }

    @Override
    public void setWindGustDirection(int degrees)
    {
        this.wgustdir = degrees;
    }

    @Override
    public void setWindDirection(int degrees)
    {
        this.wdir = degrees;
    }

    @Override
    public void setGustSpeed(int knots)
    {
        this.wgust = knots;
    }

    @Override
    public void setAverageWindSpeed(int knots)
    {
        this.wspeed = knots;
    }

    @Override
    public void setError(String string)
    {
        this.error = string;
    }

    @Override
    public void setFid(int fid)
    {
        this.fid = fid;
    }

    @Override
    public void setDac(int dac)
    {
        this.dac = dac;
    }

    @Override
    public void setMinute(int minute)
    {
        this.minute = minute;
    }

    @Override
    public void setHour(int hour)
    {
        this.hour = hour;
    }

    @Override
    public void setDay(int day)
    {
        this.day = day;
    }

    @Override
    public void setMonth(int month)
    {
        this.month = month;
    }

    @Override
    public void setDimensionToStarboard(int dimension)
    {
        this.dimensionToStarboard = dimension;
    }

    @Override
    public void setDimensionToPort(int dimension)
    {
        this.dimensionToPort = dimension;
    }

    @Override
    public void setDimensionToStern(int dimension)
    {
        this.dimensionToStern = dimension;
    }

    @Override
    public void setDimensionToBow(int dimension)
    {
        this.dimensionToBow = dimension;
    }

    @Override
    public void setShipType(CodesForShipType codesForShipType)
    {
        this.shipType = codesForShipType;
    }

    @Override
    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    @Override
    public void setDraught(float meters)
    {
        this.draught = meters;
    }

    @Override
    public void setVesselName(String vesselName)
    {
        this.shipname = vesselName;
    }

    @Override
    public void setCallSign(String callSign)
    {
        this.callSign = callSign;
    }

    @Override
    public void setImoNumber(int imo)
    {
        this.imoNumber = imo;
    }

    @Override
    public void setAisVersion(int arg)
    {
        this.aisVersion = arg;
    }

    @Override
    public void setEpfd(EPFDFixTypes epfdFixTypes)
    {
        this.epfd = epfdFixTypes;
    }

    @Override
    public void setOwnMessage(boolean ownMessage)
    {
        this.ownMessage = ownMessage;
    }

    @Override
    public void commit(String reason)
    {
        this.commitReason = reason;
    }

    @Override
    public void rollback(String reason)
    {
        this.rollbackReason = reason;
    }

    @Override
    public void setSequenceNumber(int seq)
    {
        this.seq = seq;
    }

    @Override
    public void setSecond(int second)
    {
        this.second = second;
    }

    @Override
    public void setHeading(int heading)
    {
        this.heading = heading;
    }

    @Override
    public void setCourse(float cog)
    {
        this.course = cog;
    }

    @Override
    public void setLatitude(double degrees)
    {
        this.latitude = degrees;
    }

    @Override
    public void setLongitude(double degrees)
    {
        this.longitude = degrees;
    }

    @Override
    public void setSpeed(float knots)
    {
        this.speed = knots;
    }

    @Override
    public void setRateOfTurn(float degreesPerMinute)
    {
        this.rateOfTurn = degreesPerMinute;
    }

    @Override
    public void setNavigationStatus(NavigationStatus navigationStatus)
    {
        this.navigationStatus = navigationStatus;
    }

    @Override
    public void setMmsi(int mmsi)
    {
        this.mmsi = mmsi;
    }

    @Override
    public void setMessageType(MessageTypes messageType)
    {
        this.messageType = messageType;
    }
    
}
