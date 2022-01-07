/* 
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
"use strict";

/* global getWindArrowPath getWindArrowColor */

var WIND_ARROWS = [
"",
"M-1 0L-1 -34L1 -34L1 0ZM1 -27L11 -32L11 -31L1 -26Z",
"M-1 0L-1 -34L1 -34L1 0ZM1 -34L18 -44L18 -43L1 -33Z",
"M-1 0L-1 -34L1 -34L1 0ZM1 -34L18 -44L18 -43L1 -33ZM1 -27L11 -32L11 -31L1 -26Z",
"M-1 0L-1 -34L1 -34L1 0ZM1 -34L18 -44L18 -43L1 -33ZM1 -27L18 -37L18 -36L1 -26Z",
"M-1 0L-1 -34L1 -34L1 0ZM1 -34L18 -44L18 -43L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L11 -25L11 -24L1 -19Z",
"M-1 0L-1 -34L1 -34L1 0ZM1 -34L18 -44L18 -43L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L18 -30L18 -29L1 -19Z",
"M-1 0L-1 -34L1 -34L1 0ZM1 -34L18 -44L18 -43L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L18 -30L18 -29L1 -19ZM1 -13L11 -18L11 -17L1 -12Z",
"M-1 0L-1 -34L1 -34L1 0ZM1 -34L18 -44L18 -43L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L18 -30L18 -29L1 -19ZM1 -13L18 -23L18 -22L1 -12Z",
"M-1 0L-1 -34L1 -34L1 0ZM1 -34L18 -44L18 -43L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L18 -30L18 -29L1 -19ZM1 -13L18 -23L18 -22L1 -12ZM1 -6L11 -11L11 -10L1 -5Z",
"M-1 0L-1 -34L1 -34L1 0ZM-1 -34L-1 -44L17 -44L1 -33Z",
"M-1 0L-1 -34L1 -34L1 0ZM-1 -34L-1 -44L17 -44L1 -33ZM1 -27L11 -32L11 -31L1 -26Z",
"M-1 0L-1 -34L1 -34L1 0ZM-1 -34L-1 -44L17 -44L1 -33ZM1 -27L18 -37L18 -36L1 -26Z",
"M-1 0L-1 -34L1 -34L1 0ZM-1 -34L-1 -44L17 -44L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L11 -25L11 -24L1 -19Z",
"M-1 0L-1 -34L1 -34L1 0ZM-1 -34L-1 -44L17 -44L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L18 -30L18 -29L1 -19Z",
"M-1 0L-1 -34L1 -34L1 0ZM-1 -34L-1 -44L17 -44L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L18 -30L18 -29L1 -19ZM1 -13L11 -18L11 -17L1 -12Z",
"M-1 0L-1 -34L1 -34L1 0ZM-1 -34L-1 -44L17 -44L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L18 -30L18 -29L1 -19ZM1 -13L18 -23L18 -22L1 -12Z",
"M-1 0L-1 -34L1 -34L1 0ZM-1 -34L-1 -44L17 -44L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L18 -30L18 -29L1 -19ZM1 -13L18 -23L18 -22L1 -12ZM1 -6L11 -11L11 -10L1 -5Z",
"M-1 0L-1 -34L1 -34L1 0ZM-1 -34L-1 -44L17 -44L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L18 -30L18 -29L1 -19ZM1 -13L18 -23L18 -22L1 -12ZM1 -6L18 -16L18 -15L1 -5Z",
"M-1 0L-1 -34L1 -34L1 0ZM-1 -34L-1 -44L17 -44L1 -33ZM1 -27L18 -37L18 -36L1 -26ZM1 -20L18 -30L18 -29L1 -19ZM1 -13L18 -23L18 -22L1 -12ZM1 -6L18 -16L18 -15L1 -5ZM1 1L11 -4L11 -3L1 2Z"
];
function getWindArrowPath(knots)
{
    return WIND_ARROWS[(knots/5).toFixed()];
};
function getWindArrowColor(knots)
{
    if (knots > 50)
    {
        return "black";
    }
    else if (knots > 40)
    {
        return "magenta";
    }
    else if (knots > 20)
    {
        return "red";
    }
    else if (knots > 15)
    {
        return "orange";
    }
    else if (knots > 10)
    {
        return "green";
    }
    else
    {
        return "gray";
    }

}
