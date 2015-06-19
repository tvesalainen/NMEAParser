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
import java.util.List;
import java.util.Map.Entry;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.vesalainen.nmea.jaxb.router.EndpointType;
import org.vesalainen.nmea.jaxb.router.NmeaType;
import org.vesalainen.nmea.jaxb.router.ObjectFactory;
import org.vesalainen.nmea.jaxb.router.RouteType;
import org.vesalainen.nmea.jaxb.router.RouterType;
import org.vesalainen.nmea.jaxb.router.SenderType;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;

/**
 *
 * @author tkv
 */
public class RouterConfig
{
    protected static JAXBContext jaxbCtx;
    protected static ObjectFactory factory;
    protected static DatatypeFactory dtFactory;
    
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
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        try (FileInputStream fis = new FileInputStream(file))
        {
            nmea = (JAXBElement<NmeaType>) unmarshaller.unmarshal(fis); //NOI18N
        }
        check();
    }

    public RouterConfig(URL url) throws IOException, JAXBException
    {
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        nmea = (JAXBElement<NmeaType>) unmarshaller.unmarshal(url);
        check();
    }

    public RouterConfig(InputStream is) throws IOException, JAXBException
    {
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        nmea = (JAXBElement<NmeaType>) unmarshaller.unmarshal(is);
        check();
    }

    public boolean isVariationSource()
    {
        SenderType senderType = getSenderType();
        if (senderType != null)
        {
            return senderType.getVariationSource() != null;
        }
        return false;
    }
    public SenderType getSenderType()
    {
        for (Object ob : nmea.getValue().getSenderOrRouter())
        {
            if (ob instanceof SenderType)
            {
                return (SenderType) ob;
            }
        }
        return null;
    }
    
    public List<EndpointType> getEndpoints()
    {
        for (Object ob : nmea.getValue().getSenderOrRouter())
        {
            if (ob instanceof RouterType)
            {
                RouterType rt = (RouterType) ob;
                return rt.getBroadcastOrBroadcastNmea0183OrDatagram();
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
        MapList<String,String> prefixes = new HashMapList<>();
        for (EndpointType et : getEndpoints())
        {
            String name = et.getName();
            for (RouteType rt : et.getRoute())
            {
                String prefix = rt.getPrefix();
                for (Entry<String,List<String>> e : prefixes.entrySet())
                {
                    String key = e.getKey();
                    if (!name.equals(key))
                    {
                        for (String pre : e.getValue())
                        {
                            if (matchesSame(prefix, pre))
                            {
                                throw new IllegalArgumentException(key+"->"+pre+" and "+name+"->"+prefix+" both match the same");
                            }
                        }
                    }
                }
                prefixes.add(name, prefix);
            }
        }
    }

    private boolean matchesSame(String p1, String p2)
    {
        int len = Math.min(p1.length(), p2.length());
        for (int ii=0;ii<len;ii++)
        {
            char c1 = p1.charAt(ii);
            char c2 = p2.charAt(ii);
            if (!((c1=='?' || c2=='?') || c1 == c2))
            {
                return false;
            }
        }
        return true;
    }
}
