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
package org.vesalainen.nmea.processor.n2kgw;

import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.IntReference;
import org.vesalainen.util.LongReference;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PriorizerTest
{
    
    public PriorizerTest()
    {
    }

    @Test
    public void test1()
    {
        IntReference ref = new IntReference(0);
        
        IntConsumer ic = Priorizer.builder()
                .setTimeout(1, TimeUnit.DAYS)
                .addPgn(129029, ()->ref.setValue(129029))
                .addPgn(129033, ()->ref.setValue(129033))
                .build();
        ic.accept(234358032);
        assertEquals(129029, ref.getValue());
        ic.accept(234359056);
        assertEquals(129029, ref.getValue());
        ic.accept(234359056);
        assertEquals(129033, ref.getValue());
    }
    @Test
    public void test2()
    {
        IntReference ref = new IntReference(0);
        
        IntConsumer ic = Priorizer.builder()
                .setTimeout(1, TimeUnit.DAYS)
                .addPgn(129029, ()->ref.setValue(129029))
                .build();
        ic.accept(234358032);
        assertEquals(129029, ref.getValue());
        ref.setValue(0);
        ic.accept(234358033);
        assertEquals(0, ref.getValue());
        ic.accept(234358034);
        assertEquals(0, ref.getValue());
        ic.accept(234358032);
        assertEquals(129029, ref.getValue());
    }
    @Test
    public void test3()
    {
        IntReference ref = new IntReference(0);
        LongReference millis = new LongReference(10);
        
        IntConsumer ic = Priorizer.builder()
                .setTimeout(5, TimeUnit.MILLISECONDS)
                .setMillisSupplier(millis::getValue)
                .addPgn(129029, ()->ref.setValue(129029))
                .build();
        ic.accept(234358032);
        assertEquals(129029, ref.getValue());
        ref.setValue(0);
        ic.accept(234358033);
        assertEquals(0, ref.getValue());
        millis.setValue(20);
        ic.accept(234358034);
        assertEquals(129029, ref.getValue());
    }
    
}
