////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFxNew;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.ConnectionStatus;
import com.exactprosystems.jf.tool.newconfig.TablePair;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServiceTreeNode extends TreeNode
{
	private ConfigurationFxNew model;
	private TreeItem<TreeNode> treeItem;

	public ServiceTreeNode(ConfigurationFxNew model, TreeItem<TreeNode> treeItem)
	{
		this.model = model;
		this.treeItem = treeItem;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = ConfigurationTreeView.add("Add service",
				e -> ConfigurationTreeView.showInputDialog("Enter new service name")
						.ifPresent(res -> Common.tryCatch(() -> this.model.addNewServiceEntry(res), "Error on add new service")));
		MenuItem itemTestVersion = new MenuItem("Test versions");
		itemTestVersion.setOnAction(e -> Common.tryCatch(() -> this.model.testServiceVersion(), "Error on test service version"));
		menu.getItems().add(itemTestVersion);
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		return new Text("Service entries");
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.SERVICE_ICON));
	}

	public void display(List<Configuration.ServiceEntry> serviceEntries, Map<String, SupportedEntry> mapSupportedEntries, Map<String, ConnectionStatus> mapServicesStatus)
	{
		this.treeItem.getChildren().clear();
		serviceEntries.stream()
				.map(entry -> new ServiceEntryNode(model, entry, mapSupportedEntries.get(entry.toString()), mapServicesStatus.get(entry.toString())))
				.map(serviceEntry -> new TreeItem<TreeNode>(serviceEntry))
				.forEach(treeItem -> this.treeItem.getChildren().add(treeItem));
	}

	private class ServiceEntryNode extends AbstractEntryNode<Configuration.ServiceEntry>
	{
		private SupportedEntry supportedEntry;
		private ConnectionStatus status;

		public ServiceEntryNode(ConfigurationFxNew model, Configuration.ServiceEntry entry, SupportedEntry supportedEntry, ConnectionStatus status)
		{
			super(model, entry);
			this.supportedEntry = supportedEntry;
			this.status = status;
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			MenuItem itemRemove = new MenuItem("Remove", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			itemRemove.setOnAction(e -> Common.tryCatch(() -> model.removeServiceEntry(getEntry()), String.format("Error on remove entry '%s'", getEntry().toString())));

			MenuItem itemAddAll = new MenuItem("Add all known params");
			itemAddAll.setOnAction(e -> Common.tryCatch(() -> model.addAllServiceParams(getEntry()), String.format("Error on add all parameters for entry '%s'", getEntry())));

			MenuItem startService = new MenuItem("Start service");
			startService.setOnAction(e -> Common.tryCatch(() -> model.startService(getEntry()), "Error on start entry"));

			MenuItem stopService = new MenuItem("Stop service");
			stopService.setOnAction(e -> Common.tryCatch(() -> model.stopService(getEntry()), "Error on stop entry"));
			if (getSupportedEntry() != null && !getSupportedEntry().isSupported())
			{
				startService.setDisable(true);
				stopService.setDisable(true);
			}
			if (status != null)
			{
				switch (status)
				{
					case NotStarted:
					case StartFailed:
						startService.setDisable(false);
						stopService.setDisable(true);
						break;
					case StartSuccessful:
						startService.setDisable(true);
						stopService.setDisable(false);
						break;
				}
			}
			menu.getItems().addAll(itemRemove, startService, stopService, itemAddAll);
			return Optional.of(menu);
		}

		@Override
		public Optional<Image> icon()
		{
			return Optional.ofNullable(status).map(ConnectionStatus::getImage);
		}

		@Override
		public List<TablePair> getParameters()
		{
			try
			{
				List<TablePair> list = new ArrayList<>();
				list.add(TablePair.TablePairBuilder.create().key(Configuration.serviceDescription).value(getEntry().get(Configuration.serviceDescription)).edit(true).build());
				list.add(TablePair.TablePairBuilder.create().key(Configuration.serviceJar).value(getEntry().get(Configuration.serviceJar)).edit(true).isPath(true).build());
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
			return () -> model.startService(getEntry());
		}

		@Override
		protected SupportedEntry getSupportedEntry()
		{
			return this.supportedEntry;
		}
	}
}
