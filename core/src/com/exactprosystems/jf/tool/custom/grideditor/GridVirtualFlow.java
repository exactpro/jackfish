////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.beans.binding.When;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableRow;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class GridVirtualFlow<T extends IndexedCell<?>> extends VirtualFlow<T>
{

	private static final Comparator<GridRow> ROWCMP = (firstRow, secondRow) -> secondRow.getIndex() - firstRow.getIndex();

	private SpreadsheetView spreadSheetView;
	private final GridViewSkin gridViewSkin;
	private final ArrayList<T> myFixedCells = new ArrayList<>();
	public final List<Node> sheetChildren;

	public GridVirtualFlow(GridViewSkin gridViewSkin)
	{
		super();
		this.gridViewSkin = gridViewSkin;
		final ChangeListener<Number> listenerY = (ov, t, t1) -> layoutTotal();
		getVbar().valueProperty().addListener(listenerY);
		ChangeListener<Number> hBarValueChangeListener = ((observable, oldValue, newValue) -> gridViewSkin.hBarValue.clear());
		getHbar().valueProperty().addListener(hBarValueChangeListener);
		widthProperty().addListener(hBarValueChangeListener);

		sheetChildren = findSheetChildren();
	}

	public void init(SpreadsheetView spv)
	{
		getHbar().maxProperty().addListener((observable, oldValue, newValue) -> {
			getHbar().setBlockIncrement(getWidth());
			getHbar().setUnitIncrement(newValue.doubleValue() / 20);
		});

		this.spreadSheetView = spv;

		Rectangle rec = new Rectangle();
		rec.widthProperty().bind(widthProperty().subtract(new When(getVbar().visibleProperty()).then(getVbar().widthProperty()).otherwise(0)));
		rec.heightProperty().bind(heightProperty().subtract(new When(getHbar().visibleProperty()).then(getHbar().heightProperty()).otherwise(0)));
		gridViewSkin.rectangleSelection.setClip(rec);

		getChildren().add(gridViewSkin.rectangleSelection);
	}

	@Override
	public void show(int index)
	{
		super.show(index);
		layoutTotal();
	}

	@Override
	public void scrollTo(int index)
	{
		super.scrollTo(index);
		layoutTotal();
	}

	@Override
	public double adjustPixels(final double delta)
	{
		final double returnValue = super.adjustPixels(delta);
		layoutTotal();
		return returnValue;
	}

	List<T> getFixedCells()
	{
		return myFixedCells;
	}

	GridRow getTopRow()
	{
		if (!sheetChildren.isEmpty())
		{
			return (GridRow) sheetChildren.get(sheetChildren.size() - 1);
		}
		return null;
	}

	@Override
	protected void layoutChildren()
	{
		if (spreadSheetView != null)
		{
			sortRows();
			super.layoutChildren();
			layoutTotal();

			if (getVbar().getVisibleAmount() == 0.0 && getVbar().isVisible() && getCells().size() != getCellCount())
			{
				getVbar().setMax(1);
				getVbar().setVisibleAmount(getCells().size() / (float) getCellCount());
			}
		}
	}

	protected void layoutTotal()
	{
		sortRows();

		for (GridRow row : gridViewSkin.deportedCells.keySet())
		{
			gridViewSkin.deportedCells.get(row).forEach(row::removeCell);
		}
		gridViewSkin.deportedCells.clear();
		if (getCells().isEmpty())
		{
			reconfigureCells();
		}
		((List<GridRow>) getCells()).stream().filter(cell -> cell != null && (!gridViewSkin.hBarValue.get(cell.getIndex()) || gridViewSkin.rowToLayout.get(cell.getIndex()))).forEach(GridRow::requestLayout);
	}

	protected ScrollBar getVerticalBar()
	{
		return getVbar();
	}

	protected ScrollBar getHorizontalBar()
	{
		return getHbar();
	}

	@Override
	protected List<T> getCells()
	{
		return super.getCells();
	}

	private List<Node> findSheetChildren()
	{
		if (!getChildren().isEmpty())
		{
			if (getChildren().get(0) instanceof Region)
			{
				Region region = (Region) getChildren().get(0);
				if (!region.getChildrenUnmodifiable().isEmpty())
				{
					if (region.getChildrenUnmodifiable().get(0) instanceof Group)
					{
						return ((Group) region.getChildrenUnmodifiable().get(0)).getChildren();
					}
				}
			}
		}
		return new ArrayList<>();
	}

	/**
	 * Sort the rows so that they stay in order for layout
	 */
	private void sortRows()
	{
		final List<GridRow> temp = (List<GridRow>) getCells();
		final List<GridRow> tset = new ArrayList<>(temp);
		Collections.sort(tset, ROWCMP);
		tset.forEach(TableRow::toFront);
	}
}

