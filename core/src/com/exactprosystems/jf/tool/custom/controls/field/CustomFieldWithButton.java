////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.field;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class CustomFieldWithButton extends CustomField
{
	private Label     label;
	private StackPane stackPane;

	/**
	 * default constructor. Button - X, handler - clear().
	 */
	public CustomFieldWithButton(String text)
	{
		super(text);
		super.getStyleClass().add(CssVariables.CUSTOM_FIELD_WITH_BUTTON);
		this.updateStyle("X", event -> super.clear());
	}

	public CustomFieldWithButton()
	{
		this("");
	}

	public void setHandler(EventHandler<MouseEvent> handler)
	{
		this.stackPane.setOnMouseReleased(handler);
	}

	public void setButtonText(String text)
	{
		this.label.setText(text);
	}

	private void updateStyle(String buttonText, EventHandler<MouseEvent> handler)
	{
		this.label = new Label(buttonText);
		this.label.getStyleClass().addAll(CssVariables.CUSTOM_FIELD_CUSTOM_BUTTON);
		this.stackPane = new StackPane(this.label);
		this.stackPane.getStyleClass().addAll(CssVariables.CUSTOM_FIELD_CUSTOM_PANE);
		this.stackPane.setCursor(Cursor.DEFAULT);
		this.stackPane.setOnMouseReleased(handler);
		super.rightProperty().set(this.stackPane);
	}
}
