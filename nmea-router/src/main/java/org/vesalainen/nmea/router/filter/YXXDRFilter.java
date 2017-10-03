/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router.filter;

import org.vesalainen.lang.Primitives;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class YXXDRFilter extends AbstractNMEAFilter
{

    @Override
    protected Cond acceptField(CharSequence cs, int index, int begin, int end)
    {
        switch (index)
        {
            case 0:
                if (!equals("$YXXDR", cs, begin, end))
                {
                    return Cond.Accept;
                }
                break;
            case 4:
            case 8:
            case 12:
                if (
                        equals("RRAT", cs, begin, end) ||
                        equals("PRAT", cs, begin, end) ||
                        equals("YRAT", cs, begin, end) ||
                        equals("RRTR", cs, begin, end) ||
                        equals("PRTR", cs, begin, end) ||
                        equals("YRTR", cs, begin, end)
                        )
                {
                    return Cond.Reject;
                }
                break;
        }
        return Cond.GoOn;
    }
    
}
