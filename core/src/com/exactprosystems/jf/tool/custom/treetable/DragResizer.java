////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.Optional;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;

public class DragResizer
{
	private static final int RESIZE_MARGIN = 5;
	private final Region region;
	private final int minValue = 40;
	private DoublePredicate predicate;
	private double y;
	private boolean initMinHeight;
	private boolean dragging;
	private DoubleConsumer consumer;

	private DragResizer(Region aRegion, DoubleConsumer consumer)
	{
		this.region = aRegion;
		this.consumer = consumer;
	}

	public static void makeResizable(Region region, DoubleConsumer consumer)
	{
		final DragResizer resizer = new DragResizer(region, consumer);

		region.setOnMousePressed(resizer::mousePressed);
		region.setOnMouseDragged(resizer::mouseDragged);
//		region.setOnMouseMoved(resizer::mouseOver);
		region.addEventFilter(MouseEvent.MOUSE_MOVED, resizer::mouseOver);
		region.setOnMouseReleased(resizer::mouseReleased);
		region.setOnMouseExited(resizer::mouseExited);
		region.getStyleClass().add(CssVariables.RESIZABLE_REGION);
	}

	public static void makeResizable(Region region, DoublePredicate minPredicate, DoubleConsumer consumer)
	{
		final DragResizer resizer = new DragResizer(region, consumer);
		resizer.predicate = minPredicate;
		region.setOnMousePressed(resizer::mousePressed);
		region.setOnMouseDragged(resizer::mouseDragged);
		region.setOnMouseMoved(resizer::mouseOver);
		region.setOnMouseReleased(resizer::mouseReleased);
		region.setOnMouseExited(resizer::mouseExited);
		region.getStyleClass().add(CssVariables.RESIZABLE_REGION);
	}

	protected void mouseReleased(MouseEvent event)
	{
		this.dragging = false;
		this.region.setCursor(Cursor.DEFAULT);
		Optional.ofNullable(this.consumer).ifPresent(c -> c.accept(this.region.getPrefHeight()));
	}

	protected void mouseExited(MouseEvent event)
	{
		if (!this.dragging)
		{
			this.region.getStyleClass().remove(CssVariables.RESIZABLE_REGION_HOVER);
		}
	}

	protected void mouseOver(MouseEvent event)
	{
		if (this.isInDraggableZone(event) || this.dragging)
		{
			this.region.setCursor(Cursor.S_RESIZE);
			if (!this.region.getStyleClass().contains(CssVariables.RESIZABLE_REGION_HOVER))
			{
				this.region.getStyleClass().add(CssVariables.RESIZABLE_REGION_HOVER);
			}
		}
		else
		{
			this.region.getStyleClass().remove(CssVariables.RESIZABLE_REGION_HOVER);
			this.region.setCursor(Cursor.DEFAULT);
		}
	}

	protected boolean isInDraggableZone(MouseEvent event)
	{
		return event.getY() >= (region.getHeight() - RESIZE_MARGIN);
	}

	protected void mouseDragged(MouseEvent event)
	{
		if (!dragging)
		{
			return;
		}
		double mousey = event.getY();
		double newHeight = region.getMinHeight() + (mousey - y);
		Boolean value = Optional.ofNullable(this.predicate).map(pr -> pr.test(newHeight)).orElse(newHeight > minValue);
		if (value)
		{
			region.setMinHeight(newHeight);
			region.setPrefHeight(newHeight);
			region.setMaxHeight(newHeight);
			y = mousey;
		}
	}

	protected void mousePressed(MouseEvent event)
	{
		if (!isInDraggableZone(event))
		{
			return;
		}
		this.dragging = true;
		this.region.setMinHeight(this.region.getHeight());
//		if (!this.initMinHeight)
//		{
//			this.region.setMinHeight(this.region.getHeight());
//			this.initMinHeight = true;
//		}

		this.y = event.getY();
	}
}