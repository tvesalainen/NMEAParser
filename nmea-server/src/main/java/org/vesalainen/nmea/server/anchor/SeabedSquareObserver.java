/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.server.anchor;

import java.util.Locale;
import static java.util.Locale.US;
import org.vesalainen.json.JSONBuilder;
import org.vesalainen.json.JSONBuilder.Obj;
import org.vesalainen.json.SseWriter;
import org.vesalainen.nmea.server.Observer;
import org.vesalainen.nmea.server.Property;
import org.vesalainen.nmea.server.SseServlet;
import static org.vesalainen.nmea.server.anchor.SeabedSquareProperty.getObj;
import org.vesalainen.nmea.server.anchor.SeabedSurveyor;
import org.vesalainen.nmea.server.anchor.SeabedSurveyor.Square;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeabedSquareObserver extends Observer<Square>
{
    private String last;
    
    public SeabedSquareObserver(String event, Property property, SseServlet.SseReference sseReference, Locale locale)
    {
        super(event, property, sseReference, locale);
    }

    @Override
    public boolean accept(String property, long time, Square square)
    {
        SseServlet.SseHandler sse = sseReference.getHandler();
        if (sse != null)
        {
            String depth = String.format(US, "%.1f", square.getStandardDepth());
            if (!depth.equals(last))
            {
                sse.fireEvent(new SseWriter(event,
                    JSONBuilder
                        .object()
                        .value("name", ()->property)
                        .number("time", ()->time)
                        .object("value", getObj(square))));
                last = depth;
            }
            return true;
        }
        else
        {
            return false;
        }
    }

}
