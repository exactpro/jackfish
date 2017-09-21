////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.tool.Common;
import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.util.BitSet;
import java.util.List;

public class HorizontalHeader extends TableHeaderRow
{
	final GridViewSkin gridViewSkin;

	private boolean working = true;
	protected BitSet selectedColumns = new BitSet();

	public HorizontalHeader(final GridViewSkin skin)
	{
		super(skin);
		gridViewSkin = skin;
	}

	public void init()
	{
		updateHorizontalHeaderVisibility(true);

		gridViewSkin.verticalHeader.verticalHeaderWidthProperty().addListener(verticalHeaderListener);

		gridViewSkin.getSelectedColumns().addListener(selectionListener);

		Common.runLater(() -> {
			requestLayout();
			installHeaderMouseEvent();
		});

		getRootHeader().getColumnHeaders().addListener((Observable o) -> {
			updateHighlightSelection();
			installHeaderMouseEvent();
		});
	}

	@Override
	public HorizontalHeaderColumn getRootHeader()
	{
		return (HorizontalHeaderColumn) super.getRootHeader();
	}

	void clearSelectedColumns()
	{
		selectedColumns.clear();
	}

	@Override
	protected void updateTableWidth()
	{
		super.updateTableWidth();
		double padding = 0;

		if (working && gridViewSkin != null && gridViewSkin.spreadsheetView != null && gridViewSkin.verticalHeader != null)
		{
			padding += gridViewSkin.verticalHeader.getVerticalHeaderWidth();
		}

		Rectangle clip = ((Rectangle) getClip());

		clip.setWidth(clip.getWidth() == 0 ? 0 : clip.getWidth() - padding);
	}

	@Override
	protected void updateScrollX()
	{
		super.updateScrollX();
		gridViewSkin.horizontalPickers.updateScrollX();

		if (working)
		{
			requestLayout();
			getRootHeader().layoutFixedColumns();
		}
	}

	@Override
	protected NestedTableColumnHeader createRootHeader()
	{
		return new HorizontalHeaderColumn(getTableSkin(), null);
	}

	private void installHeaderMouseEvent()
	{
		for (final TableColumnHeader columnHeader : getRootHeader().getColumnHeaders())
		{
			EventHandler<MouseEvent> mouseEventHandler = (MouseEvent mouseEvent) -> {
				if (mouseEvent.isPrimaryButtonDown())
				{
					headerClicked((TableColumn) columnHeader.getTableColumn(), mouseEvent);
				}
			};
			columnHeader.getChildrenUnmodifiable().get(0).setOnMousePressed(mouseEventHandler);
		}
	}

	private void headerClicked(TableColumn column, MouseEvent event)
	{
		TableViewSelectionModel<ObservableList<SpreadsheetCell>> sm = gridViewSkin.handle.getGridView().getSelectionModel();
		int lastRow = gridViewSkin.spreadsheetView.getProvider().rowCount() - 1;
		int indexColumn = column.getTableView().getColumns().indexOf(column);
		TablePosition focusedPosition = sm.getTableView().getFocusModel().getFocusedCell();
		if (event.isShortcutDown())
		{
			BitSet tempSet = (BitSet) selectedColumns.clone();
			sm.selectRange(0, column, lastRow, column);
			selectedColumns.or(tempSet);
			selectedColumns.set(indexColumn);
		}
		else if (event.isShiftDown() && focusedPosition != null && focusedPosition.getTableColumn() != null)
		{
			sm.clearSelection();
			sm.selectRange(0, column, lastRow, focusedPosition.getTableColumn());
			sm.getTableView().getFocusModel().focus(0, focusedPosition.getTableColumn());
			int min = Math.min(indexColumn, focusedPosition.getColumn());
			int max = Math.max(indexColumn, focusedPosition.getColumn());
			selectedColumns.set(min, max + 1);
		}
		else
		{
			sm.clearSelection();
			sm.selectRange(0, column, lastRow, column);
			sm.getTableView().getFocusModel().focus(0, column);
			selectedColumns.set(indexColumn);
		}
	}


	private final InvalidationListener verticalHeaderListener = observable -> updateTableWidth();

	private final InvalidationListener selectionListener = valueModel -> updateHighlightSelection();

	private void updateHighlightSelection()
	{
		for (final TableColumnHeader i : getRootHeader().getColumnHeaders())
		{
			i.getStyleClass().removeAll("selected"); //$NON-NLS-1$

		}
		final List<Integer> selectedColumns = gridViewSkin.getSelectedColumns();
		selectedColumns.stream().filter(i -> getRootHeader().getColumnHeaders().size() > i).forEach(i -> {
			getRootHeader().getColumnHeaders().get(i).getStyleClass().addAll("selected"); //$NON-NLS-1$
		});

	}

	private void updateHorizontalHeaderVisibility(boolean visible)
	{
		working = visible;
		setManaged(working);
		requestLayout();
		getRootHeader().layoutFixedColumns();
		updateHighlightSelection();
	}
}
