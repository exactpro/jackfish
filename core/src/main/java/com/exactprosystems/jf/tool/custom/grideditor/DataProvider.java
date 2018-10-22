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
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.collections.ObservableList;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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

	void fire();

	void setOnChangeListener(BiConsumer<Integer, Integer> consumer);

	void display();

	void displayFunction(Consumer<DataProvider<T>> displayFunction);

	void swapRows(int current, int swapTo);

	void swapColumns(int current, int swapTo);
}
