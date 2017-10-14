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
package org.vesalainen.nmea.router.seatalk;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialInputStream;
import org.vesalainen.parsers.seatalk.SeaTalk2NMEA;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeaTalkInputStreamT
{
    
    public SeaTalkInputStreamT()
    {
    }

    @Test
    public void test() throws IOException
    {
        SerialChannel.Builder builder = new SerialChannel.Builder("COM15", 4800)
                .setReplaceError(true)
                .setParity(SerialChannel.Parity.SPACE);
        try (SerialChannel sc = builder.get())
        {
            SerialInputStream sis = new SerialInputStream(sc, 100);
            SeaTalkInputStream sea = new SeaTalkInputStream(sis);
            int cc = sea.read();
            while (cc != -1)
            {
                System.err.print((char)cc);
                cc = sea.read();
            }
        }
    }
    
}
