/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea.ais;

import java.time.Clock;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;
import org.vesalainen.parsers.mmsi.MMSIEntry;
import org.vesalainen.parsers.mmsi.MMSIParser;
import org.vesalainen.parsers.mmsi.MMSIType;
import org.vesalainen.util.TimeToLiveSet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISCache
{
    private static AISCache CACHE;
    private MMSIParser mmsiParser = MMSIParser.getInstance();

    private TimeToLiveSet<Integer> ttlSet;
    private Map<Integer,CacheEntry> map = new WeakHashMap<>();
    private Function<String,Properties> loader;

    public AISCache(Clock clock, long timeout, TimeUnit unit, Function<String, Properties> loader)
    {
        this.ttlSet = new TimeToLiveSet<>(clock, timeout, unit);
        this.loader = loader;
    }
    
    public void update(Properties properties)
    {
        String mmsiString = properties.getProperty("mmsi");
        if (mmsiString != null)
        {
            Integer mmsi = Integer.valueOf(mmsiString);
            ttlSet.refresh(mmsi);
            CacheEntry entry = map.get(mmsi);
            if (entry == null)
            {
                entry = new CacheEntry();
                map.put(mmsi, entry);
                entry.update(loader.apply(mmsiString));
            }
            entry.update(properties);
        }
    }
    public CacheEntry getEntry(Integer mmsi)
    {
        ttlSet.refresh(mmsi);
        CacheEntry entry = map.get(mmsi);
        if (entry == null)
        {
            entry = new CacheEntry();
            map.put(mmsi, entry);
            entry.update(loader.apply(mmsi.toString()));
        }
        return entry;
    }
    public Stream<CacheEntry> activeVessels()
    {
        return ttlSet.stream().map((k)->map.get(k));
    }

    public static AISCache getInstance()
    {
        return CACHE;
    }

    public static void setInstance(AISCache CACHE)
    {
        AISCache.CACHE = CACHE;
    }
    
    public class CacheEntry
    {
        private Properties properties = new Properties();
        private MMSIEntry mmsiEntry;

        public CacheEntry()
        {
        }
        
        public void update(Properties properties)
        {
            this.properties.putAll(properties);
        }
        
        public MMSIType getMMSIType()
        {
            if (mmsiEntry == null)
            {
                String mmsi = properties.getProperty("mmsi");
                mmsiEntry = mmsiParser.parse(mmsi);
            }
            return mmsiEntry.getType();
        }

        public Properties getProperties()
        {
            return properties;
        }
        
    }
}
