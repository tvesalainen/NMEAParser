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
import org.vesalainen.math.Stats;
import org.vesalainen.parsers.nmea.ais.AISBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Area
{
    protected static final double COORD_COEF = 600000.0;
    
    public static Area getInstance(CharSequence seq)
    {
        if (seq.length() != 90)
        {
            throw new IllegalArgumentException("length should be 90");
        }
        int shape = Primitives.parseInt(seq, 2, 0, 3);
        switch (shape)
        {
            case 0:
                return new CircleArea(seq);
            case 1:
                return new RectangleArea(seq);
            case 2:
                return new SectorArea(seq);
            case 3:
                return new PolylineArea(seq);
            case 4:
                return new PolygonArea(seq);
            case 5:
                return new AssociatedText(seq);
            default:
                throw new UnsupportedOperationException(shape+" not supported");
        }
    }

    static int scale(int bits, int... values)
    {
        int lim = (1<<(bits))-1;
        int max = Stats.max(values);
        int scale = 0;
        while (max > lim)
        {
            scale++;
            max /= 10;
        }
        if (scale > 3)
        {
            throw new IllegalArgumentException("scale > 3");
        }
        return scale;
    }
    protected abstract void build(AISBuilder builder);
    




}
