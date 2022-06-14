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
import java.net.URL;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.parsers.mmsi.MMSIEntry;
import org.vesalainen.parsers.mmsi.MMSIParser;
import org.vesalainen.parsers.mmsi.MMSIType;
import org.vesalainen.parsers.nmea.NMEASentence;
import static org.vesalainen.parsers.nmea.ais.CodesForShipType.NotAvailableDefault;
import static org.vesalainen.parsers.nmea.ais.EPFDFixTypes.UndefinedDefault;

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

    public AISTargetData(URL url) throws IOException
    {
        super(MethodHandles.lookup(), url);
    }

    public AISTargetData(URL url, boolean reportMissingProperties) throws IOException
    {
        super(MethodHandles.lookup(), url, reportMissingProperties);
    }
    /**
     * Returns true if received all static data from msg 5 (Class A) or from
     * msg 24 A and B (Class B)
     * @return 
     */
    public boolean hasAllData()
    {
        return vesselName != null && !vesselName.isEmpty() && callSign != null && !callSign.isEmpty();
    }
    public NMEASentence[] getMsg5()
    {
        return AISSentence.getMsg5(mmsi, aisVersion, imoNumber, callSign, vesselName, shipType, dimensionToBow, dimensionToStern, dimensionToPort, dimensionToStarboard, epfd, etaMonth, etaDay, etaHour, etaMinute, draught, destination, dte);
    }
    protected NMEASentence[] getMsg24()
    {
        return AISSentence.getMsg24(mmsi, callSign, vesselName, shipType, dimensionToBow, dimensionToStern, dimensionToPort, dimensionToStarboard, vendorId, unitModelCode, serialNumber, mothershipMMSI);
    }
    /**
     * Returns MMSI type of target
     * @return 
     */
    public String getMMSIType()
    {
        ensureMMSIParsed();
        if (mmsiEntry != null)
        {
            return mmsiEntry.getType().toString();
        }
        else
        {
            return "???";
        }
    }
    /**
     * Returns country of target
     * @return 
     */
    public String getCountry()
    {
        ensureMMSIParsed();
        if (mmsiEntry != null)
        {
            return mmsiEntry.getMid().getCountry();
        }
        else
        {
            return "???";
        }
    }
    private void ensureMMSIParsed()
    {
        if (mmsiEntry == null)
        {
            try
            {
                mmsiEntry = MMSIParser.PARSER.parse(mmsi);
            }
            catch (Exception ex)
            {
            }
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
