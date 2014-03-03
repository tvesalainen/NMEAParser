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
public enum SensorReportTypes
{

    /**
     * Site location
     */
    SiteLocation("Site location"),
    /**
     * Station ID
     */
    StationID("Station ID"),
    /**
     * Wind
     */
    Wind("Wind"),
    /**
     * Water level
     */
    WaterLevel("Water level"),
    /**
     * Current flow (2D)
     */
    CurrentFlow2D("Current flow (2D)"),
    /**
     * Current flow (3D)
     */
    CurrentFlow3D("Current flow (3D)"),
    /**
     * Horizontal current flow
     */
    HorizontalCurrentFlow("Horizontal current flow"),
    /**
     * Sea state
     */
    SeaState("Sea state"),
    /**
     * Salinity
     */
    Salinity("Salinity"),
    /**
     * Weather
     */
    Weather("Weather"),
    /**
     * Air gap/Air draft
     */
    AirGapAirDraft("Air gap/Air draft"),
    /**
     * (reserved for future use)
     */
    ReservedForFutureUse("(reserved for future use)");
    private String description;

    SensorReportTypes(String description)
    {
        this.description = description;
    }

    public String toString()
    {
        return description;
    }
}
