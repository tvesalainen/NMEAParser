/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router.endpoint.n2kgw;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.nio.channels.ByteBufferChannel;
import org.vesalainen.nmea.jaxb.router.ConsumerEndpointType;
import org.vesalainen.nmea.router.endpoint.AbstractConsumer;
import org.vesalainen.parsers.nmea.NMEAParser;
import org.vesalainen.parsers.nmea.ais.AISObserver;
import org.vesalainen.parsers.nmea.ais.MessageTypes;
import org.vesalainen.util.TimeToLiveMap;
import org.vesalainen.util.logging.AttachedLogger;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AisComparator extends AbstractConsumer implements AttachedLogger
{
    private final TimeToLiveMap<MessageKey,Map<String,Object>> ttlMap = new TimeToLiveMap(1, TimeUnit.SECONDS);
    private final Map<String,Parser> parsers = new HashMap<>();

    public AisComparator(ConsumerEndpointType config)
    {
        super(config);
    }

    @Override
    public void accept(String source, String nmea)
    {
        //info("%s: %s", source, nmea);
        if (nmea.length() > 78)
        {
            warning("%s: %s over 80 chars", source, nmea);
        }
        Parser parser = parsers.get(source);
        if (parser == null)
        {
            parser = new Parser(source);
            parsers.put(source, parser);
        }
        parser.parse(nmea);
    }

    private void verify(Map<String, Object> m1, Map<String, Object> m2)
    {
        StringBuilder sb = new StringBuilder();
        if (m2.size() > m1.size())
        {
            Map<String, Object> m0 = m1;
            m1 = m2;
            m2 = m0;
        }
        String s1 = (String) m1.get("source");
        String s2 = (String) m2.get("source");
        String a1 = (String) m1.get("nmea");
        String a2 = (String) m2.get("nmea");
        boolean fail = false;
        for (Entry<String, Object> e : m1.entrySet())
        {
            String n1 = e.getKey();
            Object v1 = e.getValue();
            if (!"clock".equals(n1) && !"source".equals(n1) && !"nmea".equals(n1) && !"channel".equals(n1))
            {
                sb.append('\n');
                Object v2 = m2.get(n1);
                if (!Objects.equals(v1, v2))
                {
                    sb.append(n1+": "+v1+" != "+v2);
                    fail = true;
                }
                else
                {
                    sb.append(n1+": "+v1+" == "+v2);
                }
            }
        }
        if (fail)
        {
            warning("%s<>%s \n%s%s%s", s1, s2, a1, a2, sb);
        }
    }
    private class Parser implements InvocationHandler, Runnable
    {
        private final String source;
        private final NMEAParser parser;
        private final AISObserver observer;
        private final Map<String, Object> map = new HashMap<>();
        private final ByteBufferChannel in;
        private final ByteBufferChannel out;
        private final ByteBuffer bb;

        public Parser(String source)
        {
            this.source = source;
            this.parser = NMEAParser.newInstance();
            this.observer = (AISObserver) Proxy.newProxyInstance(
                    AISObserver.class.getClassLoader(), 
                    new Class<?>[]{AISObserver.class}, 
                    this);
            this.bb = ByteBuffer.allocate(256);
            ByteBufferChannel[] pipe = ByteBufferChannel.open(1024, false);
            this.in = pipe[0];
            this.out = pipe[1];
            Thread thread = new Thread(this, source);
            thread.start();
        }
        

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            String name = method.getName();
            switch (name)
            {
                case "commit":
                    if (map.containsKey("mmsi") && map.containsKey("messageType"))
                    {
                        map.put("source", source);
                        MessageKey key = new MessageKey(map);
                        if (ttlMap.containsKey(key))
                        {
                            Map<String, Object> m = ttlMap.get(key);
                            verify(map, m);
                        }
                        else
                        {
                            ttlMap.put(key, new HashMap<>(map));
                        }
                    }
                    map.clear();
                    break;
                case "rollback":
                    map.clear();
                    System.err.println(args[0]);
                    break;
                default:
                    if (name.startsWith("set") && name.length() > 3 && args.length == 1)
                    {
                        String property = Character.toLowerCase(name.charAt(3))+name.substring(4);
                        map.put(property, args[0]);
                    }
                    break;
            }
            return null;
        }

        private void parse(String nmea)
        {
            String text = (String) map.get("nmea");
            if (text != null)
            {
                map.put("nmea", text+nmea+'\n');
            }
            else
            {
                map.put("nmea", nmea+'\n');
            }
            try
            {
                bb.clear();
                bb.put(nmea.getBytes(US_ASCII));
                bb.put((byte)'\r');
                bb.put((byte)'\n');
                bb.flip();
                out.write(bb);
            }
            catch (IOException ex)
            {
                Logger.getLogger(AisComparator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run()
        {
            try
            {
                parser.parse(in, null, observer);
            }
            catch (IOException ex)
            {
                Logger.getLogger(AisComparator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private class MessageKey
    {
        private int mmsi;
        private MessageTypes messageType;
        private int partNumber;

        public MessageKey(Map<String,Object> map)
        {
            this.mmsi = (int) map.get("mmsi");
            this.messageType = (MessageTypes) map.get("messageType");
            if (messageType == MessageTypes.StaticDataReport)
            {
                this.partNumber = (int) map.get("partNumber");
            }
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 59 * hash + this.mmsi;
            hash = 59 * hash + Objects.hashCode(this.messageType);
            hash = 59 * hash + this.partNumber;
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final MessageKey other = (MessageKey) obj;
            if (this.mmsi != other.mmsi)
            {
                return false;
            }
            if (this.partNumber != other.partNumber)
            {
                return false;
            }
            if (this.messageType != other.messageType)
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return mmsi + "=" + messageType;
        }
        
    }
}
