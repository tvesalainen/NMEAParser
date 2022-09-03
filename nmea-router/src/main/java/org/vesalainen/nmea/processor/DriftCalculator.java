/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import static java.lang.Math.*;
import java.lang.invoke.MethodHandles;
import java.nio.channels.GatheringByteChannel;
import java.util.Collection;
import org.vesalainen.code.Property;
import org.vesalainen.navi.Navis;
import org.vesalainen.nmea.jaxb.router.DriftCalculatorType;
import org.vesalainen.parsers.nmea.NMEASentence;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DriftCalculator extends AbstractProcessorTask
{
    private final GatheringByteChannel channel;
    private double currentAngle;
    private double currentSpeed;
    private NMEASentence cur = NMEASentence.drift(()->currentAngle, ()->currentSpeed);

    private @Property float speedOverGround;
    private @Property float trueHeading;
    private @Property float trackMadeGood;
    private @Property float waterSpeed;     // speed over water
    
    public DriftCalculator(GatheringByteChannel channel, DriftCalculatorType cct)
    {
        super(MethodHandles.lookup());
        this.channel = channel;
    }
    
    @Override
    protected void commitTask(String reason, Collection<String> updatedProperties)
    {
        if (updatedProperties.contains("waterSpeed"))
        {
            double ogRad = toRadians(trackMadeGood);
            double ogY = speedOverGround*sin(ogRad);
            double ogX = speedOverGround*cos(ogRad);
            double owRad = toRadians(trueHeading);
            double owY = waterSpeed*sin(owRad);
            double owX = waterSpeed*cos(owRad);
            double curY = ogY - owY;
            double curX = ogX - owX;
            currentSpeed = hypot(curY, curX);
            currentAngle = Navis.normalizeAngle(toDegrees(atan2(curY, curX)));
            try
            {
                cur.writeTo(channel);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void stop()
    {
    }
    
}
