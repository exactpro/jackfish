////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.area;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class CustomAreaWithButton  extends CustomArea
{
	private final EventHandler<MouseEvent> defaultHandler = event -> this.clear();
	private Label label = new Label();
	private StackPane stackPane = new StackPane();

	/**
	 *  default constructor. Button - X, handler - clear().
	 */
	public CustomAreaWithButton(String text)
	{
		super(text);
		this.getStyleClass().add(CssVariables.CUSTOM_AREA_WITH_BUTTON);
		updateStyle("X", defaultHandler);
	}

	public CustomAreaWithButton()
	{
		this("");
	}

	private void updateStyle(String buttonText, EventHandler<MouseEvent> handler)
	{
		this.label = new Label(buttonText);
		label.getStyleClass().addAll(CssVariables.CUSTOM_AREA_CUSTOM_BUTTON);
		this.stackPane = new StackPane(label);
		stackPane.getStyleClass().addAll(CssVariables.CUSTOM_AREA_CUSTOM_PANE);
		stackPane.setCursor(Cursor.DEFAULT);
		stackPane.setOnMouseReleased(handler);
		this.rightProperty().set(stackPane);
	}

	public void setHandler(EventHandler<MouseEvent> handler)
	{
		this.stackPane.setOnMouseReleased(handler);
	}

	public void setButtonText(String text)
	{
		this.label.setText(text);
	}
}
