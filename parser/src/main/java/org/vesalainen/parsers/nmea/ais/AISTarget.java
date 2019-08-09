/*
 * Copyright (C) 2013 Timo Vesalainen
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import org.vesalainen.navi.BoatPosition;
import org.vesalainen.parsers.mmsi.MMSIEntry;
import org.vesalainen.parsers.mmsi.MMSIParser;
import org.vesalainen.parsers.mmsi.MMSIType;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 * @author Timo Vesalainen
 */
public class AISTarget implements BoatPosition
{
    private CachedScheduledThreadPool executor;
    private long maxLogSize;
    private Path dataPath;
    private Path dynamicPath;
    private byte[] dataDigest;
    private AISLogFile log;
    private boolean logExists;
    private AISTargetData data;
    private AISTargetDynamic dynamic;
    private MMSIEntry mmsiEntry;

    public AISTarget(long maxLogSize, CachedScheduledThreadPool executor, int mmsi, Path dir, AISTargetData data, AISTargetDynamic dynamic)
    {
        this.maxLogSize = maxLogSize;
        this.executor = executor;
        if (dir != null)
        {
            this.dataPath = dir.resolve(mmsi+".dat");
            this.dynamicPath = dir.resolve(mmsi+".log");
        }
        this.data = data;
        this.dynamic = dynamic;
        this.dataDigest = data.getSha1();
    }
    public void open() throws IOException
    {
        if (dataPath != null)
        {
            logExists = Files.exists(dynamicPath);
            log = new AISLogFile(dynamicPath, maxLogSize, executor);
        }
    }
    public void update(AISTargetData dat, AISTargetDynamic dyn, Collection<String> updatedProperties) throws IOException
    {
        data.copyFrom(dat, updatedProperties, false);
        dynamic.copyFrom(dyn, updatedProperties, false);
        dynamic.setInstant(dyn.getInstant());
        if (dataPath != null && dynamic.getChannel() != '-')    // radio messages has channel A/B
        {
            dynamic.print(log, logExists);
            logExists = true;
        }
    }
    public void close() throws IOException
    {
        if (dataPath != null)
        {
            log.close();
            byte[] sha1 = data.getSha1();
            if (!Arrays.equals(dataDigest, sha1))
            {
                try
                {
                    data.store(dataPath);
                }
                catch (IOException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
        }
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
    /**
     * Returns AIS antenna distance to starboard side.
     * @return 
     */
    @Override
    public double getDimensionToStarboard()
    {
        return data.getDimensionToStarboard();
    }
    /**
     * Returns AIS antenna distance to port side.
     * @return 
     */
    @Override
    public double getDimensionToPort()
    {
        return data.getDimensionToPort();
    }
    /**
     * Returns AIS antenna distance to bow.
     * @return 
     */
    @Override
    public double getDimensionToBow()
    {
        return data.getDimensionToBow();
    }
    /**
     * Returns AIS antenna distance to stern.
     * @return 
     */
    @Override
    public double getDimensionToStern()
    {
        return data.getDimensionToStern();
    }
    /**
     * Returns center latitude corrected with dimensions
     * @return 
     */
    public double getCenterLatitude()
    {
        return centerLatitude(dynamic.getLatitude(), dynamic.getLongitude(), dynamic.getHeading());
    }
    /**
     * Returns center longitude corrected with dimensions
     * @return 
     */
    public double getCenterLongitude()
    {
        return centerLongitude(dynamic.getLatitude(), dynamic.getLongitude(), dynamic.getHeading());
    }

    /**
     * Returns latitude reported by target
     * @return 
     */
    public double getLatitude()
    {
        return dynamic.getLatitude();
    }
    /**
     * Returns longitude reported by target
     * @return 
     */
    public double getLongitude()
    {
        return dynamic.getLongitude();
    }

    public int getMmsi()
    {
        return data.getMmsi();
    }

    public String getVesselName()
    {
        return data.getVesselName();
    }

    public String getCallSign()
    {
        return data.getCallSign();
    }

    public int getImoNumber()
    {
        return data.getImoNumber();
    }

    public CodesForShipType getShipType()
    {
        return data.getShipType();
    }

    public float getDraught()
    {
        return data.getDraught();
    }

    public boolean isCsUnit()
    {
        return data.isCsUnit();
    }

    public boolean isDisplay()
    {
        return data.isDisplay();
    }

    public boolean isDsc()
    {
        return data.isDsc();
    }

    public int getUnitModelCode()
    {
        return data.getUnitModelCode();
    }

    public boolean isPositionAccuracy()
    {
        return data.isPositionAccuracy();
    }

    public int getSerialNumber()
    {
        return data.getSerialNumber();
    }

    public String getVendorId()
    {
        return data.getVendorId();
    }

    public MessageTypes getMessageType()
    {
        return dynamic.getMessageType();
    }

    public Instant getInstant()
    {
        return dynamic.getInstant();
    }

    public float getCourse()
    {
        return dynamic.getCourse();
    }

    public float getSpeed()
    {
        return dynamic.getSpeed();
    }

    public int getHeading()
    {
        return dynamic.getHeading();
    }

    public float getRateOfTurn()
    {
        return dynamic.getRateOfTurn();
    }

    public NavigationStatus getNavigationStatus()
    {
        return dynamic.getNavigationStatus();
    }

    public ManeuverIndicator getManeuver()
    {
        return dynamic.getManeuver();
    }

    public char getChannel()
    {
        return dynamic.getChannel();
    }

    public int getAltitude()
    {
        return dynamic.getAltitude();
    }

    public String getDestination()
    {
        return dynamic.getDestination();
    }

    public boolean isBand()
    {
        return dynamic.isBand();
    }

    public boolean isMsg22()
    {
        return dynamic.isMsg22();
    }

    public boolean isAssignedMode()
    {
        return dynamic.isAssignedMode();
    }

    public boolean isRaim()
    {
        return dynamic.isRaim();
    }

    public int getRadioStatus()
    {
        return dynamic.getRadioStatus();
    }

    public EPFDFixTypes getEpfdFixTypes()
    {
        return dynamic.getEpfdFixTypes();
    }

    public int getEtaMonth()
    {
        return dynamic.getEtaMonth();
    }

    public int getEtaDay()
    {
        return dynamic.getEtaDay();
    }

    public int getEtaHour()
    {
        return dynamic.getEtaHour();
    }

    public int getEtaMinute()
    {
        return dynamic.getEtaMinute();
    }

    private void ensureMMSIParsed()
    {
        if (mmsiEntry == null)
        {
            mmsiEntry = MMSIParser.PARSER.parse(data.getMmsi());
        }
    }
    
}
