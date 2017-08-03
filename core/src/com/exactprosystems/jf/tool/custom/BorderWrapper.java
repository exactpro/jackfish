////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BorderWrapper
{
	private double outerPad = 16;
	private double innerPad = 16;
	private double radius = 5;
	private double thick = 1;
	private BorderStrokeStyle strokeStyle = BorderStrokeStyle.SOLID;

	private Color color = Color.DARKGRAY;
	private String title;
	private final Node node;
	private final List<StrokeBorder> borders;

	public static BorderWrapper wrap(Node n)
	{
		return new BorderWrapper(n);
	}

	public BorderWrapper title(String title)
	{
		this.title = title;
		return this;
	}

	public BorderWrapper color(Color color)
	{
		this.color = color;
		return this;
	}

	public BorderWrapper outerPadding(double value)
	{
		this.outerPad = value;
		return this;
	}

	public BorderWrapper innerPadding(double value)
	{
		this.innerPad = value;
		return this;
	}

	private BorderWrapper(Node n)
	{
		this.node = n;
		this.borders = new ArrayList<>();
	}

	public Node build()
	{
		BorderStroke borderStroke = new BorderStroke(
				color, color, color, color,
				strokeStyle, strokeStyle, strokeStyle, strokeStyle,
				new CornerRadii(radius, radius, radius, radius, false),
				new BorderWidths(thick, thick, thick, thick),
				null);

		BorderStroke outerPadding = new BorderStroke(
				null,
				BorderStrokeStyle.NONE,
				null,
				new BorderWidths(outerPad, outerPad, outerPad, outerPad),
				Insets.EMPTY);

		BorderStroke innerPadding = new BorderStroke(
				null,
				BorderStrokeStyle.NONE,
				null,
				new BorderWidths(innerPad, innerPad, innerPad, innerPad),
				Insets.EMPTY);

		borders.add(new StrokeBorder(null, outerPadding));
		borders.add(new StrokeBorder(title, borderStroke));
		borders.add(new StrokeBorder(null, innerPadding));

		Node bundle = node;
		for (int i = borders.size() - 1; i >= 0; i--)
		{
			StrokeBorder border = borders.get(i);
			bundle = border.wrap(bundle);
		}
		return bundle;
	}

	private static class StrokeBorder
	{
		private static final int TITLE_PADDING = 3;
		private static final double GAP_PADDING = TITLE_PADDING * 2 - 1;

		private final String title;
		private final BorderStroke[] borderStrokes;

		public StrokeBorder(String title, BorderStroke... borderStrokes)
		{
			this.title = title;
			this.borderStrokes = borderStrokes;
		}

		public Node wrap(final Node n)
		{
			StackPane pane = new StackPane()
			{
				Label titleLabel;

				{
					// add in the node we are wrapping
					getChildren().add(n);


					// if the title string is set, then also add in the title label
					if (title != null)
					{
						titleLabel = new Label(title);
						titleLabel.getStyleClass().add(CssVariables.BORDER_WRAPPER_TITLE);

						// give the text a bit of space on the left...
						titleLabel.setPadding(new Insets(0, 0, 0, TITLE_PADDING));
						getChildren().add(titleLabel);
					}
				}

				@Override
				protected void layoutChildren()
				{
					super.layoutChildren();

					if (titleLabel != null)
					{
						// layout the title label
						final double labelHeight = titleLabel.prefHeight(-1);
						final double labelWidth = titleLabel.prefWidth(labelHeight) + TITLE_PADDING;
						titleLabel.resize(labelWidth, labelHeight);
						titleLabel.relocate(TITLE_PADDING * 2, -labelHeight / 2.0 - 1);

						List<BorderStroke> newBorderStrokes = new ArrayList<>(2);

						// create a line gap for the title label
						for (BorderStroke bs : borderStrokes)
						{
							List<Double> dashList = new ArrayList<>();

							// Create a dash list for the line gap or add it at the beginning of an existing dash list. This gap should be wide enough for the title label.
							if (bs.getTopStyle().getDashArray().isEmpty())
								dashList.addAll(Arrays.asList(GAP_PADDING, labelWidth, Double.MAX_VALUE));
							else
							{ // dash pattern exists
								// insert gap in existing dash pattern and multiply original pattern so that gap does not show more then once
								double origDashWidth = bs.getTopStyle().getDashArray().stream().mapToDouble(d -> d).sum();

								if (origDashWidth > GAP_PADDING)
								{
									dashList.add(GAP_PADDING);
									dashList.add(labelWidth);
								}
								else
								{ // need to insert dash pattern before the gap
									int no = (int) (GAP_PADDING / origDashWidth);

									for (int i = 0; i < no; i++)
										dashList.addAll(bs.getTopStyle().getDashArray());

									if ((dashList.size() & 1) == 0) // if size is even number, add one more element because gap must be at odd index to be transparent
										dashList.add(0d);

									dashList.add(labelWidth + GAP_PADDING - no * origDashWidth);
								}

								for (int i = 0; i < (getWidth() - labelWidth - origDashWidth) / origDashWidth; i++)
									dashList.addAll(bs.getTopStyle().getDashArray());
							}

							// create new border stroke style for the top border line with new dash list
							BorderStrokeStyle topStrokeStyle = new BorderStrokeStyle(bs.getTopStyle().getType(), bs.getTopStyle().getLineJoin(), bs.getTopStyle().getLineCap(), bs.getTopStyle().getMiterLimit(), bs.getTopStyle().getDashOffset(), dashList);

							// change existing border stroke to utilize new top border line stroke style
							newBorderStrokes.add(new BorderStroke(bs.getTopStroke(), bs.getRightStroke(), bs.getBottomStroke(), bs.getLeftStroke(), topStrokeStyle, bs.getRightStyle(), bs.getBottomStyle(), bs.getLeftStyle(), bs.getRadii(), bs.getWidths(), null));
						}

						setBorder(new javafx.scene.layout.Border(newBorderStrokes.toArray(new BorderStroke[newBorderStrokes.size()])));
					}
				}
			};

			pane.setBorder(new javafx.scene.layout.Border(borderStrokes));
			return pane;
		}
	}
}
