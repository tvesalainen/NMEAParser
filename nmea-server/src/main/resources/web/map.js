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

/* global Map, SVG_NS m2d d2m */

var METER_DEGREE = 60*1852;

function Map(parent)
{
    this.svg = document.createElementNS(SVG_NS, 'svg');
    parent.appendChild(this.svg);
    this.svg.setAttributeNS(null, 'preserveAspectRatio', 'xMaxYMax');
    
    this.call = function(data)
    {
        var json = JSON.parse(data);
        var name = json['name'];
        if (name === 'anchorManager')
        {
            
        }
        var value = json['value'];
        if (value)
        {
            switch (name)
            {
                case 'seabedSquare':
                    break;
                case 'lonMin':
                    this.lonMin = value;
                    this.updateViewBox();
                    break;
                case 'latMin':
                    this.latMin = value;
                    this.updateViewBox();
                    break;
                case 'lonWidth':
                    this.width = value;
                    this.updateViewBox();
                    break;
                case 'latHeight':
                    this.height = value;
                    this.updateViewBox();
                    break;
            }
        }
    };

    this.updateViewBox = function()
    {
        if (this.lonMin && this.latMin && this.width && this.height)
        {
            this.svg.setAttributeNS(null, 'viewBox', this.lonMin+' '+this.latMin+' '+this.width+' '+this.height);
            this.svg.setAttributeNS(null, 'transform', 'matrix('+1/this.departure()+' 0 0 -1 0 '+this.height+')');
        }
    };  

    this.tick = function()
    {
    };
    
    this.departure = function()
    {
        return Math.cos(this.latMin*Math.PI/180);
    };
    
}
function m2d(m)
{
    return m/METER_DEGREE;
};
function d2m(m)
{
    return m*METER_DEGREE;
};
