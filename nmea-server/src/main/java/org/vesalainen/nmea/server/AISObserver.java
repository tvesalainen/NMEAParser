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
package org.vesalainen.nmea.server;

import java.util.Locale;
import org.vesalainen.json.JSONBuilder;
import org.vesalainen.json.JSONBuilder.Obj;
import org.vesalainen.json.SseWriter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISObserver extends Observer<Obj>
{
    public AISObserver(String event, Property property, SseServlet.SseReference sseReference, Locale locale)
    {
        super(event, property, sseReference, locale);
    }

    @Override
    public boolean accept(long time, Obj arg)
    {
        SseServlet.SseHandler sse = sseReference.getHandler();
        if (sse != null)
        {
            sse.fireEvent(new SseWriter(event,
                JSONBuilder
                    .object()
                    .value("name", ()->name)
                    .number("time", ()->time)
                    .object("value", arg)));
            return true;
        }
        else
        {
            return false;
        }
    }

}
