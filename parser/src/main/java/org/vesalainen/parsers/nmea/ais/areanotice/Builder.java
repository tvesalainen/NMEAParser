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

import java.util.ArrayList;
import java.util.List;
import org.vesalainen.math.UnitType;
import org.vesalainen.navi.Navis;
import org.vesalainen.parsers.nmea.ais.AISBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
abstract class Builder<S extends Area, N extends PolyArea>
{
    
    private List<Area> list = new ArrayList<>();
    private PolyArea current;
    private int index;
    private double longitude;
    private double latitude;

    protected Builder()
    {
    }

    public void add(double longitude, double latitude)
    {
        if (list.isEmpty())
        {
            list.add(starter(longitude, latitude));
        }
        else
        {
            if (current == null || index > 3)
            {
                current = next();
                list.add(current);
                index = 0;
            }
            current.set(index, (int) Math.round(Navis.bearing(this.latitude, this.longitude, latitude, longitude)), (int) Math.round(UnitType.convert(Navis.distance(this.latitude, this.longitude, latitude, longitude), UnitType.NM, UnitType.Meter)));
            index++;
        }
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public void build(AISBuilder builder)
    {
        list.forEach((Area area) ->
        {
            area.build(builder);
        });
    }

    protected abstract S starter(double longitude, double latitude);

    protected abstract N next();
    
}
