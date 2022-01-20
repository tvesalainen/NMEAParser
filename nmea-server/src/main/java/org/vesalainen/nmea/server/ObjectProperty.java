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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.vesalainen.json.JSONBuilder;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.util.TimeToLiveList;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ObjectProperty extends Property
{
    
    protected TimeToLiveList<Object> history;

    public ObjectProperty(CachedScheduledThreadPool executor, PropertyType property)
    {
        super(executor, property);
        init();
    }

    public ObjectProperty(CachedScheduledThreadPool executor, PropertyType property, String... sources)
    {
        super(executor, property, sources);
        init();
    }

    @Override
    protected final void init()
    {
        long historyMinutes = getHistoryMillis();
        if (historyMinutes > 0)
        {
            this.history = new TimeToLiveList(historyMinutes, TimeUnit.MILLISECONDS);
        }
        else
        {
            this.history = null;
        }
    }

    @Override
    public <T> void set(String property, long time, T arg)
    {
        super.set(property, time, arg);
        if (history != null)
        {
            history.add(time, (T) arg);
        }
    }

    @Override
    public void attach(Observer observer)
    {
        super.attach(observer);
        if (history != null)
        {
            List<Object> list = new ArrayList<>();
            history.forEach((t, d) ->
            {
                list.add(String.valueOf(t));
                list.add(d);
            });
            observer.fireEvent(JSONBuilder.object().objectArray("historyData", list::stream));
        }
    }
    
}
