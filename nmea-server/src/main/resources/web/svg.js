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
    var viewBox = x+" "+y+" "+width+" "+height; //'-50,-40,100,40';
    this.svgNS = 'http://www.w3.org/2000/svg';
    this.xlinkNS = 'http://www.w3.org/1999/xlink';
    this.svg = createSvg(viewBox);

    this.setFrame = function()
    {
        this.svg.appendChild(createFrame(this.x, this.y, this.width, this.height));
    }

    this.setTitle = function(str)
    {
        if (!this.title)
        {
            this.titleText = '';
            this.title = createTitle(this.x, this.y, this.padding);
            this.svg.appendChild(this.title);
            this.titleText = document.createTextNode('');
            this.title.appendChild(this.titleText);
        }
        var t = document.createTextNode(str);
        this.title.replaceChild(t, this.titleText);
        this.titleText = t;
    };
    this.setUnit = function(str)
    {
        if (!this.unit)
        {
            this.unit = createUnit(this.x, this.y, this.width, this.height, this.padding);
            this.svg.appendChild(this.unit);
            this.unitText = document.createTextNode('');
            this.unit.appendChild(this.unitText);
        }
        this.unitString = str;
        var t = document.createTextNode(str);
        this.unit.replaceChild(t, this.unitText);
        this.unitText = t;
    };

    this.setText = function(str)
    {
        if (!this.text)
        {
            this.text = createText(this.x, this.y, this.width, this.height, this.padding);
            this.svg.appendChild(this.text);
            this.txt = document.createTextNode('');
            this.text.appendChild(this.txt);
        }
        var t = document.createTextNode(str);
        this.text.replaceChild(t, this.txt);
        this.txt = t;
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
        this.compass = createCompass(r*0.8);
        this.svg.appendChild(this.compass);

        this.boatGroup = createBoat(r/3);
        this.svg.appendChild(this.boatGroup);

        this.windIndicatorGroup = createWindIndicator(r);
        this.boatGroup.appendChild(this.windIndicatorGroup);
    };
    this.setTrueHeading = function(heading)
    {
        this.boatGroup.setAttributeNS(null, "transform", "rotate("+heading+")");
    };
    this.setRelativeWindAngle = function(angle)
    {
        this.windIndicatorGroup.setAttributeNS(null, "transform", "rotate("+angle+")");
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
