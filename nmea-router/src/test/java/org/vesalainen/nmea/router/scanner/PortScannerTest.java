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
package org.vesalainen.nmea.router.scanner;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.SymmetricDifferenceMatcher;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.nmea.router.PortType;
import static org.vesalainen.nmea.router.PortType.*;
import org.vesalainen.util.ConditionalSet;
import org.vesalainen.util.RepeatingIterator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PortScannerTest
{
    
    public PortScannerTest()
    {
    }

    @Test
    public void test1()
    {
        Map<PortType,SymmetricDifferenceMatcher<String,String>> prtMtchr = new HashMap<>();
        SymmetricDifferenceMatcher<String, String> nmea = new SymmetricDifferenceMatcher<>();
        prtMtchr.put(NMEA, nmea);
        SymmetricDifferenceMatcher<String, String> nmea_hs = new SymmetricDifferenceMatcher<>();
        prtMtchr.put(NMEA_HS, nmea_hs);
        SymmetricDifferenceMatcher<String, String> seaTalk = new SymmetricDifferenceMatcher<>();
        prtMtchr.put(SEA_TALK, seaTalk);
        nmea.map("$AGHDG", "Compass");
        nmea.map("$AGHDM", "Compass");
        nmea.map("$AGHSC", "Compass");
        nmea.map("$AGRSA", "Compass");
        nmea.map("$GPDTM", "GPS");
        nmea.map("$GPGGA", "GPS");
        nmea.map("$GPGLL", "GPS");
        nmea.map("$GPRMC", "GPS");
        nmea.map("$GPVTG", "GPS");
        nmea.map("$GPZDA", "GPS");
        nmea.map("$IIMWV", "Wind");
        nmea.map("$IIVWR", "Wind");
        nmea.map("$AGBWC", "GPS_2");
        nmea.map("$AGBWW", "GPS_2");
        nmea.map("$AGGLL", "GPS_2");
        nmea.map("$AGHDG", "GPS_2");
        nmea.map("$AGHDM", "GPS_2");
        nmea.map("$AGHSC", "GPS_2");
        nmea.map("$AGRMB", "GPS_2");
        nmea.map("$AGRMC", "GPS_2");
        nmea.map("$AGRSA", "GPS_2");
        nmea.map("$AGXTE", "GPS_2");
        nmea_hs.map("$HCHDG", "Compass_2");
        nmea_hs.map("$HCHDT", "Compass_2");
        nmea_hs.map("$TIROT", "Compass_2");
        nmea_hs.map("$HCTHS", "Compass_2");
        nmea_hs.map("$YXXDR", "Compass_2");
        nmea_hs.map("$GPGGA", "AIS");
        nmea_hs.map("$GPGLL", "AIS");
        nmea_hs.map("$GPRMC", "AIS");
        nmea_hs.map("!AIVDM", "AIS");
        nmea_hs.map("!AIVDO", "AIS");
        seaTalk.map("$SDDBT", "Sounder");
        seaTalk.map("$YCMTW", "Sounder");
        seaTalk.map("$VWVHW", "Sounder");
        
        ConditionalSet<PortType> portTypes = new ConditionalSet<>(prtMtchr.keySet(), (PortType k)->
        {   // only unresolved port types
            SymmetricDifferenceMatcher<String,String> m = prtMtchr.get(k);
            return m!=null && !m.getUnresolved().isEmpty();
        });        
        RepeatingIterator<PortType> it = new RepeatingIterator<>(portTypes, NMEA);
    }

}
