/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.tool.git.pull;

import com.exactprosystems.jf.api.common.i18n.R;
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
		Common.tryCatch(this.model::close, R.GIT_PULL_CONTR_ERROR_CLOSE_CANCEL.get());
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
		Common.tryCatch(() -> this.model.pull(this.progressMonitor, this.cbBranches.getSelectionModel().getSelectedItem()), R.GIT_PULL_CONTR_ERROR_PULLING.get());
	}

	public void startPulling()
	{
		this.vbox.setVisible(true);
		this.vbox.getChildren().add(new Text(R.GIT_PULL_CONTR_START_PULLING.get()));
	}

	public void endPulling(String text)
	{
		this.vbox.getChildren().clear();
		Common.runLater(() -> {
			Text e = new Text(text);
			e.setFill(Color.GREEN);
			this.vbox.getChildren().add(e);
		});
		this.btnCancel.setText(R.COMMON_CLOSE.get());
	}

	public void displayFiles(List<GitPullBean> list)
	{
		this.tableView.getItems().addAll(FXCollections.observableList(list));
	}

	//region private methods
	private void initDialog()
	{
		this.dialog = DialogsHelper.createGitDialog(R.GIT_PULL_CONTR_PULLING.get(), this.parent);
		this.progressMonitor = new VBoxProgressMonitor(this.vbox);
	}

	private void initTable()
	{
		TableColumn<GitPullBean, String> nameColumn = new TableColumn<>(R.GIT_PULL_CONTR_FILE_NAME.get());
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
		nameColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.6));

		TableColumn<GitPullBean, Boolean> mergeColumn = new TableColumn<>(R.GIT_PULL_CONTR_MERGE.get());
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
						menuButton.setText(R.GIT_PULL_CONTR_NEED_MERGE.get());
						MenuItem acceptTheirs = new MenuItem(R.GIT_PULL_CONTR_ACCEPT_THEIRS.get());
						MenuItem acceptYours = new MenuItem(R.GIT_PULL_CONTR_ACCEPT_YOURS.get());
						MenuItem merge = new MenuItem(R.GIT_PULL_CONTR_MERGE.get());

						menuButton.getItems().addAll(acceptTheirs, acceptYours, merge);

						acceptTheirs.setOnAction(e -> Common.tryCatch(() -> {
							GitPullBean pullBean = (GitPullBean) getTableRow().getItem();
							GitUtil.mergeTheirs(credential, pullBean.getFileName());
							pullBean.resolve();
							refresh();
						}, R.GIT_PULL_CONTR_ERROR_THEIRS.get()));

						acceptYours.setOnAction(e -> Common.tryCatch(() -> {
							GitPullBean pullBean = (GitPullBean) getTableRow().getItem();
							GitUtil.mergeYours(credential, pullBean.getFileName());
							pullBean.resolve();
							refresh();
						}, R.GIT_PULL_CONTR_ERROR_YOURS.get()));

						merge.setOnAction(e -> Common.tryCatch(() -> {
							model.merge((GitPullBean) getTableRow().getItem());
							refresh();
						}, R.GIT_PULL_CONTR_ERROR_ON_MERGE.get()));

						menuButton.setOnAction(e -> Common.tryCatch(() -> {
							model.merge((GitPullBean) getTableRow().getItem());
							refresh();
						}, R.GIT_PULL_CONTR_ERROR_ON_MERGE.get()));
						setGraphic(menuButton);
					}
					else
					{
						setGraphic(new Label(R.GIT_PULL_CONTR_ALL_OK.get()));
					}
				}
			}
		});
		this.tableView.getColumns().addAll(nameColumn, mergeColumn);
	}

	private void refresh()
	{
		this.tableView.getColumns().forEach(c -> Common.runLater(() -> {
			c.setVisible(false);
			c.setVisible(true);
		}));

	}
	//endregion
}