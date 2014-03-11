/*
 * Copyright (C) 2014 tkv
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
import java.io.StringBufferInputStream;
import java.io.StringReader;

/**
 *
 * @author tkv
 */
public class AisContentDumper
{
    public static void dump(String content) throws IOException
    {
        StringBufferInputStream sbis = new StringBufferInputStream(content);
        AISInputStream ais = new AISInputStream(sbis);
        int cc = ais.read();
        int count = 1;
        while (cc != -1)
        {
            System.err.println(count+": "+(char)cc);
            cc = ais.read();
            count++;
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            dump("H39T18P4pdG:222222222222220\n");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
}
