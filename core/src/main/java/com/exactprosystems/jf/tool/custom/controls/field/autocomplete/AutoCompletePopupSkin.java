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
package com.exactprosystems.jf.tool.custom.controls.field.autocomplete;

import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseButton;


public class AutoCompletePopupSkin<T> implements Skin<AutoCompletePopup<T>>
{
	private final AutoCompletePopup<T> control;
	private final ListView<T>          suggestionList;
	private static final int LIST_CELL_HEIGHT = 24;

	public AutoCompletePopupSkin(AutoCompletePopup<T> control)
	{
		this.control = control;
		this.suggestionList = new ListView<>(control.getSuggestions());
		this.suggestionList.getStyleClass().add(AutoCompletePopup.DEFAULT_STYLE_CLASS);
		this.suggestionList.prefHeightProperty().bind(Bindings.min(control.visibleRowCountProperty(), Bindings.size(this.suggestionList.getItems())).multiply(LIST_CELL_HEIGHT).add(5));
		this.suggestionList.setCellFactory(TextFieldListCell.forListView(control.getConverter()));
		this.registerEventListener();
	}

	//region interface Skin
	@Override
	public Node getNode()
	{
		return this.suggestionList;
	}

	@Override
	public AutoCompletePopup<T> getSkinnable()
	{
		return this.control;
	}

	@Override
	public void dispose()
	{
	}
	//endregion

	//region private methods
	private void registerEventListener()
	{
		this.suggestionList.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getButton() == MouseButton.PRIMARY)
			{
				this.onSuggestionChosen(this.suggestionList.getSelectionModel().getSelectedItem());
			}
		});
		this.suggestionList.setOnKeyPressed(keyEvent ->
		{
			switch (keyEvent.getCode())
			{
				case ENTER:
				case TAB:
					this.onSuggestionChosen(this.suggestionList.getSelectionModel().getSelectedItem());
					break;
				case ESCAPE:
					if (this.control.isHideOnEscape())
					{
						this.control.hide();
					}
					break;
				default:
					break;
			}
		});
	}

	private void onSuggestionChosen(T suggestion)
	{
		if (suggestion != null)
		{
			Event.fireEvent(this.control, new AutoCompletePopup.SuggestionEvent<>(suggestion));
		}
	}
	//endregion
}
