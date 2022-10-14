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

/* global SVG_NS */

function Boat(parent, length, beam)
{
    this.boat = document.createElementNS(SVG_NS, 'g');
    parent.appendChild(this.boat);
    this.boat.setAttributeNS(null, "d", 
            "M -20 40 l 40 0 "+
            "C 23 10 20 -15 0 -40"+
            "M -20 40 "+
            "C -23 10 -20 -15 0 -40"
    );
    
    this.setHeading = function(hdg)
    {
        
    };
    this.setLongitude = function(lon)
    {
        
    };
    this.setLatitude = function(lat)
    {
        
    };
}
