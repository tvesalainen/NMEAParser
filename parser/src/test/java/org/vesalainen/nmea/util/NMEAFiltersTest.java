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
package org.vesalainen.nmea.util;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEAFiltersTest
{
    
    public NMEAFiltersTest()
    {
    }

    @Test
    public void testAccumulatorMap()
    {
        NMEAStream.Builder builder = new NMEAStream.Builder();
        builder.addSample()
                .setProperty("a", 1);
        builder.addSample()
                .setProperty("b", 2);
        List<NMEASample> list = builder.build().map(NMEAFilters.accumulatorMap()).collect(Collectors.toList());
        assertEquals(2, list.size());
        assertTrue(list.get(0).hasProperty("a"));
        assertFalse(list.get(0).hasProperty("b"));
        assertTrue(list.get(1).hasProperty("a"));
        assertTrue(list.get(1).hasProperty("b"));
    }
    
    @Test
    public void testPeriodicFilter()
    {
        NMEAStream.Builder builder = new NMEAStream.Builder();
        builder.addWaypoint(0, 0, 0);
        builder.addWaypoint(1, 0, 0);
        builder.addWaypoint(2, 0, 0);
        List<NMEASample> list = builder.build().filter(NMEAFilters.periodicFilter(1, TimeUnit.MILLISECONDS)).collect(Collectors.toList());
        assertEquals(2, list.size());
        assertEquals(0, list.get(0).getTime());
        assertEquals(2, list.get(1).getTime());
    }
}
