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

import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.util.Transactional;

/**
 * AISObserver is observer class for AIS data. AISParser calls methods of this 
 * interface.
 * 
 * <p>It is mostly easier to derive your class from AbstractAISObserver class.
 * AbstractAISObserver has empty methods for all AISObserver methods.
 * 
 * <p>Observer methods are called as soon they are found in input. Parsing might
 * cause syntax error or NMEA sentence checksum might fail. In that case rollback
 * is called. Critical application should store the values and use them only after
 * commit. 
 * @author Timo Vesalainen
 */
public interface AISObserver extends Transactional
{   
    /**
     * Set own message status. if message is !AISVDM ownMessage = false. 
     * if message is !AISVDO ownMessage = true.
     * @param ownMessage 
     */
    void setOwnMessage(boolean ownMessage);
    /**
     * Set the type of message
     * @param messageTypes 
     */
    void setMessageType(MessageTypes messageTypes);
 
    /**
     * Repeat Indicator. Message repeat count.
     * @param repeatIndicator 
     */
    void setRepeatIndicator(int repeatIndicator);
    /**
     * Maritime Mobile Service Identity. 9 decimal digits.
     * @see <a href="http://en.wikipedia.org/wiki/Maritime_Mobile_Service_Identity">Maritime Mobile Service Identity</a>
     * @param mmsi 
     */
    void setMmsi(int mmsi);
    /**
     * Radio channel code. AIS uses the high
     * side of the duplex from two VHF radio channels: AIS Channel A is
     * 161.975Mhz (87B); AIS Channel B is 162.025Mhz (88B).
     * @param channel 
     */
    public void setChannel(char channel);
    /**
     * Navigation Status
     * @param navigationStatus 
     */
    void setNavigationStatus(NavigationStatus navigationStatus);
    /**
     * Rate of Turn (ROT)
     * @param degreesPerMinute degrees / minute
     */
    void setRateOfTurn(float degreesPerMinute);
    /**
     * Speed Over Ground (SOG). 
     * @param knots speed in knots. value 102.2 indicates 102.2 knots or higher.
     */
    void setSpeed(float knots);
    /**
     * The position accuracy flag indicates the accuracy of the fix. 
     * A value of true indicates a DGPS-quality fix with an accuracy of &lt; 10ms. false, 
     * the default, indicates an unaugmented GNSS fix with accuracy &gt; 10m.
     * @param accuracy 
     */
    void setPositionAccuracy(boolean accuracy);
    /**
     * Values up to plus or minus 180 degrees, East = positive, West = negative. 
     * @param degrees longitude in degrees
     */
    void setLongitude(float degrees);
    /**
     * Values up to plus or minus 90 degrees, North = positive, South = negative.
     * @param degrees latitude in degrees
     */
    void setLatitude(float degrees);
    /**
     * Course Over Ground (COG). Relative to true north, to 0.1 degree precision.
     * @param cog 
     */
    void setCourse(float cog);
    /**
     * True Heading (HDG)
     * @param heading 0 to 359 degrees
     */
    void setHeading(int heading);
    /**
     * Second of UTC timestamp
     * @param second 
     */
    void setSecond(int second);
    /**
     * Maneuver Indicator
     * @param maneuverIndicator 
     */
    void setManeuver(ManeuverIndicator maneuverIndicator);
    /**
     * The RAIM flag indicates whether Receiver Autonomous Integrity Monitoring 
     * is being used to check the performance of the EPFD. 
     * false = RAIM not in use(default), true = RAIM in use.
     * @see <a href="http://en.wikipedia.org/wiki/Receiver_Autonomous_Integrity_Monitoring">Receiver autonomous integrity monitoring</a>
     * @param raim 
     */
    void setRaim(boolean raim);
    /**
     * Diagnostic information for the radio system.
     * @param radio 
     */
    void setRadioStatus(int radio);
    /**
     * Year (UTC). UTC, 1-999
     * @param year 
     */
    void setYear(int year);
    /**
     * Month (UTC). 
     * @param month 1-12
     */
    void setMonth(int month);
    /**
     * Day (UTC) 1-31
     * @param day 
     */
    void setDay(int day);
    /**
     * Hour (UTC) 0-23
     * @param hour 
     */
    void setHour(int hour);
    /**
     * Minute (UTC) 0-59
     * @param minute 
     */
    void setMinute(int minute);
    /**
     * Type of EPFD
     * @param epfdFixTypes 
     */
    void setEpfd(EPFDFixTypes epfdFixTypes);
    /**
     * AIS Version. 0 = ITU1371
     * @see <a href="http://www.itu.int/rec/R-REC-M.1371-4-201004-I/en">Technical characteristics for an automatic identification system using time-division multiple access in the VHF maritime mobile band</a>
     * @param version 
     */
    void setVersion(int version);
    /**
     * IMO ship ID number
     * @param imo 
     */
    void setImoNumber(int imo);
    /**
     * Call Sign
     * @param callSign
     */
    void setCallSign(String callSign);
    /**
     * Vessel Name
     * @param vesselName
     */
    void setVesselName(String vesselName);
    /**
     * Dimension to Bow
     * @param dimension meters 
     */
    void setDimensionToBow(int dimension);
    /**
     * Dimension to Stern
     * @param dimension meters 
     */
    void setDimensionToStern(int dimension);
    /**
     * Dimension to Port
     * @param dimension meters 
     */
    void setDimensionToPort(int dimension);
    /**
     * Dimension to Starboard
     * @param dimension meters 
     */
    void setDimensionToStarboard(int dimension);
    /**
     * Draught
     * @param meters 
     */
    void setDraught(float meters);
    /**
     * Destination
     * @param destination
     */
    void setDestination(String destination);
    /**
     * Data terminal ready. Note! True is not ready.
     * @param ready
     */
    void setDte(boolean ready);
    /**
     * Ship Type
     * @param codesForShipType 
     */
    void setShipType(CodesForShipType codesForShipType);
    /**
     * Sequence Number
     * @param seq 
     */
    void setSequenceNumber(int seq);
    /**
     * Destination MMSI
     * @see <a href="http://en.wikipedia.org/wiki/Maritime_Mobile_Service_Identity">Maritime Mobile Service Identity</a>
     * @param mmsi 
     */
    void setDestinationMMSI(int mmsi);
    /**
     * Retransmit flag
     * @param retransmit 
     */
    void setRetransmit(boolean retransmit);
    /**
     * Designated Area Code. The Designated Area Code, which is a jurisdiction code: 
     * 366 for the United States. It uses the same encoding as the area designator 
     * in MMMSIs; 1 designates international (ITU) messages.
     * @see <a href="http://www.itu.int/online/mms/glad/cga_mids.sh">Table of Maritime Identification Digits</a>
     * @param dac 
     */
    void setDac(int dac);
    /**
     * Functional ID for a message subtype. 
     * @param fid 
     */
    void setFid(int fid);
    /**
     * Last Port Of Call. 
     * @param lastPort
     */
    void setLastPort(String lastPort);

