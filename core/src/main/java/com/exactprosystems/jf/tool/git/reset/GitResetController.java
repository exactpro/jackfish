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
package com.exactprosystems.jf.tool.git.reset;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

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
			Common.tryCatch(() -> this.model.select(newValue), R.GIT_RESET_CONTR_ERROR_ON_SELECT.get());
		});
	}
	//endregion

	public void cancel(ActionEvent actionEvent)
	{
		this.hide();
	}

	public void reset(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.reset(this.tableView.getSelectionModel().getSelectedItem()), R.GIT_RESET_CONTR_ERROR_ON_RESET.get());
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

	public void displayFiles(List<FileWithStatusBean> files)
	{
		this.vboxFiles.getChildren().clear();
		for (FileWithStatusBean bean : files)
		{
			Text e = new Text(bean.getChangeType().name().charAt(0) +"\t"+ Common.getRelativePath(bean.getFile().getAbsolutePath()));
			e.setFill(bean.getColor());
			this.vboxFiles.getChildren().add(e);
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
		this.dialog = DialogsHelper.createGitDialog(R.GIT_RESET_CONTR_INIT_DIALOG_TITLE.get(), this.parent);
	}

	private void initTable()
	{
		TableColumn<GitResetBean, String> commitColumn = new TableColumn<>(R.GIT_RESET_CONTR_COLUMN_ID.get());
		commitColumn.setCellValueFactory(new PropertyValueFactory<>("commitId"));

		TableColumn<GitResetBean, String> usernameColumn = new TableColumn<>(R.GIT_RESET_CONTR_USERNAME.get());
		usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

		TableColumn<GitResetBean, Date> dateColumn = new TableColumn<>(R.GIT_RESET_CONTR_DATE.get());
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