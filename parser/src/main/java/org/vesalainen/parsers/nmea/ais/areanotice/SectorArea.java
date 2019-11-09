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
import org.vesalainen.math.Sector;
import org.vesalainen.parsers.nmea.ais.AISBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SectorArea extends ScalingArea
{
    
    final int longitude;
    final int latitude;
    final int radius;
    final int left;
    final int right;

    public SectorArea(Sector sector)
    {
        this(sector.getX(), sector.getY(), (int) sector.getRadius(), (int) sector.getLeftAngle(), (int) sector.getRightAngle());
    }

    public SectorArea(double longitude, double latitude, int radius, int left, int right)
    {
        super(2, 12, radius);
        this.longitude = (int) (longitude * COORD_COEF);
        this.latitude = (int) (latitude * COORD_COEF);
        this.radius = radius;
        this.left = left;
        this.right = right;
    }

    public SectorArea(CharSequence seq)
    {
        super(seq);
        this.longitude = Primitives.parseInt(seq, -2, 5, 33);
        this.latitude = Primitives.parseInt(seq, -2, 33, 60);
        this.radius = Primitives.parseInt(seq, 2, 60, 72);
        this.left = Primitives.parseInt(seq, 2, 72, 81);
        this.right = Primitives.parseInt(seq, 2, 81, 90);
    }

    @Override
    public void build(AISBuilder builder)
    {
        super.build(builder);
        builder.integer(28, longitude).integer(27, latitude).integer(12, radius).integer(9, left).integer(9, right).spare(5);
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

    public int getLeft()
    {
        return left;
    }

    public int getRight()
    {
        return right;
    }
    
}
