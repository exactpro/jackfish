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

import com.sun.javafx.scene.control.behavior.TableViewBehavior;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

public class GridViewBehavior extends TableViewBehavior<ObservableList<SpreadsheetCell>>
{
	private GridViewSkin skin;

	public GridViewBehavior(TableView<ObservableList<SpreadsheetCell>> control)
	{
		super(control);
	}

	void setGridViewSkin(GridViewSkin skin)
	{
		this.skin = skin;
	}

	@Override
	protected void updateCellVerticalSelection(int delta, Runnable defaultAction)
	{
		TableSelectionModel<ObservableList<SpreadsheetCell>> sm = getSelectionModel();
		if (sm == null || sm.getSelectionMode() == SelectionMode.SINGLE)
		{
			return;
		}

		TableFocusModel fm = getFocusModel();
		if (fm == null)
		{
			return;
		}

		final TablePositionBase focusedCell = getFocusedCell();

		if (isShiftDown && getAnchor() != null)
		{

			final SpreadsheetCell cell = getControl().getItems().get(fm.getFocusedIndex()).get(focusedCell.getColumn());

			int newRow;
			if (delta < 0)
			{
				newRow = cell.getRow() + delta;
			}
			else
			{
				newRow = cell.getRow() + delta;
			}

			newRow = Math.max(Math.min(getItemCount() - 1, newRow), 0);

			final TablePositionBase<?> anchor = getAnchor();
			int minRow = Math.min(anchor.getRow(), newRow);
			int maxRow = Math.max(anchor.getRow(), newRow);
			int minColumn = Math.min(anchor.getColumn(), focusedCell.getColumn());
			int maxColumn = Math.max(anchor.getColumn(), focusedCell.getColumn());

			sm.clearSelection();
			if (minColumn != -1 && maxColumn != -1)
			{
				sm.selectRange(minRow, getControl().getColumns().get(minColumn), maxRow, getControl().getColumns().get(maxColumn));
			}
			fm.focus(newRow, focusedCell.getTableColumn());
		}
		else
		{
			final int focusIndex = fm.getFocusedIndex();
			if (!sm.isSelected(focusIndex, focusedCell.getTableColumn()))
			{
				sm.select(focusIndex, focusedCell.getTableColumn());
			}
			defaultAction.run();
		}
	}

	@Override
	protected void updateCellHorizontalSelection(int delta, Runnable defaultAction)
	{
		TableSelectionModel sm = getSelectionModel();
		if (sm == null || sm.getSelectionMode() == SelectionMode.SINGLE)
		{
			return;
		}

		TableFocusModel fm = getFocusModel();
		if (fm == null)
		{
			return;
		}

		final TablePositionBase focusedCell = getFocusedCell();
		if (focusedCell == null || focusedCell.getTableColumn() == null)
		{
			return;
		}

		TableColumnBase adjacentColumn = getColumn(focusedCell.getTableColumn(), delta);
		if (adjacentColumn == null)
		{
			return;
		}

		final int focusedCellRow = focusedCell.getRow();

		if (isShiftDown && getAnchor() != null)
		{
			final int columnPos = getVisibleLeafIndex(focusedCell.getTableColumn());

			final SpreadsheetCell cell = getControl().getItems().get(focusedCellRow).get(columnPos);

			final int newColumn;
			if (delta < 0)
			{
				newColumn = cell.getColumn() + delta;
			}
			else
			{
				newColumn = cell.getColumn() + delta;
			}
			final TablePositionBase<?> anchor = getAnchor();
			int minRow = Math.min(anchor.getRow(), focusedCellRow);
			int maxRow = Math.max(anchor.getRow(), focusedCellRow);
			int minColumn = Math.min(anchor.getColumn(), newColumn);
			int maxColumn = Math.max(anchor.getColumn(), newColumn);

			sm.clearSelection();
			if (minColumn != -1 && maxColumn != -1)
			{
				sm.selectRange(minRow, getControl().getColumns().get(minColumn), maxRow, getControl().getColumns().get(maxColumn));
			}
			fm.focus(focusedCell.getRow(), getColumn(newColumn));
		}
		else
		{
			defaultAction.run();
		}

	}

	@Override
	protected void focusPreviousRow()
	{
		focusVertical(true);
	}

	@Override
	protected void focusNextRow()
	{
		focusVertical(false);
	}

	@Override
	protected void focusLeftCell()
	{
		focusHorizontal(true);
	}

	@Override
	protected void focusRightCell()
	{
		focusHorizontal(false);
	}

	@Override
	protected void discontinuousSelectPreviousRow()
	{
		discontinuousSelectVertical(true);
	}

	@Override
	protected void discontinuousSelectNextRow()
	{
		discontinuousSelectVertical(false);
	}

