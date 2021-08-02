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
package org.vesalainen.nmea.processor;

import org.vesalainen.nmea.util.AbstractSampleConsumer;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.vesalainen.net.ObjectServer;
import org.vesalainen.nmea.jaxb.router.AisLogType;
import org.vesalainen.nmea.jaxb.router.AnchorManagerType;
import org.vesalainen.nmea.jaxb.router.CompassCorrectorType;
import org.vesalainen.nmea.jaxb.router.CompressedLogType;
import org.vesalainen.nmea.jaxb.router.N2KGatewayType;
import org.vesalainen.nmea.jaxb.router.ProcessorType;
import org.vesalainen.nmea.jaxb.router.SntpServerType;
import org.vesalainen.nmea.jaxb.router.TrackerType;
import org.vesalainen.nmea.jaxb.router.TrueWindSourceType;
import org.vesalainen.nmea.jaxb.router.VariationSourceType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class Processor<T extends ByteChannel & ScatteringByteChannel & GatheringByteChannel> extends NMEAService<T> implements Runnable, AutoCloseable
{
    private ProcessorType processorType;
    private List<Stoppable> processes = new ArrayList<>();
    private ObjectServer dataServer;

    public Processor(ProcessorType processorType, T channel, CachedScheduledThreadPool executor) throws IOException
    {
        super(channel, executor);
        this.processorType = processorType;
    }

    @Override
    public void start()
    {
        try
        {
            super.start();
            if (processorType.getDataAccessPort() != null)
            {
                dataServer = new ObjectServer(processorType.getDataAccessPort(), executor);
                dataServer.start();
            }
            if (running != null)
            {
                running.await();
            }
            if (!clock.waitUntilReady(5, TimeUnit.SECONDS))
            {
                warning("Clock didn't start in 5 seconds");
            }
            for (Object ob : processorType.getVariationSourceOrTrueWindSourceOrTracker())
            {
                if (ob instanceof N2KGatewayType)
                {
                    info("starting N2K Gateway");
                    N2KGatewayType type = (N2KGatewayType) ob;
                    N2KGateway n2kGateway = new N2KGateway(type, channel, executor);
                    processes.add(n2kGateway);
                    n2kGateway.start();
                    
                    continue;
                }
                if (ob instanceof CompassCorrectorType)
                {
                    info("starting Compass Corrector");
                    CompassCorrectorType type = (CompassCorrectorType) ob;
                    CompassCorrector compassCorrector = new CompassCorrector(type, channel, executor, this);
                    processes.add(compassCorrector);
                    addNMEAObserver(compassCorrector);
                    
                    continue;
                }
                if (ob instanceof AisLogType)
                {
                    info("starting AIS Log");
                    AisLogType type = (AisLogType) ob;
                    String directory = type.getDirectory();
                    Long ttlMinutes = type.getTtlMinutes();
                    BigInteger maxLogSize = type.getMaxLogSize();
                    AISLog aisLog = AISLog.getInstance(
                            this, 
                            Paths.get(directory), 
                            ttlMinutes != null ? ttlMinutes : 10, 
                            maxLogSize != null ? maxLogSize.longValue() : 1024*1024, 
                            executor,
                            channel);
                    processes.add(aisLog);
                    continue;
                }
                if (ob instanceof CompressedLogType)
                {
                    info("starting CompressedLog");
                    CompressedLogType type = (CompressedLogType) ob;
                    CompressedLog compressedLog = new CompressedLog(type, executor);
                    processes.add(compressedLog);
                    addNMEAObserver(compressedLog);
                    
                    continue;
                }
                AbstractSampleConsumer process = null;
                if (ob instanceof VariationSourceType)
                {
                    VariationSourceType vst = (VariationSourceType) ob;
                    info("starting VariationSource");
                    process = new VariationSource(channel, vst, (ScheduledExecutorService) executor);
                }
                if (ob instanceof TrueWindSourceType)
                {
                    TrueWindSourceType vst = (TrueWindSourceType) ob;
                    info("starting TrueWindSource");
                    process = new TrueWindSource(channel, vst, (ScheduledExecutorService) executor);
                }
                if (ob instanceof TrackerType)
                {
                    TrackerType tt = (TrackerType) ob;
                    info("starting Tracker");
                    process = new Tracker(tt, (ScheduledExecutorService) executor);
                }
                if (ob instanceof SntpServerType)
                {
                    info("starting SntpServer");
                    SntpServerType sntpServerType = (SntpServerType) ob;
                    SNTPServerProc server = new SNTPServerProc(sntpServerType, executor);
                    processes.add(server);
                    addNMEAObserver(server);
                    server.start("");
                    continue;
                }
                if (ob instanceof AnchorManagerType)
                {
                    info("starting AnchorManager");
                    AnchorManagerType anchorManagerType = (AnchorManagerType) ob;
                    AnchorManager anchorManager = new AnchorManager(this, channel, anchorManagerType, executor);
                    processes.add(anchorManager);
                    addNMEAObserver(anchorManager);
                    anchorManager.start("");
                    continue;
                }
                if (process == null)
                {
                    throw new UnsupportedOperationException(ob+" not supported");
                }
                process.start(this);
                processes.add(process);
            }
        }
        catch (IOException | InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public void setData(String name, Object target)
    {
        if (dataServer != null)
        {
            dataServer.put(name, target);
        }
        else
        {
            warning("dataServer not configured");
        }
    }
    @Override
    public void stop()
    {
        processes.stream().forEach((process) ->
        {
            process.stop();
        });
        if (dataServer != null)
        {
            dataServer.stop();
        }
        super.stop();
    }
    
}
