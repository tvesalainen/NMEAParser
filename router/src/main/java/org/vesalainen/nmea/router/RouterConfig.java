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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.vesalainen.comm.channel.SerialChannel.Speed;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.NmeaType;
import org.vesalainen.nmea.jaxb.router.ObjectFactory;
import org.vesalainen.nmea.jaxb.router.RouterType;
import org.vesalainen.nmea.jaxb.router.ScriptType;
import org.vesalainen.nmea.jaxb.router.ProcessorType;
import org.vesalainen.nmea.script.ScriptParser;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapSet;

/**
 *
 * @author tkv
 */
public class RouterConfig
{
    protected static JAXBContext jaxbCtx;
    protected static ObjectFactory factory;
    protected static DatatypeFactory dtFactory;
    protected MessageDigest digest;
    
    protected JAXBElement<NmeaType> nmea;
    static
    {
        try
        {
            jaxbCtx = JAXBContext.newInstance("org.vesalainen.nmea.jaxb.router");
            factory = new ObjectFactory();
            dtFactory = DatatypeFactory.newInstance();
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
        nmea = factory.createNmea(factory.createNmeaType());
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
            check();
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

    public List<EndpointType> getEndpoints()
    {
        for (Object ob : nmea.getValue().getRouter())
        {
            if (ob instanceof RouterType)
            {
                RouterType rt = (RouterType) ob;
                return rt.getProcessorOrMulticastOrMulticastNmea0183();
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
            marshaller.marshal(nmea, writer);
        }
        catch (JAXBException ex)
        {
            throw new IOException(ex);
        }
    }

    private void check()
    {
        for (EndpointType et : getEndpoints())
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
}
