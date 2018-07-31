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

import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.config.ClientEntry;
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

public class ClientTreeNode extends TreeNode
{
	private ConfigurationFx				model;
	private TreeItem<TreeNode>			treeItem;
	private TreeItem<TreeNode>			clientTreeItem;
	private ClientDictionaryTreeNode	clientDictionaryTreeNode;


	private static final SerializablePair<R, String> ADD_NEW_CLIENT = new SerializablePair<>(R.CLIENT_TREE_NODE_ADD_NEW_CLIENT, CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<R, String> TEST_VERSION = new SerializablePair<>(R.CLIENT_TREE_NODE_TEST_VERSIONS, null);
	private static final SerializablePair<R, String> REFRESH = new SerializablePair<>(R.CLIENT_TREE_NODE_REFRESH, CssVariables.Icons.REFRESH);
	private static final SerializablePair<R, String> EXCLUDE_CLIENT_DIC_FOLDER = new SerializablePair<>(R.CLIENT_TREE_NODE_EXCLUDE_FOLDER, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> OPEN_DICTIONARY = new SerializablePair<>(R.CLIENT_TREE_NODE_OPEN_DICTIONARY, CssVariables.Icons.APP_DICTIONARY_ICON);
	private static final SerializablePair<R, String> REMOVE = new SerializablePair<>(R.CLIENT_TREE_NODE_REMOVE, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> SHOW_POSSIBILITIES = new SerializablePair<>(R.CLIENT_TREE_NODE_POSSIBILITIES, null);
	private static final SerializablePair<R, String> ADD_ALL_KNOWN_PARAMETERS = new SerializablePair<>(R.CLIENT_TREE_NODE_ADD_ALL_KNOWN_PARAMETERS, CssVariables.Icons.HELP_ICON);

	public ClientTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
	{
		this.model = model;
		this.treeItem = treeItem;
		this.clientTreeItem = new TreeItem<>();
		this.clientDictionaryTreeNode = new ClientDictionaryTreeNode(model, this.clientTreeItem);
		this.clientTreeItem.setValue(this.clientDictionaryTreeNode);
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = ConfigurationTreeView.add(
				R.CLIENT_TREE_NODE_ADD_CLIENT.get(),
				e -> DialogsHelper.showInputDialog(R.CLIENT_TREE_NODE_ENTRE_NEW_NAME.get(), "").ifPresent(
						res -> Common.tryCatch(() -> this.model.addNewClientEntry(res), R.CLIENT_TREE_NODE_ERROR_ADD_NEW_CLIENT.get())));
		menu.getItems().addAll(
				ConfigurationTreeView.createItem(TEST_VERSION, () -> this.model.testClientVersion(), R.CLIENT_TREE_NODE_ERROR_ON_TEST.get()),
				ConfigurationTreeView.createDisabledItem(REFRESH),
				ConfigurationTreeView.createDisabledItem(EXCLUDE_CLIENT_DIC_FOLDER),
				ConfigurationTreeView.createDisabledItem(OPEN_DICTIONARY),
				ConfigurationTreeView.createDisabledItem(REMOVE),
				ConfigurationTreeView.createDisabledItem(SHOW_POSSIBILITIES),
				ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMETERS),
				ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
		);
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		return new Text(R.CLIENT_TREE_NODE_CLIENT_ENTRIES.get());
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.CLIENT_ICON));
	}

	public void display(List<ClientEntry> clientEntries, List<File> listClientDictionaries)
	{
		this.treeItem.getChildren().clear();
		this.treeItem.getChildren().add(this.clientTreeItem);
		this.clientDictionaryTreeNode.display(listClientDictionaries);
		clientEntries.stream()
				.map(e -> new ClientEntryNode(model, e))
				.map(e -> new TreeItem<TreeNode>(e))
				.forEach(i -> this.treeItem.getChildren().add(i));
	}

