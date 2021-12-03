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

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.vesalainen.nmea.jaxb.router.BoatDataType;
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
    private Path path;
    private FileTime lastModified;
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
        catch (DatatypeConfigurationException | JAXBException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public RouterConfig(Path path) throws IOException
    {
        super(RouterConfig.class);
        this.path = path;
        nmea = objectFactory.createNmea(objectFactory.createNmeaType());
    }
    
    public void load() throws IOException, JAXBException
    {
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        unmarshaller.setListener(new Lst());
        nmea = (JAXBElement<NmeaType>) unmarshaller.unmarshal(path.toFile());
        checkScriptSyntax();
        this.lastModified = Files.getLastModifiedTime(path);
    }

    public MessageDigest getDigest()
    {
        return digest;
    }

    public BoatDataType getBoatData()
    {
        return nmea.getValue().getBoat();
    }
    public Stream<EndpointType> getRouterEndpoints()
    {
        return nmea
                .getValue()
                .getEndPoints().getN2KGatewayOrConsumerEndpointOrLogEndpoint()
                .stream()
                .filter(this::checkEnabled);
    }
    private boolean checkEnabled(EndpointType endpointType)
    {
        if (endpointType.isEnable())
        {
            return true;
        }
        else
        {
            warning("%s is not enabled and therefore not started!", endpointType.getName());
            return false;
        }
    }
    public void add(EndpointType endpointType)
    {
        nmea.getValue().getEndPoints().getN2KGatewayOrConsumerEndpointOrLogEndpoint().add(endpointType);
    }
    public NmeaType getNmeaType()
    {
        return nmea.getValue();
    }
    public MulticastNMEAType createMulticastNMEAType()
    {
        MulticastNMEAType type = objectFactory.createMulticastNMEAType();
        add(type);
        return type;
    }
    public TcpEndpointType createTcpEndpointType()
    {
        TcpEndpointType listener = objectFactory.createTcpEndpointType();
        add(listener);
        return listener;
    }
    public DatagramType createDatagramType()
    {
        DatagramType datagram = objectFactory.createDatagramType();
        add(datagram);
        return datagram;
    }
    public Nmea0183Type createNmea0183Type()
    {
        Nmea0183Type serial = objectFactory.createNmea0183Type();
        add(serial);
        return serial;
    }
    public Nmea0183HsType createNmea0183HsType()
    {
        Nmea0183HsType serial = objectFactory.createNmea0183HsType();
        add(serial);
        return serial;
    }
    public SeatalkType createSeatalkType()
    {
        SeatalkType serial = objectFactory.createSeatalkType();
        add(serial);
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
        Optional<EndpointType> opt = getRouterEndpoints().filter((e)->e.getName().equals(name)).findFirst();
        opt.get().getRoute().add(route);
        return route;
    }
    public synchronized void changeDevice(SerialType serial, String newDevice) throws IOException
    {
        String oldDevice = serial.getDevice();
        if (!newDevice.equals(oldDevice))
        {
            serial.setDevice(newDevice);
            config("%s device changed from %s to %s", serial.getName(), oldDevice, newDevice);
            store();
        }
    }
    public synchronized void updateAllDevices(List<String> allPorts) throws IOException
    {
        List<String> allDevices = nmea.getValue().getAllDevices();
        allDevices.clear();
        allDevices.addAll(allPorts);
        store();
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
    public List<String> getDontScan()
    {
        return nmea.getValue().getDontScan();
    }
    public void store() throws IOException
    {
        FileTime lmt = Files.getLastModifiedTime(path);
        if (lastModified == null || lastModified.compareTo(lmt) == 0)
        {
            try (FileWriter writer = new FileWriter(path.toFile()))
            {
                store(writer);
            }
        }
        else
        {
            warning("Not storing because %s is modified after loading", path);
        }
    }
    public synchronized void store(Writer writer) throws IOException
    {
        try
        {
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(nmea, writer);
            lastModified = Files.getLastModifiedTime(path);
        }
        catch (JAXBException ex)
        {
            throw new IOException(ex);
        }
    }

    private void checkScriptSyntax()
    {
        getRouterEndpoints().forEach((et)->
        {
            ScriptType scriptType = et.getScript();
            if (scriptType != null)
            {
                ScriptParser se = ScriptParser.newInstance();
                se.check(scriptType.getValue());
            }
        });
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

    public boolean isNativeDebug()
    {
        Boolean nativeDebug = nmea.getValue().isNativeDebug();
        return nativeDebug != null ? nativeDebug : false;
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
