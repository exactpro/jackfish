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
package com.exactprosystems.jf.tool.git.merge;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GitMergeController implements Initializable, ContainingParent
{
	public Parent parent;
	private GitMerge model;

	private Alert dialog;
	public TableView<GitMergeBean> tableView;


	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	//endregion
	public void init(GitMerge model, List<GitMergeBean> collect)
	{
		this.model = model;
		initDialog();
		initTable();
		this.tableView.getItems().addAll(collect);
	}

	public void show()
	{
		this.dialog.setOnShown(e -> this.tableView.getSelectionModel().select(0));
		this.dialog.showAndWait();
	}


	public void removeBean(GitMergeBean bean)
	{
		this.tableView.getItems().remove(bean);
	}

	//region actions methods
	public void acceptTheirs(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.acceptTheirs(this.tableView.getSelectionModel().getSelectedItem()), R.GIT_MERGE_CONTR_ACCEPT_THEIRS.get());
	}

	public void acceptYours(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.acceptYours(this.tableView.getSelectionModel().getSelectedItem()), R.GIT_MERGE_CONTR_ACCEPT_YOURS.get());
	}

	public void merge(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.merge(this.tableView.getSelectionModel().getSelectedItem()), R.GIT_MERGE_CONTR_ERROR_ON_MERGE.get());
	}

	public void close(ActionEvent actionEvent)
	{
		this.dialog.hide();
	}
	//endregion

	//region private methods
	private void initDialog()
	{
		this.dialog = DialogsHelper.createGitDialog(R.GIT_MERGE_CONTR_RESOLVE_CONFLICTS.get(), this.parent);
	}

	private void initTable()
	{
		this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		TableColumn<GitMergeBean, String> nameColumn = new TableColumn<>(R.COMMON_SHIFT_NAME.get());
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

		this.tableView.getColumns().add(nameColumn);
	}
	//endregion
}