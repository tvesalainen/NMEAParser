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
 * Electronic Position Fixing Device
 * @author Timo Vesalainen
 */
public enum EPFDFixTypes
{

    /**
     * Undefined (default)
     */
    UndefinedDefault("Undefined (default)"),
    /**
     * GPS
     */
    GPS("GPS"),
    /**
     * GLONASS
     */
    GLONASS("GLONASS"),
    /**
     * Combined GPS/GLONASS
     */
    CombinedGPSGLONASS("Combined GPS/GLONASS"),
    /**
     * Loran-C
     */
    LoranC("Loran-C"),
    /**
     * Chayka
     */
    Chayka("Chayka"),
    /**
     * Integrated navigation system
     */
    IntegratedNavigationSystem("Integrated navigation system"),
    /**
     * Surveyed
     */
    Surveyed("Surveyed"),
    /**
     * Galileo
     */
    Galileo("Galileo"),
    
    Undefined9("Undefined9"),
    Undefined10("Undefined10"),
    Undefined11("Undefined11"),
    Undefined12("Undefined12"),
    Undefined13("Undefined13"),
    Undefined14("Undefined14"),
    /**
     * Note: though values 9-15 are marked "not used" in [IALA], 
     * the EPFD type value 15 (all field bits 1) is not uncommon in the wild; 
     * it appears some receivers emit it as the Undefined value. 
     * Decoders should be prepared to accept this.
     */
    Undefined15("Undefined15");
    private String description;

    EPFDFixTypes(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
