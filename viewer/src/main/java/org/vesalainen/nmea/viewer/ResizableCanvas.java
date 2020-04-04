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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.binding.When;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleablePropertyFactory;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ResizableCanvas extends Canvas implements Initializable
{
    private static final StyleablePropertyFactory<ResizableCanvas> FACTORY = 
            new StyleablePropertyFactory<>(Canvas.getClassCssMetaData());
    private static final CssMetaData<ResizableCanvas,Font> FONT = FACTORY.createFontCssMetaData("-fx-font", s->s.font);
    private static final CssMetaData<ResizableCanvas,Paint> TEXT_FILL = FACTORY.createPaintCssMetaData("-fx-text-fill", s->s.textFill, Color.BLACK, true);
    private static final CssMetaData<ResizableCanvas,Paint> BACKGROUND = FACTORY.createPaintCssMetaData("-fx-background", s->s.background, Color.WHITE, true);
    
    private final SimpleStyleableObjectProperty<Font> font = new SimpleStyleableObjectProperty<>(FONT, this, "font");

    public Font getFont()
    {
        return font.get();
    }

    public void setFont(Font value)
    {
        font.set(value);
    }

    public ObjectProperty fontProperty()
    {
        return font;
    }
    private final SimpleStyleableObjectProperty<Paint> textFill = new SimpleStyleableObjectProperty<>(TEXT_FILL, this, "textFill");

    public Paint getTextFill()
    {
        return textFill.get();
    }

    public void setTextFill(Paint value)
    {
        textFill.set(value);
    }

    public ObjectProperty textFillProperty()
    {
        return textFill;
    }
    private final SimpleStyleableObjectProperty<Paint> background = new SimpleStyleableObjectProperty<>(BACKGROUND, this, "background");

    public Paint getBackground()
    {
        return background.get();
    }

    public void setBackground(Paint value)
    {
        background.set(value);
    }

    public ObjectProperty backgroundProperty()
    {
        return background;
    }
    

    private boolean square;
    protected URL location;
    protected ResourceBundle resources;

    public ResizableCanvas()
    {
        this(false);
    }

    public ResizableCanvas(boolean square)
    {
        getStyleClass().add("resizable-canvas");
        fontProperty().addListener(evt->onDraw());
        textFillProperty().addListener(evt->onDraw());
        backgroundProperty().addListener(evt->onDraw());
        this.square = square;
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle(I18n.class.getName(), Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ResizableCanvas.fxml"), bundle);
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        parentProperty().addListener(this::setParent);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData()
    {
        return FACTORY.getCssMetaData();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData()
    {
        return FACTORY.getCssMetaData();
    }
    /**
     * Returns color whose brightness is double the current background.
     * @param origColor
     * @return 
     */
    protected Color getColor(Color origColor)
    {
        Paint bg = getBackground();
        if (bg instanceof Color)
        {
            Color bgColor = (Color) bg;
            double brightness = 0.5*bgColor.getBrightness()+0.5;
            return Color.hsb(origColor.getHue(), origColor.getSaturation(), brightness, origColor.getOpacity());
        }
        else
        {
            return origColor;
        }
    }
    @Override
    public double prefHeight(double width)
    {
        return getHeight();
    }

    @Override
    public double prefWidth(double height)
    {
        return getWidth();
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    protected void onDraw()
    {
    }

    @Override
    public final void initialize(URL location, ResourceBundle resources)
    {
        this.location = location; 
        this.resources = resources; 
    }
    private void setParent(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newParent)
    {
        if (newParent instanceof Region)
        {
            Region region = (Region) newParent;
            ReadOnlyDoubleProperty regionWidth = region.widthProperty();
            ReadOnlyDoubleProperty regionHeight = region.heightProperty();
            
            if (square)
            {
                widthProperty()
                        .bind(new When(regionWidth.lessThanOrEqualTo(regionHeight))
                                                .then(regionWidth)
                                                .otherwise(regionHeight));
                heightProperty()
                        .bind(new When(regionHeight.lessThanOrEqualTo(regionWidth))
                                                .then(regionHeight)
                                                .otherwise(regionWidth));
            }
            else
            {
                widthProperty().bind(regionWidth);
                heightProperty().bind(regionHeight);
            }
            widthProperty().addListener(evt -> onDraw());
            heightProperty().addListener(evt -> onDraw());
        }
        else
        {
            System.err.println(newParent+" not suitable for ResizableCanvas");
        }
    }
}
