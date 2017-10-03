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
package org.vesalainen.nmea.router;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Checksum;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.regex.Regex;
import org.vesalainen.regex.WildcardMatcher;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEAMatcher extends WildcardMatcher<Route>
{
    enum State {Prefix, Data, Checksum1, Checksum2, Cr, Lf};
    private State state = State.Prefix;
    private final Checksum checksum;
    private int cs;
    private float matches;
    private boolean parsing;
    private float errors;
    private Route matched;
    private final List<Route> routes = new ArrayList<>();

    public NMEAMatcher()
    {
        this.checksum = new NMEAChecksum();
    }

    public void addNMEAExpression(String expr, Route attach, Regex.Option... options)
    {
        super.addExpression(expr, attach, options);
        routes.add(attach);
    }

    public List<Route> getRoutes()
    {
        return routes;
    }
    
    public int getMatches()
    {
        return (int) matches;
    }

    public int getErrors()
    {
        return (int) errors;
    }
    public float getErrorPrecent()
    {
        if (matches == 0)
        {
            return 0;
        }
        return 100*errors/matches;
    }
    @Override
    public Status match(int cc)
    {
        checksum.update(cc);
        switch (state)
        {
            case Prefix:
                Status status = super.match(cc);
                switch (status)
                {
                    case Match:
                        matched = super.getMatched();
                        state = State.Data;
                        parsing = true;
                        return Status.WillMatch;
                    case Error:
                        return error();
                }
                return status;
            case Data:
                switch (cc)
                {
                    case '*':
                        cs = (int) checksum.getValue();
                        state = State.Checksum1;
                        return Status.WillMatch;
                    case '\r':
                    case '\n':
                    return error();
                    default:
                        if (cc < ' ' || cc > '~')
                        {
                            return error();
                        }
                        return Status.WillMatch;
                }
            case Checksum1:
                if ((cs>>4) != Character.digit(cc, 16))
                {
                    return error();
                }
                state = State.Checksum2;
                return Status.WillMatch;
            case Checksum2:
                if ((cs&0xf) != Character.digit(cc, 16))
                {
                    return error();
                }
                state = State.Cr;
                return Status.WillMatch;
            case Cr:
                if (cc != '\r')
                {
                    return error();
                }
                state = State.Lf;
                return Status.WillMatch;
            case Lf:
                if (cc != '\n')
                {
                    return error();
                }
                clear();
                matches++;
                return Status.Match;
            default:
                throw new IllegalArgumentException(state+" unknown");
        }
    }

    @Override
    public Route getMatched()
    {
        return matched;
    }

    private Status error()
    {
        if (parsing)
        {
            errors++;
        }
        clear();
        return Status.Error;
    }
    
    @Override
    public void clear()
    {
        super.clear();
        state = State.Prefix;
        parsing = false;
    }
    
}
