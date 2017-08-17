/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.trackplotter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PlotterTest
{
    
    public PlotterTest()
    {
    }

    @Test
    public void test1()
    {
        try
        {
            URL url = PlotterTest.class.getResource("/20160612200334.trc");
            File file = new File(url.toURI());
            Plotter.main(file.getPath());
        }
        catch (URISyntaxException ex)
        {
            fail(ex.getMessage());
            Logger.getLogger(PlotterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
