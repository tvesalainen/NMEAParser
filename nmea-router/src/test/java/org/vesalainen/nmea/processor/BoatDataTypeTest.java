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
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import static java.time.ZoneOffset.UTC;
import java.util.function.Consumer;
import org.junit.Test;
import org.vesalainen.io.CompressedInput;
import org.vesalainen.nmea.jaxb.router.BoatDataType;
import org.vesalainen.nmea.jaxb.router.DepthSounderPositionType;
import org.vesalainen.nmea.jaxb.router.GpsPositionType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BoatDataTypeTest
{
    
    public BoatDataTypeTest()
    {
    }

    @Test
    public void test() throws IOException
    {
        BoatDataType boat = new BoatDataType();
        boat.setLength(BigDecimal.valueOf(12));
        boat.setBeam(BigDecimal.valueOf(4));
        boat.setDraft(BigDecimal.valueOf(1.7));
        boat.setAnchorWeight(BigDecimal.valueOf(20));
        boat.setChainDiameter(BigDecimal.valueOf(10));
        boat.setMaxChainLength(BigDecimal.valueOf(80));
        
        GpsPositionType gps = new GpsPositionType();
        gps.setToSb(BigDecimal.valueOf(1));
        gps.setToBow(BigDecimal.valueOf(12));
        boat.getGpsPositionOrDepthSounderPosition().add(gps);
        
        DepthSounderPositionType dpt = new DepthSounderPositionType();
        dpt.setToSb(BigDecimal.valueOf(2));
        dpt.setToBow(BigDecimal.valueOf(7));
        boat.getGpsPositionOrDepthSounderPosition().add(dpt);

    }
    
    private class Clk extends Clock implements Consumer<CompressedInput>
    {
        private long millis;
        @Override
        public ZoneId getZone()
        {
            return UTC;
        }

        @Override
        public Clock withZone(ZoneId zone)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Instant instant()
        {
            return Instant.ofEpochMilli(millis);
        }

        @Override
        public long millis()
        {
            return millis;
        }

        @Override
        public void accept(CompressedInput cio)
        {
            millis = cio.getLong("time");
        }
        
    }
}
