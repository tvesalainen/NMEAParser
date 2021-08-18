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

import java.util.HashSet;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.util.ThreadSafe;

/**
 *
 * TODO move to org.vesalainen.can
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TransactionalPropertyStoreSignalCompiler implements SignalCompiler
{
    private final AnnotatedPropertyStoreSignalCompiler compiler;
    protected ThreadSafe<Set<String>> updated = new ThreadSafe<>(HashSet::new);

    public TransactionalPropertyStoreSignalCompiler(AnnotatedPropertyStore store)
    {
        this.compiler = new AnnotatedPropertyStoreSignalCompiler(store);
    }

    public AnnotatedPropertyStoreSignalCompiler addSetter(int canId, String source, String target)
    {
        return compiler.addSetter(canId, source, target);
    }

    public AnnotatedPropertyStoreSignalCompiler addPgnSetter(int pgn, String source, String target)
    {
        return compiler.addPgnSetter(pgn, source, target);
    }

    @Override
    public boolean needCompilation(int canId)
    {
        return compiler.needCompilation(canId);
    }
    

    @Override
    public Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier)
    {
        Runnable act = compiler.compile(mc, sc, supplier);
        if (act != null)
        {
            String name = sc.getName();
            String param = compiler.ctx.get().signals.get(name);
            return ()->
            {
                act.run();
                updated.get().add(param);
            };
        }
        else
        {
            return null;
        }
    }

    @Override
    public Runnable compile(MessageClass mc, SignalClass sc, DoubleSupplier supplier)
    {
        Runnable act = compiler.compile(mc, sc, supplier);
        if (act != null)
        {
            String name = sc.getName();
            String param = compiler.ctx.get().signals.get(name);
            return ()->
            {
                act.run();
                updated.get().add(param);
            };
        }
        else
        {
            return null;
        }
    }

    @Override
    public Runnable compile(MessageClass mc, SignalClass sc, Supplier<String> ss)
    {
        Runnable act = compiler.compile(mc, sc, ss);
        if (act != null)
        {
            String name = sc.getName();
            String param = compiler.ctx.get().signals.get(name);
            return ()->
            {
                act.run();
                updated.get().add(param);
            };
        }
        else
        {
            return null;
        }
    }

    @Override
    public Runnable compile(MessageClass mc, SignalClass sc, LongSupplier supplier)
    {
        Runnable act = compiler.compile(mc, sc, supplier);
        if (act != null)
        {
            String name = sc.getName();
            String param = compiler.ctx.get().signals.get(name);
            return ()->
            {
                act.run();
                updated.get().add(param);
            };
        }
        else
        {
            return null;
        }
    }

    @Override
    public Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier, IntFunction<String> map)
    {
        Runnable act = compiler.compile(mc, sc, supplier, map);
        if (act != null)
        {
            String name = sc.getName();
            String param = compiler.ctx.get().signals.get(name);
            return ()->
            {
                act.run();
                updated.get().add(param);
            };
        }
        else
        {
            return null;
        }
    }

    @Override
    public Runnable compileBegin(MessageClass mc)
    {
        Runnable act = compiler.compileBegin(mc);
        Runnable begin = ()->
        {
            updated.get().clear();
            compiler.store.begin(null);
        };
        if (act != null)
        {
            return ()->
            {
                begin.run();
                act.run();
            };
        }
        else
        {
            return begin;
        }
    }

    @Override
    public Runnable compileEnd(MessageClass mc)
    {
        Runnable act = compiler.compileEnd(mc);
        Runnable commit = ()->
        {
            compiler.store.commit(null, updated.get());
        };
        if (act != null)
        {
            return ()->
            {
                act.run();
                commit.run();
            };
        }
        else
        {
            return commit;
        }
    }
    
}
