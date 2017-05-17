////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.documents.config.AppEntry;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.TablePair;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class AppTreeNode extends TreeNode
{
	private TreeItem<TreeNode> treeItem;
	private ConfigurationFx model;
	private TreeItem<TreeNode> appTreeItem;
	private AppDictionaryTreeNode appDictionaryTreeNode;

	private static final SerializablePair<String, String> ADD_NEW_APP = new SerializablePair<>("Add new app", CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<String, String> TEST_VERSION = new SerializablePair<>("Test versions", null);
	private static final SerializablePair<String, String> REFRESH = new SerializablePair<>("Refresh", CssVariables.Icons.REFRESH);
	private static final SerializablePair<String, String> EXCLUDE_APP_DIC_FOLDER = new SerializablePair<>("Exclude app dictionary folder", CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<String, String> OPEN_DICTIONARY = new SerializablePair<>("Open dictionary", CssVariables.Icons.APP_DICTIONARY_ICON);
	private static final SerializablePair<String, String> REMOVE = new SerializablePair<>("Remove", CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<String, String> ADD_ALL_KNOWN_PARAMS = new SerializablePair<>("Add all known parameters", null);
	private static final SerializablePair<String, String> SHOW_HELP = new SerializablePair<>("Show help", CssVariables.Icons.HELP_ICON);

	public AppTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
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
		menu.getItems().addAll(
				ConfigurationTreeView.createItem(TEST_VERSION, () -> this.model.testAppVersion(), "Error on test app version"),
				ConfigurationTreeView.createDisabledItem(REFRESH),
				ConfigurationTreeView.createDisabledItem(EXCLUDE_APP_DIC_FOLDER),
				ConfigurationTreeView.createDisabledItem(OPEN_DICTIONARY),
				ConfigurationTreeView.createDisabledItem(REMOVE),
				ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMS),
				ConfigurationTreeView.createDisabledItem(SHOW_HELP),
				ConfigurationTreeView.createDisabledItem("Git", null)
		);
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

	public void display(List<AppEntry> entries, List<File> listAppsDictionaries)
	{
		this.treeItem.getChildren().clear();
		this.treeItem.getChildren().add(this.appTreeItem);
		this.appDictionaryTreeNode.display(listAppsDictionaries);
		entries.stream()
				.map(e -> new AppEntryNode(model, e))
				.map(e -> new TreeItem<TreeNode>(e))
				.forEach(i -> this.treeItem.getChildren().add(i));
	}

	private class AppEntryNode extends AbstractEntryNode<AppEntry>
	{
		public AppEntryNode(ConfigurationFx model, AppEntry entry)
		{
			super(model, entry);
		}

		@Override
		protected String getEntryName()
		{
			try
			{
				AppEntry entry = getEntry();
				StringBuilder sb = new StringBuilder(entry.toString()).append(" : ");
				String pathToJar = entry.get(Configuration.appJar);
				sb.append(pathToJar == null ? "<none>" : pathToJar.substring(pathToJar.lastIndexOf(File.separator) + 1)).append(" , ");
				String dicPath = entry.get(Configuration.appDicPath);
				sb.append(dicPath == null ? "<none>" : dicPath.substring(dicPath.lastIndexOf(File.separator) + 1));
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
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(ADD_NEW_APP),
					ConfigurationTreeView.createDisabledItem(TEST_VERSION),
					ConfigurationTreeView.createDisabledItem(REFRESH),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_APP_DIC_FOLDER),
					ConfigurationTreeView.createItem(OPEN_DICTIONARY, () -> model.openAppsDictionary(getEntry()), "Error on open dictionary"),
					ConfigurationTreeView.createItem(REMOVE, () -> model.removeAppEntry(getEntry()), String.format("Error on remove entry '%s'", getEntry().toString())),
					ConfigurationTreeView.createItem(ADD_ALL_KNOWN_PARAMS, () -> model.addAllAppParams(getEntry()), String.format("Error on add all parameters for entry '%s'", getEntry())),
					ConfigurationTreeView.createItem(SHOW_HELP, () -> model.showAppHelp(getEntry()), "Error on show help"),
					ConfigurationTreeView.createDisabledItem("Git", null)
			);
			return Optional.of(menu);
		}

		@Override
		public List<TablePair> getParameters()
		{
			try
			{
				List<TablePair> list = new ArrayList<>();
				list.add(TablePair.TablePairBuilder.create(Configuration.appDescription, getEntry().get(Configuration.appDescription)).edit(true).build());
				list.add(TablePair.TablePairBuilder.create(Configuration.appJar, getEntry().get(Configuration.appJar)).edit(true).pathFunc(
						() -> DialogsHelper.showOpenSaveDialog("Choose plugin for adapter "+getEntry().toString(), "Jar files(*.jar)", "*.jar", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
				list.add(TablePair.TablePairBuilder.create(Configuration.appDicPath, getEntry().get(Configuration.appDicPath)).edit(true).pathFunc(
						() -> DialogsHelper.showOpenSaveDialog("Choose dictionary", "Xml files(*.xml)", "*.xml", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
				list.add(TablePair.TablePairBuilder.create(Configuration.appWorkDir, getEntry().get(Configuration.appWorkDir)).edit(true).build());
				list.add(TablePair.TablePairBuilder.create(Configuration.appStartPort, getEntry().get(Configuration.appStartPort)).edit(true).build());
				getEntry().getParameters().stream()
						.map(parameter -> new TablePair(parameter.getKey(), parameter.getValue()))
						.forEach(list::add);
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

	}

	private class AppDictionaryTreeNode extends TreeNode
	{
		private TreeItem<TreeNode> treeItem;
		private ConfigurationFx model;

		public AppDictionaryTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
		{
			this.treeItem = treeItem;
			this.model = model;
			BuildTree.addListenerToExpandChild(this.treeItem);
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

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			Optional<ContextMenu> contextMenu = super.contextMenu();
			ContextMenu ret = contextMenu.orElse(new ContextMenu());
			ret.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(ADD_NEW_APP),
					ConfigurationTreeView.createDisabledItem(TEST_VERSION),
					ConfigurationTreeView.createItem(REFRESH, () -> this.model.updateAppDictionaries(), "Error on refresh app dictionaries"),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_APP_DIC_FOLDER),
					ConfigurationTreeView.createDisabledItem(OPEN_DICTIONARY),
					ConfigurationTreeView.createDisabledItem(REMOVE),
					ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMS),
					ConfigurationTreeView.createDisabledItem(SHOW_HELP),
					ConfigurationTreeView.createDisabledItem("Git", null)
			);
			return Optional.of(ret);
		}

		public void display(List<File> listAppDictionaries)
		{
			this.treeItem.getChildren().clear();
			Function<File, ContextMenu> topFolderFunc = file -> {
				ContextMenu menu = new ContextMenu();
				menu.getItems().addAll(
						ConfigurationTreeView.createDisabledItem(ADD_NEW_APP),
						ConfigurationTreeView.createDisabledItem(TEST_VERSION),
						ConfigurationTreeView.createDisabledItem(REFRESH),
						ConfigurationTreeView.createItem(EXCLUDE_APP_DIC_FOLDER, () -> model.excludeAppDictionaryFolder(file.getName()), "Error on excluded matrix directory"),
						ConfigurationTreeView.createDisabledItem(OPEN_DICTIONARY),
						ConfigurationTreeView.createDisabledItem(REMOVE),
						ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMS),
						ConfigurationTreeView.createDisabledItem(SHOW_HELP)
				);
				return menu;
			};
			Function<File,ContextMenu> filesFunc = file ->  {
				ContextMenu menu = new ContextMenu();
				menu.getItems().addAll(
						ConfigurationTreeView.createDisabledItem(ADD_NEW_APP),
						ConfigurationTreeView.createDisabledItem(TEST_VERSION),
						ConfigurationTreeView.createDisabledItem(REFRESH),
						ConfigurationTreeView.createDisabledItem(EXCLUDE_APP_DIC_FOLDER),
						ConfigurationTreeView.createItem(OPEN_DICTIONARY, () -> this.model.openAppsDictionary(file), "Error on open app dictionary"),
						ConfigurationTreeView.createDisabledItem(REMOVE),
						ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMS),
						ConfigurationTreeView.createDisabledItem(SHOW_HELP)
				);
				return menu;
			};
			listAppDictionaries.forEach(file ->
					new BuildTree(file, this.treeItem, model.getFileComparator())
							.fileFilter(f -> ConfigurationFx.getExtension(f.getAbsolutePath()).equals(Configuration.dictExt))
							.doubleClickEvent(f -> () -> this.model.openAppsDictionary(f))
							.menuTopFolder(topFolderFunc)
							.menuFiles(filesFunc)
							.byPass()
			);
		}
	}
}