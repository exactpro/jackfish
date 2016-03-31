////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.console;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

public class CustomListView<T> extends ListView<ConsoleText<T>>
{
	private OnDoubleClickListener<T> listener;

	public CustomListView(final boolean mayClear)
	{
		super();
		this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.setCellFactory(cellConsoleFxListView -> new ColorCell<>(mayClear));

		this.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getClickCount() == 2)
			{
				ConsoleText<T> selectedItem = getSelectionModel().getSelectedItem();
				if (selectedItem != null && listener != null && selectedItem.getItem() != null)
				{
					listener.onDoubleClick(selectedItem.getItem());
				}
			}
		});
	}

	public CustomListView(OnDoubleClickListener<T> listener, boolean mayClear)
	{
		this(mayClear);
		this.listener = listener;
	}

	public void autoScroll(boolean flag)
	{
		if (flag)
		{
			this.getItems().addListener(changeListener);
		}
		else
		{
			this.getItems().removeListener(changeListener);
		}
	}

	public void setListener(OnDoubleClickListener<T> listener)
	{
		this.listener = listener;
	}

	private ListChangeListener<ConsoleText<T>> changeListener = c -> {
		int size = this.getItems().size();
		if (size > 0)
		{
			Platform.runLater(() -> this.scrollTo(size - 1));
		}
	};
}
