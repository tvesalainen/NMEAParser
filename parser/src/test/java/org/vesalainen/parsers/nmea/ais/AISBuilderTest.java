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

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.NMEASentence;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISBuilderTest
{
    
    public AISBuilderTest()
    {
    }

    @Test
    public void testInteger()
    {
        AISBuilder builder = new AISBuilder();
        builder.integer(3, 2);
        assertEquals("010", builder.bits());
        builder.integer(3, -2);
        assertEquals("010110", builder.bits());
    }
    @Test
    public void testString()
    {
        AISBuilder builder = new AISBuilder();
        String txt = "IIRIS 3231";
        builder.string(120, txt);
        assertEquals(txt, AISUtil.makeString(builder.bits()));
    }
    @Test
    public void test24A() throws IOException
    {
        NMEASentence[] sentences = new AISBuilder(MessageTypes.StaticDataReport, 230123250)
                .integer(2, 0)
                .string(120, "IIRIS")
                .spare(8)
                .build();
        assertEquals(1, sentences.length);
        NMEASentence sentence = sentences[0];
        NMEAParser parser = NMEAParser.newInstance();
        TC tc = new TC();
        parser.parse(sentence.toString(), null, tc);
        assertEquals(230123250, tc.mmsi);
        assertEquals("IIRIS", tc.shipname);
    }
    
}
