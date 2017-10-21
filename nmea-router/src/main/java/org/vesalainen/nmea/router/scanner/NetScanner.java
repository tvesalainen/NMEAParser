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
package org.vesalainen.nmea.router.scanner;

import java.nio.channels.ScatteringByteChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.nmea.router.NMEAMatcher;
import org.vesalainen.nmea.router.NMEAReader;
import org.vesalainen.util.CharSequences;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class NetScanner implements Callable<Set<String>>
{
    private String address;
    private Set<String> fingerPrint;
    private NMEAMatcher<Boolean> matcher;

    public NetScanner(String address, Set<String> fingerPrint)
    {
        this.address = address;
        this.fingerPrint = fingerPrint;
        matcher = new NMEAMatcher<>();
        matcher.addExpression("$", true);
        matcher.addExpression("!", true);
        matcher.compile();
    }

    @Override
    public Set<String> call() throws Exception
    {
        try (final ScatteringByteChannel channel = UnconnectedDatagramChannel.open(address, 10110, PortScanner.BUF_SIZE, true, false))
        {
            NMEAReader reader = new NMEAReader(address, matcher, channel, PortScanner.BUF_SIZE, PortScanner.BUF_SIZE, this::onOk, this::onError);
            reader.read();
        }
        finally
        {
            return fingerPrint;
        }
    }

    private void onOk(RingByteBuffer ring, long timestamp)
    {
        int idx = CharSequences.indexOf(ring, ',');
        if (idx != -1)
        {
            String prefix = ring.subSequence(0, idx).toString();
            fingerPrint.add(prefix);
        }
    }

    private void onError(Supplier<byte[]> errInput)
    {
    }
    
}