	@Override
	protected void discontinuousSelectPreviousColumn()
	{
		discontinuousSelectHorizontal(true);
	}

	@Override
	protected void discontinuousSelectNextColumn()
	{
		discontinuousSelectHorizontal(false);
	}

	private void focusVertical(boolean previous)
	{
		TableSelectionModel<ObservableList<SpreadsheetCell>> sm = getSelectionModel();
		if (sm == null || sm.getSelectionMode() == SelectionMode.SINGLE)
		{
			return;
		}

		TableFocusModel fm = getFocusModel();
		if (fm == null)
		{
			return;
		}

		final TablePositionBase focusedCell = getFocusedCell();
		if (focusedCell == null || focusedCell.getTableColumn() == null)
		{
			return;
		}

		final SpreadsheetCell cell = getControl().getItems().get(focusedCell.getRow()).get(focusedCell.getColumn());
		sm.clearAndSelect(previous ? findPreviousRow(focusedCell, cell) : findNextRow(focusedCell, cell), focusedCell.getTableColumn());
		skin.focusScroll();
	}

	private void focusHorizontal(boolean previous)
	{
		TableSelectionModel sm = getSelectionModel();
		if (sm == null)
		{
			return;
		}

		TableFocusModel fm = getFocusModel();
		if (fm == null)
		{
			return;
		}
		final TablePositionBase focusedCell = getFocusedCell();
		if (focusedCell == null || focusedCell.getTableColumn() == null)
		{
			return;
		}

		final SpreadsheetCell cell = getControl().getItems().get(focusedCell.getRow()).get(focusedCell.getColumn());

		sm.clearAndSelect(focusedCell.getRow(), getControl().getColumns().get(previous ? findPreviousColumn(focusedCell, cell) : findNextColumn(focusedCell, cell)));
		skin.focusScroll();
	}

	private int findPreviousRow(TablePositionBase focusedCell, SpreadsheetCell cell)
	{
		final ObservableList<ObservableList<SpreadsheetCell>> items = getControl().getItems();
		SpreadsheetCell temp;
		if (isEmpty(cell))
		{
			for (int row = focusedCell.getRow() - 1; row >= 0; --row)
			{
				temp = items.get(row).get(focusedCell.getColumn());
				if (!isEmpty(temp))
				{
					return row;
				}
			}
		}
		else if (focusedCell.getRow() - 1 >= 0 && !isEmpty(items.get(focusedCell.getRow() - 1).get(focusedCell.getColumn())))
		{
			for (int row = focusedCell.getRow() - 2; row >= 0; --row)
			{
				temp = items.get(row).get(focusedCell.getColumn());
				if (isEmpty(temp))
				{
					return row + 1;
				}
			}
		}
		else
		{
			for (int row = focusedCell.getRow() - 2; row >= 0; --row)
			{
				temp = items.get(row).get(focusedCell.getColumn());
				if (!isEmpty(temp))
				{
					return row;
				}
			}
		}
		return 0;
	}

	private int findNextRow(TablePositionBase focusedCell, SpreadsheetCell cell)
	{
		final ObservableList<ObservableList<SpreadsheetCell>> items = getControl().getItems();
		final int itemCount = getItemCount();
		SpreadsheetCell temp;
		if (isEmpty(cell))
		{
			for (int row = focusedCell.getRow() + 1; row < itemCount; ++row)
			{
				temp = items.get(row).get(focusedCell.getColumn());
				if (!isEmpty(temp))
				{
					return row;
				}
			}
		}
		else if (focusedCell.getRow() + 1 < itemCount && !isEmpty(items.get(focusedCell.getRow() + 1).get(focusedCell.getColumn())))
		{
			for (int row = focusedCell.getRow() + 2; row < getItemCount(); ++row)
			{
				temp = items.get(row).get(focusedCell.getColumn());
				if (isEmpty(temp))
				{
					return row - 1;
				}
			}
		}
		else
		{
			for (int row = focusedCell.getRow() + 2; row < itemCount; ++row)
			{
				temp = items.get(row).get(focusedCell.getColumn());
				if (!isEmpty(temp))
				{
					return row;
				}
			}
		}
		return itemCount - 1;
	}

