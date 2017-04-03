////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.collections.ObservableList;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface DataProvider<T>
{
	int rowCount();

	int columnCount();

	T getCellValue(int column, int row);

	T defaultValue();

	String columnName(int column);

	void setCellValue(int column, int row, T value);

	void clearCells(List<Point> points);

	void updateCells(Map<Point, String> values);

	void setColumnName(int column, String name);

	void setColumnValues(int columnIndex, T[] values);

	void paste(int column, int row, boolean withHeaders);

	void addNewColumn(int columnIndex);

	void removeColumns(Integer[] columnsIndex);

	void addRow(int row);

	void removeRows(Integer[] rowsIndexes);

	void extendsTable(int prefCol, int prefRow);

	ObservableList<ObservableList<SpreadsheetCell>> getRows();

	ObservableList<String> getRowHeaders();

	ObservableList<String> getColumnHeaders();

	void display();

	void displayFunction(Consumer<DataProvider<T>> displayFunction);
}
