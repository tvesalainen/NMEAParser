/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.parsers.nmea;

import java.io.IOException;
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
            DatagramInputStream dis = new DatagramInputStream(10110);
            NMEATracer nmeaTracer = new NMEATracer();
            AISTracer aisTracer = new AISTracer();
            parser.parse(dis, nmeaTracer, aisTracer);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
}
