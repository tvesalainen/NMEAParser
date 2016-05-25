/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.parsers.nmea.time;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.time.MutableClock;
import org.vesalainen.time.SimpleMutableTime;

/**
 *
 * @author tkv
 */
public final class GPSClock extends MutableClock implements NMEAClock
{
    SimpleMutableTime uncommitted = new SimpleMutableTime();

    public GPSClock()
    {
        this(Clock.systemUTC());
    }

    public GPSClock(Clock clock)
    {
        super(clock);
    }
    
    @Override
    public void start(String reason)
    {
    }

    @Override
    public void commit(String reason)
    {
        fields.putAll(uncommitted.getFields());
    }

    @Override
    public void rollback(String reason)
    {
    }

    @Override
    public void set(ChronoField chronoField, int amount)
    {
        uncommitted.set(chronoField, amount);
    }
    
    /**
     * Sets Zone hour offset. This method exist to support ZDA sentence which is
     * most probable deprecated.
     * @param localZoneHours 
     */
    @Override
    public void setZoneHours(int localZoneHours)
    {
        if (localZoneHours != 0)
        {
            ZonedDateTime zonedDateTime = getZonedDateTime();
            zonedDateTime = zonedDateTime.plusHours(-localZoneHours);
            setZonedDateTime(zonedDateTime);
        }
    }
    /**
     * Sets Zone minute offset. This method exist to support ZDA sentence which is
     * most probable deprecated.
     * @param localZoneMinutes 
     */
    @Override
    public void setZoneMinutes(int localZoneMinutes)
    {
        if (localZoneMinutes != 0)
        {
            ZonedDateTime zonedDateTime = getZonedDateTime();
            zonedDateTime = zonedDateTime.plusMinutes(-localZoneMinutes);
            setZonedDateTime(zonedDateTime);
        }
    }

    @Override
    public boolean isCommitted()
    {
        return !fields.isEmpty();
    }

    @Override
    public long offset()
    {
        return Clock.systemUTC().millis() - millis();
    }

}
