/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.parsers.nmea.MessageType.*;
import static org.vesalainen.parsers.nmea.TalkerId.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEATest
{
    
    public NMEATest()
    {
    }

    @Test
    public void testGetPrefix()
    {
        assertEquals("!AIVDM", NMEA.getPrefix("!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"));
        assertEquals("$PICOA", NMEA.getPrefix("$PICOA,08,90,TXF,*1F\r\n"));
    }
    @Test
    public void testFindMessage()
    {
        String exp = "!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n";
        assertEquals(exp, NMEA.findMessage(exp));
        assertEquals(exp, NMEA.findMessage("*63\r\n!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n$PICOA,08,90,TXF,*1F\r\n"));
    }
    @Test
    public void testGetMessageType()
    {
        assertEquals(VDM, NMEA.getMessageType("!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"));
        assertNull(NMEA.getMessageType("$PICOA,08,90,TXF,*1F\r\n"));
    }
    @Test
    public void testGetTalkerId()
    {
        assertEquals(AI, NMEA.getTalkerId("!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"));
        assertNull(NMEA.getTalkerId("$PICOA,08,90,TXF,*1F\r\n"));
    }
    @Test
    public void testIsProprietary()
    {
        assertTrue(NMEA.isProprietory("$PICOA,08,90,TXF,*1F\r\n"));
        assertFalse(NMEA.isProprietory("!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"));
    }
    @Test
    public void testIsNMEA()
    {
        assertTrue(NMEA.isNMEA("$GPBWC,010003,1248.4128,S,03827.6978,W,338.4,T,1.5,M,0.314,N,BA01,A*61\r\n"));
        assertFalse(NMEA.isNMEA("!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"));
    }
    @Test
    public void testIsAIS()
    {
        assertFalse(NMEA.isAIS("$GPBWC,010003,1248.4128,S,03827.6978,W,338.4,T,1.5,M,0.314,N,BA01,A*61\r\n"));
        assertTrue(NMEA.isAIS("!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"));
    }
    @Test
    public void testIsNMEAOrAIS()
    {
        assertTrue(NMEA.isNMEAOrAIS("$GPBWC,010003,1248.4128,S,03827.6978,W,338.4,T,1.5,M,0.314,N,BA01,A*61\r\n"));
        assertTrue(NMEA.isNMEAOrAIS("!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n"));
    }
    
}
