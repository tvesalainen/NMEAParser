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
package org.vesalainen.nmea.router.endpoint;

import org.vesalainen.nmea.script.EndpointScriptEngine;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static java.util.logging.Level.*;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.FilterType;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.ScriptType;
import org.vesalainen.nmea.router.NMEAMatcher;
import org.vesalainen.nmea.router.NMEAReader;
import org.vesalainen.nmea.router.Route;
import org.vesalainen.nmea.router.Router;
import org.vesalainen.nmea.router.filter.MessageFilter;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.Matcher;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Endpoint<E extends EndpointType, T extends ScatteringByteChannel & GatheringByteChannel> extends DataSource
{
    protected final Router router;
    protected final E endpointType;
    protected final int bufferSize;
    protected T channel;
    protected NMEAMatcher<Route> matcher;
    protected RingByteBuffer ring;
    protected EndpointScriptEngine scriptEngine;
    protected List<MessageFilter> filterList;
    protected long lastRead;
    protected long lastWrite;
    protected Set<String> fingerPrint = new HashSet<>();

    public Endpoint(E endpointType, Router router, int bufferSize)
    {
        super(endpointType.getName());
        this.router = router;
        this.endpointType = endpointType;
        this.bufferSize = bufferSize;
        init();
        config("started %s", endpointType.getName());
    }

    private void init()
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

    protected NMEAMatcher<Route> createMatcher(EndpointType endpointType)
    {
        NMEAMatcher<Route> wm = null;
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
                    wm.addExpression(prefix, new Route(rt));
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
    public int write(RingByteBuffer ring) throws IOException
    {
        lastWrite = System.currentTimeMillis();
        int cnt = 0;
        if (channel != null)
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
            cnt = ring.write(channel);
            finer("write %s = %d", ring, cnt);
            writeCount++;
            writeBytes += cnt;
        }
        return cnt;
    }

    @Override
    public int write(ByteBuffer bb) throws IOException
    {
        lastWrite = System.currentTimeMillis();
        int cnt = 0;
        if (channel != null)
        {
            cnt = channel.write(bb);
            finest("write %s = %d", bb, cnt);
            writeCount++;
            writeBytes += cnt;
        }
        return cnt;
    }

    public abstract T createChannel() throws IOException;
    
    @Override
    public void run()
    {
        String safeName = "uninitialised";
        try (T ch = createChannel())
        {
            safeName = Thread.currentThread().getName();
            Thread.currentThread().setName(name);
            channel = ch;
            config("started %s", channel);
            if (scriptEngine != null)
            {
                scriptEngine.start();
            }
            NMEAReader reader = new NMEAReader(name, matcher, channel, bufferSize, this::onOk, this::onError);
            reader.read();
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s stopped because of %s", name, ex);
        }
        finally
        {
            Thread.currentThread().setName(safeName);
            if (scriptEngine != null)
            {
                scriptEngine.stop();
            }
        }
    }

    private void onOk(RingByteBuffer ring) throws IOException
    {
        readBytes += ring.length();
        readCount++;
        lastRead = System.currentTimeMillis();
        finer("read: %s", ring);
        matcher.getMatched().write(name, ring);
        if (scriptEngine != null)
        {
            scriptEngine.write(ring);
        }
        int idx = CharSequences.indexOf(ring, ',');
        if (idx != -1)
        {
            String prefix = ring.subSequence(0, idx).toString();
            fingerPrint.add(prefix);
        }
    }
    private void onError(byte[] errInput) throws IOException
    {
        lastRead = System.currentTimeMillis();
        errorBytes += errInput.length;
        warning("rejected %d bytes", errInput.length);
        finest(()->HexDump.toHex(errInput));
    }
    @Override
    public Date getLastRead()
    {
        return new Date(lastRead);
    }

    @Override
    public Date getLastWrite()
    {
        return new Date(lastWrite);
    }

    @Override
    public Set<String> getFingerPrint()
    {
        return fingerPrint;
    }

    public E getEndpointType()
    {
        return endpointType;
    }

    @Override
    public String toString()
    {
        return "Endpoint{" + "name=" + name + '}';
    }

}
