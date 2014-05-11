/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.parsers.nmea;

import java.io.IOException;
import org.vesalainen.parser.GenClassFactory;

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
//            NMEAParser parser = (NMEAParser) GenClassFactory.createDynamicInstance(NMEAParser.class);//NMEAParser.newInstance();
            NMEAParser parser = NMEAParser.newInstance();
            DatagramInputStream dis = new DatagramInputStream(10110);
            Tracer tracer = new Tracer();
            parser.parse(dis, tracer, tracer);
        }
        catch (NoSuchMethodException | IOException | NoSuchFieldException | ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            ex.printStackTrace();
        }
    }
    
}
