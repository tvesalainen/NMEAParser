/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.server;

import org.vesalainen.math.sliding.TimeValueConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class Delay implements TimeValueConsumer
{
    
    private final long delay;
    private long last;
    private TimeValueConsumer next;

    public Delay(long delay, TimeValueConsumer next)
    {
        this.delay = delay;
        this.next = next;
    }

    @Override
    public void accept(long t, double v)
    {
        if (t - last >= delay)
        {
            next.accept(t, v);
            last = t;
        }
    }
    
}