	private void discontinuousSelectVertical(boolean previous)
	{
		TableSelectionModel sm = getSelectionModel();
		if (sm == null)
		{
			return;
		}

		TableFocusModel fm = getFocusModel();
		if (fm == null)
		{
			return;
		}
		final TablePositionBase focusedCell = getFocusedCell();
		if (focusedCell == null || focusedCell.getTableColumn() == null)
		{
			return;
		}

		final SpreadsheetCell cell = getControl().getItems().get(fm.getFocusedIndex()).get(focusedCell.getColumn());

		int newRow = previous ? findPreviousRow(focusedCell, cell) : findNextRow(focusedCell, cell);

		newRow = Math.max(Math.min(getItemCount() - 1, newRow), 0);

		final TablePositionBase<?> anchor = getAnchor();
		int minRow = Math.min(anchor.getRow(), newRow);
		int maxRow = Math.max(anchor.getRow(), newRow);
		int minColumn = Math.min(anchor.getColumn(), focusedCell.getColumn());
		int maxColumn = Math.max(anchor.getColumn(), focusedCell.getColumn());

		sm.clearSelection();
		if (minColumn != -1 && maxColumn != -1)
		{
			sm.selectRange(minRow, getControl().getColumns().get(minColumn), maxRow, getControl().getColumns().get(maxColumn));
		}
		fm.focus(newRow, focusedCell.getTableColumn());
		skin.focusScroll();
	}

	private void discontinuousSelectHorizontal(boolean previous)
	{
		TableSelectionModel sm = getSelectionModel();
		if (sm == null)
		{
			return;
		}

		TableFocusModel fm = getFocusModel();
		if (fm == null)
		{
			return;
		}
		final TablePositionBase focusedCell = getFocusedCell();
		if (focusedCell == null || focusedCell.getTableColumn() == null)
		{
			return;
		}

		final int columnPos = getVisibleLeafIndex(focusedCell.getTableColumn());
		int focusedCellRow = focusedCell.getRow();
		final SpreadsheetCell cell = getControl().getItems().get(focusedCellRow).get(columnPos);

		final int newColumn = previous ? findPreviousColumn(focusedCell, cell) : findNextColumn(focusedCell, cell);

		final TablePositionBase<?> anchor = getAnchor();
		int minRow = Math.min(anchor.getRow(), focusedCellRow);
		int maxRow = Math.max(anchor.getRow(), focusedCellRow);
		int minColumn = Math.min(anchor.getColumn(), newColumn);
		int maxColumn = Math.max(anchor.getColumn(), newColumn);

		sm.clearSelection();
		if (minColumn != -1 && maxColumn != -1)
		{
			sm.selectRange(minRow, getControl().getColumns().get(minColumn), maxRow, getControl().getColumns().get(maxColumn));
		}
		fm.focus(focusedCell.getRow(), getColumn(newColumn));
		skin.focusScroll();
	}

	private int findNextColumn(TablePositionBase focusedCell, SpreadsheetCell cell)
	{
		final ObservableList<ObservableList<SpreadsheetCell>> items = getControl().getItems();
		final int itemCount = getControl().getColumns().size();
		SpreadsheetCell temp;
		if (isEmpty(cell))
		{
			for (int column = focusedCell.getColumn() + 1; column < itemCount; ++column)
			{
				temp = items.get(focusedCell.getRow()).get(column);
				if (!isEmpty(temp))
				{
					return column;
				}
			}
		}
		else if (focusedCell.getColumn() + 1 < itemCount && !isEmpty(items.get(focusedCell.getRow()).get(focusedCell.getColumn() + 1)))
		{
			for (int column = focusedCell.getColumn() + 2; column < itemCount; ++column)
			{
				temp = items.get(focusedCell.getRow()).get(column);
				if (isEmpty(temp))
				{
					return column - 1;
				}
			}
		}
		else
		{
			for (int column = focusedCell.getColumn() + 2; column < itemCount; ++column)
			{
				temp = items.get(focusedCell.getRow()).get(column);
				if (!isEmpty(temp))
				{
					return column;
				}
			}
		}
		return itemCount - 1;
	}

	private int findPreviousColumn(TablePositionBase focusedCell, SpreadsheetCell cell)
	{
		final ObservableList<ObservableList<SpreadsheetCell>> items = getControl().getItems();
		SpreadsheetCell temp;
		if (isEmpty(cell))
		{
			for (int column = focusedCell.getColumn() - 1; column >= 0; --column)
			{
				temp = items.get(focusedCell.getRow()).get(column);
				if (!isEmpty(temp))
				{
					return column;
				}
			}
		}
		else if (focusedCell.getColumn() - 1 >= 0 && !isEmpty(items.get(focusedCell.getRow()).get(focusedCell.getColumn() - 1)))
		{
			for (int column = focusedCell.getColumn() - 2; column >= 0; --column)
			{
				temp = items.get(focusedCell.getRow()).get(column);
				if (isEmpty(temp))
				{
					return column + 1;
				}
			}
		}
		else
		{
			for (int column = focusedCell.getColumn() - 2; column >= 0; --column)
			{
				temp = items.get(focusedCell.getRow()).get(column);
				if (!isEmpty(temp))
				{
					return column;
				}
			}
		}
		return 0;
	}

	private boolean isEmpty(SpreadsheetCell cell)
	{
		return (cell.getItem() == null || (cell.getItem() instanceof Double && ((Double) cell.getItem()).isNaN()));
	}
}
