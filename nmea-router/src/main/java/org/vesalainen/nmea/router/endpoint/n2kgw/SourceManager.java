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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.vesalainen.can.j1939.AddressManager.Name;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.lang.Primitives;
import org.vesalainen.nmea.jaxb.router.N2KGatewayType;
import org.vesalainen.nmea.jaxb.router.SourceType;
import org.vesalainen.parsers.nmea.TalkerId;
import static org.vesalainen.parsers.nmea.TalkerId.*;
import org.vesalainen.text.CamelCase;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SourceManager extends JavaLogging
{
    private Map<Byte,Source> map = new HashMap<>();
    private Set<TalkerId> free;
    private List<SourceType> source;

    public SourceManager()
    {
        this(null);
    }

    public SourceManager(N2KGatewayType type)
    {
        super(SourceManager.class);
        free = new HashSet<>();
        CollectionHelp.addAll(free, U0, U1, U2, U3, U4, U5, U6, U7, U8);
        if (type != null)
        {
            this.source = type.getSource();
        }
    }
    public void nameChanged(Name name)
    {
        SourceType res = null;
        for (SourceType st : source)
        {
            if (st.getName() != null)
            {
                if (name.getName() == st.getName())
                {
                    res = st;
                    break;
                }
            }
            String modelId = st.getModelId();
            String manufacturerSModelId = name.getManufacturerSModelId();
            if (modelId != null && manufacturerSModelId != null)
            {
                if (CamelCase.camelCase(modelId).equals(CamelCase.camelCase(manufacturerSModelId)))
                {
                    res = st;
                    config("resolved name %s with address %d talker id %s", modelId, name.getSource(), st.getTalkerId());
                    break;
                }
            }
        }
        if (res != null)
        {
            Source old = map.put((byte)name.getSource(), new Source(res));
            if (old != null)
            {
                free.add(old.id);
            }
        }
    }
    public int getInstanceOffset(int canId)
    {
        int sa = PGN.sourceAddress(canId);
        Source src = map.get(sa);
        if (src != null)
        {
            return src.instanceOffset;
        }
        return 0;
    }
    public TalkerId getTalkerId(int canId)
    {
        int sa = PGN.sourceAddress(canId);
        Source src = map.get((byte)sa);
        if (src != null)
        {
            return src.id;
        }
        else
        {
            if (!free.isEmpty())
            {
                TalkerId next = free.iterator().next();
                src = new Source(next, 0);
                map.put((byte)sa, src);
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

        public Source(SourceType type)
        {
            this.id = TalkerId.valueOf(type.getTalkerId());
            this.instanceOffset = Primitives.getInt(type.getInstanceOffset(), 0);
        }
    }
}
