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

/* global Svg, Title, OneRow, serverMillis */

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
function OneRow()
{
    this.viewBox = '-50,-40,100,40';
    this.svgNS = 'http://www.w3.org/2000/svg';
    this.xlinkNS = 'http://www.w3.org/1999/xlink';
    this.svg = document.createElementNS(this.svgNS, 'svg');
    this.svg.setAttributeNS(null, 'viewBox', this.viewBox);
    var rect = document.createElementNS(this.svgNS, 'rect');
    rect.setAttributeNS(null, 'x', '-50');
    rect.setAttributeNS(null, 'y', '-40');
    rect.setAttributeNS(null, 'width', '100');
    rect.setAttributeNS(null, 'height', '40');
    rect.setAttributeNS(null, 'fill', 'none');
    rect.setAttributeNS(null, 'stroke', 'blue');
    rect.setAttributeNS(null, 'stroke-width', '2');
    this.svg.appendChild(rect);

    this.titleText = '';
    this.title = document.createElementNS(this.svgNS, 'text');
    this.svg.appendChild(this.title);
    this.title.setAttribute("class", "title");
    this.title.setAttributeNS(null, "x", "-45");
    this.title.setAttributeNS(null, "y", "-32");
    this.title.setAttributeNS(null, "text-anchor", "start");
    this.titleText = document.createTextNode('');
    this.title.appendChild(this.titleText);
    this.title.setAttributeNS(null, "style", "font-size: 0.5em");

    this.unit = document.createElementNS(this.svgNS, 'text');
    this.svg.appendChild(this.unit);
    this.unit.setAttribute("class", "unit");
    this.unit.setAttributeNS(null, "x", "45");
    this.unit.setAttributeNS(null, "y", "-32");
    this.unit.setAttributeNS(null, "text-anchor", "end");
    this.unitText = document.createTextNode('');
    this.unit.appendChild(this.unitText);
    this.unit.setAttributeNS(null, "style", "font-size: 0.5em");
    this.setTitle = function(str)
    {
        var t = document.createTextNode(str);
        this.title.replaceChild(t, this.titleText);
        this.titleText = t;
    };
    this.setUnit = function(str)
    {
        this.unitString = str;
        var t = document.createTextNode(str);
        this.unit.replaceChild(t, this.unitText);
        this.unitText = t;
    };

    this.text = document.createElementNS(this.svgNS, 'text');
    this.svg.appendChild(this.text);
    this.text.setAttribute("class", "text");
    this.text.setAttributeNS(null, "x", "40");
    this.text.setAttributeNS(null, "y", "-5");
    this.text.setAttributeNS(null, "text-anchor", "end");
    this.text.setAttributeNS(null, "style", "font-size: 2em");
    this.txt = document.createTextNode('');
    this.text.appendChild(this.txt);
    this.setText = function(str)
    {
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
            this.history = document.createElementNS(this.svgNS, 'svg');
            this.svg.appendChild(this.history);
            var gap = max - min;
            this.ratio = history/gap;
            this.history.setAttributeNS(null, 'x', '-50');
            this.history.setAttributeNS(null, 'y', '-40');
            this.history.setAttributeNS(null, 'viewBox', "0 0 "+gap+" "+gap);
            this.history.setAttributeNS(null, "preserveAspectRatio", "none");
            this.history.setAttributeNS(null, "fill", "none");
            this.history.setAttributeNS(null, "stroke", "currentColor");
            this.history.setAttributeNS(null, "stroke-opacity", "0.5");
            this.polyline = document.createElementNS(this.svgNS, 'polyline');
            this.history.appendChild(this.polyline);
            this.polyline.setAttributeNS(null, "stroke-width", "0.5");
            this.data = [];
            refreshables.push(this);
            // x-scale
            var div = 1;
            var un;
            while (history / div > 10)
            {
                switch (div)
                {
                    case 1:
                        div *= 1000;
                        un = 'sec';
                        break;
                    case 1000:
                        div *= 60;
                        un = 'min';
                        break;
                    case 60000:
                        div *= 60;
                        un = 'hour';
                        break;
                    case 3600000:
                        div *= 2;
                        un = '2hour';
                        break;
                    case 7200000:
                        div *= 1.5;
                        un = '3hour';
                        break;
                    case 14400000:
                        div *= 2;
                        un = '6hour';
                        break;
                    case 28800000:
                        div *= 2;
                        un = '12hour';
                        break;
                    case 57600000:
                        div *= 2;
                        un = 'day';
                        break;
                    default:
                        un = undefined;
                        break;
                }
            }
            var grid = document.createElementNS(this.svgNS, 'path');
            grid.setAttributeNS(null, "stroke-width", "0.3");
            this.history.appendChild(grid);
            var d = "";
            var x = history;
            while (x > 0)
            {
                
                d += "M "+x/this.ratio+" 0 V"+gap;
                x -= div;
            }
            div = 1;
            while (gap / div > 5)
            {
                switch (div)
                {
                    case 1:
                        div = 5;
                        break;
                    default:
                        div *= 2;
                        break;
                }
            }
            x = gap;
            while (x > 0)
            {
                
                d += "M 0 "+x+" H"+history/this.ratio;
                x -= div;
            }
            grid.setAttributeNS(null, "d", d);
            var unit = document.createElementNS(this.svgNS, 'text');
            this.history.appendChild(unit);
            var xx = gap/2;
            var yy = gap-4;
            unit.setAttributeNS(null, "x", xx);
            unit.setAttributeNS(null, "y", yy);
            unit.setAttributeNS(null, "text-anchor", "middle");
            unit.setAttributeNS(null, "style", "font-size: 0.5em");
            unit.setAttributeNS(null, "stroke-width", "0.5");
            var unitText = document.createTextNode("Grid: "+div+" "+this.unitString+" / "+un);
            unit.appendChild(unitText);
        }
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
            arr.push((this.historyMillis-(time-t))/this.ratio);
            arr.push(this.max - v - this.min);
        }
        this.polyline.setAttributeNS(null, "points", arr.join(' '));
    };
}
