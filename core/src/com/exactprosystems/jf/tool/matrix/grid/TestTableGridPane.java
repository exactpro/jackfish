////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.matrix.grid;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Table;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TestTableGridPane extends TableView<Map<String, Object>>
{
	private Table table;
	private MatrixItem matrixItem;
	private final static int columnWidth = 100;
	public static final int PREF_TABLE_HEIGHT = 400;

	public TestTableGridPane(MatrixItem matrixItem, Table table)
	{
		this.setPrefHeight(PREF_TABLE_HEIGHT);
		this.matrixItem = matrixItem;
		this.table = table;
		this.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
		initHeaders();
		initBody();
		this.setContextMenu(createContextMenu());
		this.getColumns().addListener((ListChangeListener<TableColumn<Map<String, Object>, ?>>) c -> {
			int size = c.getList().size();
			if (size == 0)
			{
				this.setPrefWidth(columnWidth);
				return;
			}
			this.setPrefWidth(size * columnWidth);
			this.setMinWidth(size * columnWidth - 1);
			this.setMaxWidth(size * columnWidth + 1);
		});
	}

	private ContextMenu createContextMenu()
	{
		ContextMenu menu = new ContextMenu();
		MenuItem addCol = new MenuItem("Add column");
		addCol.setOnAction(event -> addColumn());
		MenuItem addRow = new MenuItem("Add row");
		addRow.setOnAction(event -> addRow());

		MenuItem removeCol = new MenuItem("Remove column");
		removeCol.setOnAction(event -> removeColumn());
		MenuItem removeRow = new MenuItem("Remove row");
		removeRow.setOnAction(event -> removeRow());
		menu.getItems().addAll(addCol, addRow, removeCol, removeRow);
		return menu;
	}

	private void initHeaders()
	{
		int headerSize = this.table.getHeaderSize();
		for (int i = 0; i < headerSize; i++)
		{
			String header = this.table.getHeader(i);
			CustomColumn column = new CustomColumn(header, this.table);
			this.getColumns().add(column);
		}
	}

	private void initBody()
	{
		for (Map<String, Object> row : this.table)
		{
			this.getItems().add(row);
		}
	}

	private void removeRow()
	{
		if (this.getItems().size() == 0)
		{
			return;
		}
		int index = this.getItems().size() - 1;
		this.table.removeRow(index);
		this.getItems().remove(index);
		updateItems();
	}

	private void addRow()
	{
		if (this.getColumns().size() == 0)
		{
			return;
		}
		HashMap<String, Object> newRow = new HashMap<>();
		this.table.addValue(-1, newRow);
		this.getItems().add(newRow);
		updateItems();
	}

	private void removeColumn()
	{
		if (this.getColumns().size() == 0)
		{
			return;
		}
		TableColumn<Map<String, Object>, ?> column = this.getColumns().remove(this.getColumns().size() - 1);
		this.table.removeColumns(column.getText());
		updateItems();
	}

	private void addColumn()
	{
		String nameNewColumn = "newColumn";
		this.table.addColumns(nameNewColumn);
		this.getColumns().add(new CustomColumn(nameNewColumn, this.table));
		updateItems();
	}

	private void updateItems()
	{
		this.getItems().clear();
		for (Map<String, Object> row : this.table)
		{
			this.getItems().add(row);
		}
		this.focusModelProperty().getValue().focus(this.getItems().size() - 1, this.getColumns().get(0));
		this.focusModelProperty().getValue().focusNext();
		TableColumn<Map<String, Object>, ?> firstColumn = this.getColumns().get(0);
		Object cellData = firstColumn.getCellData(this.getItems().get(getItems().size() - 1));
		TextField newTextField = (TextField) cellData;
		((TextField) cellData).requestFocus();
	}

	private class CustomColumn extends TableColumn<Map<String, Object>, TextField>
	{
		public CustomColumn(String name, Table table)
		{
			super(name);
			setMaxWidth(columnWidth + 1);
			setPrefWidth(columnWidth);
			setMinWidth(columnWidth - 1);
			this.setSortable(false);
			this.setCellValueFactory(param -> {
				Object value = param.getValue().get(getText());
				String text = Str.asString(value);
				TextField textField = new TextField(text);
				return new SimpleObjectProperty<>(textField);
			});
			this.setCellFactory(param -> new CustomTableCell(table));
			this.setContextMenu(createContextMenu(table));
		}

		private ContextMenu createContextMenu(Table table)
		{
			ContextMenu menu = new ContextMenu();
			MenuItem rename = new MenuItem("Rename");
			rename.setOnAction(event -> {
				Dialog<String> dialog = new TextInputDialog();
				dialog.setHeaderText("Enter new name");
				dialog.setTitle("Rename");
				Optional<String> result = dialog.showAndWait();
				result.ifPresent(newName -> {
					this.setText(newName);
					int index = this.getTableView().getColumns().indexOf(this);
					table.setHeader(index, newName);
				});
			});
			menu.getItems().add(rename);
			return menu;
		}
	}

	private class CustomTableCell extends TableCell<Map<String, Object>, TextField>
	{
		private String oldString = "";
		private Table table;

		public CustomTableCell(Table table)
		{
			this.table = table;
		}

		@Override
		protected void updateItem(TextField tf, boolean b)
		{
			super.updateItem(tf, b);
			if (b || tf == null)
			{
				setGraphic(null);
			}
			else
			{
				setGraphic(tf);
				tf.focusedProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue && !oldValue)
					{
						oldString = tf.getText();
					}
					if (!newValue && oldValue && !Str.areEqual(oldString, tf.getText()))
					{
						commitEdit(tf);
						String columnName = Str.asString(getTableColumn().getText());
						this.table.changeValue(columnName, this.getIndex(),tf.getText());
					}
				});
				tf.setOnAction(event -> tf.requestFocus());
				tf.setOnKeyPressed(event -> {
					if (event.getCode() == KeyCode.TAB)
					{
						TableColumn<Map<String, Object>, TextField> currentColumn = this.getTableColumn();

						int indexColumn = this.getTableView().getColumns().indexOf(currentColumn);
						int rowIndex = this.getIndex();

						if (rowIndex == this.getTableView().getItems().size() - 1 && indexColumn == this.getTableView().getColumns().size() - 1)
						{
							((TestTableGridPane) this.getTableView()).addRow();
						}
					}
				});
			}
		}
	}
}
