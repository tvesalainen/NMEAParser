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

/* global createSvg createFrame createTitle createUnit createText createHistory createCompass createBoat createWindIndicator createRudder createInclinoMeter */

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
    frame.setAttributeNS(null, 'class', 'frame');
    frame.setAttributeNS(null, 'fill', 'none');
    //frame.setAttributeNS(null, 'stroke', 'blue');
    //frame.setAttributeNS(null, 'stroke-width', '1');
    return frame;
};

function createTitle(parent, x, y, padding)
{
    var  title = document.createElementNS(SVG_NS, 'text');
    title.setAttribute("class", "title");
    title.setAttributeNS(null, "x", x+padding);
    title.setAttributeNS(null, "y", y+padding*2);
    title.setAttributeNS(null, "text-anchor", "start");
    title.setAttributeNS(null, "fill", "currentColor");
    parent.appendChild(title);
    return title;
};

function createUnit(parent, x, y, width, height, padding)
{
    var unit = document.createElementNS(SVG_NS, 'text');
    unit.setAttribute("class", "unit");
    unit.setAttributeNS(null, "x", x+width-padding);
    unit.setAttributeNS(null, "y", y+padding*2);
    unit.setAttributeNS(null, "text-anchor", "end");
    unit.setAttributeNS(null, "fill", "currentColor");
    parent.appendChild(unit);
    return unit;
};

function createText(parent, x, y, width, height, padding)
{
    var text = document.createElementNS(SVG_NS, 'text');
    text.setAttribute("class", "text");
    text.setAttributeNS(null, "x", x+width-padding);
    text.setAttributeNS(null, "y", y+height-5);
    text.setAttributeNS(null, "text-anchor", "end");
    text.setAttributeNS(null, "fill", "currentColor");
    parent.appendChild(text);
    return text;
};

function createText1(parent, x, y, width, height, padding)
{
    var text = document.createElementNS(SVG_NS, 'text');
    text.setAttribute("class", "text1");
    text.setAttributeNS(null, "x", x+width-padding);
    text.setAttributeNS(null, "y", y+height-20);
    text.setAttributeNS(null, "text-anchor", "end");
    text.setAttributeNS(null, "fill", "currentColor");
    parent.appendChild(text);
    return text;
};

function createText2(parent, x, y, width, height, padding)
{
    var text = document.createElementNS(SVG_NS, 'text');
    text.setAttribute("class", "text2");
    text.setAttributeNS(null, "x", x+width-padding);
    text.setAttributeNS(null, "y", y+height-5);
    text.setAttributeNS(null, "text-anchor", "end");
    text.setAttributeNS(null, "fill", "currentColor");
    parent.appendChild(text);
    return text;
};

function createHistory(parent, historyMillis, min, max, width, height, unitString)
{
    var gap = max - min;
    var ratioX = historyMillis/width;
    var ratioY = gap/height;
    var history = document.createElementNS(SVG_NS, 'g');
    parent.appendChild(history);
    history.setAttributeNS(null, "class", "history");
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
                div *= 2;
                un = '20min';
                break;
            case 1200000:
                div *= 1.5;
                un = '30min';
                break;
            case 1800000:
                div *= 2;
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
    var g = document.createElementNS(SVG_NS, 'g');
    history.appendChild(g);
    g.setAttributeNS(null, "text-anchor", "middle");
    g.setAttributeNS(null, "stroke", "none");
    g.setAttributeNS(null, "fill", "currentColor");
    var unitX = document.createElementNS(SVG_NS, 'text');
    g.appendChild(unitX);
    var xx = 3;
    var yy = height/2;
    unitX.setAttributeNS(null, "x", 0);
    unitX.setAttributeNS(null, "y", 0);
    unitX.setAttributeNS(null, "transform", "translate("+xx+", "+yy+") rotate(-90) ");
    var unitTextX = document.createTextNode(div+" "+unitString);
    unitX.appendChild(unitTextX);
    var unitY = document.createElementNS(SVG_NS, 'text');
    g.appendChild(unitY);
    xx = width/2;
    yy = height-1;
    unitY.setAttributeNS(null, "x", xx);
    unitY.setAttributeNS(null, "y", yy);
    var unitTextY = document.createTextNode(un);
    unitY.appendChild(unitTextY);
    var polyline = document.createElementNS(SVG_NS, 'polyline');
    history.appendChild(polyline);
    polyline.setAttributeNS(null, "class", "history-graph");
    polyline.setAttributeNS(null, "fill", "none");
    polyline.setAttributeNS(null, "stroke", "currentColor");
    polyline.setAttributeNS(null, "stroke-width", "0.1em");
    return [history, polyline];
};

