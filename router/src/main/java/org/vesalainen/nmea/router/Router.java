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
import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.comm.channel.SerialChannel.Configuration;
import org.vesalainen.io.AppendablePrinter;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.channels.AppendableByteChannel;
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
import org.vesalainen.util.AutoCloseableList;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;
import org.vesalainen.util.Matcher;
import org.vesalainen.util.Matcher.Status;
import org.vesalainen.util.OrMatcher;
import org.vesalainen.util.SimpleMatcher;
import org.vesalainen.util.logging.ChannelHandler;
import org.vesalainen.util.logging.MinimalFormatter;

/**
 *
 * @author tkv
 */
public class Router
{
    private static final String whiteSpace = "[ \r\n\t]+";
    private final RouterConfig config;
    private static final long ResolvTimeout = 5000;
    private static final int BufferSize = 1024;
    private AutoCloseableList<AutoCloseable> autoCloseables;
    private MultiProviderSelector selector;
    private Set<SerialChannel> portPool = new HashSet<>();
    private Set<SerialEndpoint> resolvPool = new HashSet<>();
    private final Map<String,DataSource> targets = new HashMap<>();
    private final MapList<String,DataSource> sources = new HashMapList<>();
    private List<InetAddress> localAddresses;
    private int serialCount = 0;
    private int resolvCount = 0;
    private boolean canForce;
    private String proprietaryPrefix;
    private Integer tcpPort = 10110;
    private static Logger rootLog;

    public Router(RouterConfig config, Logger rootLog)
    {
        this.config = config;
        this.rootLog = rootLog;
    }
    
