/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.nmea.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map.Entry;
import java.util.Set;
import org.vesalainen.lang.Primitives;
import org.vesalainen.navi.WayPoint;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.TalkerId;
import org.vesalainen.util.FloatMap;
import org.vesalainen.util.FloatReference;
import org.vesalainen.util.Recyclable;
import org.vesalainen.util.Recycler;

/**
 *
 * @author tkv
 */
public class NMEASample implements Recyclable, Comparable<NMEASample>, WayPoint
{
    private long time;
    private TalkerId talkerId;
    private MessageType messageType;
    private FloatMap<String> map = new FloatMap<>();
    private Object origin;

    public Set<String> getProperties()
    {
        return map.keySet();
    }

    public FloatMap<String> getMap()
    {
        return map;
    }
    
    public NMEASample setProperty(String property, float value)
    {
        map.put(property, value);
        return this;
    }
    public boolean hasProperties()
    {
        return !map.isEmpty();
    }
    
    public boolean hasProperty(String property)
    {
        return map.containsKey(property);
    }
    
    public float getProperty(String property)
    {
        return map.getFloat(property);
    }
    @Override
    public long getTime()
    {
        return time;
    }
    /**
     * Return time in seconds since epoch.
     * @return 
     */
    public float getFloatTime()
    {
        return ((float)time)/1000.0F;
    }
    void setTime(long time)
    {
        this.time = time;
    }

    public TalkerId getTalkerId()
    {
        return talkerId;
    }

    void setTalkerId(TalkerId talkerId)
    {
        this.talkerId = talkerId;
    }

    public MessageType getMessageType()
    {
        return messageType;
    }

    void setMessageType(MessageType messageType)
    {
        this.messageType = messageType;
    }

    public Object getOrigin()
    {
        return origin;
    }

    void setOrigin(Object origin)
    {
        this.origin = origin;
    }
    
    @Override
    public void clear()
    {
        time = 0;
        talkerId = null;
        messageType = null;
        Recycler.recycle(map.values());
        map.clear();
        origin = null;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(talkerId);
        sb.append(messageType);
        sb.append("[time=");
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
        sb.append(zdt);
        for (Entry<String,FloatReference> e : map.entrySet())
        {
            sb.append(", ");
            sb.append(e.getKey());
            sb.append("=");
            sb.append(e.getValue().getValue());
            
        }
        sb.append("]");
        if (origin != null)
        {
            sb.append(" <- ");
            sb.append(origin);
        }
        return sb.toString();
    }

    @Override
    public int compareTo(NMEASample o)
    {
        return (int) Primitives.signum(time - o.time);
    }

    @Override
    public double getLatitude()
    {
        return map.getFloat("latitude");
    }

    @Override
    public double getLongitude()
    {
        return map.getFloat("longitude");
    }
    
}
