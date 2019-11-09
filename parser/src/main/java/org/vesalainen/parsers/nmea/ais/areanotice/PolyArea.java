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
import org.vesalainen.math.MoreMath;
import org.vesalainen.math.Stats;
import org.vesalainen.parsers.nmea.ais.AISBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class PolyArea extends ScalingArea
{
    
    final int[] data;

    protected PolyArea(int type)
    {
        super(type, 11);
        this.data = new int[8];
    }

    protected PolyArea(CharSequence seq)
    {
        super(seq);
        this.data = new int[8];
        int off = 5;
        for (int ii = 0; ii < 4; ii++)
        {
            data[2 * ii] = Primitives.parseInt(seq, 2, off, off + 10);
            data[2 * ii + 1] = Primitives.parseInt(seq, 2, off + 10, off + 21);
            off += 21;
        }
    }

    @Override
    public void build(AISBuilder builder)
    {
        super.build(builder);
        for (int ii = 0; ii < 4; ii++)
        {
            builder.integer(10, data[2 * ii]);
            builder.integer(11, data[2 * ii + 1]);
        }
        builder.spare(1);
    }

    public void set(int index, int angle, int distance)
    {
        int newScale = Stats.max(scale, scale(11, distance, data[1], data[3], data[5], data[7]));
        for (int ii = 0; ii < 4; ii++)
        {
            data[2 * ii + 1] *= MoreMath.pow(10, newScale - scale);
        }
        this.scale = newScale;
        this.pow = MoreMath.pow(10, scale);
        data[2 * index] = angle;
        data[2 * index + 1] = distance / pow;
    }

    public int getAngle(int index)
    {
        return data[2 * index];
    }

    public int getDistance(int index)
    {
        return data[2 * index + 1] * pow;
    }
    
}
