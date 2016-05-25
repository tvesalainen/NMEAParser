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
    private int prevTime;
    private int time;
    
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
            case 1:
                time = Primitives.parseInt(cs, begin, end);
                if (time == prevTime)
                {
                    log.warning("rejected because same time %d as previous %d", time, prevTime);
                    return Cond.Reject;
                }
                prevTime = time;
                break;
            case 3:
            case 5:
                value = coordinate(cs, begin, end);
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
                        log.warning("latitude %f differs too much from %f rejected", latitude, value);
                        if (count < 2)
                        {
                            reset();
                        }
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
                        log.warning("longitude %f differs too much from %f rejected", longitude, value);
                        if (count < 2)
                        {
                            reset();
                        }
                        return Cond.Reject;
                    }
                }
                longitude = value;
                log.finest("longitude=%f", longitude);
                break;
            case 11:
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
    private float coordinate(CharSequence cs, int begin, int end)
    {
        int idx = -1;
        for (int ii=begin;ii<end;ii++)
        {
            if (cs.charAt(ii) == '.')
            {
                idx = ii-2;
                break;
            }
        }
        if (idx < 2)
        {
            throw new IllegalArgumentException("illegal coordinate "+cs);
        }
        float deg = Primitives.parseFloat(cs, begin, idx);
        float min = Primitives.parseFloat(cs, idx, end);
        return deg+min/60F;
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
