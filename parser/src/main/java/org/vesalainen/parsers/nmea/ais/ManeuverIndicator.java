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

/**
 *
 * @author Timo Vesalainen
 */
public enum ManeuverIndicator
{

    /**
     * Not available (default)
     */
    NotAvailableDefault(""),
    /**
     * No special maneuver
     */
    NoSpecialManeuver("No special maneuver"),
    /**
     * Special maneuver (such as regional passing arrangement)
     */
    SpecialManeuverSuchAsRegionalPassingArrangement("Special maneuver (such as regional passing arrangement)");
    private String description;

    ManeuverIndicator(String description)
    {
        this.description = description;
    }
    public String getDescription()
    {
        return description;
    }
}
