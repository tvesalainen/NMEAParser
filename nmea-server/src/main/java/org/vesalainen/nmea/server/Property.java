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
package org.vesalainen.nmea.server;

import java.util.Iterator;
import static java.util.concurrent.TimeUnit.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.vesalainen.lang.Primitives;
import org.vesalainen.management.AbstractDynamicMBean;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingAverage;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingSlope;
import org.vesalainen.math.sliding.TimeValueConsumer;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.util.TimeToLiveList;
import org.vesalainen.util.WeakList;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Property extends AbstractDynamicMBean
{
    private final PropertyType property;
    private WeakList<Observer> observers = new WeakList<>();

    private Property(PropertyType property)
    {
        super(property.getDescription(), property);
        this.property = property;
        register();
    }

    @Override
    public final void register()
    {
        super.register();
    }

    public static Property getInstance(PropertyType property)
    {
        NMEAProperties p = NMEAProperties.getInstance();
        Class<?> type = p.getType(property.getName());
        switch (type.getSimpleName())
        {
            case "int":
            case "long":
            case "float":
            case "double":
                return new DoubleProperty(property);
            case "char":
            case "String":
                return new ObjectProperty(property);
            default:
                if (type.isEnum())
                {
                    return new ObjectProperty(property);
                }
                else
                {
                    throw new UnsupportedOperationException(type+" not supported");
                }
        }
    }

    public void attach(Observer observer)
    {
        observers.add(observer);
    }

    public <T> void set(long time, double value)
    {
        Iterator<Observer> iterator = observers.iterator();
        while (iterator.hasNext())
        {
            Observer next = iterator.next();
            if (!next.accept(time, value))
            {
                iterator.remove();
            }
        }
    }
    public <T> void set(long time, T value)
    {
        Iterator<Observer> iterator = observers.iterator();
        while (iterator.hasNext())
        {
            Observer next = iterator.next();
            if (!next.accept(time, value.toString()))
            {
                iterator.remove();
            }
        }
    }
    
    @Override
    protected ObjectName createObjectName() throws MalformedObjectNameException
    {
        return ObjectName.getInstance(Config.class.getName(), "Property", property.getName());
    }

    public String getName()
    {
        return property.getName();
    }

    public String getDescription()
    {
        return property.getDescription();
    }

    public double getMin()
    {
        return Primitives.getDouble(property.getMin(), -Double.MAX_VALUE);
    }

    public double getMax()
    {
        return Primitives.getDouble(property.getMax(), Double.MAX_VALUE);
    }

    public UnitType getUnit()
    {
        String unit = property.getUnit();
        try
        {
            return UnitType.valueOf(unit);
        }
        catch (IllegalArgumentException ex)
        {
            return UNITLESS;
        }
    }

    public int getDecimals()
    {
        return Primitives.getInt(property.getDecimals(), 0);
    }

    public long getAverageMillis()
    {
        return Primitives.getLong(property.getAverageMillis(), 0);
    }

    public long getPeriodMillis()
    {
        return Primitives.getLong(property.getPeriodMillis(), 0);
    }

    public long getHistoryMinutes()
    {
        return Primitives.getLong(property.getHistoryMinutes(), 0);
    }

    private static class DoubleProperty extends Property
    {
        private DoubleTimeoutSlidingSlope history;
        private TimeValueConsumer func;
        
        public DoubleProperty(PropertyType property)
        {
            super(property);
            func = (t,v)->accept(t, v);
            long periodMillis = getPeriodMillis();
            if (periodMillis > 0)
            {
                long per = periodMillis;
                func = new Delay(per, func);
            }
            long averageMillis = getAverageMillis();
            if (averageMillis > 0)
            {
                DoubleTimeoutSlidingAverage ave = new DoubleTimeoutSlidingAverage(8, averageMillis);
                TimeValueConsumer oldFunc = func;
                func = (t,v)->
                {
                    ave.accept(v, t);
                    oldFunc.accept(t, ave.fast());
                };
            }
            long historyMinutes = getHistoryMinutes();
            if (historyMinutes > 0)
            {
                this.history = new DoubleTimeoutSlidingSlope(256, MILLISECONDS.convert(historyMinutes, MINUTES));
            }
        }
        @Override
        public void set(long time, double arg)
        {
            func.accept(time, arg);
        }
        private void accept(long time, double arg)
        {
            super.set(time, arg);
            if (history != null)
            {
                history.accept(arg, time);
            }
        }

        @Override
        public void attach(Observer observer)
        {
            super.attach(observer);
            if (history != null)
            {
                history.forEach((t, d)->observer.accept(t, d));
            }
        }

    }
    private static class ObjectProperty extends Property
    {
        private TimeToLiveList<String> history;
        public ObjectProperty(PropertyType property)
        {
            super(property);
            long historyMinutes = getHistoryMinutes();
            if (historyMinutes > 0)
            {
                this.history = new TimeToLiveList(historyMinutes, MINUTES);
            }
        }
        @Override
        public <T> void set(long time, T arg)
        {
            super.set(time, arg);
            if (history != null)
            {
                history.add(time, arg.toString());
            }
        }

        @Override
        public void attach(Observer observer)
        {
            super.attach(observer);
            if (history != null)
            {
                history.forEach((t, d)->observer.accept(t, d));
            }
        }

    }
    private class Delay implements TimeValueConsumer
    {
        private final long delay;
        private long last;
        private TimeValueConsumer next;

        public Delay(long delay, TimeValueConsumer next)
        {
            this.delay = delay;
            this.next = next;
        }

        @Override
        public void accept(long t, double v)
        {
            if (t - last >= delay)
            {
                next.accept(t, v);
                last = t;
            }
        }
        
    }
}