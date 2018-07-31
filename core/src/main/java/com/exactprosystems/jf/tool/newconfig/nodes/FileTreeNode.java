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

import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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
		ContextMenu value = ConfigurationTreeView.gitContextMenu(getFile());

		MenuItem copyName = new MenuItem(R.FILE_TN_COPY_NAME.get());
		copyName.setOnAction(e -> Sys.copyToClipboard(this.file.getName()));
		value.getItems().add(0, copyName);

		return Optional.of(value);
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
