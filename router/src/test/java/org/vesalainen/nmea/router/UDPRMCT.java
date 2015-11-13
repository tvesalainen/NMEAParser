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
package org.vesalainen.nmea.router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
/**
 *
 * @author tkv
 */
public class UDPRMCT
{
    static String GPRMC = "$GPRMC";
    static byte[] RMC = GPRMC.getBytes();
    
    @Test
    public void test()
    {
        try (UnconnectedDatagramChannel ch = UnconnectedDatagramChannel.open("224.0.0.3", 10110, 100, true, true))
        {
            byte[] buffer = new byte[256];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            while (true)
            {
                bb.clear();
                ch.read(bb);
                //if (bb.limit() > 10 && equals(RMC, buffer))
                {
                    String sentence = new String(buffer, 0, bb.position());
                    System.err.print(new Date());
                    System.err.println(sentence);
                }
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(UDPRMCT.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
    
    private boolean equals(byte[] one, byte[] other)
    {
        if (one.length > other.length)
        {
            return false;
        }
        for (int ii=0;ii<one.length;ii++)
        {
            if (one[ii] != other[ii])
            {
                return false;
            }
        }
        return true;
    }
}
