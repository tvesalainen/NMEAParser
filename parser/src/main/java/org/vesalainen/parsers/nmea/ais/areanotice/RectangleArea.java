/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea.ais.areanotice;

import org.vesalainen.lang.Primitives;
import org.vesalainen.parsers.nmea.ais.AISBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RectangleArea extends ScalingArea
{
    
    final int longitude;
    final int latitude;
    final int east;
    final int north;
    final int orientation;

    public RectangleArea(double longitude, double latitude, int east, int north, int orientation)
    {
        super(1, 8, east, north);
        this.longitude = (int) (longitude * COORD_COEF);
        this.latitude = (int) (latitude * COORD_COEF);
        this.east = east;
        this.north = north;
        this.orientation = orientation;
    }

    public RectangleArea(CharSequence seq)
    {
        super(seq);
        longitude = Primitives.parseInt(seq, -2, 5, 33);
        latitude = Primitives.parseInt(seq, -2, 33, 60);
        east = Primitives.parseInt(seq, 2, 60, 68);
        north = Primitives.parseInt(seq, 2, 68, 76);
        orientation = Primitives.parseInt(seq, 2, 76, 85);
    }

    @Override
    public void build(AISBuilder builder)
    {
        super.build(builder);
        builder.integer(28, longitude).integer(27, latitude).integer(8, east).integer(8, north).integer(9, orientation).spare(5);
    }

    public double getLongitude()
    {
        return longitude / COORD_COEF;
    }

    public double getLatitude()
    {
        return latitude / COORD_COEF;
    }

    public int getEast()
    {
        return east * pow;
    }

    public int getNorth()
    {
        return north * pow;
    }

    public int getOrientation()
    {
        return orientation;
    }
    
}
