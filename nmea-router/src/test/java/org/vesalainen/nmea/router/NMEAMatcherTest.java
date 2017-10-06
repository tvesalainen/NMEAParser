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
package org.vesalainen.nmea.router;

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.Matcher.Status;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEAMatcherTest
{
    
    public NMEAMatcherTest()
    {
    }

    @Test
    public void test1()
    {
        String sentence = "$IIHDG,171,,,06,E*13\r\n";
        NMEAMatcher nm = new NMEAMatcher();
        nm.addExpression("$IIHDG", new OldRoute());
        nm.compile();
        Status status = null;
        for (int ii=0;ii<sentence.length();ii++)
        {
            status = nm.match(sentence.charAt(ii));
            assertFalse(status == Status.Error);
        }
        assertEquals(Status.Match, status);
        assertEquals(0, nm.getErrors());
        assertEquals(1, nm.getMatches());
        assertEquals(0F, nm.getErrorPrecent(), 1e-8);
    }
    
    @Test
    public void test2()
    {
        String sentence = "$IIHDG,171,,,06,E*23\r\n$IIHDG,171,,,06,E*13\r\n$IIHDG,171,,,06,E*14\r\n";
        NMEAMatcher nm = new NMEAMatcher();
        nm.addExpression("$IIHDG", new OldRoute());
        nm.compile();
        for (int ii=0;ii<sentence.length();ii++)
        {
            nm.match(sentence.charAt(ii));
        }
        assertEquals(2, nm.getErrors());
        assertEquals(1, nm.getMatches());
        assertEquals(200F, nm.getErrorPrecent(), 1e-8);
    }
    
}
