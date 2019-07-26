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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.NMEAClock;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.TimeToLiveMap;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISService extends AnnotatedPropertyStore implements Transactional, Stoppable
{
    private final Path dir;
    private final AISTargetData data = new AISTargetData();
    private final AISTargetDynamic dynamic = new AISTargetDynamic();
    private TimeToLiveMap<Integer,AISTarget> map;
    
    @Property private NMEAClock clock;
    @Property private MessageTypes messageType;
    @Property private int mmsi;
    @Property private boolean ownMessage;

    public AISService(Path dir, long ttlMinutes)
    {
        super(MethodHandles.lookup());
        this.dir = dir;
        this.map = new TimeToLiveMap<>(ttlMinutes, TimeUnit.MINUTES, this::targetRemoved);
    }

    @Override
    public void commit(String reason, Collection<String> updatedProperties)
    {
        try
        {
            if (mmsi != 0)
            {
                AISTarget target = getTarget();
                dynamic.setInstant(Instant.now((Clock) clock));
                target.update(data, dynamic, updatedProperties);
            }
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public void targetRemoved(Integer mmsi, AISTarget target)
    {
        target.close();
    }
    public void attach(NMEAService svc)
    {
        svc.addAISObserver(this);
        svc.addAISObserver(data);
        svc.addAISObserver(dynamic, false);
    }
    public void detach(NMEAService svc)
    {
        svc.removeAISObserver(dynamic);
        svc.removeAISObserver(data);
        svc.removeAISObserver(this);
    }
    @Override
    public void stop()
    {
        map.clear();    // stores data
    }

    private AISTarget getTarget() throws IOException
    {
        AISTarget target = map.get(mmsi);
        if (target == null)
        {
            AISTargetData dat;
            AISTargetDynamic dyn = new AISTargetDynamic();
            Path dataPath = getDataPath();
            if (Files.exists(dataPath))
            {
                dat = new AISTargetData(dataPath, false);
            }
            else
            {
                dat = new AISTargetData();
            }
            target = new AISTarget(mmsi, dir, dat, dyn);
            map.put(mmsi, target);
            target.open();
        }
        return target;
    }
    
    private Path getDataPath()
    {
        return dir.resolve(mmsi+".dat");
    }
}
