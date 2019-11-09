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
import org.vesalainen.math.Circle;
import org.vesalainen.parsers.nmea.ais.AISBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CircleArea extends ScalingArea
{
    
    final int longitude;
    final int latitude;
    final int precision;
    final int radius;

    public CircleArea(Circle circle)
    {
        this(circle.getX(), circle.getY(), (int) circle.getRadius());
    }

    public CircleArea(double longitude, double latitude, int radius)
    {
        super(0, 12, radius);
        this.longitude = (int) (longitude * COORD_COEF);
        this.latitude = (int) (latitude * COORD_COEF);
        this.precision = 4;
        this.radius = radius / pow;
    }

    public CircleArea(CharSequence seq)
    {
        super(seq);
        this.longitude = Primitives.parseInt(seq, -2, 5, 33);
        this.latitude = Primitives.parseInt(seq, -2, 33, 60);
        this.precision = Primitives.parseInt(seq, 2, 60, 63);
        this.radius = Primitives.parseInt(seq, 2, 63, 75);
    }

    @Override
    public void build(AISBuilder builder)
    {
        super.build(builder);
        builder.integer(28, longitude).integer(27, latitude).integer(3, precision).integer(12, radius).spare(15);
    }

    public double getLongitude()
    {
        return longitude / COORD_COEF;
    }

    public double getLatitude()
    {
        return latitude / COORD_COEF;
    }

    public int getRadius()
    {
        return radius * pow;
    }

    @Override
    public String toString()
    {
        return "CircleArea{" + getLongitude() + ", " + getLatitude() + ", " + getRadius() + '}';
    }
    
}
