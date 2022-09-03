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
package org.vesalainen.parsers.nmea;

import org.vesalainen.math.Unit;
import static org.vesalainen.math.UnitType.*;
import static org.vesalainen.parsers.nmea.MessageType.*;
import static org.vesalainen.parsers.nmea.NMEACategory.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface XdrObserver
{
    @NMEA0183({XDR})
    @NMEACat(SPEED)
    @Unit(value=KNOT, min=0, max=50)
    void setDriftSpeedOverGround(float value);
    @NMEA0183({XDR})
    @NMEACat(WIND)
    @Unit(value=DEGREE, min=0, max=360)
    void setDriftAngleOverGround(float value);
    @NMEA0183({XDR})
    @NMEACat(SPEED)
    @Unit(value=KNOT, min=0, max=50)
    void setWindSpeedOverGround(float value);
    @NMEA0183({XDR})
    @NMEACat(WIND)
    @Unit(value=DEGREE, min=0, max=360)
    void setWindAngleOverGround(float value);
    @NMEA0183({XDR})
    @NMEACat(TEMPERATURE)
    @Unit(value=CELSIUS, min=-60, max=60)
    void setOutsideTemperature(float value);
    @NMEA0183({XDR})
    @NMEACat(PRESSURE)
    @Unit(value=HPA, min=800, max=1400)
    void setAtmosphericPressure(float value);
    @NMEA0183({XDR})
    void setYaw(float value);
    /**
     * Pitch: oscillation of vessel about its latitudinal axis. Bow moving up is
     * positive. Value reported to the nearest 0.1 degree.
     * @param value 
     */
    @NMEA0183({XDR})
    @NMEACat(ATTITUDE)
    @Unit(value=DEGREE_NEG, min=-60, max=60)
    void setPitch(float value);
    /**
     * Roll: oscillation of vessel about its longitudinal axis. Roll to the
     * starboard is positive. Value reported to the nearest 0.1 degree.
     * @param value 
     */
    @NMEA0183({XDR})
    @NMEACat(ATTITUDE)
    @Unit(value=DEGREE_NEG, min=-100, max=100)
    void setRoll(float value);
    @NMEA0183({XDR})
    @NMEACat(VOLTAGE)
    @Unit(value=VOLT, min=0, max=100)
    void setBatteryVoltage0(float v);
    @NMEA0183({XDR})
    @NMEACat(VOLTAGE)
    @Unit(value=VOLT, min=0, max=100)
    void setBatteryVoltage1(float v);
    @NMEA0183({XDR})
    @NMEACat(VOLTAGE)
    @Unit(value=VOLT, min=0, max=100)
    void setBatteryVoltage2(float v);
    @NMEA0183({XDR})
    @NMEACat(VOLTAGE)
    @Unit(value=VOLT, min=0, max=100)
    void setBatteryVoltage3(float v);
    @NMEA0183({XDR})
    @NMEACat(ELECTRIC_CURRENT)
    @Unit(value=AMPERE, min=0, max=100)
    void setBatteryCurrent0(float a);
    @NMEA0183({XDR})
    @NMEACat(ELECTRIC_CURRENT)
    @Unit(value=AMPERE, min=0, max=100)
    void setBatteryCurrent1(float a);
    @NMEA0183({XDR})
    @NMEACat(ELECTRIC_CURRENT)
    @Unit(value=AMPERE, min=0, max=100)
    void setBatteryCurrent2(float a);
    @NMEA0183({XDR})
    @NMEACat(ELECTRIC_CURRENT)
    @Unit(value=AMPERE, min=0, max=100)
    void setBatteryCurrent3(float a);
    @NMEA0183({XDR})
    @NMEACat(TEMPERATURE)
    @Unit(value=CELSIUS, min=0, max=100)
    void setBatteryTemperature0(float c);
    @NMEA0183({XDR})
    @NMEACat(TEMPERATURE)
    @Unit(value=CELSIUS, min=0, max=100)
    void setBatteryTemperature1(float c);
    @NMEA0183({XDR})
    @NMEACat(TEMPERATURE)
    @Unit(value=CELSIUS, min=0, max=100)
    void setBatteryTemperature2(float c);
    @NMEA0183({XDR})
    @NMEACat(TEMPERATURE)
    @Unit(value=CELSIUS, min=0, max=100)
    void setBatteryTemperature3(float c);
    @NMEA0183({XDR})
    @NMEACat(DISTANCE)
    @Unit(value=METER, min=0, max=16)
    void setTideRange(float c);
    @NMEA0183({XDR})
    @NMEACat(PHASE)
    @Unit(value=DEGREE, min=0, max=360)
    void setTidePhase(float c);
    @NMEA0183({XDR})
    @NMEACat(PERCENT)
    @Unit(value=UNITLESS, min=0, max=100)
    void setStateOfCharge0(float c);
    @NMEA0183({XDR})
    @NMEACat(PERCENT)
    @Unit(value=UNITLESS, min=0, max=100)
    void setStateOfCharge1(float c);
    @NMEA0183({XDR})
    @NMEACat(PERCENT)
    @Unit(value=UNITLESS, min=0, max=100)
    void setStateOfCharge2(float c);
    @NMEA0183({XDR})
    @NMEACat(PERCENT)
    @Unit(value=UNITLESS, min=0, max=100)
    void setStateOfCharge3(float c);
    @NMEA0183({XDR})
    @NMEACat(PERCENT)
    @Unit(value=UNITLESS, min=0, max=100)
    void setStateOfHealth0(float c);
    @NMEA0183({XDR})
    @NMEACat(PERCENT)
    @Unit(value=UNITLESS, min=0, max=100)
    void setStateOfHealth1(float c);
    @NMEA0183({XDR})
    @NMEACat(PERCENT)
    @Unit(value=UNITLESS, min=0, max=100)
    void setStateOfHealth2(float c);
    @NMEA0183({XDR})
    @NMEACat(PERCENT)
    @Unit(value=UNITLESS, min=0, max=100)
    void setStateOfHealth3(float c);
}
