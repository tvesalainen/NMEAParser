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
import org.vesalainen.navi.WayPoint;
import org.vesalainen.parsers.mmsi.MMSIType;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.navi.cpa.Course;
import org.vesalainen.parsers.nmea.NMEASentence;

/**
 * @author Timo Vesalainen
 */
public class AISTarget implements BoatPosition, WayPoint, Course, Comparable<AISTarget>
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
    private double distance;    // from own target in NM
    private double estimatedDistance;    // from own target in NM
    private double estimatedLatitude;
    private double estimatedLongitude;
    private double cpaTime;
    private double cpaDistance;
    private AISTarget ownTarget;
    private boolean updated;
    private long lastUpdate;
    private char deviceClass;

    public AISTarget(long maxLogSize, CachedScheduledThreadPool executor, int mmsi, Path dir, AISTargetData data, AISTargetDynamic dynamic)
    {
        this.maxLogSize = maxLogSize;
        this.executor = executor;
        this.dataPath = AISService.getDataPath(dir, mmsi);
        this.dynamicPath = AISService.getDynamicPath(dir, mmsi);
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
    public void update(MessageTypes type, AISTargetData dat, AISTargetDynamic dyn, Collection<String> updatedProperties) throws IOException
    {
        detectClass(type);
        data.copyFrom(dat, updatedProperties, false);
        dynamic.copyFrom(dyn, updatedProperties, false);
        dynamic.setTimestamp(dyn.getTimestamp());
        if (dataPath != null && dynamic.getChannel() != '-')    // radio messages has channel A/B
        {
            dynamic.print(log, logExists);
            logExists = true;
        }
        updated = true;
    }
    public void setOwnTarget(AISTarget ownTarget)
    {
        this.ownTarget = ownTarget;
    }
    private void calc()
    {
        if (updated && ownTarget != null)
        {
            double x0 = getLatitude()-ownTarget.getLatitude();
            double x1 = deltaLatitude()-ownTarget.deltaLatitude();
            double x2 = getLongitude()-ownTarget.getLongitude();
            double x3 = deltaLongitude()-ownTarget.deltaLongitude();
            double x4 = x0*x0;
            double x5 = x0*x1;
            double x6 = x1*x0;
            double x7 = x5+x6;
            double x8 = x1*x1;
            double x9 = x2*x2;
            double x10 = x2*x3;
            double x11 = x3*x2;
            double x12 = x10+x11;
            double x13 = x3*x3;
            double x14 = x4+x9;
            double x15 = x7+x12;
            double x16 = x8+x13;
            double x17 = 1*x15;
            double x18 = 2*x16;
            // distance is sqrt((x16*t*t+x15*t+x14)
            // derivative of distance is 2*x16+x15 (= x18*t+x17)
            // x16 >= 0
            // x15 < 0 when distance minimum is in future
            distance = Math.sqrt(x14)*60.0; // t = 0
            if (x15 < 0)
            {
                double t = (-x17/x18);
                cpaTime = t/60000.0;
                cpaDistance = Math.sqrt((x16*t+x15)*t+x14)*60.0;
            }
            else
            {
                cpaTime = Double.NaN;
                cpaDistance = Double.NaN;
            }
            updated = false;
        }
    }

    public double getCPADistance()
    {
        calc();
        return cpaDistance;
    }
    /**
     * Returns time to collision in minutes
     * @return 
     */
    public double getCPATime()
    {
        calc();
        return cpaTime;
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

    public AISTargetData getData()
    {
        return data;
    }

    public AISTargetDynamic getDynamic()
    {
        return dynamic;
    }
    
    /**
     * Returns MMSI type of target
     * @return 
     */
    public MMSIType getMMSIType()
    {
        return data.getMMSIType();
    }
    /**
     * Returns country of target
     * @return 
     */
    public String getCountry()
    {
        return data.getCountry();
    }
    /**
     * {@inheritDoc}
     * @return 
     */
    public boolean hasAllData()
    {
        return data.hasAllData() && deviceClass != 0;
    }
    /**
     * Returns true is target is class A. This is unreliable before hasAllData()
     * returns true.
     * @return 
     */
    public boolean isClassA()
    {
        return deviceClass == 'A';
    }
    public NMEASentence[] getPositionReport()
    {
        if (hasAllData())
        {
            if (isClassA())
            {
                return dynamic.getMsg1();
            }
            else
            {
                return dynamic.getMsg18();
            }
        }
        else
        {
            return AISBuilder.EMPTY;
        }
    }
    public NMEASentence[] getStaticReport()
    {
        if (hasAllData())
        {
            if (isClassA())
            {
                return data.getMsg5();
            }
            else
            {
                return data.getMsg24();
            }
        }
        else
        {
            return AISBuilder.EMPTY;
        }
    }    
    /**
     * Returns distance to ownTarget in NM
     * @return 
     */
    public double getDistance()
    {
        calc();
        return distance;
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
    @Override
    public double getLatitude()
    {
        return centerLatitude(dynamic.getLatitude(), dynamic.getLongitude(), dynamic.getHeading());
    }
    /**
     * Returns center longitude corrected with dimensions
     * @return 
     */
    @Override
    public double getLongitude()
    {
        return centerLongitude(dynamic.getLatitude(), dynamic.getLongitude(), dynamic.getHeading());
    }

    @Override
    public long getTime()
    {
        return dynamic.getTimestamp().toEpochMilli();
    }

    /**
     * Returns latitude reported by target
     * @return 
     */
    public double getAntennaLatitude()
    {
        return dynamic.getLatitude();
    }
    /**
     * Returns longitude reported by target
     * @return 
     */
    public double getAntennaLongitude()
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
        return dynamic.isCsUnit();
    }

    public boolean isDisplay()
    {
        return dynamic.isDisplay();
    }

    public boolean isDsc()
    {
        return dynamic.isDsc();
    }

    public int getUnitModelCode()
    {
        return data.getUnitModelCode();
    }

    public boolean isPositionAccuracy()
    {
        return dynamic.isPositionAccuracy();
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

    public Instant getTimestamp()
    {
        return dynamic.getTimestamp();
    }

    @Override
    public double getCourse()
    {
        return dynamic.getCourse();
    }

    @Override
    public double getSpeed()
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
        return data.getDestination();
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

    public EPFDFixTypes getEpfd()
    {
        return data.getEpfd();
    }

    public int getEtaMonth()
    {
        return data.getEtaMonth();
    }

    public int getEtaDay()
    {
        return data.getEtaDay();
    }

    public int getEtaHour()
    {
        return data.getEtaHour();
    }

    public int getEtaMinute()
    {
        return data.getEtaMinute();
    }

    @Override
    public String toString()
    {
        return "AISTarget{" + getMmsi() + " "+ getVesselName() + " distance="+ getDistance() + " cpa=" + getCPATime() + '}';
    }

    @Override
    public int compareTo(AISTarget o)
    {
        return Double.compare(distance, o.distance);
    }

    private void detectClass(MessageTypes type)
    {
        switch (type)
        {
            case PositionReportClassA:
            case PositionReportClassAAssignedSchedule:
            case PositionReportClassAResponseToInterrogation:
            case StandardSARAircraftPositionReport:
            case StaticAndVoyageRelatedData:
                deviceClass = 'A';
                break;
            case StandardClassBCSPositionReport:
            case ExtendedClassBEquipmentPositionReport:
            case StaticDataReport:
                deviceClass = 'B';
                break;
        }
    }

    
}
