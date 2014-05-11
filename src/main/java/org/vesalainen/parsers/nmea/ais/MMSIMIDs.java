/*
 * Copyright (C) 2014 tkv
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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tkv
 */
public class MMSIMIDs
{

    private static final Map<Integer, String> map = new HashMap<>();

    static
    {
        map.put(201, "Albania (Republic of)");
        map.put(202, "Andorra (Principality of)");
        map.put(203, "Austria");
        map.put(204, "Azores");
        map.put(205, "Belgium");
        map.put(206, "Belarus (Republic of)");
        map.put(207, "Bulgaria (Republic of)");
        map.put(208, "Vatican City State");
        map.put(209, "Cyprus (Republic of)");
        map.put(210, "Cyprus (Republic of)");
        map.put(211, "Germany (Federal Republic of)");
        map.put(212, "Cyprus (Republic of)");
        map.put(213, "Georgia");
        map.put(214, "Moldova (Republic of)");
        map.put(215, "Malta");
        map.put(216, "Armenia (Republic of)");
        map.put(218, "Germany (Federal Republic of)");
        map.put(219, "Denmark");
        map.put(220, "Denmark");
        map.put(224, "Spain");
        map.put(225, "Spain");
        map.put(226, "France");
        map.put(227, "France");
        map.put(228, "France");
        map.put(230, "Finland");
        map.put(231, "Faroe Islands");
        map.put(232, "United Kingdom of Great Britain and Northern Ireland");
        map.put(233, "United Kingdom of Great Britain and Northern Ireland");
        map.put(234, "United Kingdom of Great Britain and Northern Ireland");
        map.put(235, "United Kingdom of Great Britain and Northern Ireland");
        map.put(236, "Gibraltar");
        map.put(237, "Greece");
        map.put(238, "Croatia (Republic of)");
        map.put(239, "Greece");
        map.put(240, "Greece");
        map.put(241, "Greece");
        map.put(242, "Morocco (Kingdom of)");
        map.put(243, "Hungary (Republic of)");
        map.put(244, "Netherlands (Kingdom of the)");
        map.put(245, "Netherlands (Kingdom of the)");
        map.put(246, "Netherlands (Kingdom of the)");
        map.put(247, "Italy");
        map.put(248, "Malta");
        map.put(249, "Malta");
        map.put(250, "Ireland");
        map.put(251, "Iceland");
        map.put(252, "Liechtenstein (Principality of)");
        map.put(253, "Luxembourg");
        map.put(254, "Monaco (Principality of)");
        map.put(255, "Madeira");
        map.put(256, "Malta");
        map.put(257, "Norway");
        map.put(258, "Norway");
        map.put(259, "Norway");
        map.put(261, "Poland (Republic of)");
        map.put(262, "Montenegro");
        map.put(263, "Portugal");
        map.put(264, "Romania");
        map.put(265, "Sweden");
        map.put(266, "Sweden");
        map.put(267, "Slovak Republic");
        map.put(268, "San Marino (Republic of)");
        map.put(269, "Switzerland (Confederation of)");
        map.put(270, "Czech Republic");
        map.put(271, "Turkey");
        map.put(272, "Ukraine");
        map.put(273, "Russian Federation");
        map.put(274, "The Former Yugoslav Republic of Macedonia");
        map.put(275, "Latvia (Republic of)");
        map.put(276, "Estonia (Republic of)");
        map.put(277, "Lithuania (Republic of)");
        map.put(278, "Slovenia (Republic of)");
        map.put(279, "Serbia (Republic of)");
        map.put(301, "Anguilla");
        map.put(303, "Alaska (State of)");
        map.put(304, "Antigua and Barbuda");
        map.put(305, "Antigua and Barbuda");
        map.put(306, "Netherlands Antilles");
        map.put(307, "Aruba");
        map.put(308, "Bahamas (Commonwealth of the)");
        map.put(309, "Bahamas (Commonwealth of the)");
        map.put(310, "Bermuda");
        map.put(311, "Bahamas (Commonwealth of the)");
        map.put(312, "Belize");
        map.put(314, "Barbados");
        map.put(316, "Canada");
        map.put(319, "Cayman Islands");
        map.put(321, "Costa Rica");
        map.put(323, "Cuba");
        map.put(325, "Dominica (Commonwealth of)");
        map.put(327, "Dominican Republic");
        map.put(329, "Guadeloupe (French Department of)");
        map.put(330, "Grenada");
        map.put(331, "Greenland");
        map.put(332, "Guatemala (Republic of)");
        map.put(334, "Honduras (Republic of)");
        map.put(336, "Haiti (Republic of)");
        map.put(338, "United States of America");
        map.put(339, "Jamaica");
        map.put(341, "Saint Kitts and Nevis (Federation of)");
        map.put(343, "Saint Lucia");
        map.put(345, "Mexico");
        map.put(347, "Martinique (French Department of)");
        map.put(348, "Montserrat");
        map.put(350, "Nicaragua");
        map.put(351, "Panama (Republic of)");
        map.put(352, "Panama (Republic of)");
        map.put(353, "Panama (Republic of)");
        map.put(354, "Panama (Republic of)");
        map.put(355, "-");
        map.put(356, "-");
        map.put(357, "-");
        map.put(358, "Puerto Rico");
        map.put(359, "El Salvador (Republic of)");
        map.put(361, "Saint Pierre and Miquelon (Territorial Collectivity of)");
        map.put(362, "Trinidad and Tobago");
        map.put(364, "Turks and Caicos Islands");
        map.put(366, "United States of America");
        map.put(367, "United States of America");
        map.put(368, "United States of America");
        map.put(369, "United States of America");
        map.put(370, "Panama (Republic of)");
        map.put(371, "Panama (Republic of)");
        map.put(372, "Panama (Republic of)");
        map.put(375, "Saint Vincent and the Grenadines");
        map.put(376, "Saint Vincent and the Grenadines");
        map.put(377, "Saint Vincent and the Grenadines");
        map.put(378, "British Virgin Islands");
        map.put(379, "United States Virgin Islands");
        map.put(401, "Afghanistan");
        map.put(403, "Saudi Arabia (Kingdom of)");
        map.put(405, "Bangladesh (People's Republic of)");
        map.put(408, "Bahrain (Kingdom of)");
        map.put(410, "Bhutan (Kingdom of)");
        map.put(412, "China (People's Republic of)");
        map.put(413, "China (People's Republic of)");
        map.put(416, "Taiwan (Province of China)");
        map.put(417, "Sri Lanka (Democratic Socialist Republic of)");
        map.put(419, "India (Republic of)");
        map.put(422, "Iran (Islamic Republic of)");
        map.put(423, "Azerbaijani Republic");
        map.put(425, "Iraq (Republic of)");
        map.put(428, "Israel (State of)");
        map.put(431, "Japan");
        map.put(432, "Japan");
        map.put(434, "Turkmenistan");
        map.put(436, "Kazakhstan (Republic of)");
        map.put(437, "Uzbekistan (Republic of)");
        map.put(438, "Jordan (Hashemite Kingdom of)");
        map.put(440, "Korea (Republic of)");
        map.put(441, "Korea (Republic of)");
        map.put(443, "Palestine (In accordance with Resolution 99 Rev. Antalya, 2006)");
        map.put(445, "Democratic People's Republic of Korea");
        map.put(447, "Kuwait (State of)");
        map.put(450, "Lebanon");
        map.put(451, "Kyrgyz Republic");
        map.put(453, "Macao (Special Administrative Region of China)");
        map.put(455, "Maldives (Republic of)");
        map.put(457, "Mongolia");
        map.put(459, "Nepal (Federal Democratic Republic of)");
        map.put(461, "Oman (Sultanate of)");
        map.put(463, "Pakistan (Islamic Republic of)");
        map.put(466, "Qatar (State of)");
        map.put(468, "Syrian Arab Republic");
        map.put(470, "United Arab Emirates");
        map.put(473, "Yemen (Republic of)");
        map.put(475, "Yemen (Republic of)");
        map.put(477, "Hong Kong (Special Administrative Region of China)");
        map.put(478, "Bosnia and Herzegovina");
        map.put(501, "Adelie Land");
        map.put(503, "Australia");
        map.put(506, "Myanmar (Union of)");
        map.put(508, "Brunei Darussalam");
        map.put(510, "Micronesia (Federated States of)");
        map.put(511, "Palau (Republic of)");
        map.put(512, "New Zealand");
        map.put(514, "Cambodia (Kingdom of)");
        map.put(515, "Cambodia (Kingdom of)");
        map.put(516, "Christmas Island (Indian Ocean)");
        map.put(518, "Cook Islands");
        map.put(520, "Fiji (Republic of)");
        map.put(523, "Cocos (Keeling) Islands");
        map.put(525, "Indonesia (Republic of)");
        map.put(529, "Kiribati (Republic of)");
        map.put(531, "Lao People's Democratic Republic");
        map.put(533, "Malaysia");
        map.put(536, "Northern Mariana Islands (Commonwealth of the)");
        map.put(538, "Marshall Islands (Republic of the)");
        map.put(540, "New Caledonia");
        map.put(542, "Niue");
        map.put(544, "Nauru (Republic of)");
        map.put(546, "French Polynesia");
        map.put(548, "Philippines (Republic of the)");
        map.put(553, "Papua New Guinea");
        map.put(555, "Pitcairn Island");
        map.put(557, "Solomon Islands");
        map.put(559, "American Samoa");
        map.put(561, "Samoa (Independent State of)");
        map.put(563, "Singapore (Republic of)");
        map.put(564, "Singapore (Republic of)");
        map.put(565, "Singapore (Republic of)");
        map.put(567, "Thailand");
        map.put(570, "Tonga (Kingdom of)");
        map.put(572, "Tuvalu");
        map.put(574, "Viet Nam (Socialist Republic of)");
        map.put(576, "Vanuatu (Republic of)");
        map.put(578, "Wallis and Futuna Islands");
        map.put(601, "South Africa (Republic of)");
        map.put(603, "Angola (Republic of)");
        map.put(605, "Algeria (People's Democratic Republic of)");
        map.put(607, "Saint Paul and Amsterdam Islands");
        map.put(608, "Ascension Island");
        map.put(609, "Burundi (Republic of)");
        map.put(610, "Benin (Republic of)");
        map.put(611, "Botswana (Republic of)");
        map.put(612, "Central African Republic");
        map.put(613, "Cameroon (Republic of)");
        map.put(615, "Congo (Republic of the)");
        map.put(616, "Comoros (Union of the)");
        map.put(617, "Cape Verde (Republic of)");
        map.put(618, "Crozet Archipelago");
        map.put(619, "Côte d'Ivoire (Republic of)");
        map.put(621, "Djibouti (Republic of)");
        map.put(622, "Egypt (Arab Republic of)");
        map.put(624, "Ethiopia (Federal Democratic Republic of)");
        map.put(625, "Eritrea");
        map.put(626, "Gabonese Republic");
        map.put(627, "Ghana");
        map.put(629, "Gambia (Republic of the)");
        map.put(630, "Guinea-Bissau (Republic of)");
        map.put(631, "Equatorial Guinea (Republic of)");
        map.put(632, "Guinea (Republic of)");
        map.put(633, "Burkina Faso");
        map.put(634, "Kenya (Republic of)");
        map.put(635, "Kerguelen Islands");
        map.put(636, "Liberia (Republic of)");
        map.put(637, "Liberia (Republic of)");
        map.put(642, "Socialist People's Libyan Arab Jamahiriya");
        map.put(644, "Lesotho (Kingdom of)");
        map.put(645, "Mauritius (Republic of)");
        map.put(647, "Madagascar (Republic of)");
        map.put(649, "Mali (Republic of)");
        map.put(650, "Mozambique (Republic of)");
        map.put(654, "Mauritania (Islamic Republic of)");
        map.put(655, "Malawi");
        map.put(656, "Niger (Republic of the)");
        map.put(657, "Nigeria (Federal Republic of)");
        map.put(659, "Namibia (Republic of)");
        map.put(660, "Reunion (French Department of)");
        map.put(661, "Rwanda (Republic of)");
        map.put(662, "Sudan (Republic of the)");
        map.put(663, "Senegal (Republic of)");
        map.put(664, "Seychelles (Republic of)");
        map.put(665, "Saint Helena");
        map.put(666, "Somali Democratic Republic");
        map.put(667, "Sierra Leone");
        map.put(668, "Sao Tome and Principe (Democratic Republic of)");
        map.put(669, "Swaziland (Kingdom of)");
        map.put(670, "Chad (Republic of)");
        map.put(671, "Togolese Republic");
        map.put(672, "Tunisia");
        map.put(674, "Tanzania (United Republic of)");
        map.put(675, "Uganda (Republic of)");
        map.put(676, "Democratic Republic of the Congo");
        map.put(677, "Tanzania (United Republic of)");
        map.put(678, "Zambia (Republic of)");
        map.put(679, "Zimbabwe (Republic of)");
        map.put(701, "Argentine Republic");
        map.put(710, "Brazil (Federative Republic of)");
        map.put(720, "Bolivia (Plurinational State of)");
        map.put(725, "Chile");
        map.put(730, "Colombia (Republic of)");
        map.put(735, "Ecuador");
        map.put(740, "Falkland Islands (Malvinas)");
        map.put(745, "Guiana (French Department of)");
        map.put(750, "Guyana");
        map.put(755, "Paraguay (Republic of)");
        map.put(760, "Peru");
        map.put(765, "Suriname (Republic of)");
        map.put(770, "Uruguay (Eastern Republic of)");
        map.put(775, "Venezuela (Bolivarian Republic of)");
    }
    
    public static final String getCountry(int mmsi)
    {
        String ms = String.valueOf(mmsi);
        if (ms.length() < 3)
        {
            return null;
        }
        String s = ms.substring(0, 3);
        int mid = Integer.parseInt(s);
        return map.get(mid);
    }
}
