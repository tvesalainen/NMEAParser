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
package org.vesalainen.nmea.processor.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.stream.Stream;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.io.CompressedInput;
import org.vesalainen.time.SimpleClock;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class CompressedLogReader
{
    public static <T extends PropertySetter & Transactional> void readTransactional(Path dir, T setter) throws IOException
    {
        Stream<Path> paths = Files.list(dir).filter((p)->p.getFileName().toString().endsWith(".mea"));
        CompressedInput.readTransactional(paths, new PropertySetterImpl(setter));
    }
    
    private static class PropertySetterImpl extends AbstractPropertySetter
    {
        private PropertySetter inner;
        private long time;
        private Clock clock;

        public PropertySetterImpl(PropertySetter inner)
        {
            this.inner = inner;
        }

        @Override
        public String[] getPrefixes()
        {
            return inner.getPrefixes();
        }

        @Override
        public String[] getProperties()
        {
            return inner.getProperties();
        }

        @Override
        public boolean wantsProperty(String property)
        {
            return inner.wantsProperty(property);
        }

        @Override
        public void set(String property, boolean arg)
        {
            inner.set(property, arg);
        }

        @Override
        public void set(String property, byte arg)
        {
            inner.set(property, arg);
        }

        @Override
        public void set(String property, char arg)
        {
            inner.set(property, arg);
        }

        @Override
        public void set(String property, short arg)
        {
            inner.set(property, arg);
        }

        @Override
        public void set(String property, int arg)
        {
            inner.set(property, arg);
        }

        @Override
        public void set(String property, long arg)
        {
            switch (property)
            {
                case "time":
                    time = arg;
                    if (clock == null)
                    {
                        clock = new SimpleClock(()->time);
                        inner.set("clock", clock);
                    }
                    break;
                default:
                    inner.set(property, arg);
                    break;
            }
        }

        @Override
        public void set(String property, float arg)
        {
            inner.set(property, arg);
        }

        @Override
        public void set(String property, double arg)
        {
            inner.set(property, arg);
        }

        @Override
        public <T> void set(String property, T arg)
        {
            inner.set(property, arg);
        }
        
    }
}
