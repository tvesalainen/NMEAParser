/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.nmea.router;

import java.net.URL;
import org.junit.Test;

/**
 *
 * @author tkv
 */
public class ProcessorT
{
    
    public ProcessorT()
    {
    }

    @Test
    public void test()
    {
        URL url = RouterConfigTest.class.getClassLoader().getResource("processor.xml");
        String filename = url.getFile();
        CommandLine.main("-ll", "CONFIG", "-pl", "CONFIG", filename);
    }
    
}
