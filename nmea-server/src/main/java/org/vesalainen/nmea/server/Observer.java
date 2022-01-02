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

import java.util.Locale;
import org.vesalainen.math.UnitType;
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
    protected final SseHandler sseHandler;
    private String last;

    private Observer(String event, Property property, SseHandler sseHandler)
    {
        this.event = event;
        this.property = property;
        this.sseHandler = sseHandler;
    }
    
    public static Observer getInstance(String event, Property property, String unit, String decimals, SseHandler sseHandler)
    {
        NMEAProperties p = NMEAProperties.getInstance();
        Class<?> type = p.getType(property.getName());
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

    public boolean accept(long time, double arg)
    {
        throw new UnsupportedOperationException("not supported");
    }
    
    public boolean accept(long time, String arg)
    {
        if (!arg.equals(last))
        {
            boolean succeeded = sseHandler.fireEvent(event, "{\"time\": \""+time+"\", \"value\": \""+arg+"\"}");
            last = arg;
            return succeeded;
        }
        return true;
    }
    
    public static class DoubleObserver extends Observer
    {
        private UnitType from;
        private UnitType to;
        private int decimals;
        private String format;
        
        public DoubleObserver(String event, Property property, String unit, String decimals, SseHandler sseHandler)
        {
            super(event, property, sseHandler);
            this.from = NMEAProperties.getInstance().getUnit(property.getName());
            this.to = unit!=null?UnitType.valueOf(unit):property.getUnit();
            this.decimals = decimals!=null?Integer.parseInt(decimals):property.getDecimals();
            this.format = String.format("%%.%df", this.decimals);
        }

        @Override
        public boolean accept(long time, double arg)
        {
            return accept(time, String.format(Locale.US, format, arg));
        }

    }
}
