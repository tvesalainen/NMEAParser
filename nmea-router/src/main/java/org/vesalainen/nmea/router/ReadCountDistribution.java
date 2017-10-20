/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ReadCountDistribution
{
    private int[] distribution;
    private String format;

    public ReadCountDistribution(int count)
    {
        distribution = new int[count];
        int precision = (int) Math.round(Math.log10(count))+1;
        format = "%"+precision+"d: %d\n";
    }
    public void increment(int count)
    {
        distribution[count-1]++;
    }

    public List<String> getDistribution()
    {
        List<String> list = new ArrayList<>();
        int len = distribution.length;
        for (int ii=0;ii<len;ii++)
        {
            list.add(String.format(format, ii+1, distribution[ii]));
        }
        return list;
    }
    
}
