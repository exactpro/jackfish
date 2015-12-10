////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.layout;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

public class CustomRectangle
{
	public enum LinePosition
	{
		TOP,
		RIGHT,
		BOT,
		LEFT
	}

	public static final Color DEFAULT_COLOR = Color.BLACK;

	public Object data;

	private Line top;
	private Line left;
	private Line right;
	private Line bot;

	public CustomRectangle(Rectangle rectangle)
	{
		this();
		updateRectangle(rectangle);
	}

	public CustomRectangle()
	{

		this.top = new Line();
		this.left = new Line();
		this.right = new Line();
		this.bot = new Line();
	}

	public void setUserData(Object data)
	{
		this.data = data;
	}

	public Object getUserData()
	{
		return this.data;
	}

	public void updateRectangle(Rectangle rectangle)
	{
		double x = rectangle.getX();
		double y = rectangle.getY();
		double w = rectangle.getWidth();
		double h = rectangle.getHeight();
		this.updateRectangle(x, y, w, h);
	}

	public void updateRectangle(double x, double y, double w, double h)
	{
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

	public void setLineStrokeCap(StrokeLineCap lineCap, LinePosition position)
	{
		getLineByPosition(position).ifPresent(line -> line.setStrokeLineCap(lineCap));
	}

	public Rectangle getRectangle()
	{
		return new Rectangle(((int) top.getStartX()), ((int) top.getStartY()), ((int) bot.getEndX()), ((int) bot.getEndY()));
	}

	public void setGroup(Group group)
	{
		group.getChildren().addAll(top, right, bot, left);
	}

	public void setVisible(boolean flag)
	{
		Arrays.asList(top, left, bot, right).stream().forEach(line -> line.setVisible(flag));
	}

	public void setColor(Color color)
	{
		this.setColor(color, color, color, color);
	}

	public void setColor(Color top, Color right, Color bot, Color left)
	{
		this.top.setStroke(top);
		this.right.setStroke(right);
		this.bot.setStroke(bot);
		this.left.setStroke(left);
	}

	public void setColor(Color color, LinePosition position)
	{
		getLineByPosition(position).ifPresent(line -> line.setStroke(color));
	}

	public void setWidthLine(double width)
	{
		this.setWidthLine(width, width, width, width);
	}

	public void setWidthLine(double top, double right, double bot, double left)
	{
		this.top.setStrokeWidth(top);
		this.right.setStrokeWidth(right);
		this.bot.setStrokeWidth(bot);
		this.left.setStrokeWidth(left);
	}

	public void setWidthLine(double width, LinePosition position)
	{
		getLineByPosition(position).ifPresent(line -> line.setStrokeWidth(width));
	}

	public void addStyleClass(String styleClass)
	{
		this.addStyleClass(styleClass, styleClass, styleClass, styleClass);
	}

	public void addStyleClass(String styleClassTop, String styleClassRight, String styleClassBot, String styleClassLeft)
	{
		this.top.getStyleClass().add(styleClassTop);
		this.right.getStyleClass().add(styleClassRight);
		this.bot.getStyleClass().add(styleClassBot);
		this.left.getStyleClass().add(styleClassLeft);
	}

	public void addStyleClass(String styleClass, LinePosition position)
	{
		this.getLineByPosition(position).ifPresent(line -> line.getStyleClass().add(styleClass));
	}

	//============================================================
	// events methods
	//============================================================
	public void setOnMouseExited(EventHandler<MouseEvent> handler, LinePosition position)
	{
		getLineByPosition(position).ifPresent(line -> line.setOnMouseExited(handler));
	}

	public void setOnMouseEntered(EventHandler<MouseEvent> handler, LinePosition position)
	{
		getLineByPosition(position).ifPresent(line -> line.setOnMouseEntered(handler));
	}

	public void setOnMouseClick(EventHandler<MouseEvent> handler, LinePosition position)
	{
		getLineByPosition(position).ifPresent(line -> line.setOnMouseClicked(handler));
	}

	//============================================================
	// private methods
	//============================================================
	private Optional<Line> getLineByPosition(LinePosition position)
	{
		switch (position)
		{
			case TOP:
				return Optional.of(this.top);
			case RIGHT:
				return Optional.of(this.right);
			case BOT:
				return Optional.of(this.bot);
			case LEFT:
				return Optional.of(this.left);
		}
		return Optional.empty();
	}
}
