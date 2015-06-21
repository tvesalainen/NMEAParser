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
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import java.util.logging.SocketHandler;
import java.util.prefs.Preferences;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.comm.channel.SerialChannel.Configuration;
import org.vesalainen.io.AppendablePrinter;
import org.vesalainen.nio.RingBuffer;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nio.channels.AppendableByteChannel;
import org.vesalainen.nio.channels.MultiProviderSelector;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.nmea.jaxb.router.BroadcastNMEAType;
import org.vesalainen.nmea.jaxb.router.BroadcastType;
import org.vesalainen.nmea.jaxb.router.DatagramType;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.FlowControlType;
import org.vesalainen.nmea.jaxb.router.Nmea0183HsType;
import org.vesalainen.nmea.jaxb.router.Nmea0183Type;
import org.vesalainen.nmea.jaxb.router.NmeaType;
import org.vesalainen.nmea.jaxb.router.ParityType;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.SenderType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.nmea.sender.Sender;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.util.AutoCloseableList;
import org.vesalainen.util.CmdArgs;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapSet;
import org.vesalainen.util.Matcher;
import org.vesalainen.util.Matcher.Status;
import org.vesalainen.util.OrMatcher;
import org.vesalainen.util.SimpleMatcher;
import org.vesalainen.util.concurrent.ConcurrentArraySet;
import org.vesalainen.util.logging.ChannelHandler;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.logging.MinimalFormatter;

/**
 *
 * @author tkv
 */
public class Router extends JavaLogging
{
    private static final String ConfigDigestKey = "config.digest";
    private static final long RestartLimit = 300000;
    private static final String whiteSpace = "[ \r\n\t]+";
    private final RouterConfig config;
    private static final long ResolvTimeout = 2000;
    private static final long MaxResolvTimeout = 30000;
    private static final int BufferSize = 1024;
    private AutoCloseableList<AutoCloseable> autoCloseables;
    private MultiProviderSelector selector;
    private Set<SerialChannel> portPool = new ConcurrentArraySet<>();
    private Set<SerialEndpoint> resolvPool = new ConcurrentArraySet<>();
    private final Map<String,DataSource> targets = new HashMap<>();
    private final MapSet<String,DataSource> sources = new HashMapSet<>();
    private int serialCount = 0;
    private int resolvCount = 0;
    private boolean canForce;
    private String proprietaryPrefix;
    private int ctrlTcpPort;
    private final Preferences prefs;
    private boolean configChanged=true;
    private final CommandLine commandLine;

    public Router(RouterConfig config, Logger rootLog, CommandLine commandLine)
    {
        super(rootLog);
        this.config = config;
        this.commandLine = commandLine;
        this.prefs = Preferences.userNodeForPackage(this.getClass());
        
        Boolean forcePortConfig = commandLine.getOption("-f");
        if (!forcePortConfig)
        {
            MessageDigest digest = config.getDigest();
            if (digest != null)
            {
                byte[] db = digest.digest();
                HexBinaryAdapter hba = new HexBinaryAdapter();
                String newDigest = hba.marshal(db);
                String oldDigest = prefs.get(ConfigDigestKey, null);
                if (oldDigest != null)
                {
                    configChanged = !oldDigest.equals(newDigest);
                    info("config file changed %b", configChanged);
                }
                prefs.put(ConfigDigestKey, newDigest);
            }
        }
    }
    
