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

/**
 *
 * @author tkv
 */
public class IcomManagerTest
{
    
    public IcomManagerTest()
    {
    }

    @Test
    public void test()
    {
        try
        {
            IcomManager manager = IcomManager.getInstance();
            manager.setRemote(true);
            manager.setReceiveFrequency(8.502);
            //manager.setRemote(false);
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
            Logger.getLogger(IcomManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(IcomManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
