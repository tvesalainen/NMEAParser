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

/* global Gauge zoneOffset */

var zoneOffset=0;

function Gauge(element, seq)
{
    this.event = seq;
    this.property = element.getAttribute("data-property");
    this.div = document.createElement("div");
    element.appendChild(this.div);
     switch (this.property)
    {
        case "message":
            this.svg = new Svg(-50,40,100,40);
            this.svg.setFrame();
            this.request = {event : this.event, property : this.property};
            this.svg.svg.setAttributeNS(null, "class", "active");
            this.call = function(data)
            {
                var json = JSON.parse(data);
                var name = json["name"];
                var msg = json["value"];
                if (msg)
                {
                    var mid = msg["id"];
                    var msg = msg["msg"];
                }
            };
            this.tick = function()
            {
            };
            break;
        case "localDateTime":
            this.svg = new Svg(-50,40,100,40);
            this.svg.setFrame();
            this.svg.svg.setAttributeNS(null, "class", "active");
            prefs("zoneOffset", this.svg, function(th, res)
            {
                if (res)
                {
                    zoneOffset=parseInt(res);
                }
            });
            this.tick = function()
            {
                var d = new Date(getServerTime()+zoneOffset);
                var is = d.toISOString();
                this.svg.setText1(is.substr(0, 10));
                this.svg.setText2(is.substr(11, 8));
            };
            break;
        case "utcDateTime":
            this.svg = new Svg(-50,40,100,40);
            this.svg.setFrame();
            this.svg.svg.setAttributeNS(null, "class", "active");
            this.tick = function()
            {
                var d = new Date(getServerTime());
                var is = d.toISOString();
                this.svg.setText1(is.substr(0, 10));
                this.svg.setText2(is.substr(11, 8));
            };
            break;
        case "tworow":
            this.property1 = element.getAttribute("data-property1");
            this.property2 = element.getAttribute("data-property2");
            this.svg = new Svg(-50,40,100,40);
            this.svg.setFrame();
            this.request = {event : this.event, property : [this.property1, this.property2]};
            this.call = function(data)
            {
                var json = JSON.parse(data);
                var name = json["name"];
                var value = json["value"];
                if (value)
                {
                    switch (name)
                    {
                        case this.property1:
                            this.svg.setText1(value);
                            break;
                        case this.property2:
                            this.svg.setText2(value);
                            break;
                    }
                }
            };
            this.tick = function()
            {
                this.passivate();
            };
            break;
        case "rudder":
            this.svg = new Svg(-50,40,100,40);
            this.svg.rudder(45);
            this.svg.setFrame();
            this.request = {event : this.event, property : "rudderSensor"};
            this.call = function(data)
            {
                var json = JSON.parse(data);
                var name = json["name"];
                var value = json["value"];
                if (value)
                {
                    switch (name)
                    {
                        case "rudderSensor":
                            this.svg.setRudder(value);
                            break;
                    }
                }
            };
            this.tick = function()
            {
                this.passivate();
            };
            break;
        case "inclino":
            this.svg = new Svg(-50,40,100,40);
            this.svg.inclino(45);
            this.svg.setFrame();
            this.request = {event : this.event, property : "roll"};
            this.call = function(data)
            {
                var json = JSON.parse(data);
                var name = json["name"];
                var value = json["value"];
                if (value)
                {
                    switch (name)
                    {
                        case "roll":
                            this.svg.setRoll(value);
                            break;
                    }
                }
            };
            this.tick = function()
            {
                this.passivate();
            };
            break;
        case "tacktical":
            this.svg = new Svg(-50,-50,100,100);
            this.svg.tacktical(45);
            this.request = {event : this.event, property : ["speedOverGround", "trueHeading", "trueWindSpeed", "trueWindAngle", "relativeWindAngle", "trackMadeGood", "magneticVariation"]};
            this.call = function(data)
            {
                var json = JSON.parse(data);
                var name = json["name"];
                var value = json["value"];
                if (value)
                {
                    switch (name)
                    {
                        case "trueHeading":
                            this.svg.setTrueHeading(value);
                            break;
                        case "relativeWindAngle":
                            this.svg.setRelativeWindAngle(value);
                            break;
                        case "trueWindAngle":
                            this.svg.setTrueWindAngle(value);
                            break;
                        case "trueWindSpeed":
                            this.svg.setTrueWindSpeed(value);
                            break;
                        case "trackMadeGood":
                            this.svg.setCOG(value);
                            break;
                        case "speedOverGround":
                            this.svg.setSpeedOverGround(value);
                            break;
                        case "magneticVariation":
                            this.svg.setVariation(value);
                            break;
                    }
                }
            };
            this.tick = function()
            {
                this.passivate();
            };
            break;
        case "estimatedTimeOfArrival":
            this.svg = new Svg(0,0,100,40);
            this.svg.setFrame();
            this.svg.eta();
            this.request = {event : this.event, property : ["estimatedTimeOfArrival", "toWaypoint"]};
            this.call = function(data)
            {
                var json = JSON.parse(data);
                if (json['historyData'])
                {
                    this.svg.setHistoryData(json['historyData']);
                }
                else
                {
                    var name = json["name"];
                    if (json['time'] && json['value'])
                    {
                        switch (name)
                        {
                            case "estimatedTimeOfArrival":
                                this.svg.setEta(json['time'], json['value']);
                                break;
                            case "toWaypoint":
                                this.svg.resetEta(json['time'], json['value']);
                                break;
                        }
                    }
                    if (json['title'] && name === "estimatedTimeOfArrival")
                    {
                        this.svg.setTitle(json['title'], json['unit']);
                    }
                }
            };
            this.tick = function()
            {
                this.passivate();
            };
            break;
        default:
            this.svg = new Svg(0,0,100,40);
            this.svg.setFrame();
            this.request = {event : this.event, property : this.property};
            this.call = function(data)
            {
                var json = JSON.parse(data);
                if (json['historyData'])
                {
                    this.svg.setHistoryData(json['historyData']);
                }
                else
                {
                    if (json['time'] && json['value'])
                    {
                        this.svg.setData(json['time'], json['value']);
                    }
                    if (json['title'])
                    {
                        this.svg.setTitle(json['title']);
                        this.svg.setUnit(json['unit']);
                        this.svg.setHistory(json['history'], json['min'], json['max']);
                    }
                }
            };
            this.tick = function()
            {
                this.svg.tick();
                this.passivate();
            };
            break;
    }
    var title = element.getAttribute("data-title");
    if (title)
    {
        i18n(title, this.svg, function(th, res)
        {
            th.setTitle(res);
        });
    }
    var unit = element.getAttribute("data-unit");
    if (unit)
    {
        i18n(unit, this.svg, function(th, res)
        {
            th.setUnit(res);
        });
    }
    this.div.appendChild(this.svg.svg);
    this.refreshTime = 0;
    this.activate = function()
    {
        var now = new Date();
        this.refreshTime = now.getTime();
        if (!this.status || this.status !== "active")
        {
            this.svg.svg.setAttributeNS(null, 'class', 'active');
            this.status = "active";
        }
    };
    this.passivate = function()
    {
        var now = new Date();
        if (now.getTime()-this.refreshTime > 2000)
        {
            if (!this.status || this.status === "active")
            {
                this.svg.svg.setAttributeNS(null, 'class', 'passive');
                this.status = "passive";
            }
        }
    };
    
}
async function i18n(key, th, res)
{
    let x = await fetch("/i18n?"+key);
    let y = await x.text();
    let json = JSON.parse(y);
    res(th, json[key]);
}
async function prefs(key, th, res)
{
    let x = await fetch("/prefs?"+key);
    let y = await x.text();
    let json = JSON.parse(y);
    res(th, json[key]);
}

function changeZoneOffset(th)
{
    var zo = prompt("Enter Local Zone Offset", "+/-hh:mm");
    var i = zo.indexOf(":");
    if (i !== -1)
    {
        var h = parseInt(zo.substring(0, i));
        var m = parseInt(zo.substring(i+1));
        var o = (60*h+m)*60000;
    }
    zoneOffset = o;
    $.post("/prefs", {zoneOffset: o});
}