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
import java.lang.invoke.MethodHandles;
import java.nio.channels.GatheringByteChannel;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.Navis;
import org.vesalainen.nmea.jaxb.router.TrueWindSourceType;
import org.vesalainen.navi.TrueWind;
import org.vesalainen.nmea.util.NMEAFilters;
import org.vesalainen.nmea.util.NMEAMappers;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.util.navi.Velocity;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TrueWindSource extends AnnotatedPropertyStore implements Stoppable
{
    @Property private float trueHeading;
    @Property private float trackMadeGood;
    @Property private float relativeWindAngle;
    @Property private float relativeWindSpeed;
    @Property private float speedOverGround;

    private final GatheringByteChannel channel;
    private final TrueWind trueWind;
    private final NMEASentence mwv;

    public TrueWindSource(GatheringByteChannel channel, TrueWindSourceType trueWindSourceType, ScheduledExecutorService executor)
    {
        super(MethodHandles.lookup());
        this.channel = channel;
        this.trueWind = new TrueWind();
        this.mwv = NMEASentence.mwv(trueWind::getTrueAngle, trueWind::getTrueSpeed, KNOT, true);
    }

    @Override
    public void commit(String reason, Collection<String> updatedProperties)
    {
        if (updatedProperties.contains("relativeWindSpeed"))
        {
            try
            {
                trueWind.setBoatSpeed(speedOverGround);
                double driftAngle = Navis.angleDiff(trueHeading, trackMadeGood);
                trueWind.setDriftAngle(driftAngle);
                trueWind.setRelativeAngle(relativeWindAngle);
                trueWind.setRelativeSpeed(relativeWindSpeed);
                trueWind.calc();
                finest("%s", trueWind);
                mwv.writeTo(channel);
                finest(trueWind::toString);
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "TrueWindSource %s", ex.getMessage());
            }
        }
    }

    @Override
    public void stop()
    {
    }
    
}
