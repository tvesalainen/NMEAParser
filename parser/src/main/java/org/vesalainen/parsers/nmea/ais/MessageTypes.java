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
     * 1. Position Report Class A
     */
    PositionReportClassA("Position Report Class A"),
    /**
     * 2. Position Report Class A (Assigned schedule)
     */
    PositionReportClassAAssignedSchedule("Position Report Class A (Assigned schedule)"),
    /**
     * 3. Position Report Class A (Response to interrogation)
     */
    PositionReportClassAResponseToInterrogation("Position Report Class A (Response to interrogation)"),
    /**
     * 4. Base Station Report
     */
    BaseStationReport("Base Station Report"),
    /**
     * 5. Static and Voyage Related Data
     */
    StaticAndVoyageRelatedData("Static and Voyage Related Data"),
    /**
     * 6. Binary Addressed Message
     */
    BinaryAddressedMessage("Binary Addressed Message"),
    /**
     * 7. Binary Acknowledge
     */
    BinaryAcknowledge("Binary Acknowledge"),
    /**
     * 8. Binary Broadcast Message
     */
    BinaryBroadcastMessage("Binary Broadcast Message"),
    /**
     * 9. Standard SAR Aircraft Position Report
     */
    StandardSARAircraftPositionReport("Standard SAR Aircraft Position Report"),
    /**
     * 10. UTC and Date Inquiry
     */
    UTCAndDateInquiry("UTC and Date Inquiry"),
    /**
     * 11. UTC and Date Response
     */
    UTCAndDateResponse("UTC and Date Response"),
    /**
     * 12. Addressed Safety Related Message
     */
    AddressedSafetyRelatedMessage("Addressed Safety Related Message"),
    /**
     * 13. Safety Related Acknowledgement
     */
    SafetyRelatedAcknowledgement("Safety Related Acknowledgement"),
    /**
     * 14. Safety Related Broadcast Message
     */
    SafetyRelatedBroadcastMessage("Safety Related Broadcast Message"),
    /**
     * 15. Interrogation
     */
    Interrogation("Interrogation"),
    /**
     * 16. Assignment Mode Command
     */
    AssignmentModeCommand("Assignment Mode Command"),
    /**
     * 17. DGNSS Binary Broadcast Message
     */
    DGNSSBinaryBroadcastMessage("DGNSS Binary Broadcast Message"),
    /**
     * 18. Standard Class B CS Position Report
     */
    StandardClassBCSPositionReport("Standard Class B CS Position Report"),
    /**
     * 19. Extended Class B Equipment Position Report
     */
    ExtendedClassBEquipmentPositionReport("Extended Class B Equipment Position Report"),
    /**
     * 20. Data Link Management
     */
    DataLinkManagement("Data Link Management"),
    /**
     * 21. Aid-to-Navigation Report
     */
    AidToNavigationReport("Aid-to-Navigation Report"),
    /**
     * 22. Channel Management
     */
    ChannelManagement("Channel Management"),
    /**
     * 23. Group Assignment Command
     */
    GroupAssignmentCommand("Group Assignment Command"),
    /**
     * 24. Static Data Report
     */
    StaticDataReport("Static Data Report"),
    /**
     * 25. Single Slot Binary Message,
     */
    SingleSlotBinaryMessage("Single Slot Binary Message,"),
    /**
     * 26. Multiple Slot Binary Message With Communications State
     */
    MultipleSlotBinaryMessageWithCommunicationsState("Multiple Slot Binary Message With Communications State"),
    /**
     * 27. Position Report For Long-Range Applications
     */
    PositionReportForLongRangeApplications("Position Report For Long-Range Applications");
    private String description;

    MessageTypes(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
    
    public boolean isPositionReport()
    {
        switch (this)
        {
            case PositionReportClassA:
            case PositionReportClassAAssignedSchedule:
            case PositionReportClassAResponseToInterrogation:
            case StandardSARAircraftPositionReport:
            case StandardClassBCSPositionReport:
            case ExtendedClassBEquipmentPositionReport:
                return true;
            default:
                return false;
        }
    }
}
