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
import java.io.IOException;
import java.io.Serializable;
import static java.lang.Math.*;
import java.lang.invoke.MethodHandles;
import java.nio.channels.GatheringByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.function.Supplier;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.code.Property;
import org.vesalainen.io.IO;
import org.vesalainen.math.Circle;
import org.vesalainen.math.LevenbergMarquardt;
import org.vesalainen.math.LevenbergMarquardt.Function;
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
import org.vesalainen.ui.Plotter;
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
    private static final float MAX_SPEED = 0.2F;
    private static final float MIN_WIND = 3F;
    private static final float MIN_WIND_DIFF = 3F;
    private static final float MIN_RANGE = 15F;
    private static final int POINTS_SIZE = 64;
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

    private class DepthFilter extends AbstractChainedState<Collection<String>>
    {

        @Override
        protected Action test(Collection<String> updatedProperties)
        {
            if (updatedProperties.contains("depthOfWater"))
            {
                if (depthOfWater < maxDepth)
                {
                    chainLength = chain.chainLength(maxFairleadTension, depthOfWater);
                    horizontalScope = chain.horizontalScope(maxFairleadTension, depthOfWater, chainLength);
                    return FORWARD;
                }
                else
                {
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
        protected AbstractChainedState<Collection<String>> createNext()
        {
            return new SpeedFilter();
        }
        
    }
    private class SpeedFilter extends AbstractChainedState<Collection<String>>
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
        protected AbstractChainedState<Collection<String>> createNext()
        {
            return new MoveFilter();
        }
        
    }
    private class MoveFilter extends AbstractChainedState<Collection<String>>
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
        protected AbstractChainedState<Collection<String>> createNext()
        {
            return new WindManager();
        }
        
    }
    private class WindManager extends AbstractChainedState<Collection<String>>
    {
        private float previous = 180;
        private float hdg;
        private AngleRange range = new AngleRange();
        private DoubleMatrix points;
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
                    info("TURN %f %f", trueHeading, relativeWindSpeed);
                    add();
                    if (range.getRange() > MIN_RANGE)
                    {
                        if (estimator == null)
                        {
                            estimator = tryInstance(points, localLongitude);
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
            if (points == null)
            {
                points = new DoubleMatrix(POINTS_SIZE, 4);
                points.reshape(0, 4);
                localLongitude = LocalLongitude.getInstance(longitude, latitude);
            }
            if (count < POINTS_SIZE)
            {
                points.addRow(
                        localLongitude.getInternal(longitude),
                        latitude,
                        relativeWindSpeed,
                        Navis.degreesToCartesian(trueHeading)
                );
            }
            else
            {
                points.setRow(count % POINTS_SIZE,
                        localLongitude.getInternal(longitude),
                        latitude,
                        relativeWindSpeed,
                        Navis.degreesToCartesian(trueHeading)
                );
            }
            count++;
        }

        private AnchorEstimator tryInstance(DoubleMatrix points, LocalLongitude localLongitude)
        {
            // scope estimate
            double estimatedChainLength = chain.chainLength(maxFairleadTension, depthOfWater);
            double maxScope = chain.horizontalScope(maxFairleadTension, depthOfWater, estimatedChainLength);
            double minScope = estimatedChainLength - depthOfWater;
            // estimated anchor position
            SimpleLine l1 = new SimpleLine();
            SimpleLine l2 = new SimpleLine();
            SimplePoint p0 = new SimplePoint();
            int len = points.rows()-1;
            for (int i=0;i<len;i++)
            {
                double x1 = points.get(i, 0);
                double y1 = points.get(i, 1);
                double r1 = points.get(i, 3);
                l1.setFromAngle(r1, x1, y1);
                //plotter.drawLine(x1, y1, r1, METER.convertTo(5, NAUTICAL_DEGREE));
                int j=points.rows()-1;
                double x2 = points.get(j, 0);
                double y2 = points.get(j, 1);
                double r2 = points.get(j, 3);
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
                        double w = points.get(j, 2);
                        double coef = force/(w*w);
                        params.set(0, 0, pc.getX());
                        params.set(1, 0, pc.getY());
                        params.set(2, 0, coef);
                        params.set(3, 0, estimatedChainLength);
                        info("AnchorEstimate(%f, %f, %f, %f)", params.get(0, 0), params.get(1, 0), params.get(2, 0), params.get(3, 0));
                        return new AnchorEstimator(localLongitude, depthOfWater, points, params);
                    }
                }
            }
            return null;
        }

        @Override
        protected void failed(Collection<String> input)
        {
            info("STOP");
        }
        
        private boolean near(double angle, double r1)
        {
            return abs(angle-r1) < 1.0;
        }
        
    }
    public class AnchorEstimator implements Function
    {
        private DoubleMatrix points;
        private LocalLongitude localLongitude;
        private Plotter plotter;
        private final DoubleMatrix params;
        private final DoubleMatrix zero = new DoubleMatrix(1, 1);
        private final LevenbergMarquardt levenbergMarquardt = new LevenbergMarquardt(this, null);
        private final double depth;

        public AnchorEstimator(LocalLongitude localLongitude, double depth, DoubleMatrix points, DoubleMatrix params)
        {
            this.localLongitude = localLongitude;
            this.depth = depth;
            this.points = points;
            this.params = params;
        }
        private void update()
        {
            if (zero.rows() != points.rows())
            {
                zero.reshape(points.rows(), 1);
            }
            if (levenbergMarquardt.optimize(params, points, zero))
            {
                params.set(levenbergMarquardt.getParameters());
                info("AnchorEstimate(%f, %f, %f, %f)", params.get(0, 0), params.get(1, 0), params.get(2, 0), params.get(3, 0));
            }
            else
            {
                throw new IllegalArgumentException("Fit failed");
            }
        }

        @Override
        public void compute(DoubleMatrix param, DoubleMatrix x, DoubleMatrix y)
        {
            double xx = param.get(0, 0);
            double yy = param.get(1, 0);
            double co = param.get(2, 0);
            double s = param.get(3, 0);
            int len = points.rows();
            for (int row=0;row<len;row++)
            {
                double px = x.get(row, 0);
                double py = x.get(row, 1);
                double wi = x.get(row, 2);
                double di = NAUTICAL_DEGREE.convertTo(hypot(xx-px, yy-py), METER);
                double f = co*wi*wi;
                double scope = chain.horizontalScopeForChain(f, depth, s);
                y.set(row, 0, scope-di);
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
