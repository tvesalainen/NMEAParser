/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.Objects;
import java.util.Properties;
import org.vesalainen.code.AbstractPropertySetter;
import org.vesalainen.nmea.jaxb.router.AisLogType;
import org.vesalainen.nmea.util.Stoppable;
import org.vesalainen.parsers.nmea.ais.AISProperties;
import org.vesalainen.parsers.nmea.ais.ManeuverIndicator;
import org.vesalainen.parsers.nmea.ais.MessageTypes;
import org.vesalainen.parsers.nmea.ais.NavigationStatus;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.logging.AttachedLogger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISLog extends AbstractPropertySetter implements AttachedLogger, Stoppable
{
    private Path dir;
    private Properties props = new Properties();
    private String[] prefixes;
    private Clock clock;
    private NavigationStatus status;
    private ManeuverIndicator maneuver;
    private MessageTypes type;
    private int repeat;
    private int second;
    private int radio;
    private float lat;
    private float lon;
    private float turn;
    private float speed;
    private float course;
    private float heading;
    private boolean accuracy;
    private boolean raim;
    private boolean msg22;
    private boolean dsc;
    private boolean csUnit;
    private boolean display;
    private boolean assignedMode;
    private boolean band;
    
    AISLog(AisLogType type)
    {
        String dirName = type.getDirectory();
        Objects.requireNonNull(dirName, "ais-log directory");
        dir = Paths.get(dirName);
        prefixes = CollectionHelp.toArray(AISProperties.getInstance().getAllProperties(), String.class);
    }

    @Override
    public void set(String property, Object arg)
    {
        switch (property)
        {
            case "clock":
                clock = (Clock) arg;
                break;
            case "messageType":
                type = (MessageTypes) arg;
                break;
            case "status":
                status = (NavigationStatus) arg;
                break;
            case "maneuver":
                maneuver = (ManeuverIndicator) arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
            case "latitude":
                lat = arg;
                break;
            case "longitude":
                lon = arg;
                break;
            case "turn":
                turn = arg;
                break;
            case "speed":
                speed = arg;
                break;
            case "course":
                course = arg;
                break;
            case "heading":
                heading = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, int arg)
    {
        switch (property)
        {
            case "repeatIndicator":
                repeat = arg;
                break;
            case "second":
                second = arg;
                break;
            case "radioStatus":
                radio = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void set(String property, boolean arg)
    {
        switch (property)
        {
            case "positionAccuracy":
                accuracy = arg;
                break;
            case "raim":
                raim = arg;
                break;
            case "msg22":
                msg22 = arg;
                break;
            case "dsc":
                dsc = arg;
                break;
            case "csUnit":
                csUnit = arg;
                break;
            case "display":
                display = arg;
                break;
            case "assignedMode":
                assignedMode = arg;
                break;
            case "band":
                band = arg;
                break;
            default:
                super.set(property, arg);
                break;
        }
    }

    @Override
    public void commit(String reason)
    {
        props.clear();
    }

    @Override
    public void rollback(String reason)
    {
        props.clear();
    }

    @Override
    protected void setProperty(String property, Object arg)
    {
        props.setProperty(property, arg.toString());
    }

    @Override
    public String[] getPrefixes()
    {
        return prefixes;
    }

    @Override
    public void stop()
    {
    }

    
}
