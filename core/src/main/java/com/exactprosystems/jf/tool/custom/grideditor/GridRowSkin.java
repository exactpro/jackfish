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

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import com.sun.javafx.scene.control.behavior.TableRowBehavior;
import com.sun.javafx.scene.control.skin.CellSkinBase;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableRow;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

public class GridRowSkin extends CellSkinBase<TableRow<ObservableList<SpreadsheetCell>>, CellBehaviorBase<TableRow<ObservableList<SpreadsheetCell>>>>
{
	private final SpreadsheetHandle handle;
	private final SpreadsheetView spreadsheetView;
	private Reference<HashMap<TableColumnBase, CellView>> cellsMap;
	private final List<CellView> cells = new ArrayList<>();

	public GridRowSkin(SpreadsheetHandle handle, TableRow<ObservableList<SpreadsheetCell>> gridRow)
	{
		super(gridRow, new TableRowBehavior<>(gridRow));
		this.handle = handle;
		spreadsheetView = handle.getView();

		getSkinnable().setPickOnBounds(false);

		registerChangeListener(gridRow.itemProperty(), "ITEM");
		registerChangeListener(gridRow.indexProperty(), "INDEX");
	}

	@Override
	protected void handleControlPropertyChanged(String p)
	{
		super.handleControlPropertyChanged(p);

		if ("INDEX".equals(p))
		{
			if (getSkinnable().isEmpty())
			{
				requestCellUpdate();
			}
		}
		else if ("ITEM".equals(p))
		{
			requestCellUpdate();
		}
	}

	private void requestCellUpdate()
	{
		getSkinnable().requestLayout();

		final int newIndex = getSkinnable().getIndex();
		getChildren().clear();
		cells.stream().forEach(cell -> cell.updateIndex(newIndex));
	}

	@Override
	protected void layoutChildren(double x, final double y, final double w, final double h)
	{

		final ObservableList<? extends TableColumnBase<?, ?>> visibleLeafColumns = handle.getGridView().getVisibleLeafColumns();
		if (visibleLeafColumns.isEmpty())
		{
			super.layoutChildren(x, y, w, h);
			return;
		}

		final GridRow control = (GridRow) getSkinnable();
		final SpreadsheetGridView gridView = handle.getGridView();
		final DataProvider provider = spreadsheetView.providerProperty().get();
		final int index = control.getIndex();

		if (index < 0 || index >= gridView.getItems().size())
		{
			getChildren().clear();
			putCellsInCache();
			return;
		}

		ObservableList rows = provider.getRows();
		if (rows.size() > 0)
		{
			final List<SpreadsheetCell> row = (List<SpreadsheetCell>) rows.get(index);
			final List<SpreadsheetColumn> columns = spreadsheetView.getColumns();
			final ObservableList<TableColumn<ObservableList<SpreadsheetCell>, ?>> tableViewColumns = gridView.getColumns();
			if (columns.size() != tableViewColumns.size())
			{
				return;
			}

			getSkinnable().setVisible(true);
			double width;
			double height;

			final double verticalPadding = snappedTopInset() + snappedBottomInset();
			final double horizontalPadding = snappedLeftInset() + snappedRightInset();
			double controlHeight = getTableRowHeight(index);
			double customHeight = controlHeight == -1 ? GridViewSkin.DEFAULT_CELL_HEIGHT : controlHeight;

			final GridViewSkin skin = handle.getCellsViewSkin();
			skin.hBarValue.set(index, true);

			double headerWidth = gridView.getWidth();
			final double hbarValue = skin.getHBar().getValue();

			((GridRow) getSkinnable()).verticalShift.setValue(0);

			double fixedColumnWidth = 0;
			List<CellView> fixedCells = new ArrayList<>();

			putCellsInCache();

			for (int indexColumn = 0; indexColumn < columns.size(); indexColumn++)
			{

				width = snapSize(columns.get(indexColumn).getWidth()) - snapSize(horizontalPadding);

				final SpreadsheetCell spreadsheetCell = row.get(indexColumn);
				boolean isVisible = !isInvisible(x, width, hbarValue, headerWidth);

				if (!isVisible)
				{
					x += width;
					continue;
				}
				final CellView tableCell = getCell(gridView.getColumns().get(indexColumn));

				cells.add(0, tableCell);

				// In case the node was treated previously
				tableCell.setManaged(true);

				double tableCellX = 0;
				boolean increaseFixedWidth = false;
				if (tableCell.getIndex() != index)
				{
					tableCell.updateIndex(index);
				}
				else
				{
					tableCell.updateItem(spreadsheetCell, false);
				}
				if (tableCell.getParent() == null)
				{
					getChildren().add(0, tableCell);
				}

				if (controlHeight == -1 && !tableCell.isEditing())
				{
					double tempHeight = tableCell.prefHeight(width);
					if (tempHeight > customHeight)
					{
						skin.rowHeightMap.put(index, tempHeight);
						for (CellView cell : cells)
						{
							cell.resize(cell.getWidth(), cell.getHeight() + (tempHeight - customHeight));
						}
						customHeight = tempHeight;
						skin.getFlow().layoutChildren();
					}
				}

				height = customHeight;
				height = snapSize(height) - snapSize(verticalPadding);
				tableCell.resize(width, height);

				double spaceBetweenTopAndMe = 0;
				for (int p = spreadsheetCell.getRow(); p < index; ++p)
				{
					spaceBetweenTopAndMe += skin.getRowHeight(p);
				}

				tableCell.relocate(x + tableCellX, snappedTopInset() - spaceBetweenTopAndMe + ((GridRow) getSkinnable()).verticalShift.get());

				x += width;
			}
			skin.fixedColumnWidth = fixedColumnWidth;
			handleFixedCell(fixedCells, index);
			removeUselessCell(index);
			if (handle.getCellsViewSkin().lastRowLayout.get())
			{
				handle.getCellsViewSkin().lastRowLayout.setValue(false);
			}
		}
	}
	private void removeUselessCell(int index)
	{
		getChildren().removeIf((Node t) -> t instanceof CellView && !cells.contains(t) && ((CellView) t).getIndex() == index);
	}

