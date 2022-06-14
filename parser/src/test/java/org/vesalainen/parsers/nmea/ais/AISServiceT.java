/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.io.Reader;
import java.nio.channels.Channels;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.READ;
import java.time.Instant;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nio.channels.GZIPChannel;
import org.vesalainen.parsers.nmea.NMEADispatcher;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISServiceT
{
    
    public AISServiceT()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.CONFIG);
    }

    //@Test
    public void test0() throws IOException
    {
        Path p = Paths.get("c:\\temp\\538001664.log.2.gz");
        GZIPChannel c = new GZIPChannel(p, READ);
        try (   Reader r = Channels.newReader(c, UTF_8.name());
                BufferedReader br = new BufferedReader(r))
        {
            String line = br.readLine();
            while (line != null)
            {
                System.err.println(line);
                line = br.readLine();
            }
        }
    }
    @Test
    public void test1() throws IOException, InterruptedException
    {
        CachedScheduledThreadPool executor = new CachedScheduledThreadPool();
        NMEAService nmeaService = new NMEAService("224.0.0.3", 10110, executor);
        Path dir = Paths.get("c:\\temp");
        AISService aisService = AISService.getInstance(nmeaService, dir.toUri(), 10, 1024*1024, executor);
        nmeaService.start();
        while (true)
        {
            Thread.sleep(10000);
            System.err.println();
            Collection<AISTarget> liveList = aisService.getLiveList();
            liveList.forEach((t) -> testAISTarget(t));
        }

        //aisService.addObserver((s, m, t)->System.err.println(s+" "+m));
        //Thread.sleep(100000000);
    }
    private void testAISTarget(AISTarget target)
    {
        try
        {
            if (target.hasAllData())
            {
                NMEAParser parser = NMEAParser.newInstance();
                NMEADispatcher nmeaDispatcher = NMEADispatcher.newInstance();
                AISDispatcher aisDispatcher = AISDispatcher.newInstance();
                NMEASentence[] positionReport = target.getPositionReport();
                if (positionReport.length > 0)
                {
                    String pos = positionReport[0].toString();
                    AISTargetDynamic dyn = new AISTargetDynamic();
                    aisDispatcher.addObserver(dyn, false);
                    parser.parse(pos, nmeaDispatcher, aisDispatcher);
                    AISTargetDynamic dynamic = target.getDynamic();
                    dyn.setTimestamp(dynamic.getTimestamp());
                    dyn.setChannel(dynamic.getChannel());
                    dyn.setMessageType(dynamic.getMessageType());
                    assertEquals(dynamic, dyn);
                }
                NMEASentence[] staticReport = target.getStaticReport();
                if (staticReport.length > 0)
                {
                    StringBuilder sb = new StringBuilder();
                    for (NMEASentence ns : staticReport)
                    {
                        sb.append(ns.toString());
                    }
                    AISTargetData dat = new AISTargetData();
                    aisDispatcher.addObserver(dat, false);
                    parser.parse(sb.toString(), nmeaDispatcher, aisDispatcher);
                    AISTargetData data = target.getData();
                    assertEquals(data, dat);
                }
                System.err.println("tested "+target);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
}
