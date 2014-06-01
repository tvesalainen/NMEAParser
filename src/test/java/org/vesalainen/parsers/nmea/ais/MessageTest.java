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
package org.vesalainen.parsers.nmea.ais;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.parsers.nmea.NMEAParser;

/**
 *
 * @author Timo Vesalainen
 */
public class MessageTest
{
    private final NMEAParser parser;

    public MessageTest()
    {
        parser = NMEAParser.newInstance();
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void types1_2_3()
    {
        try
        {
            String nmea = "!AIVDM,1,1,,A,13HOI:0P0000VOHLCnHQKwvL05Ip,0*23\r\n";
            TC tc = new TC();
            parser.parse(nmea, null, tc);
            AisContentHelper ach = new AisContentHelper(nmea);
            assertEquals(MessageTypes.values()[ach.getInt(0, 6)], tc.messageType);
            assertEquals(ach.getInt(8, 38), tc.mmsi);
            assertEquals(NavigationStatus.values()[ach.getInt(38, 42)], tc.navigationStatus);
            float roti = ach.getInt(42, 50);
            float rot = (float) ((float) Math.signum(roti)*Math.sqrt(roti/4.733));
            assertEquals(rot, tc.rateOfTurn, 0.00001);
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    public class TC extends AbstractAISObserver
    {
        private boolean ownMessage;
        private String commitReason;
        private String rollbackReason;
        private int seq;
        private int second;
        private int heading;
        private float cog;
        private float latitude;
        private float longitude;
        private float knots;
        private float rateOfTurn;
        private NavigationStatus navigationStatus;
        private int mmsi;
        private int sequentialMessageId;
        private MessageTypes messageType;

        @Override
        public void setOwnMessage(boolean ownMessage)
        {
            this.ownMessage = ownMessage;
        }

        @Override
        public void commit(String reason)
        {
            this.commitReason = reason;
        }

        @Override
        public void rollback(String reason)
        {
            this.rollbackReason = reason;
        }

        @Override
        public void setSequenceNumber(int seq)
        {
            this.seq = seq;
        }

        @Override
        public void setSecond(int second)
        {
            this.second = second;
        }

        @Override
        public void setHeading(int heading)
        {
            this.heading = heading;
        }

        @Override
        public void setCourse(float cog)
        {
            this.cog = cog;
        }

        @Override
        public void setLatitude(float degrees)
        {
            this.latitude = degrees;
        }

        @Override
        public void setLongitude(float degrees)
        {
            this.longitude = degrees;
        }

        @Override
        public void setSpeed(float knots)
        {
            this.knots = knots;
        }

        @Override
        public void setRateOfTurn(float degreesPerMinute)
        {
            this.rateOfTurn = degreesPerMinute;
        }

        @Override
        public void setNavigationStatus(NavigationStatus navigationStatus)
        {
            this.navigationStatus = navigationStatus;
        }

        @Override
        public void setMMSI(int mmsi)
        {
            this.mmsi = mmsi;
        }

        @Override
        public void setMessageType(MessageTypes messageType)
        {
            this.messageType = messageType;
        }
        
    }
}
