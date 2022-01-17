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

/* global Gauge */

function Gauge(element, seq)
{
    this.event = seq;
    this.property = element.getAttribute("data-property");
    switch (this.property)
    {
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
                        this.svg.setTitle(json['title'], json['unit']);
                        this.svg.setHistory(json['history'], json['min'], json['max']);
                    }
                }
            };
            break;
    }
    element.appendChild(this.svg.svg);
    
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

