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
import static java.lang.Math.*;
import java.lang.invoke.MethodHandles;
import java.nio.channels.GatheringByteChannel;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collection;
import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntPredicate;
import java.util.function.LongToDoubleFunction;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.code.Property;
import org.vesalainen.io.IO;
import org.vesalainen.lang.Primitives;
import org.vesalainen.math.LevenbergMarquardt;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.math.matrix.ReadableDoubleMatrix;
import org.vesalainen.navi.AnchorWatch;
import org.vesalainen.navi.BoatPosition;
import org.vesalainen.navi.Chain;
import org.vesalainen.navi.LocalLongitude;
import org.vesalainen.navi.LocationCenter;
import org.vesalainen.navi.Navis;
import org.vesalainen.navi.SimpleBoatPosition;
import org.vesalainen.nmea.jaxb.router.AnchorManagerType;
import org.vesalainen.nmea.jaxb.router.BoatDataType;
import org.vesalainen.nmea.jaxb.router.BoatPositionType;
import org.vesalainen.nmea.jaxb.router.DepthSounderPositionType;
import org.vesalainen.nmea.jaxb.router.GpsPositionType;
import static org.vesalainen.nmea.processor.AbstractChainedState.Action.*;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.ui.AbstractPlotter.Polyline;
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
    private static final float GOOD_WIND = 15F;
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
    private BoatPosition gpsPosition;
    private BoatPosition depthSounderPosition;
    private BoatPosition bowPosition;
    
    private long timestamp;
    private double chainLength;
    private double horizontalScope;
    private AnchorWatch anchorWatch;
    private Path path;
    private NMEAManager nmeaManager = new NMEAManager();
    private final double maxDepth;
    private final DepthFilter depthFilter;
    private final double boatLength;
    private final double boatBeam;
    private final double coef;
    
    public AnchorManager(Processor processor, GatheringByteChannel channel, BoatDataType boat, AnchorManagerType type, CachedScheduledThreadPool executor) throws IOException
    {
        super(MethodHandles.lookup(), 20, MINUTES);
        this.processor = processor;
        this.channel = channel;
        this.anchorWeight = Primitives.getLong(boat.getAnchorWeight());
        this.chainDiameter = Primitives.getLong(boat.getChainDiameter());
        this.maxChainLength = Primitives.getLong(boat.getMaxChainLength());
        this.boatLength = Primitives.getDouble(boat.getLength());
        this.boatBeam = Primitives.getDouble(boat.getBeam());
        this.chain = new Chain(chainDiameter, maxChainLength);
        this.coef = Primitives.getDouble(boat.getCrossSectionCoefficient(), 0.0089*Math.pow(boatLength, 1.66));
        this.maxFairleadTension = coef*60*60;  // http://alain.fraysse.free.fr/sail/rode/forces/forces.htm
        this.maxDepth = chain.maximalDepth(maxFairleadTension, maxChainLength);
        for (BoatPositionType pos : boat.getGpsPositionOrDepthSounderPosition())
        {
            if (pos instanceof GpsPositionType)
            {
                gpsPosition = createBoatPosition(pos);
            }
            if (pos instanceof DepthSounderPositionType)
            {
                depthSounderPosition = createBoatPosition(pos);
            }
        }
        this.bowPosition = new SimpleBoatPosition(boatBeam/2, boatBeam/2, 0, boatLength);
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

    private BoatPosition createBoatPosition(BoatPositionType pos)
    {
        return new SimpleBoatPosition(
            Primitives.getDouble(pos.getToSb(), ()->boatBeam-Primitives.getDouble(pos.getToPort())), 
            Primitives.getDouble(pos.getToPort(), ()->boatBeam-Primitives.getDouble(pos.getToSb())), 
            Primitives.getDouble(pos.getToBow(), ()->boatLength-Primitives.getDouble(pos.getToStern())), 
            Primitives.getDouble(pos.getToStern(), ()->boatLength-Primitives.getDouble(pos.getToBow())));
    }

    @Override
    public void commitTask(String reason, Collection<String> updatedProperties)
    {
        depthFilter.input(updatedProperties);
    }

    public class DepthFilter extends AbstractChainedState<Collection<String>,String>
    {

        public DepthFilter()
        {
            super("DepthFilter");
        }

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
    public class SpeedFilter extends AbstractChainedState<Collection<String>,String>
    {

        public SpeedFilter()
        {
            super("SpeedFilter");
        }

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
    public class MoveFilter extends AbstractChainedState<Collection<String>,String>
    {

        private final double lat;
        private final double lon;

        public MoveFilter()
        {
            super("MoveFilter");
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
    public class WindManager extends AbstractChainedState<Collection<String>,String>
    {
        private float previous = 180;
        private DoubleMatrix data = new DoubleMatrix(0, 7);
        private DoubleMatrix params = new DoubleMatrix(4, 1);
        private LocalLongitude localLongitude = LocalLongitude.getInstance(longitude, latitude);
        private int count;
        private AnchorEstimator estimator;
        private final DoubleBinaryOperator bowLatitude;
        private final DoubleBinaryOperator bowLongitude;
        private SeabedSurveyor seabedSurveyor;
        private final LocationCenter center = new LocationCenter();
        private final NMEAManager nmeaManager = new NMEAManager();
        private double bowLat;
        private double bowLon;
        private Plot p;
        private Polyline path;
        private Polyline centers;

        public WindManager()
        {
            super("SeabedSurveyor");
            this.bowLatitude = gpsPosition.latitudeAtOperator(bowPosition);
            this.bowLongitude = gpsPosition.longitudeAtOperator(bowPosition, latitude);
            this.seabedSurveyor = new SeabedSurveyor(clock, latitude, 3, METER, gpsPosition, depthSounderPosition);
            if (false)
            {
                ZonedDateTime zdt = ZonedDateTime.now(clock);
                this.p = new Plot("c:\\temp\\"+zdt.toString().replace(':', '-')+".png");
                this.path = p.polyline(Color.LIGHT_GRAY);
                p.drawPolyline(path);
                this.centers = p.polyline(Color.RED);
                p.drawPolyline(path);
            }
            params.set(2, 0, coef);
            fine("anchoring started");
        }

        @Override
        protected Action test(Collection<String> updatedProperties)
        {
            if (path != null) path.lineTo(longitude, latitude);
            if (updatedProperties.contains("relativeWindAngle"))
            {
                update();
            }
            if (updatedProperties.contains("latitude"))
            {
                seabedSurveyor.update(longitude, latitude, depthOfWater, trueHeading);
            }
            return NEUTRAL;
        }
        private void update()
        {
            bowLat = bowLatitude.applyAsDouble(latitude, trueHeading);
            bowLon = bowLongitude.applyAsDouble(longitude, trueHeading);
            if (
                    (relativeWindAngle < 30 && previous > 330 ||
                    relativeWindAngle > 330 && previous < 30)
                    )
                    
            {
                if (relativeWindSpeed > MIN_WIND)
                {
                    if (estimator == null)
                    {
                        estimator = new AnchorEstimator(localLongitude, seabedSurveyor, data, params);
                    }
                    add();
                    params.set(0, 0, localLongitude.getInternal(center.longitude()));
                    params.set(1, 0, center.latitude());
                    params.set(3, 0, chain.chainLength(maxFairleadTension, depthOfWater));
                    estimator.update();
                }
                else
                {
                    if (relativeWindSpeed > 0)
                    {
                        addUpwindCenter(bowLon, bowLat, relativeWindSpeed, trueHeading, depthOfWater, coef);
                    }
                    else
                    {
                        center.add(bowLon, bowLat);
                    }
                }
            }
            else
            {
                center.add(bowLon, bowLat);
            }
            previous = relativeWindAngle;
            if (estimator != null)
            {
                double lon = localLongitude.getExternal(params.get(0, 0));
                double lat = params.get(1, 0);
                nmeaManager.estimated(lon, lat);
                if (centers != null) centers.lineTo(lon, lat);
            }
            else
            {
                double lon = center.longitude();
                double lat = center.latitude();
                nmeaManager.estimated(lon, lat);
                if (centers != null) centers.lineTo(lon, lat);
            }
        }

        private void add()
        {
            if (data == null)
            {
                data = new DoubleMatrix(POINTS_SIZE, 7);
                data.reshape(0, 7);
                localLongitude = LocalLongitude.getInstance(longitude, latitude);
            }
            if (count < POINTS_SIZE)
            {
                data.addRow(
                        bowLon,
                        bowLat,
                        localLongitude.getInternal(bowLon),
                        relativeWindSpeed,
                        trueHeading,
                        depthOfWater,
                        clock.millis()
                );
            }
            else
            {
                data.setRow(count % POINTS_SIZE,
                        bowLon,
                        bowLat,
                        localLongitude.getInternal(bowLon),
                        relativeWindSpeed,
                        trueHeading,
                        depthOfWater,
                        clock.millis()
                );
            }
            count++;
        }

        private void addUpwindCenter(double lon, double lat, double wind, double heading, double depth, double coef)
        {
            double estimatedChainLength = chain.chainLength(maxFairleadTension, depth);
            double f = coef*wind*wind;
            double scope = chain.horizontalScopeForChain(f, depth, estimatedChainLength);
            double nm = METER.convertTo(scope, NAUTICAL_MILE);
            double dLat = Navis.deltaLatitude(nm, heading);
            double dLon = Navis.deltaLongitude(lat, nm, heading);
            center.add(lon + dLon, lat + dLat, 1 + wind*wind);
        }
        @Override
        protected void failed(String reason)
        {
            fine("anchoring stopped because %s", reason);
            nmeaManager.stop();
            if (estimator != null)
            {
                estimator.finalizeEstimate(p);
            }
            try
            {
                if (p != null)
                {
                    seabedSurveyor.draw(p);
                    p.plot();
                }
            }
            catch (IOException ex)
            {
                log(Level.SEVERE, ex, "%s", ex.getMessage());
            }
        }

        public double getMeanDepth()
        {
            return seabedSurveyor.getMeanDepth();
        }

        public double getCoefficient()
        {
            return seabedSurveyor.getCoefficient();
        }

        public double getPhase()
        {
            return seabedSurveyor.getPhase();
        }

        public int getSquareCount()
        {
            return seabedSurveyor.getSquareCount();
        }

        public int getPointCount()
        {
            return seabedSurveyor.getPointCount();
        }

        public double getFinalCost()
        {
            return seabedSurveyor.getFinalCost();
        }

        public double getTide()
        {
            return seabedSurveyor.getTide();
        }

        public double getDerivative()
        {
            return seabedSurveyor.getDerivative();
        }

        public boolean isValid()
        {
            return seabedSurveyor.isValid();
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
        private final LevenbergMarquardt scopeCoefSolver = new LevenbergMarquardt(this::computeScopeCoef, null);
        private final LevenbergMarquardt scopeLengthSolver = new LevenbergMarquardt(this::computeScopeLength, null);
        private final LevenbergMarquardt circleSolver = new LevenbergMarquardt(this::computeRadius, null);
        private final SeabedSurveyor seabedSurveyor;
        private final DoubleMatrix heading;
        private final DoubleMatrix time;

        public AnchorEstimator(LocalLongitude localLongitude, SeabedSurveyor seabedSurveyor, DoubleMatrix data, DoubleMatrix params)
        {
            this.localLongitude = localLongitude;
            this.seabedSurveyor = seabedSurveyor;
            this.data = data;
            this.params = params;
            this.internPoints = data.getSparse(-1, 2, 2, 1);
            this.coordinates = data.getSub(0, 0, -1, 2);
            this.wind = data.getSub(0, 3, -1, 1);
            this.heading = data.getSub(0, 4, -1, 1);
            this.time = data.getSub(0, 6, -1, 1);
            this.centerParam = params.getSub(0, 0, 2, 1);
            this.chainParam = params.getSub(3, 0, 1, 1);
            this.coefParam = params.getSub(2, 0, 1, 1);
        }
        private void update()
        {
            if (scope.rows() != data.rows())
            {
                radius.reshape(data.rows(), 1);
                scope.reshape(data.rows(), 1);
            }
            computeScopeCoef(coefParam, wind, scope);
            if (circleSolver.optimize(centerParam, internPoints, scope))
            {
                centerParam.set(circleSolver.getParameters());
            }
        }

        private void computeRadius(DoubleMatrix param, ReadableDoubleMatrix x, DoubleMatrix y)
        {
            double xx = param.get(0, 0);
            double yy = param.get(1, 0);
            int len = x.rows();
            for (int row=0;row<len;row++)
            {
                double px = x.get(row, 0);
                double py = x.get(row, 1);
                double di = NAUTICAL_DEGREE.convertTo(hypot(xx-px, yy-py), METER);
                if (Double.isFinite(di))
                {
                    y.set(row, 0, di);
                }
                else
                {
                    throw new IllegalArgumentException("nan");
                }
            }
        }
        private void computeScopeCoef(DoubleMatrix param, ReadableDoubleMatrix x, DoubleMatrix y)
        {
            LongToDoubleFunction depthFunc = seabedSurveyor.getDepthAt(localLongitude.getExternal(centerParam.get(0, 0)), centerParam.get(1, 0));
            double co = param.get(0, 0);
            double s = chainParam.get(0, 0);
            int len = x.rows();
            for (int row=0;row<len;row++)
            {
                long t = (long) time.get(row, 0);
                double depth = depthFunc.applyAsDouble(t);
                double wi = x.get(row, 0);
                double f = co*wi*wi;
                double scope = chain.horizontalScopeForChain(f, depth, s);
                y.set(row, 0, scope);
            }
        }
        private void computeScopeLength(DoubleMatrix param, ReadableDoubleMatrix x, DoubleMatrix y)
        {
            LongToDoubleFunction depthFunc = seabedSurveyor.getDepthAt(localLongitude.getExternal(centerParam.get(0, 0)), centerParam.get(1, 0));
            double co = coefParam.get(0, 0);
            double s = param.get(0, 0);
            int len = x.rows();
            for (int row=0;row<len;row++)
            {
                long t = (long) time.get(row, 0);
                double depth = depthFunc.applyAsDouble(t);
                double wi = x.get(row, 0);
                double f = co*wi*wi;
                double scope = chain.horizontalScopeForChain(f, depth, s);
                y.set(row, 0, scope);
            }
        }

        private void finalizeEstimate(Plot p)
        {
            finalCalc();
            fine("AnchorEstimate(%f, %f, %f, %f)=%f", params.get(0, 0), params.get(1, 0), params.get(2, 0), params.get(3, 0), circleSolver.getFinalCost());
            double meanDepth = seabedSurveyor.getMeanDepth();
            if (p != null)
            {
                drawPoints(p);
                p.setColor(BLACK);
                p.drawCross(localLongitude.getExternal(centerParam.get(0, 0)), centerParam.get(1, 0));
                p.drawCircle(localLongitude.getExternal(centerParam.get(0, 0)), centerParam.get(1, 0), METER.convertTo(horizontalScope, NAUTICAL_DEGREE));
                p.drawTitle(Direction.TOP, String.format("coef=%f.1, s=%f.1, d=%f.1 cost=%f.1", params.get(2, 0), params.get(3, 0), meanDepth, circleSolver.getFinalCost()));
            }
        }

        private void finalCalc()
        {
            IntPredicate pred = (r)->wind.get(r, 0)>GOOD_WIND;
            DoubleMatrix goodWind = wind.getConditionalRows(pred);
            if (!goodWind.isEmpty())
            {
                if (!goodWind.sameDimensions(radius))
                {
                    radius.reshape(goodWind.rows(), 1);
                }
                computeRadius(centerParam, internPoints, radius);
                if (scopeCoefSolver.optimize(coefParam, goodWind, radius))
                {
                    double newCoef = scopeCoefSolver.getParameters().get(0, 0);
                    info("calculated crossSectionCoefficient=%f (%f)", newCoef, coef);
                }
                if (scopeLengthSolver.optimize(chainParam, goodWind, radius))
                {
                    double newChain = scopeLengthSolver.getParameters().get(0, 0);
                    info("calculated actual chain length=%f (%f)", newChain, chainParam.get(0, 0));
                }
            }
        }
        private void drawPoints(Plot p)
        {
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
    private class NMEAManager
    {
        private NMEASentence tll;
        private long next;
        private double longitude = Double.NaN;
        private double latitude = Double.NaN;
        
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
                    //fine("send %s", tll);
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
            fine("stop TLL");
            if (Double.isFinite(latitude))
            {
                tll = NMEASentence.tll(0, latitude, longitude, "Anchor", clock, 'L', "");
                transmit();
            }
            tll = null;
        }

        public void estimated(double longitude, double latitude)
        {
            char status = tll == null ? 'Q' : 'T';
            tll = NMEASentence.tll(0, latitude, longitude, "Anchor", clock, status, "");
            transmit();
            this.longitude = longitude;
            this.latitude = latitude;
        }

    }
}
