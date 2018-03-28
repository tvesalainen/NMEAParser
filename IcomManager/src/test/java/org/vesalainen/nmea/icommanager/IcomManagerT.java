/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.nmea.icommanager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IcomManagerT
{
    
    public IcomManagerT()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINE);
    }

    @Test
    public void test()
    {
        try
        {
            try (IcomManager manager = IcomManager.getInstance(0, "COM5"))
            {
                manager.setRemote(true);
                manager.setMode("J3E");
                manager.setAutomaticGainControl(false);
                manager.setSquelch(false);
                manager.setNoiseBlanker(true);
                manager.setRFGain(5);
                manager.setFrequency(8.502);
            }
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
            Logger.getLogger(IcomManagerT.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(IcomManagerT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
