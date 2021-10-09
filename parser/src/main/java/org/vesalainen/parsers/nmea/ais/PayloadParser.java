/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;

/**
 * @deprecated Use PayloadBuilder
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname("org.vesalainen.parsers.nmea.ais.PayloadParserImpl")
@GrammarDef
public abstract class PayloadParser
{
    public static final PayloadParser getInstance()
    {
        return (PayloadParser) GenClassFactory.loadGenInstance(PayloadParser.class);
    }

    @Rule
    protected StringBuilder payload()
    {
        return new StringBuilder();
    }
    @Rule("payload sixBit")
    protected StringBuilder payload(StringBuilder payload, char sixBit)
    {
        payload.append(sixBit);
        return payload;
    }
    @Terminal(expression="[01]{6}", radix=2)
    protected char sixBit(int bits)
    {
        char sixBit = (char) (bits + 48);
        if (bits >= 40)
        {
            sixBit += 8;
        }
        return sixBit;
    }
    
    @ParseMethod(start = "payload")
    public abstract StringBuilder parse(CharSequence txt);
}
