/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.nmea.processor.GeoMagManager;
import java.time.Clock;
import java.time.Instant;
import static java.time.ZoneOffset.UTC;
import org.junit.Test;
import org.vesalainen.nmea.processor.GeoMagManager;
import static org.junit.Assert.*;
import org.vesalainen.nmea.processor.GeoMagManager.Observer;
import static org.vesalainen.nmea.processor.GeoMagManager.Type.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoMagManagerTest
{
    
    public GeoMagManagerTest()
    {
    }

    @Test
    public void test1()
    {
        GeoMagManager gmm = new GeoMagManager();
        gmm.addObserver(DECLINATION, 0.1, (x)->System.err.println("declination "+x));
        gmm.addObserver(DIP_ANGLE, 0.1, (x)->System.err.println("dip "+x));
        gmm.addObserver(VERTICAL_INTENSITY, 0.1, (x)->System.err.println("intensity "+x));
        gmm.update(60, 25, 0);
        gmm.update(60, 25, 0);
        gmm.update(-8, -140, 0);
    }
    @Test
    public void test2015()
    {
        GeoMagManager gmm = new GeoMagManager(Clock.fixed(Instant.parse("2015-01-01T00:00:00Z"), UTC));
        
        Observer o1 = gmm.addObserver(DECLINATION, 0.1, (x)->assertEquals(-3.9, x, 1e-2));
        Observer o2 = gmm.addObserver(DIP_ANGLE, 0.1, (x)->assertEquals(83.03, x, 1e-2));
        gmm.update(80, 0, 0);
        gmm.removeObservers(o1, o2);
        
        o1 = gmm.addObserver(DECLINATION, 0.1, (x)->assertEquals(0.55, x, 1e-2));
        o2 = gmm.addObserver(DIP_ANGLE, 0.1, (x)->assertEquals(-15.86, x, 1e-2));
        gmm.update(0, 120, 0);
        gmm.removeObservers(o1, o2);
    }    
}
