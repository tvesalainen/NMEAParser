/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.router.endpoint.n2kgw;

import d3.env.TSAGeoMag;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.channels.WritableByteChannel;
import java.time.Clock;
import java.time.ZonedDateTime;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.math.UnitType;
import static org.vesalainen.math.UnitType.*;
import org.vesalainen.parsers.nmea.NMEAPGN;
import static org.vesalainen.parsers.nmea.NMEAPGN.*;
import org.vesalainen.parsers.nmea.NMEASentence;
import org.vesalainen.time.SimpleClock;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NMEASender extends AnnotatedPropertyStore
{
    private @Property long millis;
    private @Property int canId;
    private @Property double latitude;
    private @Property double longitude;
    private @Property float depthOfWaterRelativeToTransducer;
    private @Property float transducerOffset;
    private @Property float maximumRangeScale;
    private @Property float waterTemperature;
    private @Property float waterSpeed;
    private @Property float trueHeading;
    private @Property float relativeWindAngle;
    private @Property float relativeWindSpeed;
    private @Property float yaw;
    private @Property float pitch;
    private @Property float roll;
    private @Property float speedOverGround;
    private @Property float trackMadeGood;
    private @Property int methodGnss;
    private @Property int batteryInstance;
    private @Property float voltage;
    private @Property float current;
    private @Property float temperature;
    private @Property float outsideTemperature;
    private @Property float atmosphericPressure;
    private @Property int stateOfCharge;
    private @Property int stateOfHealth;
    
    private Clock frameClock;
    private N2KClock positionClock;
    private final SourceManager sourceManager;
    private final WritableByteChannel channel;
    private final TSAGeoMag geoMag = new TSAGeoMag();
    
    private final NMEASentence rmc;
    private final NMEASentence rmc2;
    private final NMEASentence dpt;
    private final NMEASentence hdt;
    private final NMEASentence hdg;
    private final NMEASentence mtw;
    private final NMEASentence mwv;
    private final NMEASentence vhw;
    private final NMEASentence attitude;
    private final NMEASentence battery;
    private final NMEASentence environmental;
    private final NMEASentence dcDetailedStatus;
    
    public NMEASender(SourceManager sourceManager, WritableByteChannel channel)
    {
        super(MethodHandles.lookup());
        this.frameClock = new SimpleClock(()->millis);
        this.positionClock = new N2KClock();
        this.sourceManager = sourceManager;
        this.channel = channel;
        
        this.rmc = NMEASentence.rmc(
                ()->sourceManager.getTalkerId(canId),
                ()->frameClock,
                ()->'A',
                ()->latitude, 
                ()->longitude, 
                ()->speedOverGround, 
                METERS_PER_SECOND,
                ()->trackMadeGood, 
                ()->magneticVariation(frameClock),
                ()->'A');
        this.rmc2 = NMEASentence.rmc(
                ()->sourceManager.getTalkerId(canId),
                ()->positionClock, 
                this::status,
                ()->latitude, 
                ()->longitude, 
                ()->speedOverGround, 
                METERS_PER_SECOND,
                ()->trackMadeGood, 
                ()->magneticVariation(positionClock),
                this::faa);
        this.dpt = NMEASentence.dpt(
                ()->sourceManager.getTalkerId(canId), 
                ()->depthOfWaterRelativeToTransducer,
                ()->transducerOffset,
                ()->maximumRangeScale,
                UnitType.METER);
        this.hdt = NMEASentence.hdt(
                ()->sourceManager.getTalkerId(canId), 
                ()->trueHeading);
        this.hdg = NMEASentence.hdg(
                ()->sourceManager.getTalkerId(canId), 
                ()->trueHeading,
                ()->magneticVariation(positionClock));
        this.mtw = NMEASentence.mtw(
                ()->sourceManager.getTalkerId(canId), 
                ()->waterTemperature, 
                UnitType.CELSIUS);
        this.mwv = NMEASentence.mwv(
                ()->sourceManager.getTalkerId(canId), 
                ()->relativeWindAngle, 
                ()->relativeWindSpeed, 
                METERS_PER_SECOND, 
                false);
        this.vhw = NMEASentence.vhw(
                ()->sourceManager.getTalkerId(canId), 
                ()->waterSpeed, 
                METERS_PER_SECOND);
        this.attitude = NMEASentence.attitude(
                ()->sourceManager.getTalkerId(canId),
                ()->yaw,
                ()->pitch,
                ()->roll);
        this.battery = NMEASentence.battery(
                ()->sourceManager.getTalkerId(canId),
                ()->batteryInstance+sourceManager.getInstanceOffset(canId),
                ()->voltage,
                ()->current,
                ()->temperature);
        this.environmental = NMEASentence.environmental(
                ()->sourceManager.getTalkerId(canId),
                ()->outsideTemperature,
                ()->waterTemperature,
                ()->atmosphericPressure/1000F);
        this.dcDetailedStatus = NMEASentence.dcDetailedStatus(
                ()->sourceManager.getTalkerId(canId),
                ()->batteryInstance+sourceManager.getInstanceOffset(canId),
                ()->stateOfCharge,
                ()->stateOfHealth);
        
    }
    private double magneticVariation(Clock clock)
    {
        ZonedDateTime now = ZonedDateTime.now(clock);
        return geoMag.getDeclination(latitude, longitude, (double)(now.getYear()+now.getDayOfYear()/365.0), 0);
    }
    private char status()
    {
        switch (methodGnss)
        {
            case 0: //no GNSS
            case 6: //Estimated (DR) mode
            case 7: //Manual Input
            case 8: //Simulate mode
                return 'V'; // not valid
            case 1: //GNSS fix
            case 3: //Precise GNSS
            case 2: //DGNSS fix
            case 4: //RTK Fixed Integer
            case 5: //RTK float
                return 'A'; // autonomous
            default:
                return 'V';
        }
    }
    private char faa()
    {
        switch (methodGnss)
        {
            case 0: //no GNSS
                return 'N'; // not valid
            case 1: //GNSS fix
                return 'A'; // autonomous
            case 3: //Precise GNSS
                return 'P'; // Precise
            case 2: //DGNSS fix
                return 'D'; // differential
            case 4: //RTK Fixed Integer
                return 'R'; // RTK Integer mode
            case 5: //RTK float
                return 'F'; // RTK Float mode
            case 6: //Estimated (DR) mode
                return 'E'; // Estimated
            case 7: //Manual Input
                return 'M'; // Manual input mode
            case 8: //Simulate mode
                return 'S'; // Simulator
            default:
                return 'N';
        }
    }
    @Override
    public void commit(String reason)
    {
        try
        {
            NMEAPGN pgn = NMEAPGN.getForPgn(PGN.pgn(canId));
            switch (pgn)
            {
                case GNSS_POSITION_DATA:
                    rmc2.writeTo(channel);
                    break;
                case POSITION_RAPID_UPDATE:
                    rmc.writeTo(channel);
                    break;
                case WATER_DEPTH:
                    if (Float.isFinite(depthOfWaterRelativeToTransducer))
                    {
                        dpt.writeTo(channel);
                    }
                    break;
                case VESSEL_HEADING:
                    hdt.writeTo(channel);
                    hdg.writeTo(channel);
                    break;
                case ENVIRONMENTAL_PARAMETERS:
                    mtw.writeTo(channel);
                    environmental.writeTo(channel);
                    break;
                case WIND_DATA:
                    mwv.writeTo(channel);
                    break;
                case SPEED_WATER_REFERENCED:
                    vhw.writeTo(channel);
                    break;
                case ATTITUDE:
                    attitude.writeTo(channel);
                    break;
                case BATTERY_STATUS:
                    if (Float.isFinite(voltage) || Float.isFinite(current) || Float.isFinite(temperature))
                    {
                        battery.writeTo(channel);
                    }
                    break;
                case DC_DETAILED_STATUS:
                    if (stateOfCharge != 255 || stateOfHealth != 255)
                    {
                        dcDetailedStatus.writeTo(channel);
                    }
                    break;
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    private @Property void setPositionDate(int days)
    {
        positionClock.setDays(days);
    }
    private @Property void setPositionTime(long micros)
    {
        positionClock.setMicros(micros);
    }
    
}
