////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.fields;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class CustomFieldWithButton extends CustomField
{
	private final EventHandler<MouseEvent> defaultHandler = event -> this.clear();
	private Label label = new Label();
	private StackPane stackPane = new StackPane();

	/**
	 *  default constructor. Button - X, handler - clear().
	 */
	public CustomFieldWithButton(String text)
	{
		super(text);
		this.getStyleClass().add("customFieldWithButton");
		updateStyle("X", defaultHandler);
	}

	public CustomFieldWithButton()
	{
		this("");
	}

	private void updateStyle(String buttonText, EventHandler<MouseEvent> handler)
	{
		this.label = new Label(buttonText);
		label.getStyleClass().addAll("customButton");

		this.stackPane = new StackPane(label);
		stackPane.getStyleClass().addAll("customPane"); //$NON-NLS-1$
		stackPane.setOpacity(this.getText().isEmpty() ? 0.0 : 1.0);
		stackPane.setCursor(Cursor.DEFAULT);
		stackPane.setOnMouseReleased(handler);
		stackPane.setVisible(!this.getText().isEmpty());

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
