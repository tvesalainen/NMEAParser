/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.logging.Level;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.nmea.jaxb.router.CompassCorrectorType;
import static org.vesalainen.nmea.processor.GeoMagManager.Type.DECLINATION;
import org.vesalainen.nmea.processor.deviation.DeviationManager;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.MessageType;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.AttachedLogger;

/**
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompassCorrector extends AnnotatedPropertyStore implements Transactional, Stoppable
{
    private WritableByteChannel out;
    @Property private Clock clock = Clock.systemUTC();
    @Property private double latitude;
    @Property private double longitude;
    @Property private float magneticHeading;
    @Property private MessageType messageType;
    private final Path path;
    private final GeoMagManager geoMagMgr;
    private DeviationManager deviationMgr;
    private final CachedScheduledThreadPool executor;
    private final NMEAService service;
    private CompassCalibrator calibrator;

    public CompassCorrector(CompassCorrectorType type, WritableByteChannel out, CachedScheduledThreadPool executor, NMEAService service) throws IOException
    {
        super(MethodHandles.lookup());
        this.out = out;
        this.executor = executor;
        this.service = service;
        this.path = Paths.get(type.getConfigFile());
        this.geoMagMgr = new GeoMagManager();
        geoMagMgr.addObserver(DECLINATION, 0.1, this::updateVariance);
    }

    private void updateVariance(double variance)
    {
        executor.submit(()->updVar(variance));
    }
    private void updVar(double variance)
    {
        if (deviationMgr == null)
        {
            try
            {
                deviationMgr = new DeviationManager(path, variance);
                calibrator = new CompassCalibrator(deviationMgr, path, variance, executor);
                calibrator.attach(service);
            }
            catch (Exception ex)
            {
                log(Level.SEVERE, ex, "%s", ex.getMessage());
            }
        }
        else
        {
            deviationMgr.updateVariation(variance);
        }
    }
    @Override
    public void commit(String reason)
    {
        if (messageType != null)
        {
            switch (messageType)
            {
                case RMC:
                    geoMagMgr.update(latitude, longitude, 0);
                    break;
                case HDG:
                    if (deviationMgr != null)
                    {
                        try 
                        {
                            ByteBuffer bb = deviationMgr.getHDT(magneticHeading);
                            if (bb != null)
                            {
                                out.write(bb);
                                bb.flip();
                            }
                        }
                        catch (IOException ex) 
                        {
                            throw new IllegalArgumentException(ex);
                        }
                        deviationMgr.updateMagneticHeading(magneticHeading);
                    }
                    break;
            }
        }
    }

    @Override
    public void rollback(String reason)
    {
    }

    @Override
    public void stop()
    {
        if (calibrator != null)
        {
            calibrator.detach(service);
        }
    }

}
