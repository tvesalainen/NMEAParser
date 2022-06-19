/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISVendors
{
    private static final Map<String,String> map = new HashMap<>();
    static
    {
        populate();
    }
    private static void populate()
    {
        try (
            InputStream is = AISVendors.class.getClassLoader().getResourceAsStream(AISVendors.class.getCanonicalName().replace('.', '/')+".txt");
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr)
                )
        {
            String line = br.readLine();
            while (line != null)
            {
                String[] split = line.split(" ", 2);
                if (split.length == 2)
                {
                    map.put(split[0], split[1]);
                }
                line = br.readLine();
            }
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
    public static String getVendor(String code)
    {
        String vendor = map.get(code);
        if (vendor != null)
        {
            return vendor;
        }
        else
        {
            return "???";
        }
    }
}
