////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFxNew;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileSystemTreeNode extends TreeNode
{
	private ConfigurationFxNew	model;
	private TreeItem<TreeNode>	treeItem;

	public FileSystemTreeNode(ConfigurationFxNew model, TreeItem<TreeNode> treeItem)
	{
		this.model = model;
		this.treeItem = treeItem;
	}

	@Override
	public Node getView()
	{
		return new Text("File system");
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.FILE_SYSTEM_ICON));
	}

	public void display(File[] initialFiles, List<String> ignoreFiles)
	{
		Iterator<TreeItem<TreeNode>> iterator = this.treeItem.getChildren().iterator();
		boolean needRemove = false;
		while (iterator.hasNext())
		{
			TreeItem<TreeNode> next = iterator.next();
			if (needRemove)
			{
				iterator.remove();
			}
			if (next.getValue() instanceof SeparatorTreeNode)
			{
				needRemove = true;
			}
		}
		if (initialFiles != null)
		{
			Function<File, ContextMenu> menuFolders = f ->
			{
				ContextMenu menu = new ContextMenu();

				MenuItem itemAddAsMatrix = new MenuItem("Add as matrix src", new ImageView(new Image(CssVariables.Icons.MATRIX_ICON)));
				itemAddAsMatrix.setOnAction(e -> Common.tryCatch(() -> model.addAsMatrix(f.getName()), "Error on add folder as matrix src"));

				MenuItem itemAddAsLibrary = new MenuItem("Add as library src", new ImageView(new Image(CssVariables.Icons.LIBRARY_ICON)));
				itemAddAsLibrary.setOnAction(e -> Common.tryCatch(() -> model.addAsLibrary(f.getName()), "Error on add folder as library"));

				MenuItem itemAddAsAppDic = new MenuItem("Add as app dictionary", new ImageView(new Image(CssVariables.Icons.APP_DICTIONARY_ICON)));
				itemAddAsAppDic.setOnAction(e -> Common.tryCatch(() -> model.addAppDictionaryFolder(f.getName()), "Error on add folder as app dictionary"));

				MenuItem itemAddAsClientDic = new MenuItem("Add as Client dictionary", new ImageView(new Image(CssVariables.Icons.CLIENT_DICTIONARY_ICON)));
				itemAddAsClientDic.setOnAction(e -> Common.tryCatch(() -> model.addClientDictionaryFolder(f.getName()), "Error on add folder as client dictionary"));

				MenuItem itemSetReportDir = new MenuItem("Set report dir", new ImageView(new Image(CssVariables.Icons.REPORT_ICON)));
				itemSetReportDir.setOnAction(e -> Common.tryCatch(() -> model.setReportFolder(f.getName()), "Error on set report folder"));

				menu.getItems().addAll(itemAddAsMatrix, itemAddAsLibrary, itemAddAsAppDic, itemAddAsClientDic, itemSetReportDir);
				return menu;
			};
			Arrays.stream(initialFiles)
					.sorted(ConfigurationTreeView.comparator)
					.forEach(
							file -> new BuildTree(file, this.treeItem)
									.ignoredFiles(ignoreFiles.stream().map(ConfigurationFxNew::path).collect(Collectors.toList())).menuFolder(menuFolders)
									.byPass());
		}
	}
}