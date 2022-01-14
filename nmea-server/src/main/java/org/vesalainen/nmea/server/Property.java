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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import static java.util.concurrent.TimeUnit.*;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.json.JSONWriter;
import org.vesalainen.json.JSONBuilder;
import org.vesalainen.lang.Primitives;
import org.vesalainen.management.AbstractDynamicMBean;
import org.vesalainen.math.UnitCategory;
import static org.vesalainen.math.UnitCategory.*;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingAverage;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingSeries;
import org.vesalainen.math.sliding.TimeValueConsumer;
import org.vesalainen.math.sliding.TimeoutSlidingAngleAverage;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.util.TimeToLiveList;
import org.vesalainen.util.WeakList;
import web.I18n;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Property extends AbstractDynamicMBean implements NotificationListener, Comparable<Property>
{
    private final String name;
    private final PropertyType property;
    private Deque<Observer> observers = new ConcurrentLinkedDeque<>();

    private Property(PropertyType property)
    {
        super(property.getName(), property);
        this.property = property;
        this.name = property.getName();
        register();
    }

    @Override
    public final void register()
    {
        super.register();
        MBeanServer pbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            pbs.addNotificationListener(objectName, this, null, null);
        }
        catch (InstanceNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static Property getInstance(PropertyType property)
    {
        Class<?> type = getType(property);
        switch (type.getSimpleName())
        {
            case "int":
            case "long":
            case "float":
            case "double":
                return new DoubleProperty(property);
            case "char":
            case "String":
            case "CharSequence":
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
        advertise(observer);
        observers.add(observer);
    }

    private void advertise(Observer observer)
    {
        String description = I18n.get().getString(name);
        UnitType unit = getUnit();
        observer.fireEvent(
            JSONBuilder
                .object()
                .string("name", this::getName)
                .string("title", ()->description)
                .string("unit", unit::getUnit)
                .number("history", this::getHistoryMillis)
                .number("min", this::getMin)
                .number("max", this::getMax)
        );
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
        return name;
    }

    public String getProperty()
    {
        return property.getSource()!=null?property.getSource():property.getName();
    }
    
    public double getMin()
    {
        return Primitives.getDouble(property.getMin(), -Double.MAX_VALUE);
    }

    public double getMax()
    {
        return Primitives.getDouble(property.getMax(), Double.MAX_VALUE);
    }

    public String getFormat()
    {
        return property.getFormat();
    }

    public UnitType getUnit()
    {
        String unit = property.getUnit();
        if (unit != null && !unit.isEmpty())
        {
            try
            {
                return UnitType.valueOf(unit);
            }
            catch (IllegalArgumentException ex)
            {
                throw new IllegalArgumentException(unit+" illegal");
            }
        }
        return NMEAProperties.getInstance().getUnit(getProperty());
    }

    public int getDecimals()
    {
        Integer decimals = property.getDecimals();
        if (decimals != null)
        {
            return decimals;
        }
        UnitType unit = getUnit();
        UnitCategory category = unit.getCategory();
        switch (category)
        {
            case PLANE_ANGLE:
                return 0;
            default:
                Class<?> type = getType();
                switch (type.getSimpleName())
                {
                    case "float":
                        return 1;
                    case "double":
                        return 3;
                    default:
                        return 0;
                }
        }
    }

    public Class<?> getType()
    {
        return getType(property);
    }
    public static Class<?> getType(PropertyType property)
    {
        NMEAProperties p = NMEAProperties.getInstance();
        if (p.isProperty(property.getName()))
        {
            return p.getType(property.getName());
        }
        else
        {
            return p.getType(property.getSource());
        }
    }
    public long getAverageMillis()
    {
        return getMillis(property.getAverage());
    }

    public long getPeriodMillis()
    {
        return getMillis(property.getPeriod());
    }

    public long getHistoryMillis()
    {
        return getMillis(property.getHistory());
    }
    
    private long getMillis(String text)
    {
        if (text != null && !text.isEmpty())
        {
            return (long) DURATION_MILLI_SECONDS.parse(text);
        }
        else
        {
            return 0;
        }
    }

    @Override
    public void handleNotification(Notification notification, Object handback)
    {
        init();
        observers.forEach((o)->advertise(o));
    }

    protected void init()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(Property o)
    {
        return name.compareTo(o.name);
    }
    private static class DoubleProperty extends Property
    {
        private DoubleTimeoutSlidingSeries history;
        private TimeValueConsumer func;
        
        public DoubleProperty(PropertyType property)
        {
            super(property);
            init();
        }

        @Override
        protected void init()
        {
            TimeValueConsumer f = (t,v)->accept(t, v);
            long periodMillis = getPeriodMillis();
            if (periodMillis > 0)
            {
                long per = periodMillis;
                f = new Delay(per, f);
            }
            long averageMillis = getAverageMillis();
            if (averageMillis > 0)
            {
                TimeValueConsumer oldFunc = f;
                UnitCategory category = getUnit().getCategory();
                if (PLANE_ANGLE == category)
                {
                    TimeoutSlidingAngleAverage ave = new TimeoutSlidingAngleAverage(8, averageMillis);
                    f = (t,v)->
                    {
                        ave.accept(v);
                        oldFunc.accept(t, ave.fast());
                    };
                }
                else
                {
                    DoubleTimeoutSlidingAverage ave = new DoubleTimeoutSlidingAverage(8, averageMillis);
                    f = (t,v)->
                    {
                        ave.accept(v, t);
                        oldFunc.accept(t, ave.fast());
                    };
                }
            }
            long historyMillis = getHistoryMillis();
            if (historyMillis > 0)
            {
                this.history = new DoubleTimeoutSlidingSeries(256, historyMillis);
            }
            else
            {
                this.history = null;
            }
            func = f;
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
                observer.fireEvent(
                    JSONBuilder
                        .object()
                        .numberArray("historyData", history::pointStream)
                );
            }
        }

    }
    private static class ObjectProperty extends Property
    {
        private TimeToLiveList<String> history;
        public ObjectProperty(PropertyType property)
        {
            super(property);
            init();
            long historyMinutes = getHistoryMillis();
            if (historyMinutes > 0)
            {
                this.history = new TimeToLiveList(historyMinutes, MILLISECONDS);
            }
        }

        @Override
        protected void init()
        {
            long historyMinutes = getHistoryMillis();
            if (historyMinutes > 0)
            {
                this.history = new TimeToLiveList(historyMinutes, MILLISECONDS);
            }
            else
            {
                this.history = null;
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
                List<String> list = new ArrayList<>();
                history.forEach((t, d)->
                {
                    list.add(String.valueOf(t));
                    list.add(d);
                });
                observer.fireEvent(
                    JSONBuilder
                    .object()
                    .stringArray("historyData", list::stream)
                );
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
