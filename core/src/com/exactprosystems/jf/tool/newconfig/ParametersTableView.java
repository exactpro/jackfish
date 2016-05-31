////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.newconfig.nodes.TreeNode;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParametersTableView extends TableView<TablePair>
{
	private TreeNode	editableNode;

	public ParametersTableView()
	{
		this.hide();
		this.setHeightTable(200);
		TableColumn<TablePair, String> keyColumn = new TableColumn<>();
		keyColumn.setText("Parameter");
		keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
		keyColumn.setCellFactory(e -> new TableContextMenuCell());
		TableColumn<TablePair, String> valueColumn = new TableColumn<>();
		valueColumn.setText("Value");
		valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
		valueColumn.setCellFactory(e -> new TableColumnCell());
		valueColumn.setOnEditCommit(e -> Optional.ofNullable(editableNode).ifPresent(node ->
		{
			node.updateParameter(e.getRowValue().getKey(), e.getNewValue());
			this.updateParameters(null);
			this.updateParameters(editableNode.getParameters());
		}));
		valueColumn.setEditable(true);
		this.setEditable(true);

		this.getColumns().addAll(keyColumn, valueColumn);
		this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	public void show()
	{
		this.setDisable(false);
	}

	public void hide()
	{
		this.setDisable(true);
	}

	public void updateParameters(List<TablePair> list)
	{
		if (list == null)
		{
			this.getItems().clear();
		}
		else
		{
			this.getItems().addAll(list);
		}
	}

	public void setEditableNode(TreeNode editableNode)
	{
		this.editableNode = editableNode;
	}

	private void setHeightTable(int value)
	{
		this.setPrefHeight(value);
		this.setMinHeight(value);
		this.setMaxHeight(value);
		this.setOpacity(value == 0 ? 0 : 1);
	}

	private class TableContextMenuCell extends TableCell<TablePair, String>
	{
		public TableContextMenuCell()
		{
		}

		@Override
		protected void updateItem(String item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				setText(item);
			}
			else
			{
				setText(null);
			}
		}
	}

	private class TableColumnCell extends TableContextMenuCell
	{
		private TextField	textField;

		public TableColumnCell()
		{
			super();
		}

		@Override
		public void startEdit()
		{
			super.startEdit();
			TablePair pair = get();
			if (pair != null && !pair.isEditable())
			{
				return;
			}
//			if (this.textField == null)
//			{
				createTextField();
//			}
			this.textField.setText(getString());
			setGraphic(this.textField);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			Platform.runLater(this.textField::requestFocus);
		}

		@Override
		public void cancelEdit()
		{
			super.cancelEdit();
			setText(String.valueOf(getItem()));
			setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		@Override
		protected void updateItem(String s, boolean b)
		{
			super.updateItem(s, b);
			if (b || s == null)
			{
				setText(null);
				setGraphic(null);
			}
			else
			{
				setText(getString());
				setContentDisplay(ContentDisplay.TEXT_ONLY);
				TablePair pair = get();
				if (pair != null)
				{
					String tooltip = s;
					if (pair.getTooltipSeparator() != null)
					{
						tooltip = Arrays.stream(s.split(pair.getTooltipSeparator())).collect(Collectors.joining("\n"));
					}
					setTooltip(new Tooltip(tooltip));
				}
			}
		}

		private String getString()
		{
			return String.valueOf(getItem() == null ? "" : getItem());
		}

		private void createTextField()
		{
			this.textField = new TextField(getString());
			TablePair pair = get();
			if (pair != null && pair.isPath())
			{
				this.textField = new CustomFieldWithButton(getString());
				((CustomFieldWithButton) this.textField).setButtonText("...");
				((CustomFieldWithButton) this.textField).setHandler(e ->
				{
					File file = pair.getPathFunction().get();
					if (file != null)
					{
						needCancel = false;
						commitEdit(Common.getRelativePath(file.getAbsolutePath()));
					}
					else
					{
						cancelEdit();
					}
				});
			}
			else
			{
				this.textField.focusedProperty().addListener((observable, oldValue, newValue) ->
				{
					if (!newValue && oldValue)
					{
						if (needCancel)
						{
							cancelEdit();
							needCancel = true;
						}
					}
				});
			}
			this.textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			this.textField.setOnKeyPressed(t ->
			{
				if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.TAB)
				{
					needCancel = false;
					commitEdit(this.textField.getText());
				}
				else if (t.getCode() == KeyCode.ESCAPE)
				{
					cancelEdit();
				}
			});
		}

		boolean needCancel = true;

		private TablePair get()
		{
			return getTableRow().getItem() != null ? ((TablePair) getTableRow().getItem()) : null;
		}
	}
}
