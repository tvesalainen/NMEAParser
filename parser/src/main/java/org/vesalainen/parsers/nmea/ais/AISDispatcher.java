/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea.ais;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.locks.ReentrantLock;
import org.vesalainen.code.InterfaceDispatcher;
import static org.vesalainen.code.InterfaceDispatcher.newInstance;
import org.vesalainen.code.InterfaceDispatcherAnnotation;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@InterfaceDispatcherAnnotation
public abstract class AISDispatcher extends InterfaceDispatcher implements AISObserver
{
    private ReentrantLock lock = new ReentrantLock();
    public static AISDispatcher newInstance()
    {
        return newInstance(AISDispatcher.class);
    }

    public AISDispatcher(MethodHandles.Lookup lookup)
    {
        super(lookup);
    }

    @Override
    public void commit(String reason)
    {
        try
        {
            super.commit(reason);
        }
        finally
        {
            System.err.println("commit "+lock+" "+Thread.currentThread());
            lock.unlock();
        }
    }

    @Override
    public void rollback(String reason)
    {
        try
        {
            super.rollback(reason);
        }
        finally
        {
            System.err.println("rollback "+lock+" "+Thread.currentThread());
            lock.unlock();
        }
    }

    @Override
    public void start(String reason)
    {
        if (!lock.isHeldByCurrentThread())
        {
            System.err.println("start "+lock+" "+Thread.currentThread());
            lock.lock();
        }
        else
        {
            warning("thread was held by current thread!!!");
        }
        super.start(reason);
    }
    
}
