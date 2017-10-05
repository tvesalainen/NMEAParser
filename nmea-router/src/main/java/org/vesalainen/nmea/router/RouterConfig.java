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
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.NmeaType;
import org.vesalainen.nmea.jaxb.router.ObjectFactory;
import org.vesalainen.nmea.jaxb.router.RouterType;
import org.vesalainen.nmea.jaxb.router.ScriptType;
import org.vesalainen.nmea.jaxb.router.TcpEndpointType;
import org.vesalainen.nmea.jaxb.router.TcpListenerType;
import org.vesalainen.nmea.script.ScriptParser;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RouterConfig
{
    protected static JAXBContext jaxbCtx;
    protected static ObjectFactory objectFactory;
    protected static DatatypeFactory dataTypeFactory;
    protected MessageDigest digest;
    
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

    public RouterConfig()
    {
        nmea = objectFactory.createNmea(objectFactory.createNmeaType());
        NmeaType type = nmea.getValue();
        type.getTcpListenerOrRouter().add(objectFactory.createRouterType());
        type.getTcpListenerOrRouter().add(objectFactory.createTcpListenerType());
    }
    
    public RouterConfig(File file) throws IOException, JAXBException

    {
        this(new FileInputStream(file));
    }

    public RouterConfig(URL url) throws IOException, JAXBException
    {
        this(url.openStream());
    }

    public RouterConfig(InputStream is) throws IOException, JAXBException
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
    
    public void write(Writer writer) throws IOException
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

    public static ObjectFactory getObjectFactory()
    {
        return objectFactory;
    }

    public static DatatypeFactory getDataTypeFactory()
    {
        return dataTypeFactory;
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
            write(sw);
            return sw.toString();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
}
