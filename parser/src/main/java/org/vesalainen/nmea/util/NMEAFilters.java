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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.vesalainen.navi.Navis;
import static org.vesalainen.navi.Navis.bearing;
import org.vesalainen.util.FloatMap;
import org.vesalainen.util.FloatReference;
import org.vesalainen.util.Lists;
import org.vesalainen.util.Recycler;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.stream.Streams;

/**
 * Filters for NMEASample streams
 * <p>These filters are recycling! No need to use Streams.recyclingPredicate method.
 * @author tkv
 * @see org.vesalainen.util.stream.Streams#recyclingPredicate(java.util.function.Predicate) 
 */
public class NMEAFilters
{
    /**
     * Creates a filter that filters samples so, that no two samples have time
     * closer than period.
     * <p>Use with accumulatorMap and containsAllFilter to make sure all needed
     * properties are present.
     * @param period
     * @param unit
     * @return 
     */
    public static final Predicate<NMEASample> periodicFilter(long period, TimeUnit unit)
    {
        return Streams.recyclingPredicate(new PeriodicFilter(unit.toMillis(period)));
    }
    private static class PeriodicFilter implements Predicate<NMEASample>
    {
        private long period;
        private long limit;

        public PeriodicFilter(long period)
        {
            this.period = period;
        }
        
