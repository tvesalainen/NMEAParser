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

import java.awt.Color;
import static java.awt.Color.*;
import java.io.IOException;
import java.io.Serializable;
import static java.lang.Math.*;
import java.lang.invoke.MethodHandles;
import java.nio.channels.GatheringByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.function.IntPredicate;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.code.Property;
import org.vesalainen.io.IO;
import org.vesalainen.math.Circle;
import org.vesalainen.math.LevenbergMarquardt;
import org.vesalainen.math.Point;
import org.vesalainen.math.Polygon;
import org.vesalainen.math.SimpleLine;
import org.vesalainen.math.SimplePoint;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.navi.AnchorWatch;
import org.vesalainen.navi.AnchorWatch.Watcher;
import org.vesalainen.navi.AngleRange;
import org.vesalainen.navi.Chain;
import org.vesalainen.navi.LocalLongitude;
import org.vesalainen.navi.Navis;
import static org.vesalainen.navi.Navis.*;
import org.vesalainen.navi.SafeSector;
import org.vesalainen.nmea.jaxb.router.AnchorManagerType;
import static org.vesalainen.nmea.processor.AbstractChainedState.Action.*;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.ui.Direction;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AnchorManager extends AbstractProcessorTask
{
    private static final String ANCHOR_WATCH_NAME = "anchorWatch";
    private static final String ANCHOR_WATCH_FILENAME = ANCHOR_WATCH_NAME+".ser";
    private static final double DEFAULT_MAX_FAIRLEAD_TENSION = 2000;    // N
    private static final float MAX_SPEED = 1.0F;
    private static final float MIN_WIND = 10F;
    private static final float MIN_WIND_DIFF = 3F;
    private static final float MIN_RANGE = 15F;
    private static final int POINTS_SIZE = 1024;
    private static final long REFRESH_PERIOD = 60000;
    private @Property Clock clock;
    private @Property double latitude;
    private @Property double longitude;
    private @Property float speedOverGround;
    private @Property float trueHeading;
    private @Property float trackMadeGood;
    private @Property float relativeWindAngle;
    private @Property float relativeWindSpeed;
    private @Property float depthOfWater;

    private final Processor processor;
    private final GatheringByteChannel channel;
    private final double maxFairleadTension;
    private final Chain chain;
    private final long anchorWeight;
    private final long chainDiameter;
    private final long maxChainLength;
    
    private long timestamp;
    private double chainLength;
    private double horizontalScope;
    private AnchorWatch anchorWatch;
    private Path path;
    private NMEAManager nmeaManager = new NMEAManager();
    private final double maxDepth;
    private final DepthFilter depthFilter;
    
    public AnchorManager(Processor processor, GatheringByteChannel channel, AnchorManagerType type, CachedScheduledThreadPool executor) throws IOException
    {
        super(MethodHandles.lookup(), 20, MINUTES);
        this.processor = processor;
        this.channel = channel;
        this.anchorWeight = type.getAnchorWeight();
        this.chainDiameter = type.getChainDiameter();
        this.maxChainLength = type.getMaxChainLength();
        this.chain = new Chain(chainDiameter, maxChainLength);
        if (type.getMaxFairleadTension() != null)
        {
            maxFairleadTension = type.getMaxFairleadTension().doubleValue();
        }
        else
        {
            maxFairleadTension = DEFAULT_MAX_FAIRLEAD_TENSION;
        }
        this.maxDepth = chain.maximalDepth(maxFairleadTension, maxChainLength);
        this.depthFilter = new DepthFilter();
    }

    @Override
    public void stop()
    {
        if (anchorWatch != null)
        {
            try
            {
                IO.serialize(anchorWatch, path);
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "storing %s", path);
            }
        }
    }

    private void startAnchoring()
    {
        config("start anchoring");
        anchorWatch = new AnchorWatch();
        anchorWatch.addWatcher(nmeaManager);
        anchorWatch.setChainLength((int) horizontalScope);
        processor.setData(ANCHOR_WATCH_NAME, anchorWatch);
    }

    private void stopAnchoring()
    {
        nmeaManager.stop();
        processor.setData(ANCHOR_WATCH_NAME, null);
        anchorWatch = null;
        try
        {
            if (path != null)
            {
                Files.deleteIfExists(path);
            }
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "deleting%s", path);
        }
        config("stop anchoring");
    }

    @Override
    public void commitTask(String reason, Collection<String> updatedProperties)
    {
        depthFilter.input(updatedProperties);
    }

    private class DepthFilter extends AbstractChainedState<Collection<String>,String>
    {

        @Override
        protected Action test(Collection<String> updatedProperties)
        {
            if (updatedProperties.contains("depthOfWater"))
            {
                if (depthOfWater < maxDepth)
                {
                    chainLength = chain.chainLength(maxFairleadTension, depthOfWater);
                    horizontalScope = chain.horizontalScopeForChain(maxFairleadTension, depthOfWater, chainLength);
                    return FORWARD;
                }
                else
                {
                    reason = String.format("depthOfWater(%f) > %f", depthOfWater, maxDepth);
                    return FAIL;
                }
            }
            return NEUTRAL;
        }

        @Override
        protected boolean hasNext()
        {
            return true;
        }

        @Override
        protected AbstractChainedState<Collection<String>,String> createNext()
        {
            return new SpeedFilter();
        }
        
    }
    private class SpeedFilter extends AbstractChainedState<Collection<String>,String>
    {

        @Override
        protected Action test(Collection<String> updatedProperties)
        {
            if (updatedProperties.contains("speedOverGround"))
            {
                if (speedOverGround < MAX_SPEED)
                {
                    return FORWARD;
                }
                else
                {
                    reason = String.format("speedOverGround(%f) > %f", speedOverGround, MAX_SPEED);
                    return FAIL;
                }
            }
            return NEUTRAL;
        }

        @Override
        protected boolean hasNext()
        {
            return true;
        }

        @Override
        protected AbstractChainedState<Collection<String>,String> createNext()
        {
            return new MoveFilter();
        }
        
    }
    private class MoveFilter extends AbstractChainedState<Collection<String>,String>
    {

        private final double lat;
        private final double lon;

        public MoveFilter()
        {
            this.lat = latitude;
            this.lon = longitude;
        }

        @Override
        protected Action test(Collection<String> updatedProperties)
        {
            if (updatedProperties.contains("latitude"))
            {
                if (Navis.distance(latitude, longitude, lat, lon) < METER.convertTo(2*horizontalScope, NAUTICAL_MILE) )
                {
                    return FORWARD;
                }
                else
                {
                    reason = String.format("distance > %f", METER.convertTo(2*horizontalScope, NAUTICAL_MILE));
                    return FAIL;
                }
            }
            return NEUTRAL;
        }

        @Override
        protected boolean hasNext()
        {
            return true;
        }

        @Override
        protected AbstractChainedState<Collection<String>,String> createNext()
        {
            return new WindManager();
        }
        
    }
    private class WindManager extends AbstractChainedState<Collection<String>,String>
    {
        private float previous = 180;
        private float hdg;
        private AngleRange range = new AngleRange();
        private DoubleMatrix data;
        private LocalLongitude localLongitude;
        private int count;
        private AnchorEstimator estimator;

        @Override
        protected Action test(Collection<String> updatedProperties)
        {
            if (updatedProperties.contains("relativeWindAngle"))
            {
                update();
            }
            return NEUTRAL;
        }
        private void update()
        {
            if (
                    (relativeWindAngle < 30 && previous > 330 ||
                    relativeWindAngle > 330 && previous < 30) &&
                    relativeWindSpeed > MIN_WIND)
            {
                boolean newHdg = range.add(trueHeading);
                if (abs(angleDiff(trueHeading, hdg)) > MIN_WIND_DIFF || newHdg)
                {
                    add();
                    if (range.getRange() > MIN_RANGE)
                    {
                        if (estimator == null)
                        {
                            estimator = tryInstance();
                        }
                        if (estimator != null)
                        {
                            estimator.update();
                        }
                    }
                    hdg = trueHeading;
                }
            }
            previous = relativeWindAngle;
        }

        private void add()
        {
            if (data == null)
            {
                data = new DoubleMatrix(POINTS_SIZE, 6);
                data.reshape(0, 6);
                localLongitude = LocalLongitude.getInstance(longitude, latitude);
            }
            if (count < POINTS_SIZE)
            {
                data.addRow(
                        longitude,
                        latitude,
                        localLongitude.getInternal(longitude),
                        relativeWindSpeed,
                        Navis.degreesToCartesian(trueHeading),
                        depthOfWater
                );
            }
            else
            {
                data.setRow(count % POINTS_SIZE,
                        longitude,
                        latitude,
                        localLongitude.getInternal(longitude),
                        relativeWindSpeed,
                        Navis.degreesToCartesian(trueHeading),
                        depthOfWater
                );
            }
            count++;
        }

        private AnchorEstimator tryInstance()
        {
            // scope estimate
            double estimatedChainLength = chain.chainLength(maxFairleadTension, depthOfWater);
            double maxScope = Chain.maximalScope(depthOfWater, estimatedChainLength);
            double minScope = Chain.minimalScope(depthOfWater, estimatedChainLength);
            // estimated anchor position
            SimpleLine l1 = new SimpleLine();
            SimpleLine l2 = new SimpleLine();
            SimplePoint p0 = new SimplePoint();
            int len = data.rows()-1;
            for (int i=0;i<len;i++)
            {
                double x1 = data.get(i, 2);
                double y1 = data.get(i, 1);
                double r1 = data.get(i, 4);
                l1.setFromAngle(r1, x1, y1);
                //plotter.drawLine(x1, y1, r1, METER.convertTo(5, NAUTICAL_DEGREE));
                int j=data.rows()-1;
                double x2 = data.get(j, 2);
                double y2 = data.get(j, 1);
                double r2 = data.get(j, 4);
                l2.setFromAngle(r2, x2, y2);
                Point pc = SimpleLine.crossPoint(l1, l2, p0);
                if (pc != null)
                {
                    double distance = SimplePoint.distance(x1, y1, pc.getX(), pc.getY());
                    double meters = NAUTICAL_DEGREE.convertTo(distance, METER);
                    double angle = SimplePoint.angle(x1, y1, pc.getX(), pc.getY());
                    if (
                            meters > minScope && 
                            meters < maxScope &&
                            (near(angle, r1) || near(angle, r2))
                            )
                    {
                        DoubleMatrix params = new DoubleMatrix(4, 1);
                        double force = chain.forceForScope(meters, depthOfWater, estimatedChainLength);
                        double w = data.get(j, 3);
                        double coef = force/(w*w);
                        params.set(0, 0, pc.getX());
                        params.set(1, 0, pc.getY());
                        params.set(2, 0, 0.06);//coef);
                        params.set(3, 0, estimatedChainLength);
                        info("AnchorEstimate(%f, %f, %f, %f)", params.get(0, 0), params.get(1, 0), params.get(2, 0), params.get(3, 0));
                        return new AnchorEstimator(localLongitude, depthOfWater, data, params);
                    }
                }
            }
            return null;
        }

        @Override
        protected void failed(String reason)
        {
            info("anchoring stopped because %s", reason);
            if (estimator != null)
            {
                estimator.plot();
            }
        }
        
        private boolean near(double angle, double r1)
        {
            return abs(angle-r1) < 1.0;
        }
        
    }
    public class AnchorEstimator
    {
        private DoubleMatrix data;
        private DoubleMatrix coordinates;
        private DoubleMatrix internPoints;
        private DoubleMatrix centerParam;
        private DoubleMatrix chainParam;
        private DoubleMatrix coefParam;
        private DoubleMatrix wind;
        private LocalLongitude localLongitude;
        private final DoubleMatrix params;
        private final DoubleMatrix radius = new DoubleMatrix(1, 1);
        private final DoubleMatrix scope = new DoubleMatrix(1, 1);
        private final DoubleMatrix centers = new DoubleMatrix(0, 2);
        private final LevenbergMarquardt scopeCoefSolver = new LevenbergMarquardt(this::computeScopeCoef, null);
        private final LevenbergMarquardt scopeLengthSolver = new LevenbergMarquardt(this::computeScopeLength, null);
        private final LevenbergMarquardt circleSolver = new LevenbergMarquardt(this::computeRadius, null);
        private final double depth;
        private int goodWindRows = 1;
        private final DoubleMatrix heading;

        public AnchorEstimator(LocalLongitude localLongitude, double depth, DoubleMatrix data, DoubleMatrix params)
        {
            this.localLongitude = localLongitude;
            this.depth = depth;
            this.data = data;
            this.params = params;
            this.internPoints = data.getSparse(-1, 2, 2, 1);
            this.coordinates = data.getSub(0, 0, -1, 2);
            this.wind = data.getSub(0, 3, -1, 1);
            this.heading = data.getSub(0, 4, -1, 1);
            this.centerParam = params.getSub(0, 0, 2, 1);
            this.chainParam = params.getSub(3, 0, 1, 1);
            this.coefParam = params.getSub(2, 0, 1, 1);
            centers.addRow(localLongitude.getExternal(centerParam.get(0, 0)), centerParam.get(1, 0));
        }
        private void update()
        {
            if (scope.rows() != data.rows())
            {
                radius.reshape(data.rows(), 1);
                scope.reshape(data.rows(), 1);
            }
            IntPredicate pred = (r)->wind.get(r, 0)>10;
            /*
            DoubleMatrix goodWind = wind.getConditionalRows(pred);
            if (goodWind.rows() > goodWindRows)
            {
                goodWindRows = goodWind.rows();
                DoubleMatrix windPoints = internPoints.getConditionalRows(pred);
                if (radius.rows() != goodWind.rows())
                {
                    radius.reshape(goodWind.rows(), 1);
                }
                computeRadius(centerParam, windPoints, radius);
                if (scopeLengthSolver.optimize(chainParam, goodWind, radius))
                {
                    chainParam.set(scopeLengthSolver.getParameters());
                    if (chainParam.get(0, 0) > 80)
                    {
                        System.err.println();
                    }
                }
                if (scopeCoefSolver.optimize(coefParam, goodWind, radius))
                {
                    coefParam.set(scopeCoefSolver.getParameters());
                }
            }
            */
            computeScopeCoef(coefParam, wind, scope);
            if (circleSolver.optimize(centerParam, internPoints, scope))
            {
                centerParam.set(circleSolver.getParameters());
                centers.addRow(localLongitude.getExternal(centerParam.get(0, 0)), centerParam.get(1, 0));
            }
        }

        public void computeRadius(DoubleMatrix param, DoubleMatrix x, DoubleMatrix y)
        {
            double xx = param.get(0, 0);
            double yy = param.get(1, 0);
            int len = x.rows();
            for (int row=0;row<len;row++)
            {
                double px = x.get(row, 0);
                double py = x.get(row, 1);
                double di = NAUTICAL_DEGREE.convertTo(hypot(xx-px, yy-py), METER);
                y.set(row, 0, di);
            }
        }
        public void computeScopeCoef(DoubleMatrix param, DoubleMatrix x, DoubleMatrix y)
        {
            double co = param.get(0, 0);
            double s = chainParam.get(0, 0);
            int len = x.rows();
            for (int row=0;row<len;row++)
            {
                double wi = x.get(row, 0);
                double f = co*wi*wi;
                double scope = chain.horizontalScopeForChain(f, depth, s);
                y.set(row, 0, scope);
            }
        }
        public void computeScopeLength(DoubleMatrix param, DoubleMatrix x, DoubleMatrix y)
        {
            double co = coefParam.get(0, 0);
            double s = param.get(0, 0);
            int len = x.rows();
            for (int row=0;row<len;row++)
            {
                double wi = x.get(row, 0);
                double f = co*wi*wi;
                double scope = chain.horizontalScopeForChain(f, depth, s);
                y.set(row, 0, scope);
            }
        }

        private void plot()
        {
            info("AnchorEstimate(%f, %f, %f, %f)=%f", params.get(0, 0), params.get(1, 0), params.get(2, 0), params.get(3, 0), circleSolver.getFinalCost());
            ZonedDateTime zdt = ZonedDateTime.now(clock);
            Plot p = new Plot("c:\\temp\\"+zdt.toString().replace(':', '-')+".png");
            drawPoints(p);
            p.setColor(BLACK);
            p.drawLines(centers);
            p.drawCross(localLongitude.getExternal(centerParam.get(0, 0)), centerParam.get(1, 0));
            p.drawCircle(localLongitude.getExternal(centerParam.get(0, 0)), centerParam.get(1, 0), METER.convertTo(horizontalScope, NAUTICAL_DEGREE));
            p.drawTitle(Direction.TOP, String.format("coef=%f.1, s=%f.1, d=%f.1 cost=%f.1", params.get(2, 0), params.get(3, 0), depth, circleSolver.getFinalCost()));
            try
            {
                p.plot();
            }
            catch (IOException ex)
            {
                Logger.getLogger(AnchorManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void drawPoints(Plot p)
        {
            double r = METER.convertTo(horizontalScope, NAUTICAL_DEGREE);
            int rows = coordinates.rows();
            for (int ii=0;ii<rows;ii++)
            {
                double wi = wind.get(ii, 0);
                Color hsb = Color.getHSBColor((float) (wi/30), 1, 1);
                p.setColor(hsb);
                p.drawPlus(coordinates.get(ii, 0), coordinates.get(ii, 1));
                p.drawLineTo(coordinates.get(ii, 0), coordinates.get(ii, 1), heading.get(ii, 0), METER.convertTo(scope.get(ii, 0), NAUTICAL_DEGREE));
            }
        }

    }
    private class NMEAManager implements Watcher, Serializable
    {
        private NMEASentence tll;
        private long next;
        private Circle estimated;
        
        public void refresh()
        {
            if (timestamp > next)
            {
                transmit();
            }
        }
        private void transmit()
        {
            if (tll != null)
            {
                try
                {
                    fine("send %s", tll);
                    tll.writeTo(channel);
                }
                catch (IOException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
                finally
                {
                    next = timestamp + REFRESH_PERIOD;
                }
            }
        }
        public void stop()
        {
            config("stop TLL");
            if (estimated != null)
            {
                tll = NMEASentence.tll(0, estimated.getY(), estimated.getX(), "Anchor", clock, 'L', "");
                transmit();
            }
            tll = null;
        }

        @Override
        public void alarm(double distance)
        {
            warning("Anchor alarm %f meters", distance);
        }

        @Override
        public void location(double x, double y, long time, double accuracy, double speed)
        {
            refresh();
        }

        @Override
        public void area(Polygon area)
        {
        }

        @Override
        public void outer(Polygon path)
        {
        }

        @Override
        public void estimated(Circle estimated)
        {
            char status = tll == null ? 'Q' : 'T';
            tll = NMEASentence.tll(0, estimated.getY(), estimated.getX(), "Anchor", clock, status, "");
            transmit();
            this.estimated = estimated;
        }

        @Override
        public void safeSector(SafeSector safe)
        {
        }

        @Override
        public void suggestNextUpdateIn(double seconds, double meters)
        {
        }
    }
}
