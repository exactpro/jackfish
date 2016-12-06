////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.service.ServiceStatus;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.ServiceEntry;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.TablePair;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServiceTreeNode extends TreeNode
{
	private ConfigurationFx model;
	private TreeItem<TreeNode> treeItem;

	private static final SerializablePair<String, String> ADD_NEW_SERVICE = new SerializablePair<>("Add new service", CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<String, String> TEST_VERSION = new SerializablePair<>("Test versions", null);
	private static final SerializablePair<String, String> REMOVE = new SerializablePair<>("Remove", CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<String, String> START_SERVICE = new SerializablePair<>("Start service", CssVariables.Icons.REFRESH);
	private static final SerializablePair<String, String> STOP_SERVICE = new SerializablePair<>("Stop service", CssVariables.Icons.REFRESH);
	private static final SerializablePair<String, String> ADD_ALL_KNOWN_PARAMS = new SerializablePair<>("Add all known parameters", null);

	public ServiceTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
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
		menu.getItems().addAll(
				ConfigurationTreeView.createItem(TEST_VERSION, () -> this.model.testServiceVersion(), "Error on test service version"),
				ConfigurationTreeView.createDisabledItem(REMOVE),
				ConfigurationTreeView.createDisabledItem(START_SERVICE),
				ConfigurationTreeView.createDisabledItem(STOP_SERVICE),
				ConfigurationTreeView.createDisabledItem(ADD_ALL_KNOWN_PARAMS)
		);
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

	public void display(List<ServiceEntry> serviceEntries, Map<String, ServiceStatus> mapStatus)
	{
		this.treeItem.getChildren().clear();
		serviceEntries.stream()
				.map(entry -> new ServiceEntryNode(model, entry, mapStatus.get(entry.toString())))
				.map(serviceEntry -> new TreeItem<TreeNode>(serviceEntry))
				.forEach(treeItem -> this.treeItem.getChildren().add(treeItem));
	}

	private class ServiceEntryNode extends AbstractEntryNode<ServiceEntry>
	{
		private ServiceStatus status;

		public ServiceEntryNode(ConfigurationFx model, ServiceEntry entry, ServiceStatus status)
		{
			super(model, entry);
			this.status = status;
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(ADD_NEW_SERVICE),
					ConfigurationTreeView.createDisabledItem(TEST_VERSION),
					ConfigurationTreeView.createItem(REMOVE, () -> model.removeServiceEntry(getEntry()), String.format("Error on remove entry '%s'", getEntry().toString())),
					ConfigurationTreeView.createItem(ADD_ALL_KNOWN_PARAMS,() -> model.addAllServiceParams(getEntry()), String.format("Error on add all parameters for entry '%s'", getEntry()))
			);
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
						startService.setDisable(false);
						stopService.setDisable(true);
						break;
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
			menu.getItems().add(3,startService);
			menu.getItems().add(4,stopService);
			return Optional.of(menu);
		}

		@Override
		public Node getView()
		{
			Node view = super.getView();
			if (this.status != ServiceStatus.StartFailed)
			{
				return view;
			}
			HBox box = new HBox();
			Label lbl = new Label("( " + this.status.getMsg() + " ) ");
			lbl.setTooltip(new Tooltip(this.status.getMsg()));
			box.getChildren().addAll(view, lbl);
			return box;

		}

		@Override
		public Optional<Image> icon()
		{
			switch (this.status)
			{
				case NotStarted:		return Optional.of(new Image(CssVariables.Icons.SERVICE_NOT_STARTED_ICON));
				case StartSuccessful:	return Optional.of(new Image(CssVariables.Icons.SERVICE_STARTED_GOOD_ICON));
				case StartFailed:		return Optional.of(new Image(CssVariables.Icons.SERVICE_STARTED_FAIL_ICON));
			}
			return Optional.empty();
		}

		@Override
		public List<TablePair> getParameters()
		{
			try
			{
				List<TablePair> list = new ArrayList<>();
				list.add(TablePair.TablePairBuilder.create(Configuration.serviceDescription, getEntry().get(Configuration.serviceDescription)).edit(true).build());
				list.add(TablePair.TablePairBuilder.create(Configuration.serviceJar, getEntry().get(Configuration.serviceJar)).edit(true).pathFunc(
						() -> DialogsHelper.showOpenSaveDialog("Choose service", "Jar files(*.jar)", "*.jar", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
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
			return () -> model.startService(getEntry());
		}

	}
}
