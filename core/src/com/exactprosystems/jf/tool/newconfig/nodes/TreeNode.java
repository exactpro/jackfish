////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.TablePair;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class TreeNode
{
	private boolean isExpanded;

	public Optional<ContextMenu> contextMenu()
	{
		return Optional.empty();
	}

	public void onContextMenuShowing(ContextMenu contextMenu)
	{

	}

	public abstract Node getView();

	public abstract Optional<Image> icon();

	//TODO think about it. We need implement all icons from css, not from code.
	public void setExpanded(boolean isExpanded)
	{
		this.isExpanded = isExpanded;
	}

	protected boolean isExpanded()
	{
		return isExpanded;
	}

	public List<TablePair> getParameters()
	{
		return null;
	}

	public void updateParameter(String key, String value)
	{

	}

	public Common.Function onDoubleClickEvent()
	{
		return null;
	}

	protected void selectFile(File file, Consumer<TreeItem<TreeNode>> consumer, TreeItem<TreeNode> nodeTreeItem)
	{
		for (TreeItem<TreeNode> treeItem : nodeTreeItem.getChildren())
		{
			if (select(treeItem, file, consumer))
			{
				return;
			}
		}
	}

	private boolean select(TreeItem<TreeNode> root, File file, Consumer<TreeItem<TreeNode>> consumer)
	{
		TreeNode value = root.getValue();
		if (value instanceof FileTreeNode)
		{
			File file1 = ((FileTreeNode) value).getFile();
			if (ConfigurationFx.path(file1).equals(ConfigurationFx.path(file)))
			{
				consumer.accept(root);
				return true;
			}
		}
		for (TreeItem<TreeNode> item : root.getChildren())
		{
			if (select(item, file, consumer))
			{
				return true;
			}
		}
		return false;
	}
}