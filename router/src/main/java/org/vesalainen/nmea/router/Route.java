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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.util.concurrent.ConcurrentArraySet;

/**
 *
 * @author tkv
 */
public final class Route
{
    private final NMEAPrefix prefix;
    private final Set<DataSource> targets = new ConcurrentArraySet<>();
    
    public Route(String prefix, List<String> targets)
    {
        this.prefix = new NMEAPrefix(prefix);
        for (String target : targets)
        {
            DataSource ds = DataSource.get(target);
            if (ds == null)
            {
                throw new IllegalArgumentException(target+" not found");
            }
            this.targets.add(ds);
        }
    }
    
    public final void write(RingByteBuffer ring, boolean partial) throws IOException
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
    }
    
}
