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

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumnBase;

public class HorizontalHeaderColumn extends NestedTableColumnHeader
{
	int lastColumnResized = -1;

	public HorizontalHeaderColumn(TableViewSkinBase<?, ?, ?, ?, ?, ?> skin, TableColumnBase<?, ?> tc)
	{
		super(skin, tc);
		widthProperty().addListener((Observable observable) -> {
			((GridViewSkin) skin).hBarValue.clear();
			((GridViewSkin) skin).rectangleSelection.updateRectangle();
		});

		columnReorderLine.layoutXProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			HorizontalHeader headerRow = (HorizontalHeader) skin.getTableHeaderRow();
			GridViewSkin mySkin = ((GridViewSkin) skin);
			if (newValue.intValue() == 0 && lastColumnResized >= 0 && headerRow.selectedColumns.get(lastColumnResized))
			{
				double width1 = mySkin.getColumns().get(lastColumnResized).getWidth();
				for (int i = headerRow.selectedColumns.nextSetBit(0); i >= 0; i = headerRow.selectedColumns.nextSetBit(i + 1))
				{
					mySkin.getColumns().get(i).setPrefWidth(width1);
				}
			}
		});
	}

	@Override
	protected TableColumnHeader createTableColumnHeader(final TableColumnBase col)
	{
		TableViewSkinBase<?, ?, ?, ?, ?, TableColumnBase<?, ?>> tableViewSkin = getTableViewSkin();
		if (col.getColumns().isEmpty())
		{
			final TableColumnHeader columnHeader = new TableColumnHeader(tableViewSkin, col);
			columnHeader.setOnMousePressed(mouseEvent -> {
				if (mouseEvent.getClickCount() == 2 && mouseEvent.isPrimaryButtonDown()) {
					int columnIndex = getColumnHeaders().indexOf(columnHeader);
					SpreadsheetColumn spreadsheetColumn = ((GridViewSkin) (Object) tableViewSkin).handle.getView().getColumns().get(columnIndex);
					Platform.runLater(spreadsheetColumn::startRenameColumn);
				}
			});
			return columnHeader;
		}
		else
		{
			return new HorizontalHeaderColumn(getTableViewSkin(), col);
		}
	}

	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();
		layoutFixedColumns();
	}

	public void layoutFixedColumns()
	{
		SpreadsheetHandle handle = ((GridViewSkin) (Object) getTableViewSkin()).handle;
		final SpreadsheetView spreadsheetView = handle.getView();
		if (handle.getCellsViewSkin() == null || getChildren().isEmpty())
		{
			return;
		}
		int max = getColumnHeaders().size();
		max = max > spreadsheetView.getColumns().size() ? spreadsheetView.getColumns().size() : max;
		for (int j = 0; j < max; j++)
		{
			final TableColumnHeader n = getColumnHeaders().get(j);
			n.setPrefHeight(24.0);
		}

	}
}
