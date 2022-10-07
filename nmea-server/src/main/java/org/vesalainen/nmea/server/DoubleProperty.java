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

import java.util.function.DoubleFunction;
import org.vesalainen.json.JSONBuilder;
import org.vesalainen.math.UnitCategory;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingAverage;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingMax;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingMin;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingSeries;
import org.vesalainen.math.sliding.TimeoutSlidingAngleAverage;
import org.vesalainen.nmea.server.Observer.DoubleObserver;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleProperty extends Property
{
    
    protected DoubleTimeoutSlidingSeries history;
    protected TimePropertyConsumer func;
    private DoubleTimeoutSlidingAverage ave;
    private DoubleTimeoutSlidingMin min;
    private DoubleTimeoutSlidingMax max;

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
        TimePropertyConsumer f = (p, t, v) -> super.set(p, t, v);
        long historyMillis = getHistoryMillis();
        if (historyMillis > 0)
        {
            double delta = 1.0/Math.pow(10, getDecimals());
            this.history = new DoubleTimeoutSlidingSeries(System::currentTimeMillis, 256, historyMillis, (l)->l, (v1,v2)->Math.abs(v1-v2)<delta);
            TimePropertyConsumer oldFunc = f;
            f = (p, t, v) ->
            {
                oldFunc.accept(p, t, v);
                history.accept(v, t);
            };
        }
        long periodMillis = getPeriodMillis();
        if (periodMillis > 0)
        {
            long per = periodMillis;
            f = new Delay(per, f);
        }
        long averageMillis = getAverageMillis();
        if (averageMillis > 0)
        {
            TimePropertyConsumer oldFunc = f;
            UnitCategory category = getUnit().getCategory();
            if (UnitCategory.PLANE_ANGLE == category)
            {
                TimeoutSlidingAngleAverage ave = new TimeoutSlidingAngleAverage(8, averageMillis);
                f = (p, t, v) ->
                {
                    ave.accept(v);
                    oldFunc.accept(p, t, ave.fast());
                };
            }
            else
            {
                ave = new DoubleTimeoutSlidingAverage(8, averageMillis);
                f = (p, t, v) ->
                {
                    ave.accept(v, t);
                    oldFunc.accept(p, t, ave.fast());
                };
            }
        }
        if (historyMillis > 0)
        {
            this.min = new DoubleTimeoutSlidingMin(8, historyMillis);
            this.max = new DoubleTimeoutSlidingMax(8, historyMillis);
            TimePropertyConsumer oldFunc = f;
            f = (p, t, v) ->
            {
                oldFunc.accept(p, t, v);
                min.accept(v, t);
                max.accept(v, t);
                super.set("min", t, min.getMin());
                super.set("max", t, max.getMax());
            };
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
        func.accept(property, time, arg);
    }

    @Override
    public void attach(Observer observer)
    {
        super.attach(observer);
        if (history != null)
        {
            DoubleObserver dob = (DoubleObserver) observer;
            DoubleFunction<String> format = dob.getFormat();
            JSONBuilder.Obj<?> obj = JSONBuilder.object();
            JSONBuilder.Array<JSONBuilder.Obj> array = obj.array("historyData");
            Ref prev = new Ref();
            long timeOffset = observer.getTimeOffset();
            history.forEach((long t,double v)->
            {
                String value = format.apply(v);
                if (!value.equals(prev.ref))
                {
                    array.number(()->(t-timeOffset)/1000);
                    array.number(()->Double.valueOf(value));
                    prev.ref = value;
                }
            });
            observer.fireEvent(obj);
        }
    }
    
    private class Ref
    {
        String ref;
    }
}
