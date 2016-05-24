////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.commit;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.git.GitBean;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class GitCommitController implements Initializable, ContainingParent
{
	public Parent parent;
	public TableView<GitBean> tableView;
	public TextArea taMessage;
	public Button btnCommit;
	public Button btnPush;
	public Button btnClose;

	private GitCommit model;
	private Alert dialog;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

	}
	//endregion

	public void init(GitCommit model)
	{
		this.model = model;
		initDialog();
		initTable();
	}

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	//region event methods
	public void commitSelected(ActionEvent actionEvent)
	{

	}

	public void pushSelected(ActionEvent actionEvent)
	{

	}

	public void close(ActionEvent actionEvent)
	{
		this.hide();
	}
	//endregion

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
		this.dialog.setTitle("Clone project");
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
		TableColumn<GitBean, Boolean> checkedColumn = new TableColumn<>("");
		checkedColumn.setCellValueFactory(new PropertyValueFactory<>("checked"));
		checkedColumn.setPrefWidth(30);
		checkedColumn.setMaxWidth(30);
		checkedColumn.setMinWidth(30);

		TableColumn<GitBean, String> fileColumn = new TableColumn<>("Name");
		fileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));
		fileColumn.prefWidthProperty().bind(this.tableView.widthProperty().subtract(30.0 + 100.0));

		TableColumn<GitBean, GitBean.Status> statusColumn = new TableColumn<>("Status");
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
		statusColumn.setPrefWidth(100);
		statusColumn.setMaxWidth(100);
		statusColumn.setMinWidth(100);

		this.tableView.getColumns().addAll(checkedColumn, fileColumn, statusColumn);
	}

	//endregion
}