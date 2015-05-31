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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.OP_READ;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Configuration;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.channels.MultiProviderSelector;
import org.vesalainen.nmea.jaxb.router.BroadcastNMEAType;
import org.vesalainen.nmea.jaxb.router.BroadcastType;
import org.vesalainen.nmea.jaxb.router.DatagramType;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.FlowControlType;
import org.vesalainen.nmea.jaxb.router.NmeaHsType;
import org.vesalainen.nmea.jaxb.router.NmeaType;
import org.vesalainen.nmea.jaxb.router.ParityType;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.util.AutoCloseableCollection;
import org.vesalainen.util.OrMatcher;
import org.vesalainen.util.SimpleMatcher;

/**
 *
 * @author tkv
 */
public class Router
{
    private final RouterConfig config;
    private static final int BufferSize = 256;
    private Map<String,GatheringByteChannel> targets = new HashMap<>();

    public Router(RouterConfig config)
    {
        this.config = config;
    }
    
    private void start() throws IOException
    {
        Set<String> portPool = new HashSet<>();
        portPool.addAll(SerialChannel.getFreePorts());
        List<AutoCloseable> autoCloseables = new ArrayList<>();
        try (AutoCloseableCollection<AutoCloseable> closer = new AutoCloseableCollection<>(autoCloseables))
        {
            MultiProviderSelector selector = new MultiProviderSelector();
            autoCloseables.add(selector);
            for (EndpointType et : config.getEndpoints())
            {
                Endpoint endpoint = getInstance(et);
                createChannel(endpoint, selector, portPool);
            }
            while (true)
            {
                int count = selector.select(5000);
                if (count > 0)
                {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext())
                    {
                        SelectionKey sk = iterator.next();
                        iterator.remove();
                        Endpoint endpoint = (Endpoint) sk.attachment();
                        endpoint.read(sk);
                    }
                }
                else
                {   // maintenance
                    for (SelectionKey sk : selector.keys())
                    {
                        Endpoint endpoint = (Endpoint) sk.attachment();
                        if (!endpoint.isReading())
                        {
                            String port = endpoint.getPortname();
                            portPool.add(port);
                            sk.channel().close();
                        }
                    }
                    for (SelectionKey sk : selector.keys())
                    {
                        Endpoint endpoint = (Endpoint) sk.attachment();
                        if (!endpoint.isReading())
                        {
                            createChannel(endpoint, selector, portPool);
                        }
                    }
                }
            }
        }
    }

    private void createChannel(Endpoint endpoint, MultiProviderSelector selector, Set<String> portPool) throws ClosedChannelException
    {
        SelectableChannel sc = endpoint.createChannel(portPool);
        String port = endpoint.getPortname();
        portPool.remove(port);
        sc.register(selector, OP_READ, endpoint);
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
    private Endpoint getInstance(EndpointType endpointType)
    {
        if (endpointType instanceof BroadcastNMEAType)
        {
            return new BroadcastNMEAEndpoint((BroadcastNMEAType) endpointType);
        }
        if (endpointType instanceof BroadcastType)
        {
            return new BroadcastEndpoint((BroadcastType) endpointType);
        }
        if (endpointType instanceof DatagramType)
        {
            return new DatagramEndpoint((DatagramType) endpointType);
        }
        if (endpointType instanceof NmeaHsType)
        {
            return new NmeaHsEndpoint((NmeaHsType) endpointType);
        }
        if (endpointType instanceof NmeaType)
        {
            return new NmeaEndpoint((NmeaType) endpointType);
        }
        if (endpointType instanceof SeatalkType)
        {
            return new SeaTalkEndpoint((SeatalkType) endpointType);
        }
        if (endpointType instanceof SerialType)
        {
            return new SerialEndpoint((SerialType) endpointType);
        }
        throw new IllegalArgumentException(endpointType+" unknown");
    }
    private class BroadcastNMEAEndpoint extends DatagramEndpoint
    {
        public BroadcastNMEAEndpoint(BroadcastNMEAType broadcastNMEAType)
        {
            super(broadcastNMEAType);
        }
        @Override
        protected SocketAddress createSocketAddress(String address, Integer port)
        {
            if (address == null)
            {
                address = "255.255.255.255";
            }
            if (port == null)
            {
                port = 10110;
            }
            return new InetSocketAddress(name, port);
        }
    }
    private class BroadcastEndpoint extends DatagramEndpoint
    {
        public BroadcastEndpoint(BroadcastType broadcastType)
        {
            super(broadcastType);
        }
        @Override
        protected SocketAddress createSocketAddress(String address, Integer port)
        {
            if (address != null && port != null)
            {
                return new InetSocketAddress(name, port);
            }
            else
            {
                if (port != null)
                {
                    return new InetSocketAddress("255.255.255.255", port);
                }
                else
                {
                    throw new IllegalArgumentException("port is null");
                }
            }
        }
    }
    private class DatagramEndpoint extends Endpoint
    {
        protected SocketAddress socketAddress;
        public DatagramEndpoint(DatagramType datagramType)
        {
            super(datagramType);
            init(datagramType);
        }
        protected SocketAddress createSocketAddress(String address, Integer port)
        {
            if (address != null && port != null)
            {
                return new InetSocketAddress(name, port);
            }
            else
            {
                if (port != null)
                {
                    return new InetSocketAddress(port);
                }
                else
                {
                    throw new IllegalArgumentException("port is null");
                }
            }
        }

        private void init(DatagramType datagramType)
        {
            socketAddress = createSocketAddress(datagramType.getAddress(), datagramType.getPort());
        }

        @Override
        protected SelectableChannel createChannel(Set<String> portPool)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected boolean isReading()
        {
            return true;
        }

        @Override
        protected String getPortname()
        {
            return null;
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
        private String portname;
        public SerialEndpoint(SerialType serialType)
        {
            super(serialType);
            init(serialType);
        }

        protected Configuration createConfig()
        {
            return new Configuration().setSpeed(SerialChannel.Speed.B4800);
        }

        private void init(SerialType serialType)
        {
            config = createConfig();
            Long speed = serialType.getSpeed();
            if (speed != null)
            {
                config.setSpeed(SerialChannel.getSpeed(speed.intValue()));
            }
            Integer bits = serialType.getBits();
            if (bits != null)
            {
                config.setDataBits(SerialChannel.getDataBits(bits));
            }
            ParityType parity = serialType.getParity();
            if (parity != null)
            {
                config.setParity(SerialChannel.getParity(parity.name()));
            }
            Integer stops = serialType.getStops();
            if (stops != null)
            {
                config.setStopBits(SerialChannel.getStopBits(stops));
            }
            FlowControlType flowControl = serialType.getFlowControl();
            if (flowControl != null)
            {
                config.setFlowControl(SerialChannel.getFlowControl(flowControl.name()));
            }
        }

        @Override
        protected SelectableChannel createChannel(Set<String> portPool)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected String getPortname()
        {
            return portname;
        }
    }
    private abstract class Endpoint
    {
        protected final String name;
        protected OrMatcher<String> matcher;
        protected RingByteBuffer ring = new RingByteBuffer(BufferSize, true);
        private boolean reading;
        private boolean mark = true;

        public Endpoint(EndpointType endpointType)
        {
            this.name = endpointType.getName();
            init(endpointType);
        }
        private void init(EndpointType endpointType)
        {
            List<RouteType> route = endpointType.getRoute();
            if (!endpointType.getRoute().isEmpty())
            {
                matcher = new OrMatcher<>();
                for (RouteType rt : route)
                {
                    matcher.add(new SimpleMatcher(rt.getPrefix()+"*\r\n"), rt.getTarget());
                }
            }
        }

        protected void read(SelectionKey sk) throws IOException
        {   // TODO partial writes!!!
            ScatteringByteChannel channel = (ScatteringByteChannel) sk.channel();
            ring.read(channel);
            while (ring.hasRemaining())
            {
                byte b = ring.get(mark);
                switch (matcher.match(b))
                {
                    case Ok:
                        mark = false;
                        break;
                    case Error:
                        mark = true;
                        break;
                    case Match:
                        for (String target : matcher.getLastMatched())
                        {
                            GatheringByteChannel ch = targets.get(target);
                            if (ch != null)
                            {
                                ring.write(ch);
                            }
                        }
                        mark = true;
                        break;
                }
            }
        }

        protected abstract SelectableChannel createChannel(Set<String> portPool);

        protected boolean isReading()
        {
            return reading;
        }

        protected abstract String getPortname();
        
    }
}
