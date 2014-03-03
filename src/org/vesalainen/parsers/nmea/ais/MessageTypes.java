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

/**
 *
 * @author Timo Vesalainen
 */
public enum MessageTypes
{
    ZeroNotUsed("not used"),
    /**
     * Position Report Class A
     */
    PositionReportClassA("Position Report Class A"),
    /**
     * Position Report Class A (Assigned schedule)
     */
    PositionReportClassAAssignedSchedule("Position Report Class A (Assigned schedule)"),
    /**
     * Position Report Class A (Response to interrogation)
     */
    PositionReportClassAResponseToInterrogation("Position Report Class A (Response to interrogation)"),
    /**
     * Base Station Report
     */
    BaseStationReport("Base Station Report"),
    /**
     * Static and Voyage Related Data
     */
    StaticAndVoyageRelatedData("Static and Voyage Related Data"),
    /**
     * Binary Addressed Message
     */
    BinaryAddressedMessage("Binary Addressed Message"),
    /**
     * Binary Acknowledge
     */
    BinaryAcknowledge("Binary Acknowledge"),
    /**
     * Binary Broadcast Message
     */
    BinaryBroadcastMessage("Binary Broadcast Message"),
    /**
     * Standard SAR Aircraft Position Report
     */
    StandardSARAircraftPositionReport("Standard SAR Aircraft Position Report"),
    /**
     * UTC and Date Inquiry
     */
    UTCAndDateInquiry("UTC and Date Inquiry"),
    /**
     * UTC and Date Response
     */
    UTCAndDateResponse("UTC and Date Response"),
    /**
     * Addressed Safety Related Message
     */
    AddressedSafetyRelatedMessage("Addressed Safety Related Message"),
    /**
     * Safety Related Acknowledgement
     */
    SafetyRelatedAcknowledgement("Safety Related Acknowledgement"),
    /**
     * Safety Related Broadcast Message
     */
    SafetyRelatedBroadcastMessage("Safety Related Broadcast Message"),
    /**
     * Interrogation
     */
    Interrogation("Interrogation"),
    /**
     * Assignment Mode Command
     */
    AssignmentModeCommand("Assignment Mode Command"),
    /**
     * DGNSS Binary Broadcast Message
     */
    DGNSSBinaryBroadcastMessage("DGNSS Binary Broadcast Message"),
    /**
     * Standard Class B CS Position Report
     */
    StandardClassBCSPositionReport("Standard Class B CS Position Report"),
    /**
     * Extended Class B Equipment Position Report
     */
    ExtendedClassBEquipmentPositionReport("Extended Class B Equipment Position Report"),
    /**
     * Data Link Management
     */
    DataLinkManagement("Data Link Management"),
    /**
     * Aid-to-Navigation Report
     */
    AidToNavigationReport("Aid-to-Navigation Report"),
    /**
     * Channel Management
     */
    ChannelManagement("Channel Management"),
    /**
     * Group Assignment Command
     */
    GroupAssignmentCommand("Group Assignment Command"),
    /**
     * Static Data Report
     */
    StaticDataReport("Static Data Report"),
    /**
     * Single Slot Binary Message,
     */
    SingleSlotBinaryMessage("Single Slot Binary Message,"),
    /**
     * Multiple Slot Binary Message With Communications State
     */
    MultipleSlotBinaryMessageWithCommunicationsState("Multiple Slot Binary Message With Communications State"),
    /**
     * Position Report For Long-Range Applications
     */
    PositionReportForLongRangeApplications("Position Report For Long-Range Applications");
    private String description;

    MessageTypes(String description)
    {
        this.description = description;
    }

    public String toString()
    {
        return description;
    }
}
