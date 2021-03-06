/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea.ais.areanotice;

import org.vesalainen.lang.Primitives;
import org.vesalainen.parsers.nmea.ais.AISBuilder;
import org.vesalainen.parsers.nmea.ais.AISUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AssociatedText extends Area
{
    
    private final int type;
    private final String text;

    public AssociatedText(String text)
    {
        this.type = 5;
        this.text = text;
    }

    public AssociatedText(CharSequence seq)
    {
        this.type = Primitives.parseInt(seq, 2, 0, 3);
        this.text = AISUtil.makeString(seq, 3, 87);
    }

    @Override
    public void build(AISBuilder builder)
    {
        builder.integer(3, type).string(84, text).spare(3);
    }

    public String getText()
    {
        return text;
    }
    
}
