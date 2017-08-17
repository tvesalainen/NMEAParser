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
package org.vesalainen.parsers.nmea.charset;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEACharsetProvider extends CharsetProvider
{
    private static final NMEACharset nmeaCharset = new NMEACharset();
    private static final List<Charset> list = new ArrayList<>();
    static
    {
        list.add(nmeaCharset);
    }
    @Override
    public Iterator<Charset> charsets()
    {
        return list.iterator();
    }

    @Override
    public Charset charsetForName(String charsetName)
    {
        switch (charsetName)
        {
            case "NMEA":
                return nmeaCharset;
            default:
                return null;
        }
    }
    
}
