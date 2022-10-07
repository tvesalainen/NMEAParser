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
package org.vesalainen.nmea.server.anchor;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.vesalainen.management.AbstractDynamicMBean;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractChainedState<T,U> extends AbstractDynamicMBean
{
    protected enum Action {NEUTRAL, FORWARD, FAIL};
    private AbstractChainedState<T,U> nextState;
    protected U reason;
    protected String description;

    protected AbstractChainedState(String description)
    {
        super(description);
        this.description = description;
        addAttributes(this);
        register();
    }

    @Override
    protected ObjectName createObjectName() throws MalformedObjectNameException
    {
        return new ObjectName(getClass().getName(), "Type", description);
    }

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
        unregister();
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
