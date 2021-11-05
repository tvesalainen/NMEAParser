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
package org.vesalainen.parsers.nmea;

import org.vesalainen.math.Unit;
import static org.vesalainen.math.UnitType.DEGREE_NEG;
import static org.vesalainen.math.UnitType.GFORCE_EARTH;
import static org.vesalainen.parsers.nmea.NMEACategory.ACCELERATION;
import static org.vesalainen.parsers.nmea.NMEACategory.ATTITUDE;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface XdrObserver
{
    public void setYaw(float value);
    /**
     * Pitch: oscillation of vessel about its latitudinal axis. Bow moving up is
     * positive. Value reported to the nearest 0.1 degree.
     * @param value 
     */
    @NMEACat(ATTITUDE)
    @Unit(value=DEGREE_NEG, min=-60, max=60)
    public void setPitch(float value);
    /**
     * Roll: oscillation of vessel about its longitudinal axis. Roll to the
     * starboard is positive. Value reported to the nearest 0.1 degree.
     * @param value 
     */
    @NMEACat(ATTITUDE)
    @Unit(value=DEGREE_NEG, min=-100, max=100)
    public void setRoll(float value);
}
