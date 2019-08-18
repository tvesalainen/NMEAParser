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
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.parsers.mmsi.MMSIEntry;
import org.vesalainen.parsers.mmsi.MMSIParser;
import org.vesalainen.parsers.mmsi.MMSIType;
import org.vesalainen.parsers.nmea.NMEASentence;
import static org.vesalainen.parsers.nmea.ais.CodesForShipType.NotAvailableDefault;
import static org.vesalainen.parsers.nmea.ais.EPFDFixTypes.UndefinedDefault;
import static org.vesalainen.parsers.nmea.ais.MessageTypes.StaticAndVoyageRelatedData;
import static org.vesalainen.parsers.nmea.ais.MessageTypes.StaticDataReport;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISTargetData extends AnnotatedPropertyStore
{
    @Property(ordinal=1) private int mmsi;
    @Property(ordinal=2) private String vesselName = "";
    @Property(ordinal=3) private String callSign = "";
    @Property(ordinal=4) private int imoNumber;
    @Property(ordinal=5) private CodesForShipType shipType = NotAvailableDefault;
    @Property(ordinal=6) private int dimensionToBow;
    @Property(ordinal=7) private int dimensionToStern;
    @Property(ordinal=8) private int dimensionToPort;
    @Property(ordinal=9) private int dimensionToStarboard;
    @Property(ordinal=10) private float draught;
    @Property(ordinal=14) private int unitModelCode;
    @Property(ordinal=15) private int serialNumber;
    @Property(ordinal=16) private String vendorId = "";
    @Property(ordinal=17) private int aisVersion;
    @Property(ordinal=18) private EPFDFixTypes epfd = UndefinedDefault;
    @Property(ordinal=19) private int etaMonth;
    @Property(ordinal=20) private int etaDay;
    @Property(ordinal=21) private int etaHour;
    @Property(ordinal=22) private int etaMinute;
    @Property(ordinal=23) private String destination = "";
    @Property(ordinal=24) private boolean dte;
    @Property(ordinal=25) private int mothershipMMSI;

    private MMSIEntry mmsiEntry;
    
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
    /**
     * Returns true if received all static data from msg 5 (Class A) or from
     * msg 24 A and B (Class B)
     * @return 
     */
    public boolean hasAllData()
    {
        return vesselName != null && callSign != null;
    }
    
    public NMEASentence[] getMsg5()
    {
        return new AISBuilder(StaticAndVoyageRelatedData, mmsi)
            .integer(2, aisVersion)
            .integer(30, imoNumber)
            .string(42, callSign)
            .string(120, vesselName)
            .integer(8, shipType, NotAvailableDefault)
            .integer(9, dimensionToBow)
            .integer(9, dimensionToStern)
            .integer(6, dimensionToPort)
            .integer(6, dimensionToStarboard)
            .integer(4, epfd, EPFDFixTypes.UndefinedDefault)
            .integer(4, etaMonth)
            .integer(5, etaDay)
            .integer(5, etaHour)
            .integer(6, etaMinute)
            .decimal(8, draught, 10)
            .string(120, destination)
            .bool(dte)
            .spare(1)
            .build();
    }
    protected NMEASentence[] getMsg24()
    {
        NMEASentence[] msg24A = new AISBuilder(StaticDataReport, mmsi)
                .integer(2, 0)
                .string(120, vesselName)
                .spare(8)
                .build();

        AISBuilder b24 = new AISBuilder(StaticDataReport, mmsi)
                .integer(2, 1)
            .integer(8, shipType, NotAvailableDefault)
                .string(18, vendorId)
                .integer(4, unitModelCode)
                .integer(20, serialNumber)
                .string(42, callSign);
        if (mothershipMMSI != 0)
        {
            b24.integer(30, mothershipMMSI);
        }
        else
        {
            b24.integer(9, dimensionToBow);
            b24.integer(9, dimensionToStern);
            b24.integer(6, dimensionToPort);
            b24.integer(6, dimensionToStarboard);
        }
        NMEASentence[] msg24B = b24.build();
        return new NMEASentence[]{msg24A[0], msg24B[0]};
    }
    /**
     * Returns MMSI type of target
     * @return 
     */
    public MMSIType getMMSIType()
    {
        ensureMMSIParsed();
        return mmsiEntry.getType();
    }
    /**
     * Returns country of target
     * @return 
     */
    public String getCountry()
    {
        ensureMMSIParsed();
        return mmsiEntry.getMid().getCountry();
    }
    private void ensureMMSIParsed()
    {
        if (mmsiEntry == null)
        {
            mmsiEntry = MMSIParser.PARSER.parse(mmsi);
        }
    }

    public int getMmsi()
    {
        return mmsi;
    }

    public String getVesselName()
    {
        return vesselName;
    }

    public String getCallSign()
    {
        return callSign;
    }

    public int getImoNumber()
    {
        return imoNumber;
    }

    public CodesForShipType getShipType()
    {
        return shipType;
    }

    public int getDimensionToBow()
    {
        return dimensionToBow;
    }

    public int getDimensionToStern()
    {
        return dimensionToStern;
    }

    public int getDimensionToPort()
    {
        return dimensionToPort;
    }

    public int getDimensionToStarboard()
    {
        return dimensionToStarboard;
    }

    public float getDraught()
    {
        return draught;
    }

    public int getUnitModelCode()
    {
        return unitModelCode;
    }

    public int getSerialNumber()
    {
        return serialNumber;
    }

    public String getVendorId()
    {
        return vendorId;
    }

    public int getAisVersion()
    {
        return aisVersion;
    }

    public EPFDFixTypes getEpfd()
    {
        return epfd;
    }

    public int getEtaMonth()
    {
        return etaMonth;
    }

    public int getEtaDay()
    {
        return etaDay;
    }

    public int getEtaHour()
    {
        return etaHour;
    }

    public int getEtaMinute()
    {
        return etaMinute;
    }

    public String getDestination()
    {
        return destination;
    }

    public boolean isDte()
    {
        return dte;
    }

    public int getMothershipMMSI()
    {
        return mothershipMMSI;
    }

    public MMSIEntry getMmsiEntry()
    {
        return mmsiEntry;
    }
    
}
