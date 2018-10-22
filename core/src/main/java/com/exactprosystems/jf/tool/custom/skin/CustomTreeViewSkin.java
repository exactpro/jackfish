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
