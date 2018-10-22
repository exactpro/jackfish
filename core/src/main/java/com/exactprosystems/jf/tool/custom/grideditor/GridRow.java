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

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;

public class GridRow extends TableRow<ObservableList<SpreadsheetCell>>
{

	private final SpreadsheetHandle handle;
	DoubleProperty verticalShift = new SimpleDoubleProperty();

	public GridRow(SpreadsheetHandle handle)
	{
		super();
		this.handle = handle;
		this.indexProperty().addListener(setPrefHeightListener);
		this.visibleProperty().addListener(setPrefHeightListener);

		handle.getView().providerProperty().addListener(setPrefHeightListener);

		handle.getCellsViewSkin().rowHeightMap.addListener((MapChangeListener<Integer, Double>) change -> {
			if (change.wasAdded() && change.getKey() == getIndex())
			{
				setRowHeight(change.getValueAdded());
			}
			else if (change.wasRemoved() && change.getKey() == getIndex())
			{
				setRowHeight(computePrefHeight(-1));
			}
		});
	}

	void addCell(CellView cell)
	{
		getChildren().add(cell);
	}

	void removeCell(CellView gc)
	{
		getChildren().remove(gc);
	}

	SpreadsheetView getSpreadsheetView()
	{
		return handle.getView();
	}

	@Override
	protected double computePrefHeight(double width)
	{
		return handle.getCellsViewSkin().getRowHeight(getIndex());
	}

	@Override
	protected double computeMinHeight(double width)
	{
		return handle.getCellsViewSkin().getRowHeight(getIndex());
	}

	@Override
	protected Skin<?> createDefaultSkin()
	{
		return new GridRowSkin(handle, this);
	}

	private final InvalidationListener setPrefHeightListener = o -> setRowHeight(computePrefHeight(-1));

	public void setRowHeight(double height)
	{
		CellView.getValue(() -> setHeight(height));

		setPrefHeight(height);
		handle.getCellsViewSkin().rectangleSelection.updateRectangle();
	}
}
