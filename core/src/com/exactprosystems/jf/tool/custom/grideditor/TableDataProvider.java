////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.functions.Table;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TableDataProvider implements DataProvider<String>
{
	private Table table;

	public TableDataProvider(Table table)
	{
		this.table = table;
	}

	@Override
	public int rowCount()
	{
		return this.table.size();
	}

	@Override
	public int columnCount()
	{
		return this.table.getHeaderSize();
	}

	@Override
	public String getCellValue(int column, int row)
	{
		String name = this.table.getHeader(column);
		Object value = this.table.get(row).get(name);
		if (value == null)
		{
			return defaultValue();
		}
		return "" + value;
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	@Override
	public void setCellValue(int column, int row, String value)
	{
		final String oldCellValue = getCellValue(column, row);
		if (oldCellValue.equals(value))
		{
			return;
		}
		String name = this.table.getHeader(column);
		this.table.changeValue(name, row, value);
	}

	@Override
	public String columnName(int column)
	{
		return this.table.getHeader(column);
	}

	@Override
	public void setColumnName(int column, String name)
	{
		this.table.setHeader(column, name);
	}

	@Override
	public void addColumn(int column, String name)
	{
		this.table.addColumns(column, name);
	}

	@Override
	public void removeColumns(Integer ... columnsIndex)
	{
		this.table.removeColumns(
				Arrays.stream(columnsIndex)
						.map(this.table::getHeader)
						.collect(Collectors.toList())
						.toArray(new String[columnsIndex.length])
		);
	}

	@Override
	public void addRow(int row)
	{
		Object[] ar = new Object[this.columnCount()];
		for (int i = 0; i < ar.length; i++)
		{
			ar[i] = this.defaultValue();
		}
		this.table.addValue(row, ar);
	}

	@Override
	public void removeRow(int row)
	{
		this.table.remove(row);
	}

	@Override
	public ObservableList<ObservableList<SpreadsheetCell>> getRows()
	{
		final ObservableList<ObservableList<SpreadsheetCell>> observableRows = FXCollections.observableArrayList();
		for (int i = 0; i < this.rowCount(); i++)
		{
			ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
			for (int j = 0; j < this.columnCount(); j++)
			{
				Object cellValue = this.getCellValue(j, i);
				list.add(StringCellType.createCell(i, j, String.valueOf(cellValue)));
			}
			observableRows.add(list);
		}
		return observableRows;
	}

	@Override
	public ObservableList<String> getRowHeaders()
	{
		ObservableList<String> strings = FXCollections.observableArrayList();
		for (int i = 0; i < this.rowCount(); i++)
		{
			strings.add(String.valueOf(i));
		}
		return strings;
	}

	@Override
	public ObservableList<String> getColumnHeaders()
	{
		ObservableList<String> strings = FXCollections.observableArrayList();
		for (int i = 0; i < this.columnCount(); i++)
		{
			strings.add(this.columnName(i));
		}
		return strings;
	}

	public Table getTable()
	{
		return table;
	}
}
