/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collection;
import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.function.DoubleSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.code.Property;
import org.vesalainen.modbus.ModbusTcp;
import org.vesalainen.navi.SolarPosition;
import org.vesalainen.nmea.jaxb.router.SunsetManagerType;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SunsetManager extends AbstractProcessorTask
{
    private final CachedScheduledThreadPool executor;
    private final int batteryNumber;
    private final String modbusHost;
    private final int unitId;
    private final int relayAddress;

    private @Property Clock clock;
    private @Property double latitude;
    private @Property double longitude;
    private @Property float batteryVoltage0;
    private @Property float batteryVoltage1;
    private @Property float batteryVoltage2;
    private @Property float batteryVoltage3;
    private @Property float batteryCurrent0;
    private @Property float batteryCurrent1;
    private @Property float batteryCurrent2;
    private @Property float batteryCurrent3;
    
    private SolarPosition solarPosition;
    private short relay;
    private DoubleSupplier power;
    private float switchOffPower = Float.MAX_VALUE;
    private float switchOnPower;
    
    public SunsetManager(SunsetManagerType type, CachedScheduledThreadPool executor)
    {
        super(MethodHandles.lookup(), 20, MINUTES, props(type.getBatteryNumber()));
        this.executor = executor;
        this.batteryNumber = type.getBatteryNumber();
        this.modbusHost = type.getModbusHost();
        this.unitId = type.getUnitId();
        this.relayAddress = type.getRelayAddress();
        switch (batteryNumber)
        {
            case 0:
                power = ()->batteryVoltage0*batteryCurrent0;
                break;
            case 1:
                power = ()->batteryVoltage1*batteryCurrent1;
                break;
            case 2:
                power = ()->batteryVoltage2*batteryCurrent2;
                break;
            case 3:
                power = ()->batteryVoltage3*batteryCurrent3;
                break;
            default:
                throw new UnsupportedOperationException("battery "+batteryNumber+" not supported");
        }
        try (ModbusTcp modbus = ModbusTcp.open(modbusHost))
        {
            relay = modbus.getShort(unitId, relayAddress);
            info("modbus ok");
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    private static String[] props(int batteryNumber)
    {
        return new String[]{
            "longitude",
            "latitude",
            "batteryVoltage"+batteryNumber,
            "batteryCurrent"+batteryNumber
        };
    }
    @Override
    protected void commitTask(String reason, Collection<String> updatedProperties)
    {
        if (solarPosition == null)
        {
            initSunset();
        }
        float pow = (float) power.getAsDouble();
        if (pow < switchOnPower)
        {
            info("power %f < %f switching on", pow, switchOnPower);
            switchRelay((short)1);
        }
        else
        {
            if (pow > switchOffPower)
            {
                info("power %f > %f switching off", pow, switchOnPower);
                switchRelay((short)1);
            }
        }
    }

    @Override
    public void stop()
    {
    }

    private void initSunset()
    {
        if (solarPosition == null)
        {
            solarPosition = new SolarPosition(ZonedDateTime.now(clock), longitude, latitude);
        }
        switch (solarPosition.getDayPhase())
        {
            case DAY:
                scheduleSunset();
                break;
            case NIGHT:
            case TWILIGHT:
                scheduleSunrise();
                break;
        }
    }
    private void scheduleSunrise()
    {
        ZonedDateTime sunrise = solarPosition.nextSunset();
        executor.schedule(this::sunrise, sunrise);
        info("scheduled sunrise at %s", sunrise);
    }
    private void scheduleSunset()
    {
        ZonedDateTime sunset = solarPosition.nextSunset();
        executor.schedule(this::sunset, sunset);
        info("scheduled sunset at %s", sunset);
    }
    private void sunrise()
    {
        float pow = (float) power.getAsDouble();
        switchOffPower = max(switchOffPower, pow);
        info("sunrise %fW new off=%fW", pow, switchOffPower);
        switchRelay((short)0);
        solarPosition.set(ZonedDateTime.now(clock), longitude, latitude);
        scheduleSunset();
    }    
    private void sunset()
    {
        float pow = (float) power.getAsDouble();
        switchOnPower = min(switchOnPower, pow);
        info("sunset %fW new on=%fW", pow, switchOnPower);
        switchRelay((short)1);
        solarPosition.set(ZonedDateTime.now(clock), longitude, latitude);
        scheduleSunrise();
    }    

    private void switchRelay(short on)
    {
        if (relay != on)
        {
            try (ModbusTcp modbus = ModbusTcp.open(modbusHost))
            {
                modbus.setShort(unitId, relayAddress, on);
                relay = modbus.getShort(unitId, relayAddress);
                info("set relay(%d) = %d", on, relay);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
}
