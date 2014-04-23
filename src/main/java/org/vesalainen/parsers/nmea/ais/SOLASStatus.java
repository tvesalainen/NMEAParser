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
public enum SOLASStatus
{

    /**
     * Not available or requested (default)
     */
    NotAvailableOrRequestedDefault("Not available or requested (default)"),
    /**
     * Equipment operational
     */
    EquipmentOperational("Equipment operational"),
    /**
     * Equipment not operational
     */
    EquipmentNotOperational("Equipment not operational"),
    /**
     * No data (equipment may or may not be on board/or its status is unknown)
     */
    NoDataEquipmentMayOrMayNotBeOnBoardOrItsStatusIsUnknown("No data (equipment may or may not be on board/or its status is unknown)");
    private String description;

    SOLASStatus(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
