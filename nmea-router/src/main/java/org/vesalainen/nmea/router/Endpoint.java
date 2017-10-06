/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.nmea.script.EndpointScriptEngine;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.vesalainen.nio.RingBuffer;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.FilterType;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.ScriptType;
import org.vesalainen.nmea.router.filter.MessageFilter;
import org.vesalainen.util.Matcher;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Endpoint<T extends ScatteringByteChannel & GatheringByteChannel> extends DataSource
{
    private final Router router;
    protected T channel;
    protected NMEAMatcher<OldRoute> matcher;
    protected RingByteBuffer ring;
    private boolean mark = true;
    protected EndpointScriptEngine scriptEngine;
    private long lastRead;
    private long nowRead;
    protected List<MessageFilter> filterList;
    private final int bufferSize;

    public Endpoint(Router router, EndpointType endpointType, int bufferSize)
    {
        super(endpointType.getName());
        this.router = router;
        this.bufferSize = bufferSize;
        this.ring = new RingByteBuffer(bufferSize, true);
        config("started %s", endpointType.getName());
        init(endpointType);
    }

    private void init(EndpointType endpointType)
    {
        ScriptType scriptType = endpointType.getScript();
        if (scriptType != null)
        {
            scriptEngine = new EndpointScriptEngine(router, this, scriptType.getValue());
        }
        List<FilterType> filters = endpointType.getFilter();
        if (filters != null && !filters.isEmpty())
        {
            config("add filters for %s", name);
            filterList = new ArrayList<>();
            for (FilterType filterType : filters)
            {
                String classname = filterType.getClassname();
                config("creating filter %s", classname);
                try
                {
                    Class<?> cls = Class.forName(classname);
                    if (MessageFilter.class.isAssignableFrom(cls))
                    {
                        MessageFilter filterInstance = (MessageFilter) cls.newInstance();
                        filterList.add(filterInstance);
                    }
                    else
                    {
                        throw new IllegalArgumentException(cls + " not subclass of " + MessageFilter.class);
                    }
                }
                catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
        }
        matcher = createMatcher(endpointType);
    }

    protected NMEAMatcher<OldRoute> createMatcher(EndpointType endpointType)
    {
        NMEAMatcher<OldRoute> wm = null;
        List<RouteType> route = endpointType.getRoute();
        if (!endpointType.getRoute().isEmpty())
        {
            for (RouteType rt : route)
            {
                List<String> targetList = rt.getTarget();
                String prefix = rt.getPrefix();
                if (prefix != null && !prefix.isEmpty())
                {
                    if (wm == null)
                    {
                        wm = new NMEAMatcher<>();
                    }
                    wm.addExpression(prefix, new OldRoute(rt));
                }
                for (String trg : targetList)
                {
                    router.addSource(trg, this);
                }
            }
        }
        if (wm != null)
        {
            wm.compile();
        }
        return wm;
    }

    @Override
    protected int write(RingByteBuffer ring) throws IOException
    {
        if (filterList != null)
        {
            for (MessageFilter filter : filterList)
            {
                if (!filter.accept(ring))
                {
                    return 0;
                }
            }
        }
        int cnt = 0;
        cnt = ring.write(channel);
        finer("write %s = %d", ring, cnt);
        writeCount++;
        writeBytes += cnt;
        return cnt;
    }

    @Override
    protected int write(ByteBuffer bb) throws IOException
    {
        int cnt = 0;
        cnt = channel.write(bb);
        finest("write %s = %d", bb, cnt);
        writeCount++;
        writeBytes += cnt;
        return cnt;
    }

    public boolean isSource()
    {
        return matcher != null;
    }

    @Override
    protected void handle(SelectionKey sk) throws IOException
    {
        lastRead = nowRead;
        nowRead = System.currentTimeMillis();
        int count = ring.read(channel);;
        fine("handle %s read %d bytes", name, count);
        if (count == -1)
        {
            throw new EOFException(name);
        }
        if (ring.isFull())
        {
            long elapsed = nowRead - lastRead;
            warning("buffer %s not big enough (%s) time from last read %d millis count %d", ring, name, elapsed, count);
        }
        readCount++;
        readBytes += count;
        if (matcher == null)
        {
            warning("receive %s without matcher for %s", ring, name);
            return;
        }
        Matcher.Status match = null;
        while (ring.hasRemaining())
        {
            byte b = ring.get(mark);
            match = matcher.match(b);
            switch (match)
            {
                case Error:
                    finest("drop: '%1$c' %1$d 0x%1$02X %2$s", b & 0xff, (RingBuffer) ring);
                    mark = true;
                    break;
                case Ok:
                case WillMatch:
                    mark = false;
                    break;
                case Match:
                    finer("read: %s", ring);
                    write(matcher, ring, lastPosition);
                    write(matcher, ring, -2);
                    lastPosition = -1;
                    if (!matched)
                    {
                        matched(ring);
                    }
                    mark = true;
                    break;
            }
        }
        if (match == Matcher.Status.WillMatch)
        {
            write(matcher, ring, lastPosition);
            lastPosition = ring.getPosition();
        }
    }

    @Override
    public String toString()
    {
        return "Endpoint{" + "name=" + name + '}';
    }

    private void write(Matcher<OldRoute> matcher, RingByteBuffer ring, int lastPosition) throws IOException
    {
        matcher.getMatched().write(name, ring, lastPosition);
        if (scriptEngine != null)
        {
            scriptEngine.write(ring);
        }
    }

}
