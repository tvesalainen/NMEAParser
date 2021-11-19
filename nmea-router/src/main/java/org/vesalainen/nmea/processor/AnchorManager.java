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
import static java.util.logging.Level.SEVERE;
import org.vesalainen.code.Property;
import org.vesalainen.io.IO;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Polygon;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.AnchorWatch;
import org.vesalainen.navi.AnchorWatch.Watcher;
import org.vesalainen.navi.AngleRange;
import org.vesalainen.navi.Chain;
import org.vesalainen.navi.Navis;
import static org.vesalainen.navi.Navis.*;
import org.vesalainen.navi.SafeSector;
import org.vesalainen.nmea.jaxb.router.AnchorManagerType;
import static org.vesalainen.nmea.processor.AbstractChainedState.Action.*;
import org.vesalainen.parsers.nmea.NMEASentence;
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
    private static final float MIN_WIND = 2F;
    private static final float MIN_WIND_DIFF = 3F;
    private static final float MIN_RANGE = 15F;
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
        super(MethodHandles.lookup(), 2, MINUTES);
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
            boolean newHdg = range.add(trueHeading);
            if (
                    (relativeWindAngle < 30 && previous > 330 ||
                    relativeWindAngle > 330 && previous < 30) &&
                    relativeWindSpeed > MIN_WIND &&
                    (abs(angleDiff(trueHeading, hdg)) > MIN_WIND_DIFF || newHdg)
                    )
            {
                info("TURN %f %f", trueHeading, relativeWindSpeed);
                hdg = trueHeading;
            }
            previous = relativeWindAngle;
        }

        @Override
        protected boolean hasNext()
        {
            return false;
        }

        @Override
        protected AbstractChainedState<Collection<String>> createNext()
        {
            throw new UnsupportedOperationException("Not supported yet.");
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