	private class ClientEntryNode extends AbstractEntryNode<ClientEntry>
	{
		public ClientEntryNode(ConfigurationFx model, ClientEntry entry)
		{
			super(model, entry);
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(ADD_NEW_CLIENT),
					ConfigurationTreeView.createDisabledItem(TEST_VERSION),
					ConfigurationTreeView.createDisabledItem(REFRESH),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_CLIENT_DIC_FOLDER),
					ConfigurationTreeView.createDisabledItem(OPEN_DICTIONARY),
					ConfigurationTreeView.createItem(REMOVE, () -> model.removeClientEntry(getEntry()), String.format(R.CLIENT_TREE_NODE_ERROR_REMOVE_ENTRY.get(), getEntry().toString())),
					ConfigurationTreeView.createItem(SHOW_POSSIBILITIES, () -> model.showPossibilities(getEntry()), String.format(R.CLIENT_TREE_NODE_ERROR_ON_SHOW_POSSIBILITIES.get(), getEntry().toString())),
					ConfigurationTreeView.createItem(ADD_ALL_KNOWN_PARAMETERS, () -> model.addAllClientParams(getEntry()),String.format(R.CLIENT_TREE_NODE_ERROR_ADD_ALL_PARAMS.get(), getEntry())),
					ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
			);
			return Optional.of(menu);
		}

		@Override
		protected String getEntryName()
		{
			try
			{
				ClientEntry entry = getEntry();
				StringBuilder sb = new StringBuilder(entry.toString()).append(" : ");
				String pathToJar = entry.get(Configuration.clientJar);
				sb.append(pathToJar.substring(pathToJar.lastIndexOf(File.separator) + 1)).append(" , ");
				String pathToDic = entry.get(Configuration.clientDictionary);
				sb.append(pathToDic.substring(pathToDic.lastIndexOf(File.separator) + 1));
				return sb.toString();
			}
			catch (Exception e)
			{

			}
			return super.getEntryName();
		}

		@Override
		protected Optional<String> getDescription()
		{
			try
			{
				return Optional.of(getEntry().get(Configuration.clientDescription));
			}
			catch (Exception e)
			{

			}
			return super.getDescription();
		}

		@Override
		public List<TablePair> getParameters()
		{
			try
			{
				List<TablePair> list = new ArrayList<>();
				list.add(TablePair.TablePairBuilder.create(Configuration.clientDescription, getEntry().get(Configuration.clientDescription)).edit(true).build());
				list.add(TablePair.TablePairBuilder.create(Configuration.clientJar, getEntry().get(Configuration.clientJar)).edit(true).required().pathFunc(
						() -> DialogsHelper.showOpenSaveDialog(String.format(R.CLIENT_TREE_NODE_CHOOSE_CLIENT.get(), getEntry().toString()), R.COMMON_JAR_FILTER.get(), "*.jar", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
				list.add(TablePair.TablePairBuilder.create(Configuration.clientDictionary, getEntry().get(Configuration.clientDictionary)).required().edit(true).pathFunc(
						() -> DialogsHelper.showOpenSaveDialog(R.CLIENT_TREE_NODE_CHOOSE_CLIENT_DIC.get(), R.COMMON_XML_FILTER.get(), "*.xml", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
				list.add(TablePair.TablePairBuilder.create(Configuration.clientLimit, getEntry().get(Configuration.clientLimit)).edit(true).build());
				getEntry().getParameters().stream().map(parameter -> new TablePair(parameter.getKey(), parameter.getValue())).forEach(list::add);
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
			return () -> model.openClientDictionary(getEntry());
		}

	}

	private static class ClientDictionaryTreeNode extends TreeNode
	{
		private TreeItem<TreeNode>	treeItem;
		private ConfigurationFx	model;

		public ClientDictionaryTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
		{
			this.treeItem = treeItem;
			this.model = model;
			BuildTree.addListenerToExpandChild(this.treeItem);
		}

		@Override
		public Node getView()
		{
			return new Text(R.CLIENT_TREE_NODE_CLIENT_DIC.get());
		}

		@Override
		public Optional<Image> icon()
		{
			return Optional.of(new Image(CssVariables.Icons.CLIENT_DICTIONARY_ICON));
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			Optional<ContextMenu> menu = super.contextMenu();
			ContextMenu ret = menu.orElse(new ContextMenu());
			ret.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(ADD_NEW_CLIENT),
					ConfigurationTreeView.createDisabledItem(TEST_VERSION),
					ConfigurationTreeView.createItem(REFRESH, () -> this.model.refreshClientDictionaries(), R.CLIENT_TREE_NODE_REFRESH_CLIENT_DIC.get()),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_CLIENT_DIC_FOLDER),
					ConfigurationTreeView.createDisabledItem(OPEN_DICTIONARY),
					ConfigurationTreeView.createDisabledItem(REMOVE),
					ConfigurationTreeView.createDisabledItem(SHOW_POSSIBILITIES),
					ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMETERS),
					ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
			);
			return Optional.of(ret);
		}

		public void display(List<File> listClientDictionaries)
		{
			this.treeItem.getChildren().clear();
			Function<File, ContextMenu> topFolderFunc = file ->
			{
				ContextMenu menu = new ContextMenu();
				menu.getItems().addAll(
						ConfigurationTreeView.createDisabledItem(ADD_NEW_CLIENT),
						ConfigurationTreeView.createDisabledItem(TEST_VERSION),
						ConfigurationTreeView.createDisabledItem(REFRESH),
						ConfigurationTreeView.createItem(EXCLUDE_CLIENT_DIC_FOLDER, () -> model.excludeClientDictionaryFolder(file.getName()), R.CLIENT_TREE_NODE_ERROR_EXCLUDED_DIR.get()),
						ConfigurationTreeView.createDisabledItem(OPEN_DICTIONARY),
						ConfigurationTreeView.createDisabledItem(REMOVE),
						ConfigurationTreeView.createDisabledItem(SHOW_POSSIBILITIES),
						ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMETERS)
				);
				return menu;
			};
			Function<File, ContextMenu> filesFunc = file ->
			{
				ContextMenu menu = new ContextMenu();
				menu.getItems().addAll(
						ConfigurationTreeView.createDisabledItem(ADD_NEW_CLIENT),
						ConfigurationTreeView.createDisabledItem(TEST_VERSION),
						ConfigurationTreeView.createDisabledItem(REFRESH),
						ConfigurationTreeView.createDisabledItem(EXCLUDE_CLIENT_DIC_FOLDER),
						ConfigurationTreeView.createItem(OPEN_DICTIONARY, () -> this.model.openClientDictionary(file), R.CLIENT_TREE_NODE_ERROR_OPEN_DIC.get()),
						ConfigurationTreeView.createDisabledItem(REMOVE),
						ConfigurationTreeView.createDisabledItem(SHOW_POSSIBILITIES),
						ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMETERS)
				);
				return menu;
			};
			listClientDictionaries.forEach(file -> new BuildTree(file, this.treeItem,model.getFileComparator())
					.fileFilter(f -> ConfigurationFx.getExtension(f.getAbsolutePath()).equals(Configuration.dictExt))
					.doubleClickEvent(f -> () -> this.model.openClientDictionary(f)).menuTopFolder(topFolderFunc).menuFiles(filesFunc).byPass());
		}

	}
}
