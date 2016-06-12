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
package org.vesalainen.nmea.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class NMEAMappersTest
{
    private static final double Epsilon = 1e-10;
    public NMEAMappersTest()
    {
    }

    @Test
    public void testDriftAngleMap1()
    {
        NMEAStream.Builder builder = new NMEAStream.Builder();
        builder.addSample()
                .setProperty("trackMadeGood", 350)
                .setProperty("trueHeading", 340);
        NMEASample sample = builder.build().map(NMEAMappers.driftAngleMap()).findFirst().get();
        assertEquals(10, sample.getProperty("driftAngle"), Epsilon);
    }
    
    @Test
    public void testDriftAngleMap2()
    {
        NMEAStream.Builder builder = new NMEAStream.Builder();
        builder.addSample()
                .setProperty("trueHeading", 340);
        NMEASample sample = builder.build().map(NMEAMappers.driftAngleMap()).findFirst().get();
        assertEquals(0, sample.getProperty("driftAngle"), Epsilon);
    }
    
}
