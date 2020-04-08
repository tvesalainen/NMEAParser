/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.viewer;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Binding;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Simulator
{
    private final CachedScheduledThreadPool executor;
    private ViewerPreferences preferences;
    private WritableByteChannel channel;
    private BoatSimulator simulator;
    private Binding<String> host;
    private Binding<Integer> port;
    private Binding<Boolean> simulate;
    private Binding<Float> simBoatSpeed;
    private Binding<Float> simBoatDirection;
    private Binding<Float> simWindSpeed;
    private Binding<Float> simWindDirection;
    private Binding<Float> simCurrentSpeed;
    private Binding<Float> simCurrentDirection;

    public Simulator(ViewerPreferences preferences, CachedScheduledThreadPool executor)
    {
        this.preferences = preferences;
        this.executor = executor;
        host = preferences.getBinding("host");
        port = preferences.getBinding("port");
        simulate = preferences.getBinding("simulate");
        simBoatSpeed = preferences.getBinding("simBoatSpeed");
        simBoatDirection = preferences.getBinding("simBoatDirection");
        simWindSpeed = preferences.getBinding("simWindSpeed");
        simWindDirection = preferences.getBinding("simWindDirection");
        simCurrentSpeed = preferences.getBinding("simCurrentSpeed");
        simCurrentDirection = preferences.getBinding("simCurrentDirection");
        
        simulate.addListener(evt->startStop());
        startStop();
    }

    private void startStop()
    {
        if (simulator == null && simulate.getValue())
        {
            try 
            {
                channel = UnconnectedDatagramChannel.open(host.getValue(), port.getValue(), 100, true, false);
                simulator = new BoatSimulator(
                        channel,
                        executor,
                        Clock.systemUTC(),
                        0,
                        0,
                        ()->simBoatSpeed.getValue(),
                        ()->simBoatDirection.getValue(),
                        ()->simWindSpeed.getValue(),
                        ()->simWindDirection.getValue(),
                        ()->simCurrentSpeed.getValue(),
                        ()->simCurrentDirection.getValue()
                );
                simulator.start();
            }
            catch (IOException ex) 
            {
                throw new RuntimeException(ex);
            }
        }
        else
        {
            if (simulator != null && !simulate.getValue())
            {
                try {
                    simulator.stop();
                    simulator = null;
                    channel.close();
                    channel = null;
                }
                catch (IOException ex) 
                {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
