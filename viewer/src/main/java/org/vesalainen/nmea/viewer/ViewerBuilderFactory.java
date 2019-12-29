/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import javafx.fxml.JavaFXBuilderFactory;
import javafx.util.Builder;
import javafx.util.BuilderFactory;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ViewerBuilderFactory implements BuilderFactory
{
    private BuilderFactory defaultFactory = new JavaFXBuilderFactory();
    private ViewerDataSource viewerDataSource;

    public ViewerBuilderFactory(ViewerDataSource viewerDataSource)
    {
        this.viewerDataSource = viewerDataSource;
    }
    
    @Override
    public Builder<?> getBuilder(Class<?> type)
    {
        return new ViewerBuilder(type);
    }
    
    private class ViewerBuilder implements Builder
    {
        private Builder builder;

        public ViewerBuilder(Class<?> type)
        {
            this.builder = defaultFactory.getBuilder(type);
        }
        
        @Override
        public Object build()
        {
            Object object = builder.build();
            if (object instanceof ViewerDataSourceConsumer)
            {
                ViewerDataSourceConsumer vdsc = (ViewerDataSourceConsumer) object;
                vdsc.accept(viewerDataSource);
            }
            return object;
        }
    }    
}
