////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.tool.CssVariables;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

import java.awt.*;
import java.util.Arrays;

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
	
	private Rectangle rectangle;

	private Line top;
	private Line left;
	private Line right;
	private Line bot;

	private Line outLine;

	private boolean isInit = false;
	private boolean isVisible = false;

	public CustomRectangle(Rectangle rectangle, double scaleFactor)
	{
		this();
		updateRectangle(rectangle, scaleFactor);
	}

	public CustomRectangle()
	{
		this.rectangle = new Rectangle();

		this.top = new Line();
		this.left = new Line();
		this.right = new Line();
		this.bot = new Line();

		this.outLine = new Line();
		this.outLine.getStyleClass().add(CssVariables.RECTANGLE_OUTLINE);
		this.outLine.setStrokeWidth(LayoutExpressionBuilderController.OFFSET);
		this.outLine.getStrokeDashArray().addAll(5.0, 5.0);
	}

	public void setUserData(Object data)
	{
		this.data = data;
	}

	public Object getUserData()
	{
		return this.data;
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

		this.isInit = true;
	}

	public boolean isInit()
	{
		return isInit;
	}

	public void setInit(boolean init)
	{
		isInit = init;
	}

	public void setLineStrokeCap(StrokeLineCap lineCap, LinePosition position)
	{
		getLineByPosition(position).setStrokeLineCap(lineCap);
	}

	public Rectangle getRectangle()
	{
		return this.rectangle;
	}

	public void setGroup(Group group)
	{
		group.getChildren().addAll(top, right, bot, left, outLine);
	}

	public void removeGroup(Group group)
	{
		group.getChildren().removeAll(top, right, bot, left, outLine);
	}

	public void setVisible(boolean flag)
	{
		this.isVisible = flag;
		Arrays.asList(top, left, bot, right).stream().forEach(line -> line.setVisible(flag));
		this.outLine.setVisible(false);
	}

	public boolean isVisible()
	{
		return this.isVisible;
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
		getLineByPosition(position).setStrokeWidth(width);
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

	public void setFill(Color color)
	{
		this.top.setStroke(color);
		this.right.setStroke(color);
		this.bot.setStroke(color);
		this.left.setStroke(color);
	}

	public void addStyleClass(String styleClass, LinePosition position)
	{
		this.getLineByPosition(position).getStyleClass().add(styleClass);
	}

	public void clearOutline()
	{
		this.outLine.setVisible(false);
	}

	/**
	 * @param direction - arrow direction. if direction vertical outLine should be horizontal and contrariwise
	 */
	public void displayOutLine(int point, CustomArrow.ArrowDirection direction, int where, boolean isNeedCrossLine)
	{
		if (direction == CustomArrow.ArrowDirection.HORIZONTAL)
		{
			if ((top.getStartX() + top.getEndX()) / 2 == point)
			{
				double endY;
				if (where < left.getStartY())
				{
					endY = bot.getEndY();
				}
				else
				{
					endY = top.getEndY();
				}
				double startY = where;
				if (where > left.getStartY() && where < left.getEndY())
				{
					startY = left.getStartY();
					endY = left.getEndY();
				}
				outLine.setStartX(point);
				outLine.setEndX(point);
				outLine.setStartY(startY);
				outLine.setEndY(endY);
				outLine.setVisible(true);
				return;
			}
			/**
			 * outLine Vertical
			 */
			if (dotOnVerticalLine(left, where) || dotOnVerticalLine(right, where))
			{
				/**
				 * we not need to draw outline, because Vertical lines contains point
				 */
				outLine.setVisible(false);
				return;
			}

			/**
			 *  point on the continuation of right line
			 */
			if (right.getStartX() - LayoutExpressionBuilderController.OFFSET == point || right.getStartX() + LayoutExpressionBuilderController.OFFSET == point)
			{
				outLine.setStartX(point);
				outLine.setEndX(point);
				outLine.setEndY(where);
				outLine.setStartY(right.getStartY() < point ? right.getStartY() : right.getEndY());
			}
			/**
			 *  point on the continuation of left line
			 */
			else if (left.getStartX() - LayoutExpressionBuilderController.OFFSET == point || left.getStartX() + LayoutExpressionBuilderController.OFFSET == point)
			{
				outLine.setStartX(point);
				outLine.setEndX(point);
				outLine.setEndY(where);
				outLine.setStartY(left.getStartY() < point ? left.getStartY() : left.getEndY());
			}
		}
		else if (direction == CustomArrow.ArrowDirection.VERTICAL)
		{
			if ((right.getStartY() + right.getEndY()) / 2 == point)
			{
				double startX;
				double endX = where;
				if (where < left.getStartX())
				{
					startX = right.getStartX();
				}
				else
				{
					startX = left.getStartX();
				}
				if (where > left.getEndX() && where < right.getStartX())
				{
					startX = left.getStartX();
					endX = right.getStartX();
				}
				outLine.setStartY(point);
				outLine.setEndY(point);
				outLine.setEndX(endX);
				outLine.setStartX(startX);
				outLine.setVisible(true);
				return;
			}
			/**
			 * outLine Horizontal
			 */
			if (dotOnHorizontalLine(top, where) || dotOnHorizontalLine(bot, where))
			{
				/**
				 * we not need to draw outline, because horizontal lines contains point/end point
				 */
				outLine.setVisible(false);
				return;
			}
			if (top.getStartY() - LayoutExpressionBuilderController.OFFSET == point || top.getStartY() + LayoutExpressionBuilderController.OFFSET == point)
			{
				outLine.setStartY(point);
				outLine.setEndY(point);
				outLine.setEndX(where);
				outLine.setStartX(top.getStartX() < point ? top.getEndX() : top.getStartX());
			}
			else if (bot.getStartY() - LayoutExpressionBuilderController.OFFSET == point || bot.getStartY() + LayoutExpressionBuilderController.OFFSET == point)
			{
				outLine.setStartY(point);
				outLine.setEndY(point);
				outLine.setEndX(where);
				outLine.setStartX(bot.getStartX() < point ? bot.getEndX() : bot.getStartX());
			}
		}
		outLine.setVisible(true);
	}

	public boolean isSame(CustomRectangle other)
	{
		return this.top.getStartX() == other.top.getStartX()
						&& this.top.getEndX() == other.top.getEndX()
						&& this.left.getStartY() == other.left.getStartY()
						&& this.left.getEndY() == other.left.getEndY();
	}

	//============================================================
	// events methods
	//============================================================
	public void setOnMouseExited(EventHandler<MouseEvent> handler, LinePosition position)
	{
		getLineByPosition(position).setOnMouseExited(handler);
	}

	public void setOnMouseEntered(EventHandler<MouseEvent> handler, LinePosition position)
	{
		getLineByPosition(position).setOnMouseEntered(handler);
	}

	public void setOnMouseClick(EventHandler<MouseEvent> handler, LinePosition position)
	{
		getLineByPosition(position).setOnMouseClicked(handler);
	}

	//============================================================
	// private methods
	//============================================================
	private Line getLineByPosition(LinePosition position)
	{
		switch (position)
		{
			case TOP:
				return this.top;
			case RIGHT:
				return this.right;
			case BOT:
				return this.bot;
			case LEFT:
				return this.left;
		}
		throw new RuntimeException("Unexpected position : " + position);
	}

	private boolean dotOnVerticalLine(Line line, int dot)
	{
		double min = Math.min(line.getStartY(), line.getEndY());
		double max = Math.max(line.getStartY(), line.getEndY());
		return min <= dot && dot <= max;
	}

	private boolean dotOnHorizontalLine(Line line, int dot)
	{
		double min = Math.min(line.getStartX(), line.getEndX());
		double max = Math.max(line.getStartX(), line.getEndX());
		return min <= dot && dot <= max;
	}
}
