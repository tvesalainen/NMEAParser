/*
 * Copyright (C) 2013 Timo Vesalainen
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

package org.vesalainen.parsers.nmea.ais;

import java.util.HashMap;
import java.util.Map;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.parsers.nmea.Clock;
import org.vesalainen.util.Transactional;

/**
 * @author Timo Vesalainen
 */
public class VesselMonitor extends AbstractPropertySetter implements Transactional
{
    private final Map<Integer,Vessel> map = new HashMap<>();
    private Clock clock;
    private Vessel target;

    public VesselMonitor(Clock clock)
    {
        this.clock = clock;
    }

    @Override
    public void rollback(String reason)
    {
        target.rollback(reason);
        target = null;
    }

    @Override
    public void commit(String reason)
    {
        target.commit(reason);
        target = null;
    }

    @Override
    public void set(String property, boolean arg)
    {
        target.set(property, arg);
    }

    @Override
    public void set(String property, char arg)
    {
        target.set(property, arg);
    }

    @Override
    public void set(String property, int arg)
    {
        switch (property)
        {
            case "mmsi":
                target = map.get(arg);
                if (target == null)
                {
                    target = new Vessel(clock, arg);
                    map.put(arg, target);
                }
                break;
            default:
                target.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        target.set(property, arg);
    }

}
