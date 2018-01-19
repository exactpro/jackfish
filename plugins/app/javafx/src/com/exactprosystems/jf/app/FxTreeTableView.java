////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FxTreeTableView
{
	static        Logger           logger;
	private final TreeTableView<?> treeTableView;
	private final TreeItem<?>      root;

	private final int rowCount;
	private final int colCount;

	public FxTreeTableView(TreeTableView<?> treeTableView)
	{
		this.treeTableView = treeTableView;
		this.root = this.treeTableView.getRoot();

		this.rowCount = this.treeTableView.getExpandedItemCount();
		this.colCount = this.treeTableView.getColumns().size();
	}

	//region public methods
	public int size()
	{
		return this.rowCount;
	}

	String[][] getTable(String[] headers)
	{
		String[][] resultTable = new String[this.rowCount + 1][this.colCount];
		ObservableList<? extends TreeTableColumn<?, ?>> tableColumns = this.treeTableView.getColumns();

		this.fillHeaders(resultTable, headers);
		IntStream.range(0, this.rowCount).forEach(row -> IntStream.range(0, tableColumns.size()).forEach(col -> resultTable[row + 1][col] = this.getCellValue(col, row)));
		return resultTable;
	}

	public String getCellValue(int column, int row)
	{
		this.checkTreeTable(column, row);
		Node cell = this.findCell(column, row);
		String cellValue = this.getCellValue(cell);
		if (cellValue != null)
		{
			return cellValue;
		}
		return this.getStringCellValue(column, row);
	}

	/**
	 * Return node by passed row and column indexes.
	 *
	 * @param colIndex for finding cell. If column index is {@link Integer#MIN_VALUE} - will return TreeTableRow
	 * @param rowIndex for finding cell.
	 * @return Node
	 */
	public Node findCell(int colIndex, int rowIndex)
	{
		logger.debug(String.format("Find cell into tree table view. colIndex = %s, rowIndex = %s", colIndex, rowIndex));
		if (colIndex == Integer.MIN_VALUE)
		{
			if (this.rowCount < rowIndex)
			{
				throw new TableOutOfBoundsException(String.format(R.FX_TABLE_VIEW_TOOB_EXCEPTION.get(), rowIndex, this.rowCount));
			}
			Node cell = UtilsFx.runOnFxThreadAndWaitResult(() -> {
				this.treeTableView.scrollToColumnIndex(0);
				this.treeTableView.scrollTo(rowIndex);
				return (Node) this.treeTableView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN, rowIndex, 0);
			});
			logger.debug("Found cell : " + cell);
			logger.debug("cell instanceof TreeTableCell : " + (cell instanceof TreeTableCell));
			if (cell instanceof TreeTableCell)
			{
				return ((TreeTableCell) cell).getTreeTableRow();
			}

			SimpleRow tableRow = new SimpleRow();
			for (int i = 0; i < this.colCount; i++)
			{
				int finalI = i;
				tableRow.add(UtilsFx.runOnFxThreadAndWaitResult(() -> {
					this.treeTableView.scrollToColumnIndex(finalI);
					this.treeTableView.scrollTo(rowIndex);
					return (Node) this.treeTableView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN, rowIndex, finalI);
				}));
			}
			return tableRow;
		}
		else
		{
			this.checkTreeTable(colIndex, rowIndex);
			Node cell = UtilsFx.runOnFxThreadAndWaitResult(() -> {
				try
				{
					logger.debug(String.format("Start scroll to column %s", colIndex));
					this.treeTableView.scrollToColumnIndex(colIndex);
					logger.debug(String.format("Start scroll to row %s", rowIndex));
					this.treeTableView.scrollTo(rowIndex);
					logger.debug("Start getting cell");
					return (Node) this.treeTableView.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN, rowIndex, colIndex);
				}
				catch (Exception e)
				{
					logger.error(String.format("findCell(%s,%s,%s)", this.treeTableView, colIndex, rowIndex));
					logger.error(e.getMessage(), e);
				}
				return null;
			});
			logger.debug(String.format("Found cell : %s", cell));
			return cell;
		}
	}

	public Map<String, String> getRow(String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		if (valueCondition == null)
		{
			throw new Exception(R.FX_TABLE_VIEW_GET_ROW_NULL_CONDITION.get());
		}
		List<String> tableHeaders = this.getTableHeaders(columns);
		List<Map<String, Object>> listOfRows = IntStream.range(0, this.rowCount)
				.mapToObj(row -> IntStream.range(0, tableHeaders.size())
						.boxed()
						.collect(Collectors.toMap(tableHeaders::get, j -> (Object) this.getCellValue(j, row), (u, v) -> {
					throw new IllegalStateException(String.format(R.FX_TABLE_VIEW_GET_ROW_DUPLICATE_KEY.get(), u));
				}, LinkedHashMap::new)))
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

	public List<String> getRowIndexes(String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
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

	public Map<String, String> getRowByIndex(String[] columns, int rowIndex)
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

	public Map<String, ValueAndColor> getRowWithColor(String[] columns, int rowIndex, PluginInfo info) throws Exception
	{
		Map<String, ValueAndColor> result = new LinkedHashMap<>();
		List<String> tableHeaders = this.getTableHeaders(columns);
		for (int column = 0; column < tableHeaders.size(); column++)
		{
			String headerName = tableHeaders.get(column);

			Color backgroundColor = Color.WHITE;
			Color foregroundColor = Color.BLACK;
			Paint fgPaint = null;

			Node nodeTreeTableCell = this.findCell(column, rowIndex);
			if (nodeTreeTableCell instanceof TreeTableCell<?, ?>)
			{
				TreeTableCell treeTableCell = (TreeTableCell) nodeTreeTableCell;
				backgroundColor = ((TreeTableRow<?>) treeTableCell.getTreeTableRow()).getBackground().getFills()
						.stream()
						.map(BackgroundFill::getFill)
						.filter(paint -> paint instanceof javafx.scene.paint.Color)
						.map(paint -> (javafx.scene.paint.Color) paint)
						.findFirst()
						.map(UtilsFx::convert).orElse(Color.WHITE);

				if (treeTableCell.getGraphic() != null)
				{
					//use graphic
					Node treeTableCellGraphic = treeTableCell.getGraphic();
					if (treeTableCellGraphic instanceof Text)
					{
						fgPaint = ((Text) treeTableCellGraphic).getFill();
					}
					else if (treeTableCellGraphic instanceof Labeled)
					{
						fgPaint = ((Labeled) treeTableCellGraphic).getTextFill();
					}
					else
					{
						//try to find all Labeled or Text nodes from current cell and get color
						Locator emptyLocator = new Locator();
						List<EventTarget> allDescendants = new MatcherFx(info, emptyLocator, treeTableCellGraphic).findAllDescedants();
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
					fgPaint = treeTableCell.getTextFill();
				}
			}

			if (fgPaint instanceof javafx.scene.paint.Color)
			{
				foregroundColor = UtilsFx.convert((javafx.scene.paint.Color) fgPaint);
			}

			String treeCellValue = this.getCellValue(nodeTreeTableCell);
			if (treeCellValue == null)
			{
				treeCellValue = this.getStringCellValue(column, rowIndex);
			}
			result.put(headerName, new ValueAndColor(treeCellValue, foregroundColor, backgroundColor));
		}
		return result;
	}

	//endregion

	//region private methods
	private void byPass(TreeItem<?> root, Consumer<TreeItem<?>> consumer)
	{
		if (root != null && root.getValue() != null)
		{
			consumer.accept(root);
			root.getChildren().forEach(child -> byPass(child, consumer));
		}
	}

	private void fillHeaders(String[][] array, String[] customHeaders)
	{
		List<String> tableHeaders = this.getTableHeaders(customHeaders);
		IntStream.range(0, Math.min(this.colCount, array[0].length)).forEach(i -> array[0][i] = tableHeaders.get(i));
	}

	private List<String> getTableHeaders(String[] columns)
	{
		List<String> list = new ArrayList<>();
		ObservableList<? extends TreeTableColumn<?, ?>> tableColumns = this.treeTableView.getColumns();

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

	private void checkTreeTable(int columnIndex, int rowIndex)
	{
		if (this.rowCount < rowIndex)
		{
			throw new TableOutOfBoundsException(String.format(R.FX_TABLE_VIEW_TOOB_EXCEPTION.get(), rowIndex, this.rowCount));
		}

		if (this.colCount < columnIndex)
		{
			throw new TableOutOfBoundsException(String.format(R.FX_TABLE_VIEW_COLUMN_TOOB_EXCEPTION.get(), columnIndex, this.colCount));
		}
	}

	private String getCellValue(Node cell)
	{
		if (cell instanceof TreeTableCell<?, ?>)
		{
			TreeTableCell treeTableCell = (TreeTableCell) cell;
			if (treeTableCell.getGraphic() == null)
			{
				return treeTableCell.getText();
			}
			else
			{
				return MatcherFx.getText(treeTableCell.getGraphic());
			}
		}
		return null;
	}

	private String getStringCellValue(int columnIndex, int rowIndex)
	{
		TreeTableColumn<?, ?> tableColumn = this.treeTableView.getColumns().get(columnIndex);
		ObservableValue<?> cellObservableValue = tableColumn.getCellObservableValue(rowIndex);
		if (cellObservableValue == null)
		{
			return "";
		}
		return String.valueOf(cellObservableValue.getValue());
	}
	//endregion
}
