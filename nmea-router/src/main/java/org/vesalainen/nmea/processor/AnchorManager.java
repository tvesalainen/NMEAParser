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
import java.lang.invoke.MethodHandles;
import java.nio.channels.GatheringByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Collection;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.io.IO;
import org.vesalainen.math.AngleAverage;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Point;
import org.vesalainen.math.Polygon;
import org.vesalainen.navi.AnchorWatch;
import org.vesalainen.navi.AnchorWatch.Watcher;
import org.vesalainen.navi.Chain;
import org.vesalainen.navi.Navis;
import org.vesalainen.navi.SafeSector;
import org.vesalainen.nmea.jaxb.router.AnchorManagerType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AnchorManager extends AnnotatedPropertyStore implements Stoppable
{
    private static final String ANCHOR_WATCH_NAME = "anchorWatch";
    private static final String ANCHOR_WATCH_FILENAME = ANCHOR_WATCH_NAME+".ser";
    private static final double DEFAULT_MAX_FAIRLEAD_TENSION = 2000;    // N
    private static final long DEPTH_EXPIRES = 100000;    // milli seconds
    private static final float MAX_SPEED = 2F;
    private static final long REFRESH_PERIOD = 60000;
    @Property private Clock clock;
    @Property private double latitude;
    @Property private double longitude;
    @Property private float speedOverGround;
    @Property private float horizontalDilutionOfPrecision = 3;
    @Property private float depthOfWater;

    private final Processor processor;
    private final GatheringByteChannel channel;
    private final double maxFairleadTension;
    private final Chain chain;
    private final long anchorWeight;
    private final long chainDiameter;
    private final long maxChainLength;
    private final DepthManager depthManager = new DepthManager();
    
    private long timestamp;
    private double chainLength;
    private double horizontalScope;
    private boolean anchoring;
    private AnchoringArea area;
    private boolean depthOk;
    private boolean speedOk;
    private boolean areaOk;
    private AnchorWatch anchorWatch;
    private Path path;
    private NMEAManager nmeaManager = new NMEAManager();
    
    public AnchorManager(Processor processor, GatheringByteChannel channel, AnchorManagerType type, CachedScheduledThreadPool executor) throws IOException
    {
        super(MethodHandles.lookup());
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
        String directory = type.getDirectory();
        if (directory != null)
        {
            Path dir = Paths.get(directory);
            this.path = dir.resolve(ANCHOR_WATCH_FILENAME);
            if (Files.exists(path))
            {
                try
                {
                    anchorWatch = IO.deserialize(path);
                    anchorWatch.addWatcher(nmeaManager);
                    area = new AnchoringArea(anchorWatch.getCenter());
                }
                catch (ClassNotFoundException ex)
                {
                    throw new IOException(ex);
                }
            }
        }
    }

    @Override
    public void commit(String reason, Collection<String> updatedProperties)
    {
        timestamp = clock.millis();
        if (updatedProperties.contains("depthOfWater"))
        {
            depthManager.update();
            chainLength = chain.chainLength(maxFairleadTension, depthOfWater);
            horizontalScope = chain.horizontalScope(maxFairleadTension, depthOfWater, chainLength);
            depthOk = chainLength < maxChainLength;
        }
        else
        {
            if (depthOk && updatedProperties.contains("speedOverGround"))
            {
                speedOk = speedOverGround < MAX_SPEED;
                if (speedOk)
                {
                    if (area == null)
                    {
                        area = new AnchoringArea();
                    }
                    areaOk = area.update();
                    update();
                }
            }
        }
        updateStatus();
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
        depthOk = false;
        speedOk = false;
        areaOk = false;
        updateStatus();
    }

    private void update()
    {
        if (anchorWatch != null)
        {
            synchronized(anchorWatch)
            {
                anchorWatch.update(longitude, latitude, timestamp, horizontalDilutionOfPrecision, speedOverGround);
            }
        }
    }
    private void updateStatus()
    {
        boolean can = depthOk && speedOk && areaOk && depthManager.depthNotExpired();
        if (can != anchoring)
        {
            if (can)
            {
                startAnchoring();
            }
            else
            {
                stopAnchoring();
            }
            anchoring = can;
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
        area = null;
        anchorWatch = null;
        try
        {
            Files.deleteIfExists(path);
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "deleting%s", path);
        }
        config("stop anchoring");
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
            tll = NMEASentence.tll(0, estimated.getY(), estimated.getX(), "Anchor", LocalTime.now(clock), 'L', "");
            transmit();
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
            tll = NMEASentence.tll(0, estimated.getY(), estimated.getX(), "Anchor", LocalTime.now(clock), status, "");
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
    private class AnchoringArea
    {
        private int count;
        private AngleAverage latAve = new AngleAverage();
        private AngleAverage lonAve = new AngleAverage();

        public AnchoringArea()
        {
        }
        
        public AnchoringArea(Point point)
        {
            latAve.addDeg(point.getY());
            lonAve.addDeg(point.getX());
            count++;
        }
        
        public boolean update()
        {
            if (count > 0)
            {
                double distance = Navis.distance(latitude, longitude, centerLatitude(), centerLongitude())*1852;
                if (horizontalScope < distance)
                {
                    return false;
                }
            }
            latAve.addDeg(latitude);
            lonAve.addDeg(longitude);
            count++;
            return true;
        }
        public double centerLatitude()
        {
            return Navis.normalizeToHalfAngle(latAve.averageDeg());
        }
        public double centerLongitude()
        {
            return Navis.normalizeToHalfAngle(lonAve.averageDeg());
        }
    }
    private class DepthManager
    {
        private long time;
        private float depth;
        
        public void update()
        {
            time = timestamp;
            depth = depthOfWater;
        }
        public boolean depthNotExpired()
        {
            return timestamp - time < DEPTH_EXPIRES;
        }
    }
}
