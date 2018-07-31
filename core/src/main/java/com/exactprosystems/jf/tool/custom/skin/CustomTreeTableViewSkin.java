/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.custom.skin;

import com.sun.javafx.scene.control.skin.TreeTableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;

public class CustomTreeTableViewSkin<T> extends TreeTableViewSkin<T>
{
	public CustomTreeTableViewSkin(TreeTableView<T> treeTableView)
	{
		super(treeTableView);
	}

	public void scrollTo(int index)
	{
		if (!this.isIndexVisible(index))
		{
			this.show(index);
		}
	}

	@Override
	public void resizeColumnToFitContent(TreeTableColumn<T, ?> tc, int maxRows)
	{
		super.resizeColumnToFitContent(tc, maxRows);
	}

	public ScrollBar getVSB()
	{
		return (ScrollBar) super.queryAccessibleAttribute(AccessibleAttribute.VERTICAL_SCROLLBAR);
	}

	@Override
	protected VirtualFlow<TreeTableRow<T>> createVirtualFlow()
	{
		return new CustomVirtualFlow<>();
	}

	//region private methods
	private void show(int index)
	{
		super.flow.show(index);
	}

	private boolean isIndexVisible(int index)
	{
		return super.flow.getFirstVisibleCell() != null &&
				super.flow.getLastVisibleCell() != null &&
				super.flow.getFirstVisibleCell().getIndex() <= index - 1 &&
				super.flow.getLastVisibleCell().getIndex() >= index + 1;
	}
	//endregion
}
