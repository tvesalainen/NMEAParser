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

function Anchoring(parent)
{
    this.svg = document.createElementNS(SVG_NS, 'svg');
    parent.appendChild(this.svg);
    this.svg.setAttributeNS(null, "style", "display:none");
    this.map = new Map(parent);
    createBoat(this.svg);
    
    this.call = function(data)
    {
        var json = JSON.parse(data);
        var name = json['name'];
        if (name === 'anchorManager')
        {
            this.boat = new Boat(this.map.svg, json['boatLength'], json['boatBeam']);
        }
        var value = json['value'];
        if (value)
        {
            switch (name)
            {
                case 'seabedSquare':
                    break;
                case 'trueHeading':
                    this.boat.setHeading(value);
                    break;
                case 'lon':
                    this.boat.setLongitude(value);
                    break;
                case 'lat':
                    this.boat.setLatitude(value);
                    break;
                default:
                    this.map.call(data);
                    break;
            }
        }
    };

    this.tick = function()
    {
    };
    
}
