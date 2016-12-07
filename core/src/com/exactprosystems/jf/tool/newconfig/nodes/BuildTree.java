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
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class BuildTree
{
	private final File initialFile;
	private final TreeItem<TreeNode> initialTreeItem;
	private Function<File, ContextMenu> menuTopFolder;
	private Predicate<File> fileFilter;
	private Function<File, ContextMenu> menuFiles;
	private Function<File, ContextMenu> menuFolder;
	private Function<File, Common.Function> doubleClickEvent;
	private List<String> ignoredFiles = new ArrayList<>();

	public BuildTree(File initialFile, TreeItem<TreeNode> initialTreeItem)
	{
		this.initialFile = initialFile;
		this.initialTreeItem = initialTreeItem;
		this.menuTopFolder = (f) -> null;
		this.fileFilter = (f) -> true;
		this.menuFiles = (f) -> null;
		this.menuFolder = (f) -> null;
		this.doubleClickEvent = (f) -> () -> {
		};
	}

	public BuildTree menuTopFolder(Function<File, ContextMenu> menuTopFolder)
	{
		this.menuTopFolder = menuTopFolder;
		return this;
	}

	public BuildTree fileFilter(Predicate<File> fileFilter)
	{
		this.fileFilter = fileFilter;
		return this;
	}

	public BuildTree menuFiles(Function<File, ContextMenu> menuFiles)
	{
		this.menuFiles = menuFiles;
		return this;
	}

	public BuildTree doubleClickEvent(Function<File, Common.Function> event)
	{
		this.doubleClickEvent = event;
		return this;
	}

	public BuildTree menuFolder(Function<File, ContextMenu> menuFolder)
	{
		this.menuFolder = menuFolder;
		return this;
	}

	public BuildTree ignoredFiles(List<String> list)
	{
		this.ignoredFiles = list;
		return this;
	}

	public void byPass()
	{
		this.byPassReq(this.initialFile, this.initialTreeItem, this.menuTopFolder, this.fileFilter, this.menuFiles, this.menuFolder, this.doubleClickEvent);
	}

	private void byPassReq(File rootFile, TreeItem<TreeNode> rootNode, Function<File, ContextMenu> topFolderMenu, Predicate<File> fileFilter, Function<File, ContextMenu> menuFiles, Function<File, ContextMenu> menuFolder, Function<File, Common.Function> doubleClickEvent)
	{
		if (this.ignoredFiles.contains(ConfigurationFx.path(rootFile)))
		{
			return;
		}
		if (rootFile.isDirectory())
		{
			FolderTreeNode folderTreeNode = new FolderTreeNode(rootFile)
			{
				@Override
				public Optional<ContextMenu> contextMenu()
				{
					List<MenuItem> items = new ArrayList<>();
					Optional.ofNullable(menuFolder).map(menu -> menu.apply(rootFile)).ifPresent(menu -> addAll(menu, items));
					Optional.ofNullable(topFolderMenu).map(menu -> menu.apply(rootFile)).ifPresent(menu -> addAll(menu, items));
					super.contextMenu().ifPresent(cm -> {
						if (!items.isEmpty())
						{
							items.add(new SeparatorMenuItem());
						}
						items.addAll(cm.getItems());
					});
					ContextMenu newMenu = new ContextMenu();
					newMenu.getItems().addAll(items);
					return Optional.of(newMenu);
				}
			};
			TreeItem<TreeNode> folderNode = new TreeItem<>(folderTreeNode);
			addListenerToExpandChild(folderNode);
			rootNode.getChildren().add(folderNode);
			Optional.ofNullable(rootFile.listFiles()).ifPresent(files -> Arrays.stream(files).sorted(ConfigurationTreeView.comparator).forEach(file -> byPassReq(file, folderNode, null, fileFilter, menuFiles, this.menuFolder, doubleClickEvent)));
		}
		else if (fileFilter.test(rootFile))
		{
			TreeItem<TreeNode> fileNode = new TreeItem<>(new FileTreeNode(rootFile)
			{
				@Override
				public Optional<ContextMenu> contextMenu()
				{
					ContextMenu contextMenu = new ContextMenu();
					Optional.ofNullable(menuFiles).map(cm -> cm.apply(rootFile)).map(ContextMenu::getItems).ifPresent(contextMenu.getItems()::addAll);
					super.contextMenu().ifPresent(cm -> {
						if (!contextMenu.getItems().isEmpty())
						{
							contextMenu.getItems().add(new SeparatorMenuItem());
						}
						contextMenu.getItems().addAll(cm.getItems());
					});
					return Optional.of(contextMenu);
				}

				@Override
				public Common.Function onDoubleClickEvent()
				{
					return doubleClickEvent.apply(rootFile);
				}
			});
			rootNode.getChildren().add(fileNode);
		}
	}

	private static void addAll(ContextMenu menu, List<MenuItem> items)
	{
		menu.getItems()
				.stream()
				.filter(item -> items.stream().noneMatch(i -> i.getText().equals(item.getText())))
				.forEach(items::add);
	}

	public static void addListenerToExpandChild(TreeItem<TreeNode> rootItem)
	{
		rootItem.expandedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue)
			{
				if (rootItem.getChildren().size() == 1 && rootItem.getChildren().get(0).getChildren().size() != 0)
				{
					rootItem.getChildren().get(0).setExpanded(true);
				}
			}
		});
	}
}
