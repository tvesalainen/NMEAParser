/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.parsers.nmea;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.parsers.nmea.ais.AbstractAISObserver;
import org.vesalainen.util.navi.Knots;

/**
 * 
 * @author Timo Vesalainen
 */
public class AISUSBTest
{
    private final NMEAParser parser;
    private final double Epsilon = 0.00001;
    
    public AISUSBTest()
    {
        parser = NMEAParser.newInstance();
    }

    @Test
    public void test()
    {
        try(InputStream is = AISUSBTest.class.getResourceAsStream("/aisdataausbeestä"))
        {
            parser.parse(is, new AbstractNMEAObserver(), new AbstractAISObserver());
        }
        catch (IOException ex)
        {
            Logger.getLogger(AISUSBTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
