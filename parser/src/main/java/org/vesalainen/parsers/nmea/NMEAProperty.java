/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.math.UnitType;
import org.vesalainen.text.CamelCase;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum NMEAProperty
{
	AGE_OF_DIFFERENTIAL_GPSDATA(NMEACategory.GPS, UnitType.UNITLESS, float.class),
	ALMANAC_REFERENCE_TIME(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	ANTENNA_ALTITUDE(NMEACategory.GPS, UnitType.METER, float.class),
	ARGUMENT_OF_PERIGEE(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	ARRIVAL_CIRCLE_RADIUS(NMEACategory.WAYPOINT, UnitType.NAUTICAL_MILE, float.class),
	ARRIVAL_STATUS(NMEACategory.WAYPOINT, UnitType.UNITLESS, char.class),
	AZIMUTH(NMEACategory.GPS, UnitType.DEGREE, int.class),
	BEARING_FROM_OWN_SHIP(NMEACategory.BEARING, UnitType.UNITLESS, float.class),
	BEARING_TO_DESTINATION(NMEACategory.WAYPOINT, UnitType.DEGREE, float.class),
	CROSS_TRACK_ERROR(NMEACategory.BEARING, UnitType.NAUTICAL_MILE, float.class),
	DEPTH_BELOW_KEEL(NMEACategory.DEPTH, UnitType.METER, float.class),
	DEPTH_BELOW_SURFACE(NMEACategory.DEPTH, UnitType.METER, float.class),
	DEPTH_BELOW_TRANSDUCER(NMEACategory.DEPTH, UnitType.METER, float.class),
	DEPTH_OF_WATER(NMEACategory.DEPTH, UnitType.METER, float.class),
	DEPTH_OFFSET_OF_WATER(NMEACategory.DEPTH, UnitType.METER, float.class),
	DESTINATION_CLOSING_VELOCITY(NMEACategory.WAYPOINT, UnitType.KNOTS, float.class),
	DESTINATION_WAYPOINT_LATITUDE(NMEACategory.WAYPOINT, UnitType.COORDINATE_DEGREES_AND_MINUTES, double.class),
	DESTINATION_WAYPOINT_LONGITUDE(NMEACategory.WAYPOINT, UnitType.COORDINATE_DEGREES_AND_MINUTES, double.class),
	DIFFERENTIAL_REFERENCE_STATION_ID(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	DISTANCE_OF_CPA(NMEACategory.DISTANCE, UnitType.UNITLESS, float.class),
	DISTANCE_TO_WAYPOINT(NMEACategory.WAYPOINT, UnitType.NAUTICAL_MILE, float.class),
	ECCENTRICITY(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	ELEVATION(NMEACategory.GPS, UnitType.DEGREE, int.class),
	F0CLOCK_PARAMETER(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	F1CLOCK_PARAMETER(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	FROM_WAYPOINT(NMEACategory.WAYPOINT, UnitType.UNITLESS, CharSequence.class),
	GEOIDAL_SEPARATION(NMEACategory.GPS, UnitType.METER, float.class),
	GPS_QUALITY_INDICATOR(NMEACategory.GPS, UnitType.UNITLESS, GPSQualityIndicator.class),
	GPS_WEEK_NUMBER(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	HDOP(NMEACategory.GPS, UnitType.UNITLESS, float.class),
	HORIZONTAL_DILUTION_OF_PRECISION(NMEACategory.GPS, UnitType.METER, float.class),
	INCLINATION_ANGLE(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	LATITUDE(NMEACategory.COORDINATE, UnitType.COORDINATE_DEGREES_AND_MINUTES, double.class),
	LONGITUDE(NMEACategory.COORDINATE, UnitType.COORDINATE_DEGREES_AND_MINUTES, double.class),
	LONGITUDE_OF_ASCENSION_NODE(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	MAGNETIC_BEARING(NMEACategory.BEARING, UnitType.DEGREE, float.class),
	MAGNETIC_BEARING_ORIGIN_TO_DESTINATION(NMEACategory.WAYPOINT, UnitType.DEGREE, float.class),
	MAGNETIC_BEARING_PRESENT_POSITION_TO_DESTINATION(NMEACategory.WAYPOINT, UnitType.DEGREE, float.class),
	MAGNETIC_DEVIATION(NMEACategory.BEARING, UnitType.DEGREE, float.class),
	MAGNETIC_HEADING(NMEACategory.BEARING, UnitType.DEGREE, float.class),
	MAGNETIC_HEADING_TO_STEER_TO_DESTINATION(NMEACategory.WAYPOINT, UnitType.DEGREE, float.class),
	MAGNETIC_VARIATION(NMEACategory.BEARING, UnitType.DEGREE, float.class),
	MAGNETIC_WATER_HEADING(NMEACategory.BEARING, UnitType.DEGREE, float.class),
	MEAN_ANOMALY(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	NUMBER_OF_SATELLITES_IN_VIEW(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	P_RAT(NMEACategory.ACCELERATION, UnitType.UNITLESS, float.class),
	P_RTR(NMEACategory.ACCELERATION, UnitType.UNITLESS, float.class),
	PDOP(NMEACategory.GPS, UnitType.UNITLESS, float.class),
	PITCH(NMEACategory.ATTITUDE, UnitType.DEGREE_NEG, float.class),
	PRN(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	R_RAT(NMEACategory.ACCELERATION, UnitType.UNITLESS, float.class),
	R_RTR(NMEACategory.ACCELERATION, UnitType.UNITLESS, float.class),
	RANGE_TO_DESTINATION(NMEACategory.WAYPOINT, UnitType.NAUTICAL_MILE, float.class),
	RATE_OF_RIGHT_ASCENSION(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	RATE_OF_TURN(NMEACategory.TURN, UnitType.UNITLESS, float.class),
	RELATIVE_WIND_ANGLE(NMEACategory.WIND, UnitType.DEGREE, float.class),
	ROLL(NMEACategory.ATTITUDE, UnitType.DEGREE_NEG, float.class),
	ROOT_OF_SEMI_MAJOR_AXIS(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	RPM(NMEACategory.RPM, UnitType.UNITLESS, float.class),
	RPM_SOURCE(NMEACategory.RPM, UnitType.UNITLESS, char.class),
	RPM_SOURCE_NUMBER(NMEACategory.RPM, UnitType.UNITLESS, int.class),
	SATELLITE_ID1(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID10(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID11(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID12(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID2(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID3(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID4(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID5(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID6(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID7(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID8(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_ID9(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SATELLITE_PRNNUMBER(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SNR(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	SPEED_OVER_GROUND(NMEACategory.SPEED, UnitType.KNOTS, float.class),
	STATUS(NMEACategory.GPS, UnitType.UNITLESS, char.class),
	SV_HEALTH(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	TARGET_COURSE(NMEACategory.BEARING, UnitType.UNITLESS, float.class),
	TARGET_DISTANCE(NMEACategory.DISTANCE, UnitType.UNITLESS, float.class),
	TARGET_SPEED(NMEACategory.SPEED, UnitType.UNITLESS, float.class),
	TIME_DIFFERENCE_A(NMEACategory.GPS, UnitType.UNITLESS, float.class),
	TIME_DIFFERENCE_B(NMEACategory.GPS, UnitType.UNITLESS, float.class),
	TO_WAYPOINT(NMEACategory.WAYPOINT, UnitType.UNITLESS, CharSequence.class),
	TOTAL_NUMBER_OF_SATELLITES_IN_VIEW(NMEACategory.GPS, UnitType.UNITLESS, int.class),
	TRACK_MADE_GOOD(NMEACategory.BEARING, UnitType.DEGREE, float.class),
	TRUE_BEARING(NMEACategory.BEARING, UnitType.DEGREE, float.class),
	TRUE_BEARING_ORIGIN_TO_DESTINATION(NMEACategory.WAYPOINT, UnitType.DEGREE, float.class),
	TRUE_BEARING_PRESENT_POSITION_TO_DESTINATION(NMEACategory.WAYPOINT, UnitType.DEGREE, float.class),
	TRUE_HEADING(NMEACategory.BEARING, UnitType.DEGREE, float.class),
	TRUE_HEADING_TO_STEER_TO_DESTINATION(NMEACategory.WAYPOINT, UnitType.DEGREE, float.class),
	TRUE_WATER_HEADING(NMEACategory.BEARING, UnitType.DEGREE, float.class),
	TRUE_WIND_ANGLE(NMEACategory.WIND, UnitType.DEGREE, float.class),
	VDOP(NMEACategory.GPS, UnitType.UNITLESS, float.class),
	VELOCITY_TO_WAYPOINT(NMEACategory.WAYPOINT, UnitType.KNOTS, float.class),
	WATER_DISTANCE(NMEACategory.DISTANCE, UnitType.NAUTICAL_MILE, float.class),
	WATER_DISTANCE_SINCE_RESET(NMEACategory.DISTANCE, UnitType.NAUTICAL_MILE, float.class),
	WATER_SPEED(NMEACategory.SPEED, UnitType.KNOTS, float.class),
	WATER_TEMPERATURE(NMEACategory.TEMPERATURE, UnitType.CELSIUS, float.class),
	WAYPOINT_STATUS(NMEACategory.WAYPOINT, UnitType.UNITLESS, char.class),
	WIND_SPEED(NMEACategory.WIND, UnitType.METERS_PER_SECOND, float.class),
	X_ACCELERATION(NMEACategory.ACCELERATION, UnitType.GFORCE_EARTH, float.class),
	Y_ACCELERATION(NMEACategory.ACCELERATION, UnitType.GFORCE_EARTH, float.class),
	Y_RAT(NMEACategory.ACCELERATION, UnitType.UNITLESS, float.class),
	Y_RTR(NMEACategory.ACCELERATION, UnitType.UNITLESS, float.class),
	Z_ACCELERATION(NMEACategory.ACCELERATION, UnitType.GFORCE_EARTH, float.class),
;
    
    private NMEACategory category;
    private UnitType unit;
    private Class<?> type;

    private NMEAProperty(NMEACategory category, UnitType unit, Class<?> type)
    {
        this.category = category;
        this.unit = unit;
        this.type = type;
    }
    
    public String property()
    {
        return CamelCase.property(name());
    }
}
