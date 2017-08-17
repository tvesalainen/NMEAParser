/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.time.MutableClock;
import org.vesalainen.time.SimpleMutableDateTime;

/**
 * Transactional MutableClock.
 * <p>
 * There are two modes depending on base clock.
 * <p>Live (default). Data coming from active GPS. Time is updated between 
 * time-setting NMEA sentences to give accurate timing for other sentences as well.
 * <p>Fixed (use fixed base clock). Data coming from recorded NMEA sentences
 * like track file. Time is not updated between time-setting NMEA sentences.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class GPSClock extends MutableClock implements NMEAClock
{
    private SimpleMutableDateTime uncommitted = new SimpleMutableDateTime();
    private int localZoneMinutes;
    private long startTime;
    private long updTime;
    /**
     * Creates a GPSClock in live mode using systemUTC base clock.
     */
    public GPSClock()
    {
        this(Clock.systemUTC());
    }
    /**
     * Creates a GPSClock in live or fixed mode depending on argument.
     * @param live 
     */
    public GPSClock(boolean live)
    {
        this(live ? Clock.systemUTC() : Clock.fixed(Instant.now(), ZoneOffset.UTC));
    }
    
    /**
     * Creates a GPSClock. If clock is fixed mode is fixed.
     * <p>It only matter if the clock is fixed or live. This clock always returns
     * time according to GPS data. Live or recorded.
     * @param clock 
     * @see java.time.Clock#fixed(java.time.Instant, java.time.ZoneId) 
     */
    public GPSClock(Clock clock)
    {
        super(clock);
    }
    
    @Override
    public void start(String reason)
    {
        startTime = clock.millis();
        this.localZoneMinutes = 0;
    }

    @Override
    public void commit(String reason)
    {
        updTime = startTime;
        uncommitted.getFields().entrySet().stream().forEach((e) ->
        {
            super.set(e.getKey(), e.getValue().getValue());
        });
        if (localZoneMinutes != 0)
        {
            ZonedDateTime zonedDateTime = getZonedDateTime();
            zonedDateTime = zonedDateTime.plusMinutes(-localZoneMinutes);
            setZonedDateTime(zonedDateTime);
        }
    }

    @Override
    public void rollback(String reason)
    {
    }

    @Override
    protected long getUpdated()
    {
        return updTime;
    }

    @Override
    public void set(ChronoField chronoField, int amount)
    {
        uncommitted.set(chronoField, amount);
    }

    @Override
    public void setZonedDateTime(ZonedDateTime zonedDateTime)
    {
        for (ChronoField cf : SupportedFields)
        {
            super.set(cf, zonedDateTime.get(cf));
        }
    }
    
    /**
     * Sets Zone hour offset. This method exist to support ZDA sentence which is
     * most probable deprecated.
     * @param localZoneHours 
     */
    @Override
    public void setZoneHours(int localZoneHours)
    {
        this.localZoneMinutes += 60*localZoneHours;
    }
    /**
     * Sets Zone minute offset. This method exist to support ZDA sentence which is
     * most probable deprecated.
     * @param localZoneMinutes 
     */
    @Override
    public void setZoneMinutes(int localZoneMinutes)
    {
        this.localZoneMinutes += 60*localZoneMinutes;
    }

    @Override
    public boolean isCommitted()
    {
        return !fields.isEmpty();
    }
    /**
     * Returns the offset in milliseconds between systemUTC time and this clock.
     * @return 
     */
    @Override
    public long offset()
    {
        return Clock.systemUTC().millis() - millis();
    }

}
