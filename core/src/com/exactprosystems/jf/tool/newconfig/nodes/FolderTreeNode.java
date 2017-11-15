////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.tool.CssVariables;

import javafx.scene.image.Image;

import java.io.File;
import java.util.Optional;

public class FolderTreeNode extends FileTreeNode
{
	public FolderTreeNode(File file)
	{
		super(file);
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(super.isExpanded() ? CssVariables.Icons.FOLDER_ICON_OPENED : CssVariables.Icons.FOLDER_ICON));
	}
}
