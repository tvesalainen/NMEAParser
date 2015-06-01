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
import static java.net.StandardSocketOptions.IP_MULTICAST_LOOP;
import static java.net.StandardSocketOptions.SO_BROADCAST;
import java.nio.channels.DatagramChannel;
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
import org.vesalainen.comm.channel.SerialChannel.Builder;
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
import org.vesalainen.util.Matcher;
import org.vesalainen.util.Matcher.Status;
import org.vesalainen.util.OrMatcher;
import org.vesalainen.util.SimpleMatcher;

/**
 *
 * @author tkv
 */
public class Router
{
    private final RouterConfig config;
    private static final long ResolvTimeout = 5000;
    private static final int BufferSize = 5000;
    private Map<String,Endpoint> targets = new HashMap<>();

    public Router(RouterConfig config)
    {
        this.config = config;
    }
    
    private void start() throws IOException
    {
        boolean allResolved = false;
        Set<SelectionKey> resolvPool = new HashSet<>();
        List<AutoCloseable> autoCloseables = new ArrayList<>();
        try (AutoCloseableCollection<AutoCloseable> closer = new AutoCloseableCollection<>(autoCloseables))
        {
            MultiProviderSelector selector = new MultiProviderSelector();
            autoCloseables.add(selector);
            Builder builder = new SerialChannel.Builder("", SerialChannel.Speed.B4800);
            Map<String,SerialChannel> portPool = new HashMap<>();
            for (String port : SerialChannel.getFreePorts())
            {
                SerialChannel sc = builder.setPort(port).get();
                autoCloseables.add(sc);
                portPool.put(port, sc);
                SelectionKey sk = sc.register(selector, OP_READ);
            }
            for (EndpointType et : config.getEndpoints())
            {
                Endpoint endpoint = getInstance(et);
                configureChannel(endpoint, selector, portPool);
            }
            while (true)
            {
                int count = selector.select(ResolvTimeout);
                System.err.println("count="+count);
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
                if (!allResolved)
                {
                    allResolved = true;
                    for (SelectionKey sk : selector.keys())
                    {
                        Endpoint endpoint = (Endpoint) sk.attachment();
                        if (endpoint.resolving())
                        {
                            allResolved = false;
                        }
                        else
                        {
                            if (endpoint.failed())
                            {
                                allResolved = false;
                                SerialChannel channel = endpoint.getChannel();
                                if (channel != null)
                                {
                                    String port = channel.getPort();
                                    portPool.put(port, endpoint.getChannel());
                                }
                                resolvPool.add(sk);
                            }
                        }
                    }
                    for (SelectionKey sk : resolvPool)
                    {
                        Endpoint endpoint = (Endpoint) sk.attachment();
                        configureChannel(endpoint, selector, portPool);
                    }
                    resolvPool.clear();
                    if (allResolved)
                    {   // release extra ports
                        for (SerialChannel sc : portPool.values())
                        {
                            sc.close();
                            autoCloseables.remove(sc);
                        }
                        portPool = null;
                    }
                }
            }
        }
    }

