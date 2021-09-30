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
package org.vesalainen.nmea.processor.n2kgw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongSupplier;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.util.ArrayHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Priorizer
{
    private final IntFunction<Runnable> pgnMap;
    private final IntBinaryOperator comparator;
    private int last;
    private int lastRun;
    private long lastTime;
    private Runnable lastAction;
    private final long timeout;
    private final LongSupplier millis;

    private Priorizer(IntFunction<Runnable> pgnMap, IntBinaryOperator comparator, long timeout, LongSupplier millis)
    {
        this.pgnMap = pgnMap;
        this.comparator = comparator;
        this.timeout = timeout;
        this.millis = millis;
    }
    
    private void fire(int canId)
    {
        long now = millis.getAsLong();
        if (now > lastTime+timeout)
        {
            lastAction = pgnMap.apply(PGN.pgn(canId));
            lastAction.run();
            lastRun = canId;
            lastTime = now;
        }
        else
        {
            if (lastRun == canId || last == canId)
            {
                lastAction.run();
                lastRun = canId;
                lastTime = now;
            }
            else
            {
                Runnable act = pgnMap.apply(PGN.pgn(canId));
                if (act == null)
                {
                    throw new IllegalArgumentException(canId+" not allowed");
                }
                if (comparator.applyAsInt(canId, last) < 0)
                {
                    act.run();
                    lastRun = canId;
                    lastTime = now;
                }
                last = canId;
                lastAction = act;
            }
        }
    }

    public static Builder builder()
    {
        return new Builder();
    }
    public static class Builder
    {
        private List<Integer> pgns = new ArrayList<>();
        private List<Runnable> actions = new ArrayList<>();
        private long timeout = 1000;
        private LongSupplier millis = System::currentTimeMillis;
        
        public Builder addPgn(int pgn, Runnable act)
        {
            pgns.add(pgn);
            actions.add(act);
            return this;
        }
        
        public Builder setTimeout(long timeout, TimeUnit unit)
        {
            this.timeout = TimeUnit.MILLISECONDS.convert(timeout, unit);
            return this;
        }
        
        public Builder setMillisSupplier(LongSupplier millis)
        {
            this.millis = millis;
            return this;
        }
        
        public IntConsumer build()
        {
            if (pgns.isEmpty())
            {
                throw new IllegalStateException("no mappings");
            }
            Priorizer priorizer;
            if (pgns.size() > 1)
            {
                int[] arr = new int[pgns.size()];
                for (int ii=0;ii<arr.length;ii++)
                {
                    arr[ii] = pgns.get(ii);
                }
                Comp comp = new Comp(arr);
                Map<Integer,Runnable> map = new HashMap<>();
                for (int ii=0;ii<arr.length;ii++)
                {
                    map.put(pgns.get(ii), actions.get(ii));
                }
                priorizer = new Priorizer(map::get, comp::compare, timeout, millis);
            }
            else
            {
                Runnable act = actions.get(0);
                priorizer = new Priorizer((x)->act, (i,j)->0, timeout, millis);
            }
            return (id)->priorizer.fire(id);
        }
    }
    private static class Comp
    {
        private int[] array;

        public Comp(int[] array)
        {
            this.array = array;
        }

        public int compare(int o1, int o2)
        {
            int i1 = ArrayHelp.indexOf(array, PGN.pgn(o1));
            int i2 = ArrayHelp.indexOf(array, PGN.pgn(o2));
            return i1 - i2;
        }
        
    }
}
