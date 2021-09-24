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
package org.vesalainen.nmea.processor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import static java.time.ZoneOffset.UTC;
import static java.util.concurrent.TimeUnit.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class N2KClock extends Clock
{
    private static final long MILLIS_IN_DAY = MILLISECONDS.convert(1, DAYS);
    private long days;
    private long micros;

    public void setDays(int days)
    {
        this.days = days;
    }

    public void setMicros(long micros)
    {
        this.micros = micros;
    }
    
    @Override
    public ZoneId getZone()
    {
        return UTC;
    }

    @Override
    public Clock withZone(ZoneId zone)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Instant instant()
    {
        return Instant.ofEpochMilli(millis());
    }

    @Override
    public long millis()
    {
        return days*MILLIS_IN_DAY + micros/1000;
    }
    
}
