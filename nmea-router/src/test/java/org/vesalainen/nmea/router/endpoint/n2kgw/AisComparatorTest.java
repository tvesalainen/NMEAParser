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
package org.vesalainen.nmea.router.endpoint.n2kgw;

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.parsers.nmea.ais.AISContentHelper;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AisComparatorTest
{
    
    public AisComparatorTest()
    {
    }

    @Test
    public void test()
    {
        AISContentHelper h1 = new AISContentHelper("!AIVDM,1,1,,A,18I>TH1000dV>aMbRgE1hbF62d0`,0*3A");
        AISContentHelper h2 = new AISContentHelper("!AIVDM,1,1,,A,18I>TH1wh0dV>aMbRgE1hbF62d0`,0*25");
        int i1 = h1.getInt(42, 49);
        int i2 = h2.getInt(42, 49);
    }
    
}
