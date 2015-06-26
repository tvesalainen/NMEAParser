/*
 * Copyright (C) 2015 tkv
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

import java.util.zip.Checksum;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.util.Matcher;
import org.vesalainen.util.SimpleMatcher;

/**
 *
 * @author tkv
 */
public class NMEAMatcher implements Matcher
{
    enum State {Prefix, Data, Checksum1, Checksum2, Cr, Lf};
    private State state = State.Prefix;
    private final SimpleMatcher prefixMatcher;
    private final Checksum checksum;
    private int cs;
    private float matches;
    private boolean parsing;
    private float errors;

    public NMEAMatcher(String prefix)
    {
        this.prefixMatcher = new SimpleMatcher(prefix);
        this.checksum = new NMEAChecksum();
    }

    public String getPrefix()
    {
        return prefixMatcher.getExpression();
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
                Status status = prefixMatcher.match(cc);
                switch (status)
                {
                    case Match:
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
                        return Status.WillMatch;
                }
            case Checksum1:
                if ((cs>>4) != parseHex(cc))
                {
                    return error();
                }
                state = State.Checksum2;
                return Status.WillMatch;
            case Checksum2:
                if ((cs&0xf) != parseHex(cc))
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

    private Status error()
    {
        if (parsing)
        {
            errors++;
        }
        clear();
        return Status.Error;
    }
    
    private final int parseHex(int cc)
    {
        switch (cc)
        {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return cc-'0';
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return cc-'a'+10;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return cc-'A'+10;
            default:
                return -1;
        }
    }
    @Override
    public void clear()
    {
        prefixMatcher.clear();
        state = State.Prefix;
        parsing = false;
    }
    
}
