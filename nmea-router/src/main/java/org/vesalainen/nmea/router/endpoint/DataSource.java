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
package org.vesalainen.nmea.router.endpoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.logging.Logger;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.vesalainen.management.SimpleNotificationEmitter;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.nmea.router.DataSourceMXBean;
import static org.vesalainen.nmea.router.RouterManager.POOL;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class DataSource extends JavaLogging implements Runnable, DataSourceMXBean, NotificationEmitter
{
    protected final String name;
    protected long readCount;
    protected long readBytes;
    protected long writeCount;
    protected long writeBytes;
    protected long errorBytes;
    protected long lastRead;
    protected long lastWrite;
    protected SimpleNotificationEmitter emitter;
    protected ObjectName objectName;

    public DataSource(String name)
    {
        this.name = name;
        try
        {
            this.objectName = getObjectName();
        }
        catch (MalformedObjectNameException ex)
        {
            throw new RuntimeException(ex);
        }
        this.emitter = new SimpleNotificationEmitter(
                POOL, 
                "org.vesalainen.nmea.router.endpoint.notification", 
                objectName, 
                new MBeanNotificationInfo(
                        new String[]{"org.vesalainen.nmea.router.endpoint.notification"},
                        "javax.management.Notification",
                        "NMEA router message/error")
                );
        setLogger(Logger.getLogger(this.getClass().getName().replace('$', '.') + "." + name));
    }

    protected ObjectName getObjectName() throws MalformedObjectNameException
    {
        return new ObjectName("org.vesalainen.nmea.router.endpoint:type="+name);
    }
    public abstract int write(ByteBuffer readBuffer) throws IOException;

    public abstract int write(Endpoint src, RingByteBuffer ring) throws IOException;

    public synchronized void sendNotification(Supplier<String> textSupplier, Supplier<byte[]> userDataSupplier, LongSupplier timestampSupplier)
    {
        emitter.sendNotification(textSupplier, userDataSupplier, timestampSupplier);
    }

    public synchronized void sendNotification(String text, byte[] userData, long timestamp)
    {
        emitter.sendNotification(text, userData, timestamp);
    }

    @Override
    public synchronized void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
    {
        emitter.removeNotificationListener(listener, filter, handback);
    }

    @Override
    public synchronized void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
    {
        emitter.addNotificationListener(listener, filter, handback);
    }

    @Override
    public synchronized void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
    {
        emitter.removeNotificationListener(listener);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo()
    {
        return emitter.getNotificationInfo();
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public long getReadCount()
    {
        return readCount;
    }

    @Override
    public long getReadBytes()
    {
        return readBytes;
    }

    @Override
    public long getWriteCount()
    {
        return writeCount;
    }

    @Override
    public long getWriteBytes()
    {
        return writeBytes;
    }

    @Override
    public long getErrorBytes()
    {
        return errorBytes;
    }

    @Override
    public Date getLastRead()
    {
        return new Date(lastRead);
    }

    @Override
    public Date getLastWrite()
    {
        return new Date(lastWrite);
    }

}
