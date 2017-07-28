package com.exactprosystems.jf.tool.custom;

import com.sun.javafx.scene.control.skin.TreeViewSkin;
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


	//region pritvate methods
	private void expandAll(TreeItem<T> item)
	{
		TreeItem<T> parent = item.getParent();
		while (parent != null && !parent.isExpanded())
		{
			parent.setExpanded(true);
			parent = parent.getParent();
		}
	}

	private void scrollTo(int index)
	{
		if (!isIndexVisible(index))
		{
			show(index);
		}
	}

	private boolean isIndexVisible(int index)
	{
		return flow.getFirstVisibleCell() != null &&
				flow.getLastVisibleCell() != null &&
				flow.getFirstVisibleCell().getIndex() <= index - 1 &&
				flow.getLastVisibleCell().getIndex() >= index + 1;
	}

	private void show(int index)
	{
		flow.show(index);
	}
	//endregion
}
