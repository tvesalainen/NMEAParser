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
public enum MooringPosition
{

    /**
     * Not available (default)
     */
    NotAvailableDefault("Not available (default)"),
    /**
     * Port-side to
     */
    PortSideTo("Port-side to"),
    /**
     * Starboard-side to
     */
    StarboardSideTo("Starboard-side to"),
    /**
     * Mediterranean (end-on) mooring
     */
    MediterraneanEndOnMooring("Mediterranean (end-on) mooring"),
    /**
     * Mooring buoy
     */
    MooringBuoy("Mooring buoy"),
    /**
     * Anchorage
     */
    Anchorage("Anchorage"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse7("Reserved for future use");
    private String description;

    MooringPosition(String description)
    {
        this.description = description;
    }

    public String toString()
    {
        return description;
    }
}
