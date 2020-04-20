/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nmea.viewer;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FunctionalInvalidationListener implements InvalidationListener, Runnable
{
    private Runnable runner;
    private boolean valid = true;
    private BooleanProperty predicate;

    public FunctionalInvalidationListener(Runnable runner)
    {
        this.runner = runner;
    }
    
    public void bind(Observable... observables)
    {
        for (Observable observable : observables)
        {
            observable.addListener(this);
        }
    }
    /**
     * If predicate is set it will prevent action if it is false.
     * @param predicate 
     */
    public void setPredicate(BooleanProperty predicate)
    {
        this.predicate = predicate;
        bind(predicate);
    }
    @Override
    public void invalidated(Observable observable)
    {
        if (predicate == null || predicate.get())
        {
            if (valid)
            {
                valid = false;
                Platform.runLater(this);
            }
        }
    }

    @Override
    public void run()
    {
        try
        {
            runner.run();
        }
        finally
        {
            valid = true;
        }
    }
    
}
