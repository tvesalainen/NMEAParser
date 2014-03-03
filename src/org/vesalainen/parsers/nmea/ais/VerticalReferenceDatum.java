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
public enum VerticalReferenceDatum
{

    /**
     * Mean Lower Low Water (MLLW)
     */
    MeanLowerLowWaterMLLW("Mean Lower Low Water (MLLW)"),
    /**
     * International Great Lakes Datum (IGLD-85)
     */
    InternationalGreatLakesDatumIGLD85("International Great Lakes Datum (IGLD-85)"),
    /**
     * Local river datum
     */
    LocalRiverDatum("Local river datum"),
    /**
     * Station Datum (STND)
     */
    StationDatumSTND("Station Datum (STND)"),
    /**
     * Mean Higher High Water (MHHW)
     */
    MeanHigherHighWaterMHHW("Mean Higher High Water (MHHW)"),
    /**
     * Mean High Water (MHW)
     */
    MeanHighWaterMHW("Mean High Water (MHW)"),
    /**
     * Mean Sea Level (MSL)
     */
    MeanSeaLevelMSL("Mean Sea Level (MSL)"),
    /**
     * Mean Low Water (MLW)
     */
    MeanLowWaterMLW("Mean Low Water (MLW)"),
    /**
     * National Geodetic Vertical Datum (NGVD-29)
     */
    NationalGeodeticVerticalDatumNGVD29("National Geodetic Vertical Datum (NGVD-29)"),
    /**
     * North American Vertical Datum (NAVD-88)
     */
    NorthAmericanVerticalDatumNAVD88("North American Vertical Datum (NAVD-88)"),
    /**
     * World Geodetic System (WGS-84)
     */
    WorldGeodeticSystemWGS84("World Geodetic System (WGS-84)"),
    /**
     * Lowest Astronomical Tide (LAT)
     */
    LowestAstronomicalTideLAT("Lowest Astronomical Tide (LAT)"),
    /**
     * pool
     */
    Pool("pool"),
    /**
     * gauge
     */
    Gauge("gauge"),
    /**
     * Unknown/not available (default)
     */
    UnknownNotAvailableDefault("Unknown/not available (default)"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse16("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse17("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse18("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse19("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse20("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse21("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse22("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse23("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse24("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse25("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse26("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse27("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse28("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse29("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse30("Reserved for future use");
    private String description;

    VerticalReferenceDatum(String description)
    {
        this.description = description;
    }

    public String toString()
    {
        return description;
    }
}
