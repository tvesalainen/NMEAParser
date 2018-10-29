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

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import static java.util.logging.Level.SEVERE;
import java.util.stream.Stream;
import org.vesalainen.navi.cpa.Vessel;
import org.vesalainen.parsers.mmsi.MMSIEntry;
import org.vesalainen.parsers.mmsi.MMSIParser;
import org.vesalainen.parsers.mmsi.MMSIType;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.util.TimeToLiveSet;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISMonitor extends JavaLogging
{
    private static AISMonitor MONITOR;
    private MMSIParser mmsiParser = MMSIParser.getInstance();

    private CachedScheduledThreadPool executor;
    private Clock clock;
    private boolean refreshStaticData;
    private int interpolateSeconds;
    private TimeToLiveSet<Integer> ttlSet;
    private Map<Integer,CacheEntry> map = new WeakHashMap<>();
    private Function<String,Properties> loader;
    private Vessel ownVessel;
    private final ByteChannel channel;

    public AISMonitor(ByteChannel channel, CachedScheduledThreadPool executor, Clock clock, long ttlMinutes, boolean refreshStaticData, int interpolateSeconds, Function<String, Properties> loader)
    {
        super(AISMonitor.class);
        this.channel = channel;
        this.executor = executor;
        this.clock = clock;
        this.refreshStaticData = refreshStaticData;
        this.interpolateSeconds = interpolateSeconds;
        this.ttlSet = new TimeToLiveSet<>(clock, ttlMinutes, TimeUnit.MINUTES);
        this.loader = loader;
    }
    
    public void updateOwn(
            double latitude, 
            double longitude, 
            double speed, 
            double bearing, 
            double rateOfTurn
    )
    {
        if (ownVessel == null)
        {
            ownVessel = new Vessel();
        }
        ownVessel.update(clock.millis(), latitude, longitude, speed, bearing, rateOfTurn);
    }
    public void update(
                MessageTypes type, 
                ZonedDateTime timestamp,
                Properties properties,
                double latitude, 
                double longitude, 
                double speed, 
                double bearing, 
                double rateOfTurn
    )
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
                if (refreshStaticData)
                {
                    entry.sendStaticData(channel);
                }
            }
            entry.update(properties);
            entry.update(type, timestamp, latitude, longitude, speed, bearing, rateOfTurn);
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

    public static AISMonitor getInstance()
    {
        return MONITOR;
    }

    public static void setInstance(AISMonitor MONITOR)
    {
        AISMonitor.MONITOR = MONITOR;
    }

    public static void fastBoot(ByteChannel channel)
    {
        if (MONITOR != null)
        {
            MONITOR.doFastBoot(channel);
        }
    }
    public void doFastBoot(ByteChannel channel)
    {
        FastBooter fastBooter = new FastBooter(channel);
        executor.submit(fastBooter);
    }    
    public class CacheEntry
    {
        private char deviceClass;
        private Properties properties = new Properties();
        private MMSIEntry mmsiEntry;
        private Vessel vessel;

        public CacheEntry()
        {
        }
        
        public void fastBoot(ByteChannel channel) throws IOException
        {
            if (vessel != null)
            {
                try 
                {
                    sendPositionEstimate(channel);
                }
                catch (NoSuchElementException nse)
                {
                    warning("position report failed %s", nse.getMessage());
                }
            }
            try 
            {
                sendStaticData(channel);
            }
            catch (NoSuchElementException nse)
            {
                warning("position report failed %s", nse.getMessage());
            }
        }    
        public void update(Properties properties)
        {
            this.properties.putAll(properties);
        }
        public void update(
                MessageTypes type, 
                ZonedDateTime timestamp,
                double latitude, 
                double longitude, 
                double speed, 
                double bearing, 
                double rateOfTurn
        )
        {
            detectClass(type);
            if (type.isPositionReport())
            {
                long millis = timestamp.toInstant().toEpochMilli(); // this is synced with second
                if (vessel == null)
                {
                    vessel = new Vessel();
                }
                else
                {
                    warning("delta %s", new Location(
                            latitude-vessel.estimatedLatitude(millis),
                            longitude-vessel.estimatedLongitude(millis)
                                    )
                            );
                }
                vessel.update(millis, latitude, longitude, speed, bearing, rateOfTurn);
            }
        }

        public boolean isOwn()
        {
            return Boolean.parseBoolean(properties.getProperty("ownMessage", "false"));
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

        private void detectClass(MessageTypes type)
        {
            switch (type)
            {
                case PositionReportClassA:
                case PositionReportClassAAssignedSchedule:
                case PositionReportClassAResponseToInterrogation:
                case StandardSARAircraftPositionReport:
                    deviceClass = 'A';
                    break;
                case StandardClassBCSPositionReport:
                case ExtendedClassBEquipmentPositionReport:
                    deviceClass = 'B';
                    break;
            }
        }

        private void sendStaticData(ByteChannel ch)
        {
            try
            {
                if (properties.containsKey("vesselName"))   // has static data
                {
                    if (properties.containsKey("imoNumber"))    // is class A
                    {
                        NMEASentence[] msg5 = AISMessageGen.msg5(this);
                        for (NMEASentence ns : msg5)
                        {
                            ns.writeTo(ch);
                        }
                    }
                    else
                    {
                        NMEASentence[] msg24A = AISMessageGen.msg24A(this);
                        msg24A[0].writeTo(channel);
                        if (properties.containsKey("callSign"))
                        {
                            NMEASentence[] msg24B = AISMessageGen.msg24B(this);
                            msg24B[0].writeTo(channel);
                        }
                    }
                }
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "");
            }
        }

        private void sendPositionEstimate(ByteChannel channel)
        {
            try
            {
                ZonedDateTime zdt = ZonedDateTime.now(clock);
                int second = zdt.getSecond();
                long millis = clock.millis();
                Location estimatedLocation = vessel.estimatedLocation(millis);
                if (deviceClass == 'B')
                {
                    NMEASentence[] msg18 = AISMessageGen.msg18(this, second, estimatedLocation.getLatitude(), estimatedLocation.getLongitude());
                    msg18[0].writeTo(channel);
                }
                else
                {
                    NMEASentence[] msg1 = AISMessageGen.msg1(this, second, estimatedLocation.getLatitude(), estimatedLocation.getLongitude());
                    msg1[0].writeTo(channel);
                }
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "");
            }
        }
        
    }
    private class FastBooter implements Runnable
    {
        private ByteChannel channel;

        public FastBooter(ByteChannel channel)
        {
            this.channel = channel;
        }
        
        @Override
        public void run()
        {
            try
            {
                for (Integer mmsi : ttlSet)
                {
                    CacheEntry entry = map.get(mmsi);
                    //if (!entry.isOwn())
                    {
                        entry.fastBoot(channel);
                    }
                }
            }
            catch (Exception ex)
            {
                log(SEVERE, ex, "fast boot failed");
            }
        }
        
    }
}
