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
import java.time.Instant;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.parsers.nmea.NMEASentence;
import static org.vesalainen.parsers.nmea.ais.ManeuverIndicator.*;
import static org.vesalainen.parsers.nmea.ais.MessageTypes.*;
import static org.vesalainen.parsers.nmea.ais.NavigationStatus.*;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISTargetDynamic extends AnnotatedPropertyStore
{
    @Property private MessageTypes messageType;
    @Property private Instant timestamp;
    @Property private int mmsi;
    @Property private double latitude = Double.NaN;
    @Property private double longitude = Double.NaN;
    @Property private float course = 360;
    @Property private float speed = 102.3F;
    @Property private int heading = 511;
    @Property private float rateOfTurn;
    @Property private NavigationStatus navigationStatus = NotDefinedDefault; 
    @Property private ManeuverIndicator maneuver = NotAvailableDefault;
    @Property private char channel;
    @Property private int altitude;
    @Property private boolean band;
    @Property private boolean msg22;
    @Property private boolean assignedMode;
    @Property private boolean raim;
    @Property private int radioStatus;
    @Property private boolean positionAccuracy;
    @Property private int second = 60;
    @Property private boolean csUnit;
    @Property private boolean display;
    @Property private boolean dsc;
    
    public AISTargetDynamic()
    {
        super(MethodHandles.lookup());
    }

    public AISTargetDynamic(AISTargetDynamic aps)
    {
        super(aps);
    }

    public AISTargetDynamic(Path path) throws IOException
    {
        super(MethodHandles.lookup(), path);
    }
    
    protected NMEASentence[] getMsg1()
    {
        return AISSentence.getMsg1(mmsi, navigationStatus, longitude, latitude, rateOfTurn, speed, positionAccuracy, course, heading, second, maneuver, raim, radioStatus);
    }
    protected NMEASentence[] getMsg18()
    {
        return AISSentence.getMsg18(mmsi, navigationStatus, longitude, latitude, speed, positionAccuracy, course, heading, second, csUnit, display, dsc, band, msg22, assignedMode, raim, radioStatus);
    }
    public void print(AISLogFile pw, boolean logExists) throws IOException
    {
        switch (messageType)
        {
            case PositionReportClassA:  // 1
            case PositionReportClassAAssignedSchedule:  // 2
            case PositionReportClassAResponseToInterrogation:   // 3
                if (!logExists)
                {
                    pw.format("#Msg  Timestamp Location Course Speed Heading ROT Status Maneuver Channel\r\n");
                }
                pw.format("Msg%d %s %s %.1f %.1f %d %.1f %s %s %c\r\n",
                        messageType.ordinal(),
                        timestamp,
                        new Location(latitude, longitude),
                        course,
                        speed,
                        heading,
                        rateOfTurn,
                        navigationStatus,
                        maneuver,
                        channel
                        );
                break;
            case StandardSARAircraftPositionReport:
                if (!logExists)
                {
                    pw.format("#Msg  Timestamp Location Course Speed Altitude Channel Assigned\r\n");
                }
                pw.format("Msg%d %s %s %.1f %.1f %d %c %b\r\n",
                        messageType.ordinal(),
                        timestamp,
                        new Location(latitude, longitude),
                        course,
                        speed,
                        altitude,
                        channel,
                        assignedMode
                        );
                break;
            case StandardClassBCSPositionReport:
            case ExtendedClassBEquipmentPositionReport:
                if (!logExists)
                {
                    pw.format("#Msg  Timestamp Location Course Speed Heading Channel Assigned\r\n");
                }
                pw.format("Msg%d %s %s %.1f %.1f %d %c %b\r\n",
                        messageType.ordinal(),
                        timestamp,
                        new Location(latitude, longitude),
                        course,
                        speed,
                        heading,
                        channel,
                        assignedMode
                        );
                break;
        }
    }

    @Override
    public String toString()
    {
        return "AISTargetDynamic{" + "messageType=" + messageType + ", instant=" + timestamp + '}';
    }

    public AISTargetDynamic setTimestamp(Instant timestamp)
    {
        this.timestamp = timestamp;
        return this;
    }

    public AISTargetDynamic setMessageType(MessageTypes messageType)
    {
        this.messageType = messageType;
        return this;
    }

    public AISTargetDynamic setLatitude(double latitude)
    {
        this.latitude = latitude;
        return this;
    }

    public AISTargetDynamic setLongitude(double longitude)
    {
        this.longitude = longitude;
        return this;
    }

    public AISTargetDynamic setCourse(float course)
    {
        this.course = course;
        return this;
    }

    public AISTargetDynamic setSpeed(float speed)
    {
        this.speed = speed;
        return this;
    }

    public AISTargetDynamic setHeading(int heading)
    {
        this.heading = heading;
        return this;
    }

    public AISTargetDynamic setRateOfTurn(float rateOfTurn)
    {
        this.rateOfTurn = rateOfTurn;
        return this;
    }

    public AISTargetDynamic setNavigationStatus(NavigationStatus navigationStatus)
    {
        this.navigationStatus = navigationStatus;
        return this;
    }

    public AISTargetDynamic setManeuver(ManeuverIndicator maneuver)
    {
        this.maneuver = maneuver;
        return this;
    }

    public AISTargetDynamic setChannel(char channel)
    {
        this.channel = channel;
        return this;
    }

    public AISTargetDynamic setAltitude(int altitude)
    {
        this.altitude = altitude;
        return this;
    }

    public AISTargetDynamic setBand(boolean band)
    {
        this.band = band;
        return this;
    }

    public AISTargetDynamic setMsg22(boolean msg22)
    {
        this.msg22 = msg22;
        return this;
    }

    public AISTargetDynamic setAssignedMode(boolean assignedMode)
    {
        this.assignedMode = assignedMode;
        return this;
    }

    public AISTargetDynamic setRaim(boolean raim)
    {
        this.raim = raim;
        return this;
    }

    public AISTargetDynamic setRadioStatus(int radioStatus)
    {
        this.radioStatus = radioStatus;
        return this;
    }

    public MessageTypes getMessageType()
    {
        return messageType;
    }

    public Instant getTimestamp()
    {
        return timestamp;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public float getCourse()
    {
        return course;
    }

    public float getSpeed()
    {
        return speed;
    }

    public int getHeading()
    {
        return heading;
    }

    public float getRateOfTurn()
    {
        return rateOfTurn;
    }

    public NavigationStatus getNavigationStatus()
    {
        return navigationStatus;
    }

    public ManeuverIndicator getManeuver()
    {
        return maneuver;
    }

    public char getChannel()
    {
        return channel;
    }

    public int getAltitude()
    {
        return altitude;
    }

    public boolean isBand()
    {
        return band;
    }

    public boolean isMsg22()
    {
        return msg22;
    }

    public boolean isAssignedMode()
    {
        return assignedMode;
    }

    public boolean isRaim()
    {
        return raim;
    }

    public int getRadioStatus()
    {
        return radioStatus;
    }

    public int getMmsi()
    {
        return mmsi;
    }

    public boolean isPositionAccuracy()
    {
        return positionAccuracy;
    }

    public int getSecond()
    {
        return second;
    }

    public boolean isCsUnit()
    {
        return csUnit;
    }

    public boolean isDisplay()
    {
        return display;
    }

    public boolean isDsc()
    {
        return dsc;
    }

}
