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
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import static java.util.logging.Level.SEVERE;
import java.util.stream.Stream;
import org.vesalainen.navi.cpa.Vessel;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.mmsi.MMSIType;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.util.TimeToLiveMap;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISMonitor extends JavaLogging implements Stoppable
{
    private static AISMonitor MONITOR;

    private enum BootState {POS, MSG5, MSG24A, MSG24B};
    private CachedScheduledThreadPool executor;
    private Clock clock;
    private boolean refreshStaticData;
    private int interpolateSeconds;
    private int refreshInitialDelayMillis;
    private int refreshDelayMillis;
    private TimeToLiveMap<String,CacheEntry> map;
    private Function<String,Properties> loader;
    private Vessel ownVessel;
    private final WritableByteChannel channel;
    private ScheduledFuture<?> interpolatorFuture;

    public AISMonitor(
            WritableByteChannel channel, 
            CachedScheduledThreadPool executor, 
            Clock clock, 
            long ttlMinutes, 
            boolean refreshStaticData, 
            int refreshInitialDelayMillis,
            int refreshDelayMillis,
            int interpolateSeconds, 
            Function<String, Properties> loader)
    {
        super(AISMonitor.class);
        this.channel = channel;
        this.executor = executor;
        this.clock = clock;
        this.refreshStaticData = refreshStaticData;
        this.interpolateSeconds = interpolateSeconds;
        this.refreshInitialDelayMillis = refreshInitialDelayMillis;
        this.refreshDelayMillis = refreshDelayMillis;
        this.map = new TimeToLiveMap<>(clock, ttlMinutes, TimeUnit.MINUTES);
        this.loader = loader;
        if (interpolateSeconds > 0)
        {
            interpolatorFuture = executor.scheduleWithFixedDelay(new Interpolator(), interpolateSeconds, interpolateSeconds, TimeUnit.SECONDS);
        }
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
        String mmsi = properties.getProperty("mmsi");
        if (mmsi != null)
        {
            CacheEntry entry = map.get(mmsi);
            if (entry == null)
            {
                entry = new CacheEntry();
                map.put(mmsi, entry);
                entry.update(loader.apply(mmsi));
                if (refreshStaticData)
                {
                    entry.sendStaticData(channel);
                }
            }
            entry.update(properties);
            entry.update(type, timestamp, latitude, longitude, speed, bearing, rateOfTurn);
        }
    }
    public CacheEntry getEntry(String mmsi)
    {
        CacheEntry entry = map.get(mmsi);
        if (entry == null)
        {
            entry = new CacheEntry();
            map.put(mmsi, entry);
            entry.update(loader.apply(mmsi));
        }
        return entry;
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

    @Override
    public void stop()
    {
        interpolatorFuture.cancel(false);
    }
    public class CacheEntry
    {
        private char deviceClass;
        private Properties properties = new Properties();
        private Vessel vessel;
        private MMSIType mmsiType;

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
            if (mmsiType == null)
            {
                String mmsi = properties.getProperty("mmsi");
                mmsiType = MMSIType.getType(Integer.parseInt(mmsi));
            }
            return mmsiType;
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

        private void sendStaticData(WritableByteChannel ch)
        {
            try
            {
                if (properties.containsKey("vesselName"))   // has static data
                {
                    if (properties.containsKey("imoNumber"))    // is class A
                    {
                        sendMsg5(ch);
                    }
                    else
                    {
                        sendMsg24A(ch);
                        sendMsg24B(ch);
                    }
                }
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "");
            }
        }

        private boolean sendMsg5(WritableByteChannel ch) throws IOException
        {
            if (properties.containsKey("imoNumber"))    // is class A
            {
                NMEASentence[] msg5 = AISMessageGen.msg5(this);
                for (NMEASentence ns : msg5)
                {
                    ns.writeTo(ch);
                }
                return true;
            }
            return false;
        }
        private boolean sendMsg24A(WritableByteChannel ch) throws IOException
        {
            if (properties.containsKey("vesselName"))   // has static data
            {
                NMEASentence[] msg24A = AISMessageGen.msg24A(this);
                msg24A[0].writeTo(channel);
                return true;
            }
            return false;
        }
        private boolean sendMsg24B(WritableByteChannel ch) throws IOException
        {
            if (properties.containsKey("callSign"))
            {
                NMEASentence[] msg24B = AISMessageGen.msg24B(this);
                msg24B[0].writeTo(channel);
                return true;
            }
            return false;
        }
        private boolean sendPositionEstimate(WritableByteChannel channel)
        {
            try
            {
                if (vessel != null && vessel.isValid())
                {
                    ZonedDateTime zdt = ZonedDateTime.now(clock);
                    int second = zdt.getSecond();
                    long millis = clock.millis();
                    Location estimatedLocation = vessel.estimatedLocation(millis);
                    if (deviceClass == 'B')
                    {
                        info("%s", properties);
                        NMEASentence[] msg18 = AISMessageGen.msg18(this, second, estimatedLocation.getLatitude(), estimatedLocation.getLongitude());
                        msg18[0].writeTo(channel);
                    }
                    else
                    {
                        NMEASentence[] msg1 = AISMessageGen.msg1(this, second, estimatedLocation.getLatitude(), estimatedLocation.getLongitude());
                        msg1[0].writeTo(channel);
                    }
                    return true;
                }
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "");
            }
            return false;
        }
        
    }
    private class Interpolator implements Runnable
    {

        @Override
        public void run()
        {
            try
            {
                for (String mmsi : map.keySet())
                {
                    CacheEntry entry = map.get(mmsi);
                    if (!entry.isOwn())
                    {
                        entry.sendPositionEstimate(channel);
                    }
                }
            }
            catch (Exception ex)
            {
                log(SEVERE, ex, "AIS interpolator failed");
            }
        }
        
    }
    private class FastBooter implements Runnable
    {
        private ByteChannel channel;
        private Iterator<CacheEntry> entries;
        private BootState state = BootState.POS;
        private ScheduledFuture<?> future;
        private CacheEntry entry;

        public FastBooter(ByteChannel channel)
        {
            this.channel = channel;
        }
        
        @Override
        public void run()
        {
            entries = map.keySet().stream().map((m)->map.get(m)).filter((e)->!e.isOwn()).iterator();
            future = executor.scheduleWithFixedDelay(this::boot, refreshInitialDelayMillis, refreshDelayMillis, TimeUnit.MILLISECONDS);
        }
        private void boot()
        {
            try
            {
                switch (state)
                {
                    case POS:
                        if (entries.hasNext())
                        {
                            entry = entries.next();
                            entry.sendPositionEstimate(channel);
                            state = BootState.MSG5;
                        }
                        else
                        {
                            future.cancel(false);
                        }
                        break;
                    case MSG5:
                        if (entry.sendMsg5(channel))
                        {
                            state = BootState.POS;
                            return;
                        }
                    case MSG24A:
                        entry.sendMsg24A(channel);
                        {
                            state = BootState.MSG24B;
                            return;
                        }
                    case MSG24B:
                        entry.sendMsg24B(channel);
                        state = BootState.POS;
                        break;
                }
            }
            catch (Exception ex)
            {
                log(SEVERE, ex, "AIS fast boot failed");
            }
        }
    }
}
