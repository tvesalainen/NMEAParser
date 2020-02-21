/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.processor;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.time.Clock;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.vesalainen.io.CompressedInput;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.parsers.nmea.NMEASender;
import org.vesalainen.time.SimpleClock;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompressedLogPlayer implements AutoCloseable
{
    private WritableByteChannel channel;
    private final NMEASender sender;
    private long millis = -1;

    private CompressedLogPlayer(String address, int port, Stream<Path> paths, CachedScheduledThreadPool executor) throws IOException
    {
        this.channel = UnconnectedDatagramChannel.open(address, port, 100, true, false);
        this.sender = new NMEASender(channel, executor);
        Clock clock = new SimpleClock(()->millis);
        sender.start("");
        sender.set("clock", clock);
        sender.commit("");
        sender.start();
        CompressedInput.readTransactional(paths ,sender, new Waiter());
    }
    
    public static CompressedLogPlayer open(String address, int port, Stream<Path> paths) throws IOException
    {
        return open(address, port, paths, new CachedScheduledThreadPool());
    }
    public static CompressedLogPlayer open(String address, int port, Stream<Path> paths, CachedScheduledThreadPool executor) throws IOException
    {
        return new CompressedLogPlayer(address, port, paths, executor);
    }
    @Override
    public void close()
    {
        try
        {
            channel.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private class Waiter implements Consumer<CompressedInput>
    {
        @Override
        public void accept(CompressedInput t)
        {
            long time = t.getLong("time");
            if (millis != -1)
            {
                try
                {
                    long s = time-millis;
                    if (s > 0)
                    {
                        Thread.sleep(s);
                    }
                }
                catch (InterruptedException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
            millis = time;
        }
        
    }
}