    private void run() throws IOException
    {
        rootLog.info(Version.getVersion());
        
        try (AutoCloseableList ac = new AutoCloseableList<>())
        {
            autoCloseables = ac;
            initialize();
            startSocketServer();
            while (true)
            {
                int count = selector.select(ResolvTimeout);
                if (count > 0)
                {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext())
                    {
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
                        if (selectionKey.isReadable())
                        {
                            DataSource dataSource = (DataSource) selectionKey.attachment();
                            rootLog.log(Level.FINE, "read {0}", dataSource);
                            dataSource.read(selectionKey);
                        }
                        else
                        {
                            if (selectionKey.isAcceptable())
                            {
                                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                                SocketChannel socketChannel = serverSocketChannel.accept();
                                rootLog.log(Level.FINE, "accept {0}", socketChannel);
                                autoCloseables.add(socketChannel);
                                socketChannel.configureBlocking(false);
                                Monitor monitor = new Monitor(socketChannel);
                                selector.register(socketChannel, OP_READ, monitor);
                            }
                        }
                    }
                }
                else
                {
                    if (selector.keys().isEmpty())
                    {
                        rootLog.warning("Couldn't resolv ports");
                        return;
                    }
                }
                if (resolvCount < serialCount)
                {
                    resolvPorts();
                }
            }
        }
    }
    private void initialize() throws IOException
    {
        selector = new MultiProviderSelector();
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
    }

    private void startSocketServer() throws IOException
    {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        autoCloseables.add(serverSocketChannel);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(tcpPort));
        selector.register(serverSocketChannel, OP_ACCEPT);
    }
    
    private void resolvPorts() throws IOException
    {
        for (SelectionKey sk : selector.keys())
        {
            DataSource dataSource = (DataSource) sk.attachment();
            if (dataSource != null && (dataSource instanceof SerialEndpoint))
            {
                SerialEndpoint endpoint = (SerialEndpoint) dataSource;
                if (!endpoint.resolving() && endpoint.failed())
                {
                    SerialChannel channel = (SerialChannel) sk.channel();
                    assert (channel != null);
                    portPool.add(channel);
                    resolvPool.add(endpoint);
                }
            }
        }
        for (Endpoint endpoint : resolvPool)
        {
            configureChannel(endpoint, selector, canForce && serialCount-resolvCount==1);
        }
        resolvPool.clear();
        if (resolvCount == serialCount)
        {   
            rootLog.info("release extra ports");
            for (SerialChannel sc : portPool)
            {
                sc.close();
                rootLog.fine("release: "+sc);
            }
            portPool = null;
            resolvPool = null;
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
        Logger log = Logger.getLogger(Router.class.getName());
        log.setUseParentHandlers(false);
        MinimalFormatter minimalFormatter = new MinimalFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(minimalFormatter);
        MemoryHandler memoryHandler = new MemoryHandler(consoleHandler, 1000, Level.SEVERE);
        memoryHandler.setFormatter(minimalFormatter);
        log.addHandler(memoryHandler);
        try
        {
            if (args.length != 1)
            {
                log.severe("usage: ... <xml configuration file>");
                System.exit(-1);
            }
            File configfile = new File(args[0]);
            RouterConfig config = new RouterConfig(configfile);
            Router router = new Router(config, log);
            router.run();
        }
        catch (Exception ex)
        {
            log.log(Level.SEVERE, ex.getMessage(), ex);
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
                                    DataSource ds = targets.get(target);
                                    if (ds != null)
                                    {
                                        readBuffer.flip();
                                        ds.write(readBuffer);
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

        @Override
        public String toString()
        {
            return "DatagramEndpoint{" + "name="+name+" socketAddress=" + socketAddress + '}';
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
                    log.log(Level.INFO, "{0} -> {1}", new Object[]{serialChannel, configuration});
                    channel = serialChannel;
                    return serialChannel;
                }
            }
            return null;
        }

        @Override
        protected void attachedWrite(RingByteBuffer ring) throws IOException
        {
            ring.write((GatheringByteChannel)channel);
        }

        @Override
        protected void write(RingByteBuffer ring) throws IOException
        {
            if (attached == null)
            {
                ring.write((GatheringByteChannel)channel);
            }
        }

        @Override
        protected void write(ByteBuffer bb) throws IOException
        {
            if (attached == null)
            {
                ((WritableByteChannel)channel).write(bb);
            }
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

        @Override
        public String toString()
        {
            return "SerialEndpoint{" + "name="+name+" configuration=" + configuration + '}';
        }
        
    }
    private abstract class Endpoint extends DataSource
    {
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
            super(endpointType.getName());
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
                List<DataSource> list = sources.get(this);
                isSingleSink = list != null && list.size() == 1;
                ready = true;
            }
            return isSingleSink;
        }

        @Override
        protected void read(SelectionKey sk) throws IOException
        {   
            if (attached != null)
            {
                int count = ring.read((ScatteringByteChannel)channel);
                if (count == -1)
                {
                    return;
                }
                ring.getAll(false);
                attached.write(ring);
            }
            else
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
        }

        @Override
        public String toString()
        {
            return "Endpoint{" + "name=" + name + '}';
        }

        private void write(OrMatcher<String> matcher, RingByteBuffer ring) throws IOException
        {
            for (String target : matcher.getLastMatched())
            {
                DataSource ds = targets.get(target);
                if (ds != null)
                {
                    ds.write(ring);
                }
            }
        }
        protected abstract SelectableChannel configureChannel(boolean force) throws IOException;

        protected abstract boolean failed();

        protected abstract boolean resolving();

        protected void matched()
        {
            log.log(Level.INFO, "matched={0}", name);
            targets.put(name, this);
        }

    }
    private class Monitor extends DataSource
    {
        private final SocketChannel channel;
        private final ByteBuffer bb = ByteBuffer.allocateDirect(4096);
        private final AppendablePrinter out;
        private final AppendableByteChannel outChannel;
        private final RingByteBuffer ring = new RingByteBuffer(100, true);
        private final OrMatcher<String> matcher = new OrMatcher<>();
        private final OrMatcher<String> nmeaMatcher = new OrMatcher<>();
        private boolean mark = true;
        private final String help;
        private ChannelHandler logHandler;
        private Level safeLevel;
        private Logger safeLogger;

        public Monitor(SocketChannel socketChannel) throws IOException
        {
            super(socketChannel.getRemoteAddress().toString());
            outChannel = new AppendableByteChannel(socketChannel, 80, true);
            out = new AppendablePrinter(outChannel, "\r\n");
            this.channel = socketChannel;
            out.println(Version.getVersion());
            
            StringBuilder sb = new StringBuilder();
            matcher.add(new SimpleMatcher("h*\n"), "help");
            sb.append("h[elp] Prints help\r\n");
            matcher.add(new SimpleMatcher("i*\n"), "info");
            sb.append("i[nfo] prints router info\r\n");
            matcher.add(new SimpleMatcher("s*\n"), "send");
            sb.append("s[end] <target> ... Send a string to target\r\n");
            matcher.add(new SimpleMatcher("a*\n"), "attach");
            sb.append("a[ttach] <target> Attach target \r\n");
            matcher.add(new SimpleMatcher("l*\n"), "log");
            sb.append("l[og] [target] [level] Log\r\n");
            matcher.add(new SimpleMatcher("exit*\n"), "exit");
            sb.append("exit Exits the session\r\n");
            help = sb.toString();
            outChannel.flush();
            
            nmeaMatcher.add(new SimpleMatcher("$*\n"));
        }
        
        @Override
        protected void read(SelectionKey sk) throws IOException
        {
            if (attached != null)
            {
                int count = ring.read(channel);
                if (count == -1)
                {
                    return;
                }
                while (ring.hasRemaining())
                {
                    byte b = ring.get(mark);
                    switch (nmeaMatcher.match(b))
                    {
                        case Error:
                            mark = true;
                            detach();
                            ring.getAll(true);
                            break;
                        case Ok:
                        case WillMatch:
                            mark = false;
                            break;
                        case Match:
                            mark = true;
                            attachedWrite(ring);
                            break;
                    }
                }
            }
            else
            {
                Matcher.Status match = null;
                int count = ring.read(channel);
                if (count == -1)
                {
                    return;
                }
                while (ring.hasRemaining())
                {
                    byte b = ring.get(mark);
                    match = matcher.match(b);
                    switch (match)
                    {
                        case Error:
                            mark = true;
                            reset();
                            break;
                        case Ok:
                        case WillMatch:
                            mark = false;
                            break;
                        case Match:
                            for (String act : matcher.getLastMatched())
                            {
                                if (!action(ring, act))
                                {
                                    return;
                                }
                            }
                            mark = true;
                            break;
                    }
                }
            }
        }

        private boolean action(RingByteBuffer ring, String act) throws IOException
        {
            try
            {
                switch (act)
                {
                    case "help":
                        out.println(help);
                        outChannel.flush();
                        break;
                    case "info":
                        info();
                        break;
                    case "send":
                        send(ring);
                        break;
                    case "attach":
                        attach(ring);
                        break;
                    case "log":
                        log(ring);
                        break;
                    case "exit":
                        channel.close();
                        return false;
                    default:
                        log.log(Level.SEVERE, "{0} unknown", act);
                }
                return true;
            }
            catch (BadInputException ex)
            {
                out.println(ex.getMessage());
                outChannel.flush();
                return true;
            }
            catch (Exception ex)
            {
                log.log(Level.SEVERE, ex.getMessage(), ex);
                channel.close();
                return false;
            }
        }

        private void info() throws IOException
        {
            for (Entry<String,DataSource> entry :targets.entrySet())
            {
                out.println(entry.getValue());
            }
            outChannel.flush();
        }

        @Override
        protected void write(ByteBuffer src) throws IOException
        {
            channel.write(src);
        }

        @Override
        protected void write(RingByteBuffer ring) throws IOException
        {
            ring.write(channel);
        }

        private void send(RingByteBuffer ring) throws IOException
        {
            String cmd = ring.getString();
            String[] arr = cmd.split(whiteSpace, 3);
            if (arr.length < 3)
            {
                throw new BadInputException("error: "+cmd);
            }
            String target = arr[1];
            DataSource ds = targets.get(target);
            if (ds == null)
            {
                throw new BadInputException("no such target: "+target);
            }
            String msg = arr[2];
            bb.clear();
            bb.put(msg.getBytes());
            bb.put((byte)'\r');
            bb.put((byte)'\n');
            bb.flip();
            ds.write(bb);
            out.println("sent: "+msg);
            outChannel.flush();
        }

        private void attach(RingByteBuffer ring) throws IOException
        {
            String cmd = ring.getString();
            String[] arr = cmd.split(whiteSpace, 3);
            if (arr.length < 2)
            {
                throw new BadInputException("error: "+cmd);
            }
            String target = arr[1];
            DataSource ds = targets.get(target);
            if (ds == null)
            {
                throw new BadInputException("no such target: "+target);
            }
            nmeaMatcher.clear();
            mark = true;
            ds.attach(this);
            attached = ds;
        }

        @Override
        protected void detach()
        {
            attached.detach();
            super.detach();
        }

        private void log(RingByteBuffer ring)
        {
            String cmd = ring.getString();
            String[] arr = cmd.split(whiteSpace);
            int l = arr.length;
            Level level = level(arr[l-1]);
            if (level != null)
            {
                l--;
            }
            Logger lg;
            switch (l)
            {
                case 1:
                    lg = rootLog;
                    break;
                case 2:
                    DataSource ds = targets.get(arr[1]);
                    if (ds == null)
                    {
                        throw new BadInputException("target: "+arr[1]+" not found");
                    }
                    lg = ds.log;
                    break;
                default:
                    throw new BadInputException("error : "+cmd);
            }
            safeLogger = lg;
            safeLevel = lg.getLevel();
            if (level != null)
            {
                lg.setLevel(level);
            }
            logHandler = new ChannelHandler(channel);
            lg.addHandler(logHandler);
        }
        private Level level(String s)
        {
            try
            {
                return Level.parse(s);
            }
            catch (IllegalArgumentException ex)
            {
                return null;
            }
        }

        private void reset()
        {
            if (logHandler != null)
            {
                safeLogger.removeHandler(logHandler);
                logHandler = null;
                safeLogger.setLevel(safeLevel);
                safeLogger = null;
                safeLevel = null;
            }
        }
    }
    private abstract class DataSource
    {
        protected final String name;
        protected DataSource attached;
        protected Logger log;

        public DataSource(String name)
        {
            this.name = name;
            log = Logger.getLogger(this.getClass().getName().replace('$', '.'));
        }
        
        protected abstract void read(SelectionKey sk) throws IOException;

        protected abstract void write(ByteBuffer readBuffer) throws IOException;

        protected abstract void write(RingByteBuffer ring) throws IOException;

        protected void attachedWrite(RingByteBuffer ring) throws IOException
        {
            
        }

        protected void attach(DataSource ds)
        {
            if (attached != null)
            {
                throw new IllegalArgumentException(name+" is already attached");
            }
            attached = ds;
        }
        protected void detach()
        {
            attached = null;
        }
        protected boolean isAttached()
        {
            return attached != null;
        }
        
    }
}
