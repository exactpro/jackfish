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
import com.exactprosystems.jf.tool.git.GitBean;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GitStatusController implements Initializable, ContainingParent
{
	public Parent parent;
	public ListView<GitBean> listView;

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
		Common.tryCatch(() -> this.model.revert(this.listView.getItems().stream().filter(GitBean::isChecked).map(GitBean::getFile).collect(Collectors.toList())), "Error on revert selected items");
	}

	public void init(GitStatus model)
	{
		this.model = model;
	}

	public void display(List<GitBean> list)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setResizable(true);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		dialog.setTitle("Git status");
		Text headerLabel = new Text();
		if (list.isEmpty())
		{
			headerLabel.setText("Already up-to-date");
		}
		BorderPane pane = new BorderPane();
		pane.setCenter(headerLabel);
		dialog.getDialogPane().setHeader(pane);
		dialog.getDialogPane().setContent(this.parent);
		this.listView.getItems().setAll(list);
		dialog.showAndWait();
	}

	private class GitStatusCell extends ListCell<GitBean>
	{
		public GitStatusCell()
		{
			super();
		}

		@Override
		protected void updateItem(GitBean item, boolean empty)
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
				GridPane gridPane = new GridPane();
				ColumnConstraints c0 = new ColumnConstraints();
				c0.setPercentWidth(30);
				ColumnConstraints c1 = new ColumnConstraints();
				c1.setPercentWidth(70);
				gridPane.getColumnConstraints().addAll(c0, c1);
				Label itemStatus = new Label(item.getStatus().getPreffix());
				Label itemFile = new Label(item.getFile().getPath());
				itemFile.getStyleClass().addAll(item.getStatus().getStyleClass());
				itemStatus.getStyleClass().addAll(item.getStatus().getStyleClass());

				gridPane.add(itemStatus, 0, 0);
				gridPane.add(itemFile, 1, 0);
				GridPane.setHalignment(itemStatus, HPos.LEFT);
				GridPane.setHalignment(itemFile, HPos.LEFT);

				pane.setCenter(gridPane);
				setGraphic(pane);
			}
		}
	}
}
