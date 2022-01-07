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
/* global Gauge */

function Gauge(element, seq)
{
    this.event = seq;
    this.property = element.getAttribute("data-property");
    switch (this.property)
    {
        case "tacktical":
            this.svg = new Svg(-50,-50,100,100);
            this.svg.tacktical(45);
            this.request = {event : this.event, property : ["speedOverGround", "trueHeading", "trueWindSpeed", "trueWindAngle", "relativeWindAngle", "trackMadeGood", "magneticVariation"]};
            this.call = function(data)
            {
                var json = JSON.parse(data);
                var name = json["name"];
                var value = json["value"];
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
            };
            break;
        default:
            this.svg = new Svg(0,0,100,40);
            this.svg.setFrame();
            this.request = {event : this.event, property : this.property};
            this.call = function(data)
            {
                var json = JSON.parse(data);
                if (json['time'] && json['value'])
                {
                    this.svg.setData(json['time'], json['value']);
                }
                else
                {
                    this.svg.setTitle(json['title']);
                    this.svg.setUnit(json['unit']);
                    this.svg.setHistory(json['history'], json['min'], json['max']);
                }
            };
            break;
    }
    element.appendChild(this.svg.svg);
    
}

