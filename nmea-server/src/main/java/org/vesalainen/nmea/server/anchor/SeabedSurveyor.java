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
package org.vesalainen.nmea.server.anchor;

import static java.lang.Math.abs;
import java.time.Clock;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongToDoubleFunction;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingSlope;
import org.vesalainen.navi.BoatPosition;
import org.vesalainen.navi.CoordinateMap;
import org.vesalainen.navi.Tide;
import org.vesalainen.navi.TideFitter;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeabedSurveyor extends JavaLogging
{
    private final PropertySetter out;
    private final CoordinateMap<Square> squareMap;
    private final DoubleBinaryOperator lonPos;
    private final DoubleBinaryOperator latPos;
    private final TideFitter tideFitter;
    private final Clock clock;
    private final double boxSize;
    private double depthSum;
    private long depthCount;
    private int squareSeq;

    public SeabedSurveyor(PropertySetter out, Clock clock, double latitude, double boxSize, UnitType unit, BoatPosition gpsPosition, BoatPosition depthSounderPosition)
    {
        super(SeabedSurveyor.class);
        this.out = out;
        this.clock = clock;
        this.boxSize = unit.convertTo(boxSize, NAUTICAL_DEGREE);
        this.squareMap = new CoordinateMap(latitude, boxSize, unit, Square::new);
        this.lonPos = gpsPosition.longitudeAtOperator(depthSounderPosition, latitude);
        this.latPos = gpsPosition.latitudeAtOperator(depthSounderPosition);
        this.tideFitter = new TideFitter(clock::millis);
    }
    
    public void update(double longitude, double latitude, double depth, double heading)
    {
        Square square = squareMap.getOrCreate(lonPos.applyAsDouble(longitude, heading), latPos.applyAsDouble(latitude, heading));
        if (square.setAndDerivate(depth))
        {
            info("coef=%f phase=%f pts=%d cost=%f\n%s", 
                    tideFitter.getCoefficient(),
                    tideFitter.getPhase(),
                    tideFitter.getPointCount(),
                    tideFitter.getFinalCost(),
                    tideFitter.getPoints()
            );
        }
        depthSum += depth;
        depthCount++;
        if (tideFitter.isValid())
        {
            out.set("tideRange", abs(tideFitter.getCoefficient()*2));
            out.set("tidePhase", tideFitter.getPhaseInDegrees());
            out.set("tideCertainty", tideCertainty());
        }
    }
    private double tideCertainty()
    {
        double finalCost = tideFitter.getFinalCost();
        if (finalCost > 10)
        {
            return 0;
        }
        else
        {
            return 1.0-Math.log10(finalCost+1);
        }
    }
    public void forEachSquare(Consumer<Square> act)
    {
        squareMap.forEachCoordinate((x,y,s)->act.accept(s));
    }
    
    public double getMeanDepth()
    {
        return depthSum/depthCount;
    }
    public LongToDoubleFunction getDepthAt(double longitude, double latitude)
    {
        Square square = squareMap.nearest(longitude, latitude);
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
    public int getSquareCount()
    {
        return squareMap.size();
    }
    public double getTide()
    {
        return tideFitter.getTide();
    }

    public double getDerivative()
    {
        return tideFitter.getDerivative();
    }
    
    private double tide(long time)
    {
        if (tideFitter.isValid())
        {
            return tideFitter.getTide(time);
        }
        return Double.NaN;
    }

    public double getCoefficient()
    {
        return tideFitter.getCoefficient();
    }

    public double getPhase()
    {
        return tideFitter.getPhase();
    }

    public int getPointCount()
    {
        return tideFitter.getPointCount();
    }

    public double getFinalCost()
    {
        return tideFitter.getFinalCost();
    }

    public boolean isValid()
    {
        return tideFitter.isValid();
    }
    
    public String getSquares()
    {
        return squareMap.toString();
    }

    public double getPhaseInDegrees()
    {
        return tideFitter.getPhaseInDegrees();
    }
    
    public class Square
    {
        private final int id;
        private final double longitude;
        private final double latitude;
        private long time;
        private double depth;
        private DoubleTimeoutSlidingSlope sloper = new DoubleTimeoutSlidingSlope(clock::millis, 1000, Tide.PERIOD/18, Tide.TIME_TO_RAD);

        public Square(double longitude, double latitude)
        {
            this.id = squareSeq++;
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public boolean setAndDerivate(double depth)
        {
            boolean fit = false;
            sloper.accept(depth);
            if (sloper.fullness() > 99)
            {
                double slope = sloper.slope();
                tideFitter.add(sloper.meanTime(), slope);
                tideFitter.fit();
                sloper.clear();
                fit = true;
            }
            this.time = clock.millis();
            this.depth = depth;
            out.set("seabedSquare", this);
            return fit;
        }

        public int getId()
        {
            return id;
        }

        public double getLongitude()
        {
            return longitude;
        }

        public double getLatitude()
        {
            return latitude;
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
            if (tideFitter.isValid())
            {
                return depth-tide(time);
            }
            else
            {
                return depth;
            }
        }

        @Override
        public String toString()
        {
            return String.format("%.1fm", depth);
        }
        
    }
}
