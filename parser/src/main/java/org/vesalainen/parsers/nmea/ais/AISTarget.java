/*
 * Copyright (C) 2013 Timo Vesalainen
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
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Timo Vesalainen
 */
public class AISTarget
{
    private Path dataPath;
    private Path dynamicPath;
    private byte[] dataDigest;
    private PrintWriter log;
    private boolean logExists;
    private AISTargetData data;
    private AISTargetDynamic dynamic;

    public AISTarget(int mmsi, Path dir, AISTargetData data, AISTargetDynamic dynamic)
    {
        if (dir != null)
        {
            this.dataPath = dir.resolve(mmsi+"dat");
            this.dynamicPath = dir.resolve(mmsi+"log");
        }
        this.data = data;
        this.dynamic = dynamic;
        this.dataDigest = data.getSha1();
    }
    public void open() throws IOException
    {
        if (dataPath != null)
        {
            logExists = Files.exists(dynamicPath);
            log = new PrintWriter(Files.newBufferedWriter(dynamicPath, CREATE, APPEND));
        }
    }
    public void update(AISTargetData dat, AISTargetDynamic dyn, Collection<String> updatedProperties)
    {
        data.copyFrom(dat, updatedProperties, false);
        dynamic.copyFrom(dyn, updatedProperties, false);
        if (dataPath != null)
        {
            dynamic.print(log, logExists);
            logExists = true;
        }
    }
    public void close()
    {
        if (dataPath != null)
        {
            log.close();
            byte[] sha1 = data.getSha1();
            if (!Arrays.equals(dataDigest, sha1))
            {
                try
                {
                    data.store(dataPath);
                }
                catch (IOException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
        }
    }
}
