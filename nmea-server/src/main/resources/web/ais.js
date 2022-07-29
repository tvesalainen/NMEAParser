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

/* global getServerTime, zoneOffset AisList populate cat getCat openTab clearOlds */

var properties = [];
var data = {};
var cat;
var currentMMSI;

function AisList(tbody)
{
    this.table = tbody;
    var header = document.getElementById("header");
    var ths = header.getElementsByTagName("th");
    for (let i=0;i<ths.length;i++)
    {
        var th = ths[i];
        var property = th.getAttribute("data-property");
        properties.push(property);
    }

    this.set = function(json)
    {
        var mmsi = json["mmsi"];
        if (mmsi)
        {
            data[mmsi] = json;
            var row = document.getElementById(mmsi);
            if (!row)
            {
                row = createRow(mmsi, json["alpha2"]);
                this.table.appendChild(row);
            }
            var cols = row.getElementsByTagName("td");
            for (let i=0;i<cols.length;i++)
            {
                var col = cols[i];
                var name = col.className;
                var value = json[name];
                if (value)
                {
                    switch (name)
                    {
                        case "time":
                            col.innerHTML = getAge(value);
                            break;
                        default:
                            col.innerHTML = value;
                            break;
                    }
                }
            }
        }
    };
    this.tick = function()
    {
        for (let mmsi in data)
        {
            var vessel = data[mmsi];
            var time = vessel["time"];
            var row = document.getElementById(mmsi);
            if (row)
            {
                var t = row.getElementsByClassName("time");
                t[0].innerHTML = getAge(time);
            }
        }
        updateMMSI(currentMMSI);
    };
}
function populate(event)
{
    var path = event.path;
    var mmsi;
    for (let i=0;i<path.length;i++)
    {
        var e = path[i];
        if (e.localName === 'tr')
        {
            mmsi = e.id;
            break;
        }
    }
    currentMMSI = Number(mmsi);
    updateMMSI(mmsi);
    var d = data[mmsi];
    cat = d["cat"];
    return cat;
}
function updateMMSI(mmsi)
{
    var d = data[mmsi];
    var elems = document.getElementsByClassName("property");
    for (let i=0;i<elems.length;i++)
    {
        var elem = elems[i];
        var property = elem.getAttribute("data-property");
        var value = d[property];
        if (value.length > 0)
        {
            var num = Number(value);
            if (Number.isFinite(num))
            {
                value = num;
            }
        }
        if (property)
        {
            switch (property)
            {
                case "cpaDistance":
                case "distance":
                    if (typeof value === "number")
                    {
                        elem.innerHTML = value+" NM";
                        elem.removeAttribute("style");
                    }
                    else
                    {
                        elem.setAttribute("style", "display: none");
                    }
                    break;
                case "cpaMinutes":
                    if (typeof value === "number")
                    {
                        elem.innerHTML = value+" min";
                        elem.removeAttribute("style");
                    }
                    else
                    {
                        elem.setAttribute("style", "display: none");
                    }
                    break;
                case "cog":
                case "hdg":
                case "bearing":
                    if (value <= 360)
                    {
                        elem.innerHTML = value+" °";
                        elem.removeAttribute("style");
                    }
                    else
                    {
                        elem.setAttribute("style", "display: none");
                    }
                    break;
                case "rot":
                    if (typeof value === "number")
                    {
                        elem.innerHTML = value+" °/min";
                        elem.removeAttribute("style");
                    }
                    else
                    {
                        elem.setAttribute("style", "display: none");
                    }
                    break;
                case "time":
                    elem.innerHTML = getAge(value);
                    break;
                case "flag":
                    var code = d["alpha2"].toLowerCase();
                    elem.innerHTML = '<img class="detail-flag" src="flag-icons-main/flags/4x3/'+code+'.svg"></img>';
                    break;
                default:
                    elem.innerHTML = value;
                    break;
            }
        }
    }
}
function getCat()
{
    return cat;
}

function createRow(mmsi, alpha2)
{
    var row = document.createElement('tr');
    row.id = mmsi;
    row.className = "item";
    for (let i=0;i<properties.length;i++)
    {
        var name = properties[i];
        switch (name)
        {
            case "flag":
                addFlag(row, alpha2, name);
                break;
            default:
                addColumn(row, name);
                break;
        }
    }
    return row;
}
function addColumn(row, name)
{
    var td = document.createElement('td');
    td.className = name;
    row.appendChild(td);
}
function addFlag(row, alpha2, name)
{
    var code = alpha2.toLowerCase();
    var td = document.createElement('td');
    td.className = name;
    row.appendChild(td);
    var img = document.createElement('img');
    td.appendChild(img);
    img.className = "row-flag";
    img.setAttribute("src", "flag-icons-main/flags/4x3/"+code+".svg");
}
function getAge(time)
{
    var s = Math.floor((getServerTime() - time)/1000);
    if (s < 60)
    {
        return s+"s";
    }
    else
    {
        var m = Math.floor(s / 60);
        s = s % 60;
        if (m < 60)
        {
            return m+"m "+s+"s";
        }
        else
        {
            var h = Math.floor(m / 60);
            m = m % 60;
            return h+"h "+m+"m "+s+"s";
        }
    }
}
function clearOlds()
{
    var olds = [];
    for (let m in data)
    {
        var d = data[m];
        var time = d["time"];
        if (getServerTime() - time > 86400000)
        {
            olds.push(m);
        }
    }
    for (let i=0;i<olds.length;i++)
    {
        var mmsi = olds[i];
        var el = document.getElementById(mmsi);
        el.remove();
        delete data[mmsi];
    }
}