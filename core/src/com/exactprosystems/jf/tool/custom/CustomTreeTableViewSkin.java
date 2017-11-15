////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom;

import com.sun.javafx.scene.control.skin.TreeTableViewSkin;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

public class CustomTreeTableViewSkin<T> extends TreeTableViewSkin<T>
{
	public CustomTreeTableViewSkin(TreeTableView<T> treeTableView)
	{
		super(treeTableView);
	}

	public void show(int index)
	{
		super.flow.show(index);
	}

	public boolean isIndexVisible(int index)
	{
		return super.flow.getFirstVisibleCell() != null &&
				super.flow.getLastVisibleCell() != null &&
				super.flow.getFirstVisibleCell().getIndex() <= index - 1 &&
				super.flow.getLastVisibleCell().getIndex() >= index + 1;
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
}
