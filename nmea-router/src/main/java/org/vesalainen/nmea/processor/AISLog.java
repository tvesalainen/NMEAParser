/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.nio.channels.GatheringByteChannel;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.parsers.nmea.ais.AISService;
import org.vesalainen.parsers.nmea.ais.AISTarget;
import org.vesalainen.parsers.nmea.ais.AISTargetDynamic;
import org.vesalainen.util.LifeCycle;
import static org.vesalainen.util.LifeCycle.OPEN;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISLog implements Stoppable
{
    private final AISService aisService;
    private final GatheringByteChannel channel;

    private AISLog(AISService aisService, GatheringByteChannel channel)
    {
        this.aisService = aisService;
        this.channel = channel;
    }
    
    public static AISLog getInstance(NMEAService nmeaService, Path dir, long ttlMinutes, long maxLogSize, CachedScheduledThreadPool executor, GatheringByteChannel out)
    {
        AISService aisService = AISService.getInstance(nmeaService, dir, ttlMinutes, maxLogSize, executor);
        AISLog aisLog = new AISLog(aisService, out);
        aisService.addObserver(aisLog::newTarget);
        return aisLog;
    }

    @Override
    public void stop()
    {
        aisService.removeObserver(this::newTarget);
        aisService.stop();
    }
    
    private void newTarget(LifeCycle status, int mmsi, AISTarget target)
    {
        if (status == OPEN)
        {
            for (NMEASentence sentence : target.getStaticReport())
            {
                try
                {
                    sentence.writeTo(channel);
                }
                catch (IOException ex)
                {
                    aisService.log(Level.SEVERE, ex, "AISLog target=%s", target);
                }
            }
        }
    }
}
