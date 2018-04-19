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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

public class FocusModelListener implements ChangeListener<TablePosition<ObservableList<SpreadsheetCell>, ?>>
{

	private final TableView.TableViewFocusModel<ObservableList<SpreadsheetCell>> tfm;
	private final SpreadsheetGridView cellsView;
	private final SpreadsheetView spreadsheetView;

	public FocusModelListener(SpreadsheetView spreadsheetView, SpreadsheetGridView cellsView)
	{
		this.tfm = cellsView.getFocusModel();
		this.spreadsheetView = spreadsheetView;
		this.cellsView = cellsView;
	}

	@Override
	public void changed(ObservableValue<? extends TablePosition<ObservableList<SpreadsheetCell>, ?>> ov, final TablePosition<ObservableList<SpreadsheetCell>, ?> oldPosition, final TablePosition<ObservableList<SpreadsheetCell>, ?> newPosition)
	{

	}

	public static int getNextRowNumber(final TablePosition<?, ?> pos, TableView<ObservableList<SpreadsheetCell>> cellsView)
	{
		return cellsView.getItems().get(pos.getRow()).get(pos.getColumn()).getRow() + 1;
	}

	public static int getPreviousRowNumber(final TablePosition<?, ?> pos, TableView<ObservableList<SpreadsheetCell>> cellsView)
	{
		return cellsView.getItems().get(pos.getRow()).get(pos.getColumn()).getRow() - 1;
	}
}
