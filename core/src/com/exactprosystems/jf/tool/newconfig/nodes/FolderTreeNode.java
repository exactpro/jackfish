/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
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
