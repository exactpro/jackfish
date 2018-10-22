/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.i18n.R;
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

	private static final SerializablePair<R, String> ADD_NEW_APP = new SerializablePair<>(R.APP_TREE_NODE_ADD_NEW_APP, CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<R, String> TEST_VERSION = new SerializablePair<>(R.APP_TREE_NODE_TEST_VERSION, null);
	private static final SerializablePair<R, String> CLOSE_APPS = new SerializablePair<>(R.APP_TREE_NODE_STOP_APP, null);
	private static final SerializablePair<R, String> REFRESH = new SerializablePair<>(R.APP_TREE_NODE_REFRESH, CssVariables.Icons.REFRESH);
	private static final SerializablePair<R, String> EXCLUDE_APP_DIC_FOLDER = new SerializablePair<>(R.APP_TREE_NODE_EXCLUDE_FOLDER, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> OPEN_DICTIONARY = new SerializablePair<>(R.APP_TREE_NODE_OPEN_DICTIONARY, CssVariables.Icons.APP_DICTIONARY_ICON);
	private static final SerializablePair<R, String> REMOVE = new SerializablePair<>(R.APP_TREE_NODE_REMOVE, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> ADD_ALL_KNOWN_PARAMS = new SerializablePair<>(R.APP_TREE_NODE_ADD_ALL_KNOWN_PARAMETERS, null);
	private static final SerializablePair<R, String> SHOW_HELP = new SerializablePair<>(R.APP_TREE_NODE_SHOW_HELP, CssVariables.Icons.HELP_ICON);

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
		ContextMenu menu = ConfigurationTreeView.add(R.APP_TREE_NODE_ADD_NEW_APP.get(),
				e -> DialogsHelper.showInputDialog(R.APP_TREE_NODE_ENTER_NEW_NAME.get(), "")
						.ifPresent(res -> Common.tryCatch(() -> this.model.addNewAppEntry(res), R.APP_TREE_NODE_ERROR_ON_ADD.get())));
		menu.getItems().addAll(
				ConfigurationTreeView.createItem(TEST_VERSION, () -> this.model.testAppVersion(), R.APP_TREE_NODE_ERROR_ON_TEST.get()),
				ConfigurationTreeView.createMenu(CLOSE_APPS, ConfigurationTreeView.createItem(ALL, null, this::closeAllApplication, R.APP_TREE_NODE_ERROR_ON_CLOSE.get())),
				ConfigurationTreeView.createDisabledItem(REFRESH),
				ConfigurationTreeView.createDisabledItem(EXCLUDE_APP_DIC_FOLDER),
				ConfigurationTreeView.createDisabledItem(OPEN_DICTIONARY),
				ConfigurationTreeView.createDisabledItem(REMOVE),
				ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMS),
				ConfigurationTreeView.createDisabledItem(SHOW_HELP),
				ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
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
										, R.APP_TREE_NODE_ERROR_ON_STOP.get()
								))
								.collect(Collectors.toList())
						);
					}
				});

	}

	@Override
	public Node getView()
	{
		return new Text(R.APP_TREE_NODE_APP_ENTRIES.get());
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
					ConfigurationTreeView.createItem(OPEN_DICTIONARY, () -> model.openAppsDictionary(getEntry()), R.APP_TREE_NODE_ERROR_OPEN_DIC.get()),
					ConfigurationTreeView.createItem(REMOVE, () -> model.removeAppEntry(getEntry()), String.format(R.APP_TREE_NODE_ERROR_REMOVE_ENTRY.get(), getEntry().toString())),
					ConfigurationTreeView.createItem(ADD_ALL_KNOWN_PARAMS, () -> model.addAllAppParams(getEntry()), String.format(R.APP_TREE_NODE_ERROR_ADD_ALL_PARAMS.get(), getEntry())),
					ConfigurationTreeView.createItem(SHOW_HELP, () -> model.showAppHelp(getEntry()), R.APP_TREE_NODE_ERROR_SHOW_HELP.get()),
					menuWizard,
					ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
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
				list.add(TablePair.TablePairBuilder.create(Configuration.appJar, getEntry().get(Configuration.appJar)).edit(true).required().pathFunc(
						() -> DialogsHelper.showOpenSaveDialog(String.format(R.APP_TREE_NODE_CHOOSE_PLUGIN.get(), getEntry().toString()), R.COMMON_JAR_FILTER.get(), "*.jar", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
				list.add(TablePair.TablePairBuilder.create(Configuration.appDicPath, getEntry().get(Configuration.appDicPath)).edit(true).required().pathFunc(
						() -> DialogsHelper.showOpenSaveDialog(R.APP_TREE_NODE_CHOOSE_DIC.get(), R.COMMON_XML_FILTER.get(), "*.xml", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
				list.add(TablePair.TablePairBuilder.create(Configuration.appWorkDir, getEntry().get(Configuration.appWorkDir)).edit(true).required().build());
				list.add(TablePair.TablePairBuilder.create(Configuration.appStartPort, getEntry().get(Configuration.appStartPort)).edit(true).required().build());
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
			return new Text(R.APP_TREE_NODE_APP_DIC.get());
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
					ConfigurationTreeView.createItem(REFRESH, () -> this.model.refreshAppDictionaries(), R.APP_TREE_NODE_ERROR_REFRESH_APP_DIC.get()),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_APP_DIC_FOLDER),
					ConfigurationTreeView.createDisabledItem(OPEN_DICTIONARY),
					ConfigurationTreeView.createDisabledItem(REMOVE),
					ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMS),
					ConfigurationTreeView.createDisabledItem(SHOW_HELP),
					ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
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
						ConfigurationTreeView.createItem(EXCLUDE_APP_DIC_FOLDER, () -> model.excludeAppDictionaryFolder(file.getName()), R.APP_TREE_NODE_ERROR_EXCLUDED_DIR.get()),
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
						ConfigurationTreeView.createItem(OPEN_DICTIONARY, () -> this.model.openAppsDictionary(file), R.APP_TREE_NODE_ERROR_OPEN_DIC.get()),
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