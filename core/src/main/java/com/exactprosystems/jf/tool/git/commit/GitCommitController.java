/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.git.commit;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.git.GitBean;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GitCommitController implements Initializable, ContainingParent
{
	public Parent parent;
	public TableView<GitBean> tableView;
	public TextArea taMessage;
	public SplitMenuButton btnCommit;
	public Button btnClose;

	private BooleanBinding binding;
	private GitCommit model;
	private Alert dialog;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.binding = this.taMessage.textProperty().isEmpty();
		this.btnCommit.disableProperty().bind(this.binding);
	}
	//endregion

	public void init(GitCommit model, List<GitBean> list)
	{
		this.model = model;
		initDialog();
		initTable();
		this.tableView.getItems().setAll(list);
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
		if (this.tableView.getItems().stream().filter(GitBean::isChecked).count() == 0)
		{
			DialogsHelper.showInfo(R.GIT_COMMIT_CONTR_SELECT_FILES.get());
			return;
		}
		Common.tryCatch(() -> this.model.commit(this.taMessage.getText(), this.tableView.getItems().stream().filter(GitBean::isChecked).collect(Collectors.toList()), false), "Error on commit");
	}

	public void pushSelected(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.push(this.taMessage.getText(), this.tableView.getItems().stream().filter(GitBean::isChecked).collect(Collectors.toList()), false), "Error on push");
	}

	public void close(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::close, R.GIT_COMMIT_CONTR_ERROR_CLOSE_CANCEL.get());
	}
	//endregion

	public void show()
	{
		this.dialog.show();
	}

	public void hide()
	{
		this.dialog.hide();
	}

	public void setDisable(boolean flag)
	{
		this.btnClose.setText(flag ? R.COMMON_CANCEL.get() : R.COMMON_CLOSE.get());
		if (flag)
		{
			this.btnCommit.disableProperty().unbind();
			this.btnCommit.setDisable(true);
		}
		else
		{
			this.btnCommit.setDisable(this.binding.getValue());
			this.btnCommit.disableProperty().bind(this.binding);

			String oldMsg = this.taMessage.getText();
			this.taMessage.setText("");
			this.taMessage.setText(oldMsg);
		}
		this.tableView.setDisable(flag);
		this.taMessage.setDisable(flag);
	}

	//region private methods
	private void initDialog()
	{
		this.dialog = DialogsHelper.createGitDialog(R.TOOL_COMMIT.get(), this.parent);
	}

	private void initTable()
	{
		this.tableView.setRowFactory(p -> new CustomTableRow());
		TableColumn<GitBean, Boolean> checkedColumn = new TableColumn<>("");
		CheckBox checkBox = new CheckBox();
		checkedColumn.setGraphic(checkBox);
		checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			this.tableView.getItems().forEach(b -> b.setChecked(newValue));
			update();
		});
		checkedColumn.setCellValueFactory(new PropertyValueFactory<>("checked"));
		checkedColumn.setCellFactory(p -> new CheckedTableCell());
		checkedColumn.setPrefWidth(30);
		checkedColumn.setMaxWidth(30);
		checkedColumn.setMinWidth(30);

		TableColumn<GitBean, String> fileColumn = new TableColumn<>(R.COMMON_SHIFT_NAME.get());
		fileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));
		fileColumn.prefWidthProperty().bind(this.tableView.widthProperty().subtract(30.0 + 100.0 + 2.0));

		TableColumn<GitBean, GitBean.Status> statusColumn = new TableColumn<>(R.TOOL_STATUS.get());
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
		statusColumn.setPrefWidth(100);
		statusColumn.setMaxWidth(100);
		statusColumn.setMinWidth(100);

		this.tableView.getColumns().addAll(checkedColumn, fileColumn, statusColumn);
	}

	private void update()
	{
		this.tableView.getColumns().forEach(column -> Common.runLater(() -> {
			column.setVisible(false);
			column.setVisible(true);
		}));
	}
	//endregion

	private class CheckedTableCell extends TableCell<GitBean, Boolean>
	{
		@Override
		protected void updateItem(Boolean item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				CheckBox box = new CheckBox();
				box.setSelected(item);
				box.selectedProperty().addListener((observable, oldValue, newValue) -> {
					TableRow<GitBean> tableRow = getTableRow();
					if (tableRow != null)
					{
						GitBean bean = tableRow.getItem();
						if (bean != null)
						{
							bean.setChecked(newValue);
						}
					}
				});
				setGraphic(box);
			}
			else
			{
				setGraphic(null);
			}
		}
	}

	private class CustomTableRow extends TableRow<GitBean>
	{
		@Override
		protected void updateItem(GitBean item, boolean empty)
		{
			super.updateItem(item, empty);
			this.getStyleClass().removeAll(Arrays.stream(GitBean.Status.values()).map(GitBean.Status::getStyleClass).collect(Collectors.toList()));
			if (item != null && !empty)
			{
				this.getStyleClass().add(item.getStatus().getStyleClass());
			}
		}
	}
}