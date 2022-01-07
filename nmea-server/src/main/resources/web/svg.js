/* 
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/* global Svg, Title, Svg, serverMillis */

var offsetMillis;
var refreshables = [];

function refresh()
{
    for (var i=0;i<refreshables.length;i++)
    {
        refreshables[i].refresh();
    }
}

function setServerTime(serverMillis)
{
    var now = new Date();
    offsetMillis = now.getTime() - serverMillis;
}
function getServerTime()
{
    var now = new Date();
    return now.getTime() - offsetMillis;
}
function Svg(x, y, width, height)
{
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.padding = 4;
    this.svgNS = 'http://www.w3.org/2000/svg';
    this.xlinkNS = 'http://www.w3.org/1999/xlink';
    this.svg = createSvg(x, y, width, height);

    this.setFrame = function()
    {
        this.svg.appendChild(createFrame(this.x, this.y, this.width, this.height));
    }

    this.setTitle = function(str)
    {
        if (!this.title)
        {
            this.title = createTitle(this.x, this.y, this.padding);
            this.svg.appendChild(this.title);
        }
        this.title.innerHTML = str;
    };
    this.setUnit = function(str)
    {
        if (!this.unit)
        {
            this.unit = createUnit(this.x, this.y, this.width, this.height, this.padding);
            this.svg.appendChild(this.unit);
        }
        this.unit.innerHTML = str;
    };

    this.setText = function(str)
    {
        if (!this.text)
        {
            this.text = createText(this.x, this.y, this.width, this.height, this.padding);
            this.svg.appendChild(this.text);
        }
        this.text.innerHTML = str;
    };
    this.setHistory = function(history, min, max)
    {
        if (history > 0)
        {
            this.historyMillis = history;
            this.min = min;
            this.max = max;
            this.gap = max - min;
            this.ratioX = this.historyMillis/this.width;
            this.ratioY = this.gap/this.height;
            this.history = createHistory(history, min, max, this.width, this.height, this.unitString);
            this.svg.appendChild(this.history);
            this.polyline = document.createElementNS(this.svgNS, 'polyline');
            this.history.appendChild(this.polyline);
            this.polyline.setAttributeNS(null, "stroke-width", "0.02em");
            this.data = [];
            refreshables.push(this);
        }
    };
    this.tacktical = function(r)
    {
        this.compass = createCompass(r*0.8, "yes");
        this.svg.appendChild(this.compass);

        //this.variation = createCompass(r*0.62);
        //this.svg.appendChild(this.variation);

        this.boat = createBoat(r/3);
        this.svg.appendChild(this.boat);

        this.windIndicator = createWindIndicator(r);
        this.boat.appendChild(this.windIndicator);
        
        this.windArrow = createWindArrow(r);
        this.boat.appendChild(this.windArrow);
        this.windArrowPath = document.createElementNS(SVG_NS, 'path');
        this.windArrow.appendChild(this.windArrowPath);
        this.windArrowPath.setAttributeNS(null,"id", "windArrow");
        this.windArrowPath.setAttributeNS(null,"stroke-width", "1");
        
        this.cog =  createCOG(r);
        this.svg.appendChild(this.cog);
    };
    this.setTrueHeading = function(heading)
    {
        this.boat.setAttributeNS(null, "transform", "rotate("+heading+")");
    };
    this.setRelativeWindAngle = function(angle)
    {
        this.windIndicator.setAttributeNS(null, "transform", "rotate("+angle+")");
    };
    this.setVariation = function(angle)
    {
        //this.variation.setAttributeNS(null, "transform", "rotate("+angle+")");
    };
    this.setTrueWindAngle = function(angle)
    {
        this.windArrow.setAttributeNS(null, "transform", "scale(0.4) rotate("+angle+") translate(0, -60)");
    };
    this.setCOG = function(angle)
    {
        this.cog.setAttributeNS(null, "transform", "rotate("+angle+")");
    };
    this.setSpeedOverGround = function(knots)
    {
        if (knots > 1)
        {
            this.cog.setAttributeNS(null, "display", "inline");
        }
        else
        {
            this.cog.setAttributeNS(null, "display", "none");
        }
    }
    this.setTrueWindSpeed = function(knots)
    {
        var d = getWindArrowPath(knots);
        var color = getWindArrowColor(knots);
        this.windArrowPath.setAttributeNS(null, "d", d);
        this.windArrowPath.setAttributeNS(null, "fill", color);
    };
    this.setData = function(time, value)
    {
        setServerTime(time);
        this.setText(value);
        if (this.history)
        {
            while (this.data.length > 0 && (time - this.data[0]) > this.historyMillis)
            {
                this.data.shift();
                this.data.shift();
            }
            this.data.push(time);
            this.data.push(value);
        }
    };
    this.refresh = function()
    {
        var arr = [];
        var time = getServerTime();
        var len = this.data.length/2;
        for (var i=0;i<len;i++)
        {
            var t = this.data[2*i];
            var v = this.data[2*i+1];
            arr.push((this.historyMillis-(time-t))/this.ratioX);
            arr.push((this.max - v)/this.ratioY); // (max-min)-(v-min)
        }
        var v = this.data[2*(len-1)+1];
        arr.push(this.historyMillis/this.ratioX);
        arr.push((this.max - v)/this.ratioY);
        this.polyline.setAttributeNS(null, "points", arr.join(' '));
    };
}
