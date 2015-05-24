/*
 * Copyright (C) 2014 Timo Vesalainen
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

import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen
 */
public class BoatMonitor extends AbstractPropertySetter implements Transactional
{
    public static final String RateOfTurn = "rateOfTurn";
    public static final String SpeedOverGround = "speedOverGround";
    public static final String Longitude = "longitude";
    public static final String Latitude = "latitude";
    
    private float latitude;
    private float longitude;
    private float speedOverGround;
    private float rateOfTurn;

    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
            case RateOfTurn:
                rateOfTurn = arg;
                break;
            case SpeedOverGround:
                speedOverGround = arg;
                break;
            case Longitude:
                longitude = arg;
                break;
            case Latitude:
                latitude = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void rollback(String reason)
    {
    }

    @Override
    public void commit(String reason)
    {
    }
    
}
