/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.vesalainen.nmea.jaxb.router.DatagramType;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.MulticastNMEAType;
import org.vesalainen.nmea.jaxb.router.Nmea0183HsType;
import org.vesalainen.nmea.jaxb.router.Nmea0183Type;
import org.vesalainen.nmea.jaxb.router.NmeaType;
import org.vesalainen.nmea.jaxb.router.ObjectFactory;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.ScriptType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.SerialType;
import org.vesalainen.nmea.jaxb.router.TcpEndpointType;
import org.vesalainen.nmea.script.ScriptParser;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RouterConfig extends JavaLogging
{
    private File file;
    private static JAXBContext jaxbCtx;
    private static ObjectFactory objectFactory;
    private static DatatypeFactory dataTypeFactory;
    private MessageDigest digest;
    
    protected JAXBElement<NmeaType> nmea;
    static
    {
        try
        {
            jaxbCtx = JAXBContext.newInstance("org.vesalainen.nmea.jaxb.router");
            objectFactory = new ObjectFactory();
            dataTypeFactory = DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        catch (JAXBException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public RouterConfig(File file)
    {
        super(RouterConfig.class);
        this.file = file;
        nmea = objectFactory.createNmea(objectFactory.createNmeaType());
    }
    
    public void load() throws IOException, JAXBException
    {
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        unmarshaller.setListener(new Lst());
        nmea = (JAXBElement<NmeaType>) unmarshaller.unmarshal(file);
        checkScriptSyntax();
    }

    public MessageDigest getDigest()
    {
        return digest;
    }

    public List<EndpointType> getRouterEndpoints()
    {
        return nmea.getValue().getTcpEndpointOrProcessorOrMulticast();
    }
    
    public NmeaType getNmeaType()
    {
        return nmea.getValue();
    }
    public MulticastNMEAType createMulticastNMEAType()
    {
        MulticastNMEAType type = objectFactory.createMulticastNMEAType();
        getRouterEndpoints().add(type);
        return type;
    }
    public TcpEndpointType createTcpEndpointType()
    {
        TcpEndpointType listener = objectFactory.createTcpEndpointType();
        getRouterEndpoints().add(listener);
        return listener;
    }
    public DatagramType createDatagramType()
    {
        DatagramType datagram = objectFactory.createDatagramType();
        getRouterEndpoints().add(datagram);
        return datagram;
    }
    public Nmea0183Type createNmea0183Type()
    {
        Nmea0183Type serial = objectFactory.createNmea0183Type();
        getRouterEndpoints().add(serial);
        return serial;
    }
    public Nmea0183HsType createNmea0183HsType()
    {
        Nmea0183HsType serial = objectFactory.createNmea0183HsType();
        getRouterEndpoints().add(serial);
        return serial;
    }
    public SeatalkType createSeatalkType()
    {
        SeatalkType serial = objectFactory.createSeatalkType();
        getRouterEndpoints().add(serial);
        return serial;
    }
    public RouteType createRouteTypeFor(EndpointType endpoint)
    {
        RouteType route = objectFactory.createRouteType();
        endpoint.getRoute().add(route);
        return route;
    }
    public RouteType createRouteTypeFor(String name)
    {
        RouteType route = objectFactory.createRouteType();
        for (EndpointType endpoint : getRouterEndpoints())
        {
            if (endpoint.getName().equals(name))
            {
                endpoint.getRoute().add(route);
                return route;
            }
        }
        throw new IllegalArgumentException(name+" endpoint not found");
    }
    public void changeDevice(SerialType serial, String newDevice) throws IOException
    {
        String oldDevice = serial.getDevice();
        if (!newDevice.equals(oldDevice))
        {
            serial.setDevice(newDevice);
            config("%s device changed from %s to %s", serial.getName(), oldDevice, newDevice);
            store();
        }
    }
    public int getRingBufferSize()
    {
        Long value = nmea.getValue().getRingBufferSize();
        if (value != null)
        {
            return value.intValue();
        }
        else
        {
            return 4096;
        }
    }
    public long getMonitorDelay()
    {
        Long value = nmea.getValue().getMonitorDelay();
        if (value != null)
        {
            return value.longValue();
        }
        else
        {
            return 5000;
        }
    }
    public long getCloseDelay()
    {
        Long value = nmea.getValue().getCloseDelay();
        if (value != null)
        {
            return value.longValue();
        }
        else
        {
            return 1000;
        }
    }
    public List<String> getAllDevices()
    {
        return nmea.getValue().getAllDevices();
    }
    public void store() throws IOException
    {
        try (FileWriter writer = new FileWriter(file))
        {
            store(writer);
        }
    }
    public void store(Writer writer) throws IOException
    {
        try
        {
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(nmea, writer);
        }
        catch (JAXBException ex)
        {
            throw new IOException(ex);
        }
    }

    private void checkScriptSyntax()
    {
        for (EndpointType et : getRouterEndpoints())
        {
            String name = et.getName();
            ScriptType scriptType = et.getScript();
            if (scriptType != null)
            {
                ScriptParser se = ScriptParser.newInstance();
                se.check(scriptType.getValue());
            }
        }
    }

    @Override
    public String toString()
    {
        try
        {
            StringWriter sw = new StringWriter();
            store(sw);
            return sw.toString();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private class Lst extends Listener
    {

        @Override
        public void afterUnmarshal(Object target, Object parent)
        {
            finest("afterUnmarshal %s", target);
            super.afterUnmarshal(target, parent); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void beforeUnmarshal(Object target, Object parent)
        {
            finest("beforeUnmarshal %s", target);
            super.beforeUnmarshal(target, parent); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
