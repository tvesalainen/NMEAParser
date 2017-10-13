/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router;

import java.io.IOException;
import java.util.List;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.router.endpoint.Endpoint;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Route extends JavaLogging
{
    private final NMEAPrefix prefix;
    private final String[] targetList;
    private boolean backup;
    private long lastWrote;
    private List<Route> backupSources;
    private long expireTime = 1500;
    private int count;
    private int backupCount;

    Route() // for test
    {
        super(Route.class);
        this.prefix = null;
        this.targetList = null;
    }
    
    public Route(RouteType routeType)
    {
        super(Route.class);
        this.prefix = new NMEAPrefix(routeType.getPrefix());
        List<String> targets = routeType.getTarget();
        if (targets != null)
        {
            targetList = new String[targets.size()];
            int index = 0;
            for (String target : targets)
            {
                this.targetList[index++] = target;
            }
        }
        else
        {
            this.targetList = new String[0];
        }
        Boolean b = routeType.isBackup();
        if (b != null)
        {
            backup = b;
        }
        Long expire = routeType.getExpire();
        if (expire != null)
        {
            expireTime = expire;
        }
    }

    public NMEAPrefix getPrefix()
    {
        return prefix;
    }

    public final void write(String origin, RingByteBuffer ring) throws IOException
    {
        lastWrote = System.currentTimeMillis();
        for (String target : targetList)
        {
            Endpoint endpoint = Endpoint.get(target);
            if (endpoint != null)
            {
                endpoint.write(ring);
            }
        }
        count++;
    }
    
}
