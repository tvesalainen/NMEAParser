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

/* global Svg, Title, Svg, serverMillis, zoneOffset setServerTime getServerTime, zoneOffset, timeOffset */

var offsetMillis=0;

function setServerTime(serverSeconds)
{
    var now = new Date();
    offsetMillis = now.getTime() - (serverSeconds*1000 + timeOffset);
}
function getServerTime()
{
    var now = new Date();
    return now.getTime() - offsetMillis;
}
function toShortTime(serverTime)
{
    return (serverTime - timeOffset)/1000;
}
function getShortTime()
{
    return toShortTime(getServerTime());
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
    };

    this.setTitle = function(title)
    {
        if (!this.title)
        {
            this.title = createTitle(this.svg, this.x, this.y, this.padding);
        }
        this.title.innerHTML = title;
    };

    this.setUnit = function(unit)
    {
        if (!this.unit)
        {
            this.unit = createUnit(this.svg, this.x, this.y, this.width, this.height, this.padding);
        }
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
    this.setText1 = function(str)
    {
        if (!this.text1)
        {
            this.text1 = createText1(this.svg, this.x, this.y, this.width, this.height, this.padding);
        }
        this.text1.innerHTML = str;
    };
    this.setText2 = function(str)
    {
        if (!this.text2)
        {
            this.text2 = createText2(this.svg, this.x, this.y, this.width, this.height, this.padding);
        }
        this.text2.innerHTML = str;
    };
    this.setHistory = function(history, min, max)
    {
        if (history > 0)
        {
            this.historySeconds = (history/1000);
            this.min = min;
            this.max = max;
            this.gap = max - min;
            var arr = createHistory(this.svg, history, min, max, this.width, this.height, this.unit.innerHTML);
            this.history = arr[0];
            this.polyline = arr[1];
            this.gridX = arr[2];
            this.gridY = arr[3];
            this.minText = arr[4];
            this.maxText = arr[5];
            this.graph = arr[6];
            this.cursor = arr[7];
            this.data = [];
        }
    };
    this.updateHistoryView = function()
    {
        var time = getShortTime();
        var x = time - this.historySeconds;
        var width = this.historySeconds;
        var y = this.min;
        var height = this.max - this.min;
        var coefX = 1.007;
        //x *= coefX;
        width *= coefX;
        var viewBox = x+" "+y+" "+width+" "+height;
        this.history.setAttributeNS(null, 'viewBox', viewBox);
        var ty = this.max + this.min;
        this.graph.setAttributeNS(null, 'transform', 'matrix(1 0 0 -1 0 '+ty+')');
        this.history.setAttributeNS(null, 'preserveAspectRatio', 'none');
    };
    this.setGridX = function()
    {
        // x-scale
        var shortTime = getShortTime();
        if (!(this.nextXGrid > shortTime))
        {
            var anc = new Date();
            anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDate(), 0, 0, 0, 0);
            var ny = toShortTime(anc.getTime());
            this.nextXGrid = shortTime+10000;
            this.gridX.setAttributeNS(null, "transform", "translate("+ny+")");
        }
    };        
    this.setGridY = function()
    {
        var shortTime = getShortTime();
        var xx = shortTime-this.historySeconds;
        this.gridY.setAttributeNS(null, "transform", "translate("+xx+")");
        this.minText.innerHTML = "Min "+this.min;
        this.maxText.innerHTML = "Max "+this.max;
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
    this.tide = function(r)
    {
        var size = 1.25*r;
        this.tide = createTide(this.svg, size*0.8);

    };
    this.setTide = function(r)
    {
        var high = r<90?90-r:360-r+90;
        var low = r<270?270-r:360-r+270;
        var st = localTime().getTime();
        var highTime = new Date(st+44700000*high/360);
        var is = highTime.toISOString();
        this.setText1(is.substr(11, 8));
        var lowTime = new Date(st+44700000*low/360);
        is = lowTime.toISOString();
        this.setText2(is.substr(11, 8));
    };
    this.setTidePhase = function(r)
    {
        var t = (180 - r)/3.6;
        this.tide.setAttributeNS(null, "transform", "translate("+t+", 0)");
    };
    this.setBoatRudder = function(r)
    {
        this.rudder.setAttributeNS(null, "transform", "rotate("+r+")");
    };
    this.rudder = function(r)
    {
        this.rudderAngle = createRudderMeter(this.svg, r);
    };
    this.setRudder = function(r)
    {
        this.rudderAngle.setAttributeNS(null, "transform", "rotate("+r+")");
    };
    this.inclino = function(r)
    {
        var arr = createInclinoMeter(this.svg, r);
        this.ball = arr[0];
        this.portLimit = arr[1];
        this.sbLimit = arr[2];
        this.minIncline = 90;
        this.maxIncline = -90;
    };
    this.eta = function()
    {
        this.data = [];
    };
    this.setEtaHistory = function(history)
    {
        this.historySeconds = history;
    };
    this.resetEta = function(time, value)
    {
        if (this.toWaypoint)
        {
            if (this.toWaypoint !== value)
            {
                this.data = [];
            }
        }
        this.toWaypoint = value;
    };
    this.setEta = function(time, value)
    {
        setServerTime(time);
        while (this.data.length > 0 && (time - this.data[0]) > this.historySeconds)
        {
            this.data.shift();
            this.data.shift();
        }
        this.data.push(time);
        this.data.push(value);

        var firstTime = this.data[0];
        var firstRange = this.data[1];
        var duration = time - firstTime;
        var range = firstRange - value;
        var left = value*duration/range;
        var eta = new Date(time+zoneOffset+left);
        var min = eta.getMinutes();
        if (min.length === 1)
        {
            min = "0"+min;
        }
        this.setText1(`${eta.getDate()}.${eta.getMonth()+1}. ${eta.getHours()}:${min}`);
        left = (left / 60000).toFixed(0);
        var m = (left % 60).toFixed(0);
        left = (left / 60).toFixed(0);
        var h = (left % 60).toFixed(0);
        left = (left / 24).toFixed(0);
        var d = left.toFixed(0);
        this.setText2(`${d}d${h}h${m}m`);
    };
    this.setRoll = function(roll)
    {
        var r = Number(-roll);
        this.ball.setAttributeNS(null, "transform", "rotate("+r+")");
        var d = 4;
        if (r < this.minIncline)
        {
            this.minIncline = r;
            var min = this.minIncline-d;
            this.sbLimit.setAttributeNS(null, "transform", "rotate("+min+")");
        }
        if (r > this.maxIncline)
        {
            this.maxIncline = r;
            var max = this.maxIncline+d;
            this.portLimit.setAttributeNS(null, "transform", "rotate("+max+")");
        }
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
    };
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
        if (this.historySeconds)
        {
            while (this.data.length > 0 && (time - this.data[0]) > this.historySeconds)
            {
                this.data.shift();
                this.data.shift();
            }
            this.data.push(time);
            this.data.push(value);
            this.updateHistory();
        }
        else
        {
            this.setText(value);
        }
    };
    this.setMin = function(min)
    {
        if (this.historySeconds)
        {
            this.min = Number(min);
            this.gap = this.max - this.min;
            this.setGridY();
            this.nextXGrid = 0;
            this.hasBounds = true;
        }
    };
    this.setMax = function(max)
    {
        if (this.historySeconds)
        {
            this.max = Number(max);
            this.gap = this.max - this.min;
            this.setGridY();
            this.nextXGrid = 0;
            this.hasBounds = true;
        }
    };
    this.updateHistory = function()
    {
        var time = getShortTime();
        if (time)
        {
            if (!this.hasBounds)
            {
                var min = this.min;
                var max = this.max;
                this.min = Number.MAX_VALUE;
                this.max = Number.MIN_VALUE;
                var len = this.data.length/2;
                var v;
                for (var i=0;i<len;i++)
                {
                    v = this.data[2*i+1];
                    this.min = Math.min(this.min, v);
                    this.max = Math.max(this.max, v);
                }
                if (min !== this.min || max !== this.max)
                {
                    this.setGridY();
                    this.nextXGrid = 0;
                }
            }
            this.gap = this.max - this.min;
            this.setGridX();
            this.polyline.setAttributeNS(null, "points", this.data.join(' '));
            var slice = this.data.slice(this.data.length-4);
            this.cursor.setAttributeNS(null, "points", slice.join(' '));
        }
    };
    this.setHistoryData = function(array)
    {
        this.data = array;
        this.updateHistory();
    };
    this.tick = function()
    {
        this.updateHistoryView();
    };
}