function createRudderMeter(parent, r)
{
    var size = 1.5*r;
    var g = document.createElementNS(SVG_NS, 'g');
    g.setAttributeNS(null, "class", "rudder");
    
    var scale1 = createCompassScale2(size, 0, 0, 140, 220, 1, 2);
    scale1.setAttributeNS(null, "stroke-width", 0.3);
    g.appendChild(scale1);

    var scale5 = createCompassScale2(size, 0, 0, 140, 220, 5, 3);
    scale5.setAttributeNS(null, "stroke-width", 0.5);
    g.appendChild(scale5);

    var scale10 = createCompassScale2(size, 0, 0, 140, 221, 10, 3.5);
    scale10.setAttributeNS(null, "stroke-width", 0.5);
    g.appendChild(scale10);

    var scale = createCircleScale2(size*1.15, 0, 0, 150, 211, 0.06, function(a)
    {
        return Math.abs(a-180);
    });
    scale.setAttributeNS(null, "class", "rudder-scale");
    scale.setAttributeNS(null, "fill", "currentColor");
    g.appendChild(scale);
    //scale.setAttributeNS(null, "font-size", "0.5em");
    
    var rudder = createRudder(g, r);
    rudder.setAttributeNS(null, "class", "rudder");
    rudder.setAttributeNS(null, "fill", "currentColor");
    
    parent.appendChild(g);
    return rudder;
};
function createInclinoMeter(parent, r)
{
    var size = 1.5*r;
    var g = document.createElementNS(SVG_NS, 'g');
    g.setAttributeNS(null, "class", "inclino");
    
    var scale1 = createCompassScale2(size, 0, 0, 140, 220, 1, 2);
    scale1.setAttributeNS(null, "stroke-width", 0.3);
    g.appendChild(scale1);

    var scale5 = createCompassScale2(size, 0, 0, 140, 220, 5, 3);
    scale5.setAttributeNS(null, "stroke-width", 0.5);
    g.appendChild(scale5);

    var scale10 = createCompassScale2(size, 0, 0, 140, 221, 10, 3.5);
    scale10.setAttributeNS(null, "stroke-width", 0.5);
    g.appendChild(scale10);

    var scale = createCircleScale2(size*1.15, 0, 0, 150, 211, 0.06, function(a)
    {
        return Math.abs(a-180);
    });
    scale.setAttributeNS(null, "class", "inclino-scale");
    scale.setAttributeNS(null, "fill", "currentColor");
    g.appendChild(scale);
    //scale.setAttributeNS(null, "font-size", "0.5em");
    
    var ball = document.createElementNS(SVG_NS, 'circle');
    g.appendChild(ball);
    ball.setAttributeNS(null, "class", "inclino-ball");
    ball.setAttributeNS(null, "cx", 0);
    ball.setAttributeNS(null, "cy", r*1.425);
    ball.setAttributeNS(null, "r", r*0.07);

    var portLimit = document.createElementNS(SVG_NS, 'line');
    g.appendChild(portLimit);
    portLimit.setAttributeNS(null, "class", "inclino-port");
    portLimit.setAttributeNS(null, "x1", 0);
    portLimit.setAttributeNS(null, "y1", r*1.35);
    portLimit.setAttributeNS(null, "x2", 0);
    portLimit.setAttributeNS(null, "y2", r*1.5);
    portLimit.setAttributeNS(null, "stroke-width", 0.06*r);

    var sbLimit = document.createElementNS(SVG_NS, 'line');
    g.appendChild(sbLimit);
    sbLimit.setAttributeNS(null, "class", "inclino-sb");
    sbLimit.setAttributeNS(null, "x1", 0);
    sbLimit.setAttributeNS(null, "y1", r*1.35);
    sbLimit.setAttributeNS(null, "x2", 0);
    sbLimit.setAttributeNS(null, "y2", r*1.5);
    sbLimit.setAttributeNS(null, "stroke-width", 0.06*r);

    parent.appendChild(g);
    return [ball, portLimit, sbLimit];
};
function createCompass(parent, r)
{
    var compass = document.createElementNS(SVG_NS, 'g');
    compass.setAttributeNS(null, "class", "compass");

    var rulerR = r*0.94;
    var scale1 = createCompassScale(1, 0, 0, 1, 0.04);
    compass.appendChild(scale1);
    scale1.setAttributeNS(null, "transform", "scale("+rulerR+")");
    scale1.setAttributeNS(null, "stroke-width", 0.2/r);

    var scale5 = createCompassScale(1, 0, 0, 5, 0.05);
    compass.appendChild(scale5);
    scale5.setAttributeNS(null, "transform", "scale("+rulerR+")");
    scale5.setAttributeNS(null, "stroke-width", 0.3/r);

    var scale10 = createCompassScale(1, 0, 0, 10, 0.06);
    compass.appendChild(scale10);
    scale10.setAttributeNS(null, "transform", "scale("+rulerR+")");
    scale10.setAttributeNS(null, "stroke-width", 0.3/r);

    var scale90 = createCompassScale(1.15, 0, 0, 90, 0.1);
    compass.appendChild(scale90);
    scale90.setAttributeNS(null, "transform", "scale("+rulerR+")");
    scale90.setAttributeNS(null, "stroke-width", 0.3/r);

    var scale = createCircleScale(1.01, 0.06);
    compass.appendChild(scale);
    scale.setAttributeNS(null, "transform", "scale("+r+")");
    scale.setAttributeNS(null, "class", "compass-scale");
    scale.setAttributeNS(null, "fill", "currentColor");
    /*
    if (haveNSWE)
    {
        var nswe = createNSWE(1.2, 0.1);
        compass.appendChild(nswe);
        nswe.setAttributeNS(null, "transform", "scale("+r+")");
        nswe.setAttributeNS(null, "font-size", "0.01em");
    }*/
    parent.appendChild(compass);
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
    return createCircleScale2(r, 0, 0, 0, 360, length, );
}
function createCircleScale2(r, cx, cy, start, end, length, conv)
{
    var g = document.createElementNS(SVG_NS, 'g');
    
    for (var a = start; a < end; a += 10)
    {
        var text = document.createElementNS(SVG_NS, 'text');
        g.appendChild(text);
        var l;
        if (conv)
        {
            l = conv(a);
        }
        else
        {
            l = a.toString();
        }
        var aTxt = document.createTextNode(l);
        text.appendChild(aTxt);
        text.setAttributeNS(null, "text-anchor", "middle");
        if (a > 90 && a < 270)
        {
            var b = a+180;
            var tick = r+length;
            text.setAttributeNS(null, "transform", "translate("+cx+", "+cy+") rotate(" + b + ") translate(0, "+tick+")");
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
function createBoat(parent, r)
{
    var boatGroup = document.createElementNS(SVG_NS, 'g');
    var g = document.createElementNS(SVG_NS, 'g');
    boatGroup.appendChild(g);
    g.setAttributeNS(null, "transform", `translate(0,-4) scale(${r/20})`);
    boatGroup.setAttributeNS(null, "transform", "rotate(0)");

    var boat = document.createElementNS(SVG_NS, 'path');
    g.appendChild(boat);
    boat.setAttributeNS(null, "class", "boat");
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
    parent.appendChild(boatGroup);
    return boatGroup;
};
function createRudder(parent, r)
{
    var rudderGroup = document.createElementNS(SVG_NS, 'g');
    var g = document.createElementNS(SVG_NS, 'g');
    rudderGroup.appendChild(g);
    g.setAttributeNS(null, "transform", `translate(0,25) scale(${r/50})`);
    var rudder = document.createElementNS(SVG_NS, 'path');
    g.appendChild(rudder);
    rudder.setAttributeNS(null, "stroke-width", "0.1");
    rudder.setAttributeNS(null, "transform", "rotate(0)");
    rudder.setAttributeNS(null, "d", "M 0 -5 C -2 -5 -5 -5 -5 15 C -5 20 -3 35 0 50 C 3 35 5 20 5 15 C 5 -5 2 -5 0 -5");
    parent.appendChild(rudderGroup);
    return rudderGroup;
}
function createTriangle(parent)
{
    var defs = document.createElementNS(SVG_NS, 'defs');
    parent.appendChild(defs);
    var marker = document.createElementNS(SVG_NS, 'marker');
    defs.appendChild(marker);
    marker.setAttributeNS(null, "id", "triangle");
    marker.setAttributeNS(null, "viewBox", "0 0 10 10");
    marker.setAttributeNS(null, "refX", "0");
    marker.setAttributeNS(null, "refY", "5");
    marker.setAttributeNS(null, "markerUnits", "strokeWidth");
    marker.setAttributeNS(null, "markerWidth", "4");
    marker.setAttributeNS(null, "markerHeight", "3");
    marker.setAttributeNS(null, "orient", "auto");
    var path = document.createElementNS(SVG_NS, 'path');
    marker.appendChild(path);
    path.setAttributeNS(null, "d", "M 0 0 L 10 5 L 0 10 z");
}
function createCOG(parent, r)
{
    var cog = document.createElementNS(SVG_NS, 'g');
    var g = document.createElementNS(SVG_NS, 'g');
    cog.appendChild(g);
    //g.setAttributeNS(null, "transform", "scale("+r/50+")");

    var arrow = document.createElementNS(SVG_NS, 'path');
    g.appendChild(arrow);
    arrow.setAttributeNS(null, "class", "cog");
    arrow.setAttributeNS(null, "stroke-width", "1");
    arrow.setAttributeNS(null, "fill", "none");
    arrow.setAttributeNS(null, "marker-end", "url(#triangle)");
    arrow.setAttributeNS(null, "d", 
            "M 0 0 L 0 "+-r*0.7
    );
    parent.appendChild(cog);
    return cog;
};

function createWindIndicator(parent, r)
{
    var windIndicatorGroup = document.createElementNS(SVG_NS, 'g');
    var g = document.createElementNS(SVG_NS, 'g');
    windIndicatorGroup.appendChild(g);
    g.setAttributeNS(null, "transform", `scale(${r/50})`);
    windIndicatorGroup.setAttributeNS(null, "transform", "rotate(0)");

    var windIndicatorTip = document.createElementNS(SVG_NS, 'path');
    g.appendChild(windIndicatorTip);
    windIndicatorTip.setAttributeNS(null, "class", "windIndicatorTip");
    windIndicatorTip.setAttributeNS(null, "stroke-width", "1");
    windIndicatorTip.setAttributeNS(null, "d", "M -3 -15 L 0 -25 L 3 -15 Z");

    var windIndicatorShaft = document.createElementNS(SVG_NS, 'path');
    g.appendChild(windIndicatorShaft);
    windIndicatorShaft.setAttributeNS(null, "class", "windIndicatorShaft");
    windIndicatorShaft.setAttributeNS(null,"stroke-width", "1");
    windIndicatorShaft.setAttributeNS(null,"fill", "none");
    windIndicatorShaft.setAttributeNS(null,"d", "M 0 15 l 0 -30");

    var windIndicatorTail = document.createElementNS(SVG_NS, 'path');
    g.appendChild(windIndicatorTail);
    windIndicatorTail.setAttributeNS(null,"class", "windIndicatorTail");
    windIndicatorTail.setAttributeNS(null,"stroke-width", "1");
    windIndicatorTail.setAttributeNS(null,"d", "M -3 25 L -3 20 L 0 15 L 3 20 L 3 25 Z");
    
    parent.appendChild(windIndicatorGroup);
    return windIndicatorGroup;

}
function createWindArrow(parent, r)
{
    var windArrow = document.createElementNS(SVG_NS, 'g');
    var g = document.createElementNS(SVG_NS, 'g');
    windArrow.appendChild(g);
    g.setAttributeNS(null, "transform", `scale(${r/120}) translate(0, -63)`);
    var windArrowPath = document.createElementNS(SVG_NS, 'path');
    g.appendChild(windArrowPath);
    windArrowPath.setAttributeNS(null,"id", "windArrow");
    windArrowPath.setAttributeNS(null,"stroke-width", "1");
    parent.appendChild(windArrow);
    return [windArrow,windArrowPath];

}