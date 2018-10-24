/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.controls.rect;

import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

public class DecoratedRectangle extends Rectangle
{
	private final MarkerStyle        style;
	private final Text               text;
	private final Timeline           timeline;
	private final java.awt.Rectangle baseRectangle;

	public DecoratedRectangle(java.awt.Rectangle rectangle, MarkerStyle style, String text)
	{
		super(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
		this.baseRectangle = rectangle;
		this.style = style;
		this.text = new Text();
		this.timeline = new Timeline();

		super.setStrokeType(StrokeType.OUTSIDE);
		super.setFill(Color.TRANSPARENT);
		super.setStrokeWidth(4);

		if (this.style != null)
		{
			if (this.style.color() == null)
			{
				super.getStyleClass().setAll(this.style.getCssStyle());
			}
			else
			{
				super.setStroke(this.style.color());
				this.text.setFill(this.style.color());
			}
		}

		this.text.setText(text);
		this.text.setX(super.getX() + 5);
		this.text.setY(super.getY() + this.text.getFont().getSize());

		this.createTransition();
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
				&& this.baseRectangle.getX() == rectangle.getX()
				&& this.baseRectangle.getY() == rectangle.getY()
				&& this.baseRectangle.getWidth() == rectangle.getWidth()
				&& this.baseRectangle.getHeight() == rectangle.getHeight();
	}

	public boolean matches(java.awt.Rectangle rectangle, Color color)
	{
		return super.getStroke() == color
				&& this.baseRectangle.getX() == rectangle.getX()
				&& this.baseRectangle.getY() == rectangle.getY()
				&& this.baseRectangle.getWidth() == rectangle.getWidth()
				&& this.baseRectangle.getHeight() == rectangle.getHeight();
	}

	public java.awt.Rectangle awtRectangle()
	{
		return this.baseRectangle;
	}

	public MarkerStyle getMarkerStyle()
	{
		return this.style;
	}

	//region private methods
	private void createTransition()
	{
		ColorAdjust color = new ColorAdjust();
		color.setBrightness(0.0);

		this.text.setEffect(color);
		this.timeline.getKeyFrames().clear();
		this.timeline.getKeyFrames().add(new KeyFrame(javafx.util.Duration.seconds(0), new KeyValue(color.brightnessProperty(), color.brightnessProperty().getValue(), Interpolator.LINEAR)));
		this.timeline.getKeyFrames().add(new KeyFrame(javafx.util.Duration.seconds(1), new KeyValue(color.brightnessProperty(), -1, Interpolator.LINEAR)));
		timeline.setAutoReverse(true);
		timeline.setCycleCount(-1);
	}

	//endregion
}
