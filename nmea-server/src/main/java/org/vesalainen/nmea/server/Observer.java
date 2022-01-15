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

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.function.DoubleFunction;
import org.vesalainen.json.JSONBuilder;
import org.vesalainen.json.JSONBuilder.Element;
import org.vesalainen.json.SseWriter;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.CardinalDirection;
import org.vesalainen.navi.CoordinateFormat;
import org.vesalainen.nmea.server.SseServlet.SseHandler;
import org.vesalainen.parsers.nmea.NMEAProperties;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Observer
{
    protected final String event;
    protected final Property property;
    protected final WeakReference<SseHandler> sseHandler;   // weak reference to not prevent Sse from gc which can happen with not updated property
    private String last;
    private final String name;
    private final SseWriter writer;
    private final SseWriter empty;
    private long time;
    private String value;

    private Observer(String event, Property property, SseHandler sseHandler)
    {
        this.event = event;
        this.property = property;
        this.name = property.getName();
        this.sseHandler = new WeakReference<>(sseHandler);
        this.writer = new SseWriter(event,
            JSONBuilder
                .object()
                .string("name", ()->name)
                .number("time", ()->time)
                .string("value", ()->value));
        this.empty = new SseWriter(event, JSONBuilder.object());
    }
    
    public static Observer getInstance(String event, Property property, String unit, String decimals, SseHandler sseHandler)
    {
        NMEAProperties p = NMEAProperties.getInstance();
        Class<?> type = property.getType();
        switch (type.getSimpleName())
        {
            case "int":
            case "long":
            case "float":
            case "double":
                return new DoubleObserver(event, property, unit, decimals, sseHandler);
            case "char":
            case "String":
                return new Observer(event, property, sseHandler);
            default:
                if (type.isEnum())
                {
                    return new Observer(event, property, sseHandler);
                }
                else
                {
                    throw new UnsupportedOperationException(type+" not supported");
                }
        }
    }

    public boolean fireEvent(Element element)
    {
        SseHandler sse = sseHandler.get();
        if (sse != null)
        {
            return sse.fireEvent(new SseWriter(event, element));
        }
        else
        {
            return false;
        }
    }

    public boolean accept(long time, double arg)
    {
        throw new UnsupportedOperationException("not supported");
    }
    
    public boolean accept(long time, String arg)
    {
        SseHandler sse = sseHandler.get();
        if (sse != null)
        {
            if (!arg.equals(last))
            {
                this.time = time;
                this.value = arg;
                boolean succeeded = sse.fireEvent(writer);
                last = arg;
                return succeeded;
            }
            else
            {
                return sse.fireEvent(empty);
            }
        }
        else
        {
            return false;
        }
    }
    
    public static class DoubleObserver extends Observer
    {
        private DoubleFunction<String> format;
        
        public DoubleObserver(String event, Property property, String unit, String decimals, SseHandler sseHandler)
        {
            super(event, property, sseHandler);
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
                    }
                    String fm = fmt;
                    this.format = (v)->String.format(fm, v);
                    break;
            }
            if (from != to)
            {
                DoubleFunction<String> f = this.format;
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
