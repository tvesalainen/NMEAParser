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

/* global SVG_NS, XLINK_NS */

function Boat(parent, length, beam)
{
    this.g = document.createElementNS(SVG_NS, 'g');
    parent.appendChild(this.g);
    this.boat = document.createElementNS(SVG_NS, 'use');
    this.g.appendChild(this.boat);
    this.boat.setAttributeNS(null, "href", "#boat");
    //this.boat.setAttributeNS(null, "width", m2d(beam));
    //this.boat.setAttributeNS(null, "height", m2d(length));
    this.beam = m2d(beam);
    this.length = m2d(length);
    
    this.setHeading = function(hdg)
    {
        this.hdg = hdg;
        //this.boat.setAttributeNS(null, "transform", "rotate(-"+hdg+")");
    };
    this.setLongitude = function(lon)
    {
        this.lon = lon;
        this.transform();
    };
    this.setLatitude = function(lat)
    {
        this.lat = lat;
        this.dep = Math.cos(lat*Math.PI/180);
        this.transform();
    };
    this.transform = function()
    {
        if (this.lon && this.lat && this.hdg)
        {
            this.g.setAttributeNS(null, "transform", "translate("+this.lon+","+this.lat+")rotate(-"+this.hdg+")scale("+this.beam+","+this.length+")");
        }
    };
}
function createBoat(parent)
{
    var boat = document.createElementNS(SVG_NS, 'symbol');
    parent.appendChild(boat);
    boat.setAttributeNS(null, "id", "boat");
    boat.setAttributeNS(null, "viewBox", "-20 -40 40 80");
    boat.setAttributeNS(null, "preserveAspectRatio", "none");
    boat.setAttributeNS(null, "width", "1");
    boat.setAttributeNS(null, "height", "1");
    boat.setAttributeNS(null, "refX", "50%");
    boat.setAttributeNS(null, "refY", "50%");
    var path = document.createElementNS(SVG_NS, 'path');
    boat.appendChild(path);
    path.setAttributeNS(null, "d", 
            "M -20 -40 l 40 0 "+
            "C 23 -10 20 15 0 40"+
            "M -20 -40 "+
            "C -23 -10 -20 15 0 40"
    );
}