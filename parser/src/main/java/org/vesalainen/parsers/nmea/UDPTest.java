/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.parsers.nmea;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import org.vesalainen.parsers.nmea.ais.AISObserver;
import org.vesalainen.parsers.nmea.ais.AISTracer;

/**
 *
 * @author tkv
 */
public class UDPTest
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            NMEAParser parser = NMEAParser.newInstance();
            DatagramChannel dc = DatagramChannel.open();
            dc.bind(new InetSocketAddress(10110));
            NMEAObserver nmeaTracer = NMEATracer.getTracer();
            AISObserver aisTracer = AISTracer.getTracer();
            parser.parse(dc, nmeaTracer, aisTracer);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
}
