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

/**
 * @author Timo Vesalainen
 */
public class Vessel extends AbstractAISObserver
{
    protected int mmsi;
    private NavigationStatus navigationStatus;
    private float degreesPerMinute;
    private float speed;
    private double longitude;
    private float latitude;
    private float courseOverGround;
    private Calendar calendar;

    public Vessel(int mmsi)
    {
        this.mmsi = mmsi;
        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void setNavigationStatus(NavigationStatus navigationStatus)
    {
        this.navigationStatus = navigationStatus;
    }

    @Override
    public void setTurn(float degreesPerMinute)
    {
        this.degreesPerMinute = degreesPerMinute;
    }

    @Override
    public void setSpeed(float speed)
    {
        this.speed = speed;
    }

    @Override
    public void setLongitude(float degrees)
    {
        this.longitude = degrees;
    }

    @Override
    public void setLatitude(float latitude)
    {
        this.latitude = latitude;
    }

    @Override
    public void setCourse(float cog)
    {
        this.courseOverGround = cog;
    }

    @Override
    public void setYear(int year)
    {
        calendar.set(Calendar.YEAR, year);
    }

    @Override
    public void setMonth(int month)
    {
        calendar.set(Calendar.MONTH, month-1);
    }

    @Override
    public void setDay(int day)
    {
        calendar.set(Calendar.DAY_OF_MONTH, day);
    }

    @Override
    public void setHour(int hour)
    {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
    }

    @Override
    public void setMinute(int minute)
    {
        calendar.set(Calendar.MINUTE, minute);
    }
    
    @Override
    public void setSecond(int second)
    {
        calendar.set(Calendar.SECOND, second);
    }

}
