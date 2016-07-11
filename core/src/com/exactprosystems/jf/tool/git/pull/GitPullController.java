////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.pull;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.git.VBoxProgressMonitor;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GitPullController implements Initializable, ContainingParent
{
	public Parent parent;

	public VBox vbox;
	public Button btnCancel;
	public TableView<GitPullBean> tableView;

	private Alert dialog;
	private GitPull model;
	private VBoxProgressMonitor progressMonitor;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
//		this.tableView.setVisible(false);
		this.vbox.setVisible(false);
	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	public void init(GitPull model)
	{
		this.model = model;
		initDialog();
		initTable();
	}

	//region actions events

	public void close(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::close, "Error on close/cancel");
	}

	//endregion

	public void hide()
	{
		this.dialog.hide();
	}

	public void show()
	{
		this.dialog.setOnShown(event -> Common.tryCatch(() -> this.model.pull(this.progressMonitor), "Error on pulling"));
		this.dialog.showAndWait();
	}

	public void startPulling()
	{
		this.vbox.setVisible(true);
		this.vbox.getChildren().add(new Text("Start pulling..."));
	}

	public void endPulling(String text)
	{
		Platform.runLater(() -> {
			Text e = new Text(text);
			e.setFill(Color.GREEN);
			this.vbox.getChildren().add(e);
		});
		this.btnCancel.setText("Close");
	}

	public void displayFiles(List<GitPullBean> list)
	{
		this.tableView.getItems().addAll(FXCollections.observableList(list));
	}

	//region private methods
	private void initDialog()
	{
		this.dialog = new Alert(Alert.AlertType.INFORMATION);
		this.dialog.setResult(new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE));
		this.dialog.setResizable(true);
		this.dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		this.dialog.setTitle("Pulling");
		this.dialog.getDialogPane().setHeader(new Label());
		this.dialog.getDialogPane().setContent(this.parent);
		ButtonType buttonCreate = new ButtonType("", ButtonBar.ButtonData.OTHER);
		this.dialog.getButtonTypes().setAll(buttonCreate);
		Button button = (Button) this.dialog.getDialogPane().lookupButton(buttonCreate);
		button.setPrefHeight(0.0);
		button.setMaxHeight(0.0);
		button.setMinHeight(0.0);
		button.setVisible(false);

		this.progressMonitor = new VBoxProgressMonitor(this.vbox);
	}

	private void initTable()
	{
		TableColumn<GitPullBean, String> nameColumn = new TableColumn<>("File name");
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
		nameColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.8));

		TableColumn<GitPullBean, Boolean> mergeColumn = new TableColumn<>("Merge");
		mergeColumn.setCellValueFactory(new PropertyValueFactory<>("needMerge"));
		mergeColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.2));
		mergeColumn.setCellFactory(p -> new TableCell<GitPullBean, Boolean>(){
			@Override
			protected void updateItem(Boolean item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty || item == null)
				{
					setGraphic(null);
				}
				else
				{
					if (item)
					{
						Button button = new Button("Need merge");
						button.setOnAction(e -> DialogsHelper.showInfo("Merge not implemented yet. Merge yourself"));
						setGraphic(button);
					}
					else
					{
						setGraphic(new Label("All ok"));
					}
				}
			}
		});
		this.tableView.getColumns().addAll(nameColumn, mergeColumn);
	}

	//endregion
}