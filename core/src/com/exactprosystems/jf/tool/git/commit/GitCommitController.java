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
	public Button btnCommit;
	public Button btnPush;
	public Button btnClose;

	private BooleanBinding binding;
	private GitCommit model;
	private Alert dialog;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		//TODO set visible true when we understand how to get commit
		this.btnCommit.setVisible(false);
		this.binding = this.taMessage.textProperty().isEmpty();
		this.btnPush.disableProperty().bind(this.binding);
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
		Common.tryCatch(() -> this.model.commit(this.taMessage.getText(), this.tableView.getItems().stream().filter(GitBean::isChecked).collect(Collectors.toList())), "Error on commit");
	}

	public void pushSelected(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.push(this.taMessage.getText(), this.tableView.getItems().stream().filter(GitBean::isChecked).collect(Collectors.toList())), "Error on push");
	}

	public void close(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::close, "Error on close/cancel");
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

	public void setDisable(boolean flag)
	{
		this.btnClose.setText(flag ? "Cancel" : "Close");
		if (flag)
		{
			this.btnPush.disableProperty().unbind();
			this.btnCommit.disableProperty().unbind();

			this.btnPush.setDisable(true);
			this.btnCommit.setDisable(true);
		}
		else
		{
			this.btnPush.setDisable(this.binding.getValue());
			this.btnCommit.setDisable(this.binding.getValue());

			this.btnPush.disableProperty().bind(this.binding);
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
		this.dialog = new Alert(Alert.AlertType.INFORMATION);
		this.dialog.setResult(new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE));
		this.dialog.setResizable(true);
		this.dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		this.dialog.setTitle("Commit");
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

		TableColumn<GitBean, String> fileColumn = new TableColumn<>("Name");
		fileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));
		fileColumn.prefWidthProperty().bind(this.tableView.widthProperty().subtract(30.0 + 100.0 + 5.0));

		TableColumn<GitBean, GitBean.Status> statusColumn = new TableColumn<>("Status");
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
		statusColumn.setPrefWidth(100);
		statusColumn.setMaxWidth(100);
		statusColumn.setMinWidth(100);

		this.tableView.getColumns().addAll(checkedColumn, fileColumn, statusColumn);
	}

	private void update()
	{
		this.tableView.getColumns().forEach(column -> Platform.runLater(() -> {
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
			this.getStyleClass().removeAll(Arrays.asList(GitBean.Status.values()).stream().map(GitBean.Status::getStyleClass).collect(Collectors.toList()));
			if (item != null && !empty)
			{
				this.getStyleClass().add(item.getStatus().getStyleClass());
			}
		}
	}
}