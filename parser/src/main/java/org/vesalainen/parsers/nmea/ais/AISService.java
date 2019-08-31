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
package org.vesalainen.parsers.nmea.ais;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.nio.channels.GZIPChannel;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.LifeCycle;
import static org.vesalainen.util.LifeCycle.*;
import org.vesalainen.util.TimeToLiveMap;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISService extends AnnotatedPropertyStore implements Transactional, Stoppable
{
    private static AISService SINGLETON;
    private NMEAService nmeaService;
    @FunctionalInterface
    public interface AISTargetObserver
    {
        void observe(LifeCycle status, int mmsi, AISTarget target);
    }
    private final Path dir;
    private final AISTargetData data = new AISTargetData();
    private final AISTargetDynamic dynamic = new AISTargetDynamic();
    private TimeToLiveMap<Integer,AISTarget> map;
    private Map<Integer,AISTarget> unmodifiableMap;
    private CachedScheduledThreadPool executor;
    private long maxLogSize;
    private AISTarget ownTarget;
    private Set<AISTargetObserver> observers = new HashSet<>();
    
    @Property private NMEAClock clock;
    @Property private MessageTypes messageType;
    @Property private int mmsi;
    @Property private boolean ownMessage;
    
    private AISService(Path dir, long ttlMinutes, long maxLogSize, CachedScheduledThreadPool executor)
    {
        super(MethodHandles.lookup());
        this.dir = dir;
        this.maxLogSize = maxLogSize;
        this.executor = executor;
        this.map = new TimeToLiveMap<>(ttlMinutes, TimeUnit.MINUTES, this::targetRemoved);
        this.unmodifiableMap = Collections.unmodifiableMap(map);
    }
    /**
     * Creates a new instance of AISService
     * @param nmeaService
     * @param dir If dir is not null will use it as AIS DB. Can be null!
     * @param ttlMinutes Time-to-live minutes for active AISTarget
     * @param maxLogSize Maximum log size
     * @param executor
     * @return 
     */
    public static AISService getInstance(NMEAService nmeaService, Path dir, long ttlMinutes, long maxLogSize, CachedScheduledThreadPool executor)
    {
        if (SINGLETON != null)
        {
            throw new IllegalStateException("AISService has already been started");
        }
        SINGLETON = new AISService(dir, ttlMinutes, maxLogSize, executor);
        SINGLETON.attach(nmeaService);
        return SINGLETON;
    }
    /**
     * Returns a running instance of AISService.
     * @return 
     */
    public static AISService getInstance()
    {
        if (SINGLETON == null)
        {
            throw new IllegalStateException("AISService has not been started");
        }
        return SINGLETON;
    }
    private void attach(NMEAService nmeaService)
    {
        this.nmeaService = nmeaService;
        nmeaService.addAISObserver(this);
        nmeaService.addAISObserver(data);
        nmeaService.addAISObserver(dynamic, false);
    }
    /**
     * Stops running AISService and detaches it from NMEAService. After this
     * AISService is not available.
     */
    @Override
    public void stop()
    {
        nmeaService.removeAISObserver(dynamic);
        nmeaService.removeAISObserver(data);
        nmeaService.removeAISObserver(this);
        SINGLETON = null;
        if (ownTarget != null)
        {
            try
            {
                ownTarget.close();
                ownTarget = null;
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        map.clear();    // stores data
    }

    public Collection<AISTarget> getLiveSet()
    {
        return unmodifiableMap.values();
    }
    public Collection<AISTarget> getLiveList()
    {
        if (ownTarget != null)
        {
            List<AISTarget> list = new ArrayList<>(map.values());
            list.sort(null);
            return list;
        }
        else
        {
            return getLiveSet();
        }
    }
    public static void fastBoot(WritableByteChannel ch) throws IOException
    {
        if (SINGLETON != null)
        {
            SINGLETON.bootAIS(ch);
        }
    }
    private void bootAIS(WritableByteChannel ch) throws IOException
    {
        Collection<AISTarget> liveList = getLiveList();
        for (AISTarget target : liveList)
        {
            for (NMEASentence ns : target.getPositionReport())
            {
                ns.writeTo(ch);
            }
        }
        for (AISTarget target : liveList)
        {
            for (NMEASentence ns : target.getStaticReport())
            {
                ns.writeTo(ch);
            }
        }
    }
    public void addObserver(AISTargetObserver observer)
    {
        observers.add(observer);
    }
    public void removeObserver(AISTargetObserver observer)
    {
        observers.remove(observer);
    }
    
    public List<AISTargetDynamic> search(int mmsi, Instant from, Instant to)
    {
        List<AISTargetDynamic> list = new ArrayList<>();
        search(mmsi, from, to, list::add);
        return list;
    }
    public void search(int mmsi, Instant from, Instant to, Consumer<AISTargetDynamic> consumer)
    {
        if (dir == null)
        {
            throw new IllegalArgumentException("AIS directory not set");
        }
        Path path = getDynamicPath(dir, mmsi);
        AISLogFile log = new AISLogFile(path, maxLogSize, executor);
        List<Path> paths = log.getPaths();
        paths.forEach((p)->
        {
            try
            {
                Instant lastModifiedTime = Files.getLastModifiedTime(p).toInstant();
                if (from == null || lastModifiedTime.isAfter(from))
                {
                    try (ReadableByteChannel channel = getChannel(p))
                    {
                        AISTargetDynamicParser.PARSER.parse(channel, (AISTargetDynamic t)->
                        {
                            Instant instant = t.getTimestamp();
                            if (
                                    (from == null || instant.isAfter(from)) &&
                                    (to == null || instant.isBefore(to))
                                    )
                            {
                                consumer.accept(t);
                            }
                        });
                    }
                }
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        });
    }
    @Override
    public void commit(String reason, Collection<String> updatedProperties)
    {
        try
        {
            if (mmsi != 0)
            {
                dynamic.setTimestamp((((Clock)clock).instant()));
                if (ownMessage)
                {
                    if (ownTarget == null)
                    {
                        ownTarget = new AISTarget(maxLogSize, executor, mmsi, dir, new AISTargetData(data), new AISTargetDynamic(dynamic));
                        ownTarget.open();
                        ownTarget.update(messageType, data, dynamic, updatedProperties);
                        map.forEach((m, t)->t.setOwnTarget(ownTarget));
                        fine("created own target");
                    }
                    else
                    {
                        ownTarget.update(messageType, data, dynamic, updatedProperties);
                        finest("updated own target");
                    }
                }
                else
                {
                    AISTarget target = getTarget();
                    target.update(messageType, data, dynamic, updatedProperties);
                    finest("updated target %s with %s", target, updatedProperties);
                    target.setOwnTarget(ownTarget);
                    observers.forEach((o)->o.observe(UPDATE, mmsi, target));
                    fine("updated target %s", target);
                }
            }
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    private void targetRemoved(Integer mmsi, AISTarget target)
    {
        try
        {
            observers.forEach((o)->o.observe(CLOSE, mmsi, target));
            target.close();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private AISTarget getTarget() throws IOException
    {
        AISTarget target = map.get(mmsi);
        if (target == null)
        {
            AISTargetData dat;
            AISTargetDynamic dyn = new AISTargetDynamic();
            Path dataPath = getDataPath(dir, mmsi);
            if (dataPath != null && Files.exists(dataPath))
            {
                dat = new AISTargetData(dataPath, false);
                fine("loaded from %s", dataPath);
            }
            else
            {
                dat = new AISTargetData();
                fine("created new ais target");
            }
            final AISTarget trg = target = new AISTarget(maxLogSize, executor, mmsi, dir, dat, dyn);
            map.put(mmsi, target);
            target.open();
            target.setOwnTarget(ownTarget);
            observers.forEach((o)->o.observe(OPEN, mmsi, trg));
        }
        return target;
    }
    
    protected static Path getDataPath(Path dir, int mmsi)
    {
        if (dir != null)
        {
            return dir.resolve(mmsi+".dat");
        }
        else
        {
            return null;
        }
    }
    protected static Path getDynamicPath(Path dir, int mmsi)
    {
        if (dir != null)
        {
            return dir.resolve(mmsi+".log");
        }
        else
        {
            return null;
        }
    }
    private static ReadableByteChannel getChannel(Path path)
    {
        try
        {
            if (path.getFileName().toString().endsWith(".gz"))
            {
                return new GZIPChannel(path, READ);
            }
            else
            {
                return FileChannel.open(path, READ);
            }
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        
    }
}