    private void run() throws IOException
    {
        info(Version.getVersion());
        
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
                        DataSource dataSource = (DataSource) selectionKey.attachment();
                        fine("handle %s", dataSource);
                        dataSource.handle(selectionKey);
                    }
                }
                else
                {
                    if (selector.keys().isEmpty())
                    {
                        warning("Couldn't resolv ports");
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
        int portCount = portPool.size();
        if (portPool.isEmpty())
        {
            throw new IOException("no ports");
        }

        NmeaType nmeaType = config.getNmeaType();
        proprietaryPrefix = nmeaType.getProprietaryPrefix();
        Integer port = nmeaType.getCtrlTcpPort();
        if (port != null)
        {
            ctrlTcpPort = port;
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
        for (SelectionKey sk : selector.keys())
        {
            DataSource ds = (DataSource) sk.attachment();
            ds.updateStatus();
        }
        if (serialCount == portCount)
        {
            canForce = true;
        }
        SenderType senderType = config.getSenderType();
        if (senderType != null)
        {
            Sender sender = new Sender(senderType);
            Thread thread = new Thread(sender, "sender");
            thread.start();
        }
    }

    private void startSocketServer() throws IOException
    {
        if (ctrlTcpPort > 0)
        {
            SocketSource ss = new SocketSource(ctrlTcpPort);
        }
    }
    
    private void resolvPorts() throws IOException
    {
        if (!configChanged)
        {
            for (SelectionKey sk : selector.keys())
            {
                DataSource ds = (DataSource) sk.attachment();
                if (ds != null && (ds instanceof SerialEndpoint))
                {
                    SerialEndpoint endpoint = (SerialEndpoint) ds;
                    endpoint.matched();
                }
            }
            info("using previous config");
            return;
        }
        Iterator<SerialEndpoint> iterator = resolvPool.iterator();
        while (iterator.hasNext())
        {
            SerialEndpoint endpoint = iterator.next();
            boolean success = configureChannel(endpoint, selector, canForce && serialCount-resolvCount==1);
            if (success)
            {
                iterator.remove();
            }
        }
        for (SelectionKey sk : selector.keys())
        {
            DataSource ds = (DataSource) sk.attachment();
            if (ds != null && (ds instanceof SerialEndpoint))
            {
                SerialEndpoint endpoint = (SerialEndpoint) ds;
                if (!endpoint.resolving() && endpoint.failed())
                {
                    SerialChannel channel = (SerialChannel) sk.channel();
                    assert (channel != null);
                    info("%s: failed %s read cound=%d bytes=%d", ds.name, channel, ds.readCount, ds.readBytes);
                    info("add portPool -> %s", channel);
                    portPool.add(channel);
                    info("add resolvPool -> %s", endpoint);
                    resolvPool.add(endpoint);
                    sk.cancel();
                }
            }
        }
        if (resolvCount == serialCount)
        {   
            for (SerialChannel sc : portPool)
            {
                sc.close();
                info("release extra port %s",sc);
            }
            portPool = null;
            resolvPool = null;
        }
    }

    private boolean configureChannel(Endpoint endpoint, MultiProviderSelector selector, boolean force) throws IOException
    {
        SelectableChannel channel = endpoint.configureChannel(force);
        if (channel != null)
        {
            channel.register(selector, OP_READ, endpoint);
            return true;
        }
        else
        {
            return false;
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
        if (endpointType instanceof Nmea0183HsType)
        {
            return new NmeaHsEndpoint((Nmea0183HsType) endpointType);
        }
        if (endpointType instanceof Nmea0183Type)
        {
            return new NmeaEndpoint((Nmea0183Type) endpointType);
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
    }
    private class BroadcastEndpoint extends DatagramEndpoint
    {
        public BroadcastEndpoint(BroadcastType broadcastType)
        {
            super(broadcastType);
        }
    }
    private class DatagramEndpoint extends Endpoint
    {
        protected String host = "255.255.255.255";
        protected int port = 10110;
        
        public DatagramEndpoint(DatagramType datagramType)
        {
            super(datagramType);
            init(datagramType);
        }

        private void init(DatagramType datagramType)
        {
            String address = datagramType.getAddress();
            if (address != null)
            {
                this.host = address;
            }
            Integer p = datagramType.getPort();
            if (p != null)
            {
                this.port = p;
            }
        }

        @Override
        protected SelectableChannel configureChannel(boolean force) throws IOException
        {
            matched();
            channel = UnconnectedDatagramChannel.open(host, port, BufferSize, true, isSource());
            channel.configureBlocking(false);
            return channel;
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

        @Override
        protected boolean isSingleSink()
        {
            return false;
        }

        @Override
        public String toString()
        {
            return "DatagramEndpoint{name=" +name+ ", host=" + host + ", port=" + port + '}';
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

        public NmeaHsEndpoint(Nmea0183HsType nmeaHsType)
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

        public NmeaEndpoint(Nmea0183Type nmeaType)
        {
            super(nmeaType);
        }
        
    }
    private class SerialEndpoint extends Endpoint
    {
        protected Configuration configuration;
        private long resolvStarted;
        private long resolvTimeout = ResolvTimeout;
        protected Set<SerialChannel> triedPorts = new HashSet<>();
        protected String lastPort;
        protected String port;
        protected OrMatcher realMatcher;
        
        public SerialEndpoint(SerialType serialType)
        {
            super(serialType);
            init(serialType);
            lastPort = prefs.get(name+".port", null);
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
        protected OrMatcher createMatcher(EndpointType endpointType)
        {
            OrMatcher orm = new OrMatcher<>();
            realMatcher = new OrMatcher<>();
            List<RouteType> route = endpointType.getRoute();
            if (!endpointType.getRoute().isEmpty())
            {
                for (RouteType rt : route)
                {
                    List<String> targetList = rt.getTarget();
                    NMEAMatcher nmeaMatcher = new NMEAMatcher(rt.getPrefix());
                    realMatcher.add(nmeaMatcher, targetList);
                    if (!config.isAmbiguousPrefix(rt.getPrefix()))
                    {
                        orm.add(nmeaMatcher, targetList);
                    }
                    for (String trg : targetList)
                    {
                        sources.add(trg, this);
                    }
                }
            }
            return orm;
        }
        @Override
        protected SelectableChannel configureChannel(boolean force) throws IOException
        {
            if (lastPort != null)
            {
                Iterator<SerialChannel> iterator = portPool.iterator();
                while (iterator.hasNext())
                {
                    SerialChannel serialChannel = iterator.next();
                    if (lastPort.equals(serialChannel.getPort()))
                    {
                        info("using last matched port %s", lastPort);
                        iterator.remove();
                        lastPort = null;
                        return configure(serialChannel, force);
                    }
                }
                configChanged = true;
            }
            Iterator<SerialChannel> iterator = portPool.iterator();
            while (iterator.hasNext())
            {
                SerialChannel serialChannel = iterator.next();
                if (!triedPorts.contains(serialChannel) || force)
                {
                    iterator.remove();
                    return configure(serialChannel, force);
                }
            }
            if (resolvTimeout < MaxResolvTimeout)
            {
                resolvTimeout += ResolvTimeout;
            }
            if (triedPorts.size() >= serialCount-resolvCount)
            {
                triedPorts.clear(); // try again
            }
            //info("set resolvTimeout=%d", resolvTimeout);
            return null;
        }

        private SelectableChannel configure(SerialChannel serialChannel, boolean force) throws IOException
        {
            serialChannel.configure(configuration);
            if (force)
            {
                matched();
            }
            else
            {
                triedPorts.add(serialChannel);
                resolvStarted = System.currentTimeMillis();
            }
            info("%s: %s -> %s", name, serialChannel, configuration);
            channel = serialChannel;
            port = serialChannel.getPort();
            return serialChannel;
        }
        
        @Override
        protected void attachedWrite(RingByteBuffer ring) throws IOException
        {
            int cnt = ring.write((GatheringByteChannel)channel);
            writeCount++;
            writeBytes += cnt;
        }

        @Override
        protected boolean failed()
        {
            return failed && resolvStarted + resolvTimeout < System.currentTimeMillis();
        }

        @Override
        protected boolean resolving()
        {
            return failed && resolvStarted + resolvTimeout > System.currentTimeMillis();
        }

        @Override
        protected void matched()
        {
            super.matched();
            resolvCount++;
            prefs.put(name+".port", port);
            matcher = realMatcher;
            realMatcher = null;
        }

        @Override
        public String toString()
        {
            return "SerialEndpoint{" + "name="+name+" port="+port+" configuration=" + configuration + '}';
        }

    }
    private abstract class Endpoint extends DataSource
    {
        protected SelectableChannel channel;
        protected OrMatcher<String> matcher;
        protected RingByteBuffer ring = new RingByteBuffer(BufferSize, true);
        protected boolean failed = true;
        private boolean mark = true;
        private int position = -1;

        public Endpoint(EndpointType endpointType)
        {
            super(endpointType.getName());
            init(endpointType);
        }
        private void init(EndpointType endpointType)
        {
            matcher = createMatcher(endpointType);
        }

        protected OrMatcher createMatcher(EndpointType endpointType)
        {
            OrMatcher orm = new OrMatcher<>();
            List<RouteType> route = endpointType.getRoute();
            if (!endpointType.getRoute().isEmpty())
            {
                for (RouteType rt : route)
                {
                    List<String> targetList = rt.getTarget();
                    orm.add(new NMEAMatcher(rt.getPrefix()), targetList);
                    for (String trg : targetList)
                    {
                        sources.add(trg, this);
                    }
                }
            }
            return orm;
        }
        @Override
        protected void writePartial(RingByteBuffer ring) throws IOException
        {
            if (attached == null)
            {
                int cnt;
                if (position == -1)
                {
                    cnt = ring.write((GatheringByteChannel)channel);
                }
                else
                {
                    cnt = ring.write((GatheringByteChannel)channel, position);
                }
                position = ring.getPosition();
                finest("write %s = %d", ring, cnt);
                writeCount++;
                writeBytes += cnt;
            }
        }

        @Override
        protected void write(RingByteBuffer ring) throws IOException
        {
            if (attached == null)
            {
                if (position != -1)
                {
                    writePartial(ring);
                    position = -1;
                }
                else
                {
                    int cnt = ring.write((GatheringByteChannel)channel);
                    finest("write %s = %d", ring, cnt);
                    writeCount++;
                    writeBytes += cnt;
                }
            }
        }

        @Override
        protected void write(ByteBuffer bb) throws IOException
        {
            if (attached == null)
            {
                int cnt = ((WritableByteChannel)channel).write(bb);
                finest("write %s = %d", bb, cnt);
                writeCount++;
                writeBytes += cnt;
            }
        }

        @Override
        protected void attachedWrite(RingByteBuffer ring) throws IOException
        {
            int cnt = ring.write((GatheringByteChannel)channel);
            finest("write %s = %d", ring, cnt);
            writeCount++;
            writeBytes += cnt;
        }

        public boolean isSource()
        {
            return matcher != null && !matcher.isEmpty();
        }

        @Override
        protected void updateStatus()
        {
            Set<DataSource> set = sources.get(name);
            isSink = set != null && set.size() > 0;
            isSingleSink = isSink && set.size() == 1;
        }
        
        @Override
        protected void handle(SelectionKey sk) throws IOException
        {   
            int count = ring.read((ScatteringByteChannel)channel);
            if (count == -1)
            {
                warning("eof(%s)", name);
                return;
            }
            if (ring.isFull())
            {
                warning("buffer not big enough (%s)", name);
            }
            readCount++;
            readBytes += count;
            if (attached != null)
            {
                ring.getAll(false);
                attached.write(ring);
                ring.mark();
            }
            else
            {
                Matcher.Status match = null;
                while (ring.hasRemaining())
                {
                    byte b = ring.get(mark);
                    match = matcher.match(b);
                    switch (match)
                    {
                        case Error:
                            finest("drop: '%1$c' %1$d 0x%1$02X %2$s", b & 0xff, (RingBuffer)ring);
                            mark = true;
                            break;
                        case Ok:
                        case WillMatch:
                            mark = false;
                            break;
                        case Match:
                            finer("read: %s", ring);
                            write(matcher, ring, false);
                            if (failed)
                            {
                                matched();
                            }
                            mark = true;
                            break;
                    }
                }
                if (match == Status.WillMatch)
                {
                    write(matcher, ring, true);
                }
            }
        }

        @Override
        public String toString()
        {
            return "Endpoint{" + "name=" + name + '}';
        }

        private void write(OrMatcher<String> matcher, RingByteBuffer ring, boolean partial) throws IOException
        {
            for (String target : matcher.getLastMatched())
            {
                DataSource ds = targets.get(target);
                if (ds != null)
                {
                    if (partial)
                    {
                        if (ds.isSingleSink())
                        {
                            finest("write partial to %s %s", target, ring);
                            ds.writePartial(ring);
                        }
                    }
                    else
                    {
                        finest("writeto %s %s", target, ring);
                        ds.write(ring);
                    }
                }
            }
        }
        protected abstract SelectableChannel configureChannel(boolean force) throws IOException;

        protected abstract boolean failed();

        protected abstract boolean resolving();

        protected void matched()
        {
            failed = false;
            info("matched=%s", name);
            targets.put(name, this);
        }

    }
    private class SocketSource extends DataSource
    {

        public SocketSource(int port) throws IOException
        {
            super("SocketSource("+port+")");
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            autoCloseables.add(serverSocketChannel);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, OP_ACCEPT, this);
        }

        @Override
        protected void handle(SelectionKey selectionKey) throws IOException
        {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null)
            {
                fine("accept %s", socketChannel);
                autoCloseables.add(socketChannel);
                socketChannel.configureBlocking(false);
                Monitor monitor = new Monitor(socketChannel);
                socketChannel.register(selector, OP_READ, monitor);
            }
            else
            {
                warning("accept = null");
            }
        }

        @Override
        protected void write(ByteBuffer readBuffer) throws IOException
        {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected void write(RingByteBuffer ring) throws IOException
        {
            throw new UnsupportedOperationException("Not supported.");
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
        private List<String> loggerNames;

        public Monitor(SocketChannel socketChannel) throws IOException
        {
            super(socketChannel.getRemoteAddress().toString());
            outChannel = new AppendableByteChannel(socketChannel, 80, true);
            out = new AppendablePrinter(outChannel, "\r\n");
            this.channel = socketChannel;
            out.println(Version.getVersion());
            outChannel.flush();
            
            StringBuilder sb = new StringBuilder();
            matcher.add(new SimpleMatcher("h*\n"), "help");
            sb.append("h[elp] - Prints help\r\n");
            matcher.add(new SimpleMatcher("i*\n"), "info");
            sb.append("i[nfo] - prints router info\r\n");
            matcher.add(new SimpleMatcher("se*\n"), "send");
            sb.append("se[nd] <target> ... - Send a string to target\r\n");
            matcher.add(new SimpleMatcher("a*\n"), "attach");
            sb.append("a[ttach] <target> - Attach target \r\n");
            matcher.add(new SimpleMatcher("kill*\n"), "kill");
            sb.append("kill <target> - Kill target \r\n");
            matcher.add(new SimpleMatcher("l*\n"), "log");
            sb.append("l[og] [target] [level] - Log\r\n");
            matcher.add(new SimpleMatcher("sho*\n"), "logs");
            sb.append("sho[w logs] - Show available logs\r\n");
            matcher.add(new SimpleMatcher("st*\n"), "statistics");
            sb.append("st[atistics] - Print statistics\r\n");
            matcher.add(new SimpleMatcher("e*\n"), "errors");
            sb.append("e[rrors] - Print errors\r\n");
            matcher.add(new SimpleMatcher("exit*\n"), "exit");
            sb.append("exit - Exits the session\r\n");
            matcher.add(new SimpleMatcher("shutdown*\n"), "shutdown");
            sb.append("shutdown - Shutdown the router\r\n");
            matcher.add(new SimpleMatcher("restart*\n"), "restart");
            sb.append("restart - Restarts the router\r\n");
            help = sb.toString();
            
            nmeaMatcher.add(new SimpleMatcher("$*\n"));
        }
        
        @Override
        protected void handle(SelectionKey sk) throws IOException
        {
            int count = ring.read(channel);
            if (count == -1)
            {
                return;
            }
            readCount++;
            readBytes += count;
            if (attached != null)
            {
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
                            attached.attachedWrite(ring);
                            break;
                    }
                }
            }
            else
            {
                Matcher.Status match = null;
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
                    case "kill":
                        kill(ring);
                        break;
                    case "log":
                        log(ring);
                        break;
                    case "logs":
                        logs();
                        break;
                    case "statistics":
                        statistics();
                        break;
                    case "errors":
                        errors();
                        break;
                    case "exit":
                        channel.close();
                        return false;
                    case "shutdown":
                        throw new ShutdownException(name);
                    case "restart":
                        throw new RestartException(name);
                    default:
                        log(Level.SEVERE, "%s unknown", act);
                }
                outChannel.flush();
                return true;
            }
            catch (BadInputException ex)
            {
                out.println(ex.getMessage());
                outChannel.flush();
                return true;
            }
            catch (ShutdownException | RestartException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                log(Level.SEVERE, ex, ex.getMessage());
                channel.close();
                return false;
            }
        }

        private void info() throws IOException
        {
            out.println("selectors:");
            for (SelectionKey sk : selector.keys())
            {
                DataSource ds = (DataSource) sk.attachment();
                out.println(ds);
            }
            out.println("targets:");
            for (Entry<String,DataSource> entry :targets.entrySet())
            {
                out.println(entry.getValue());
            }
        }

        private void statistics() throws IOException
        {
            out.println("Name\tReads\tBytes\tMean\tWrites\tBytes\tMean");
            for (Entry<String,DataSource> entry :targets.entrySet())
            {
                DataSource d = entry.getValue();
                String readMean = "N/A";
                if (d.readCount > 0)
                {
                    readMean = String.valueOf(d.readBytes/d.readCount);
                }
                String writeMean = "N/A";
                if (d.writeCount > 0)
                {
                    writeMean = String.valueOf(d.writeBytes/d.writeCount);
                }
                out.println(
                        entry.getKey()+"\t"+
                        d.readCount+"\t"+
                        d.readBytes+"\t"+
                        readMean+"\t"+
                        d.writeCount+"\t"+
                        d.writeBytes+"\t"+
                        writeMean+"\t"
                );
            }
        }
        private void errors() throws IOException
        {
            out.println("Name\tPrefix\tMatches\tErrors\t%");
            for (Entry<String,DataSource> entry :targets.entrySet())
            {
                DataSource ds = entry.getValue();
                if (ds instanceof Endpoint)
                {
                    Endpoint ep = (Endpoint) ds;
                    out.print(entry.getKey()+"\t");
                    boolean first = true;
                    for (Matcher m : ep.matcher)
                    {
                        if (!first)
                        {
                            out.print("\t");
                        }
                        first = false;
                        NMEAMatcher nm = (NMEAMatcher) m;
                        out.print(nm.getPrefix()+"\t");
                        out.print(nm.getMatches()+"\t");
                        out.print(nm.getErrors()+"\t");
                        out.print(String.format("%.1f", nm.getErrorPrecent()));
                    }
                    out.println();
                }
            }
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

        private void kill(RingByteBuffer ring)
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
            for (SelectionKey sk : selector.keys())
            {
                if (ds.equals(sk.attachment()))
                {
                    sk.cancel();
                    break;
                }
            }
        }

        @Override
        protected void detach()
        {
            attached.detach();
            super.detach();
        }

        private void logs()
        {
            int index = 1;
            loggerNames = getLoggerNames();
            if (loggerNames.isEmpty())
            {
                out.println("no logs");
            }
            else
            {
                for (String log : loggerNames)
                {
                    out.print(index);
                    out.print("\t");
                    out.println(log);
                    index++;
                }
            }
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
                    lg = Router.this.getLogger();
                    break;
                case 2:
                    lg = getLogger(arr[1]);
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
        private Logger getLogger(String s)
        {
            DataSource ds = targets.get(s);
            if (ds != null)
            {
                return ds.getLogger();
            }
            try
            {
                int idx = Integer.parseInt(s);
                if (loggerNames != null && idx > 0 && idx <= loggerNames.size())
                {
                    return Logger.getLogger(loggerNames.get(idx-1));
                }
            }
            catch (NumberFormatException ex)
            {
                Logger logger = Logger.getLogger(s);
                if (logger != null)
                {
                    return logger;
                }
            }
            throw new BadInputException(s+" log not found");
        }
        private Level level(String s)
        {
            try
            {
                return Level.parse(s.toUpperCase());
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

        @Override
        protected void write(ByteBuffer src) throws IOException
        {
            finest("write %s", src);
            int cnt = channel.write(src);
            writeCount++;
            writeBytes += cnt;
        }

        @Override
        protected void write(RingByteBuffer ring) throws IOException
        {
            finest("write %s", ring);
            int cnt = ring.write(channel);
            writeCount++;
            writeBytes += cnt;
        }

    }
    private abstract class DataSource extends JavaLogging
    {
        protected final String name;
        protected DataSource attached;
        protected boolean isSink;
        protected boolean isSingleSink;
        protected long readCount;
        protected long readBytes;
        protected long writeCount;
        protected long writeBytes;

        public DataSource(String name)
        {
            this.name = name;
            setLogger(Logger.getLogger(this.getClass().getName().replace('$', '.')+"."+name));
        }
        
        protected abstract void handle(SelectionKey sk) throws IOException;

        protected abstract void write(ByteBuffer readBuffer) throws IOException;

        protected abstract void write(RingByteBuffer ring) throws IOException;

        protected void writePartial(RingByteBuffer ring) throws IOException
        {
        }

        protected void attachedWrite(RingByteBuffer ring) throws IOException
        {
        }

        public boolean isSink()
        {
            return isSink;
        }
        /**
         * Returns true if only one endpoint writes to target and matched 
         * input is written to only one target.
         * @return 
         */
        protected boolean isSingleSink()
        {
            return isSingleSink;
        }

        protected void attach(DataSource ds)
        {
            if (attached != null)
            {
                throw new BadInputException(name+" is already attached");
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

        protected void updateStatus()
        {
        }
    }
    public static void main(String... args)
    {
        CommandLine cmdArgs = new CommandLine();
        try
        {
            cmdArgs.setArgs(args);
        }
        catch (CmdArgs.CmdArgsException ex)
        {
            Logger logger = Logger.getLogger(Router.class.getName());
            logger.log(Level.SEVERE, null, ex);
            logger.log(Level.SEVERE, ex.usage());
            System.exit(-1);
        }
        Logger log = Logger.getLogger("org.vesalainen");
        log.setUseParentHandlers(false);
        log.setLevel((Level) cmdArgs.getOption("-ll"));
        Handler handler = null;
        RouterConfig config = null;
        try
        {
            String effectiveGroup = cmdArgs.getEffectiveGroup();
            if (effectiveGroup == null)
            {
                handler = new ConsoleHandler();
            }
            else
            {
                switch (effectiveGroup)
                {
                case "filelog":
                    
                    handler = new FileHandler((String) cmdArgs.getOption("-lp"), 4096000, 1024, true);
                    break;
                case "netlog":
                    handler = new SocketHandler((String) cmdArgs.getOption("-h"), (int) cmdArgs.getOption("p"));
                    break;
                }
            }
            File configfile = (File) cmdArgs.getArgument("configuration file");
            config = new RouterConfig(configfile);
        }
        catch (IOException | SecurityException | JAXBException ex)
        {
            ex.printStackTrace();
            return;
        }
        MinimalFormatter minimalFormatter = new MinimalFormatter();
        handler.setFormatter(minimalFormatter);
        MemoryHandler memoryHandler = new MemoryHandler(handler, 256, Level.SEVERE);
        memoryHandler.setFormatter(minimalFormatter);
        memoryHandler.setPushLevel((Level) cmdArgs.getOption("-pl"));
        log.addHandler(memoryHandler);
        while (true)
        {
            long started = System.currentTimeMillis();
            try
            {
                Router router = new Router(config, log, cmdArgs);
                router.run();
            }
            catch (RestartException ex)
            {
                log.info("restarted by "+ex.getMessage());
            }
            catch (ShutdownException ex)
            {
                log.info("shutdown by "+ex.getMessage());
                return;
            }
            catch (Exception ex)
            {
                log.log(Level.SEVERE, ex.getMessage(), ex);
                long elapsed = System.currentTimeMillis()-started;
                if (elapsed > RestartLimit)
                {
                    log.info("recovering...");
                }
                else
                {
                    log.info("stopped because failing too often...");
                    return;
                }
            }
        }
    }
}
