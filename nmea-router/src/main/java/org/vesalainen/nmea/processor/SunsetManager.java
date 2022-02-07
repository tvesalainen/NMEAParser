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
import org.vesalainen.code.Property;
import org.vesalainen.modbus.ModbusTcpClient;
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
    private final String modbusHost;
    private final int unitId;
    private final int relayAddress;

    private @Property Clock clock;
    private @Property double latitude;
    private @Property double longitude;
    
    private SolarPosition solarPosition;
    private short relay;
    private DoubleSupplier voltage;
    private float switchOffVoltage = Float.MAX_VALUE;
    private float switchOnVoltage;
    
    public SunsetManager(SunsetManagerType type, CachedScheduledThreadPool executor)
    {
        super(MethodHandles.lookup(), 20, MINUTES);
        this.executor = executor;
        this.modbusHost = type.getModbusHost();
        this.unitId = type.getUnitId();
        this.relayAddress = type.getRelayAddress();
        try (ModbusTcpClient modbus = ModbusTcpClient.open(modbusHost))
        {
            relay = modbus.getShort(unitId, relayAddress);
            info("modbus ok relay=%d", relay);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    @Override
    protected void commitTask(String reason, Collection<String> updatedProperties)
    {
        if (solarPosition == null)
        {
            initSunset();
        }
        /*
        float volt = (float) voltage.getAsDouble();
        if (volt < switchOnPower)
        {
            info("voltage %f < %f switching on", volt, switchOnPower);
            switchRelay((short)1);
        }
        else
        {
            if (volt > switchOffPower)
            {
                info("voltage %f > %f switching off", volt, switchOnPower);
                switchRelay((short)1);
            }
        }
*/
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
                switchRelay((short)0);
                scheduleSunset();
                break;
            case NIGHT:
            case TWILIGHT:
                switchRelay((short)1);
                scheduleSunrise();
                break;
        }
    }
    private void scheduleSunrise()
    {
        ZonedDateTime sunrise = solarPosition.nextSunrise();
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
        float pow = (float) voltage.getAsDouble();
        switchOffVoltage = max(switchOffVoltage, pow);
        info("sunrise %fV new off=%fV", pow, switchOffVoltage);
        switchRelay((short)0);
        solarPosition.set(ZonedDateTime.now(clock), longitude, latitude);
        scheduleSunset();
    }    
    private void sunset()
    {
        float pow = (float) voltage.getAsDouble();
        switchOnVoltage = min(switchOnVoltage, pow);
        info("sunset %fV new on=%fV", pow, switchOnVoltage);
        switchRelay((short)1);
        solarPosition.set(ZonedDateTime.now(clock), longitude, latitude);
        scheduleSunrise();
    }    

    private void switchRelay(short on)
    {
        if (relay != on)
        {
            try (ModbusTcpClient modbus = ModbusTcpClient.open(modbusHost))
            {
                modbus.setShort(unitId, relayAddress, on);
                relay = on;
                info("set relay(%d) = %d", on, relay);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        else
        {
            info("not setting relay = %d", on, relay);
        }
    }
}
