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

/* global createSvg createFrame createTitle createUnit createText createHistory createCompass createBoat createWindIndicator */

var SVG_NS = 'http://www.w3.org/2000/svg';
var XLINK_NS = 'http://www.w3.org/1999/xlink';


function createSvg(x, y, width, height)
{
    var viewBox = x+" "+y+" "+width+" "+height;
    var svg = document.createElementNS(SVG_NS, 'svg');
    svg.setAttributeNS(null, 'viewBox', viewBox);
    return svg;
};

function createFrame(x, y, width, height)
{
    var frame = document.createElementNS(SVG_NS, 'rect');
    frame.setAttributeNS(null, 'x', x);
    frame.setAttributeNS(null, 'y', y);
    frame.setAttributeNS(null, 'width', width);
    frame.setAttributeNS(null, 'height', height);
    frame.setAttributeNS(null, 'fill', 'none');
    frame.setAttributeNS(null, 'stroke', 'blue');
    frame.setAttributeNS(null, 'stroke-width', '1');
    return frame;
};

function createTitle(x, y, padding)
{
    var  title = document.createElementNS(SVG_NS, 'text');
    title.setAttribute("class", "title");
    title.setAttributeNS(null, "x", x+padding);
    title.setAttributeNS(null, "y", y+padding*2);
    title.setAttributeNS(null, "text-anchor", "start");
    title.setAttributeNS(null, "style", "font-size: 0.5em");
    return title;
};

function createUnit(x, y, width, height, padding)
{
    var unit = document.createElementNS(SVG_NS, 'text');
    unit.setAttribute("class", "unit");
    unit.setAttributeNS(null, "x", x+width-padding);
    unit.setAttributeNS(null, "y", y+padding*2);
    unit.setAttributeNS(null, "text-anchor", "end");
    unit.setAttributeNS(null, "style", "font-size: 0.5em");
    return unit;
};

function createText(x, y, width, height, padding)
{
    var text = document.createElementNS(SVG_NS, 'text');
    text.setAttribute("class", "text");
    text.setAttributeNS(null, "x", x+width-padding);
    text.setAttributeNS(null, "y", y+height-5);
    text.setAttributeNS(null, "text-anchor", "end");
    text.setAttributeNS(null, "style", "font-size: 2em");
    return text;
};

