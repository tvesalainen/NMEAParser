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
    private int count;
    
    private void reset()
    {
        latitude = Float.NaN;
        longitude = Float.NaN;
        value = 0;
        count = 0;
    }
    @Override
    protected Cond acceptField(CharSequence cs, int index, int begin, int end)
    {
        switch (index)
        {
            case 0:
                if (!equals("$GPRMC", cs, begin, end))
                {
                    return Cond.Accept;
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
                        log.warning("rejected got %c expected N/S", cs.charAt(begin));
                        return Cond.Reject;
                }
                if (!Float.isNaN(latitude))
                {
                    if (Math.abs(latitude-value) > 0.1)
                    {
                        if (count < 2)
                        {
                            reset();
                        }
                        log.warning("latitude %f differs too much from %f rejected", latitude, value);
                        return Cond.Reject;
                    }
                }
                latitude = value;
                log.finest("latitude=%f", latitude);
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
                        log.warning("rejected got %c expected E/W", cs.charAt(begin));
                        return Cond.Reject;
                }
                if (!Float.isNaN(longitude))
                {
                    if (Math.abs(longitude-value) > 0.1)
                    {
                        if (count < 2)
                        {
                            reset();
                        }
                        log.warning("longitude %f differs too much from %f rejected", longitude, value);
                        return Cond.Reject;
                    }
                }
                longitude = value;
                log.finest("longitude=%f", longitude);
                break;
            case 12:
                if (count < 10)
                {
                    count++;
                }
                if (count < 2)
                {
                    return Cond.Reject;
                }
                break;
        }
        return Cond.GoOn;
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
