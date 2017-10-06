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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import javax.xml.bind.Unmarshaller;
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
import org.vesalainen.nmea.jaxb.router.RouterType;
import org.vesalainen.nmea.jaxb.router.ScriptType;
import org.vesalainen.nmea.jaxb.router.SeatalkType;
import org.vesalainen.nmea.jaxb.router.TcpEndpointType;
import org.vesalainen.nmea.jaxb.router.TcpListenerType;
import org.vesalainen.nmea.script.ScriptParser;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RouterConfig
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
        this.file = file;
        nmea = objectFactory.createNmea(objectFactory.createNmeaType());
        NmeaType type = nmea.getValue();
        type.getTcpListenerOrRouter().add(objectFactory.createRouterType());
        type.getTcpListenerOrRouter().add(objectFactory.createTcpListenerType());
    }
    
    public void load() throws IOException, JAXBException
    {
        load(new FileInputStream(file));
    }

    private void load(InputStream is) throws IOException, JAXBException
    {
        try
        {
            digest = MessageDigest.getInstance("SHA-1");
            DigestInputStream dis = new DigestInputStream(is, digest);
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            nmea = (JAXBElement<NmeaType>) unmarshaller.unmarshal(dis);
            checkScriptSyntax();
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IOException(ex);
        }
    }

    public MessageDigest getDigest()
    {
        return digest;
    }

    public List<EndpointType> getRouterEndpoints()
    {
        for (Object ob : nmea.getValue().getTcpListenerOrRouter())
        {
            if (ob instanceof RouterType)
            {
                RouterType rt = (RouterType) ob;
                return rt.getProcessorOrMulticastOrMulticastNmea0183();
            }
        }
        return null;
    }
    
    public List<TcpEndpointType> getTcpListenerEndpoints()
    {
        for (Object ob : nmea.getValue().getTcpListenerOrRouter())
        {
            if (ob instanceof TcpListenerType)
            {
                TcpListenerType tlt = (TcpListenerType) ob;
                return tlt.getTcpEndpoint();
            }
        }
        return null;
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
        getTcpListenerEndpoints().add(listener);
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
    public void store() throws IOException
    {
        try (FileWriter writer = new FileWriter(file))
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
    
}
