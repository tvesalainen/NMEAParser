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
package org.vesalainen.nmea.router.endpoint.n2kgw;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import org.vesalainen.can.AnnotatedPropertyStoreSignalCompiler;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.setter.IntSetter;
import org.vesalainen.code.setter.LongSetter;
import org.vesalainen.parsers.nmea.NMEAPGN;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractNMEACompiler extends AnnotatedPropertyStoreSignalCompiler
{
    
    protected final ReentrantLock lock = new ReentrantLock(true);

    public <T extends AnnotatedPropertyStore> AbstractNMEACompiler(T store)
    {
        super(store);
    }

    public final AnnotatedPropertyStoreSignalCompiler addPgnSetter(NMEAPGN nmeaPgn, String source, String target)
    {
        return addPgnSetter(nmeaPgn.getPGN(), source, target);
    }

    @Override
    public Runnable compileBegin(MessageClass mc, int canId, LongSupplier millisSupplier)
    {
        IntSetter canIdSetter = store.getIntSetter("canId");
        LongSetter millisSetter = store.getLongSetter("millis");
        return () ->
        {
            lock.lock();
            store.begin(null);
            canIdSetter.set(canId);
            millisSetter.set(millisSupplier.getAsLong());
        };
    }

    @Override
    public Consumer<Throwable> compileEnd(MessageClass mc)
    {
        return (ex) ->
        {
            try
            {
                if (ex == null)
                {
                    store.commit(null);
                }
                else
                {
                    store.rollback(ex.getMessage());
                }
            }
            finally
            {
                lock.unlock();
            }
        };
    }
    
}
