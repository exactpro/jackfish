////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.rect;

import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;

import javafx.animation.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

public class DecoragedRectangle  extends Rectangle
{
    private MarkerStyle        style;
    private Text               text;
    private Timeline           timeline;
    private java.awt.Rectangle baseRectangle;

	public DecoragedRectangle(java.awt.Rectangle rectangle, MarkerStyle style, String text)
	{
		super(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
		this.baseRectangle = rectangle;
		
	    this.style      = style == null ? MarkerStyle.SELECT : style; 
        this.text       = new Text();
        this.timeline   = new Timeline();

        setStrokeType(StrokeType.OUTSIDE);
        setFill(Color.TRANSPARENT);
        setStroke(this.style.color());
        getStyleClass().add(this.style.getCssStyle());
        setStrokeWidth(4);

        this.text.setText(text);
        this.text.setFill(this.style.color());
        this.text.setX(this.getX() + 5);
        this.text.setY(this.getY() + this.text.getFont().getSize());

        createTransition();
	}

	public void setTextVisible(boolean flag)
	{
		this.text.setVisible(flag);
		if (flag)
		{
			this.timeline.play();
		}
		else
		{
		    this.timeline.stop();
		}
	}

    public boolean matches(java.awt.Rectangle rectangle, MarkerStyle style)
    {
        return this.style == style 
                && getX() == rectangle.getX() 
                && getY() == rectangle.getY()
                && getWidth() == rectangle.getWidth()
                && getHeight() == rectangle.getHeight();
    }

    public java.awt.Rectangle awtRectangle()
    {
        return this.baseRectangle;
    }

    public MarkerStyle getMarkerStyle()
    {
        return this.style;
    }
    //============================================================
	// private methods
	//============================================================
	private void createTransition()
	{
		ColorAdjust color = new ColorAdjust();
        this.text.setEffect(color);
		color.setBrightness(0.0);
		this.timeline.getKeyFrames().clear();
        this.timeline.getKeyFrames().add(new KeyFrame(javafx.util.Duration.seconds(0), new KeyValue(color.brightnessProperty(), color.brightnessProperty().getValue(), Interpolator.LINEAR)));
        this.timeline.getKeyFrames().add(new KeyFrame(javafx.util.Duration.seconds(1), new KeyValue(color.brightnessProperty(), -1, Interpolator.LINEAR)));
		timeline.setAutoReverse(true);
		timeline.setCycleCount(-1);
	}
}
