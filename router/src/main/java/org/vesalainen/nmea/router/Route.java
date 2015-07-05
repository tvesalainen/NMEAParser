/*
 * Copyright (C) 2015 tkv
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

/**
 *
 * @author tkv
 */
public final class Route
{
    private final NMEAPrefix prefix;
    private final DataSource[] targets;
    private boolean backup;
    private long lastWrote;
    private List<Route> backupSources;
    private long expireTime = 1500;
    private int count;
    private int backupCount;

    Route() // for test
    {
        this.prefix = null;
        this.targets = null;
    }
    
    public Route(RouteType routeType)
    {
        this.prefix = new NMEAPrefix(routeType.getPrefix());
        List<String> targets = routeType.getTarget();
        if (targets != null)
        {
            this.targets = new DataSource[targets.size()];
            int index = 0;
            for (String target : targets)
            {
                DataSource ds = DataSource.get(target);
                if (ds == null)
                {
                    throw new IllegalArgumentException(target+" not found");
                }
                this.targets[index++] = ds;
            }
        }
        else
        {
            this.targets = new DataSource[0];
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

    public boolean isBackup()
    {
        return backup;
    }
    
    public final void write(RingByteBuffer ring, boolean partial) throws IOException
    {
        lastWrote = System.currentTimeMillis();
        if (canWrite())
        {
            for (DataSource dataSource : targets)
            {
                if (partial)
                {
                    if (dataSource.isSingleSink())
                    {
                       dataSource.writePartial(ring);
                    }
                }
                else
                {
                    dataSource.write(ring);
                }
            }
            count++;
        }
        else
        {
            backupCount++;
        }
    }

    private boolean canWrite()
    {
        if (backupSources != null && !backupSources.isEmpty())
        {
            boolean active = false;
            for (Route p : backupSources)
            {
                if (p.isActive())
                {
                    active = true;
                    break;
                }
            }
            return !active;
        }
        else
        {
            return true;
        }
    }
    private boolean isActive()
    {
        return System.currentTimeMillis()-lastWrote < expireTime;
    }
    void setBackupSources(List<Route> list)
    {
        this.backupSources = list;
    }
    
}
