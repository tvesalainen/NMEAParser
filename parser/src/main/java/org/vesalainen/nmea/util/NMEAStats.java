/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.TalkerId;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEAStats
{
    private Map<String,MessageStats> messageMap = new HashMap<>();
    private Map<String,PropertyStats> propertyMap = new HashMap<>();

    private NMEAStats()
    {
    }
    
    public static final NMEAStats stats(Stream<NMEASample> stream)
    {
        NMEAStats stats = new NMEAStats();
        stream.forEach((s)->stats.update(s));
        return stats;
    }
    protected void update(NMEASample sample)
    {
        TalkerId talkerId = sample.getTalkerId();
        MessageType messageType = sample.getMessageType();
        String key = talkerId.name()+messageType.name();
        MessageStats ms = messageMap.get(key);
        if (ms == null)
        {
            ms = new MessageStats();
            messageMap.put(key, ms);
        }
        ms.update(sample);
        sample.getMap().entrySet().stream().forEach((e) ->
        {
            String property = e.getKey();
            PropertyStats ps = propertyMap.get(property);
            if (ps == null)
            {
                ps = new PropertyStats();
                propertyMap.put(property, ps);
            }
            ps.update(e.getValue().getValue());
        });
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        for (Entry<String,MessageStats> e : messageMap.entrySet())
        {
            sb.append(e.getKey());
            sb.append(' ');
            sb.append(e.getValue());
            sb.append('\n');
        }
        for (Entry<String,PropertyStats> e : propertyMap.entrySet())
        {
            sb.append(e.getKey());
            sb.append(' ');
            sb.append(e.getValue());
            sb.append('\n');
        }
        return sb.toString();
    }
    
    
    public static class MessageStats
    {
        private int count;
        private long first;
        private long latest;
        private float messagePerSecond;
        private long minGap = Long.MAX_VALUE;
        private long maxGap = Long.MIN_VALUE;
        private Set<Object> origins = new HashSet<>();
        
        public void update(NMEASample sample)
        {
            count++;
            Object origin = sample.getOrigin();
            if (origin != null)
            {
                origins.add(origin);
            }
            long time = sample.getTime();
            if (latest != 0)
            {
                long gap = time - latest;
                if (gap > 0)
                {
                    minGap = Math.min(gap, minGap);
                    maxGap = Math.max(gap, maxGap);
                    messagePerSecond = ((float)count / (float)(time - first))*1000F;
                }
            }
            else
            {
                first = time;
            }
            latest = time;
        }

        @Override
        public String toString()
        {
            return String.format("%5d %2.2f %8d %8d %s", count, messagePerSecond, minGap, maxGap, origins);
        }
        
    }
    public static class PropertyStats
    {
        private float min = Float.POSITIVE_INFINITY;
        private float max = Float.NEGATIVE_INFINITY;

        public void update(float value)
        {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        @Override
        public String toString()
        {
            return String.format("%5.5f - %5.5f", min, max);
        }
        
    }
}
