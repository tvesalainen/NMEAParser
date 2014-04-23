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
public enum IceClass
{

    /**
     * Not classified
     */
    NotClassified("Not classified"),
    /**
     * IACS PC 1
     */
    IACSPC1("IACS PC 1"),
    /**
     * IACS PC 2
     */
    IACSPC2("IACS PC 2"),
    /**
     * IACS PC 3
     */
    IACSPC3("IACS PC 3"),
    /**
     * IACS PC 4
     */
    IACSPC4("IACS PC 4"),
    /**
     * IACS PC 5
     */
    IACSPC5("IACS PC 5"),
    /**
     * IACS PC 6 / FSICR IA Super / RS Arc5
     */
    IACSPC6FSICRIASuperRSArc5("IACS PC 6 / FSICR IA Super / RS Arc5"),
    /**
     * IACS PC 7 / FSICR IA / RS Arc4
     */
    IACSPC7FSICRIARSArc4("IACS PC 7 / FSICR IA / RS Arc4"),
    /**
     * FSICR IB / RS Ice3
     */
    FSICRIBRSIce3("FSICR IB / RS Ice3"),
    /**
     * FSICR IC / RS Ice2
     */
    FSICRICRSIce2("FSICR IC / RS Ice2"),
    /**
     * RS Ice1
     */
    RSIce1("RS Ice1"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse12("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse13("Reserved for future use"),
    /**
     * Reserved for future use
     */
    ReservedForFutureUse14("Reserved for future use"),
    /**
     * Not available = default
     */
    NotAvailableDefault("Not available = default");
    private String description;

    IceClass(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
