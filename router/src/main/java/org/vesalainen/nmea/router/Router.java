/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.nmea.router;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Configuration;
import org.vesalainen.nmea.jaxb.router.BroadcastType;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.FlowControlType;
import org.vesalainen.nmea.jaxb.router.NmeaHsType;
import org.vesalainen.nmea.jaxb.router.NmeaType;
import org.vesalainen.nmea.jaxb.router.ParityType;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.util.OrMatcher;
import org.vesalainen.util.SimpleMatcher;

/**
 *
 * @author tkv
 */
public class Router
{
    private RouterConfig config;

    public Router(RouterConfig config)
    {
        this.config = config;
    }
    
    private void start()
    {
        for (EndpointType et : config.getEndpoints())
        {
            if (et instanceof SerialType)
            {
                SerialType st = (SerialType) et;
            }
            else
            {
                BroadcastType bt = (BroadcastType) et;
            }
        }
    }
    
    public static void main(String... args)
    {
        try
        {
            if (args.length != 1)
            {
                System.err.println("usage: ... <xml configuration file>");
                System.exit(-1);
            }
            File configfile = new File(args[0]);
            RouterConfig config = new RouterConfig(configfile);
            Router router = new Router(config);
            router.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    private class SeaTalkEndpoint extends SerialEndpoint
    {

        public SeaTalkEndpoint(SeatalkType seaTalkType)
        {
            super(seaTalkType);
        }
        
        @Override
        protected Configuration createConfig()
        {
            return new Configuration()
                    .setSpeed(SerialChannel.Speed.B4800)
                    .setParity(SerialChannel.Parity.SPACE);
        }
    }
    private class NmeaHsEndpoint extends SerialEndpoint
    {

        public NmeaHsEndpoint(NmeaHsType nmeaHsType)
        {
            super(nmeaHsType);
        }
        
        @Override
        protected Configuration createConfig()
        {
            return new Configuration()
                    .setSpeed(SerialChannel.Speed.B38400);
        }
    }
    private class NmeaEndpoint extends SerialEndpoint
    {

        public NmeaEndpoint(NmeaType nmeaType)
        {
            super(nmeaType);
        }
        
    }
    private class SerialEndpoint extends Endpoint
    {
        protected Configuration config;
        public SerialEndpoint(SerialType serialType)
        {
            super(serialType);
            config = createConfig();
            Long speed = serialType.getSpeed();
            if (speed != null)
            {
                config.setSpeed(SerialChannel.getSpeed(speed.intValue()));
            }
            Integer bits = serialType.getBits();
            if (bits != null)
            {
                config.setDataBits(SerialChannel.getDataBits(bits.intValue()));
            }
            ParityType parity = serialType.getParity();
            if (parity != null)
            {
                config.setParity(SerialChannel.getParity(parity.name()));
            }
            Integer stops = serialType.getStops();
            if (stops != null)
            {
                config.setStopBits(SerialChannel.getStopBits(stops.intValue()));
            }
            FlowControlType flowControl = serialType.getFlowControl();
            if (flowControl != null)
            {
                config.setFlowControl(SerialChannel.getFlowControl(flowControl.name()));
            }
        }

        protected Configuration createConfig()
        {
            return new Configuration().setSpeed(SerialChannel.Speed.B4800);
        }
    }
    private class Endpoint
    {
        protected final String name;
        protected OrMatcher<Target> matcher;

        public Endpoint(EndpointType endpointType)
        {
            this.name = endpointType.getName();
            List<RouteType> route = endpointType.getRoute();
            if (!endpointType.getRoute().isEmpty())
            {
                matcher = new OrMatcher<>();
                for (RouteType rt : route)
                {
                    Target target = new Target(rt.getTarget());
                    matcher.add(new SimpleMatcher(rt.getPrefix()+"*\r\n"), target);
                }
            }
        }
        
    }
    private class Target
    {
        private List<String> targets;

        public Target(List<String> targets)
        {
            this.targets = targets;
        }
        
    }
}
