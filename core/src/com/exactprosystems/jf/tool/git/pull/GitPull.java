////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.pull;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.main.Main;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

import java.io.File;
import java.util.List;

public class GitPull
{
	private final Main model;

	public GitPull(Main model)
	{
		this.model = model;
	}

	public void display(String title, List<File> files)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setResizable(true);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		dialog.setTitle("Git pull");
		dialog.getDialogPane().setHeaderText(title);
		ListView<File> listView = new ListView<>(FXCollections.observableArrayList(files));
		listView.setCellFactory(p -> new ListCell<File>(){
			@Override
			protected void updateItem(File item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(Common.getRelativePath(item.getAbsolutePath()));
				}
				else
				{
					setText(null);
				}
			}
		});
		dialog.getDialogPane().setContent(listView);
		dialog.showAndWait();
	}
}
