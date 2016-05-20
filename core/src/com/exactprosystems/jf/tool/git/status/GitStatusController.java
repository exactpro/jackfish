////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.status;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GitStatusController implements Initializable, ContainingParent
{
	public Parent parent;
	public ListView<GitStatusBean> listView;

	private GitStatus model;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.listView.setCellFactory(p -> new GitStatusCell());
	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	public void revertSelected(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.revert(this.listView.getItems()
				.stream()
				.filter(GitStatusBean::isChecked)
				.map(GitStatusBean::getFile)
				.collect(Collectors.toList())
		), "Error on revert selected items");
	}

	public void init(GitStatus model)
	{
		this.model = model;
	}

	public void display(List<GitStatusBean> list)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setResizable(true);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		dialog.setTitle("Git status");
		dialog.getDialogPane().setHeader(new Label());
		dialog.getDialogPane().setContent(this.parent);
		this.listView.getItems().setAll(list);
		dialog.showAndWait();
	}

	private class GitStatusCell extends ListCell<GitStatusBean>
	{
		public GitStatusCell()
		{
			super();
		}

		@Override
		protected void updateItem(GitStatusBean item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				BorderPane pane = new BorderPane();
				CheckBox box = new CheckBox();
				box.selectedProperty().addListener((observable, oldValue, newValue) -> {
					item.setChecked(newValue);
				});
				pane.setLeft(box);
				HBox hBox = new HBox();

				pane.setCenter(hBox);
				setGraphic(pane);
			}
		}
	}
}
