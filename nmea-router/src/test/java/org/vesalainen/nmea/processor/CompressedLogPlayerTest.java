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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompressedLogPlayerTest
{
    
    public CompressedLogPlayerTest()
    {
    }

    @Test
    public void test() throws IOException
    {
        Path dir = Paths.get("src\\test\\resources");
        dir = dir.toAbsolutePath();
        try (CompressedLogPlayer log = CompressedLogPlayer.open("224.0.0.3", 10110, Files.list(dir).filter((p)->p.getFileName().toString().endsWith(".mea"))))
        {
            
        }
    }
    
}