	private void handleFixedCell(List<CellView> fixedCells, int index)
	{
		if (fixedCells.isEmpty())
		{
			return;
		}

		if (handle.getCellsViewSkin().rowToLayout.get(index))
		{
			GridRow gridRow = handle.getCellsViewSkin().getFlow().getTopRow();
			if (gridRow != null)
			{
				for (CellView cell : fixedCells)
				{
					final double originalLayoutY = getSkinnable().getLayoutY() + cell.getLayoutY();
					gridRow.removeCell(cell);
					gridRow.addCell(cell);
					if (handle.getCellsViewSkin().deportedCells.containsKey(gridRow))
					{
						handle.getCellsViewSkin().deportedCells.get(gridRow).add(cell);
					}
					else
					{
						Set<CellView> temp = new HashSet<>();
						temp.add(cell);
						handle.getCellsViewSkin().deportedCells.put(gridRow, temp);
					}
					cell.relocate(cell.getLayoutX(), originalLayoutY - gridRow.getLayoutY());
				}
			}
		}
		else
		{
			fixedCells.forEach(CellView::toFront);
		}
	}

	private HashMap<TableColumnBase, CellView> getCellsMap()
	{
		if (cellsMap == null || cellsMap.get() == null)
		{
			HashMap<TableColumnBase, CellView> map = new HashMap<>();
			cellsMap = new WeakReference<>(map);
			return map;
		}
		return cellsMap.get();
	}

	private void putCellsInCache()
	{
		for (CellView cell : cells)
		{
			getCellsMap().put(cell.getTableColumn(), cell);
		}
		cells.clear();
	}

	private CellView getCell(TableColumnBase tcb)
	{
		TableColumn tableColumn = (TableColumn<CellView, ?>) tcb;
		CellView cell;
		if (getCellsMap().containsKey(tableColumn))
		{
			return getCellsMap().remove(tableColumn);
		}
		else
		{
			cell = (CellView) tableColumn.getCellFactory().call(tableColumn);
			cell.updateTableColumn(tableColumn);
			cell.updateTableView(tableColumn.getTableView());
			cell.updateTableRow(getSkinnable());
		}
		return cell;
	}

	private double getTableRowHeight(int row)
	{
		Double rowHeightCache = handle.getCellsViewSkin().rowHeightMap.get(row);
		return rowHeightCache == null ? -1 : rowHeightCache;
	}

	private boolean isInvisible(double x, double width, double hbarValue, double headerWidth)
	{
		return (x + width < hbarValue) || (x > hbarValue + headerWidth);
	}

	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
	{
		double prefWidth = 0.0;

		final List<? extends TableColumnBase/*<T,?>*/> visibleLeafColumns = handle.getGridView().getVisibleLeafColumns();
		for (TableColumnBase visibleLeafColumn : visibleLeafColumns)
		{
			prefWidth += visibleLeafColumn.getWidth();
		}

		return prefWidth;
	}

	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
	{
		return getSkinnable().getPrefHeight();
	}

	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
	{
		return getSkinnable().getPrefHeight();
	}

	@Override
	protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
	{
		return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
	}
}
