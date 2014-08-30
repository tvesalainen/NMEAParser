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

import java.util.Calendar;
import java.util.TimeZone;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.parsers.nmea.Clock;
import org.vesalainen.util.Transactional;

/**
 * @author Timo Vesalainen
 */
public class Vessel extends AbstractPropertySetter implements Transactional
{
    protected final Clock clock;
    protected final int mmsi;
    protected String vesselName;
    protected NavigationStatus navigationStatus;
    protected float rateOfTurn;
    protected float speed;
    protected float longitude;
    protected float latitude;
    protected float courseOverGround;
    protected Calendar calendar;
    protected String callSign;
    protected int imoNumber;
    protected CodesForShipType shipType;
    protected int dimensionToBow;
    protected int dimensionToStern;
    protected int dimensionToPort;
    protected int dimensionToStarboard;
    protected float draught;
    protected String destination;
    protected int heading;
    protected boolean csUnit;
    protected boolean display;
    protected boolean dsc;
    protected boolean band;
    protected boolean msg22;
    protected boolean assigned;
    protected boolean raim;

    public Vessel(Clock clock,int mmsi)
    {
        this.clock = clock;
        this.mmsi = mmsi;
        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
            case "rateOfTurn":
                rateOfTurn = arg;
                break;
            case "speed":
                speed = arg;
                break;
            case "longitude":
                longitude = arg;
                break;
            case "latitude":
                latitude = arg;
                break;
            case "courseOverGround":
                courseOverGround = arg;
                break;
            case "draught":
                draught = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, int arg)
    {
        switch (property)
        {
            case "second":
                calendar.setTimeInMillis(clock.getTime());
                int second = calendar.get(Calendar.SECOND);
                if (second > arg)
                {
                    calendar.roll(Calendar.SECOND, arg-second);
                }
                else
                {
                    calendar.roll(Calendar.SECOND, -60+arg-second);
                }
                break;
            case "imoNumber":
                imoNumber = arg;
                break;
            case "heading":
                heading = arg;
                break;
            case "dimensionToBow":
                dimensionToBow = arg;
                break;
            case "dimensionToStern":
                dimensionToStern = arg;
                break;
            case "dimensionToPort":
                dimensionToPort = arg;
                break;
            case "dimensionToStarboard":
                dimensionToStarboard = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        switch (property)
        {
            case "vesselName":
                vesselName = (String) arg;
                break;
            case "callSign":
                callSign = (String) arg;
                break;
            case "destination":
                destination = (String) arg;
                break;
            case "navigationStatus":
                navigationStatus = (NavigationStatus) arg;
                break;
            case "shipType":
                shipType = (CodesForShipType) arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, boolean arg)
    {
        switch (property)
        {
            case "csUnit":
                csUnit = arg;
                break;
            case "display":
                display = arg;
                break;
            case "dsc":
                dsc = arg;
                break;
            case "band":
                band = arg;
                break;
            case "msg22":
                msg22 = arg;
                break;
            case "assigned":
                assigned = arg;
                break;
            case "raim":
                raim = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, char arg)
    {
        switch (property)
        {
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