function createHistory(historyMillis, min, max, width, height, unitString)
{
    var gap = max - min;
    var ratioX = historyMillis/width;
    var ratioY = gap/height;
    var history = document.createElementNS(SVG_NS, 'g');
    history.setAttributeNS(null, "fill", "none");
    history.setAttributeNS(null, "stroke", "currentColor");
    history.setAttributeNS(null, "stroke-opacity", "0.5");
    // x-scale
    var div = 1;
    var un;
    while (historyMillis / div > 10)
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
            case 10800000:
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
    var grid = document.createElementNS(SVG_NS, 'path');
    grid.setAttributeNS(null, "stroke-width", "0.2");
    history.appendChild(grid);
    var d = "";
    var x = historyMillis;
    while (x > 0)
    {

        d += "M "+x/ratioX+" 0 V"+height;
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
    x = gap;
    while (x > 0)
    {

        d += "M 0 "+x/ratioY+" H"+width;
        x -= div;
    }
    grid.setAttributeNS(null, "d", d);
    var unitX = document.createElementNS(SVG_NS, 'text');
    history.appendChild(unitX);
    var xx = 3;
    var yy = height/2;
    unitX.setAttributeNS(null, "x", 0);
    unitX.setAttributeNS(null, "y", 0);
    unitX.setAttributeNS(null, "text-anchor", "middle");
    unitX.setAttributeNS(null, "style", "font-size: 0.15em");
    unitX.setAttributeNS(null, "stroke-width", "0.05");
    unitX.setAttributeNS(null, "transform", "translate("+xx+", "+yy+") rotate(-90) ");
    var unitTextX = document.createTextNode(div+" "+unitString);
    unitX.appendChild(unitTextX);
    var unitY = document.createElementNS(SVG_NS, 'text');
    history.appendChild(unitY);
    xx = width/2;
    yy = height-1;
    unitY.setAttributeNS(null, "x", xx);
    unitY.setAttributeNS(null, "y", yy);
    unitY.setAttributeNS(null, "text-anchor", "middle");
    unitY.setAttributeNS(null, "style", "font-size: 0.15em");
    unitY.setAttributeNS(null, "stroke-width", "0.05");
    var unitTextY = document.createTextNode(un);
    unitY.appendChild(unitTextY);
    return history;
};

function createCompass(r, haveNSWE)
{
    var compass = document.createElementNS(SVG_NS, 'g');

    var rulerR = r*0.94;
    var scale1 = createCompassScale(1, 0, 0, 1, 0.04);
    compass.appendChild(scale1);
    scale1.setAttributeNS(null, "transform", "scale("+rulerR+")");
    scale1.setAttributeNS(null, "stroke", "black");
    scale1.setAttributeNS(null, "stroke-width", 0.2/r);

    var scale5 = createCompassScale(1, 0, 0, 5, 0.05);
    compass.appendChild(scale5);
    scale5.setAttributeNS(null, "transform", "scale("+rulerR+")");
    scale5.setAttributeNS(null, "stroke", "black");
    scale5.setAttributeNS(null, "stroke-width", 0.3/r);

    var scale10 = createCompassScale(1, 0, 0, 10, 0.06);
    compass.appendChild(scale10);
    scale10.setAttributeNS(null, "transform", "scale("+rulerR+")");
    scale10.setAttributeNS(null, "stroke", "black");
    scale10.setAttributeNS(null, "stroke-width", 0.3/r);

    var scale90 = createCompassScale(1.15, 0, 0, 90, 0.1);
    compass.appendChild(scale90);
    scale90.setAttributeNS(null, "transform", "scale("+rulerR+")");
    scale90.setAttributeNS(null, "stroke", "black");
    scale90.setAttributeNS(null, "stroke-width", 0.3/r);

    var scale = createCircleScale(1.01, 0.06);
    compass.appendChild(scale);
    scale.setAttributeNS(null, "transform", "scale("+r+")");
    scale.setAttributeNS(null, "font-size", "0.005em");
    
    if (haveNSWE)
    {
        var nswe = createNSWE(1.2, 0.1);
        compass.appendChild(nswe);
        nswe.setAttributeNS(null, "transform", "scale("+r+")");
        nswe.setAttributeNS(null, "font-size", "0.01em");
    }
    return compass;
};
function createCompassScale(r1, cx, cy, step, length)
{
    return createCompassScale2(r1, cx, cy, 0, 360, step, length);
}

function toRadians(a)
{
    return a*Math.PI/180;
};
function createCompassScale2(r1, cx, cy, start, end, step, length)
{
    var sb = "";
    var r2 = r1 + length;
    for (var a = start; a < end; a += step)
    {
        if (step === 1 && (a % 5) === 0)
        {
            continue;
        }
        if (step === 5 && (a % 10) === 0)
        {
            continue;
        }
        var rad = toRadians(a);
        var sin = Math.sin(rad);
        var cos = Math.cos(rad);
        var x1 = cx + sin * r1;
        var y1 = cy - cos * r1;
        var x2 = cx + sin * r2;
        var y2 = cy - cos * r2;
        if (a > 0)
        {
            sb += " ";
        }
        sb += "M";
        sb += x1;
        sb += ",";
        sb += y1;
        sb += "L";
        sb += x2;
        sb += ",";
        sb += y2;
    }
    var g = document.createElementNS(SVG_NS, 'g');
    var path = document.createElementNS(SVG_NS, 'path');
    g.appendChild(path);
    path.setAttributeNS(null, "d", sb);
    path.setAttributeNS(null, "stroke-linecap", "round");
    return g;
};
function createCircleScale(r, length)
{
    var g = document.createElementNS(SVG_NS, 'g');
    
    for (var a = 0; a < 360; a += 10)
    {
        var text = document.createElementNS(SVG_NS, 'text');
        g.appendChild(text);
        var l = a.toString();
        var aTxt = document.createTextNode(l);
        text.appendChild(aTxt);
        text.setAttributeNS(null, "text-anchor", "middle");
        if (a > 90 && a < 270)
        {
            var b = a+180;
            var tick = r+length;
            text.setAttributeNS(null, "transform", "rotate(" + b + ") translate(0, "+tick+")");
        }
        else
        {
            text.setAttributeNS(null, "transform", "rotate(" + a + ") translate(0, -"+r+")");
        }
    }
    
    return g;
};
function createNSWE(r, length)
{
    var g = document.createElementNS(SVG_NS, 'g');
    
    for (var a = 0; a < 360; a += 90)
    {
        var text = document.createElementNS(SVG_NS, 'text');
        g.appendChild(text);
        var aTxt = document.createTextNode("N");
        text.appendChild(aTxt);
        text.setAttributeNS(null, "text-anchor", "middle");
        text.setAttributeNS(null, "x", "0");
        text.setAttributeNS(null, "y", -r);

        text = document.createElementNS(SVG_NS, 'text');
        g.appendChild(text);
        var aTxt = document.createTextNode("S");
        text.appendChild(aTxt);
        text.setAttributeNS(null, "text-anchor", "middle");
        text.setAttributeNS(null, "x", "0");
        text.setAttributeNS(null, "y", r+length);

        text = document.createElementNS(SVG_NS, 'text');
        g.appendChild(text);
        var aTxt = document.createTextNode("E");
        text.appendChild(aTxt);
        text.setAttributeNS(null, "text-anchor", "start");
        text.setAttributeNS(null, "x", r);
        text.setAttributeNS(null, "y", length/2);

        text = document.createElementNS(SVG_NS, 'text');
        g.appendChild(text);
        var aTxt = document.createTextNode("W");
        text.appendChild(aTxt);
        text.setAttributeNS(null, "text-anchor", "end");
        text.setAttributeNS(null, "x", -r);
        text.setAttributeNS(null, "y", length/2);
    }
    
    return g;
};
function createBoat(r)
{
    var boatGroup = document.createElementNS(SVG_NS, 'g');
    var g = document.createElementNS(SVG_NS, 'g');
    boatGroup.appendChild(g);
    g.setAttributeNS(null, "transform", `translate(0,-4) scale(${r/20})`);
    boatGroup.setAttributeNS(null, "transform", "rotate(0)");

    var boat = document.createElementNS(SVG_NS, 'path');
    g.appendChild(boat);
    boat.setAttributeNS(null, "id", "boat");
    boat.setAttributeNS(null, "stroke", "blue");
    boat.setAttributeNS(null, "stroke-width", "1");
    boat.setAttributeNS(null, "stroke-linejoin", "miter");
    boat.setAttributeNS(null, "fill", "none");
    boat.setAttributeNS(null, "transform", "rotate(0)");
    boat.setAttributeNS(null, "d", 
            "M -20 40 l 40 0 "+
            "C 23 10 20 -15 0 -40"+
            "M -20 40 "+
            "C -23 10 -20 -15 0 -40"
    );
    return boatGroup;
};
function createCOG(r)
{
    var cog = document.createElementNS(SVG_NS, 'g');
    var g = document.createElementNS(SVG_NS, 'g');
    cog.appendChild(g);

    var boat = document.createElementNS(SVG_NS, 'path');
    g.appendChild(boat);
    boat.setAttributeNS(null, "id", "boat");
    boat.setAttributeNS(null, "stroke", "blue");
    boat.setAttributeNS(null, "stroke-width", "1");
    boat.setAttributeNS(null, "fill", "red");
    boat.setAttributeNS(null, "d", 
            "M 2 -25 L 0 -34 L -2 -25 Z"
    );
    return cog;
};

function createWindIndicator(r)
{
    var windIndicatorGroup = document.createElementNS(SVG_NS, 'g');
    var g = document.createElementNS(SVG_NS, 'g');
    windIndicatorGroup.appendChild(g);
    g.setAttributeNS(null, "transform", `scale(${r/50})`);
    windIndicatorGroup.setAttributeNS(null, "transform", "rotate(0)");

    var windIndicatorTip = document.createElementNS(SVG_NS, 'path');
    g.appendChild(windIndicatorTip);
    windIndicatorTip.setAttributeNS(null, "id", "windIndicatorTip");
    windIndicatorTip.setAttributeNS(null, "stroke", "red");
    windIndicatorTip.setAttributeNS(null, "stroke-width", "1");
    windIndicatorTip.setAttributeNS(null, "fill", "red");
    windIndicatorTip.setAttributeNS(null, "d", "M -3 -15 L 0 -25 L 3 -15 Z");

    var windIndicatorShaft = document.createElementNS(SVG_NS, 'path');
    g.appendChild(windIndicatorShaft);
    windIndicatorShaft.setAttributeNS(null, "id", "windIndicatorShaft");
    windIndicatorShaft.setAttributeNS(null,"stroke", "black");
    windIndicatorShaft.setAttributeNS(null,"stroke-width", "1");
    windIndicatorShaft.setAttributeNS(null,"fill", "none");
    windIndicatorShaft.setAttributeNS(null,"d", "M 0 15 l 0 -30");

    var windIndicatorTail = document.createElementNS(SVG_NS, 'path');
    g.appendChild(windIndicatorTail);
    windIndicatorTail.setAttributeNS(null,"id", "windIndicatorTail");
    windIndicatorTail.setAttributeNS(null,"stroke", "red");
    windIndicatorTail.setAttributeNS(null,"stroke-width", "1");
    windIndicatorTail.setAttributeNS(null,"fill", "red");
    windIndicatorTail.setAttributeNS(null,"d", "M -3 25 L -3 20 L 0 15 L 3 20 L 3 25 Z");
    
    return windIndicatorGroup;

}
function createWindArrow(r)
{
    var g = document.createElementNS(SVG_NS, 'g');
    return g;

}