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

public class CustomArrow
{
	public enum ArrowPosition
	{
		VERTICAL,
		HORIZONTAL
	}

	public static final int ARROW_SIZE = 5;
	private double start;
	private double end;
	private ArrowPosition position;

	private Line line;
	private Line l1;
	private Line l2;
	private Line l3;
	private Line l4;

	public CustomArrow()
	{
		this.line = new Line();
		this.l1 = new Line();
		this.l2 = new Line();
		this.l3 = new Line();
		this.l4 = new Line();

		this.line.setVisible(false);
		this.line.getStyleClass().add(CssVariables.LINE);

		this.l1.setVisible(false);
		this.l2.setVisible(false);
		this.l3.setVisible(false);
		this.l4.setVisible(false);
		this.l1.getStyleClass().add(CssVariables.LINE_ARROW);
		this.l2.getStyleClass().add(CssVariables.LINE_ARROW);
		this.l3.getStyleClass().add(CssVariables.LINE_ARROW);
		this.l4.getStyleClass().add(CssVariables.LINE_ARROW);

		this.position = ArrowPosition.HORIZONTAL;
	}

	public CustomArrow(double start, double end, ArrowPosition position)
	{
		this();
		this.start = start;
		this.end = end;
		this.position = position;
	}

	public void setPosition(ArrowPosition position)
	{
		this.position = position;
	}

	/**
	 * @param start - start point
	 * @param end - end point
	 */
	public void setPoints(double start, double end)
	{
		this.start = start;
		this.end = end;
	}

	/**
	 * @param x - this is x or y, depending on arrowPosition
	 */
	public void show(double x)
	{
		createLine(x);
		this.line.setVisible(true);
		this.l1.setVisible(true);
		this.l2.setVisible(true);
		this.l3.setVisible(true);
		this.l4.setVisible(true);
	}

	public void hide()
	{
		this.line.setVisible(false);
		this.l1.setVisible(false);
		this.l2.setVisible(false);
		this.l3.setVisible(false);
		this.l4.setVisible(false);
	}

	public void setGroup(Group group)
	{
		group.getChildren().addAll(this.line, this.l1, this.l2, this.l3, this.l4);
	}

	private boolean needArrow()
	{
		return Math.abs(Math.abs(this.start) - Math.abs(this.end)) > (ARROW_SIZE * 2);
	}

	private void createLine(double x)
	{
		if (this.position == ArrowPosition.VERTICAL)
		{
			if (needArrow())
			{
				/**
				 *  /|\
				 *   |
				 *  \|/
				 */
				this.line.setStartY(this.start);
				this.line.setEndY(this.end);
				this.line.setStartX(x);
				this.line.setEndX(x);

				this.l1.setStartX(x);
				this.l1.setEndX(x - ARROW_SIZE);
				this.l1.setStartY(this.start);
				this.l1.setEndY(this.start + ARROW_SIZE);

				this.l2.setStartX(x);
				this.l2.setEndX(x + ARROW_SIZE);
				this.l2.setStartY(this.start);
				this.l2.setEndY(this.start + ARROW_SIZE);

				this.l3.setStartX(x);
				this.l3.setEndX(x + ARROW_SIZE);
				this.l3.setStartY(this.end);
				this.l3.setEndY(this.end - ARROW_SIZE);

				this.l4.setStartX(x);
				this.l4.setEndX(x - ARROW_SIZE);
				this.l4.setStartY(this.end);
				this.l4.setEndY(this.end - ARROW_SIZE);
			}
			else
			{
				/**
				 *  \/
				 *
				 *  /\
				 */
				this.line.setStartX(0);
				this.line.setEndY(0);
				this.line.setStartY(0);
				this.line.setEndY(0);

				this.l1.setStartX(x);
				this.l1.setEndX(x - ARROW_SIZE);
				this.l1.setStartY(this.start);
				this.l1.setEndY(this.start - ARROW_SIZE);

				this.l2.setStartX(x);
				this.l2.setEndX(x + ARROW_SIZE);
				this.l2.setStartY(this.start);
				this.l2.setEndY(this.start - ARROW_SIZE);

				this.l3.setStartX(x);
				this.l3.setEndX(x + ARROW_SIZE);
				this.l3.setStartY(this.end);
				this.l3.setEndY(this.end + ARROW_SIZE);

				this.l4.setStartX(x);
				this.l4.setEndX(x - ARROW_SIZE);
				this.l4.setStartY(this.end);
				this.l4.setEndY(this.end + ARROW_SIZE);
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

				this.l1.setStartX(this.start);
				this.l1.setEndX(this.start + ARROW_SIZE);
				this.l1.setStartY(x);
				this.l1.setEndY(x - ARROW_SIZE);

				this.l2.setStartX(this.end);
				this.l2.setEndX(this.end - ARROW_SIZE);
				this.l2.setStartY(x);
				this.l2.setEndY(x - ARROW_SIZE);

				this.l3.setStartX(this.end);
				this.l3.setEndX(this.end - ARROW_SIZE);
				this.l3.setStartY(x);
				this.l3.setEndY(x + ARROW_SIZE);

				this.l4.setStartX(this.start);
				this.l4.setEndX(this.start + ARROW_SIZE);
				this.l4.setStartY(x);
				this.l4.setEndY(x + ARROW_SIZE);
			}
			else
			{
				/**
				 *  \  /
				 *  /  \
				 */
				this.line.setStartX(0);
				this.line.setEndY(0);
				this.line.setStartY(0);
				this.line.setEndY(0);

				this.l1.setStartX(this.start);
				this.l1.setEndX(this.start - ARROW_SIZE);
				this.l1.setStartY(x);
				this.l1.setEndY(x - ARROW_SIZE);

				this.l2.setStartX(this.end);
				this.l2.setEndX(this.end + ARROW_SIZE);
				this.l2.setStartY(x);
				this.l2.setEndY(x - ARROW_SIZE);

				this.l3.setStartX(this.end);
				this.l3.setEndX(this.end + ARROW_SIZE);
				this.l3.setStartY(x);
				this.l3.setEndY(x + ARROW_SIZE);

				this.l4.setStartX(this.start);
				this.l4.setEndX(this.start - ARROW_SIZE);
				this.l4.setStartY(x);
				this.l4.setEndY(x + ARROW_SIZE);
			}

		}
	}
}
