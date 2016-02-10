////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
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
	private final ListView<T> suggestionList;
	final int LIST_CELL_HEIGHT = 24;

	public AutoCompletePopupSkin(AutoCompletePopup<T> control)
	{
		this.control = control;
		suggestionList = new ListView<>(control.getSuggestions());

		suggestionList.getStyleClass().add(AutoCompletePopup.DEFAULT_STYLE_CLASS);

		/**
		 * Here we bind the prefHeightProperty to the minimum height between the
		 * max visible rows and the current items list. We also add an arbitrary
		 * 5 number because when we have only one item we have the vertical
		 * scrollBar showing for no reason.
		 */
		suggestionList.prefHeightProperty().bind(Bindings.min(control.visibleRowCountProperty(), Bindings.size(suggestionList.getItems())).multiply(LIST_CELL_HEIGHT).add(5));
		suggestionList.setCellFactory(TextFieldListCell.forListView(control.getConverter()));
		registerEventListener();
	}

	private void registerEventListener()
	{
		suggestionList.setOnMouseClicked(me -> {
			if (me.getButton() == MouseButton.PRIMARY)
			{
				onSuggestionChoosen(suggestionList.getSelectionModel().getSelectedItem());
			}
		});


		suggestionList.setOnKeyPressed(ke -> {
			switch (ke.getCode())
			{
				case ENTER:
				case TAB:
					onSuggestionChoosen(suggestionList.getSelectionModel().getSelectedItem());
					break;
				case ESCAPE:
					if (control.isHideOnEscape())
					{
						control.hide();
					}
					break;
				default:
					break;
			}
		});
	}

	private void onSuggestionChoosen(T suggestion)
	{
		if (suggestion != null)
		{
			Event.fireEvent(control, new AutoCompletePopup.SuggestionEvent<>(suggestion));
		}
	}


	@Override
	public Node getNode()
	{
		return suggestionList;
	}

	@Override
	public AutoCompletePopup<T> getSkinnable()
	{
		return control;
	}

	@Override
	public void dispose()
	{
	}
}
