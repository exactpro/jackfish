////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;

import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.awt.*;
import java.util.Objects;
import java.util.stream.Stream;

public class ScalableArrow  extends Rectangle
{
    private static final long serialVersionUID = -1717871114695561335L;

    private static final int OUTLINE_WIDTH = 2;
    
    private MarkerStyle state;
	private Rectangle rectangle;
	private double scale;

	private Line top;
	private Line left;
	private Line right;
	private Line bot;
	private Line outLine;
	private Text text;
	private Timeline timeline;

	public ScalableArrow(Rectangle rectangle, double scaleFactor)
	{
		this();
		updateRectangle(rectangle, scaleFactor);
	}

	public ScalableArrow()
	{
	    this.state = MarkerStyle.ADD;
        this.rectangle  = new Rectangle();
        this.top        = new Line();
        this.left       = new Line();
        this.right      = new Line();
        this.bot        = new Line();
        this.outLine    = new Line();
        this.outLine.getStyleClass().add(CssVariables.RECTANGLE_OUTLINE);
        this.outLine.setStrokeWidth(OUTLINE_WIDTH);
        this.outLine.getStrokeDashArray().addAll(5.0, 5.0);
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
		group.getChildren().addAll(top, right, bot, left, outLine, this.text);
	}

	public void removeGroup(Group group)
	{
		group.getChildren().removeAll(top, right, bot, left, outLine, this.text);
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
		this.outLine.setVisible(flag);
	}

	public boolean isVisible()
	{
	    return Stream.of(top, left, bot, right).allMatch(Line::isVisible) 
	            && this.outLine.isVisible() 
	            && this.text.isVisible();
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

	public void clearOutline()
	{
		this.outLine.setVisible(false);
	}

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

	public void setOpacity(double value)
	{
		Stream.of(top, left, bot, right, this.text, this.outLine).forEach(line -> line.setOpacity(value));
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
		return Objects.equals(this.rectangle, ((ScalableArrow)o).rectangle);
	}

	@Override
	public int hashCode()
	{
		return this.rectangle.hashCode();
	}

	//============================================================
	// private methods
	//============================================================
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
