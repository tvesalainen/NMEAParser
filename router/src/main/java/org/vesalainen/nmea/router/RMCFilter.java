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
package org.vesalainen.nmea.router;

import org.vesalainen.lang.Primitives;

/**
 *
 * @author tkv
 */
public class RMCFilter extends AbstractNMEAFilter
{
    private float latitude = Float.NaN;
    private float longitude = Float.NaN;
    private float value;
    
    @Override
    protected boolean acceptField(CharSequence cs, int index, int begin, int end)
    {
        switch (index)
        {
            case 0:
                if (!equals("$GPRMC", cs, begin, end))
                {
                    return false;
                }
                break;
            case 3:
            case 5:
                value = Primitives.parseFloat(cs, begin, end);
                break;
            case 4:
                switch (cs.charAt(begin))
                {
                    case 'N':
                        break;
                    case 'S':
                        value *= -1;
                        break;
                    default:
                        return false;
                }
                if (!Float.isNaN(latitude))
                {
                    if (Math.abs(latitude-value) > 0.1)
                    {
                        return false;
                    }
                }
                latitude = value;
                break;
            case 6:
                switch (cs.charAt(begin))
                {
                    case 'E':
                        break;
                    case 'W':
                        value *= -1;
                        break;
                    default:
                        return false;
                }
                if (!Float.isNaN(longitude))
                {
                    if (Math.abs(longitude-value) > 0.1)
                    {
                        return false;
                    }
                }
                longitude = value;
                break;
        }
        return true;
    }
    private boolean equals(String str, CharSequence cs, int begin, int end)
    {
        if (str.length() != end-begin)
        {
            return false;
        }
        for (;begin<end;begin++)
        {
            if (str.charAt(begin) != cs.charAt(begin))
            {
                return false;
            }
        }
        return true;
    }
    
}
