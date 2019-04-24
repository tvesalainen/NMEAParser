/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.experimental;

import static java.lang.Math.*;
import java.util.Arrays;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Filter
{
    private double[] coef;
    private double[] shift;
    private int m;
    private int index;
    
    public Filter(double[] coef)
    {
        this.m = coef.length;
        this.coef = coef;
        shift = new double[m];
    }
    /**
     * Returns a averaging filter
     * @param size 
     * @return 
     */
    public static Filter averageFilter(int size)
    {
        double[] coef = new double[size];
        Arrays.fill(coef, 1/(double)size);
        return new Filter(coef);
    }
    public double filter(double x)
    {
        double sum = 0;
        shift[index] = x;
        int j = index;
        for (int jj=0;jj<m;jj++)
        {
            sum += coef[jj]*shift[j];
            j++;
            if (j == m)
            {
                j = 0;
            }
        }
        index--;
        if (index < 0)
        {
            index = m-1;
        }
        return sum;
    }
}
