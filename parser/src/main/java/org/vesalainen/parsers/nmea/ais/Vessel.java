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
    public static final String RateOfTurn = "rateOfTurn";
    public static final String Speed = "speed";
    public static final String Longitude = "longitude";
    public static final String Latitude = "latitude";
    public static final String CourseOverGround = "courseOverGround";
    public static final String Draught = "draugth";
    public static final String Second = "second";
    public static final String ImoNumber = "imoNumber";
    public static final String Heading = "heading";
    public static final String DimensionToBow = "dimensionToBow";
    public static final String DimensionToStern = "dimensionToStern";
    public static final String DimensionToPort = "dimensionToPort";
    public static final String DimensionToStarboard = "dimensionToStarboard";
    public static final String VesselName = "vesselName";
    public static final String CallSign = "callSign";
    public static final String Destination = "destination";
    public static final String NavigationStatus = "navigationStatus";
    public static final String ShipType = "shipType";
    public static final String CsUnit = "csUnit";
    public static final String Display = "display";
    public static final String Dsc = "dsc";
    public static final String Band = "band";
    public static final String Msg22 = "msg22";
    public static final String Assigned = "assigned";
    public static final String Raim = "raim";
    public static final String[] Properties = new String[] {
        RateOfTurn,
        Speed,
        Longitude,
        Latitude,
        CourseOverGround,
        Draught,
        Second,
        ImoNumber,
        Heading,
        DimensionToBow,
        DimensionToStern,
        DimensionToPort,
        DimensionToStarboard,
        VesselName,
        CallSign,
        Destination,
        NavigationStatus,
        ShipType,
        CsUnit,
        Display,
        Dsc,
        Band,
        Msg22,
        Assigned,
        Raim
    };
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
            case RateOfTurn:
                rateOfTurn = arg;
                break;
            case Speed:
                speed = arg;
                break;
            case Longitude:
                longitude = arg;
                break;
            case Latitude:
                latitude = arg;
                break;
            case CourseOverGround:
                courseOverGround = arg;
                break;
            case Draught:
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
            case Second:
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
            case ImoNumber:
                imoNumber = arg;
                break;
            case Heading:
                heading = arg;
                break;
            case DimensionToBow:
                dimensionToBow = arg;
                break;
            case DimensionToStern:
                dimensionToStern = arg;
                break;
            case DimensionToPort:
                dimensionToPort = arg;
                break;
            case DimensionToStarboard:
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
            case VesselName:
                vesselName = (String) arg;
                break;
            case CallSign:
                callSign = (String) arg;
                break;
            case Destination:
                destination = (String) arg;
                break;
            case NavigationStatus:
                navigationStatus = (NavigationStatus) arg;
                break;
            case ShipType:
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
            case CsUnit:
                csUnit = arg;
                break;
            case Display:
                display = arg;
                break;
            case Dsc:
                dsc = arg;
                break;
            case Band:
                band = arg;
                break;
            case Msg22:
                msg22 = arg;
                break;
            case Assigned:
                assigned = arg;
                break;
            case Raim:
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

    @Override
    protected void setProperty(String property, Object arg)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
