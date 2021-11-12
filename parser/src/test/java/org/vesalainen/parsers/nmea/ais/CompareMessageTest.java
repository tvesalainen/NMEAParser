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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.logging.Level;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.nmea.util.SimpleStorage;
import static org.vesalainen.parsers.nmea.ais.MessageTypes.StaticDataReport;
import org.vesalainen.util.logging.JavaLogging;

/**
 * 
 * @author Timo Vesalainen
 */
public class CompareMessageTest
{
    private NMEAParser parser;

    public CompareMessageTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.INFO);
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
        parser = NMEAParser.newInstance();
    }

    @After
    public void tearDown()
    {
    }
    @Test
    public void test1() throws IOException
    {
    }
    //@Test
    public void testLog() throws IOException
    {
        StringBuilder sbNet = new StringBuilder();
        StringBuilder sbPrc = new StringBuilder();
        String state = "Net";
        try (InputStream is = CompareMessageTest.class.getResourceAsStream("/cmp.txt");
                InputStreamReader isr = new InputStreamReader(is, US_ASCII);
                BufferedReader br = new BufferedReader(isr);)
        {
            String line = br.readLine();
            while (line != null)
            {
                String ep = getEndpoint(line);
                String nmea = getNMEA(line);
                if (!state.equals(ep))
                {
                    if ("Net".equals(ep))
                    {
                        test(sbNet.toString(), sbPrc.toString());
                        sbNet.setLength(0);
                        sbPrc.setLength(0);
                    }
                    state = ep;
                }
                switch (state)
                {
                    case "Net":
                        sbNet.append(nmea);
                        break;
                    case "Prc":
                        sbPrc.append(nmea);
                        break;
                }
                line = br.readLine();
            }
        }
    }
    public void test(String n1, String n2) throws IOException
    {
        SimpleStorage net = test(n1);
        SimpleStorage prc = test(n2);
        MessageTypes messageType = (MessageTypes) net.getProperty("messageType");
        assertNotNull(messageType);
        String verify = net.verify(prc);
        if (!verify.isEmpty())
        {
            String msg = messageType.toString();
            System.err.println(msg);
            System.err.print(n1);
            System.err.print(n2);
            System.err.println(verify);
        }
        //assertEquals(msg+" "+n1+" "+n2, "", verify);
    }
    public SimpleStorage test(String nmea) throws IOException
    {
        SimpleStorage ss = new SimpleStorage();
        AISObserver observer = ss.getStorage(AISObserver.class);
        parser.parse(nmea, null, observer);
        return ss;
    }

    private String getEndpoint(String line)
    {
        int idx = line.indexOf("!AIVDM");
        return line.substring(idx-6, idx-3);
    }

    private String getNMEA(String line)
    {
        int idx = line.indexOf("!AIVDM");
        return line.substring(idx, line.length()-1)+"\r\n";
    }
}
