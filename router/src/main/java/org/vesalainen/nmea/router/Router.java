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

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.comm.channel.SerialChannel.Configuration;
import org.vesalainen.comm.channel.SerialChannel.Speed;
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
import org.vesalainen.nmea.jaxb.router.ScriptType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.SenderType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.nmea.sender.Sender;
import org.vesalainen.regex.WildcardMatcher;
import org.vesalainen.util.AbstractProvisioner.Setting;
import org.vesalainen.util.AutoCloseableList;
import org.vesalainen.util.Bijection;
import org.vesalainen.util.HashBijection;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapList;
import org.vesalainen.util.MapSet;
import org.vesalainen.util.Matcher;
import org.vesalainen.util.Matcher.Status;
import org.vesalainen.util.logging.ChannelHandler;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class Router extends JavaLogging
{
    private static final String ConfigDigestKey = "config.digest";
    private static final String whiteSpace = "[ \r\n\t]+";
    private final RouterConfig config;
    private long ResolvTimeout = 2000;
    private static final int BufferSize = 1024;
    private AutoCloseableList<AutoCloseable> autoCloseables;
    private MultiProviderSelector selector;
    private Set<SerialChannel> portPool = new HashSet<>();
    private Set<SerialEndpoint> resolvPool = new HashSet<>();
    private final Map<String,DataSource> targets = new HashMap<>();
    private final MapSet<String,DataSource> sources = new HashMapSet<>();
    private final Map<String,SerialEndpoint> allSerialEndpoints = new HashMap<>();
    private final Set<String> matchedSerialEndpoints = new HashSet<>();
    private final ReentrantLock lock = new ReentrantLock();
    private String proprietaryPrefix;
    private int ctrlTcpPort;
    private final Preferences prefs;
    private boolean configChanged=true;
    private final CommandLine commandLine;
    private int portCount;
    private RouterThreadGroup routerThreadGroup;
    private boolean forcePortConfig;
    private NMEAMatcherManager matcherManager;
    private boolean allMatched;
    
    public Router(RouterConfig config, Logger rootLog, CommandLine commandLine)
    {
        super(rootLog);
        this.config = config;
        this.commandLine = commandLine;
        this.prefs = Preferences.userNodeForPackage(this.getClass());
        
    }
    @Setting("-f")
    public void setForcePortConfig(boolean forcePortConfig)
    {
        this.forcePortConfig = forcePortConfig;
    }
    
    @Setting("-rt")
    public void setResolvTimeout(long ResolvTimeout)
    {
        this.ResolvTimeout = ResolvTimeout;
    }
    
    void run() throws Throwable
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
                        fine("select %s %d", dataSource.name, selectionKey.readyOps());
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
                if (!allMatched)
                {
                    resolvPorts();
                }
                Throwable throwable = routerThreadGroup.getThrowable();
                if (throwable != null)
                {
                    throw throwable;
                }
            }
        }
    }
    private void initialize() throws IOException
    {
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
                    fine("config file changed %b", configChanged);
                }
                prefs.put(ConfigDigestKey, newDigest);
            }
        }
        routerThreadGroup = new RouterThreadGroup("router");
        autoCloseables.add(routerThreadGroup);
        selector = new MultiProviderSelector();
        autoCloseables.add(selector);
        Builder builder = new SerialChannel.Builder("", SerialChannel.Speed.B4800)
                .setBlocking(false);
        config("free ports:");
        for (String port : SerialChannel.getFreePorts())
        {
            config("%s", port);
            SerialChannel sc = builder.setPort(port).get();
            autoCloseables.add(sc);
            portPool.add(sc);
        }
        portCount = portPool.size();
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
        MapList<Speed,EndpointType> endPointMap = new HashMapList<>();
        Bijection<SerialEndpoint,EndpointType> serialEndpointMap = new HashBijection<>();
        for (EndpointType et : config.getEndpoints())
        {
            Endpoint endpoint = getInstance(et);
            if (endpoint instanceof SerialEndpoint)
            {
                SerialEndpoint se = (SerialEndpoint) endpoint;
                endPointMap.add(se.getSpeed(), et);
                serialEndpointMap.put(se, et);
            }
            else
            {
                endpoint.init2(et);
                configureChannel(endpoint, selector);
            }
        }
        matcherManager = new NMEAMatcherManager(serialEndpointMap, endPointMap);
        for (Entry<SerialEndpoint, EndpointType> e : serialEndpointMap.entrySet())
        {
            SerialEndpoint se = e.getKey();
            allSerialEndpoints.put(se.name, se);
            se.init2(e.getValue());
            if (!configureChannel(se, selector))
            {
                config("add resolvPool -> %s", se);
                resolvPool.add(se);
            }
        }
        for (SelectionKey sk : selector.keys())
        {
            DataSource ds = (DataSource) sk.attachment();
            ds.updateStatus();
        }
        SenderType senderType = config.getSenderType();
        if (senderType != null)
        {
            Sender sender = new Sender(senderType);
            Thread thread = new Thread(routerThreadGroup, sender, "sender");
            thread.start();
        }
    }

    private synchronized void startSocketServer() throws IOException, InterruptedException
    {
        if (ctrlTcpPort > 0)
        {
            config("monitor listener at %d", ctrlTcpPort);
            SocketSource ss = new SocketSource(ctrlTcpPort);
        }
    }
    
    private void resolvPorts() throws IOException
    {
        lock.lock();
        try
        {
            Iterator<SerialEndpoint> iterator = resolvPool.iterator();
            while (iterator.hasNext())
            {
                SerialEndpoint endpoint = iterator.next();
                boolean success = configureChannel(endpoint, selector);
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
                    if (endpoint.failed())
                    {
                        SerialChannel channel = (SerialChannel) sk.channel();
                        assert (channel != null);
                        config("%s: failed %s read cound=%d bytes=%d", ds.name, channel, ds.readCount, ds.readBytes);
                        config("add portPool -> %s", channel);
                        portPool.add(channel);
                        if (allSerialEndpoints.containsKey(endpoint.name))
                        {
                            config("add resolvPool -> %s", endpoint);
                            resolvPool.add(endpoint);
                        }
                        else
                        {
                            config("killed %s", endpoint);
                        }
                        sk.cancel();
                    }
                }
            }
            if (matchedSerialEndpoints.size() == allSerialEndpoints.size())
            {   
                allMatched = true;
                config("all ports matched");
                for (SerialChannel sc : portPool)
                {
                    sc.close();
                    config("release extra port %s",sc);
                }
                portPool = null;
                resolvPool = null;
                matcherManager = null;
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private boolean configureChannel(Endpoint endpoint, MultiProviderSelector selector) throws IOException
    {
        lock.lock();
        try
        {
            SelectableChannel channel = endpoint.configureChannel();
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
        finally
        {
            lock.unlock();
        }
    }
    public boolean kill(String target)
    {
        lock.lock();
        try
        {
            config("kill(%s)", target);
            SerialEndpoint se = allSerialEndpoints.get(target);
            if (se == null)
            {
                config("kill target %s not found", target);
                return false;
            }
            if (!resolvPool.contains(se))
            {
                // endpoint is active
                config("add portPool -> %s", se.channel);
                portPool.add((SerialChannel) se.channel);
            }
            resolvPool.remove(se);
            targets.remove(target);
            sources.remove(target);
            allSerialEndpoints.remove(target);
            matchedSerialEndpoints.remove(se);
            if (se.scriptEngine != null)
            {
                se.scriptEngine.stop();
            }
            prefs.remove(se.name+".port");
            matcherManager.kill(se);
            return true;
        }
        finally
        {
            lock.unlock();
        }
    }
    public int send(String to, ByteBuffer bb) throws IOException
    {
        DataSource ds = targets.get(to);
        if (ds == null)
        {
            return 0;
        }
        return ds.write(bb);
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

    public class BroadcastNMEAEndpoint extends BroadcastEndpoint
    {
        public BroadcastNMEAEndpoint(BroadcastNMEAType broadcastNMEAType)
        {
            super(broadcastNMEAType);
        }
    }
    public class BroadcastEndpoint extends DatagramEndpoint
    {
        public BroadcastEndpoint(BroadcastType broadcastType)
        {
            super(broadcastType);
        }
    }
    public class DatagramEndpoint extends Endpoint
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
        protected SelectableChannel configureChannel() throws IOException
        {
            matched("because is datagram");
            channel = UnconnectedDatagramChannel.open(host, port, BufferSize, true, isSource());
            channel.configureBlocking(false);
            return channel;
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
    public class SeaTalkEndpoint extends SerialEndpoint
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
        protected SelectableChannel configureChannel() throws IOException
        {
            SerialChannel serialChannel = (SerialChannel) super.configureChannel();
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
    public class NmeaHsEndpoint extends SerialEndpoint
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
    public class NmeaEndpoint extends SerialEndpoint
    {

        public NmeaEndpoint(Nmea0183Type nmeaType)
        {
            super(nmeaType);
        }
        
    }
    public class SerialEndpoint extends Endpoint
    {
        protected Configuration configuration;
        private long resolvStarted;
        private long resolvTimeout = ResolvTimeout;
        protected Set<SerialChannel> triedPorts = new HashSet<>();
        protected String lastPort;
        protected String port;
        
        public SerialEndpoint(SerialType serialType)
        {
            super(serialType);
            init(serialType);
            lastPort = prefs.get(name+".port", null);
            prefs.remove(name+".port");
        }

        public Speed getSpeed()
        {
            return configuration.getSpeed();
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
        protected Matcher<Route> createMatcher(EndpointType endpointType)
        {
            List<RouteType> route = endpointType.getRoute();
            for (RouteType rt : route)
            {
                for (String trg : rt.getTarget())
                {
                    sources.add(trg, this);
                }
            }
            return matcher; // doesn't change it
        }
        @Override
        protected SelectableChannel configureChannel() throws IOException
        {
            if (matcher == null)
            {
                return null;
            }
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
                        return configure(serialChannel);
                    }
                }
                configChanged = true;
            }
            Iterator<SerialChannel> iterator = portPool.iterator();
            while (iterator.hasNext())
            {
                SerialChannel serialChannel = iterator.next();
                if (!triedPorts.contains(serialChannel))
                {
                    iterator.remove();
                    return configure(serialChannel);
                }
            }
            if (triedPorts.size() >= portCount-matchedSerialEndpoints.size())
            {
                config("starting again because tried all ports already");
                triedPorts.clear(); // try again
            }
            //info("set resolvTimeout=%d", resolvTimeout);
            return null;
        }

        private SelectableChannel configure(SerialChannel serialChannel) throws IOException
        {
            serialChannel.configure(configuration);
            triedPorts.add(serialChannel);
            resolvStarted = System.currentTimeMillis();
            info("%s: %s -> %s", name, serialChannel, configuration);
            channel = serialChannel;
            port = serialChannel.getPort();
            return serialChannel;
        }
        
        @Override
        protected int attachedWrite(RingByteBuffer ring) throws IOException
        {
            int cnt = ring.write((GatheringByteChannel)channel);
            writeCount++;
            writeBytes += cnt;
            return cnt;
        }

        protected boolean failed()
        {
            return !matched && resolvStarted + resolvTimeout < System.currentTimeMillis();
        }

        @Override
        protected void matched(CharSequence reason)
        {
            long elapsed = System.currentTimeMillis()-resolvStarted;
            super.matched(reason);
            config("matching took %d millis", elapsed);
            matchedSerialEndpoints.add(name);
            prefs.put(name+".port", port);
            config("%d/%d", matchedSerialEndpoints.size(), allSerialEndpoints.size());
            matcherManager.match(this);
        }

        @Override
        public String toString()
        {
            return "SerialEndpoint{" + "name="+name+" port="+port+" configuration=" + configuration + '}';
        }

    }
    public abstract class Endpoint extends DataSource
    {
        protected SelectableChannel channel;
        protected Matcher<Route> matcher;
        protected RingByteBuffer ring = new RingByteBuffer(BufferSize, true);
        protected boolean matched;
        private boolean mark = true;
        private int position = -1;
        protected EndpointScriptEngine scriptEngine;
        private long lastRead;
        private long nowRead;

        public Endpoint(EndpointType endpointType)
        {
            super(endpointType.getName());
            config("started %s", endpointType.getName());
            init(endpointType);
        }
        private void init(EndpointType endpointType)
        {
            ScriptType scriptType = endpointType.getScript();
            if (scriptType != null)
            {
                scriptEngine = new EndpointScriptEngine(Router.this, routerThreadGroup, this, scriptType.getValue());
            }
        }

        public void init2(EndpointType endpointType)
        {
            matcher = createMatcher(endpointType);
        }
        void setMatcher(Matcher<Route> matcher)
        {
            this.matcher = matcher;
        }
        protected Matcher<Route> createMatcher(EndpointType endpointType)
        {
            NMEAMatcher<Route> wm = null;
            List<RouteType> route = endpointType.getRoute();
            if (!endpointType.getRoute().isEmpty())
            {
                for (RouteType rt : route)
                {
                    List<String> targetList = rt.getTarget();
                    String prefix = rt.getPrefix();
                    if (prefix != null && !prefix.isEmpty())
                    {
                        if (wm == null)
                        {
                            wm = new NMEAMatcher<>();
                        }
                        wm.addExpression(prefix, new Route(prefix, targetList));
                    }
                    for (String trg : targetList)
                    {
                        sources.add(trg, this);
                    }
                }
            }
            if (wm != null)
            {
                wm.compile();
            }
            return wm;
        }
        @Override
        protected int writePartial(RingByteBuffer ring) throws IOException
        {
            int cnt = 0;
            if (matched && attached == null)
            {
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
            return cnt;
        }

        @Override
        protected int write(RingByteBuffer ring) throws IOException
        {
            int cnt = 0;
            if (matched && attached == null)
            {
                if (position != -1)
                {
                    cnt = writePartial(ring);
                    position = -1;
                }
                else
                {
                    cnt = ring.write((GatheringByteChannel)channel);
                    finest("write %s = %d", ring, cnt);
                    writeCount++;
                    writeBytes += cnt;
                }
            }
            return cnt;
        }

        @Override
        protected int write(ByteBuffer bb) throws IOException
        {
            int cnt = 0;
            if (matched && attached == null)
            {
                cnt = ((WritableByteChannel)channel).write(bb);
                finest("write %s = %d", bb, cnt);
                writeCount++;
                writeBytes += cnt;
            }
            return cnt;
        }

        @Override
        protected int attachedWrite(RingByteBuffer ring) throws IOException
        {
            if (matched)
            {
                int cnt = ring.write((GatheringByteChannel)channel);
                finest("write %s = %d", ring, cnt);
                writeCount++;
                writeBytes += cnt;
                return cnt;
            }
            return 0;
        }

        public boolean isSource()
        {
            return matcher != null;
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
            lastRead = nowRead;
            nowRead = System.currentTimeMillis();
            int count = ring.read((ScatteringByteChannel)channel);
            if (count == -1)
            {
                warning("eof(%s)", name);
                return;
            }
            if (ring.isFull())
            {
                long elapsed = System.currentTimeMillis()-lastRead;
                warning("buffer not big enough (%s) time from last read %d millis", name, elapsed);
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
                            if (!matched)
                            {
                                matched(ring);
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

        private void write(Matcher<Route> matcher, RingByteBuffer ring, boolean partial) throws IOException
        {
            matcher.getMatched().write(ring, partial);
            if (scriptEngine != null)
            {
                scriptEngine.write(ring);
            }
        }
        protected abstract SelectableChannel configureChannel() throws IOException;

        protected void matched(CharSequence reason)
        {
            matched = true;
            config("matched=%s: %s", name, reason);
            targets.put(name, this);
            if (scriptEngine != null)
            {
                scriptEngine.start();
            }
        }

    }
    public class SocketSource extends DataSource
    {

        public SocketSource(int port) throws IOException, InterruptedException
        {
            super("SocketSource("+port+")");
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            autoCloseables.add(serverSocketChannel);
            serverSocketChannel.setOption(SO_REUSEADDR, true);
            serverSocketChannel.configureBlocking(false);
            boolean bound = false;
            for (int ii=0;ii<100;ii++)
            {
                synchronized(this)
                {
                    try
                    {
                        serverSocketChannel.bind(new InetSocketAddress(port));
                        bound = true;
                        break;
                    }
                    catch (AlreadyBoundException | BindException ex)
                    {
                        config("rebound %s", serverSocketChannel);
                        wait(100);
                    }
                }
            }
            if (!bound)
            {
                throw new IllegalArgumentException("could not bound server socket");
            }
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
        protected int write(ByteBuffer readBuffer) throws IOException
        {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected int write(RingByteBuffer ring) throws IOException
        {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    public class Monitor extends DataSource
    {
        final SocketChannel channel;
        private final ByteBuffer bb = ByteBuffer.allocateDirect(4096);
        private final AppendablePrinter out;
        private final AppendableByteChannel outChannel;
        private final RingByteBuffer ring = new RingByteBuffer(100, true);
        private final WildcardMatcher<String> matcher = new WildcardMatcher<>();
        private final WildcardMatcher<String> nmeaMatcher = new WildcardMatcher<>();
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
            matcher.addExpression("h*\n", "help");
            sb.append("h[elp] - Prints help\r\n");
            matcher.addExpression("i*\n", "info");
            sb.append("i[nfo] - prints router info\r\n");
            matcher.addExpression("se*\n", "send");
            sb.append("se[nd] <target> ... - Send a string to target\r\n");
            matcher.addExpression("a*\n", "attach");
            sb.append("a[ttach] <target> - Attach target \r\n");
            matcher.addExpression("kill*\n", "kill");
            sb.append("kill <target> - Kill target \r\n");
            matcher.addExpression("l*\n", "log");
            sb.append("l[og] [target] [level] - Log\r\n");
            matcher.addExpression("sho*\n", "logs");
            sb.append("sho[w logs] - Show available logs\r\n");
            matcher.addExpression("st*\n", "statistics");
            sb.append("st[atistics] - Print statistics\r\n");
            matcher.addExpression("er*\n", "errors");
            sb.append("er[rors] - Print errors\r\n");
            matcher.addExpression("exit*\n", "exit");
            sb.append("exit - Exits the session\r\n");
            matcher.addExpression("shutdown*\n", "shutdown");
            sb.append("shutdown - Shutdown the router\r\n");
            matcher.addExpression("restart*\n", "restart");
            sb.append("restart - Restarts the router\r\n");
            help = sb.toString();
            matcher.compile();
            
            nmeaMatcher.addExpression("$*\n", null);
            nmeaMatcher.compile();
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
                            String act = matcher.getMatched();
                            if (!action(ring, act))
                            {
                                return;
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
                        killIt(ring);
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
                out.print("iOps="+sk.interestOps()+" rOps="+sk.readyOps()+" ");
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
            out.println("Name\tMatches\tErrors\t%");
            for (Entry<String,DataSource> entry :targets.entrySet())
            {
                DataSource ds = entry.getValue();
                if (ds instanceof Endpoint)
                {
                    Endpoint ep = (Endpoint) ds;
                    out.print(entry.getKey()+"\t");
                    boolean first = true;
                    NMEAMatcher m = (NMEAMatcher) ep.matcher;
                    if (m != null)
                    {
                        out.print(m.getMatches()+"\t");
                        out.print(m.getErrors()+"\t");
                        out.print(String.format("%.1f", m.getErrorPrecent()));
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

        private void killIt(RingByteBuffer ring)
        {
            String cmd = ring.getString();
            String[] arr = cmd.split(whiteSpace, 3);
            if (arr.length < 2)
            {
                throw new BadInputException("error: "+cmd);
            }
            String target = arr[1];
            if (!kill(target))
            {
                throw new BadInputException("kill failed: "+target);
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
        protected int write(ByteBuffer src) throws IOException
        {
            finest("write %s", src);
            int cnt = channel.write(src);
            writeCount++;
            writeBytes += cnt;
            return cnt;
        }

        @Override
        protected int write(RingByteBuffer ring) throws IOException
        {
            finest("write %s", ring);
            int cnt = ring.write(channel);
            writeCount++;
            writeBytes += cnt;
            return cnt;
        }

    }
}
