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

/* global Svg, Title, OneRow */

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
    this.title.setAttributeNS(null, "x", "-45");
    this.title.setAttributeNS(null, "y", "-32");
    this.title.setAttributeNS(null, "text-anchor", "start");
    this.titleText = document.createTextNode('');
    this.title.appendChild(this.titleText);
    this.title.setAttributeNS(null, "style", "font-size: 0.5em");

    this.unit = document.createElementNS(this.svgNS, 'text');
    this.svg.appendChild(this.unit);
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
        var t = document.createTextNode(str);
        this.unit.replaceChild(t, this.unitText);
        this.unitText = t;
    };

    this.text = document.createElementNS(this.svgNS, 'text');
    this.svg.appendChild(this.text);
    this.text.setAttributeNS(null, "x", "-45");
    this.text.setAttributeNS(null, "y", "-5");
    this.text.setAttributeNS(null, "textLength", "90");
    this.text.setAttributeNS(null, "lengthAdjust", "spacingAndGlyphs");
    this.text.setAttributeNS(null, "style", "font-size: 2em");
    this.txt = document.createTextNode('');
    this.text.appendChild(this.txt);
    this.setText = function(str)
    {
        var t = document.createTextNode(str);
        this.text.replaceChild(t, this.txt);
        this.txt = t;
    };
}