    private void configureChannel(Endpoint endpoint, MultiProviderSelector selector, Map<String,SerialChannel> portPool) throws IOException
    {
        SelectableChannel sc = endpoint.configureChannel(portPool);
        if (sc != null)
        {
            sc.configureBlocking(false);
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
    private class BroadcastNMEAEndpoint extends BroadcastEndpoint
    {
        public BroadcastNMEAEndpoint(BroadcastNMEAType broadcastNMEAType)
        {
            super(broadcastNMEAType);
        }
        @Override
        protected InetSocketAddress createSocketAddress(String address, Integer port)
        {
            if (address == null)
            {
                address = "255.255.255.255";
            }
            if (port == null)
            {
                port = 10110;
            }
            return new InetSocketAddress(address, port);
        }
    }
    private class BroadcastEndpoint extends DatagramEndpoint
    {
        public BroadcastEndpoint(BroadcastType broadcastType)
        {
            super(broadcastType);
        }
        @Override
        protected InetSocketAddress createSocketAddress(String address, Integer port)
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

        @Override
        protected SelectableChannel configureChannel(Map<String, SerialChannel> portPool) throws IOException
        {
            channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(socketAddress.getPort()));
            channel.setOption(SO_BROADCAST, true);
            channel.setOption(IP_MULTICAST_LOOP, false);
            channel.connect(socketAddress);
            return channel;
        }
        
    }
    private class DatagramEndpoint extends Endpoint
    {
        protected DatagramChannel channel;
        protected InetSocketAddress socketAddress;
        public DatagramEndpoint(DatagramType datagramType)
        {
            super(datagramType);
            init(datagramType);
        }
        protected InetSocketAddress createSocketAddress(String address, Integer port)
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
        protected SelectableChannel configureChannel(Map<String, SerialChannel> portPool) throws IOException
        {
            channel = DatagramChannel.open();
            channel.connect(socketAddress);
            return channel;
        }

        @Override
        protected void write(RingByteBuffer ring) throws IOException
        {
            ring.write(channel);
        }

        @Override
        protected boolean failed()
        {
            return false;
        }

        @Override
        protected boolean resolving()
        {
            return false;
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
                    .setParity(SerialChannel.Parity.SPACE)
                    .setReplaceError(true);
        }

        @Override
        protected SelectableChannel configureChannel(Map<String, SerialChannel> portPool) throws IOException
        {
            channel = (SerialChannel) super.configureChannel(portPool);
            if (channel != null)
            {
                return new SeaTalkChannel(channel);
            }
            else
            {
                return null;
            }
        }
        
        @Override
        protected SerialChannel getChannel()
        {
            return channel;
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
        protected Configuration configuration;
        private long resolvStarted;
        protected SerialChannel channel;
        protected Set<String> triedPorts = new HashSet<>();
        
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
            configuration = createConfig();
            Long speed = serialType.getSpeed();
            if (speed != null)
            {
                configuration.setSpeed(SerialChannel.getSpeed(speed.intValue()));
            }
            Integer bits = serialType.getBits();
            if (bits != null)
            {
                configuration.setDataBits(SerialChannel.getDataBits(bits));
            }
            ParityType parity = serialType.getParity();
            if (parity != null)
            {
                configuration.setParity(SerialChannel.getParity(parity.name()));
            }
            Integer stops = serialType.getStops();
            if (stops != null)
            {
                configuration.setStopBits(SerialChannel.getStopBits(stops));
            }
            FlowControlType flowControl = serialType.getFlowControl();
            if (flowControl != null)
            {
                configuration.setFlowControl(SerialChannel.getFlowControl(flowControl.name()));
            }
        }

        @Override
        protected SerialChannel getChannel()
        {
            return channel;
        }

        @Override
        protected SelectableChannel configureChannel(Map<String, SerialChannel> portPool) throws IOException
        {
            Iterator<String> iterator = portPool.keySet().iterator();
            while (iterator.hasNext())
            {
                String port = iterator.next();
                if (!triedPorts.contains(port))
                {
                    triedPorts.add(port);
                    channel = portPool.get(port);
                    iterator.remove();
                    channel.configure(configuration);
                    resolvStarted = System.currentTimeMillis();
                    System.err.println(port+" -> "+configuration);
                    return channel;
                }
            }
            return null;
        }

        @Override
        protected void write(RingByteBuffer ring) throws IOException
        {
            ring.write(channel);
        }

        @Override
        protected boolean failed()
        {
            return failed && resolvStarted + ResolvTimeout < System.currentTimeMillis();
        }

        @Override
        protected boolean resolving()
        {
            return failed && resolvStarted + ResolvTimeout > System.currentTimeMillis();
        }
    }
    private abstract class Endpoint
    {
        protected final String name;
        protected OrMatcher<String> matcher;
        protected RingByteBuffer ring = new RingByteBuffer(BufferSize, true);
        protected boolean failed = true;
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
        {   
            Matcher.Status match = null;
            ScatteringByteChannel channel = (ScatteringByteChannel) sk.channel();
            ring.read(channel);
            System.err.println("reading ->"+this+" "+ring.remaining());
            while (ring.hasRemaining())
            {
                byte b = ring.get(mark);
                System.err.print((char)b);
                match = matcher.match(b);
                switch (match)
                {
                    case Error:
                        mark = true;
                        break;
                    case Ok:
                    case WillMatch:
                        mark = false;
                        break;
                    case Match:
                        write(matcher, ring);
                        if (failed)
                        {
                            System.err.println("matched");
                        }
                        failed = false;
                        mark = true;
                        break;
                }
            }
            System.err.println();
            if (match == Status.WillMatch && canWritePartially())
            {
                write(matcher, ring);
                ring.mark();
            }
        }

        private void write(OrMatcher<String> matcher, RingByteBuffer ring) throws IOException
        {
            for (String target : matcher.getLastMatched())
            {
                Endpoint ep = targets.get(target);
                if (ep != null)
                {
                    ep.write(ring);
                }
            }
        }
        protected abstract SelectableChannel configureChannel(Map<String,SerialChannel> portPool) throws IOException;

        protected abstract void write(RingByteBuffer ring) throws IOException;
        /**
         * Can write partially if only one endpoint writes to target and matched 
         * input is written to only one target.
         * @return 
         */
        protected boolean canWritePartially()
        {
            return false;
        }

        protected abstract boolean failed();

        protected abstract boolean resolving();

        protected SerialChannel getChannel()
        {
            return null;
        }
        
    }
}
