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
package org.vesalainen.nmea.processor.n2kgw;

import org.vesalainen.nmea.processor.n2kgw.AbstractNMEACompiler;
import org.vesalainen.parsers.nmea.NMEAPGN;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class PlainNMEACompiler extends AbstractNMEACompiler
{

    public PlainNMEACompiler(NMEASender store)
    {
        super(store);
        addPgnSetter(NMEAPGN.VESSEL_HEADING, "True_Heading", "trueHeading");
        
        addPgnSetter(NMEAPGN.WATER_DEPTH, "Water_Depth_Transducer", "depthOfWater");
        addPgnSetter(NMEAPGN.WATER_DEPTH, "Offset", "transducerOffset");
        
        addPgnSetter(NMEAPGN.POSITION_RAPID_UPDATE, "Latitude", "latitude");
        addPgnSetter(NMEAPGN.POSITION_RAPID_UPDATE, "Longitude", "longitude");
        
        addPgnSetter(NMEAPGN.COG_SOG_RAPID_UPDATE, "Speed_Over_Ground", "speedOverGround");
        addPgnSetter(NMEAPGN.COG_SOG_RAPID_UPDATE, "True_Course_Over_Ground", "trackMadeGood");
        
        addPgnSetter(NMEAPGN.ENVIRONMENTAL_PARAMETERS, "Sea_Temperature", "waterTemperature");
        
        addPgnSetter(NMEAPGN.WIND_DATA, "Apparent_Wind_Speed", "relativeWindSpeed");
        addPgnSetter(NMEAPGN.WIND_DATA, "Apparent_Wind_Direction", "relativeWindAngle");
        
        addPgnSetter(NMEAPGN.SPEED_WATER_REFERENCED, "Speed_Water_Referenced", "waterSpeed");
    }

    
}
