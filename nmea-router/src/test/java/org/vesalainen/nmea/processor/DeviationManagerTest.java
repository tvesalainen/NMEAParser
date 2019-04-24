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
package org.vesalainen.nmea.processor;

import org.vesalainen.nmea.processor.DeviationManager;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import org.junit.Test;
import org.vesalainen.nmea.processor.DeviationManager;
import static org.junit.Assert.*;
import org.vesalainen.util.CharSequences;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DeviationManagerTest
{
    
    public DeviationManagerTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        DeviationManager dm = new DeviationManager(Paths.get("c:\\temp\\deviation.txt"), 10);
        assertEquals("$HCHDT,260,T*33\r\n", get(dm.getHDT(270)));
        dm.setDeviation090("-10");
        assertEquals("-10.0", dm.getDeviation090());
        assertEquals("$HCHDT,70,T*00\r\n", get(dm.getHDT(90)));
        dm.rotate(10);
        assertEquals("$HCHDT,80,T*0F\r\n", get(dm.getHDT(90)));
    }
    private String get(ByteBuffer bb)
    {
        return CharSequences.getAsciiCharSequence(bb).toString();
    }
    //@Test
    public void generate()
    {
        for (int ii=0;ii<36;ii++)
        {
            System.err.println("public String getDeviation"+ii+"0() {return getDeviation("+ii+"0);}");
            System.err.println("public void setDeviation"+ii+"0(String deviation) {setDeviation("+ii+"0, deviation);}");
        }
    }
    
}
