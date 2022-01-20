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
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.vesalainen.json.JSONBuilder;
import org.vesalainen.lang.Primitives;
import org.vesalainen.management.AbstractDynamicMBean;
import org.vesalainen.math.UnitCategory;
import static org.vesalainen.math.UnitCategory.*;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import web.I18n;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Property extends AbstractDynamicMBean implements NotificationListener, Comparable<Property>
{
    protected final CachedScheduledThreadPool executor;
    protected final String name;
    protected final PropertyType property;
    protected Deque<Observer> observers = new ConcurrentLinkedDeque<>();
    private final String[] sources;

    protected Property(CachedScheduledThreadPool executor, PropertyType property)
    {
        this(executor, property, property.getSource()!=null?new String[]{property.getSource()}:new String[]{});
    }
    protected Property(CachedScheduledThreadPool executor, PropertyType property, String... sources)
    {
        super(property.getName(), property);
        this.executor = executor;
        this.property = property;
        this.name = property.getName();
        this.sources = sources;
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

    public static Property getInstance(CachedScheduledThreadPool executor, PropertyType property)
    {
        switch (property.getName())
        {
            case "message":
                return new MessageProperty(executor, property);
            case "estimatedTimeOfArrival":
                return new ETAProperty(executor, property);
            case "dayPhase":
                return new DayPhaseProperty(executor, property);
            default:
                Class<?> type = getType(property);
                switch (type.getSimpleName())
                {
                    case "int":
                    case "long":
                    case "float":
                    case "double":
                        return new DoubleProperty(executor, property);
                    default:
                        return new ObjectProperty(executor, property);
                }
        }
    }

    public void attach(Observer observer)
    {
        advertise(observer);
        observers.add(observer);
    }

    protected void advertise(Observer observer)
    {
        String description = I18n.get(observer.getLocale()).getString(name);
        UnitType unit = getUnit();
        observer.fireEvent(
            JSONBuilder
                .object()
                .value("name", this::getName)
                .value("title", ()->description)
                .value("unit", unit::getUnit)
                .number("history", this::getHistoryMillis)
                .number("min", this::getMin)
                .number("max", this::getMax)
        );
    }
    public <T> void set(String property, long time, double value)
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
    public <T> void set(String property, long time, T value)
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
    
    @Override
    protected ObjectName createObjectName() throws MalformedObjectNameException
    {
        return ObjectName.getInstance(Config.class.getName(), "Property", property.getName());
    }

    public String[] getSources()
    {
        return sources;
    }
    
    public String getName()
    {
        return name;
    }
    /**
     * Returns concrete property name
     * @return 
     */
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
}
