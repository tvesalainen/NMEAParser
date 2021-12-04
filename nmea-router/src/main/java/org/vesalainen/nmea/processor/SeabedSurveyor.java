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

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleBinaryOperator;
import org.vesalainen.math.UnitType;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.navi.BoatPosition;
import org.vesalainen.navi.CoordinateMap;
import org.vesalainen.navi.Tide;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeabedSurveyor
{
    private final CoordinateMap<Square> map;
    private final DoubleBinaryOperator lonPos;
    private final DoubleBinaryOperator latPos;
    private final DoubleMatrix data;

    public SeabedSurveyor(double latitude, double boxSize, UnitType unit, BoatPosition gpsPosition, BoatPosition depthSounderPosition)
    {
        this.map = new CoordinateMap(latitude, boxSize, unit, Square::new);
        this.lonPos = gpsPosition.longitudeAtOperator(depthSounderPosition, latitude);
        this.latPos = gpsPosition.latitudeAtOperator(depthSounderPosition);
        this.data = new DoubleMatrix(1024,2);
        data.reshape(0, 2);
    }
    
    public void update(long time, double longitude, double latitude, double depth, double heading)
    {
        Square square = map.getOrCreate(lonPos.applyAsDouble(longitude, heading), latPos.applyAsDouble(latitude, heading));
        square.setAndDerivate(time, depth);
    }
    private static final long MIN_DELTA = Tide.PERIOD/360;

    double getDepthAt(double external, double get)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private class Square
    {
        private long time;
        private double depth;

        public void setAndDerivate(long time, double depth)
        {
            long deltaTime = time - this.time;
            double deltaDepth = depth - this.depth;
            if (deltaTime < MIN_DELTA && deltaDepth != 0)
            {
                double toDegrees = Tide.toDegrees(deltaTime, TimeUnit.MILLISECONDS);
                double toRadians = Tide.toRadians(deltaTime, TimeUnit.MILLISECONDS);
                double derivate = deltaDepth/toRadians;
                data.addRow(this.time+deltaTime/2, derivate);
            }
            this.time = time;
            this.depth = depth;
        }
        public long getTime()
        {
            return time;
        }

        public double getDepth()
        {
            return depth;
        }

    }
}
