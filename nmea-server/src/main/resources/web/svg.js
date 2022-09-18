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

/* global Svg, Title, Svg, serverMillis, zoneOffset setServerTime getServerTime, zoneOffset */

var offsetMillis=0;

function setServerTime(serverMillis)
{
    var now = new Date();
    offsetMillis = now.getTime() - (serverMillis + timeOffset);
}
function getServerTime()
{
    var now = new Date();
    return now.getTime() - offsetMillis;
}
function getShortTime()
{
    var now = new Date();
    return (now.getTime() - offsetMillis) - timeOffset;
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
            this.historyMillis = history;
            this.min = min;
            this.max = max;
            this.gap = max - min;
            this.ratioX = this.historyMillis/this.width;
            this.ratioY = this.gap/this.height;
            var arr = createHistory(this.svg, history, min, max, this.width, this.height, this.unit.innerHTML);
            this.history = arr[0];
            this.polyline = arr[1];
            this.grid = arr[2];
            this.minText = arr[3];
            this.maxText = arr[4];
            this.data = [];
        }
    };
    this.updateHistoryView = function()
    {
        var time = getShortTime();
        var x = time - this.historyMillis;
        var width = this.historyMillis;
        var y = this.min;
        var height = this.max - this.min;
        var viewBox = x+" "+y+" "+width+" "+height;
        this.history.setAttributeNS(null, 'viewBox', viewBox);
        this.history.setAttributeNS(null, 'preserveAspectRatio', 'none');
    };
    this.setGrid = function()
    {
        // x-scale
        var div = 1;
        var un;
        var x = this.historyMillis;
        var anc = new Date();
        while (x / div > 10)
        {
            switch (div)
            {
                case 1:
                    div *= 1000;
                    un = 'sec';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDay(), anc.getHours(), anc.getMinutes(), anc.getSeconds(), 0);
                    break;
                case 1000:
                    div *= 60;
                    un = 'min';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDay(), anc.getHours(), 0, 0, 0);
                    break;
                case 60000:
                    div *= 10;
                    un = '10min';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDay(), anc.getHours(), 0, 0, 0);
                    break;
                case 600000:
                    div *= 2;
                    un = '20min';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDay(), anc.getHours(), 0, 0, 0);
                    break;
                case 1200000:
                    div *= 1.5;
                    un = '30min';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDay(), anc.getHours(), 0, 0, 0);
                    break;
                case 1800000:
                    div *= 2;
                    un = 'hour';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDay(), 0, 0, 0, 0);
                    break;
                case 3600000:
                    div *= 2;
                    un = '2hour';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDay(), 0, 0, 0, 0);
                    break;
                case 7200000:
                    div *= 1.5;
                    un = '3hour';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDay(), 0, 0, 0, 0);
                    break;
                case 10800000:
                    div *= 2;
                    un = '6hour';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDay(), 0, 0, 0, 0);
                    break;
                case 28800000:
                    div *= 2;
                    un = '12hour';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), anc.getDay(), 0, 0, 0, 0);
                    break;
                case 57600000:
                    div *= 2;
                    un = 'day';
                    anc = new Date(anc.getFullYear(), anc.getMonth(), 0, 0, 0, 0, 0);
                    break;
                default:
                    un = undefined;
                    break;
            }
        }
        var time = getShortTime();
        var d = time - anc.getTime();
        var mod = time % div;
        x = time - mod;
        var gap = this.max - this.min;
        var d = "";
        for (let i=0;i<10;i++)
        {

            d += "M "+x+" "+this.min+" V"+gap;
            x -= div;
        }
        div = 0.1;
        while (this.gap / div > 5)
        {
            switch (div)
            {
                case 0.1:
                    div = 0.5;
                    break;
                case 0.5:
                    div = 1;
                    break;
                case 1:
                    div = 5;
                    break;
                case 5:
                    div = 10;
                    break;
                case 10:
                    div = 50;
                    break;
                case 50:
                    div = 100;
                    break;
                case 100:
                    div = 200;
                    break;
                case 200:
                    div = 500;
                    break;
                default:
                    div *= 10;
                    break;
            }
        }
        var mod;
        if (this.min > 0)
        {
            mod = (div -(this.min % div));
        }
        else
        {
            mod = -this.min % div;
        }
        x = this.gap-mod;
        while (x > 0)
        {

            //d += "M 0 "+x/this.ratioY+" H"+width;
            x -= div;
        }
        this.grid.setAttributeNS(null, "d", d);
        this.minText.innerHTML = this.min;
        this.maxText.innerHTML = this.max;
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
        this.historyMillis = history;
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
        while (this.data.length > 0 && (time - this.data[0]) > this.historyMillis)
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
        if (this.historyMillis)
        {
            while (this.data.length > 0 && (time - this.data[0]) > this.historyMillis)
            {
                this.data.shift();
                this.data.shift();
            }
            this.data.push(time);
            this.data.push(value);
        }
        else
        {
            this.setText(value);
        }
    };
    this.setHistoryData = function(array)
    {
        this.data = array;
    };
    this.tick = function()
    {
        if (this.data)
        {
            var arr = [];
            var time = getShortTime();
            if (time)
            {
                while (this.data.length > 0 && (time - this.data[0]) > this.historyMillis)
                {
                    this.data.shift();
                    this.data.shift();
                }
                this.min = Number.MAX_VALUE;
                this.max = Number.MIN_VALUE;
                var len = this.data.length/2;
                for (var i=0;i<len;i++)
                {
                    var v = this.data[2*i+1];
                    this.min = Math.min(this.min, v);
                    this.max = Math.max(this.max, v);
                }
                this.gap = this.max - this.min;
                this.ratioX = this.historyMillis/this.width;
                this.ratioY = this.gap/this.height;
                this.updateHistoryView();
                this.setGrid();
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
                this.polyline.setAttributeNS(null, "points", this.data.join(' '));
            }
        }
    };
}
