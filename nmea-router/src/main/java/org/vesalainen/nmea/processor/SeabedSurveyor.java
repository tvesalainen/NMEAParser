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

import java.awt.Color;
import java.time.Clock;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongToDoubleFunction;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingSlope;
import org.vesalainen.navi.BoatPosition;
import org.vesalainen.navi.CoordinateMap;
import org.vesalainen.navi.Tide;
import org.vesalainen.navi.TideFitter;
import org.vesalainen.ui.ChartPlotter;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeabedSurveyor extends JavaLogging
{
    private final CoordinateMap<Square> map;
    private final DoubleBinaryOperator lonPos;
    private final DoubleBinaryOperator latPos;
    private final TideFitter tideFitter;
    private final Clock clock;
    private final double boxSize;
    private double depthSum;
    private long depthCount;

    public SeabedSurveyor(Clock clock, double latitude, double boxSize, UnitType unit, BoatPosition gpsPosition, BoatPosition depthSounderPosition)
    {
        super(SeabedSurveyor.class);
        this.clock = clock;
        this.boxSize = unit.convertTo(boxSize, NAUTICAL_DEGREE);
        this.map = new CoordinateMap(latitude, boxSize, unit, Square::new);
        this.lonPos = gpsPosition.longitudeAtOperator(depthSounderPosition, latitude);
        this.latPos = gpsPosition.latitudeAtOperator(depthSounderPosition);
        this.tideFitter = new TideFitter(clock::millis);
    }
    
    public void update(double longitude, double latitude, double depth, double heading)
    {
        Square square = map.getOrCreate(lonPos.applyAsDouble(longitude, heading), latPos.applyAsDouble(latitude, heading));
        square.setAndDerivate(depth);
        depthSum += depth;
        depthCount++;
    }

    public double getMeanDepth()
    {
        return depthSum/depthCount;
    }
    public LongToDoubleFunction getDepthAt(double longitude, double latitude)
    {
        Square square = map.nearest(longitude, latitude);
        if (square != null)
        {
            double stdDepth = square.getStandardDepth();
            if (tideFitter.isValid())
            {
                return (t)->stdDepth - tideFitter.getTide(t);
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
        if (tideFitter.isValid())
        {
            return tideFitter.getTide(time);
        }
        return 0;
    }
    public void draw(ChartPlotter p)
    {
        info("tide a=%f b=%f cnt=%d cost=%f", tideFitter.getParamA(), tideFitter.getParamB(), tideFitter.getPointCount(), tideFitter.getFinalCost());
        map.forEachCoordinate((double lon, double lat, Square square)->
        {
            double dpt = square.getStandardDepth();
            p.setColor(Color.getHSBColor((float) (dpt/25), 1F, 1F));
            p.drawRectangle(lon, lat, boxSize, boxSize, false);
        });
    }
    private class Square
    {
        private long time;
        private double depth;
        private DoubleTimeoutSlidingSlope sloper = new DoubleTimeoutSlidingSlope(clock::millis, 1000, Tide.PERIOD/18, Tide.TIME_TO_RAD);

        public void setAndDerivate(double depth)
        {
            sloper.accept(depth);
            if (sloper.fullness() > 99)
            {
                double slope = sloper.slope();
                tideFitter.add(sloper.meanTime(), slope);
                tideFitter.fit();
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

        public double getStandardDepth()
        {
            return depth-tide(time);
        }
    }
}
