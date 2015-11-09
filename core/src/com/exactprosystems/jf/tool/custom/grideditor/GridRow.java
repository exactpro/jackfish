////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
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

	/***************************************************************************
	 * * Constructor * *
	 **************************************************************************/
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
