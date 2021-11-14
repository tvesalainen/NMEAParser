/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum NMEAPGN
{
    /**
     * Used to provide real-time operational data and status relevant to a
     * specific engine, indicated by the engine instance field. This message
     * would normally be broadcasted periodically to provide information for
     * instrumentation or control functions.
     */
    ENGINE_PARAMETERS_DYNAMIC(127489),
    /**
     * This PGN provides latitude and longitude referenced to WGS84. Being
     * defined as single frame message, as opposed to other PGNs that include
     * latitude and longitude and are defined as fast or multi-packet, this PGN
     * lends itself to being transmitted more frequently without using up
     * excessive bandwidth on the bus for the benefit of receiving equipment
     * that may require rapid position updates.
     */
    POSITION_RAPID_UPDATE(129025),
    /**
     * This parameter group provides data from ITU-R M.1371 message 4 Base
     * Station Report providing position, time, date, and current slot number of
     * a base station, and 11 UTC and date response message providing current
     * UTC and date if available. An AIS device may generate this parameter
     * group either upon receiving a VHF data link message 4 or 11, or upon
     * receipt of an ISO or NMEA request PGN.
     */
    AIS_UTC_AND_DATE_REPORT(129793),
    /**
     * This message is provided by ISO 11783 for a handshake mechanism between
     * transmitting and receiving devices. This message is the possible response
     * to acknowledge the reception of a ???normal broadcast??? message or the
     * response to a specific command to indicate compliance or failure.
     */
    ISO_ACKNOWLEDGMENT(59392),
    /**
     * As defined by ISO, this message has a data length of 3 bytes with no
     * padding added to complete the single frame. The appropriate response to
     * this message is based on the PGN being requested, and whether the
     * receiver supports the requested PGN.
     */
    ISO_REQUEST(59904),
    /**
     * ISO 11783 defines this PGN as part of the transport protocol method used
     * for transmitting messages that have 9 or more data bytes. This PGN
     * represents a single packet of a multipacket message.
     */
    ISO_TRANSPORT_PROTOCOL_DATA_TRANSFER(60160),
    /**
     * ISO 11783 defines this group function PGN as part of the transport
     * protocol method used for transmitting messages that have 9 or more data
     * bytes. This PGN???s role in the transport process is determined by the
     * group function value found in the first data byte of the PGN.
     */
    ISO_TRANSPORT_PROTOCOL_CONNECTION_MANAGEMENT_RTS_GROUP(60416),
    /**
     * This network management message is used to claim network address, reply
     * to devices requesting the claimed address, and to respond with device
     * information (NAME) requested by the ISO Request or Complex Request Group
     * Function This PGN contains several fields that are requestable, either
     * independently or in any combination.
     */
    ISO_ADDRESS_CLAIM(60928),
    /**
     * The Request / Command / Acknowledge Group type of function is defined by
     * first field. The message will be a Request, Command, or Acknowledge Group
     * Function.
     */
    NMEA_REQUEST_GROUP_FUNCTION(126208),
    /**
     * The Transmit / Receive PGN List Group type of function is defined by
     * first field. The message will be a Transmit or Receive PGN List group
     * function.
     */
    RECEIVE_TRANSMIT_PGN_S_GROUP_FUNCTION(126464),
    /**
     * Provides data with a high update rate for a specific engine in a single
     * frame message. The first field provides information as to which engine.
     */
    ENGINE_PARAMETERS_RAPID_UPDATE(127488),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 17 GNSS Broadcast Binary Message containing DGNSS corrections
     * from a base station. An AIS device may generate this parameter group
     * either upon receiving a VHF data link message 17, or upon receipt of an
     * ISO or NMEA request PGN (see ITU-R M.1371-1 for additional information).
     */
    AIS_DGNSS_BROADCAST_BINARY_MESSAGE(129792),
    /**
     * The purpose of this PGN is to provide a single transmission that
     * describes the motion of a vessel.
     */
    SPEED_WATER_REFERENCED(128259),
    /**
     * The 'Position Delta, High Precision Rapid Update' Parameter Group is
     * intended for applications where very high precision and very fast update
     * rates are needed for position data. This PGN can provide delta position
     * changes down to 1 millimeter with a delta time period accurate to 5
     * milliseconds.
     */
    POSITION_DELTA_HIGH_PRECISION_RAPID_UPDATE(129027),
    /**
     * This PGN provides the magnitude of position error perpendicular to the
     * desired course.
     */
    CROSS_TRACK_ERROR(129283),
    /**
     * This PGN provides a single transmission containing GNSS status and
     * dilution of precision components (DOP) that indicate the contribution of
     * satellite geometry to the overall positioning error. There are three DOP
     * parameters reported, horizontal (HDOP), Vertical (VDOP) and time (TDOP).
     */
    GNSS_DOPS(129539),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 6 Addressed Binary Message supporting address communication of
     * binary data. An AIS device may generate this parameter group either upon
     * receiving a VHF data link message 6, or upon receipt of an ISO or NMEA
     * request PGN.
     */
    AIS_ADDRESSED_BINARY_MESSAGE(129795),
    /**
     * This PGN is a single frame PGN that provides Course Over Ground (COG) and
     * Speed Over Ground (SOG).
     */
    COG_SOG_RAPID_UPDATE(129026),
    /**
     * GNSS common satellite receiver parameter status
     */
    GNSS_CONTROL_STATUS(129538),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 5 Ship Static and Voyage Related Data Message. An AIS device may
     * generate this parameter group either upon receiving a VHF data link
     * message 5, or upon receipt of an ISO or NMEA request PGN.
     */
    AIS_CLASS_A_STATIC_AND_VOYAGE_RELATED_DATA(129794),
    /**
     * Direction and speed of Wind. True wind can be referenced to the vessel or
     * to the ground. The Apparent Wind is what is felt standing on the (moving)
     * ship, I.e., the wind measured by the typical mast head instruments. The
     * boat referenced true wind is given by the vector sum of Apparent wind and
     * vessel's heading and speed though the water. The ground referenced true
     * wind is given by the vector sum of Apparent wind and vessel's heading and
     * speed over ground.
     */
    WIND_DATA(130306),
    /**
     * Sends commands to, and receives data from, heading control systems.
     * Allows for navigational (remote) control of a heading control system and
     * direct rudder control.
     */
    HEADING_TRACK_CONTROL(127237),
    /**
     * Used to provide the operational state and internal operating parameters
     * of a specific transmission, indicated by the transmission instance field.
     * This message would normally be broadcasted periodically to provide
     * information for instrumentation or control functions.
     */
    TRANSMISSION_PARAMETERS_DYNAMIC(127493),
    /**
     * This PGN conveys a comprehensive set of Global Navigation Satellite
     * System (GNSS) parameters, including position information.
     */
    GNSS_POSITION_DATA(129029),
    /**
     * This PGN shall return Route and WP data ahead in the Active Route. It can
     * be requested or may be transmitted without a request, typically at each
     * Waypoint advance.
     */
    NAVIGATION_ROUTE_WP_INFORMATION(129285),
    /**
     * This PGN provides a single transmission that contains relevant almanac
     * data for GPS products. The almanac contains satellite vehicle course
     * orbital parameters. This information is not considered precise and is
     * only valid for several months at a time. GPS products receive almanac
     * data directly from the satellites. This information would either be
     * transmitted to and from GPS products for update, or system interrogation.
     */
    GPS_ALMANAC_DATA(129541),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 8 Binary Broadcast Message supporting broadcast communication of
     * binary data. An AIS device may generate this parameter group either upon
     * receiving a VHF data link message 8, or upon receipt of an ISO or NMEA
     * request PGN.
     */
    AIS_BINARY_BROADCAST_MESSAGE(129797),
    /**
     * This provides Propagation times (Ranges) of Loran-C signals relative to a
     * single Group Repetition Interval.
     */
    LORAN_C_RANGE_DATA(130053),
    /**
     * The 'Altitude Delta, High Precision Rapid Update' Parameter Group is
     * intended for applications where very high precision and very fast update
     * rates are needed for altitude and course over ground data. This PG can
     * provide delta altitude changes down to 1 millimeter, a change in
     * direction as small as 0.0057 degrees, and with a delta time period
     * accurate to 5 milliseconds
     */
    ALTITUDE_DELTA_HIGH_PRECISION_RAPID_UPDATE(129028),
    /**
     * This PGN provides essential navigation data for following a
     * route.Transmissions will originate from products that can create and
     * manage routes using waypoints. This information is intended for
     * navigational repeaters.
     */
    NAVIGATION_DATA(129284),
    /**
     * GNSS information on current satellites in view tagged by sequence ID.
     * Information includes PRN, elevation, azimuth, SNR, defines the number of
     * satellites; defines the satellite number and the information.
     */
    GNSS_SATS_IN_VIEW(129540),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Messages 7 Binary Acknowledge Message and 13 Safety Related Acknowledge
     * Message. Message 7 acknowledges receipt of message 6 while message 13
     * acknowledges receipt of message 14. An AIS device may generate this
     * parameter group either upon receiving a VHF data link message 7 or 13, or
     * upon receipt of an ISO or NMEA request PGN
     */
    AIS_ACKNOWLEDGE(129796),
    /**
     * This provides Time Difference (TD) lines of position of Loran-C signals
     * relative to a single Group Repetition Interval.
     */
    LORAN_C_TD_DATA(130052),
    /**
     * This PGN is used to provide information about thruster???s operating
     * specifications and ratings.
     */
    THRUSTER_INFORMATION(128007),
    /**
     * This PGN provides status and control for a Radiotelephone, connected to a
     * NMEA 2000 network. The Radiotelephone will transmit and receive status
     * along with remote control and repeater products
     */
    RADIO_FREQUENCY_MODE_POWER(129799),
    /**
     * Environmental Conditions contains Temperature, Humidity, and Atmospheric
     * Pressure. This is a rework of PGN # 130310 and should be used for new
     * designs.
     */
    ENVIRONMENTAL_PARAMETERS(130311),
    /**
     * This PGN is used to report the status of a thruster control.
     */
    THRUSTER_CONTROL_STATUS(128006),
    /**
     * GNSS pseudorange measurement noise statistics can be translated in the
     * position domain in order to give statistical measures of the quality of
     * the position solution. Intended for use with a Receiver Autonomous
     * Integrity Monitoring (RAIM) application
     */
    GNSS_PSEUDORANGE_NOISE_STATISTICS(129542),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 9 SAR Aircraft Position Report Message for Airborne AIS units
     * conducting Search and Rescue operations. An AIS device may generate this
     * parameter group either upon receiving a VHF data link message 9, or upon
     * receipt of an ISO or NMEA request
     */
    AIS_SAR_AIRCRAFT_POSITION_REPORT(129798),
    /**
     * SNR, ECD, and ASF values of Loran-C signals
     */
    LORAN_C_SIGNAL_DATA(130054),
    /**
     * Local atmospheric environmental conditions
     */
    ENVIRONMENTAL_PARAMETERS2(130310),
    /**
     * Engine related trip information.
     */
    TRIP_PARAMETERS_ENGINE(127497),
    /**
     * This PGN provides Digital Selective Calling (DSC) data according to ITU
     * M.493-9 with optional expansion according to ITU M.821-1. DSC is a paging
     * system that is used to automate distress alerts sent over terrestrial
     * communication systems such as VHF, MF and HF marine radio systems. DSC
     * provides a mechanism to report significantly more information regarding a
     * distress call rather than just the distress itself. Products equipped
     * with DSC will transmit and receive this information.
     */
    DSC_CALL_INFORMATION(12808),
    /**
     * This PGN is used to provide the operating status and data relevant to a
     * specific Anchor Windlass.
     */
    ANCHOR_WINDLASS_OPERATING_STATUS(128777),
    /**
     * This PGN has a single transmission that provides: UTC time, UTC Date and
     * Local Offset
     */
    TIME_DATE(129033),
    /**
     * This PGN is used to provide the output from a GNSS Receiver's Receiver
     * Autonomous Integrity Monitoring (RAIM) process. The Integrity field value
     * is based upon the parameters set in PGN 130059 GNS RAIM Settings.
     */
    GNSS_RAIM_OUTPUT(129545),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 12 Addressed Safety Related Message supporting addressed
     * communication of safety related data. An AIS device may generate this
     * parameter group either upon receiving a VHF data link message 12, or upon
     * receipt of an ISO or NMEA request PGN.
     */
    AIS_ADDRESSED_SAFETY_RELATED_MESSAGE(129801),
    /**
     * Trip parameters relative to Vessel
     */
    TRIP_PARAMETERS_VESSEL(127496),
    /**
     * This PGN is used to provide the motor status and data relevant to a
     * specific thruster.
     */
    THRUSTER_MOTOR_STATUS(128008),
    /**
     * Message for reporting status and target data from tracking radar external
     * devices.
     */
    TRACKED_TARGET_DATA(128520),
    /**
     * This PGN is used to report the status of anchor windlass controls and can
     * be used with Command Group Function.
     */
    WINDLASS_CONTROL_STATUS(128776),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 10 UTC and Date Inquiry Message used to request current UTC and
     * date. An AIS device may generate this parameter group either upon
     * receiving a VHF data link message 10, or upon receipt of an ISO or NMEA
     * request PGN.
     */
    AIS_UTC_DATE_INQUIRY(129800),
    /**
     * Water depth relative to the transducer and offset of the measuring
     * transducer. Positive offset numbers provide the distance from the
     * transducer to the waterline.
     */
    WATER_DEPTH(128267),
    /**
     * The Set and Drift effect on the Vessel is the direction and the speed of
     * a current.
     */
    SET_DRIFT_RAPID_UPDATE(129291),
    /**
     * This parameter group is used to support Receiver Autonomous Integrity
     * Monitoring (RAIM). Pseudorange measurement error statistics can be
     * translated in the position domain in order to give statistical measures
     * of the quality of the position solution.
     */
    GNSS_PSEUDORANGE_ERROR_STATISTICS(129547),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 15 Interrogation Message used to request a specific ITU-R M.1371
     * message resulting in responses from one or more AIS mobile units. An AIS
     * device may generate this parameter group either upon receiving a VHF data
     * link message 15, or upon receipt of an ISO or NMEA request PGN.
     */
    AIS_INTERROGATION(129803),
    /**
     * Provides identification information and rated engine speed for the engine
     * indicated by the engine instance field. Used primarily by display
     * devices.
     */
    ENGINE_PARAMETERS_STATIC(127498),
    /**
     * This PGN is used to provide the monitoring status and data relevant to a
     * specific Anchor Windlass.
     */
    ANCHOR_WINDLASS_MONITORING_STATUS(128778),
    /**
     * This PGN is used to report the control parameters for a GNSS Receiver
     * Autonomous Integrity Monitoring (RAIM) process.
     */
    GNSS_RAIM_SETTINGS(129546),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 14 Safety Related Broadcast Message supporting broadcast
     * communication of safety related data. An AIS device may generate this
     * parameter group either upon receiving a VHF data link message 14, or upon
     * receipt of an ISO or NMEA request PGN.
     */
    AIS_SAFETY_RELATED_BROADCAST_MESSAGE(129802),
    /**
     * Rudder order command in direction or angle with current rudder angle
     * reading.
     */
    RUDDER(127245),
    /**
     * Universal status report for multiple banks of two-state indicators.
     */
    BINARY_SWITCH_BANK_STATUS(127501),
    /**
     * This PGN provides a means to pass differential GNSS corrections between
     * NMEA 2000 devices. Passing DGNSS data this way allows for more
     * flexibility than traditional methods. One differential correction
     * receiver could supply multiple GNSS receivers. Multiple differential
     * correction receivers or data streams could be connected to a GNSS
     * receiver allowing for network DGNSS approaches. This PGN can accommodate
     * DGPS and DGLONASS corrections.
     */
    DGNSS_CORRECTIONS(129549),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 20 Data Link Management Message for reserving slots for base
     * stations. An AIS device may generate this parameter group either upon
     * receiving a VHF data link message 20, or upon receipt of an ISO or NMEA
     * request PGN.
     */
    AIS_DATA_LINK_MANAGEMENT_MESSAGE(129805),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 16 Assigned Mode Command Message for assigning specific behavior
     * by a competent authority. An AIS device may generate this parameter group
     * either upon receiving a VHF data link message 16, or upon receipt of an
     * ISO or NMEA request PGN.
     */
    AIS_ASSIGNMENT_MODE_COMMAND(129804),
    /**
     * Any device with an AC Input may transmit this message
     */
    AC_INPUT_STATUS(127503),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 18 Standard Class B Equipment Position Report. An AIS device may
     * generate this parameter group either upon receiving a VHF data link
     * message 18, or upon receipt of an ISO or NMEA request PGN (see ITU-R
     * M.1371-1 for additional information).
     */
    AIS_CLASS_B_POSITION_REPORT(129039),
    /**
     * GNSS differential correction receiver status tagged by sequence ID.
     * Status information includes frequency, SNR, and use as a correction
     * source.
     */
    GNSS_DIFFERENTIAL_CORRECTION_RECEIVER_SIGNAL(129551),
    /**
     * The Group Assignment Command is transmitted by a base station when
     * operating as a controlling unit for the AIS Stations.
     */
    AIS_CLASS_B_GROUP_ASSIGNMENT(129807),
    /**
     * Universal commands to multiple banks of two-state devices.
     */
    SWITCH_BANK_CONTROL(127502),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Messages 1, 2, and 3 Position Reports, autonomous, assigned, and response
     * to interrogation, respectively. An AIS device may generate this parameter
     * group either upon receiving a VHF data link message 1,2 or 3, or upon
     * receipt of an ISO or NMEA request PGN (see ITU-R M.1371-1 for additional
     * information).
     */
    AIS_CLASS_A_POSITION_REPORT(129038),
    /**
     * GNSS common differential correction receiver parameter status.
     */
    GNSS_DIFFERENTIAL_CORRECTION_RECEIVER_INTERFACE(129550),
    /**
     * Fluid Level contains an instance number, type of fluid, level of fluid,
     * and tank capacity. For example the fluid instance may be the level of
     * fuel in a tank or the level of water in the bilge. Used primarily by
     * display or instrumentation devices.
     */
    FLUID_LEVEL(127505),
    AIS_AIDS_TO_NAVIGATION_REPORT(129041),
    /**
     * This parameter group is used by Class B 'CS' shipborne mobile equipment
     * each time Part A of ITU-R M.1372 Message 24 is received. This parameter
     * group is the first of two parts, the second being transmitted in PGN
     * 129810.
     */
    AIS_CLASS_B_CS_STATIC_REPORT_PART_A(129809),
    /**
     * Complex request for this PGN should return a list of Routes in a
     * Database.
     */
    ROUTE_AND_WP_SERVICE_ROUTE_LIST(130065),
    /**
     * Salinity station measurement data including station location, numeric
     * identifier, and name.
     */
    SALINITY_STATION_DATA(130321),
    /**
     * The purpose of this PGN is to group three fundamental vectors related to
     * vessel motion, speed and heading referenced to the water, speed and
     * course referenced to ground and current speed and flow direction
     */
    DIRECTION_DATA(130577),
    /**
     * The purpose of this PGN is twofold: To provide a regular transmission of
     * UTC time and date. To provide synchronism for measurement data
     */
    SYSTEM_TIME(126992),
    /**
     * Any device with an AC Output may transmit this message.
     */
    AC_OUTPUT_STATUS(127504),
    /**
     * This parameter group provides data associated with the ITU-R M.1371
     * Message 19 Extended Class B Equipment Position Report containing position
     * and static information. An AIS device may generate this parameter group
     * either upon receiving a VHF data link message 19, or upon receipt of an
     * ISO or NMEA request PGN.
     */
    AIS_CLASS_B_EXTENDED_POSITION_REPORT(129040),
    /**
     * Complex request for this PGN should return a list of Databases in which a
     * navigation Device organizes its Routes and WPs. A Database may contain
     * one WP-List and multiple Routes.
     */
    ROUTE_AND_WP_SERVICE_DATABASE_LIST(130064),
    /**
     * Tide station measurement data including station location, numeric
     * identifier, and name
     */
    TIDE_STATION_DATA(130320),
    /**
     * Provides data on various small craft control surfaces and speed through
     * the water. Used primarily by display or instrumentation
     */
    SMALL_CRAFT_STATUS(130576),
    /**
     * Rate of Turn is the rate of change of the Heading.
     */
    RATE_OF_TURN(127251),
    /**
     * Any device capable of charging a battery may transmit this message.
     */
    CHARGER_STATUS(127507),
    /**
     * This PGN provides the cumulative voyage distance traveled since the last
     * reset. The distance is tagged with the time and date of the distance
     * measurement
     */
    DISTANCE_LOG(128275),
    /**
     * Complex request of this PGN should return the Waypoints belonging to a
     * Route.
     */
    ROUTE_AND_WP_SERVICE_ROUTE_WP_NAME_POSITION(130067),
    /**
     * Meteorological station measurement data including station location,
     * numeric identifier, and name.
     */
    METEOROLOGICAL_STATION_DATA(130323),
    /**
     * Heading sensor value with a flag for True or Magnetic. If the sensor
     * value is Magnetic, the deviation field can be used to produce a Magnetic
     * heading, and the variation field can be used to correct the Magnetic
     * heading to produce a True heading.
     */
    VESSEL_HEADING(127250),
    /**
     * Provides parametric data for a specific battery, indicated by the battery
     * instance field. Used primarily by display or instrumentation devices, but
     * may also be used by battery management controls
     */
    DC_DETAILED_STATUS(127506),
    /**
     * This parameter group is used by Class B 'CS' shipborne mobile equipment
     * each time Part B of ITU-R M.1372 Message 24 is received. This parameter
     * group is the second of two parts, the first being transmitted in PGN
     * 129809.
     */
    AIS_CLASS_B_CS_STATIC_REPORT_PART_B(129810),
    /**
     * Complex request for this PGN should return the attributes of a Route or
     * the WP-List.
     */
    ROUTE_AND_WP_SERVICE_ROUTE_WP_LIST_ATTRIBUTES(130066),
    /**
     * Current station measurement data including station location, numeric
     * identifier, and name.
     */
    CURRENT_STATION_DATA(130322),
    /**
     * This PGN provides a single transmission that accurately describes the
     * speed of a vessel by component vectors.
     */
    VESSEL_SPEED_COMPONENTS(130578),
    /**
     * Any device capable of inverting a DC source to an SC output may transmit
     * this message.
     */
    INVERTER_STATUS(127509),
    /**
     * Transformation parameters for converting from WGS-84 to other Datums.
     */
    USER_DATUM_SETTINGS(129045),
    /**
     * Time to go to or elapsed from a generic mark, that may be non-fixed. The
     * mark is not generally a specific geographic point but may vary
     * continuously and is most often determined by calculation (the recommended
     * turning or tacking point for sailing vessels, the wheel-over point for
     * vessels making turns, a predicted collision point, etc.)
     */
    TIME_TO_FROM_MARK(129301),
    /**
     * Complex request of this PGN will return XTE Limit and/or Navigation
     * Method specific to individual legs of a Route.
     */
    ROUTE_AND_WP_SERVICE_XTE_LIMIT_NAVIGATION_METHOD(130069),
    /**
     * Local geodetic datum and datum offsets from a reference datum. T
     */
    DATUM(129044),
    /**
     * Provides product information onto the network that could be important for
     * determining quality of data coming from this product.
     */
    PRODUCT_INFORMATION(126996),
    /**
     * Provides parametric data for a specific DC Source, indicated by the
     * instance field. The type of DC Source can be identified from the DC
     * Detailed Status PGN. Used primarily by display or instrumentation
     * devices, but may also be used by power management
     */
    BATTERY_STATUS(127508),
    /**
     * This PGN provides a single transmission that contains relevant almanac
     * data for Glonass products. The almanac contains satellite vehicle course
     * orbital parameters. This information is not considered precise and is
     * only valid for several months at a time. Glonass products receive almanac
     * data directly from the satellites.This information would either be
     * transmitted to and from Glonass products for update, or system
     * interrogation.
     */
    GLONASS_ALMANAC_DATA(129556),
    /**
     * Complex request of this PGN should return the Waypoints belonging to a
     * Route.
     */
    ROUTE_AND_WP_SERVICE_ROUTE_WP_NAME(130068),
    /**
     * Moored buoy measurement data including station location and numeric
     * identifier.
     */
    MOORED_BUOY_STATION_DATA(130324),
    /**
     * Any device capable of inverting DC to AC may transmit this message.
     */
    INVERTER_CONFIGURATION_STATUS(127511),
    /**
     * Complex request of this PGN should return supplementary Comments attached
     * to Routes.
     */
    ROUTE_AND_WP_SERVICE_ROUTE_COMMENT(130071),
    /**
     * Free-form alphanumeric fields describing the installation (e.g.,
     * starboard engine room location) of the device and installation notes
     * (e.g., calibration data)
     */
    CONFIGURATION_INFORMATION(126998),
    /**
     * Any device capable of charging a battery may transmit this message.
     */
    CHARGER_CONFIGURATION_STATUS(127510),
    /**
     * Bearing and distance from the origin mark to the destination mark,
     * calculated at the origin mark, for any two arbitrary generic marks. The
     * calculation type (Rhumb Line, Great Circle) is specified, as well as the
     * bearing reference (Mag, True).
     */
    BEARING_AND_DISTANCE_BETWEEN_TWO_MARKS(129302),
    /**
     * Complex request of this PGN should return supplementary Comments attached
     * to Waypoints in a Route or a WP-List
     */
    ROUTE_AND_WP_SERVICE_WP_COMMENT(130070),
    /**
     * This PGN provides a single transmission that describes the position of a
     * vessel relative to both horizontal and vertical planes. This would
     * typically be used for vessel stabilization, vessel control and onboard
     * platform stabilization.
     */
    ATTITUDE(127257),
    /**
     * Any device connected to a battery may transmit this message.
     */
    BATTERY_CONFIGURATION_STATUS(127513),
    /**
     * Complex request of this PGN should return the Radius of Turn at specific
     * Waypoints of a Route.
     */
    ROUTE_AND_WP_SERVICE_RADIUS_OF_TURN(130073),
    /**
     * Any device that is capable of starting/stopping a generator may transmit
     * this message.
     */
    AGS_CONFIGURATION_STATUS(127512),
    /**
     * Complex request of this PGN should return supplementary Comments attached
     * to Databases in the navigation Device.
     */
    ROUTE_AND_WP_SERVICE_DATABASE_COMMENT(130072),
    /**
     * Message for transmitting variation. The message contains a sequence
     * number to allow synchronization of other messages such as Heading or
     * Course over Ground. The quality of service and age of service are
     * provided to enable recipients to determine an appropriate level of
     * service if multiple transmissions exist.
     */
    MAGNETIC_VARIATION(127258),
    /**
     * Any device capable of starting/stopping a generator may transmit this
     * message.
     */
    AGS_STATUS(127514),
    /**
     * Complex request of this PGN should return the Waypoints of a WP-List.
     */
    ROUTE_AND_WP_SERVICE_WP_LIST_WP_NAME_POSITION(130074),
    /**
     * ISO 11783 defined this message to provide a mechanism for assigning a
     * network address to a node. The NAME information in the data portion of
     * the message must match the name information of the node whose network
     * address is to be set.
     */
    ISO_COMMANDED_ADDRESS(65240),
    TEMPERATURE_EXTENDED_RANGE(130316),
    HEARTBEAT(126993);

    private int pgn;
    private static final Map<Integer, NMEAPGN> map = new HashMap<>();

    static
    {
        for (NMEAPGN np : NMEAPGN.values())
        {
            map.put(np.pgn, np);
        }
    }

    private NMEAPGN(int pgn)
    {
        this.pgn = pgn;
    }

    public int getPGN()
    {
        return pgn;
    }

    public static NMEAPGN getForPgn(int pgn)
    {
        return map.get(pgn);
    }
}
