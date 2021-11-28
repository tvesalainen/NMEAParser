/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.nio.file.Path;
import java.nio.file.Paths;
import org.vesalainen.ui.ChartPlotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Plot extends ChartPlotter
{

    private final Path path;
    
    public Plot(String filename)
    {
        super(1024, 1024);
        this.path = Paths.get(filename);
    }
    
    public void plot() throws IOException
    {
        this.setFont("ariel", 0, 6);
        drawCoordinates();
        plot(path);
    }
}
