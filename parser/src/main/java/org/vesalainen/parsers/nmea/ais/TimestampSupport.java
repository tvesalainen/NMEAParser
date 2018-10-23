/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.time.temporal.ChronoField;
import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.*;
import java.time.temporal.Temporal;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimestampSupport
{
    /**
     * This method will synchronize AIS message receive time to second timestamp
     * field.
     * @param temporal
     * @param second
     * @return 
     */
    public static Temporal adjustIntoSecond(Temporal temporal, int second)
    {
        Temporal with = temporal.with(SECOND_OF_MINUTE, second);
        if (temporal.until(with, SECONDS) > 0)
        {
            return with.minus(1, MINUTES);
        }
        return with;
    }
}