    /**
     * ETA Month (UTC). 
     * @param month 1-12
     */
    void setLastPortMonth(int month);
    /**
     * ETA Day (UTC) 1-31
     * @param day 
     */
    void setLastPortDay(int day);
    /**
     * ETA Hour (UTC) 0-23
     * @param hour 
     */
    void setLastPortHour(int hour);
    /**
     * ETA Minute (UTC) 0-59
     * @param minute 
     */
    void setLastPortMinute(int minute);
    /**
     * Next Port Of Call. 
     * @param nextPort
     */
    void setNextPort(String nextPort);
    /**
     * ETA Month (UTC). 
     * @param month 1-12
     */
    void setNextPortMonth(int month);
    /**
     * ETA Day (UTC) 1-31
     * @param day 
     */
    void setNextPortDay(int day);
    /**
     * ETA Hour (UTC) 0-23
     * @param hour 
     */
    void setNextPortHour(int hour);
    /**
     * ETA Minute (UTC) 0-59
     * @param minute 
     */
    void setNextPortMinute(int minute);
    /**
     * Main Dangerous Good
     * @param mainDangerousGood
     */
    void setMainDangerousGood(String mainDangerousGood);
    /**
     * IMD Category
     * @param imdCategory
     */
    void setImdCategory(String imdCategory);
    /**
     * UN Number
     * @param unid 
     */
    void setUnNumber(int unid);
    /**
     * Amount of Cargo
     * @param amount 
     */
    void setAmountOfCargo(int amount);
    /**
     * Unit of Quantity
     * @param cargoUnitCodes 
     */
    void setUnitOfQuantity(CargoUnitCodes cargoUnitCodes);
    /**
     * From Hour (UTC) 0-23
     * @param hour 
     */
    void setFromHour(int hour);
    /**
     * From Minute (UTC) 0-59
     * @param minute 
     */
    void setFromMinute(int minute);
    /**
     * To Hour (UTC) 0-23
     * @param hour 
     */
    void setToHour(int hour);
    /**
     * To Minute (UTC) 0-59
     * @param minute 
     */
    void setToMinute(int minute);
    /**
     * Current Direction Predicted
     * @param currentDirection degrees 0-359
     */
    void setSurfaceCurrentDirection(int currentDirection);
    /**
     * Current Speed Predicted
     * @param knots 
     */
    void setCurrentSpeed(float knots);
    /**
     * # persons on board. 
     * @param persons 
     */
    void setPersonsOnBoard(int persons);
    /**
     * Message Linkage ID
     * @param id 
     */
    void setLinkage(int id);
    /**
     * Name of Port & Berth
     * @param portName
     */
    void setPortname(String portName);
    /**
     * Notice Description
     * @param areaNoticeDescription 
     */
    void setAreaNotice(AreaNoticeDescription areaNoticeDescription);
    /**
     * Duration
     * @param duration In minutes, 0 = cancel this notice.
     */
    void setDuration(int duration);
    /**
     * Shape of area
     * @param subareaType 
     */
    void setShape(SubareaType subareaType);
    /**
     * Scale factor
     * @param scale Exponent for area dimensions 1 = meters (default)
     */
    void setScale(int scale);
    /**
     * Precision
     * @param precision Decimal places of precision (defaults to 4)
     */
    void setPrecision(int precision);
    /**
     * Radius
     * @param radius Radius of area 0 = point (default), else 1-4095 * 10^scale m
     */
    void setRadius(int radius);
    /**
     * E dimension
     * @param east Box dimension east 0 = N/S line (default), else 1-255 * 10^scale m
     */
    void setEast(int east);
    /**
     * N dimension
     * @param north Box dimension north 0 = E/W line (default), else 1-255 * 10^scale m
     */
    void setNorth(int north);
    /**
     * Orientation
     * @param orientation Degrees clockwise from true N, 0 = no rotation (default), else 1-359, 360-511 reserved.
     */
    void setOrientation(int orientation);
    /**
     * Left boundary
     * @param left Degrees clockwise from true N, 0 = no rotation (default), else 1-359, 360-511 reserved.
     */
    void setLeft(int left);
    /**
     * Right boundary
     * @param right Degrees clockwise from true N, 0 = no rotation (default), else 1-359, 360-511 reserved.
     */
    void setRight(int right);
    /**
     * Bearing
     * @param bearing True bearing in half-degree steps from previous waypoint; 720 = N/A (default).
     */
    void setBearing(int bearing);
    /**
     * Distance
     * @param distance Distance from prev. waypoint, 0 = no point (default), else 1-1023 * 10^scale m
     */
    void setDistance(int distance);
    /**
     * Text
     * @param text
     */
    void setText(String text);
    /**
     * Berth length
     * @param meters In 1m steps, 1-510m, 511 = >= 511m 0 = N/A (default).
     */
    void setBerthLength(int meters);
    /**
     * Berth Water Depth
     * @param meters 0.1-25.4m in 0.1 steps 255 = >= 25.5m 0 = N/A (default)
     */
    void setBerthDepth(float meters );
    /**
     * Services Availability
     * @param available 
     */
    void setServicesAvailability(boolean available );
    /**
     * Name of Berth
     * @param berthName
     */
    void setBerthName(String berthName);
    /**
     * Mooring Position
     * @param mooringPosition 
     */
    void setMooringPosition(MooringPosition mooringPosition);
    /**
     * Agent
     * @param serviceStatus 
     */
    void setAgentServiceStatus(ServiceStatus serviceStatus);
    /**
     * Bunker/fuel
     * @param serviceStatus 
     */
    void setFuelServiceStatus(ServiceStatus serviceStatus);
    /**
     * Chandler
     * @param serviceStatus 
     */
    void setChandlerServiceStatus(ServiceStatus serviceStatus);
    /**
     * Stevedore
     * @param serviceStatus 
     */
    void setStevedoreServiceStatus(ServiceStatus serviceStatus);
    /**
     * Electrical
     * @param serviceStatus 
     */
    void setElectricalServiceStatus(ServiceStatus serviceStatus);
    /**
     * Potable water
     * @param serviceStatus 
     */
    void setWaterServiceStatus(ServiceStatus serviceStatus);
    /**
     * Customs house
     * @param serviceStatus 
     */
    void setCustomsServiceStatus(ServiceStatus serviceStatus);
    /**
     * Cartage
     * @param serviceStatus 
     */
    void setCartageServiceStatus(ServiceStatus serviceStatus);
    /**
     * Crane(s)
     * @param serviceStatus 
     */
    void setCraneServiceStatus(ServiceStatus serviceStatus);
    /**
     * Lift(s)
     * @param serviceStatus 
     */
    void setLiftServiceStatus(ServiceStatus serviceStatus);
    /**
     * Medical facilities
     * @param serviceStatus 
     */
    void setMedicalServiceStatus(ServiceStatus serviceStatus);
    /**
     * Navigation repair
     * @param serviceStatus 
     */
    void setNavrepairServiceStatus(ServiceStatus serviceStatus);
    /**
     * Provisions
     * @param serviceStatus 
     */
    void setProvisionsServiceStatus(ServiceStatus serviceStatus);
    /**
     * Ship repair
     * @param serviceStatus 
     */
    void setShiprepairServiceStatus(ServiceStatus serviceStatus);
    /**
     * Surveyor
     * @param serviceStatus 
     */
    void setSurveyorServiceStatus(ServiceStatus serviceStatus);
    /**
     * Steam
     * @param serviceStatus 
     */
    void setSteamServiceStatus(ServiceStatus serviceStatus);
    /**
     * Tugs
     * @param serviceStatus 
     */
    void setTugsServiceStatus(ServiceStatus serviceStatus);
    /**
     * Waste disposal (solid)
     * @param serviceStatus 
     */
    void setSolidwasteServiceStatus(ServiceStatus serviceStatus);
    /**
     * Waste disposal (liquid)
     * @param serviceStatus 
     */
    void setLiquidwasteServiceStatus(ServiceStatus serviceStatus);
    /**
     * Waste disposal (hazardous)
     * @param serviceStatus 
     */
    void setHazardouswasteServiceStatus(ServiceStatus serviceStatus);
    /**
     * Reserved ballast exchange
     * @param serviceStatus 
     */
    void setBallastServiceStatus(ServiceStatus serviceStatus);
    /**
     * Additional services
     * @param serviceStatus 
     */
    void setAdditionalServiceStatus(ServiceStatus serviceStatus);
    /**
     * Regional reserved 1
     * @param serviceStatus 
     */
    void setRegional1ServiceStatus(ServiceStatus serviceStatus);
    /**
     * Regional reserved 2
     * @param serviceStatus 
     */
    void setRegional2ServiceStatus(ServiceStatus serviceStatus);
    /**
     * Reserved for future
     * @param serviceStatus 
     */
    void setFuture1ServiceStatus(ServiceStatus serviceStatus);
    /**
     * Reserved for future
     * @param serviceStatus 
     */
    void setFuture2ServiceStatus(ServiceStatus serviceStatus);
    /**
     * Sender Class
     * @param sender 0 = ship (default), 1 = authority, 27 = reserved for future use
     */
    void setSender(int sender);
    /**
     * Waypoint count
     * @param count 
     */
    void setWaypointCount(int count);
    /**
     * Route Type
     * @param routeTypeCodes 
     */
    void setRouteType(RouteTypeCodes routeTypeCodes);
    /**
     * Description
     * @param description
     */
    void setDescription(String description);
    /**
     * MMSI number 1
     * @param mmsi 
     */
    void setMmsi1(int mmsi);
    /**
     * MMSI number 2
     * @param mmsi 
     */
    void setMmsi2(int mmsi);
    /**
     * MMSI number 3
     * @param mmsi 
     */
    void setMmsi3(int mmsi);
    /**
     * MMSI number 4
     * @param mmsi 
     */
    void setMmsi4(int mmsi);
    /**
     * Average Wind Speed
     * @param knots 10-min avg wind speed, knots
     */
    void setAverageWindSpeed(int knots);
    /**
     * Gust Speed
     * @param knots 10-min max wind speed, knots
     */
    void setGustSpeed(int knots);
    /**
     * Wind Direction
     * @param degrees 0-359, degrees fom true north 
     */
    void setWindDirection(int degrees);
    /**
     * Wind Gust Direction
     * @param degrees 0-359, degrees fom true north 
     */
    void setWindGustDirection(int degrees);
    /**
     * Air Temperature
     * @param degrees C
     */
    void setAirTemperature(float degrees);
    /**
     * Relative Humidity
     * @param humidity 0-100%, units of 1%, 127 = N/A (default).
     */
    void setRelativeHumidity(int humidity);
    /**
     * Dew Point
     * @param degrees -20.0 to +50.0: 0.1 deg C
     */
    void setDewPoint(float degrees);
    /**
     * 800-1200hPa: units 1hPa
     * @param pressure 
     */
    void setAirPressure(int pressure);
    /**
     * Pressure Tendency
     * @param tendency 0 = steady, 1 = decreasing, 2 = increasing, 3 - N/A (default).
     */
    void setAirPressureTendency(Tendency tendency);
    /**
     * Horiz. Visibility
     * @param nm 
     */
    void setVisibility(float nm);
    /**
     * Water Level
     * @param meters 
     */
    void setWaterLevel(float meters);
    /**
     * Water Level Trend
     * @param trend 0 = steady, 1 = decreasing, 2 = increasing, 3 - N/A (default).
     */
    void setWaterLevelTrend(Tendency trend);
    /**
     * Surface Current Speed
     * @param knots 0.0-25.0 knots: units 0.1 knot
     */
    void setSurfaceCurrentSpeed(float knots);
    /**
     * Current Speed #2
     * @param knots 0.0-25.0 in units of 0.1 knot, >=251 = N/A (default).
     */
    void setCurrentSpeed2(float knots);
    /**
     * Current Direction #2
     * @param degrees 0-359: deg. fom true north, >=360 = N/A (default)
     */
    void setCurrentDirection2(int degrees);
    /**
     * Measurement Depth #2
     * @param meters 0-30m down: units 0.1m, 31 = N/A (default).
     */
    void setMeasurementDepth2(int meters);
    /**
     * Current Speed #3
     * @param knots 0.0-25.0 in units of 0.1 knot, >=251 = N/A (default).
     */
    void setCurrentSpeed3(float knots);
    /**
     * Current Direction #3
     * @param degrees 0-359: deg. fom true north, >=360 = N/A (default)
     */
    void setCurrentDirection3(int degrees);
    /**
     * Measurement Depth #3
     * @param meters 0-30m down: units 0.1m, 31 = N/A (default).
     */
    void setMeasurementDepth3(int meters);
    /**
     * Wave height
     * @param meters 0-25m: units of 0.1m, >=251 = N/A (default).
     */
    void setWaveHeight(float meters);
    /**
     * Wave period
     * @param seconds Seconds 0-60: >= 61 = N/A (default).
     */
    void setWavePeriod(int seconds);
    /**
     * Wave direction
     * @param degrees 0-359: deg. fom true north, >=360 = N/A (default).
     */
    void setWaveDirection(int degrees);
    /**
     * Swell height
     * @param meters 0-25m: units of 0.1m, >=251 = N/A (default).
     */
    void setSwellHeight(float meters);
    /**
     * Swell period
     * @param seconds Seconds 0-60: >= 61 = N/A (default).
     */
    void setSwellPeriod(int seconds);
    /**
     * Swell direction
     * @param degrees 0-359: deg. fom true north, >=360 = N/A (default).
     */
    void setSwellDirection(int degrees);
    /**
     * Water Temperature
     * @param degrees -10.0 to 50.0: units 0.1 C
     */
    void setWaterTemperature(float degrees);
    /**
     * Salinity
     * @param f 0.0-50.0%: units 0.1%
     */
    void setSalinity(float f);
    /**
     * Ice
     * @param ice Yes/No (??? this is 2-bit field???)
     */
    void setIce(int ice);
    /**
     * Precipitation
     * @param precipitationTypes 
     */
    void setPrecipitation(PrecipitationTypes precipitationTypes);
    /**
     * Sea state
     * @param beaufortScale 
     */
    void setSeaState(BeaufortScale beaufortScale);
    /**
     * Reason For Closing
     * @param reasonForClosing
     */
    void setReasonForClosing(String reasonForClosing);
    /**
     * Location Of Closing From
     * @param closingFrom
     */
    void setClosingFrom(String closingFrom);
    /**
     * Location of Closing To
     * @param closingTo
     */
    void setClosingTo(String closingTo);
    /**
     * Unit of extension
     * @param unit
     */
    void setUnitOfExtension(ExtensionUnit unit);
    /**
     * From month (UTC)
     * @param month 1-12
     */
    void setFromMonth(int month);
    /**
     * From day (UTC)
     * @param day 1-31
     */
    void setFromDay(int day);
    /**
     * To month (UTC)
     * @param month 1-12
     */
    void setToMonth(int month);
    /**
     * To day (UTC)
     * @param day 1-31
     */
    void setToDay(int day);
    /**
     * Air Draught
     * @param meters Height in meters
     */
    void setAirDraught(int meters);
    /**
     * Identifier type
     * @param targetIdentifierType 
     */
    void setIdType(TargetIdentifierType targetIdentifierType);
    /**
     * Target identifier
     * @param id Target ID data.
     * @see #setIdType(TargetIdentifierType)
     */
    void setId(long id);
    /**
     * Name of Signal Station
     * @param station
     */
    void setStation(String station);
    /**
     * Signal In Service
     * @param marineTrafficSignals 
     */
    void setSignal(MarineTrafficSignals marineTrafficSignals);
    /**
     * Expected Next Signal
     * @param marineTrafficSignals 
     */
    void setNextSignal(MarineTrafficSignals marineTrafficSignals);
    /**
     * Variant
     * @param variant 
     */
    void setVariant(int variant);
    /**
     * Location
     * @param location
     */
    void setLocation(String location);
    /**
     * Present Weather
     * @param wmoCode45501 
     */
    void setWeather(WMOCode45501 wmoCode45501);
    /**
     * Visibility Limit
     * @param reached when on, indicates that the maximum range of the 
     * visibility equipment was reached and the visibility reading shall be 
     * regarded as > x.x NM.
     */
    void setVisibilityLimit(boolean reached);
    /**
     * Pressure at sea level
     * @param pressure 90-1100 hPa
     */
    void setAirPressure(float pressure);
    /**
     * Pressure Change
     * @param delta -50-+50hPa: units of 0.1hPa averaged over last 3 hours.
     */
    void setAirPressureChange(float delta);
    /**
     * Name in sixbit chars
     * @param name
     */
    void setName(String name);
    /**
     * If present, the Name Extension consists of packed six-bit ASCII 
     * characters followed by 0-6 bits of padding to an 8-bit boundary. 
     * The [IALA] description says "This parameter should be omitted when no 
     * more than 20 characters for the name of the A-to-N are needed in total. 
     * Only the required number of characters should be transmitted, 
     * i.e. no @-character should be used." A decoder can deduce the bit 
     * length of the name extension field by subtracting 272 from the total 
     * message bit length.
     * @param nameExtension
     */
    void setNameExtension(String nameExtension);

