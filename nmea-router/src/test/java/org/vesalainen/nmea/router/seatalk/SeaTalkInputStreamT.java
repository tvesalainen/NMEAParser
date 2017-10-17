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
import java.io.InputStream;
import java.nio.channels.Channels;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialInputStream;
import org.vesalainen.parsers.seatalk.SeaTalk2NMEA;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeaTalkInputStreamT
{
    
    public SeaTalkInputStreamT()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", JavaLogging.DEBUG);
    }

    @Test
    public void test() throws IOException
    {
        SerialChannel.Builder builder = new SerialChannel.Builder("COM15", 4800)
                .setReplaceError(true)
                .setParity(SerialChannel.Parity.SPACE);
        try (SerialChannel sc = builder.get())
        {
            InputStream is = Channels.newInputStream(sc);
            SeaTalkInputStream sea = new SeaTalkInputStream(is);
            byte[] buf = new byte[10];
            int rc = sea.read(buf);
            while (rc != -1)
            {
                System.err.print(new String(buf, 0, rc, ISO_8859_1));
                rc = sea.read(buf);
            }
        }
    }
    
}
