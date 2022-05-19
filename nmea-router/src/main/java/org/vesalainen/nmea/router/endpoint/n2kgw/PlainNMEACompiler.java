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
package org.vesalainen.nmea.router.endpoint.n2kgw;

import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import org.vesalainen.can.CanSource;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
import static org.vesalainen.can.dbc.ValueType.SIGNED;
import static org.vesalainen.parsers.nmea.NMEAPGN.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class PlainNMEACompiler extends AbstractNMEACompiler
{

    public PlainNMEACompiler(NMEASender store)
    {
        super(store);
        addPgnSetter(VESSEL_HEADING, "True_Heading", "trueHeading");
        
        addPgnSetter(WATER_DEPTH, "Water_Depth_Transducer", "depthOfWaterRelativeToTransducer");
        addPgnSetter(WATER_DEPTH, "Offset", "transducerOffset");
        addPgnSetter(WATER_DEPTH, "Maximum_Range_Scale", "maximumRangeScale");
        
        addPgnSetter(POSITION_RAPID_UPDATE, "Latitude", "latitude");
        addPgnSetter(POSITION_RAPID_UPDATE, "Longitude", "longitude");
        
        addPgnSetter(GNSS_POSITION_DATA, "Position_Date", "positionDate");
        addPgnSetter(GNSS_POSITION_DATA, "Position_Time", "positionTime");
        addPgnSetter(GNSS_POSITION_DATA, "Latitude", "latitude");
        addPgnSetter(GNSS_POSITION_DATA, "Longitude", "longitude");
        addPgnSetter(GNSS_POSITION_DATA, "Method_Gnss", "methodGnss");
        
        addPgnSetter(COG_SOG_RAPID_UPDATE, "Speed_Over_Ground", "speedOverGround");
        addPgnSetter(COG_SOG_RAPID_UPDATE, "True_Course_Over_Ground", "trackMadeGood");
        
        addPgnSetter(ENVIRONMENTAL_PARAMETERS, "Sea_Temperature", "waterTemperature");
        addPgnSetter(ENVIRONMENTAL_PARAMETERS, "Outside_Temperature", "outsideTemperature");
        addPgnSetter(ENVIRONMENTAL_PARAMETERS, "Atmospheric_Pressure", "atmosphericPressure");
        
        addPgnSetter(WIND_DATA, "Apparent_Wind_Speed", "relativeWindSpeed");
        addPgnSetter(WIND_DATA, "Apparent_Wind_Direction", "relativeWindAngle");
        
        addPgnSetter(SPEED_WATER_REFERENCED, "Speed_Water_Referenced", "waterSpeed");
        
        addPgnSetter(ATTITUDE, "Yaw", "yaw");
        addPgnSetter(ATTITUDE, "Pitch", "pitch");
        addPgnSetter(ATTITUDE, "Roll", "roll");

        addPgnSetter(BATTERY_STATUS, "Battery_Instance", "batteryInstance");
        addPgnSetter(BATTERY_STATUS, "Battery_Voltage", "voltage");
        addPgnSetter(BATTERY_STATUS, "Battery_Current", "current");
        addPgnSetter(BATTERY_STATUS, "Battery_Case_Temperature", "temperature");
    }

    @Override
    public ToDoubleFunction<CanSource> compileDoubleBoundCheck(MessageClass mc, SignalClass sc, ToLongFunction<CanSource> toLongFunction)
    {
        // bit field max values usually mean that data is not available.
        int size = sc.getSize();
        if (size == 1)
        {
            return (buf)->
            {
                return toLongFunction.applyAsLong(buf);
            };
        }
        else
        {
            if (sc.getValueType() == SIGNED)
            {
                long max = -1L>>>(65-size);
                long min =-(max+1);
                return (buf)->
                {
                    long value = toLongFunction.applyAsLong(buf);
                    if (value == max || value == min)
                    {
                        return Double.NaN;
                    }
                    else
                    {
                        return value;
                    }
                };
            }
            else
            {
                long max = -1L>>>(64-size);
                return (buf)->
                {
                    long value = toLongFunction.applyAsLong(buf);
                    if (value == max)
                    {
                        return Double.NaN;
                    }
                    else
                    {
                        return value;
                    }
                };
            }
        }
    }

    
}
