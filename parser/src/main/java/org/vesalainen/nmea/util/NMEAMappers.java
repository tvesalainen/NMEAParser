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

import java.util.function.UnaryOperator;
import org.vesalainen.navi.Navis;

/**
 * NMEAMappers contains stream mappings from NMEASample to NMEASample. Same 
 * sample is mapped but added some pseudo fields
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEAMappers
{
    /**
     * Adds driftAngle property. If sample contains trackMadeGood and 
     * trueHeading properties the driftAngle is -180 - 180 degrees. Negative
     * if drifting to left. If sample doesn't contain trackMadeGood or 
     * trueHeading properties the driftAngle is 0.
     * @return 
     */
    public static final UnaryOperator<NMEASample> driftAngleMap()
    {
        return new DriftAngleMap();
    }
    private static class DriftAngleMap implements UnaryOperator<NMEASample>
    {

        @Override
        public NMEASample apply(NMEASample t)
        {
            if (t.hasProperty("trackMadeGood") && t.hasProperty("trueHeading"))
            {
                double diff = Navis.angleDiff(t.getProperty("trueHeading"), t.getProperty("trackMadeGood"));
                t.setProperty("driftAngle", (float) diff);
            }
            else
            {
                t.setProperty("driftAngle", 0);
            }
            return t;
        }
        
    }
}
