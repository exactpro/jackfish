////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.config.AppEntry;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.TablePair;
import com.exactprosystems.jf.tool.wizard.WizardButton;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AppTreeNode extends TreeNode
{
	private TreeItem<TreeNode> treeItem;
	private ConfigurationFx model;
	private TreeItem<TreeNode> appTreeItem;
	private AppDictionaryTreeNode appDictionaryTreeNode;

	private static final String ALL = "All";

	private static final SerializablePair<String, String> ADD_NEW_APP = new SerializablePair<>("Add new app", CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<String, String> TEST_VERSION = new SerializablePair<>("Test versions", null);
	private static final SerializablePair<String, String> CLOSE_APPS = new SerializablePair<>("Stop apps", null);
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
				e -> DialogsHelper.showInputDialog("Enter new app name", "")
						.ifPresent(res -> Common.tryCatch(() -> this.model.addNewAppEntry(res), "Error on add new application")));
		menu.getItems().addAll(
				ConfigurationTreeView.createItem(TEST_VERSION, () -> this.model.testAppVersion(), "Error on test app version"),
				ConfigurationTreeView.createMenu(CLOSE_APPS, ConfigurationTreeView.createItem(ALL, null, this::closeAllApplication, "Error on close all application")),
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
	public void onContextMenuShowing(ContextMenu contextMenu)
	{
		contextMenu.getItems()
				.stream()
				.filter(item -> item.getText().equals(CLOSE_APPS.getKey()))
				.findFirst()
				.map(item -> (Menu) item)
				.ifPresent(menu -> {
					IApplicationPool applicationPool = this.model.getApplicationPool();
					List<AppConnection> connections = applicationPool.getConnections();
					if (connections.size() > 0)
					{
						menu.getItems().removeIf(menuItem -> !menuItem.getText().equals(ALL));
						menu.getItems().add(new SeparatorMenuItem());
						menu.getItems().addAll(connections
								.stream()
								.map(appConnection -> ConfigurationTreeView.createItem(
										appConnection.toString()
										, null
										, () -> applicationPool.stopApplication(appConnection, true)
										, "Error on stop application. See log for details"
								))
								.collect(Collectors.toList())
						);
					}
				});

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

	private void closeAllApplication()
	{
		Task<Void> task = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				Common.progressBarVisible(true);
				model.getApplicationPool().stopAllApplications(true);
				return null;
			}
		};
		task.setOnFailed(e -> Common.progressBarVisible(false));
		task.setOnSucceeded(e -> Common.progressBarVisible(false));
		new Thread(task).start();
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

			Menu menuWizard = WizardButton.createMenu();
			WizardManager manager = model.getFactory().getWizardManager();
			Object[] criteries = new Object[]{ model.getApplicationPool(), this.getEntry()};

			java.util.List<Class<? extends Wizard>> suitableWizards = manager.suitableWizards(criteries);
			menuWizard.getItems().clear();
			menuWizard.getItems().addAll(suitableWizards.stream().map(wizardClass ->
			{
				MenuItem menuItem = new MenuItem(manager.nameOf(wizardClass));
				menuItem.setOnAction(e -> manager.runWizard(wizardClass, model.getFactory().createContext(), criteries));
				return menuItem;
			}).collect(Collectors.toList()));

			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(ADD_NEW_APP),
					ConfigurationTreeView.createDisabledItem(TEST_VERSION),
					ConfigurationTreeView.createDisabledMenu(CLOSE_APPS),
					ConfigurationTreeView.createDisabledItem(REFRESH),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_APP_DIC_FOLDER),
					ConfigurationTreeView.createItem(OPEN_DICTIONARY, () -> model.openAppsDictionary(getEntry()), "Error on open dictionary"),
					ConfigurationTreeView.createItem(REMOVE, () -> model.removeAppEntry(getEntry()), String.format("Error on remove entry '%s'", getEntry().toString())),
					ConfigurationTreeView.createItem(ADD_ALL_KNOWN_PARAMS, () -> model.addAllAppParams(getEntry()), String.format("Error on add all parameters for entry '%s'", getEntry())),
					ConfigurationTreeView.createItem(SHOW_HELP, () -> model.showAppHelp(getEntry()), "Error on show help"),
					menuWizard,
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

	private static class AppDictionaryTreeNode extends TreeNode
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
					ConfigurationTreeView.createDisabledMenu(CLOSE_APPS),
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
						ConfigurationTreeView.createDisabledMenu(CLOSE_APPS),
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
						ConfigurationTreeView.createDisabledMenu(CLOSE_APPS),
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