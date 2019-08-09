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
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Formatter;
import java.util.Locale;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.code.Property;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AISTargetDynamic extends AnnotatedPropertyStore
{
    @Property private MessageTypes messageType;
    @Property private Instant instant;
    @Property private double latitude;
    @Property private double longitude;
    @Property private float course;
    @Property private float speed;
    @Property private int heading;
    @Property private float rateOfTurn;
    @Property private NavigationStatus navigationStatus;
    @Property private ManeuverIndicator maneuver;
    @Property private char channel;
    @Property private int altitude;
    @Property private boolean band;
    @Property private boolean msg22;
    @Property private boolean assignedMode;
    @Property private boolean raim;
    @Property private int radioStatus;
    @Property private EPFDFixTypes epfd;
    @Property private int etaMonth;
    @Property private int etaDay;
    @Property private int etaHour;
    @Property private int etaMinute;
    @Property private String destination;
    
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
                        instant,
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
            case StaticAndVoyageRelatedData:    // 5
                pw.format("Msg%d %s \"%s\" %d %d %d %d %s\r\n",
                        messageType.ordinal(),
                        instant,
                        destination,
                        etaMonth,
                        etaDay,
                        etaHour,
                        etaMinute,
                        epfd
                        );
                break;
            case StandardSARAircraftPositionReport:
                if (!logExists)
                {
                    pw.format("#Msg  Timestamp Location Course Speed Altitude Channel Assigned\r\n");
                }
                pw.format("Msg%d %s %s %.1f %.1f %d %c %b\r\n",
                        messageType.ordinal(),
                        instant,
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
                        instant,
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
        return "AISTargetDynamic{" + "messageType=" + messageType + ", instant=" + instant + '}';
    }

    public AISTargetDynamic setInstant(Instant instant)
    {
        this.instant = instant;
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

    public AISTargetDynamic setDestination(String destination)
    {
        this.destination = destination;
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

    public AISTargetDynamic setEpfdFixTypes(EPFDFixTypes epfdFixTypes)
    {
        this.epfd = epfdFixTypes;
        return this;
    }

    public AISTargetDynamic setEtaMonth(int etaMonth)
    {
        this.etaMonth = etaMonth;
        return this;
    }

    public AISTargetDynamic setEtaDay(int etaDay)
    {
        this.etaDay = etaDay;
        return this;
    }

    public AISTargetDynamic setEtaHour(int etaHour)
    {
        this.etaHour = etaHour;
        return this;
    }

    public AISTargetDynamic setEtaMinute(int etaMinute)
    {
        this.etaMinute = etaMinute;
        return this;
    }

    public MessageTypes getMessageType()
    {
        return messageType;
    }

    public Instant getInstant()
    {
        return instant;
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

    public String getDestination()
    {
        return destination;
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

    public EPFDFixTypes getEpfdFixTypes()
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
    
}
