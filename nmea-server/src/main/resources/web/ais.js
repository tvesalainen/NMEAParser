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

/* global serverMillis, zoneOffset AisList */

function AisList()
{
    this.table = document.createElement('table');
    this.table.appendChild(createHeader());

    this.set = function(json)
    {
        var mmsi = json["mmsi"];
        if (mmsi)
        {
            var id = "row-"+mmsi;
            var row = document.getElementById(id);
            if (!row)
            {
                row = createRow(mmsi);
                this.table.appendChild(row);
            }
            var cols = row.getElementsByTagName("td");
            for (let i=0;i<cols.length;i++)
            {
                var col = cols[i];
                var name = col.getAttribute("data-name");
                var value = json[name];
                if (value)
                {
                    col.innerHTML = value;
                }
            }
        }
    };
}

function createRow(mmsi)
{
    var row = document.createElement('tr');
    row.id = "row-"+mmsi;
    addColumn(row, "mmsi");
    addColumn(row, "name");
    addColumn(row, "country");
    return row;
}
function createHeader()
{
    var hdr = document.createElement('tr');
    addHeader(hdr, "MMSI");
    addHeader(hdr, "Name");
    addHeader(hdr, "Country");
    return hdr;
}
function addColumn(row, name)
{
    var td = document.createElement('td');
    td.setAttribute("data-name", name);
    row.appendChild(td);
}
function addHeader(hdr, title)
{
    var th = document.createElement('th');
    hdr.appendChild(th);
    th.innerHTML = title;
}
