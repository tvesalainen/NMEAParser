/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.util.navi.Fathom;
import org.vesalainen.util.navi.Feet;
import org.vesalainen.util.navi.KilometersInHour;
import org.vesalainen.util.navi.Knots;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Converter
{
    public static final char KTS = 'N';
    public static final char M = 'M';
    public static final char KMH = 'K';
    public static final char CELCIUS = 'C';
    public static final char FATH = 'F';
    public static final char FT = 'f';
    public static final char N = 'N';
    
    public static float toKnots(float velocity, char unit)
    {
        return (float) getSpeedType(unit).convertTo(velocity, KNOT);
    }
    public static UnitType getSpeedType(char unit)
    {
        switch (unit)
        {
            case KTS:
                return KNOT;
            case KMH:
                return UnitType.KILO_METERS_PER_HOUR;
            default:
                throw new IllegalArgumentException(unit+" unknown expected N/M/K");
        }
    }
    public static UnitType getDepthType(char unit)
    {
        switch (unit)
        {
            case M:
                return METER;
            case FATH:
                return UnitType.FATHOM;
            case FT:
                return UnitType.FOOT;
            default:
                throw new IllegalArgumentException(unit+" unknown expected N/M/K");
        }
    }
    public static UnitType getTempType(char unit)
    {
        switch (unit)
        {
            case CELCIUS:
                return UnitType.CELSIUS;
            default:
                throw new IllegalArgumentException(unit+" unknown expected N/M/K");
        }
    }
    public static UnitType getDistanceType(char unit)
    {
        switch (unit)
        {
            case N:
                return UnitType.NAUTICAL_MILE;
            default:
                throw new IllegalArgumentException(unit+" unknown expected N/M/K");
        }
    }
    public static float toMetersPerSecond(float velocity, char unit)
    {
        return (float) getSpeedType(unit).convertTo(velocity, METERS_PER_SECOND);
    }

    public static final char Left = 'L';
    public static final char Right = 'R';
    public static float leftOrRight(float dir, char unit)
    {
        switch (unit)
        {
            case Left:
                return -dir;
            case Right:
                return dir;
            default:
                throw new IllegalArgumentException(unit+" unknown expected L/R");
        }
    }

    public static float toCelsius(float temp, char unit)
    {
        return (float) getTempType(unit).convertTo(temp, UnitType.CELSIUS);
    }

    public static float toMeters(float depth, char unit)
    {
        return (float) getDepthType(unit).convertTo(depth, METER);
    }

    public static float toNauticalMiles(float dist, char unit)
    {
        return (float) getDistanceType(unit).convertTo(dist, UnitType.NAUTICAL_MILE);
    }

}
