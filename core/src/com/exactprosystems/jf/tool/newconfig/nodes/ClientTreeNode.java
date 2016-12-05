////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

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
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
				"Add client",
				e -> ConfigurationTreeView.showInputDialog("Enter new client name").ifPresent(
						res -> Common.tryCatch(() -> this.model.addNewClientEntry(res), "Error on add new client")));
		MenuItem itemTestVersion = new MenuItem("Test versions");
		itemTestVersion.setOnAction(e -> Common.tryCatch(() -> this.model.testClientVersion(), "Error on test client version"));
		menu.getItems().add(itemTestVersion);
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		return new Text("Client entries");
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
			MenuItem itemRemove = new MenuItem("Remove", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			itemRemove.setOnAction(e -> Common.tryCatch(() -> model.removeClientEntry(getEntry()),
					String.format("Error on remove entry '%s'", getEntry().toString())));

			MenuItem itemPossibilities = new MenuItem("Possibilities");
			itemPossibilities.setOnAction(e -> Common.tryCatch(() -> model.showPossibilities(getEntry()),
					String.format("Error on show possibilities for entry '%s'", getEntry().toString())));

			MenuItem itemAddAll = new MenuItem("Add all known params");
			itemAddAll.setOnAction(e -> Common.tryCatch(() -> model.addAllClientParams(getEntry()),
					String.format("Error on add all parameters for entry '%s'", getEntry())));
			menu.getItems().addAll(itemRemove, itemPossibilities, itemAddAll);
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
				list.add(TablePair.TablePairBuilder.create(Configuration.clientJar, getEntry().get(Configuration.clientJar)).edit(true).pathFunc(
						() -> DialogsHelper.showOpenSaveDialog("Choose client for " + getEntry().toString(), "Jar files(*.jar)", "*.jar", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
				list.add(TablePair.TablePairBuilder.create(Configuration.clientDictionary, getEntry().get(Configuration.clientDictionary)).edit(true).pathFunc(
						() -> DialogsHelper.showOpenSaveDialog("Choose client dictionary", "Xml files(*.xml)", "*.xml", DialogsHelper.OpenSaveMode.OpenFile))
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

	private class ClientDictionaryTreeNode extends TreeNode
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
			return new Text("Client dictionaries");
		}

		@Override
		public Optional<Image> icon()
		{
			return Optional.of(new Image(CssVariables.Icons.CLIENT_DICTIONARY_ICON));
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			Optional<ContextMenu> contextMenu = super.contextMenu();

			MenuItem refresh = new MenuItem("Refresh", new ImageView(new Image(CssVariables.Icons.REFRESH)));
			refresh.setOnAction(e -> Common.tryCatch(() -> this.model.updateClientDictionaries(), "Error on refresh client dictionaries"));
			ContextMenu ret = contextMenu.orElse(new ContextMenu());
			ret.getItems().add(0, refresh);

			return Optional.of(ret);
		}

		public void display(List<File> listClientDictionaries)
		{
			this.treeItem.getChildren().clear();
			Function<File, ContextMenu> topFolderFunc = file ->
			{
				ContextMenu menu = new ContextMenu();
				MenuItem itemRemove = new MenuItem("Exclude client dictionary dir", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
				itemRemove.setOnAction(e -> Common.tryCatch(() -> model.excludeClientDictionaryFolder(file.getName()), "Error on excluded matrix directory"));
				menu.getItems().addAll(itemRemove);
				return menu;
			};
			Function<File, ContextMenu> filesFunc = file ->
			{
				ContextMenu menu = new ContextMenu();
				MenuItem itemOpedDic = new MenuItem("Open dictionary", new ImageView(new Image(CssVariables.Icons.CLIENT_DICTIONARY_ICON)));
				itemOpedDic.setOnAction(e -> Common.tryCatch(() -> this.model.openClientDictionary(file), "Error on open client dictionary"));
				menu.getItems().addAll(itemOpedDic);
				return menu;
			};
			listClientDictionaries.forEach(file -> new BuildTree(file, this.treeItem)
					.fileFilter(f -> ConfigurationFx.getExtension(f.getAbsolutePath()).equals(Configuration.dictExt))
					.doubleClickEvent(f -> () -> this.model.openClientDictionary(f)).menuTopFolder(topFolderFunc).menuFiles(filesFunc).byPass());
		}

	}
}
