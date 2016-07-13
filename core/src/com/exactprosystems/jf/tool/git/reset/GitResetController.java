////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.reset;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class GitResetController implements Initializable, ContainingParent
{
	public Parent parent;
	public VBox vboxFiles;
	public Label lblCommitMessage;
	public TableView<GitResetBean> tableView;
	private Alert dialog;

	private GitReset model;
	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Common.tryCatch(() -> this.model.select(newValue), "Error on select");
		});
	}
	//endregion

	public void cancel(ActionEvent actionEvent)
	{

	}

	public void reset(ActionEvent actionEvent)
	{

	}

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	//endregion
	public void init(GitReset model, List<GitResetBean> list)
	{
		this.model = model;
		initDialog();
		initTable();
		this.tableView.getItems().clear();
		this.tableView.getItems().addAll(list);
	}

	public void displayMessage(String message)
	{
		this.lblCommitMessage.setText(message);
	}

	public void displayFiles(List<File> files)
	{
		this.vboxFiles.getChildren().clear();
		for (File file : files)
		{
			this.vboxFiles.getChildren().add(new Text(Common.getRelativePath(file.getAbsolutePath())));
		}
	}


	public void show()
	{
		this.dialog.showAndWait();
	}

	public void hide()
	{
		this.dialog.hide();
	}

	//region private methods
	private void initDialog()
	{
		this.dialog = new Alert(Alert.AlertType.INFORMATION);
		this.dialog.setResult(new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE));
		this.dialog.setResizable(true);
		this.dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		this.dialog.setTitle("Git reset");
		this.dialog.getDialogPane().setHeader(new Label());
		this.dialog.getDialogPane().setContent(this.parent);
		ButtonType buttonCreate = new ButtonType("", ButtonBar.ButtonData.OTHER);
		this.dialog.getButtonTypes().setAll(buttonCreate);
		Button button = (Button) this.dialog.getDialogPane().lookupButton(buttonCreate);
		button.setPrefHeight(0.0);
		button.setMaxHeight(0.0);
		button.setMinHeight(0.0);
		button.setVisible(false);
	}

	private void initTable()
	{
		TableColumn<GitResetBean, String> commitColumn = new TableColumn<>("CommitId");
		commitColumn.setCellValueFactory(new PropertyValueFactory<>("commitId"));

		TableColumn<GitResetBean, String> usernameColumn = new TableColumn<>("Username");
		usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

		TableColumn<GitResetBean, Date> dateColumn = new TableColumn<>("Date");
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
		dateColumn.setCellFactory(p -> new TableCell<GitResetBean, Date>(){
			@Override
			protected void updateItem(Date item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(formatter.format(item));
				}
				else
				{
					setText(null);
				}
			}
		});

		this.tableView.getColumns().addAll(dateColumn, commitColumn, usernameColumn);

	}

	private static final SimpleDateFormat formatter = new SimpleDateFormat(Common.DATE_TIME_PATTERN);
	//endregion

}