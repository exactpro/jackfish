/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.List;

public class SpreadsheetViewSelectionModel
{

	private final TableViewSpanSelectionModel selectionModel;
	private final SpreadsheetView spv;

	SpreadsheetViewSelectionModel(SpreadsheetView spv, TableViewSpanSelectionModel selectionModel)
	{
		this.spv = spv;
		this.selectionModel = selectionModel;
	}

	public final void clearAndSelect(int row, SpreadsheetColumn column)
	{
		selectionModel.clearAndSelect(row, column.column);
	}

	public final void select(int row, SpreadsheetColumn column)
	{
		selectionModel.select(row, column.column);
	}

	public final void clearSelection()
	{
		selectionModel.clearSelection();
	}

	public final ObservableList<TablePosition> getSelectedCells()
	{
		return selectionModel.getSelectedCells();
	}

	public final void selectAll()
	{
		selectionModel.selectAll();
	}

	public final TablePosition getFocusedCell()
	{
		return selectionModel.getTableView().getFocusModel().getFocusedCell();
	}

	public final void focus(int row, SpreadsheetColumn column)
	{
		selectionModel.getTableView().getFocusModel().focus(row, column.column);
	}

	public final void setSelectionMode(SelectionMode value)
	{
		selectionModel.setSelectionMode(value);
	}

	public SelectionMode getSelectionMode()
	{
		return selectionModel.getSelectionMode();
	}

	public void selectCells(List<Pair<Integer, Integer>> selectedCells)
	{
		selectionModel.verifySelectedCells(selectedCells);
	}

	public void selectCells(Pair<Integer, Integer>... selectedCells)
	{
		selectionModel.verifySelectedCells(Arrays.asList(selectedCells));
	}

	public void selectRange(int minRow, SpreadsheetColumn minColumn, int maxRow, SpreadsheetColumn maxColumn)
	{
		selectionModel.selectRange(minRow, minColumn.column, maxRow, maxColumn.column);
	}

	public void clearAndSelectLeftCell()
	{
		TablePosition<ObservableList<SpreadsheetCell>, ?> position = getFocusedCell();
		int row = position.getRow();
		int column = position.getColumn();
		column -= 1;
		if (column < 0)
		{
			if (row == 0)
			{
				column++;
			}
			else
			{
				column = spv.getProvider().columnCount() - 1;
				row--;
			}
		}
		clearAndSelect(row, spv.getColumns().get(column));
	}

	public void clearAndSelectRightCell()
	{
		TablePosition<ObservableList<SpreadsheetCell>, ?> position = getFocusedCell();
		int row = position.getRow();
		int column = position.getColumn();
		column += 1;
		if (column >= spv.getColumns().size())
		{
			if (row == spv.getProvider().rowCount() - 1)
			{
				column--;
			}
			else
			{
				column = 0;
				row++;
			}
		}
		clearAndSelect(row, spv.getColumns().get(column));
	}

	public void clearAndSelectPreviousCell()
	{
		TablePosition<ObservableList<SpreadsheetCell>, ?> position = getFocusedCell();
		int nextRow = FocusModelListener.getPreviousRowNumber(position, selectionModel.getTableView());
		if (nextRow >= 0)
		{
			clearAndSelect(nextRow, spv.getColumns().get(position.getColumn()));
		}
	}

	public void clearAndSelectNextCell(int column, int row)
	{
		int nextRow = row + 1;
		if (nextRow < spv.getProvider().rowCount())
		{
			clearAndSelect(nextRow, spv.getColumns().get(column));
		}
	}
}
