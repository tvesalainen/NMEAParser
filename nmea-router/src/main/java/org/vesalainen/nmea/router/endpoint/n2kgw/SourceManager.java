/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router.endpoint.n2kgw;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nmea.jaxb.router.N2KGatewayType;
import org.vesalainen.nmea.jaxb.router.SourceType;
import org.vesalainen.parsers.nmea.TalkerId;
import static org.vesalainen.parsers.nmea.TalkerId.*;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SourceManager extends JavaLogging
{
    private Map<Byte,Source> map = new HashMap<>();
    private Iterator<TalkerId> free;

    public SourceManager()
    {
        this(null);
    }

    public SourceManager(N2KGatewayType type)
    {
        super(SourceManager.class);
        Set<TalkerId> freeSet = new HashSet<>();
        CollectionHelp.addAll(freeSet, U0, U1, U2, U3, U4, U5, U6, U7, U8);
        if (type != null)
        {
            for (SourceType sourceType : type.getSource())
            {
                TalkerId id = TalkerId.valueOf(sourceType.getTalkerId());
                Source source = new Source(id, Primitives.getInt(sourceType.getInstanceOffset(), 0));
                map.put((byte)sourceType.getSource(), source);
                freeSet.remove(id);
            }
        }
        this.free = freeSet.iterator();
    }
    
    public int getInstanceOffset(int canId)
    {
        int sa = PGN.sourceAddress(canId);
        Source source = map.get(sa);
        if (source != null)
        {
            return source.instanceOffset;
        }
        return 0;
    }
    public TalkerId getTalkerId(int canId)
    {
        int sa = PGN.sourceAddress(canId);
        Source source = map.get(sa);
        if (source != null)
        {
            return source.id;
        }
        else
        {
            if (free.hasNext())
            {
                TalkerId next = free.next();
                source = new Source(next, 0);
                map.put((byte)sa, source);
                return next;
            }
        }
        return U9;
    }
    private class Source
    {
        private TalkerId id;
        private int instanceOffset;

        public Source(TalkerId id, int instanceOffset)
        {
            this.id = id;
            this.instanceOffset = instanceOffset;
        }
        
    }
}
