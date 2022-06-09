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
package org.vesalainen.nmea.server;

import static java.lang.Math.*;
import java.util.function.DoubleFunction;
import org.junit.Test;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CreateSine
{
    
    public CreateSine()
    {
    }

    @Test
    public void test0()
    {
        DoubleFunction fx = (x)->toDegrees(x)/3.6;
        DoubleFunction fy = (y)->40-(20*y+20);
        double startX = -2*PI;
        double sl = 0.512286623256592433;
        StringBuilder sb = new StringBuilder();
        sb.append("M ");
        sb.append(fx.apply(startX));
        sb.append(" ");
        sb.append(fy.apply(sin(startX)));
        for (double x0=startX;x0<2*PI;x0+=2*PI)
        {
            double x1 = x0+PI/2;
            double x2 = x1+PI/2;
            double x3 = x2+PI/2;
            double x4 = x3+PI/2;
            c(sb, fx, fy, x0+sl,    sl,     x0+1,   1,      x1, 1);
            c(sb, fx, fy, x2-1,     1,      x2-sl,  sl,     x2, 0);
            c(sb, fx, fy, x2+sl,    -sl,    x2+1,   -1,     x3, -1);
            c(sb, fx, fy, x4-1,     -1,     x4-sl,  -sl,    x4, 0);
        }
        System.err.println(sb.toString());
    }
    private void c(StringBuilder sb, DoubleFunction fx, DoubleFunction fy, double px1, double py1, double px2, double py2, double x, double y)
    {
        sb.append("C ");
        sb.append(fx.apply(px1));
        sb.append(" ");
        sb.append(fy.apply(py1));
        sb.append(" ");
        sb.append(fx.apply(px2));
        sb.append(" ");
        sb.append(fy.apply(py2));
        sb.append(" ");
        sb.append(fx.apply(x));
        sb.append(" ");
        sb.append(fy.apply(y));
    }
}
