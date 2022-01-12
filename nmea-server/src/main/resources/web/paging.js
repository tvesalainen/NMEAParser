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

/* global */

var nextHref = "";
var prevHref = "";

$(document).ready(function () 
{
    var links = document.getElementsByTagName("link");
    for (var i=0;i<links.length;i++)
    {
        var link = links[i];
        var rel = link.rel;
        if (rel === 'next')
        {
            nextHref = link.href;
        }
        if (rel === 'prev')
        {
            prevHref = link.href;
        }
    }
    var x = 0;
    var y = 0;
    document.addEventListener('pointerdown', function(event)
    {
        x = event.screenX;
        y = event.screenY;
    });
    document.addEventListener('pointerup', function(event)
    {
        var dx = event.screenX - x;
        var dy = event.screenY - y;
        if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 10)
        {
            if (dx < 0)
            {
                if (nextHref)
                {
                    location.href = nextHref;
                }
            }
            else
            {
                if (prevHref)
                {
                    location.href = prevHref;
                }
            }
        }
    });
});



