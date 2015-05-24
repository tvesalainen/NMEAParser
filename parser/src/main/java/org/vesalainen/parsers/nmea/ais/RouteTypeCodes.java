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
public enum RouteTypeCodes
{

    /**
     * Undefined (default)
     */
    UndefinedDefault("Undefined (default)"),
    /**
     * Mandatory
     */
    Mandatory("Mandatory"),
    /**
     * Recommended
     */
    Recommended("Recommended"),
    /**
     * Alternative
     */
    Alternative("Alternative"),
    /**
     * Recommended route through ice
     */
    RecommendedRouteThroughIce("Recommended route through ice"),
    /**
     * Ship route plan
     */
    ShipRoutePlan("Ship route plan"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage7("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage8("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage9("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage10("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage11("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage12("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage13("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage14("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage15("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage16("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage17("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage18("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage19("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage20("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage21("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage22("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage23("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage24("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage25("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage26("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage27("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage28("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage29("Reserved for future usage"),
    /**
     * Reserved for future usage
     */
    ReservedForFutureUsage30("Reserved for future usage"),
    /**
     * Cancel route identified by message linkage
     */
    CancelRouteIdentifiedByMessageLinkage("Cancel route identified by message linkage");
    private String description;

    RouteTypeCodes(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
