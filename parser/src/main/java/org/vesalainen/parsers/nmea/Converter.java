/*
 * Copyright (C) 2015 tkv
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

import org.vesalainen.util.navi.Fathom;
import org.vesalainen.util.navi.Feet;
import org.vesalainen.util.navi.KilometersInHour;
import org.vesalainen.util.navi.Knots;
import org.vesalainen.util.navi.Velocity;

/**
 *
 * @author tkv
 */
public class Converter
{
    public static final char Kts = 'N';
    public static final char M = 'M';
    public static final char KMH = 'K';
    public static float toKnots(float velocity, char unit)
    {
        switch (unit)
        {
            case Kts:
                return velocity;
            case M:
                return (float) Velocity.toKnots(velocity);
            case KMH:
                return (float) KilometersInHour.toKnots(velocity);
            default:
                throw new IllegalArgumentException(unit+" unknown expected N/M/K");
        }
    }

    public static float toMetersPerSecond(float velocity, char unit)
    {
        switch (unit)
        {
            case Kts:
                return (float) Knots.toMetersPerSecond(velocity);
            case M:
                return velocity;
            case KMH:
                return (float) KilometersInHour.toMetersPerSecond(velocity);
            default:
                throw new IllegalArgumentException(unit+" unknown expected N/M/K");
        }
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

    public static final char Celcius = 'C';
    public static float toCelcius(float temp, char unit)
    {
        switch (unit)
        {
            case Celcius:
                return temp;
            default:
                throw new IllegalArgumentException(unit+" unknown expected C");
        }
    }

    public static final char Fath = 'F';
    public static final char Ft = 'f';
    public static float toMeters(float depth, char unit)
    {
        switch (unit)
        {
            case Fath:
                return (float) Fathom.toMeters(depth);
            case M:
                return depth;
            case Ft:
                return (float) Feet.toMeters(depth);
            default:
                throw new IllegalArgumentException(unit+" unknown expected f/M/F");
        }
    }

    public static float distance(float dist, char unit)
    {
        switch (unit)
        {
            case 'N':
                return dist;
            default:
                throw new IllegalArgumentException(unit+" unknown expected N");
        }
    }

}
