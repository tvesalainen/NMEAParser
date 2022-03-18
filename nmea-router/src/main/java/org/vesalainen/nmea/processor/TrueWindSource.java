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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.channels.GatheringByteChannel;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.code.Property;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.navi.Navis;
import org.vesalainen.nmea.jaxb.router.TrueWindSourceType;
import org.vesalainen.navi.TrueWindCalculator;
import org.vesalainen.parsers.nmea.NMEASentence;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TrueWindSource extends AbstractProcessorTask
{
    private @Property float trueHeading;
    private @Property float trackMadeGood;
    private @Property float relativeWindAngle;
    private @Property float relativeWindSpeed;
    private @Property float speedOverGround;

    private final GatheringByteChannel channel;
    private final TrueWindCalculator trueWindCalculator;
    private boolean overGround;
    private final NMEASentence mwv;
    private final NMEASentence wog;

    public TrueWindSource(GatheringByteChannel channel, TrueWindSourceType trueWindSourceType, ScheduledExecutorService executor)
    {
        super(MethodHandles.lookup());
        this.channel = channel;
        this.trueWindCalculator = new TrueWindCalculator();
        this.mwv = NMEASentence.mwv(trueWindCalculator::getTrueWindAngle, trueWindCalculator::getTrueWindSpeed, KNOT, true);
        this.wog = NMEASentence.windOverGround(trueWindCalculator::getTrueWindAngle, trueWindCalculator::getTrueWindSpeed);
        this.overGround = trueWindSourceType.isOverGround()!=null?trueWindSourceType.isOverGround():false;
    }

    @Override
    protected void commitTask(String reason, Collection<String> updatedProperties)
    {
        if (updatedProperties.contains("relativeWindSpeed"))
        {
            try
            {
                trueWindCalculator.setZeroAngle(trueHeading);
                trueWindCalculator.setTrueHeading(trueHeading);
                trueWindCalculator.setRelativeWindAngle(relativeWindAngle);
                trueWindCalculator.setRelativeWindSpeed(relativeWindSpeed);

                double driftAngle = Navis.angleDiff(trueHeading, trackMadeGood);
                double cor = Math.cos(Math.toRadians(driftAngle));
                trueWindCalculator.setSpeed(speedOverGround*cor);
                trueWindCalculator.setSpeedAngle(trueHeading);
                mwv.writeTo(channel);
                
                trueWindCalculator.setZeroAngle(0);
                trueWindCalculator.setSpeed(speedOverGround);
                trueWindCalculator.setSpeedAngle(trackMadeGood);
                wog.writeTo(channel);
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
