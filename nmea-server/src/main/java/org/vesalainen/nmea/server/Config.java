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
package org.vesalainen.nmea.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import java.util.logging.Logger;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.vesalainen.lang.Primitives;
import org.vesalainen.management.AbstractDynamicMBean;
import org.vesalainen.math.UnitCategory;
import static org.vesalainen.math.UnitCategory.*;
import org.vesalainen.math.UnitType;
import org.vesalainen.nmea.server.jaxb.NmeaServerType;
import org.vesalainen.nmea.server.jaxb.ObjectFactory;
import org.vesalainen.nmea.server.jaxb.PropertyType;
import org.vesalainen.parsers.nmea.NMEAProperties;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Config extends JavaLogging
{
    private static JAXBContext jaxbCtx;
    private static ObjectFactory objectFactory;
    private static DatatypeFactory dataTypeFactory;
    
    protected JAXBElement<NmeaServerType> element;
    private FileTime lastModified;
    private final NmeaServerType server;
    private final Path path;
    private final ConfigMBean mBean;
    private final Map<String,PropertyType> map = new HashMap<>();
    
    static
    {
        try
        {
            jaxbCtx = JAXBContext.newInstance("org.vesalainen.nmea.server.jaxb");
            objectFactory = new ObjectFactory();
            dataTypeFactory = DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException | JAXBException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
    public Config(Path path) throws IOException, JAXBException
    {
        super(Config.class);
        this.path = path;
        if (Files.exists(path))
        {
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            element = (JAXBElement<NmeaServerType>) unmarshaller.unmarshal(path.toFile());
            this.server = element.getValue();
            this.lastModified = Files.getLastModifiedTime(path);
            server.getProperty().forEach((pt)->
            {
                if (pt != null)
                {
                    map.put(pt.getName(), pt);
        }
            });
        }
        else
        {
            element = objectFactory.createNmeaServer(objectFactory.createNmeaServerType());
            this.server = element.getValue();
            init();
        }
        mBean = new ConfigMBean("NMEA Server Config", server);
        mBean.register();
    }

    public List<PropertyType> getProperties()
    {
        return server.getProperty();
    }

    
    public PropertyType getProperty(String property)
    {
        PropertyType pt = map.get(property);
        if (pt == null)
        {
            pt = objectFactory.createPropertyType();
            pt.setName(property);
            server.getProperty().add(pt);
            map.put(property, pt);
        }
        return pt;
    }
    
    public void store() throws IOException
    {
        try
        {
            FileTime lmt = getLastModifiedTime(path);
            if (lastModified == null || lastModified.compareTo(lmt) == 0)
            {
                try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8, CREATE, TRUNCATE_EXISTING))
                {
                    store(writer);
                }
            }
            else
            {
                warning("Not storing because %s is modified after loading", path);
            }
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "storing %s", path);
        }
    }
    public synchronized void store(Writer writer) throws IOException
    {
        try
        {
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(element, writer);
            lastModified = getLastModifiedTime(path);
        }
        catch (JAXBException ex)
        {
            throw new IOException(ex);
        }
    }
    public String getNmeaMulticastAddress()
    {
        return server.getNmeaMulticastAddress();
    }

    public int getNmeaMulticastPort()
    {
        return Primitives.getInt(server.getNmeaMulticastPort());
    }

    public int getHttpPort()
    {
        return Primitives.getInt(server.getHttpPort());
    }

    public URI getAisDirectory()
    {
        String aisDirectory = server.getAisDirectory();
        if (aisDirectory != null)
        {
            try
            {
                return new URI(aisDirectory);
            }
            catch (URISyntaxException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        else
        {
            return null;
        }
    }

    public long getAisMaxLogSize()
    {
        String aisMaxLogSize = server.getAisMaxLogSize();
        if (aisMaxLogSize != null)
        {
            return (long) UnitType.MULTIPLIER_2_ONE.parse(aisMaxLogSize);
        }
        else
        {
            return Long.MAX_VALUE;
        }
    }

    public long getAisTtl()
    {
        String aisTtl = server.getAisTtl();
        if (aisTtl != null)
        {
            return (long) UnitType.DURATION_MILLI_SECONDS.parse(aisTtl);
        }
        else
        {
            return Long.MAX_VALUE;
        }
    }

    private void init()
    {
        server.setNmeaMulticastAddress("224.0.0.3");
        server.setNmeaMulticastPort(10110);
        server.setHttpPort(8080);
    }

    private FileTime getLastModifiedTime(Path path) throws IOException
    {
        if (Files.exists(path))
        {
            return Files.getLastModifiedTime(path);
        }
        else
        {
            return FileTime.fromMillis(0);
        }
    }

    private void init(PropertyType pt)
    {
        String property = pt.getName();
        NMEAProperties p = NMEAProperties.getInstance();
        if (p.isProperty(property))
        {
            pt.setMax(BigDecimal.valueOf(p.getMax(property)));
            pt.setMin(BigDecimal.valueOf(p.getMin(property)));
            pt.setUnit(p.getUnit(property).name());
            UnitType unit = p.getUnit(property);
            UnitCategory category = unit.getCategory();
            switch (category)
            {
                case PLANE_ANGLE:
                    pt.setDecimals(0);
                    break;
                default:
                    Class<?> type = p.getType(property);
                    switch (type.getSimpleName())
                    {
                        case "float":
                            pt.setDecimals(1);
                            break;
                        case "double":
                            pt.setDecimals(3);
                            break;
                    }
                    break;
            }
        }
    }
    
    private class ConfigMBean extends AbstractDynamicMBean
    {

        public ConfigMBean(String description, Object target)
        {
            super(description, target);
            addOperation(Config.this, "store");
        }

        @Override
        protected ObjectName createObjectName() throws MalformedObjectNameException
        {
            return ObjectName.getInstance(Config.class.getName(), "Type", "Config");
        }
        
    }
}
