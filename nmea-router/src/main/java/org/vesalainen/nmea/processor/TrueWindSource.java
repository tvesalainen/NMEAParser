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
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.zip.CheckedOutputStream;
import org.vesalainen.math.UnitType;
import org.vesalainen.nio.ByteBufferOutputStream;
import org.vesalainen.nmea.jaxb.router.TrueWindSourceType;
import org.vesalainen.navi.TrueWind;
import org.vesalainen.nmea.util.NMEAFilters;
import org.vesalainen.nmea.util.NMEAMappers;
import org.vesalainen.nmea.util.NMEASample;
import org.vesalainen.parsers.nmea.NMEAChecksum;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.util.navi.Velocity;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TrueWindSource extends AbstractSampleConsumer
{
    private static final String[] Prefixes = new String[]{
        "trueHeading",
        "trackMadeGood",
        "relativeWindAngle",
        "windSpeed",
        "speedOverGround"
            };
    private final GatheringByteChannel channel;
    private final TrueWind trueWind = new TrueWind();
    private final ByteBuffer bb = ByteBuffer.allocateDirect(100);

    public TrueWindSource(GatheringByteChannel channel, TrueWindSourceType trueWindSourceType)
    {
        super(TrueWindSource.class);
        this.channel = channel;
    }
    
    @Override
    public String[] getProperties()
    {
        return Prefixes;
    }

    @Override
    public void init(Stream<NMEASample> stream)
    {
        this.stream = stream
                .map(NMEAFilters.accumulatorMap())
                .filter(NMEAFilters.containsAllFilter("relativeWindAngle", "windSpeed", "speedOverGround"))
                .map(NMEAMappers.driftAngleMap());
    }

    @Override
    protected void process(NMEASample sample)
    {
        bb.clear();
        try
        {
            trueWind.setBoatSpeed(sample.getProperty("speedOverGround"));
            trueWind.setDriftAngle(sample.getProperty("driftAngle"));
            trueWind.setRelativeAngle(sample.getProperty("relativeWindAngle"));
            trueWind.setRelativeSpeed(Velocity.toKnots(sample.getProperty("windSpeed")));
            trueWind.calc();
            finest("%s", trueWind);
            int trueAngle = (int) trueWind.getTrueAngle();
            double trueSpeed = trueWind.getTrueSpeed();
            NMEASentence mwv = NMEASentence.mwv(trueAngle, trueSpeed, UnitType.Knot, true);
            bb.clear();
            mwv.writeTo(bb);
            bb.flip();
            channel.write(bb);
            finest("send MWV trueAngle=%d trueSpeed=%f", trueAngle, trueSpeed);
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

}
