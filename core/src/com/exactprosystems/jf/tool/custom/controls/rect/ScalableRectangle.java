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
import javafx.scene.Group;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.awt.*;
import java.util.Objects;
import java.util.stream.Stream;

public class ScalableRectangle  extends Rectangle
{
    private static final long serialVersionUID = -1717871114695561335L;

    private MarkerStyle state;
	private Rectangle rectangle;
	private double scale;

	private Line top;
	private Line left;
	private Line right;
	private Line bot;
	private Text text;
	private Timeline timeline;

	public ScalableRectangle(Rectangle rectangle, double scaleFactor)
	{
		this();
		updateRectangle(rectangle, scaleFactor);
	}

	public ScalableRectangle()
	{
	    this.state = MarkerStyle.ADD;
        this.rectangle  = new Rectangle();
        this.top        = new Line();
        this.left       = new Line();
        this.right      = new Line();
        this.bot        = new Line();
        this.text       = new Text();
        this.timeline   = new Timeline();
	}


	public void update(double scaleFactor)
	{
		updateRectangle(this.rectangle, scaleFactor);
	}

	public void updateRectangle(Rectangle rectangle, double scaleFactor)
	{
		double x = rectangle.getX() * scaleFactor;
		double y = rectangle.getY() * scaleFactor;
		double w = rectangle.getWidth() * scaleFactor;
		double h = rectangle.getHeight() * scaleFactor;
		this.updateRectangle(x, y, w, h);
	}

	public void updateRectangle(double x, double y, double w, double h)
	{
		this.rectangle.setRect(x, y, w, h);
		
		this.top.setStartX(x);
		this.top.setStartY(y);
		this.top.setEndX(x + w);
		this.top.setEndY(y);

		this.left.setStartX(x);
		this.left.setStartY(y);
		this.left.setEndX(x);
		this.left.setEndY(y + h);

		this.right.setStartX(x + w);
		this.right.setStartY(y);
		this.right.setEndX(x + w);
		this.right.setEndY(y + h);

		this.bot.setStartX(x);
		this.bot.setStartY(y + h);
		this.bot.setEndX(x + w);
		this.bot.setEndY(y + h);
	}

	public Rectangle getRectangle()
	{
		return this.rectangle;
	}

	public void setGroup(Group group)
	{
		group.getChildren().addAll(top, right, bot, left, this.text);
	}

	public void removeGroup(Group group)
	{
		group.getChildren().removeAll(top, right, bot, left, this.text);
	}

	public void setText(String text)
	{
		this.text.setText(text);
        this.text.setFill(this.state.color());
	}

	public void setTextVisible(boolean flag)
	{
		this.text.setVisible(flag);
		this.text.setX(this.top.getStartX() + 5);
		this.text.setY(this.top.getStartY() + this.text.getFont().getSize());
		if (flag)
		{
			createTransition();
			this.timeline.play();
		}
	}

	public void setVisible(boolean flag)
	{
		Stream.of(top, left, bot, right).forEach(line -> line.setVisible(flag));
	}

	public boolean isVisible()
	{
	    return Stream.of(top, left, bot, right).allMatch(Line::isVisible) && this.text.isVisible();
	}

	public void setWidthLine(double width)
	{
	    Stream.of(top, left, bot, right).forEach(l -> l.setStrokeWidth(width));
	}

	public void addStyleClass(String styleClass)
	{
        Stream.of(top, left, bot, right).forEach(l -> l.getStyleClass().add(styleClass));
	}

	public void setFill(Color color)
	{
        Stream.of(top, left, bot, right).forEach(l -> l.setStroke(color));
	}

	public void setOpacity(double value)
	{
		Stream.of(top, left, bot, right, this.text).forEach(l -> l.setOpacity(value));
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		return Objects.equals(this.rectangle, ((ScalableRectangle)o).rectangle);
	}

	@Override
	public int hashCode()
	{
		return this.rectangle.hashCode();
	}

	//============================================================
	// private methods
	//============================================================
	private void createTransition()
	{
		this.timeline.stop();
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
