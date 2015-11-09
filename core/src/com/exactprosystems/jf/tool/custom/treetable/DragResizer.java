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

public class DragResizer
{

	private static final int RESIZE_MARGIN = 5;
	private final Region region;
	private final int MIN_VALUE = 40;
	private double y;
	private boolean initMinHeight;
	private boolean dragging;

	private DragResizer(Region aRegion)
	{
		region = aRegion;
	}

	public static void makeResizable(Region region)
	{
		final DragResizer resizer = new DragResizer(region);

		region.setOnMousePressed(resizer::mousePressed);
		region.setOnMouseDragged(resizer::mouseDragged);
		region.setOnMouseMoved(resizer::mouseOver);
		region.setOnMouseReleased(resizer::mouseReleased);
		region.setOnMouseExited(resizer::mouseExited);
	}

	protected void mouseReleased(MouseEvent event)
	{
		dragging = false;
		region.setCursor(Cursor.DEFAULT);
	}

	protected void mouseExited(MouseEvent event)
	{
		if (!dragging)
			region.getStyleClass().remove(CssVariables.RESIZBLE_REGION);
	}

	protected void mouseOver(MouseEvent event)
	{
		if (isInDraggableZone(event) || dragging)
		{
			region.setCursor(Cursor.S_RESIZE);
			if (!region.getStyleClass().contains(CssVariables.RESIZBLE_REGION))
				region.getStyleClass().add(CssVariables.RESIZBLE_REGION);
		}
		else
		{
			region.getStyleClass().remove(CssVariables.RESIZBLE_REGION);
			region.setCursor(Cursor.DEFAULT);
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
		if (newHeight > MIN_VALUE)
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
		System.out.println(event.getSource());
		dragging = true;
		if (!initMinHeight)
		{
			region.setMinHeight(region.getHeight());
			initMinHeight = true;
		}

		y = event.getY();
	}
}