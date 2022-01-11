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

    this.setTitle = function(title, unit)
    {
        if (!this.title)
        {
            this.title = createTitle(this.svg, this.x, this.y, this.padding);
            this.unit = createUnit(this.svg, this.x, this.y, this.width, this.height, this.padding);
        }
        this.title.innerHTML = title;
        this.unit.innerHTML = unit;
    };

    this.setText = function(str)
    {
        if (!this.text)
        {
            this.text = createText(this.svg, this.x, this.y, this.width, this.height, this.padding);
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
            this.history = createHistory(this.svg, history, min, max, this.width, this.height, this.unit.innerHTML);
            this.polyline = document.createElementNS(this.svgNS, 'polyline');
            this.history.appendChild(this.polyline);
            this.polyline.setAttributeNS(null, "stroke-width", "0.02em");
            this.data = [];
            refreshables.push(this);
        }
    };
    this.tacktical = function(r)
    {
        var size = 1.25*r;
        this.compass = createCompass(this.svg, size*0.8);

        //this.variation = createCompass(r*0.62);
        //this.svg.appendChild(this.variation);

        this.boat = createBoat(this.svg, size/3);
        this.rudder = createRudder(this.boat, size/3);

        this.windIndicator = createWindIndicator(this.boat, size);
        
        var arr = createWindArrow(this.boat, size);
        this.windArrow = arr[0];
        this.windArrowPath = arr[1];
        
        createTriangle(this.svg);
        this.cog =  createCOG(this.svg, size);
    };
    this.inclino = function(r)
    {
        var arr = createInclinoMeter(this.svg, r);
        this.ball = arr[0];
        this.portLimit = arr[1];
        this.sbLimit = arr[2];
        this.minIncline = 90;
        this.maxIncline = -90;
    }
    this.setRoll = function(roll)
    {
        this.ball.setAttributeNS(null, "transform", "rotate("+roll+")");
        this.minIncline = Math.min(roll, this.minIncline);
        this.maxIncline = Math.max(roll, this.maxIncline);
        var d = 14;
        var max = this.maxIncline+d;
        var min = this.minIncline-d;
        this.portLimit.setAttributeNS(null, "transform", "rotate("+max+")");
        this.sbLimit.setAttributeNS(null, "transform", "rotate("+min+")");
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
        this.windArrow.setAttributeNS(null, "transform", "rotate("+angle+")");
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
    this.setHistoryData = function(array)
    {
        this.data = array;
    }
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
