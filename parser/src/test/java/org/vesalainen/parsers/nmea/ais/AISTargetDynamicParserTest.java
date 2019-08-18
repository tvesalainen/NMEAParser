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
package org.vesalainen.parsers.nmea.ais;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISTargetDynamicParserTest
{
    
    public AISTargetDynamicParserTest()
    {
    }

    @Test
    public void test1()
    {
        AISTargetDynamicParser parser = AISTargetDynamicParser.PARSER;
        AISTargetDynamic atd = parser.parse("#Msg  Timestamp Location Course Speed Heading Channel Assigned");
        assertNull(atd);
        atd = parser.parse("Msg18 2019-07-14T21:10:54.127Z 16°3.49'S 145°37.26'W 42.0 0.2 351 - false");
        assertEquals(MessageTypes.StandardClassBCSPositionReport, atd.getMessageType());
        assertEquals(Instant.parse("2019-07-14T21:10:54.127Z").toEpochMilli(), atd.getTimestamp());
        assertEquals(-16-3.49/60, atd.getLatitude(), 1e-8);
        assertEquals(-145-37.26/60, atd.getLongitude(), 1e-6);
        assertEquals(42F, atd.getCourse(), 1e-10F);
        assertEquals(0.2F, atd.getSpeed(), 1e-10F);
        assertEquals(351, atd.getHeading());
        assertEquals('-', atd.getChannel());
        assertFalse(atd.isAssignedMode());
    }
    @Test
    public void test2()
    {
        Path path = Paths.get("src\\test\\resources\\230123250.log");
        AISTargetDynamicParser parser = AISTargetDynamicParser.PARSER;
        parser.parse(path, (t)->System.err.println(t));
    }
}
