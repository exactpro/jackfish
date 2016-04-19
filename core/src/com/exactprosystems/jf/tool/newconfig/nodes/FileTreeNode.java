////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.Image;
import javafx.scene.text.Text;

import java.io.File;
import java.util.Optional;

public class FileTreeNode extends TreeNode
{
	private File file;

	public FileTreeNode(File file)
	{
		this.file = file;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		return Optional.of(ConfigurationTreeView.gitContextMenu(getFile()));
	}

	@Override
	public Node getView()
	{
		return new Text(this.file.getName());
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.empty();
	}

	public File getFile()
	{
		return file;
	}
}
