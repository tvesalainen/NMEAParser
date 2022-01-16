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

import org.vesalainen.json.JSONBuilder;
import org.vesalainen.math.UnitCategory;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingAverage;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingSeries;
import org.vesalainen.math.sliding.TimeValueConsumer;
import org.vesalainen.math.sliding.TimeoutSlidingAngleAverage;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleProperty extends Property
{
    
    protected DoubleTimeoutSlidingSeries history;
    protected TimeValueConsumer func;

    public DoubleProperty(CachedScheduledThreadPool executor, PropertyType property)
    {
        super(executor, property);
        init();
    }

    public DoubleProperty(CachedScheduledThreadPool executor, PropertyType property, String... sources)
    {
        super(executor, property, sources);
        init();
    }

    @Override
    protected final void init()
    {
        TimeValueConsumer f = (t, v) -> accept(null, t, v);
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
            if (UnitCategory.PLANE_ANGLE == category)
            {
                TimeoutSlidingAngleAverage ave = new TimeoutSlidingAngleAverage(8, averageMillis);
                f = (t, v) ->
                {
                    ave.accept(v);
                    oldFunc.accept(t, ave.fast());
                };
            }
            else
            {
                DoubleTimeoutSlidingAverage ave = new DoubleTimeoutSlidingAverage(8, averageMillis);
                f = (t, v) ->
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
    public void set(String property, long time, double arg)
    {
        func.accept(time, arg);
    }

    private void accept(String property, long time, double arg)
    {
        super.set(property, time, arg);
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
            observer.fireEvent(JSONBuilder.object().numberArray("historyData", history::pointStream));
        }
    }
    
}