        @Override
        public boolean test(NMEASample t)
        {
            if (limit == 0)
            {
                limit = period + t.getTime();
                return true;
            }
            else
            {
                long time = t.getTime();
                if (limit < time)
                {
                    limit = period + t.getTime();
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        
    }
    /**
     * This filter passes only samples that have all of given properties.
     * @param properties
     * @return 
     */
    public static final Predicate<NMEASample> containsAllFilter(String... properties)
    {
        return Streams.recyclingPredicate(new ContainsAllFilter(properties));
    }
    private static class ContainsAllFilter implements Predicate<NMEASample>
    {
        private final Set<String> set = new HashSet<>();

        public ContainsAllFilter(String... properties)
        {
            for (String property : properties)
            {
                set.add(property);
            }
        }
        
        @Override
        public boolean test(NMEASample t)
        {
            return t.getProperties().containsAll(set);
        }
        
    }
    /**
     * Returns map that accumulates previous properties to sample.
     * 
     * @return 
     */
    public static final UnaryOperator<NMEASample> accumulatorMap()
    {
        return new AccumulatorMap();
    }
    private static class AccumulatorMap implements UnaryOperator<NMEASample>
    {
        private FloatMap<String> map = new FloatMap<>();
        @Override
        public NMEASample apply(NMEASample t)
        {
            map.entrySet().stream().filter((e) -> (!t.hasProperty(e.getKey()))).forEach((Entry<String, FloatReference> e) ->
            {
                t.setProperty(e.getKey(), e.getValue().getValue());
            });
            map.putAll(t.getMap());
            return t;
        }
        
    }
    /**
     * Returns filter that filters waypoints which bearing differs less that 
     * bearingTolerance in degrees from previous waypoint.
     * <p>This is used to filter waypoints which can be replaced by line in.
     * @param stream
     * @param bearingTolerance
     * @return 
     */
    public static final Stream<NMEASample> bearingToleranceFilter(Stream<NMEASample> stream, double bearingTolerance)
    {
        BearingToleranceSpliterator bearingToleranceSpliterator = new BearingToleranceSpliterator(stream, bearingTolerance);
        return StreamSupport.stream(bearingToleranceSpliterator, false);
    }
    private static class BearingToleranceSpliterator implements Spliterator<NMEASample>
    {
        Spliterator<NMEASample> spliterator;
        private double bearingTolerance;
        private double lastBearing = Double.NaN;
        private boolean go;
        private boolean ended;
        private NMEASample anchor;
        private NMEASample middle;

        public BearingToleranceSpliterator(Stream<NMEASample> stream, double bearingTolerance)
        {
            this(stream.spliterator(), bearingTolerance);
        }
        public BearingToleranceSpliterator(Spliterator<NMEASample> spliterator, double bearingTolerance)
        {
            this.spliterator = spliterator;
            this.bearingTolerance = bearingTolerance;
        }

        @Override
        public boolean tryAdvance(Consumer<? super NMEASample> action)
        {
            if (ended)
            {
                return false;
            }
            go = true;
            while (go)
            {
                if (!spliterator.tryAdvance((s)->handle(s, action)))
                {
                    if (middle != null)
                    {
                        action.accept(middle);
                        ended = true;
                        return true;
                    }
                    return false;
                }
            }
            return true;
        }
        
        private void handle(NMEASample t, Consumer<? super NMEASample> action)
        {
            if (t.hasProperty("latitude") && t.hasProperty("longitude"))
            {
                filter(t, action);
            }
            else
            {
                action.accept(t);
                go = false;
            }
        }

        private void filter(NMEASample wp, Consumer<? super NMEASample> action)
        {
            if (anchor == null)
            {
                anchor = wp;
                action.accept(wp);
                go = false;
            }
            else
            {
                if (Double.isNaN(lastBearing))
                {
                    lastBearing = bearing(anchor, wp);
                    middle = wp;
                }
                else
                {
                    double anchorBearing = bearing(anchor, wp);
                    double middleBearing = bearing(middle, wp);
                    if (
                            Math.abs(anchorBearing-lastBearing) > bearingTolerance ||
                            Math.abs(middleBearing-lastBearing) > bearingTolerance
                            )
                    {
                        lastBearing = middleBearing;
                        action.accept(middle);
                        go = false;
                        anchor = middle;
                        middle = wp;
                    }
                    else
                    {
                        JavaLogging.getLogger(BearingToleranceSpliterator.class).finest("%s skipped because of bearing", middle);
                        Recycler.recycle(middle);
                        middle = wp;
                    }
                }
            }
        }

        @Override
        public Comparator<? super NMEASample> getComparator()
        {
            return spliterator.getComparator();
        }
        
        @Override
        public Spliterator<NMEASample> trySplit()
        {
            return null;
        }

        @Override
        public long estimateSize()
        {
            return spliterator.estimateSize();
        }

        @Override
        public int characteristics()
        {
            return spliterator.characteristics();
        }
        
    }
    /**
     * Creates filter that filters locations until minDistance is moved
     * @param minDistance in NM
     * @return 
     */
    public static final Predicate<NMEASample> minDistanceFilter(double minDistance)
    {
        return Streams.recyclingPredicate(new MinDistanceFilter(minDistance));
    }
    private static class MinDistanceFilter implements Predicate<NMEASample>
    {
        private double minDistance;
        private double lat1 = Double.NaN;
        private double lon1 = Double.NaN;

        public MinDistanceFilter(double minDistance)
        {
            this.minDistance = minDistance;
        }

        @Override
        public boolean test(NMEASample t)
        {
            if (t.hasProperty("latitude") && t.hasProperty("longitude"))
            {
                if (Double.isNaN(lat1))
                {
                    lat1 = t.getProperty("latitude");
                    lon1 = t.getProperty("longitude");
                }
                else
                {
                    double lat2 = t.getProperty("latitude");
                    double lon2 = t.getProperty("longitude");
                    double distance = Navis.distance(lat1, lon1, lat2, lon2);
                    if (distance < minDistance)
                    {
                        return false;
                    }
                    else
                    {
                        lat1 = lat2;
                        lon1 = lon2;
                    }
                }
            }
            return true;
        }
    }
    /**
     * Creates filter locations. Dropping sample when acceleration exceeds given
     * limit. 
     * @param maxAcceleration Maximum speed change in second in knots.
     * @return 
     */
    public static final Predicate<NMEASample> locationFilter(float maxAcceleration)
    {
        return Streams.recyclingPredicate(new LocationFilter(maxAcceleration));
    }
    private static class LocationFilter extends AccelerationFilter
    {
        private long[] tim = new long[2];
        private double[] lat = new double[2];
        private double[] lon = new double[2];
        
        public LocationFilter(float maxAcceleration)
        {
            super(maxAcceleration);
        }

        @Override
        protected void setValue(NMEASample t, int idx)
        {
            tim[idx] = t.getTime();
            lat[idx] = t.getProperty("latitude");
            lon[idx] = t.getProperty("longitude");
        }

        @Override
        protected double calcSpeed(int i1, int i2)
        {
            return Navis.speed(tim[i1], lat[i1], lon[i1], tim[i2], lat[i2], lon[i2]);
        }

        @Override
        protected boolean isValid(NMEASample t)
        {
            return t.hasProperty("latitude") && t.hasProperty("longitude");
        }
        
    }
    /**
     * Creates acceleration filter for property
     * @param property
     * @param maxAcceleration Maximum value change in second in property unit.
     * @return 
     */
    public static final Predicate<NMEASample> propertyAccelerationFilter(String property, float maxAcceleration)
    {
        return Streams.recyclingPredicate(new PropertyAccelerationFilter(property, maxAcceleration));
    }
    private static class PropertyAccelerationFilter extends AccelerationFilter
    {
        private String property;
        protected double[] val = new double[2];
        
        public PropertyAccelerationFilter(String property, float maxAcceleration)
        {
            super(maxAcceleration);
            this.property = property;
        }

        @Override
        protected void setValue(NMEASample t, int idx)
        {
            val[idx] = t.getProperty(property);
        }

        @Override
        protected double calcSpeed(int i1, int i2)
        {
            return (val[i2] - val[i1])/(time[i2] - time[i1]);
        }

        @Override
        protected boolean isValid(NMEASample t)
        {
            return t.hasProperty(property);
        }
        
    }
    private abstract static class AccelerationFilter implements Predicate<NMEASample>
    {
        protected float maxAcceleration;
        protected double[] time = new double[2];
        protected double[] spd = new double[2];
        protected int seq;

        public AccelerationFilter(float maxAcceleration)
        {
            this.maxAcceleration = maxAcceleration;
        }

        @Override
        public boolean test(NMEASample t)
        {
            if (isValid(t))
            {
                time[seq] = t.getFloatTime();
                setValue(t, seq);
                if (seq > 0)
                {
                    spd[seq] = calcSpeed((seq+1)%2, seq);
                }
                if (seq > 1)
                {
                    double acc = calcAcceleration((seq+1)%2, seq);
                    if (acc > maxAcceleration)
                    {
                        JavaLogging.getLogger(TimeFilter.class).info("acceleration %f > limit %f", acc, maxAcceleration);
                        return false;
                    }
                }
                seq = seq == 0 ? 1 : 0 ;
            }
            return true;
        }

        private double calcAcceleration(int i1, int i2)
        {
            return Math.abs(spd[i2] - spd[i1])/(time[i2] - time[i1]);
        }
        
        protected abstract void setValue(NMEASample t, int idx);

        protected abstract double calcSpeed(int i1, int i2);

        protected abstract boolean isValid(NMEASample t);

        
    }
    /**
     * Creates range filter for property
     * @param property
     * @param min
     * @param max
     * @return 
     */
    public static final Predicate<NMEASample> rangeFilter(String property, float min, float max)
    {
        return Streams.recyclingPredicate(new RangeFilter(property, min, max));
    }
    private static class RangeFilter implements Predicate<NMEASample>
    {
        private String property;
        private float min;
        private float max;

        public RangeFilter(String property, float min, float max)
        {
            this.property = property;
            this.min = min;
            this.max = max;
        }
        
        @Override
        public boolean test(NMEASample t)
        {
            if (t.hasProperty(property))
            {
                float value = t.getProperty(property);
                if (value >= min && value <= max)
                {
                    return true;
                }
                else
                {
                    JavaLogging.getLogger(TimeFilter.class).info("%s: %f not in range %f - %f", property, value, min, max);
                    return false;
                }
            }
            else
            {
                return true;
            }
        }
        
    }
    /**
     * Creates time filter. Filters samples with no time or time in past.
     * @return 
     */
    public static final Predicate<NMEASample> timeFilter()
    {
        return Streams.recyclingPredicate(new TimeFilter());
    }
    private static class TimeFilter implements Predicate<NMEASample>
    {
        private long lastTime;
        
        @Override
        public boolean test(NMEASample s)
        {
            long time = s.getTime();
            if (time <= 0)
            {
                return false;
            }
            if (lastTime == 0)
            {
                lastTime = time;
                return true;
            }
            else
            {
                if (lastTime >= time )
                {
                    JavaLogging log = JavaLogging.getLogger(TimeFilter.class);
                    if (log.isLoggable(Level.INFO))
                    {
                        ZonedDateTime zp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastTime), ZoneOffset.UTC);
                        ZonedDateTime zt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
                        log.info("time %s < prev %s", zt, zp);
                    }
                    return false;
                }
            }
            lastTime = time;
            return true;
        }
        
    }
}
