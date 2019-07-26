/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.parsers.nmea.ais;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Calendar;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISTargetData extends AnnotatedPropertyStore
{
    @Property(ordinal=1) private int mmsi;
    @Property(ordinal=2) private String vesselName;
    @Property(ordinal=3) private String callSign;
    @Property(ordinal=4) private int imoNumber;
    @Property(ordinal=5) private CodesForShipType shipType;
    @Property(ordinal=6) private int dimensionToBow;
    @Property(ordinal=7) private int dimensionToStern;
    @Property(ordinal=8) private int dimensionToPort;
    @Property(ordinal=9) private int dimensionToStarboard;
    @Property(ordinal=10) private float draught;
    @Property(ordinal=11) private boolean csUnit;
    @Property(ordinal=12) private boolean display;
    @Property(ordinal=13) private boolean dsc;
    @Property(ordinal=13) private int unitModelCode;
    @Property(ordinal=13) private boolean positionAccuracy;
    @Property(ordinal=13) private int serialNumber;
    @Property(ordinal=13) private String vendorId;

    public AISTargetData()
    {
        super(MethodHandles.lookup());
    }

    public AISTargetData(AnnotatedPropertyStore aps)
    {
        super(aps);
    }

    public AISTargetData(Path path) throws IOException
    {
        super(MethodHandles.lookup(), path);
    }

    public AISTargetData(Path path, boolean reportMissingProperties) throws IOException
    {
        super(MethodHandles.lookup(), path, reportMissingProperties);
    }
    
}
