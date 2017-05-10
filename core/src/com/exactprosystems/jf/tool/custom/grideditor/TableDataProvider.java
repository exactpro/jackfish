////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableDataProvider implements DataProvider<String>
{
	private Table                          table;
	private BiConsumer<Command, Command>   undoRedoFunction;
	private Consumer<DataProvider<String>> displayFunction;

	//region Constructors
	public TableDataProvider(Table table, BiConsumer<Command, Command> undoRedoFunction)
	{
		this.table = table;
		this.undoRedoFunction = undoRedoFunction;
	}
	//endregion

	//region Interface DataProvider
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
	public String columnName(int column)
	{
		return this.table.getHeader(column);
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

	//region undo/redo functionality

	@Override
	public void setColumnName(int column, String name)
	{
		String oldHeaderName = this.table.getHeader(column);
		if (Str.areEqual(oldHeaderName, name))
		{
			return;
		}
		if (this.table.columnIsPresent(name))
		{
			DialogsHelper.showError(String.format("Column with name %s already present", name));
			return;
		}
		Command undo = () ->
		{
			this.table.setHeader(column, oldHeaderName);
			display();
		};

		Command redo = () ->
		{
			this.table.setHeader(column, name);
			display();
		};

		this.undoRedoFunction.accept(undo, redo);
	}

	public void setColumnValues(int columnIndex, String[] values)
	{
		String headerName = this.table.getHeader(columnIndex);
		List<String> oldValues = this.table.stream()
				.map(rowTable -> rowTable.get(headerName))
				.map(String::valueOf)
				.collect(Collectors.toList());

		String[] oldValuesArray = oldValues.toArray(new String[oldValues.size()]);

		Command undo = () ->
		{
			IntStream.range(0, this.table.size()).forEach(i -> this.table.changeValue(headerName, i, oldValuesArray[i]));
			display();
		};

		Command redo = () ->
		{
			IntStream.range(0, this.table.size()).forEach(i -> this.table.changeValue(headerName, i, values[i]));
			display();
		};

		this.undoRedoFunction.accept(undo, redo);
	}

	@Override
	public void addNewColumn(int columnIndex)
	{
		String newColumnName = Table.generateColumnName(this.table);
		Command undo = () ->
		{
			this.table.removeColumns(newColumnName);
			display();
		};

		Command redo = () ->
		{
			this.table.addColumns(columnIndex, newColumnName);
			display();
		};

		this.undoRedoFunction.accept(undo, redo);

	}

	@Override
	public void addRow(int row)
	{
		Command undo = () ->
		{
			this.table.removeRow(row);
			display();
		};

		Command redo = () ->
		{
			this.table.addValue(row,
					IntStream.range(0, this.columnCount())
							.mapToObj(i -> this.defaultValue())
							.toArray()
			);
			display();
		};

		this.undoRedoFunction.accept(undo, redo);
	}

	@Override
	public void removeRows(Integer[] rowsIndexes)
	{
		if (rowsIndexes.length == this.table.size())
		{
			DialogsHelper.showError("Can't remove all rows from table");
			return;
		}

		Map<Integer, RowTable> oldValues = Arrays.stream(rowsIndexes).collect(Collectors.toMap(i -> i, i -> this.table.get(i)));

		Command undo = () ->
		{
			for (Integer rowsIndex : rowsIndexes)
			{
				RowTable removedRow = oldValues.get(rowsIndex);
				this.table.add(rowsIndex, removedRow);
			}
			display();
		};

		Command redo = () ->
		{
			for (int i = rowsIndexes.length - 1; i >= 0; i--)
			{
				this.table.removeRow(rowsIndexes[i]);
			}
			display();
		};

		this.undoRedoFunction.accept(undo, redo);
	}

	@Override
	public void removeColumns(Integer... columnsIndex)
	{
		Map<String, List<String>> oldValues = new LinkedHashMap<>();
		Arrays.stream(columnsIndex)
				.map(this.table::getHeader)
				.forEach(header -> oldValues.put(header, this.table.stream()
						.map(rt -> rt.get(header))
						.map(String::valueOf)
						.collect(Collectors.toList())
				));

		Map<Integer, String> map = Arrays.stream(columnsIndex).collect(Collectors.toMap(Function.identity(), this.table::getHeader));

		Command undo = () ->
		{
			for (Map.Entry<Integer, String> entry : map.entrySet())
			{
				Integer key = entry.getKey();
				String value = entry.getValue();
				//revert removed headers
				this.table.addColumns(key, value);

				//revert removed values
				List<String> values = oldValues.get(value);
				IntStream.range(0, this.table.size())
						.forEach(i -> {
							RowTable rowTable = this.table.get(i);
							rowTable.put(value, values.get(i));
						});
			}
			display();
		};

		Command redo = () ->
		{
			this.table.removeColumns(Arrays.stream(columnsIndex)
					.map(this.table::getHeader)
					.collect(Collectors.toList())
					.toArray(new String[columnsIndex.length]));
			display();
		};

		this.undoRedoFunction.accept(undo, redo);
	}

	@Override
	public void setCellValue(int column, int row, String newValue)
	{
		final String oldValue = getCellValue(column, row);
		if (Str.areEqual(oldValue, newValue))
		{
			return;
		}
		String headerName = this.table.getHeader(column);
		Command undo = () ->
		{
			this.table.changeValue(headerName, row, oldValue);
			display();
		};

		Command redo = () ->
		{
			this.table.changeValue(headerName, row, newValue);
			display();
		};

		this.undoRedoFunction.accept(undo, redo);
	}

	@Override
	public void clearCells(List<Point> points)
	{
		this.updateCells(points.stream().collect(Collectors.toMap(Function.identity(), point -> this.defaultValue())));
	}

	@Override
	public void updateCells(Map<Point, String> values)
	{
		Set<Point> points = values.keySet();
		Map<Point, String> oldValues = points.stream()
				.collect(Collectors.toMap(Function.identity(), point -> this.getCellValue(point.x, point.y)));

		Command undo = () ->
		{
			points.forEach(point -> {
				String headerName = this.table.getHeader(point.x);
				this.table.changeValue(headerName, point.y, oldValues.get(point));
			});
			display();
		};

		Command redo = () ->
		{
			points.forEach(point -> {
				String headerName = this.table.getHeader(point.x);
				this.table.changeValue(headerName, point.y, values.get(point));
			});
			display();
		};

		this.undoRedoFunction.accept(undo, redo);
	}

	@Override
	public void paste(int column, int row, boolean withHeaders)
	{
		String text = Sys.getFromClipboard();

		//save old table
		Table oldTable = Table.emptyTable();
		oldTable.fillFromTable(this.table);

		Command undo = () ->
		{
			this.table.fillFromTable(oldTable);
			display();
		};

		Command redo = () ->
		{
			String[] rows = text.split(System.lineSeparator());

			/*
			  add columns if needs
			 */
			int columnCount = rows[0].split("\t").length;
			int currentColumnCount = this.table.getHeaderSize();
			if (column + columnCount > currentColumnCount)
			{
				int addSize = column + columnCount - currentColumnCount;
				this.table.addColumns(
						IntStream.range(0, addSize)
								.mapToObj(i -> Table.generateColumnName(this.table))
								.collect(Collectors.toList())
								.toArray(new String[addSize])
				);
			}

			if (withHeaders)
			{
				String[] headers = rows[0].split("\t");
				IntStream.range(0, columnCount).forEach(i -> this.table.setHeader(i + column, headers[i]));

				/*
				 * remove 1st line from rows, because we used this string yet
				 */
				String[] newRows = new String[rows.length - 1];
				for (int i = 1; i < rows.length; i++)
				{
					newRows[i - 1] = rows[i];
				}
				rows = newRows;
			}

			/*
			 * add rows if needs
			 */
			int rowCount = rows.length;
			int rowsSize = this.table.size();
			if (row + rowCount > rowsSize)
			{
				int addSize = row + rowCount - rowsSize;

				Supplier<Object[]> getter = () ->
						IntStream.range(0, this.columnCount())
								.mapToObj(i -> this.defaultValue())
								.toArray();

				IntStream.range(0, addSize).forEach(value -> this.table.addValue(getter.get()));
			}

			for (int i = 0; i < rows.length; i++)
			{
				String[] cells = rows[i].split("\t");
				for (int j = 0; j < cells.length; j++)
				{
					String headerName = this.table.getHeader(column + j);
					this.table.changeValue(headerName, row + i, cells[j]);
				}
			}

			display();
		};

		this.undoRedoFunction.accept(undo, redo);
	}

	@Override
	public void extendsTable(int prefCol, int prefRow)
	{
		Table oldTable = Table.emptyTable();
		oldTable.fillFromTable(this.table);

		Command undo = () ->
		{
			this.table.fillFromTable(oldTable);
			display();
		};

		Command redo = () ->
		{
			if (prefCol < this.table.getHeaderSize() || prefRow < this.table.size())
			{
				List<String> collect = IntStream.range(prefCol, this.table.getHeaderSize())
						.mapToObj(this.table::getHeader)
						.collect(Collectors.toList());

				this.table.removeColumns(collect.toArray(new String[collect.size()]));

				for(int i = this.table.size() - 1; i >= prefRow; i--)
				{
					this.table.removeRow(i);
				}
			}
			else
			{
				int addedColumns = prefCol - this.table.getHeaderSize();
				this.table.addColumns(
						IntStream.range(0, addedColumns)
								.mapToObj(i -> Table.generateColumnName(this.table))
								.collect(Collectors.toList())
								.toArray(new String[addedColumns])
				);

				int addedRows = prefRow - this.table.size();
				IntStream.range(0, addedRows)
						.mapToObj(i -> IntStream.range(0, this.table.getHeaderSize())
								.mapToObj(j -> this.defaultValue())
								.collect(Collectors.toList())
						)
						.map(list -> list.toArray(new String[list.size()]))
						.forEach(array -> this.table.addValue(array));
			}

			display();
		};

		this.undoRedoFunction.accept(undo, redo);
	}

	//endregion

	//endregion

	public void setTable(Table table)
	{
		this.table = table;
	}

	public void display()
	{
		Optional.ofNullable(this.displayFunction).ifPresent(df -> df.accept(this));
	}

	public void displayFunction(Consumer<DataProvider<String>> displayFunction)
	{
		this.displayFunction = displayFunction;
	}
}
