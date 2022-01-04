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
    this.svg = document.createElementNS(this.svgNS, 'svg');
    this.svg.setAttributeNS(null, 'viewBox', viewBox);

    this.setFrame = function()
    {
        var frame = document.createElementNS(this.svgNS, 'rect');
        frame.setAttributeNS(null, 'x', this.x);
        frame.setAttributeNS(null, 'y', this.y);
        frame.setAttributeNS(null, 'width', this.width);
        frame.setAttributeNS(null, 'height', this.height);
        frame.setAttributeNS(null, 'fill', 'none');
        frame.setAttributeNS(null, 'stroke', 'blue');
        frame.setAttributeNS(null, 'stroke-width', '1');
        this.svg.appendChild(frame);
    }

    this.setTitle = function(str)
    {
        if (!this.title)
        {
            this.titleText = '';
            this.title = document.createElementNS(this.svgNS, 'text');
            this.svg.appendChild(this.title);
            this.title.setAttribute("class", "title");
            this.title.setAttributeNS(null, "x", this.x+this.padding);
            this.title.setAttributeNS(null, "y", this.y+this.padding*2);
            this.title.setAttributeNS(null, "text-anchor", "start");
            this.titleText = document.createTextNode('');
            this.title.appendChild(this.titleText);
            this.title.setAttributeNS(null, "style", "font-size: 0.5em");
        }
        var t = document.createTextNode(str);
        this.title.replaceChild(t, this.titleText);
        this.titleText = t;
    };
    this.setUnit = function(str)
    {
        if (!this.unit)
        {
            this.unit = document.createElementNS(this.svgNS, 'text');
            this.svg.appendChild(this.unit);
            this.unit.setAttribute("class", "unit");
            this.unit.setAttributeNS(null, "x", this.x+this.width-this.padding);
            this.unit.setAttributeNS(null, "y", this.y+this.padding*2);
            this.unit.setAttributeNS(null, "text-anchor", "end");
            this.unitText = document.createTextNode('');
            this.unit.appendChild(this.unitText);
            this.unit.setAttributeNS(null, "style", "font-size: 0.5em");
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
            this.text = document.createElementNS(this.svgNS, 'text');
            this.svg.appendChild(this.text);
            this.text.setAttribute("class", "text");
            this.text.setAttributeNS(null, "x", this.x+this.width-this.padding);
            this.text.setAttributeNS(null, "y", this.y+this.height-5);
            this.text.setAttributeNS(null, "text-anchor", "end");
            this.text.setAttributeNS(null, "style", "font-size: 2em");
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
            this.history = document.createElementNS(this.svgNS, 'svg');
            this.svg.appendChild(this.history);
            var gap = max - min;
            this.ratio = history/gap;
            this.history.setAttributeNS(null, 'x', this.x);
            this.history.setAttributeNS(null, 'y', this.y);
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
                        div *= 10;
                        un = '10min';
                        break;
                    case 600000:
                        div *= 6;
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
    this.tacktical = function(r)
    {
        this.compass = document.createElementNS(this.svgNS, 'g');
        this.svg.appendChild(this.compass);

        var use = document.createElementNS(this.svgNS, 'use');
        this.compass.appendChild(use);
        use.setAttributeNS(this.xlinkNS, "href", "/defs.svg#compass-scale-1");
        use.setAttributeNS(null, "transform", "scale("+r+")");
        use.setAttributeNS(null, "stroke", "black");
        use.setAttributeNS(null, "stroke-width", 0.3/r);

        var use = document.createElementNS(this.svgNS, 'use');
        this.compass.appendChild(use);
        use.setAttributeNS(this.xlinkNS, "href", "/defs.svg#compass-scale-5");
        use.setAttributeNS(null, "transform", "scale("+r+")");
        use.setAttributeNS(null, "stroke", "black");
        use.setAttributeNS(null, "stroke-width", 0.6/r);

        var use = document.createElementNS(this.svgNS, 'use');
        this.compass.appendChild(use);
        use.setAttributeNS(this.xlinkNS, "href", "/defs.svg#compass-scale-10");
        use.setAttributeNS(null, "transform", "scale("+r+")");
        use.setAttributeNS(null, "stroke", "black");
        use.setAttributeNS(null, "stroke-width", 1/r);

        var use = document.createElementNS(this.svgNS, 'use');
        this.compass.appendChild(use);
        use.setAttributeNS(this.xlinkNS, "href", "/defs.svg#compass-scale");
        use.setAttributeNS(null, "transform", "scale("+r+")");
        use.setAttributeNS(null, "font-size", "0.008em");
        
        var g1 = document.createElementNS(this.svgNS, 'g');
        this.svg.appendChild(g1);
        //g1.setAttributeNS(null, "style", "display: none;");
        g1.setAttributeNS(null, "transform", "rotate(0)");
        //g1.setAttributeNS(null, AbstractSSESource.EventSink, "BearingToDestination-"+DEGREE+"-"+Route1);

        var g2 = document.createElementNS(this.svgNS, 'g');
        g1.appendChild(g2);
        g2.setAttributeNS(null, "transform", "rotate(0)");
        //g2.setAttributeNS(null, AbstractSSESource.EventSink, "CrossTrackError-"+NAUTICAL_MILE+"-"+Route2);
        
        var g3 = document.createElementNS(this.svgNS, 'g');
        g2.appendChild(g3);
        g3.setAttributeNS(null, "transform", "rotate(0)")
                //.setAttr(AbstractSSESource.EventSink, "CrossTrackError-"+NAUTICAL_MILE+"-"+Route3);

        var path1 = document.createElementNS(this.svgNS, 'path');
        g3.appendChild(path1);
        path1.setAttributeNS(null, "id", "centerLine");
        path1.setAttributeNS(null, "stroke", "green");
        path1.setAttributeNS(null, "stroke-width", "1");
        path1.setAttributeNS(null, "fill", "none");
        path1.setAttributeNS(null, "d", "M 0 50 l 0 -100");

        var path2 = document.createElementNS(this.svgNS, 'path');
        g3.appendChild(path2);
        path2.setAttributeNS(null, "id", "leftLine");
        path2.setAttributeNS(null, "stroke", "green");
        path2.setAttributeNS(null, "stroke-width", "1");
        path2.setAttributeNS(null, "fill", "none");
        path2.setAttributeNS(null, "d", "M -30 50 l 0 -100");

        var path3 = document.createElementNS(this.svgNS, 'path');
        g3.appendChild(path3);
        path3.setAttributeNS(null, "id", "rightLine");
        path3.setAttributeNS(null, "stroke", "green");
        path3.setAttributeNS(null, "stroke-width", "1");
        path3.setAttributeNS(null, "fill", "none");
        path3.setAttributeNS(null, "d", "M 30 50 l 0 -100");
        
        var path4 = document.createElementNS(this.svgNS, 'path');
        g3.appendChild(path4);
        path4.setAttributeNS(null, "id", "boat");
        //path4.setAttributeNS(null, "style", "display: none;");
        path4.setAttributeNS(null, "stroke", "blue");
        path4.setAttributeNS(null, "stroke-width", "1");
        path4.setAttributeNS(null, "fill", "none");
        path4.setAttributeNS(null, "transform", "rotate(0)");
        //.setAttr(AbstractSSESource.EventSink, "TrueHeading-"+DEGREE+"-"+Rotate)
        path4.setAttributeNS(null, "d", 
                "M -20 40 l 40 0 "+
                "C 23 10 20 -15 0 -40"+
                "M -20 40 "+
                "C -23 10 -20 -15 0 -40"
        );

        var relativeWindAngle = document.createElementNS(this.svgNS, 'g');
        this.svg.appendChild(relativeWindAngle);
                //relativeWindAngle.setAttributeNS(null, "style", "display: none;");
                relativeWindAngle.setAttributeNS(null, "transform", "rotate(0)");
                //.setAttr(AbstractSSESource.EventSink, "RelativeWindAngle-"+DEGREE+"-"+BoatRelativeRotate);

        var relativePath1 = document.createElementNS(this.svgNS, 'path');
        relativeWindAngle.appendChild(relativePath1);
        relativePath1.setAttributeNS(null, "id", "windIndicatorTip");
        relativePath1.setAttributeNS(null, "stroke", "red");
        relativePath1.setAttributeNS(null, "stroke-width", "1");
        relativePath1.setAttributeNS(null, "fill", "red");
        relativePath1.setAttributeNS(null, "d", "M -3 -15 L 0 -25 L 3 -15 Z");

        var relativePath2 = document.createElementNS(this.svgNS, 'path');
        relativeWindAngle.appendChild(relativePath2);
        relativePath2.setAttributeNS(null, "id", "windIndicatorShaft");
        relativePath2.setAttributeNS(null,"stroke", "black");
        relativePath2.setAttributeNS(null,"stroke-width", "1");
        relativePath2.setAttributeNS(null,"fill", "none");
        relativePath2.setAttributeNS(null,"d", "M 0 15 l 0 -30");

        var relativePath3 = document.createElementNS(this.svgNS, 'path');
        relativeWindAngle.appendChild(relativePath3);
        relativePath3.setAttributeNS(null,"id", "windIndicatorTail");
        relativePath3.setAttributeNS(null,"stroke", "red");
        relativePath3.setAttributeNS(null,"stroke-width", "1");
        relativePath3.setAttributeNS(null,"fill", "red");
        relativePath3.setAttributeNS(null,"d", "M -3 25 L -3 20 L 0 15 L 3 20 L 3 25 Z");

        
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
