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

import java.util.Collection;
import java.util.function.Supplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractChainedState<T,U>
{

    protected enum Action {NEUTRAL, FORWARD, FAIL};
    private AbstractChainedState nextState;
    protected U reason;

    public void input(T input)
    {
        switch (test(input))
        {
            case FORWARD:
            if (nextState == null && hasNext())
            {
                nextState = createNext();
            }
            case NEUTRAL:
            if (nextState != null)
            {
                nextState.input(input);
            }
            break;
            case FAIL:
                fail(reason);
            break;
        }
    }

    protected boolean hasNext() {return false;};
    protected AbstractChainedState<T,U> createNext()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    protected abstract Action test(T input);
    protected void failed(U reason)
    {
        
    }
    private void fail(U reason)
    {
        if (nextState != null)
        {
            nextState.fail(reason);
            nextState = null;
        }
        failed(reason);
    }
}
