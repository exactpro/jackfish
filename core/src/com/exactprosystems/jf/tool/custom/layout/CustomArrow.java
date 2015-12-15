////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class CustomArrow
{
	public enum ArrowDirection
	{
		VERTICAL,
		HORIZONTAL
	}

	public static final double TRIANGLE_BASE	= 10;
	public static final double TRIANGLE_HEIGHT	= 13;
	public static final double LINE_LENGTH		= 25;

	private int start;
	private int end;
	private ArrowDirection direction;

	private boolean needShowLine2 = false;
	private Line line2;
	private Line line;
	private Polygon t1;
	private Polygon t2;

	/**
	 * default constructor
	 * all lines and polygons are empty.
	 * default position = horizontal.
	 */
	public CustomArrow()
	{
		this.line = new Line();
		this.line2 = new Line();

		this.t1 = new Polygon();
		this.t2 = new Polygon();

		this.line.setVisible(false);
		this.line.getStyleClass().add(CssVariables.LINE);
		this.line2.setVisible(false);
		this.line.getStyleClass().add(CssVariables.LINE);

		this.t1.setVisible(false);
		this.t2.setVisible(false);
		this.t1.getStyleClass().add(CssVariables.LINE_TRIANGLE);
		this.t2.getStyleClass().add(CssVariables.LINE_TRIANGLE);

		this.direction = ArrowDirection.HORIZONTAL;
	}

	public CustomArrow(int start, int end, ArrowDirection direction)
	{
		this();
		this.start = start;
		this.end = end;
		this.direction = direction;
	}

	public void setDirection(ArrowDirection direction)
	{
		this.direction = direction;
	}

	/**
	 * @param start - start line point
	 * @param end - end line point
	 */
	public void setPoints(int start, int end)
	{
		this.start = start;
		this.end = end;
	}

	/**
	 * @param x - this is x or y, depending on arrowPosition
	 */
	public void show(int x)
	{
		createLine(x);
		this.line.setVisible(true);
		if (this.needShowLine2)
		{
			this.line2.setVisible(true);
		}
		this.t1.setVisible(true);
		this.t2.setVisible(true);
	}

	public void hide()
	{
		this.line.setVisible(false);
		this.line2.setVisible(false);
		this.t1.setVisible(false);
		this.t2.setVisible(false);
		this.t1.getPoints().clear();
		this.t2.getPoints().clear();
	}

	public void setGroup(Group group)
	{
		group.getChildren().addAll(this.line, this.line2, this.t1, this.t2);
	}

	private boolean needArrow()
	{
		return (Math.max(this.start, this.end) - Math.min(this.start, this.end)) > (TRIANGLE_HEIGHT * 2 + 3);
	}

	private void createLine(int x)
	{
		int temp = this.start;
		this.start = Math.min(this.start, this.end);
		this.end = Math.max(temp, this.end);
		needShowLine2 = false;
		if (this.direction == ArrowDirection.VERTICAL)
		{
			if (needArrow())
			{
				/**
				 *  /|\
				 *   |
				 *  \|/
				 */
				this.line.setStartX(x);
				this.line.setEndX(x);
				this.line.setStartY(this.start);
				this.line.setEndY(this.end);

				this.t1.getPoints().addAll(
						(double) x, (double) this.start,
						x-TRIANGLE_BASE/2, this.start + TRIANGLE_HEIGHT,
						x+TRIANGLE_BASE/2, this.start + TRIANGLE_HEIGHT
				);

				this.t2.getPoints().addAll(
						(double) x, (double) this.end,
						x-TRIANGLE_BASE/2, this.end - TRIANGLE_HEIGHT,
						x+TRIANGLE_BASE/2, this.end - TRIANGLE_HEIGHT
				);
			}
			else
			{
				/**
				 *   |
				 *  \ /
				 *  / \
				 *   |
				 */
				this.line.setStartX(x);
				this.line.setEndX(x);
				this.line.setStartY(this.start);
				this.line.setEndY(this.start - LINE_LENGTH);

				this.needShowLine2 = true;

				this.line2.setStartX(x);
				this.line2.setEndX(x);
				this.line2.setStartY(this.end);
				this.line2.setEndY(this.end + LINE_LENGTH);

				this.t1.getPoints().addAll(
						(double) x, (double) this.start,
						x - TRIANGLE_BASE/2, this.start - TRIANGLE_HEIGHT,
						x + TRIANGLE_BASE/2, this.start - TRIANGLE_HEIGHT
				);

				this.t2.getPoints().addAll(
						(double) x, (double) this.end,
						x + TRIANGLE_BASE/2, this.end + TRIANGLE_HEIGHT,
						x - TRIANGLE_BASE/2, this.end + TRIANGLE_HEIGHT
				);
			}
		}
		else
		{
			if (needArrow())
			{
				/**
				 * /   \
				 * -----
				 * \   /
				 */
				this.line.setStartX(this.start);
				this.line.setStartY(x);
				this.line.setEndX(this.end);
				this.line.setEndY(x);

				this.t1.getPoints().addAll(
						(double) this.start, (double) x,
						this.start + TRIANGLE_HEIGHT, x - TRIANGLE_BASE/2,
						this.start + TRIANGLE_HEIGHT, x + TRIANGLE_BASE/2
				);

				this.t2.getPoints().addAll(
						(double) this.end, (double) x,
						this.end - TRIANGLE_HEIGHT, x + TRIANGLE_BASE/2,
						this.end - TRIANGLE_HEIGHT, x - TRIANGLE_BASE/2
				);
			}
			else
			{
				/**
				 * __ \  / __
				 *    /  \
				 */
				this.line.setStartX(this.start);
				this.line.setEndX(this.start - LINE_LENGTH);
				this.line.setStartY(x);
				this.line.setEndY(x);

				this.needShowLine2 = true;

				this.line2.setStartX(this.end);
				this.line2.setEndX(this.end + LINE_LENGTH);
				this.line2.setStartY(x);
				this.line2.setEndY(x);

				this.t1.getPoints().addAll(
						(double) this.start, (double) x,
						this.start - TRIANGLE_HEIGHT, x + TRIANGLE_BASE/2,
						this.start - TRIANGLE_HEIGHT, x - TRIANGLE_BASE/2
				);

				this.t2.getPoints().addAll(
						(double) this.end, (double) x,
						this.end + TRIANGLE_HEIGHT, x - TRIANGLE_BASE/2,
						this.end + TRIANGLE_HEIGHT, x + TRIANGLE_BASE/2
				);
			}

		}
	}
}
