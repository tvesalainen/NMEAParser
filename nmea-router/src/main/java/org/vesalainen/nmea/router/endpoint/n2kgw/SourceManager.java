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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.nmea.jaxb.router.N2KGatewayType;
import org.vesalainen.nmea.jaxb.router.SourceType;
import org.vesalainen.parsers.nmea.NMEAPGN;
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
    private TalkerId[] map = new TalkerId[256];
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
            for (SourceType src : type.getSource())
            {
                TalkerId id = TalkerId.valueOf(src.getTalkerId());
                map[src.getSource()] = id;
                freeSet.remove(id);
            }
        }
        this.free = freeSet.iterator();
    }
    
    public TalkerId getTalkerId(int canId)
    {
        int sa = PGN.sourceAddress(canId);
        TalkerId id = map[sa];
        if (id != null)
        {
            return id;
        }
        else
        {
            if (free.hasNext())
            {
                TalkerId next = free.next();
                map[sa] = next;
                return next;
            }
        }
        return U9;
    }
}
