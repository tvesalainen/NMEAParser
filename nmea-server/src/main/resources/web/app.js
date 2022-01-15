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
/* global refresh, eventSource */

"use strict";

var gauge = {};

$(document).ready(function () {
    
    if (eventSource)
    {
        eventSource.close();
    }
    var eventSource = new EventSource("/sse");

    eventSource.onerror = function(err)
    {
        console.log("EventSource failed:", err);
        eventSource.close();
    };
    eventSource.onopen = function()
    {
        var targets = document.getElementsByClassName('gauge');
        for (var i=0;i<targets.length;i++)
        {
            var target = targets[i];
            var g = new Gauge(target, i);
            var event = g.event;
            gauge[event] = g;
            eventSource.addEventListener(event, fired, false);
            $.post("/sse", g.request);
        }
    };
    function fired(event)
    {
        var g = gauge[event.type];
        g.call(event.data);
        g.activate();
    };
    function passivate()
    {
        for (var g in gauge)
        {
            gauge[g].passivate();
        }
    };
    
    setInterval(refresh, 1000);
    setInterval(passivate, 2000);

});

