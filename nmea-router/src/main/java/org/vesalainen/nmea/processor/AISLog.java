/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.nmea.jaxb.router.AisLogType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.ais.AISProperties;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.logging.AttachedLogger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISLog extends AbstractPropertySetter implements AttachedLogger, Stoppable
{
    private Path dir;
    private Properties props = new Properties();
    private String[] prefixes;
    
    AISLog(AisLogType type)
    {
        String dirName = type.getDirectory();
        Objects.requireNonNull(dirName, "ais-log directory");
        dir = Paths.get(dirName);
        prefixes = CollectionHelp.toArray(AISProperties.getInstance().getAllProperties(), String.class);
    }

    @Override
    public void commit(String reason)
    {
        props.clear();
    }

    @Override
    public void rollback(String reason)
    {
        props.clear();
    }

    @Override
    protected void setProperty(String property, Object arg)
    {
        switch (property)
        {
            default:
                props.setProperty(property, arg.toString());
                break;
        }
    }

    @Override
    public String[] getPrefixes()
    {
        return prefixes;
    }

    @Override
    public void stop()
    {
    }

    
}
