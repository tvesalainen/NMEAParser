/*
 * Copyright (C) 2015 tkv
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

import java.io.UnsupportedEncodingException;
import org.vesalainen.parsers.nmea.charset.NMEACharset;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class NMEACharsetTest
{
    
    public NMEACharsetTest()
    {
    }

    @Test
    public void test1()
    {
        try
        {
            String exp = "ÅKE RÄÄKYY ÖISIN";
            byte[] bytes = exp.getBytes("NMEA");
            String got = new String(bytes, "NMEA");
            assertEquals(exp, got);
            String us = new String(bytes, StandardCharsets.US_ASCII);
            for (int ii=0;ii<us.length();ii++)
            {
                char c = us.charAt(ii);
                switch (c)
                {
                    case '^':
                    case ' ':
                        break;
                    default:
                        assertTrue(Character.isUpperCase(c) || Character.isDigit(c));
                        break;
                }
            }
        }
        catch (UnsupportedEncodingException ex)
        {
            fail(ex.getMessage());
        }
    }
    
}
