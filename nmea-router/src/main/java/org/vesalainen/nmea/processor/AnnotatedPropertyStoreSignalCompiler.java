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
package org.vesalainen.nmea.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.setter.DoubleSetter;
import org.vesalainen.code.setter.FloatSetter;
import org.vesalainen.code.setter.IntSetter;

/**
 * TODO move to org.vesalainen.can
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AnnotatedPropertyStoreSignalCompiler implements SignalCompiler
{
    protected final AnnotatedPropertyStore store;
    protected final Map<Integer,Msg> canIdMap = new HashMap<>();
    protected final Map<Integer,Msg> pgnMap = new HashMap<>();
    protected ThreadLocal<Msg> ctx = new ThreadLocal<>();

    public AnnotatedPropertyStoreSignalCompiler(AnnotatedPropertyStore store)
    {
        this.store = store;
    }

    public AnnotatedPropertyStoreSignalCompiler addSetter(int canId, String source, String target)
    {
        Class<?> type = store.getType(target);
        Msg msg = canIdMap.get(canId);
        if (msg == null)
        {
            msg = new Msg(canId);
            canIdMap.put(canId, msg);
        }
        msg.add(source, target);
        return this;
    }
            
    public AnnotatedPropertyStoreSignalCompiler addPgnSetter(int pgn, String source, String target)
    {
        Class<?> type = store.getType(target);
        Msg msg = pgnMap.get(pgn);
        if (msg == null)
        {
            msg = new Msg(pgn);
            pgnMap.put(pgn, msg);
        }
        msg.add(source, target);
        return this;
    }
            
    @Override
    public boolean needCompilation(int canId)
    {
        Msg msg = canIdMap.get(canId);
        if (msg == null)
        {
            msg = pgnMap.get(PGN.pgn(canId));
        }
        ctx.set(msg);
        return msg != null;
    }

    @Override
    public Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier)
    {
        String name = sc.getName();
        Msg msg = ctx.get();
        if (msg != null && msg.signals.containsKey(name))
        {
            String target = msg.signals.get(name);
            IntSetter setter = store.getIntSetter(target);
            if (setter == null || store.getType(target) != int.class)
            {
                throw new IllegalArgumentException(target+" no setter or wrong type");
            }
            return ()->setter.set(supplier.getAsInt());
        }
        return null;
    }

    @Override
    public Runnable compile(MessageClass mc, SignalClass sc, DoubleSupplier supplier)
    {
        String name = sc.getName();
        Msg msg = ctx.get();
        if (msg != null && msg.signals.containsKey(name))
        {
            String target = msg.signals.get(name);
            Class<?> type = store.getType(target);
            if (type == double.class)
            {
                DoubleSetter setter = store.getDoubleSetter(target);
                if (setter == null)
                {
                    throw new IllegalArgumentException(target+" no setter");
                }
                return ()->setter.set(supplier.getAsDouble());
            }
            else
            {
                if (type == float.class)
                {
                    FloatSetter setter = store.getFloatSetter(target);
                    if (setter == null)
                    {
                        throw new IllegalArgumentException(target+" no setter");
                    }
                    return ()->setter.set(supplier.getAsDouble());
                }
                else
                {
                    throw new IllegalArgumentException(target+" setter not suitable for double");
                }
            }
        }
        return null;
    }

    @Override
    public Runnable compile(MessageClass mc, SignalClass sc, Supplier<String> ss)
    {
        return SignalCompiler.super.compile(mc, sc, ss); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier, IntFunction<String> map)
    {
        return SignalCompiler.super.compile(mc, sc, supplier, map); //To change body of generated methods, choose Tools | Templates.
    }

    protected static class Msg
    {
        protected final int id;
        protected final Map<String,String> signals = new HashMap<>();

        public Msg(int id)
        {
            this.id = id;
        }

        public void add(String source, String target)
        {
            signals.put(source, target);
        }
    }
}
