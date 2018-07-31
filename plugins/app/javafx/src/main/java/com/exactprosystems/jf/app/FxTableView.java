/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.app.PluginInfo;
import com.exactprosystems.jf.api.app.ValueAndColor;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.app.TableOutOfBoundsException;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class FxTableView
{
	static        Logger       logger;
	private final TableView<?> tableView;
	private final int          rowCount;
	private final int          colCount;

	FxTableView(TableView<?> tableView)
	{
		this.tableView = tableView;

		this.rowCount = tableView.getItems().size();
		this.colCount = tableView.getColumns().size();
	}

	//region public methods
	int size()
	{
		return this.rowCount;
	}

	String[][] getTable(String[] headers)
	{
		String[][] resultTable = new String[this.rowCount + 1][this.colCount];
		ObservableList<? extends TableColumn<?, ?>> tableColumns = this.tableView.getColumns();

		this.fillHeaders(resultTable, headers);
		IntStream.range(0, this.rowCount).forEach(row ->
				IntStream.range(0, tableColumns.size())
						.forEach(col -> resultTable[row + 1][col] = this.getCellValue(col, row)));
		return resultTable;
	}

	String getCellValue(int columnIndex, int rowIndex)
	{
		this.checkTable(columnIndex, rowIndex);
		Node cell = this.findCell(columnIndex, rowIndex);
		String cellValue = this.getCellValue(cell);
		if (cellValue != null)
		{
			return cellValue;
		}
		return this.getStringCellValue(columnIndex, rowIndex);
	}

	/**
	 * Return node by passed row and column indexes.
	 *
	 * @param columnIndex for finding cell. If column index is {@link Integer#MIN_VALUE} - will return TableRow
	 * @param rowIndex    for finding cell.
	 * @return Node
	 */
	public Node findCell(int columnIndex, int rowIndex)
	{
		if (columnIndex == Integer.MIN_VALUE)
		{
			if (this.rowCount < rowIndex)
			{
				throw new TableOutOfBoundsException(String.format(R.FX_TABLE_VIEW_TOOB_EXCEPTION.get(), rowIndex, tableView.getItems().size()));
			}
			Node cell = UtilsFx.runOnFxThreadAndWaitResult(() -> {
				this.tableView.scrollToColumnIndex(0);
				this.tableView.scrollTo(rowIndex);
				return (Node) this.tableView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN, rowIndex, 0);
			});
			if (cell instanceof TableCell)
			{
				return ((TableCell) cell).getTableRow();
			}

			SimpleRow tableRow = new SimpleRow();
			for (int i = 0; i < this.colCount; i++)
			{
				int finalI = i;
				tableRow.add(UtilsFx.runOnFxThreadAndWaitResult(() -> {
					this.tableView.scrollToColumnIndex(finalI);
					this.tableView.scrollTo(rowIndex);
					return (Node) this.tableView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN, rowIndex, finalI);
				}));
			}
			return tableRow;
		}
		else
		{
			this.checkTable(columnIndex, rowIndex);
			Node cell = UtilsFx.runOnFxThreadAndWaitResult(() -> {
				try
				{
					logger.debug(String.format("Start scroll to column %s", columnIndex));
					this.tableView.scrollToColumnIndex(columnIndex);
					logger.debug(String.format("Start scroll to row %s", rowIndex));
					this.tableView.scrollTo(rowIndex);
					logger.debug("Start getting cell");
					return (Node) tableView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN, rowIndex, columnIndex);
				}
				catch (Exception e)
				{
					logger.error(String.format("findCell(%s,%s,%s)", tableView, columnIndex, rowIndex));
					logger.error(e.getMessage(), e);
				}
				return null;
			});
			logger.debug(String.format("Found cell : %s", cell));
			return cell;
		}
	}

	Map<String, String> getRow(String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		if (valueCondition == null)
		{
			throw new Exception(R.FX_TABLE_VIEW_GET_ROW_NULL_CONDITION.get());
		}
		List<String> tableHeaders = this.getTableHeaders(columns);
		List<Map<String, Object>> listOfRows = IntStream.range(0, this.rowCount)
				.mapToObj(row -> IntStream.range(0, tableHeaders.size())
						.boxed()
						.collect(Collectors.toMap(
								tableHeaders::get
								, j -> (Object) this.getCellValue(j, row)
								, (u,v) -> { throw new IllegalStateException(String.format(R.FX_TABLE_VIEW_GET_ROW_DUPLICATE_KEY.get(), u)); }
								, LinkedHashMap::new))
				)
				.filter(valueCondition::isMatched)
				.collect(Collectors.toList());
		if (listOfRows.size() > 1)
		{
			throw new Exception(R.FX_TABLE_VIEW_TOO_MANY_ROWS.get());
		}
		if (listOfRows.isEmpty())
		{
			throw new Exception(R.FX_TABLE_VIEW_NO_ONE_ROWS.get());
		}
		Map<String, String> map = new LinkedHashMap<>();
		listOfRows.get(0).forEach((s, o) -> map.put(s, String.valueOf(o)));
		return map;
	}

	List<String> getRowIndexes(String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		if (valueCondition == null)
		{
			throw new Exception(R.FX_TABLE_VIEW_GET_ROW_NULL_CONDITION.get());
		}
		List<String> rowNumbers = new ArrayList<>();
		List<String> tableHeaders = this.getTableHeaders(columns);
		for (int i = 0; i < this.rowCount; i++)
		{
			Map<String, Object> row = new LinkedHashMap<>();
			for (int j = 0; j < tableHeaders.size(); j++)
			{
				String cellValue = this.getCellValue(j, i);
				row.put(tableHeaders.get(j), cellValue);
			}

			if (valueCondition.isMatched(row))
			{
				rowNumbers.add(String.valueOf(i));
			}
		}
		return rowNumbers;
	}

	Map<String, String> getRowByIndex(String[] columns, int rowIndex)
	{
		Map<String, String> result = new LinkedHashMap<>();
		List<String> tableHeaders = this.getTableHeaders(columns);

		for (int j = 0; j < tableHeaders.size(); j++)
		{
			String cellValue = this.getCellValue(j, rowIndex);
			result.put(tableHeaders.get(j), cellValue);
		}
		return result;
	}

	Map<String, ValueAndColor> getRowWithColor(String[] columns, int rowIndex, PluginInfo info) throws Exception
	{
		Map<String, ValueAndColor> result = new LinkedHashMap<>();
		List<String> tableHeaders = this.getTableHeaders(columns);
		for (int column = 0; column < tableHeaders.size(); column++)
		{
			String headerName = tableHeaders.get(column);

			Color backgroundColor = Color.WHITE;
			Color foregroundColor = Color.BLACK;
			Paint fgPaint = null;

			Node nodeTableCell = this.findCell(column, rowIndex);
			if (nodeTableCell instanceof TableCell<?, ?>)
			{
				TableCell tableCell = (TableCell) nodeTableCell;
				backgroundColor = ((TableRow<?>) tableCell.getTableRow()).getBackground().getFills()
						.stream()
						.map(BackgroundFill::getFill)
						.filter(paint -> paint instanceof javafx.scene.paint.Color)
						.map(paint -> (javafx.scene.paint.Color)paint)
						.filter(Objects::nonNull)
						.findFirst()
						.map(UtilsFx::convert)
						.orElse(Color.WHITE);

				if (tableCell.getGraphic() != null)
				{
					//use graphic
					Node tableCellGraphic = tableCell.getGraphic();
					if (tableCellGraphic instanceof Text)
					{
						fgPaint = ((Text) tableCellGraphic).getFill();
					}
					else if (tableCellGraphic instanceof Labeled)
					{
						fgPaint = ((Labeled) tableCellGraphic).getTextFill();
					}
					else
					{
						//try to find all Labeled or Text nodes from current cell and get color
						Locator emptyLocator = new Locator();
						List<EventTarget> allDescendants = new MatcherFx(info, emptyLocator, tableCellGraphic).findAllDescedants();
						for (EventTarget descendant : allDescendants)
						{
							if (descendant instanceof Labeled)
							{
								fgPaint = ((Labeled) descendant).getTextFill();
								break;
							}
							if (descendant instanceof Text)
							{
								fgPaint = ((Text) descendant).getFill();
								break;
							}
						}
					}
				}
				else
				{
					fgPaint = tableCell.getTextFill();
				}
			}

			if (fgPaint instanceof javafx.scene.paint.Color)
			{
				foregroundColor = UtilsFx.convert((javafx.scene.paint.Color) fgPaint);
			}

			String cellValue = this.getCellValue(nodeTableCell);
			if (cellValue == null)
			{
				cellValue = this.getStringCellValue(column, rowIndex);
			}
			result.put(headerName, new ValueAndColor(cellValue, foregroundColor, backgroundColor));
		}
		return result;
	}
	//endregion

	//region private methods

	private void fillHeaders(String[][] array, String[] customHeaders)
	{
		List<String> tableHeaders = this.getTableHeaders(customHeaders);
		IntStream.range(0, Math.min(this.colCount, array[0].length))
				.forEach(i -> array[0][i] = tableHeaders.get(i));
	}

	private String getCellValue(Node cell)
	{
		if (cell instanceof TableCell<?, ?>)
		{
			TableCell tableCell = (TableCell) cell;
			if (tableCell.getGraphic() == null)
			{
				return tableCell.getText();
			}
			else
			{
				return MatcherFx.getText(tableCell.getGraphic());
			}
		}
		return null;
	}

	private String getStringCellValue(int columnIndex, int rowIndex)
	{
		TableColumn<?, ?> tableColumn = this.tableView.getColumns().get(columnIndex);
		ObservableValue<?> cellObservableValue = tableColumn.getCellObservableValue(rowIndex);
		return String.valueOf(cellObservableValue.getValue());
	}

	private void checkTable(int column, int row) throws TableOutOfBoundsException
	{
		if (this.rowCount < row)
		{
			throw new TableOutOfBoundsException(String.format(R.FX_TABLE_VIEW_TOOB_EXCEPTION.get(), row, tableView.getItems().size()));
		}

		if (this.colCount < column)
		{
			throw new TableOutOfBoundsException(String.format(R.FX_TABLE_VIEW_COLUMN_TOOB_EXCEPTION.get(), column, tableView.getColumns().size()));
		}
	}

	private List<String> getTableHeaders(String[] columns)
	{
		List<String> list = new ArrayList<>();
		ObservableList<? extends TableColumn<?, ?>> tableColumns = this.tableView.getColumns();

		for (int i = 0; i < this.colCount; i++)
		{
			String columnName;
			if (columns == null)
			{
				columnName = tableColumns.get(i).getText();
			}
			else
			{
				columnName = i < columns.length ? columns[i] : String.valueOf(i);
			}
			list.add(columnName.replace(' ', '_'));
		}
		return Converter.convertColumns(list);
	}

	//endregion
}
