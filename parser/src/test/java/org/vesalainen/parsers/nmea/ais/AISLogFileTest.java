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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISLogFileTest
{
    Path path = Paths.get("aisLogFile.log").toAbsolutePath();
    Path gz0 = Paths.get("aisLogFile.log.0.gz").toAbsolutePath();
    Path gz1 = Paths.get("aisLogFile.log.1.gz").toAbsolutePath();
    Path gz2 = Paths.get("aisLogFile.log.2.gz").toAbsolutePath();
    Path gz3 = Paths.get("aisLogFile.log.3.gz").toAbsolutePath();
    
    public AISLogFileTest()
    {
    }

    @After
    public void after() throws IOException
    {
        Files.deleteIfExists(path);
        Files.deleteIfExists(gz0);
        Files.deleteIfExists(gz1);
        Files.deleteIfExists(gz2);
        Files.deleteIfExists(gz3);
    }
    @Test
    public void test1() throws IOException, InterruptedException
    {
        CachedScheduledThreadPool executor = new CachedScheduledThreadPool();
        AISLogFile log = new AISLogFile(path, 80, executor);
        log.format("qwertyuiopölkjhgfdasxcvbn");
        log.format("qwertyuiopölkjhgfdasxcvbn");
        log.format("qwertyuiopölkjhgfdasxcvbn");
        log.format("qwertyuiopölkjhgfdasxcvbn");
        log.format("qwertyuiopölkjhgfdasxcvbn");
        log.flush();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);
        
        List<Path> exp = CollectionHelp.create(gz1, path);
        assertEquals(exp, log.getPaths());
        
    }
    
}
