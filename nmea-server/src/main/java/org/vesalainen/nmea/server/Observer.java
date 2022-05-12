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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.function.DoubleFunction;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.json.JSONBuilder;
import org.vesalainen.json.JSONBuilder.Element;
import org.vesalainen.json.SseWriter;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.CardinalDirection;
import org.vesalainen.navi.CoordinateFormat;
import org.vesalainen.nmea.server.SseServlet.SseHandler;
import org.vesalainen.nmea.server.SseServlet.SseReference;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Observer extends JavaLogging
{
    protected final String event;
    protected final Property property;
    protected final SseReference sseReference;
    private Object last;
    private final String name;
    private final SseWriter writer;
    private final SseWriter empty;
    private long time;
    private Object value;
    private final Locale locale;

    private Observer(String event, Property property, SseReference sseReference, Locale locale)
    {
        super(Observer.class, property+":"+event);
        this.event = event;
        this.property = property;
        this.name = property.getName();
        this.sseReference = sseReference;
        this.locale = locale;
        this.writer = new SseWriter(event,
            JSONBuilder
                .object()
                .value("name", ()->name)
                .number("time", ()->time)
                .value("value", ()->value));
        this.empty = new SseWriter(event, JSONBuilder.object());
    }
    
    public static Observer getInstance(String event, Property property, String unit, String decimals, SseReference sseReference, Locale locale)
    {
        NMEAProperties p = NMEAProperties.getInstance();
        Class<?> type = property.getType();
        if (type != null)
        {
            switch (type.getSimpleName())
            {
                case "int":
                case "long":
                case "float":
                case "double":
                    return new DoubleObserver(event, property, unit, decimals, sseReference, locale);
            }
        }
        return new Observer(event, property, sseReference, locale);
    }

    public void fireEvent(Element element) throws IOException
    {
        SseHandler sse = sseReference.getHandler();
        if (sse != null)
        {
            sse.fireEvent(new SseWriter(event, element));
        }
    }

    public boolean accept(long time, double arg)
    {
        throw new UnsupportedOperationException("not supported");
    }
    
    public boolean accept(long time, Object arg)
    {
        SseHandler sse = sseReference.getHandler();
        if (sse != null)
        {
            if (!arg.equals(last))
            {
                this.time = time;
                this.value = arg;
                sse.fireEvent(writer);
                last = arg;
            }
            else
            {
                sse.fireEvent(empty);
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    public Locale getLocale()
    {
        return locale;
    }
    
    public static class DoubleObserver extends Observer
    {
        private DoubleFunction<Object> format;
        
        public DoubleObserver(String event, Property property, String unit, String decimals, SseReference sseReference, Locale locale)
        {
            super(event, property, sseReference, locale);
            UnitType  from = property.getUnit();
            UnitType  to = unit!=null?UnitType.valueOf(unit):property.getUnit();
            int dec = decimals!=null?Integer.parseInt(decimals):property.getDecimals();
            switch (to)
            {
                case COORDINATE_DEGREES_LONGITUDE:
                case COORDINATE_DEGREES_AND_MINUTES_LONGITUDE:
                case COORDINATE_DEGREES_MINUTES_SECONDS_LONGITUDE:
                case COORDINATE_DEGREES_LATITUDE:
                case COORDINATE_DEGREES_AND_MINUTES_LATITUDE:
                case COORDINATE_DEGREES_MINUTES_SECONDS_LATITUDE:
                    this.format = (v)->CoordinateFormat.format(Locale.US, v, to);
                    break;
                case CARDINAL_DIRECTION:
                    this.format = (v)->CardinalDirection.cardinal(v).toString();
                    break;
                case INTERCARDINAL_DIRECTION:
                    this.format = (v)->CardinalDirection.interCardinal(v).toString();
                    break;
                case SECONDARY_INTERCARDINAL_DIRECTION:
                    this.format = (v)->CardinalDirection.secondaryInterCardinal(v).toString();
                    break;
                default:
                    String fmt = property.getFormat();
                    if (fmt == null)
                    {
                        fmt = String.format("%%.%df", dec);
                        String fm = fmt;
                        this.format = (v)->String.format(fm, v);
                    }
                    break;
            }
            if (from != to)
            {
                DoubleFunction<Object> f = this.format;
                this.format = (v)->f.apply(from.convertTo(v, to));
            }
        }

        @Override
        public boolean accept(long time, double arg)
        {
            return accept(time, format.apply(arg));
        }

    }
}
