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

import static java.lang.Math.*;
import java.time.Clock;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongToDoubleFunction;
import org.vesalainen.math.CosineFitter;
import org.vesalainen.math.MathFunction;
import org.vesalainen.math.UnitType;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingSlope;
import org.vesalainen.navi.BoatPosition;
import org.vesalainen.navi.CoordinateMap;
import org.vesalainen.navi.Tide;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeabedSurveyor
{
    private static final LongToDoubleFunction TIME_TO_RAD = (t)->PI*2*t/Tide.PERIOD;
    private final CoordinateMap<Square> map;
    private final DoubleBinaryOperator lonPos;
    private final DoubleBinaryOperator latPos;
    private final DoubleMatrix data;
    private final CosineFitter cosineFitter = new CosineFitter();
    private final Clock clock;
    private LongToDoubleFunction tideFunc;

    public SeabedSurveyor(Clock clock, double latitude, double boxSize, UnitType unit, BoatPosition gpsPosition, BoatPosition depthSounderPosition)
    {
        this.clock = clock;
        this.map = new CoordinateMap(latitude, boxSize, unit, Square::new);
        this.lonPos = gpsPosition.longitudeAtOperator(depthSounderPosition, latitude);
        this.latPos = gpsPosition.latitudeAtOperator(depthSounderPosition);
        this.data = new DoubleMatrix(1024,2);
        data.reshape(0, 2);
    }
    
    public void update(double longitude, double latitude, double depth, double heading)
    {
        Square square = map.getOrCreate(lonPos.applyAsDouble(longitude, heading), latPos.applyAsDouble(latitude, heading));
        square.setAndDerivate(depth);
    }

    public LongToDoubleFunction getDepthAt(double longitude, double latitude)
    {
        Square square = map.get(longitude, latitude);
        if (square != null)
        {
            long sqTime = square.getTime();
            double sqTide = tide(sqTime);
            double depth = square.getDepth();
            double stdDepth = depth-sqTide;
            if (tideFunc != null)
            {
                return (t)->stdDepth - tideFunc.applyAsDouble(t);
            }
            else
            {
                return (t)->stdDepth;
            }
        }
        return null;
    }
    private double tide(long time)
    {
        if (tideFunc != null)
        {
            return tideFunc.applyAsDouble(time);
        }
        return 0;
    }
    private class Square
    {
        private long time;
        private double depth;
        private DoubleTimeoutSlidingSlope sloper = new DoubleTimeoutSlidingSlope(clock::millis, 1000, Tide.PERIOD/18, TIME_TO_RAD);

        public void setAndDerivate(double depth)
        {
            sloper.accept(depth);
            if (sloper.fullness() > 99)
            {
                double slope = sloper.slope();
                cosineFitter.addPoints(TIME_TO_RAD.applyAsDouble(sloper.meanTime()), slope);
                cosineFitter.fit();
                MathFunction ader = cosineFitter.getAntiderivative();
                tideFunc = (t)->ader.applyAsDouble(TIME_TO_RAD.applyAsDouble((long) t));
                sloper.clear();
            }
            this.time = clock.millis();
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