    /**
     *
     * @param vendorId
     */
    void setVendorId(String vendorId);

    void setNavaidType(NavaidTypes navaidTypes);

    void setError(String string);

    void setClock(NMEAClock clock);
    /**
     * 0=[ITU1371], 1-3 = future editions
     * @param arg 
     */
    void setAisVersion(int arg);
    /**
     * Water pressure: 0.0-6000.0
     * @param decibar 
     */
    void setWaterPressure(float decibar);
    /**
     * Altitude
     * @param meters 
     */
    void setAltitude(int meters);
    /**
     * Sensor altitude above MSL
     * @param meters 
     */
    void setSensorAltitude(float meters);
    /**
     * Returns false if Class B SOTDMA unit, true if Class B CS (Carrier Sense) unit
     * @param cs 
     */
    void setCsUnit(boolean cs);
    /**
     * Returns true if has display
     * @param hasDisplay 
     */
    void setDisplay(boolean hasDisplay);
    /**
     * Returns true if, unit is attached to a VHF voice radio with DSC capability.
     * @param dsc 
     */
    void setDsc(boolean dsc);
    /**
     * Base stations can command units to switch frequency. If this flag is true, 
     * the unit can use any part of the marine channel.
     * @param flag 
     */
    void setBand(boolean flag);
    /**
     * Returns true If, unit can accept a channel assignment via Message Type 22.
     * @param b 
     */
    void setMsg22(boolean b);
    /**
     * Returns true if device is in assigned mode.
     * @param b 
     */
    void setAssignedMode(boolean b);
    /**
     * Consecutive slots 1
     * @param arg 
     */
    void setReservedSlots1(int arg);
    /**
     * Consecutive slots 2
     * @param arg 
     */
    void setReservedSlots2(int arg);
    /**
     * Consecutive slots 3
     * @param arg 
     */
    void setReservedSlots3(int arg);
    /**
     * Consecutive slots 4
     * @param arg 
     */
    void setReservedSlots4(int arg);
    /**
     * Allocation timeout in minutes 1
     * @param arg 
     */
    void setTimeout1(int arg);
    /**
     * Allocation timeout in minutes 2
     * @param arg 
     */
    void setTimeout2(int arg);
    /**
     * Allocation timeout in minutes 3
     * @param arg 
     */
    void setTimeout3(int arg);
    /**
     * Allocation timeout in minutes 4
     * @param arg 
     */
    void setTimeout4(int arg);
    /**
     * Reserved offset number 1
     * @param arg 
     */
    void setOffset1(int arg);
    /**
     * Reserved offset number 2
     * @param arg 
     */
    void setOffset2(int arg);
    /**
     * Reserved offset number 3
     * @param arg 
     */
    void setOffset3(int arg);
    /**
     * Reserved offset number 4
     * @param arg 
     */
    void setOffset4(int arg);
    /**
     * Repeat increment 1
     * @param arg 
     */
    void setIncrement1(int arg);
    /**
     * Repeat increment 2
     * @param arg 
     */
    void setIncrement2(int arg);
    /**
     * Repeat increment 3
     * @param arg 
     */
    void setIncrement3(int arg);
    /**
     * Repeat increment 4
     * @param arg 
     */
    void setIncrement4(int arg);
    /**
     * The Off-Position Indicator is for floating Aids-to-Navigation only: false 
     * means on position; true means off position. 
     * Only valid if UTC second is equal to or below 59.
     * @param off 
     */
    void setOffPosition(boolean off);
    /**
     * The Virtual Aid flag is interpreted as follows: false = default = real 
     * Aid to Navigation at indicated position; true = virtual Aid to Navigation 
     * simulated by nearby AIS station.
     * @param virtual 
     */
    void setVirtualAid(boolean virtual);
    /**
     * Channel number
     * @param arg 
     */
    void setChannelA(int arg);
    /**
     * Channel number
     */
    void setChannelB(int arg);
    /**
     * Transmit/receive mode
     * @param transmitModes 
     */
    void setTransceiverMode(TransceiverModes transceiverMode);
    /**
     * Power
     * @param high 
     */
    void setPower(boolean high);
    /**
     * NE Longitude
     * @param f 
     */
    void setNeLongitude(float f);
    /**
     * NE Latitude
     * @param f 
     */
    void setSwLongitude(float f);
    /**
     * SW Longitude
     * @param f 
     */
    void setNeLatitude(float f);
    /**
     * SW Latitude
     * @param f 
     */
    void setSwLatitude(float f);
    /**
     * false=Broadcast, true=Addressed
     * @param addressed 
     */
    void setAddressed(boolean addressed);
    /**
     * false=Default, true=12.5kHz
     * @param band 
     */
    void setChannelABand(boolean band);
    /**
     * false=Default, true=12.5kHz
     * @param band 
     */
    void setChannelBBand(boolean band);
    /**
     * Size of transitional zone
     * @param arg 
     */
    void setZoneSize(int arg);
    /**
     * Part Number
     * @param arg 
     */
    void setPartNumber(int arg);
    /**
     * Mothership MMSI
     * @param arg 
     */
    void setMothershipMMSI(int arg);
    /**
     * Unit Model Code
     * @param arg 
     */
    void setUnitModelCode(int arg);
    /**
     * Serial Number
     * @param arg 
     */
    void setSerialNumber(int arg);
    /**
     * ETA month (UTC) 1-12, 0=N/A (default)
     * @param month 
     */
    public void setEtaMonth(int month);
    /**
     * ETA day (UTC) 1-31, 0=N/A (default)
     * @param arg 
     */
    public void setEtaDay(int day);
    /**
     * ETA hour (UTC) 0-23, 24=N/A (default)
     * @param hour 
     */
    public void setEtaHour(int hour);
    /**
     * ETA minute (UTC) 0-59, 60=N/A (default)
     * @param minute 
     */
    public void setEtaMinute(int minute);

}
