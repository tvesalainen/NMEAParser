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
/* global refresh, eventSource zoneOffset timeOffset i18n */

"use strict";

var zoneOffset=0;
var timeOffset=0;

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
        prefs("zoneOffset", this.svg, function(th, res)
        {
            if (res)
            {
                zoneOffset=parseInt(res);
            }
            eventSource.addEventListener("timeOffset", setTimeOffset, false);
            var targets = document.getElementsByClassName('gauge');
            for (var i=0;i<targets.length;i++)
            {
                var target = targets[i];
                var g = new Gauge(target, i);
                var event = g.event;
                gauge[event] = g;
                if (g.request)
                {
                    eventSource.addEventListener(event, fired, false);
                    $.post("/sse", g.request);
                }
            }
            eventSource.addEventListener("dayPhase", dayPhase, false);
            $.post("/sse", {event: "dayPhase", property: "dayPhase"});
        });
    };
    function dayPhase(event)
    {
        var json = JSON.parse(event.data);
        var l = document.body.classList;
        l.remove("DAY");
        l.remove("NIGHT");
        l.remove("TWILIGHT");
        l.add(json["value"]);
    };
    function fired(event)
    {
        var g = gauge[event.type];
        g.call(event.data);
        g.activate();
    };
    function tick()
    {
        for (var g in gauge)
        {
            if (gauge[g].tick)
            {
                gauge[g].tick();
            }
        }
    };
    
    setInterval(tick, 1000);
    
    function setTimeOffset(event)
    {
        timeOffset = Number(event.data);
    };

});

