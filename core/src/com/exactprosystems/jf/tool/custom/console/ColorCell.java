////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.console;

import com.exactprosystems.jf.tool.Common;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;

public class ColorCell<T> extends ListCell<ConsoleText<T>>
{
	public ColorCell(boolean mayClear)
	{
		ContextMenu contextMenu = new ContextMenu();

		MenuItem clear = new MenuItem("Clear");
		MenuItem copy = new MenuItem("Copy");

		contextMenu.getItems().addAll(copy);
		if (mayClear)
		{
			contextMenu.getItems().add(clear);
		}

		setContextMenu(contextMenu);

		clear.setOnAction(event -> {
			getListView().getItems().forEach(i -> i.setText(""));
			getListView().getItems().clear();
		});

		copy.setOnAction(event -> {
			if (getListView().getSelectionModel().getSelectedItem() != null)
			{
				ConsoleText myT = getListView().getSelectionModel().getSelectedItem();
				Common.copyText(myT.toString());
			}
		});
	}

	@Override
	protected void updateItem(final ConsoleText<T> s, boolean b)
	{
		super.updateItem(s, b);
		Platform.runLater(() -> {
			if (s != null)
			{
				setGraphic(s);
			}
			else
			{
				setGraphic(null);
			}
		});
	}
}
