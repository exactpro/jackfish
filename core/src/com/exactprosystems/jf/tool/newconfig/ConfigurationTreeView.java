////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.nodes.ConfigurationTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.SeparatorTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.TreeNode;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.awt.*;
import java.io.File;
import java.util.Comparator;
import java.util.Optional;

public class ConfigurationTreeView extends TreeView<TreeNode>
{
	private ParametersTableView	tableView;
	private ConfigurationFx	configuration;

	public ConfigurationTreeView(ParametersTableView tableView, ConfigurationFx configuration)
	{
		this.tableView = tableView;
		this.configuration = configuration;
		this.setRoot(new TreeItem<>(new ConfigurationTreeNode(this.configuration)));
		this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.setOnContextMenuRequested(event ->
		{
			TreeNode value = this.getSelectionModel().getSelectedItem().getValue();
			Optional.ofNullable(value)
					.map(TreeNode::contextMenu)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.ifPresent(
							menu -> menu.show(this.getScene().getWindow(), MouseInfo.getPointerInfo().getLocation().x,
									MouseInfo.getPointerInfo().getLocation().y));

		});
		this.setOnMouseClicked(event ->
		{
			TreeItem<TreeNode> selectedItem = this.getSelectionModel().getSelectedItem();
			if (selectedItem != null && selectedItem.getChildren().size() == 0 && event.getClickCount() == 2)
			{
				Optional.ofNullable(selectedItem.getValue().onDoubleClickEvent()).ifPresent(fnc -> Common.tryCatch(fnc, "Error on call"));
			}
		});
		this.setCellFactory(param -> new TreeCell<TreeNode>()
		{
			@Override
			protected void updateItem(TreeNode item, boolean empty)
			{
				super.updateItem(item, empty);
				setGraphic(null);
				setText(null);
				if (item != null)
				{
					Node view = item.getView();
					if (item instanceof SeparatorTreeNode)
					{
						setGraphic(view);
					}
					else
					{
						HBox pane = new HBox();
						pane.setSpacing(5);
						item.icon().map(ImageView::new).ifPresent(pane.getChildren()::add);
						pane.getChildren().add(view);
						setGraphic(pane);
					}
				}
			}
		});

		this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			this.tableView.updateParameters(null);
			this.tableView.setEditableNode(newValue.getValue());
			this.tableView.hide();
			if (newValue.getValue().getParameters() != null)
			{
				this.tableView.show();
				this.tableView.updateParameters(newValue.getValue().getParameters());
			}
		});
	}

	public static Comparator<File>	comparator	= (f1, f2) ->
		{
			if (f1.isDirectory() && !f2.isDirectory())
			{
				return -1;
			}
			else if (!f1.isDirectory() && f2.isDirectory())
			{
				return 1;
			}
			else
			{
				return f1.getName().compareTo(f2.getName());
			}
		};

	public static Optional<String> showInputDialog(String headerText)
	{
		Dialog<String> dialog = new TextInputDialog();
		dialog.setHeaderText(headerText);
		return dialog.showAndWait();
	}

	public static ContextMenu add(String menuItemName, EventHandler<ActionEvent> eventHandler)
	{
		ContextMenu menu = new ContextMenu();
		MenuItem itemAdd = new MenuItem(menuItemName, new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
		itemAdd.setOnAction(eventHandler);
		menu.getItems().add(itemAdd);
		return menu;
	}

	public static ContextMenu gitContextMenu(File file)
	{
		ContextMenu contextMenu = new ContextMenu();

		javafx.scene.control.Menu menu = new javafx.scene.control.Menu("Git");

		MenuItem itemClone = new MenuItem("Clone");
		itemClone.setOnAction(e -> System.out.println(String.format("file %s Clone", file)));

		MenuItem itemPull = new MenuItem("Pull");
		itemPull.setOnAction(e -> System.out.println(String.format("file %s Pull", file)));

		MenuItem itemCommit = new MenuItem("Commit");
		itemCommit.setOnAction(e -> System.out.println(String.format("file %s Commit", file)));

		MenuItem itemPush = new MenuItem("Push");
		itemPush.setOnAction(e -> System.out.println(String.format("file %s Push", file)));

		MenuItem itemReset = new MenuItem("Reset");
		itemReset.setOnAction(e -> System.out.println(String.format("file %s Reset", file)));

		menu.getItems().addAll(itemClone, itemPull, itemCommit, itemPush, itemReset);
		contextMenu.getItems().add(menu);
		return contextMenu;
	}
}
