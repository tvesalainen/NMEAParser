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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import static java.net.StandardSocketOptions.IP_MULTICAST_LOOP;
import static java.net.StandardSocketOptions.SO_BROADCAST;
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.OP_READ;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
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
import org.vesalainen.nmea.jaxb.router.RouterType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.util.AutoCloseableCollection;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;
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
    private static final int BufferSize = 1024;
    private Set<SerialChannel> portPool = new HashSet<>();
    private final Map<String,Endpoint> targets = new HashMap<>();
    private final MapList<String,Endpoint> sources = new HashMapList<>();
    private List<InetAddress> localAddresses;
    private int serialCount = 0;
    private int resolvCount = 0;
    private boolean canForce;
    private String proprietaryPrefix;
    private Integer tcpPort = 10110;
    private Logger log = Logger.getGlobal();

    public Router(RouterConfig config)
    {
        this.config = config;
    }
    
    private void start() throws IOException
    {
        log.info(Version.getVersion());
        Set<Endpoint> resolvPool = new HashSet<>();
        List<AutoCloseable> autoCloseables = new ArrayList<>();
        try (AutoCloseableCollection<AutoCloseable> closer = new AutoCloseableCollection<>(autoCloseables))
        {
            MultiProviderSelector selector = new MultiProviderSelector();
            autoCloseables.add(selector);
            Builder builder = new SerialChannel.Builder("", SerialChannel.Speed.B4800)
                    .setBlocking(false);
            for (String port : SerialChannel.getFreePorts())
            {
                SerialChannel sc = builder.setPort(port).get();
                autoCloseables.add(sc);
                portPool.add(sc);
            }
            if (portPool.isEmpty())
            {
                throw new IOException("no ports");
            }
            
            RouterType routerType = config.getRouterType();
            proprietaryPrefix = routerType.getProprietaryPrefix();
            Integer port = routerType.getTcpPort();
            if (port != null)
            {
                tcpPort = port;
            }
            
            for (EndpointType et : config.getEndpoints())
            {
                Endpoint endpoint = getInstance(et);
                if (endpoint instanceof SerialEndpoint)
                {
                    serialCount++;
                }
                configureChannel(endpoint, selector, false);
            }
            if (serialCount == portPool.size())
            {
                canForce = true;
            }
            while (true)
            {
                int count = selector.select(ResolvTimeout);
                if (count > 0)
                {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext())
                    {
                        SelectionKey sk = iterator.next();
                        iterator.remove();
                        if (sk.isReadable())
                        {
                            Endpoint endpoint = (Endpoint) sk.attachment();
                            endpoint.read(sk);
                        }
                    }
                }
                else
                {
                    if (selector.keys().isEmpty())
                    {
                        log.warning("Couldn't resolv ports");
                        return;
                    }
                }
                if (resolvCount < serialCount)
                {
                    for (SelectionKey sk : selector.keys())
                    {
                        Endpoint endpoint = (Endpoint) sk.attachment();
                        if (!endpoint.resolving() && endpoint.failed())
                        {
                            SerialChannel channel = (SerialChannel) sk.channel();
                            assert (channel != null);
                            portPool.add(channel);
                            resolvPool.add(endpoint);
                        }
                    }
                    for (Endpoint endpoint : resolvPool)
                    {
                        configureChannel(endpoint, selector, canForce && serialCount-resolvCount==1);
                    }
                    resolvPool.clear();
                    if (resolvCount == serialCount)
                    {   // release extra ports
                        for (SerialChannel sc : portPool)
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

    private void configureChannel(Endpoint endpoint, MultiProviderSelector selector, boolean force) throws IOException
    {
        SelectableChannel readChannel = endpoint.configureChannel(force);
        if (readChannel != null)
        {
            selector.register(readChannel, OP_READ, endpoint);
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
        protected SelectableChannel configureChannel(boolean force) throws IOException
        {
            writeChannel = DatagramChannel.open();
            writeChannel.configureBlocking(false);
            writeChannel.bind(null);
            writeChannel.setOption(SO_BROADCAST, true);
            writeChannel.setOption(IP_MULTICAST_LOOP, false);
            writeChannel.connect(socketAddress);
            matched();
            if (isSource())
            {
                readChannel = createReadChannel();
            }
            return readChannel;
        }

    }
    private class DatagramEndpoint extends Endpoint
    {
        protected DatagramChannel readChannel;
        protected DatagramChannel writeChannel;
        protected InetSocketAddress socketAddress;
        protected ByteBuffer readBuffer;
        
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
        protected SelectableChannel configureChannel(boolean force) throws IOException
        {
            writeChannel = DatagramChannel.open();
            writeChannel.configureBlocking(false);
            writeChannel.connect(socketAddress);
            matched();
            if (isSource())
            {
                readChannel = createReadChannel();
            }
            return readChannel;
        }

        @Override
        protected void read(SelectionKey sk) throws IOException
        {
            while (true)
            {
                readBuffer.clear();
                InetSocketAddress receiveAddr = (InetSocketAddress) readChannel.receive(readBuffer);
                if (receiveAddr == null)
                {
                    return;
                }
                if (!localAddresses.contains(receiveAddr.getAddress()))
                {
                    readBuffer.flip();
                    matcher.clear();
                    while (readBuffer.hasRemaining())
                    {
                        byte b = readBuffer.get();
                        switch (matcher.match(b))
                        {
                            case Error:
                                return;
                            case Ok:
                            case WillMatch:
                                break;
                            case Match:
                                for (String target : matcher.getLastMatched())
                                {
                                    Endpoint ep = targets.get(target);
                                    if (ep != null)
                                    {
                                        readBuffer.flip();
                                        ep.write(readBuffer);
                                    }
                                }
                                return;
                        }
                    }
                }
            }
        }
        
        @Override
        protected void write(RingByteBuffer ring) throws IOException
        {
            ring.write(writeChannel);
        }

        @Override
        protected void write(ByteBuffer bb) throws IOException
        {
            writeChannel.write(bb);
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

        protected DatagramChannel createReadChannel() throws IOException
        {
            if (localAddresses == null)
            {
                localAddresses = new ArrayList<>();
                Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                while (nis.hasMoreElements())
                {
                    NetworkInterface ni = nis.nextElement();
                    Enumeration<InetAddress> ias = ni.getInetAddresses();
                    while (ias.hasMoreElements())
                    {
                        localAddresses.add(ias.nextElement());
                    }
                }
            }
            readBuffer = ByteBuffer.allocateDirect(100);
            readChannel = DatagramChannel.open();
            readChannel.configureBlocking(false);
            readChannel.setOption(SO_REUSEADDR, true);
            readChannel.bind(new InetSocketAddress(socketAddress.getPort()));
            return readChannel;
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
        protected SelectableChannel configureChannel(boolean force) throws IOException
        {
            SerialChannel serialChannel = (SerialChannel) super.configureChannel(force);
            if (serialChannel != null)
            {
                channel = new SeaTalkChannel(serialChannel);
                ((SeaTalkChannel)channel).setProprietaryPrefix(proprietaryPrefix);
                return channel;
            }
            else
            {
                return null;
            }
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
        protected Set<SerialChannel> triedPorts = new HashSet<>();
        
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
        protected SelectableChannel configureChannel(boolean force) throws IOException
        {
            Iterator<SerialChannel> iterator = portPool.iterator();
            while (iterator.hasNext())
            {
                SerialChannel serialChannel = iterator.next();
                if (!triedPorts.contains(serialChannel) || force)
                {
                    triedPorts.add(serialChannel);
                    iterator.remove();
                    serialChannel.configure(configuration);
                    resolvStarted = System.currentTimeMillis();
                    log.info(serialChannel+" -> "+configuration);
                    channel = serialChannel;
                    return serialChannel;
                }
            }
            return null;
        }

        @Override
        protected void write(RingByteBuffer ring) throws IOException
        {
            ring.write((GatheringByteChannel)channel);
        }

        @Override
        protected void write(ByteBuffer bb) throws IOException
        {
            ((WritableByteChannel)channel).write(bb);
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

        @Override
        protected void matched()
        {
            super.matched();
            resolvCount++;
        }
        
    }
    private abstract class Endpoint
    {
        protected final String name;
        protected SelectableChannel channel;
        protected OrMatcher<String> matcher;
        protected RingByteBuffer ring = new RingByteBuffer(BufferSize, true);
        protected boolean failed = true;
        private boolean mark = true;
        private boolean isSink;
        private boolean ready;
        private boolean isSingleSink;

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
                    List<String> targetList = rt.getTarget();
                    matcher.add(new SimpleMatcher(rt.getPrefix()+"*\n"), targetList);
                    for (String trg : targetList)
                    {
                        sources.add(trg, this);
                    }
                }
            }
        }

        public boolean isSource()
        {
            return matcher != null;
        }
        public boolean isSink()
        {
            if (!ready)
            {
                isSink = sources.get(this) != null;
                ready = true;
            }
            return isSink;
        }
        /**
         * Returns true if only one endpoint writes to target and matched 
         * input is written to only one target.
         * @return 
         */
        protected boolean isSingleSink()
        {
            if (!ready)
            {
                List<Endpoint> list = sources.get(this);
                isSingleSink = list != null && list.size() == 1;
                ready = true;
            }
            return isSingleSink;
        }

        protected void read(SelectionKey sk) throws IOException
        {   
            Matcher.Status match = null;
            int count = ring.read((ScatteringByteChannel)channel);
            while (ring.hasRemaining())
            {
                byte b = ring.get(mark);
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
                            matched();
                        }
                        failed = false;
                        mark = true;
                        break;
                }
            }
            if (match == Status.WillMatch && isSingleSink())
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
        protected abstract SelectableChannel configureChannel(boolean force) throws IOException;

        protected abstract void write(RingByteBuffer ring) throws IOException;
        protected abstract void write(ByteBuffer bb) throws IOException;
        protected abstract boolean failed();

        protected abstract boolean resolving();

        protected void matched()
        {
            log.info("matched="+name);
            targets.put(name, this);
        }

    }
}
