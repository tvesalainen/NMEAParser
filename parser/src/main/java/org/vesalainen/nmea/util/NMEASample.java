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
public class NMEASample implements Recyclable
{
    private long time;
    private TalkerId talkerId;
    private MessageType messageType;
    private FloatMap<String> map = new FloatMap<>();

    void setProperty(String property, float value)
    {
        map.put(property, value);
    }
    public boolean hasProperties()
    {
        return !map.isEmpty();
    }
    
    public float getProperty(String property)
    {
        return map.getFloat(property);
    }
    public long getTime()
    {
        return time;
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
    
    @Override
    public void clear()
    {
        time = 0;
        talkerId = null;
        messageType = null;
        Recycler.recycle(map.values());
        map.clear();
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
        return sb.toString();
    }
    
}
