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
package org.vesalainen.nmea.util;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TrackPlotterTest
{
    
    public TrackPlotterTest()
    {
    }

    //@Test
    public void test1Plot()
    {
            try (FileInputStream input = new FileInputStream("../router/20100515070534.trc"))
            {
                TrackPlotter tp = new TrackPlotter(input, 1000, 1000, Color.BLACK, Color.WHITE);
                tp.plot("20100515070534.png", "png");
            }
            catch (IOException ex)
            {
                fail(ex.getMessage());
                Logger.getLogger(TrackPlotterTest.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
}
