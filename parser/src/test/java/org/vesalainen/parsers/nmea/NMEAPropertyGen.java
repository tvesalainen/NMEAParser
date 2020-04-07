/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.text.CamelCase;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEAPropertyGen
{
    
    public NMEAPropertyGen()
    {
    }

    @Test
    public void generate()
    {
        NMEAProperties p = NMEAProperties.getInstance();
        List<String> list = new ArrayList<>(p.getAllProperties());
        list.sort(null);
        list.forEach((property)->
        {
            System.err.println(String.format(Locale.US, "\t%s(NMEACategory.%s, UnitType.%s, %s.class),", 
                    CamelCase.delimitedUpper(property, "_"),
                    p.getCategory(property),
                    p.getUnit(property),
                    p.getType(property).getSimpleName()
                    ));
        });
        
    }
    
}
