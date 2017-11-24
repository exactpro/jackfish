////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.skin;

import com.sun.javafx.scene.control.skin.TreeViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class CustomTreeViewSkin<T> extends TreeViewSkin<T>
{
	public CustomTreeViewSkin(TreeView treeView)
	{
		super(treeView);
	}

	public void scrollAndSelect(int index)
	{
		TreeItem<T> treeItem = getSkinnable().getTreeItem(index);

		expandAll(treeItem);

		scrollTo(index);
		getSkinnable().getSelectionModel().select(index);
	}

	public void scrollAndSelect(TreeItem<T> treeItem)
	{
		expandAll(treeItem);

		int index = getSkinnable().getRow(treeItem);
		scrollTo(index);
		getSkinnable().getSelectionModel().select(treeItem);
	}

	public void scrollTo(int index)
	{
		if (!isIndexVisible(index))
		{
			show(index);
		}
	}

	public void scrollTo(TreeItem<T> treeItem)
	{
		scrollTo(super.getSkinnable().getRow(treeItem));
	}

	@Override
	protected VirtualFlow<TreeCell<T>> createVirtualFlow()
	{
		return new CustomVirtualFlow<>();
	}

	//region private methods
	private void expandAll(TreeItem<T> item)
	{
		TreeItem<T> parent = item.getParent();
		while (parent != null && !parent.isExpanded())
		{
			parent.setExpanded(true);
			parent = parent.getParent();
		}
	}

	private boolean isIndexVisible(int index)
	{
		return flow.getFirstVisibleCell() != null && flow.getLastVisibleCell() != null && flow.getFirstVisibleCell().getIndex() <= index - 1 && flow.getLastVisibleCell().getIndex() >= index + 1;
	}

	private void show(int index)
	{
		flow.show(index);
	}
	//endregion
}
