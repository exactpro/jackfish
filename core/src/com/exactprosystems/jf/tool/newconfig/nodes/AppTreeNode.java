////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFxNew;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.TablePair;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AppTreeNode extends TreeNode
{
	private TreeItem<TreeNode> treeItem;
	private ConfigurationFxNew model;
	private TreeItem<TreeNode> appTreeItem;
	private AppDictionaryTreeNode appDictionaryTreeNode;

	public AppTreeNode(ConfigurationFxNew model, TreeItem<TreeNode> treeItem)
	{
		this.treeItem = treeItem;
		this.model = model;
		this.appTreeItem = new TreeItem<>();
		this.appDictionaryTreeNode = new AppDictionaryTreeNode(model, this.appTreeItem);
		this.appTreeItem.setValue(this.appDictionaryTreeNode);
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = ConfigurationTreeView.add("Add new app",
				e -> ConfigurationTreeView.showInputDialog("Enter new app name")
						.ifPresent(res -> Common.tryCatch(() -> this.model.addNewAppEntry(res), "Error on add new application")));
		MenuItem itemTestVersion = new MenuItem("Test versions");
		itemTestVersion.setOnAction(e -> Common.tryCatch(() -> this.model.testAppVersion(), "Error on test app version"));
		menu.getItems().add(itemTestVersion);
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		return new Text("App entries");
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.APP_ICON));
	}

	public void display(List<Configuration.AppEntry> entries, Map<String, SupportedEntry> map, List<File> listAppsDictionaries)
	{
		this.treeItem.getChildren().clear();
		this.treeItem.getChildren().add(this.appTreeItem);
		this.appDictionaryTreeNode.display(listAppsDictionaries);
		entries.stream()
				.map(e -> new AppEntryNode(model, e, map.get(e.toString())))
				.map(e -> new TreeItem<TreeNode>(e))
				.forEach(i -> this.treeItem.getChildren().add(i));
	}

	private class AppEntryNode extends AbstractEntryNode<Configuration.AppEntry>
	{
		private SupportedEntry supportedEntry;

		public AppEntryNode(ConfigurationFxNew model, Configuration.AppEntry entry, SupportedEntry supportedEntry)
		{
			super(model, entry);
			this.supportedEntry = supportedEntry;
		}

		@Override
		protected String getEntryName()
		{
			try
			{
				Configuration.AppEntry entry = getEntry();
				StringBuilder sb = new StringBuilder(entry.toString()).append(" : ");
				String pathToJar = entry.get(Configuration.appJar);
				sb.append(pathToJar.substring(pathToJar.lastIndexOf(File.separator) + 1)).append(" , ");
				String dicPath = entry.get(Configuration.appDicPath);
				sb.append(dicPath.substring(dicPath.lastIndexOf(File.separator) + 1));
				return sb.toString();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return super.getEntryName();
		}

		@Override
		protected Optional<String> getDescription()
		{
			try
			{
				return Optional.ofNullable(getEntry().get(Configuration.appDescription));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return super.getDescription();
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			MenuItem itemRemove = new MenuItem("Remove", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			itemRemove.setOnAction(e -> Common.tryCatch(() -> model.removeAppEntry(getEntry()), String.format("Error on remove entry '%s'", getEntry().toString())));

			MenuItem itemAddAll = new MenuItem("Add all known params");
			itemAddAll.setOnAction(e -> Common.tryCatch(() -> model.addAllAppParams(getEntry()), String.format("Error on add all parameters for entry '%s'", getEntry())));

			MenuItem itemOpenDictionary = new MenuItem("Open dictionary");
			itemOpenDictionary.setOnAction(e -> Common.tryCatch(() -> model.openAppsDictionary(getEntry()), "Error on open dictionary"));

			MenuItem itemShowHelp = new MenuItem("Show help", new ImageView(new Image(CssVariables.Icons.HELP_ICON)));
			itemShowHelp.setOnAction(e -> Common.tryCatch(() -> model.showAppHelp(getEntry()), "Error on show help"));

			menu.getItems().addAll(itemRemove, itemOpenDictionary, itemAddAll, itemShowHelp);
			return Optional.of(menu);
		}

		@Override
		public List<TablePair> getParameters()
		{
			try
			{
				List<TablePair> list = new ArrayList<>();
				list.add(TablePair.TablePairBuilder.create().key(Configuration.appDescription).value(getEntry().get(Configuration.appDescription)).edit(true).build());
				list.add(TablePair.TablePairBuilder.create().key(Configuration.appJar).value(getEntry().get(Configuration.appJar)).edit(true).isPath(true).build());
				list.add(TablePair.TablePairBuilder.create().key(Configuration.appDicPath).value(getEntry().get(Configuration.appDicPath)).edit(true).isPath(true).build());
				list.add(TablePair.TablePairBuilder.create().key(Configuration.appWorkDir).value(getEntry().get(Configuration.appWorkDir)).edit(true).build());
				list.add(TablePair.TablePairBuilder.create().key(Configuration.appStartPort).value(getEntry().get(Configuration.appStartPort)).edit(true).build());
				getEntry().getParameters().stream()
						.map(parameter -> new TablePair(parameter.getKey(), parameter.getValue()))
						.forEach(tp -> list.add(tp));
				return list;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public Common.Function onDoubleClickEvent()
		{
			return () -> model.openAppsDictionary(getEntry());
		}

		@Override
		protected SupportedEntry getSupportedEntry()
		{
			return this.supportedEntry;
		}
	}

	private class AppDictionaryTreeNode extends TreeNode
	{
		private TreeItem<TreeNode> treeItem;
		private ConfigurationFxNew model;

		public AppDictionaryTreeNode(ConfigurationFxNew model, TreeItem<TreeNode> treeItem)
		{
			this.treeItem = treeItem;
			this.model = model;
		}

		@Override
		public Node getView()
		{
			return new Text("App dictionaries");
		}

		@Override
		public Optional<Image> icon()
		{
			return Optional.of(new Image(CssVariables.Icons.APP_DICTIONARY_ICON));
		}

		public void display(List<File> listAppDictionaries)
		{
			this.treeItem.getChildren().clear();
			Function<File, ContextMenu> topFolderFunc = file -> {
				ContextMenu menu = new ContextMenu();
				MenuItem itemRemove = new MenuItem("Remove app dictionary dir", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
				itemRemove.setOnAction(e -> Common.tryCatch(() -> model.removeAppDictionaryFolder(file), "Error on remove matrix directory"));
				menu.getItems().addAll(itemRemove);
				return menu;
			};
			Function<File,ContextMenu> filesFunc = file ->  {
				ContextMenu menu = new ContextMenu();
				MenuItem itemOpedDic = new MenuItem("Open dictionary", new ImageView(new Image(CssVariables.Icons.APP_DICTIONARY_ICON)));
				itemOpedDic.setOnAction(e -> Common.tryCatch(() -> this.model.openAppsDictionary(file), "Error on open app dictionary"));
				menu.getItems().addAll(itemOpedDic);
				return menu;
			};
			listAppDictionaries.forEach(file ->
					new BuildTree(file, this.treeItem)
							.fileFilter(f -> ConfigurationFxNew.getExtension(f.getAbsolutePath()).equals(ConfigurationFxNew.DICTIONARY_EXTENSION))
							.doubleClickEvent(f -> () -> this.model.openAppsDictionary(f))
							.menuTopFolder(topFolderFunc)
							.menuFiles(filesFunc)
							.byPass()
			);
		}
	}
}