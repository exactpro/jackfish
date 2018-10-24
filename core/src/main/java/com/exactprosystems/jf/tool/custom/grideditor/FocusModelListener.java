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
