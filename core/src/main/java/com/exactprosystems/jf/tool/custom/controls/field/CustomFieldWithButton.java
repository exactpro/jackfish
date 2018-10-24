/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
