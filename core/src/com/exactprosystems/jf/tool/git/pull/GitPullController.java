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
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
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
	public ComboBox<String> cbBranches;

	private Alert dialog;
	private GitPull model;
	private CredentialBean credential;
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

	public void init(GitPull model, CredentialBean credential)
	{
		this.model = model;
		this.credential = credential;
		initDialog();
		initTable();
	}

	void displayBranches(List<String> list, String remoteBranch)
	{
		this.cbBranches.getItems().setAll(list);
		if (remoteBranch != null)
		{
			int index = this.cbBranches.getItems().indexOf(remoteBranch);
			this.cbBranches.getSelectionModel().select(index == -1 ? 0 : index);
		}

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
		this.dialog.show();
	}

	public void pull(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.pull(this.progressMonitor, this.cbBranches.getSelectionModel().getSelectedItem()), "Error on pulling");
	}

	public void startPulling()
	{
		this.vbox.setVisible(true);
		this.vbox.getChildren().add(new Text("Start pulling..."));
	}

	public void endPulling(String text)
	{
		this.vbox.getChildren().clear();
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
		this.dialog = DialogsHelper.createGitDialog("Pulling", this.parent);
		this.progressMonitor = new VBoxProgressMonitor(this.vbox);
	}

	private void initTable()
	{
		TableColumn<GitPullBean, String> nameColumn = new TableColumn<>("File name");
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
		nameColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.6));

		TableColumn<GitPullBean, Boolean> mergeColumn = new TableColumn<>("Merge");
		mergeColumn.setCellValueFactory(new PropertyValueFactory<>("needMerge"));
		mergeColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.4));
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
						SplitMenuButton menuButton = new SplitMenuButton();
						menuButton.setText("Need merge");
						MenuItem acceptTheirs = new MenuItem("Accept Theirs");
						MenuItem acceptYours = new MenuItem("Accept Yours");
						MenuItem merge = new MenuItem("Merge");

						menuButton.getItems().addAll(acceptTheirs, acceptYours, merge);

						acceptTheirs.setOnAction(e -> Common.tryCatch(() -> {
							GitPullBean pullBean = (GitPullBean) getTableRow().getItem();
							GitUtil.mergeTheirs(credential, pullBean.getFileName());
							pullBean.resolve();
							refresh();
						}, "Error on accept theirs"));

						acceptYours.setOnAction(e -> Common.tryCatch(() -> {
							GitPullBean pullBean = (GitPullBean) getTableRow().getItem();
							GitUtil.mergeYours(credential, pullBean.getFileName());
							pullBean.resolve();
							refresh();
						}, "Error on accept ours"));

						menuButton.setOnAction(e -> DialogsHelper.showInfo("Merge not implemented yet. Merge yourself"));
						setGraphic(menuButton);
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

	private void refresh()
	{
		this.tableView.getColumns().forEach(c -> Platform.runLater(() -> {
			c.setVisible(false);
			c.setVisible(true);
		}));

	}
	//endregion
}