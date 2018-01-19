////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.skin.CustomTreeViewSkin;
import com.exactprosystems.jf.tool.newconfig.nodes.ConfigurationTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.SeparatorTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.TreeNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.awt.MouseInfo;
import java.io.File;
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
					.ifPresent(menu -> {
						menu.setOnShowing(e -> value.onContextMenuShowing(menu));
						menu.show(this.getScene().getWindow(), MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
					});

		});
		this.setOnMouseClicked(event ->
		{
			TreeItem<TreeNode> selectedItem = this.getSelectionModel().getSelectedItem();
			if (selectedItem != null && selectedItem.getChildren().size() == 0 && event.getClickCount() == 2)
			{
				Optional.ofNullable(selectedItem.getValue().onDoubleClickEvent()).ifPresent(fnc -> Common.tryCatch(fnc, R.CONFIG_TREE_VIEW_ERROR_CALL.get()));
			}
		});

		this.setOnKeyPressed(event ->
		{
			TreeItem<TreeNode> selectedItem = this.getSelectionModel().getSelectedItem();
			if (selectedItem != null && selectedItem.getChildren().size() == 0 && event.getCode() == KeyCode.ENTER)
			{
				Optional.ofNullable(selectedItem.getValue().onDoubleClickEvent()).ifPresent(fnc -> Common.tryCatch(fnc, R.CONFIG_TREE_VIEW_ERROR_CALL.get()));
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
						item.setExpanded(this.getTreeItem().isExpanded());
						item.icon().map(ImageView::new).ifPresent(pane.getChildren()::add);
						pane.getChildren().addAll(Common.createSpacer(Common.SpacerEnum.HorizontalMin), view);
						setGraphic(pane);
					}
				}
			}
		});
		this.setSkin(new CustomTreeViewSkin<TreeNode>(this));

		this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			this.tableView.updateParameters(null);
			if (newValue != null)
			{
				this.tableView.setEditableNode(newValue.getValue());
				this.tableView.hide();
				if (newValue.getValue().getParameters() != null)
				{
					this.tableView.show();
					this.tableView.updateParameters(newValue.getValue().getParameters());
				}
			}
		});

		this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				((CustomTreeViewSkin<TreeNode>) this.getSkin()).scrollTo(newValue);
			}
		});
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

		javafx.scene.control.Menu menu = new javafx.scene.control.Menu(R.COMMON_GIT.get());

		MenuItem itemClone = new MenuItem(R.TOOL_CLONE.get());
		itemClone.setOnAction(e -> System.out.println(String.format("file %s Clone", file)));

		MenuItem itemPull = new MenuItem(R.TOOL_PULL.get());
		itemPull.setOnAction(e -> System.out.println(String.format("file %s Pull", file)));

		MenuItem itemCommit = new MenuItem(R.TOOL_COMMIT.get());
		itemCommit.setOnAction(e -> System.out.println(String.format("file %s Commit", file)));

		MenuItem itemPush = new MenuItem(R.TOOL_PUSH.get());
		itemPush.setOnAction(e -> System.out.println(String.format("file %s Push", file)));

		MenuItem itemReset = new MenuItem(R.TOOL_RESET.get());
		itemReset.setOnAction(e -> System.out.println(String.format("file %s Reset", file)));

		MenuItem itemIgnore = new MenuItem(R.TOOL_IGNORE.get());
		itemIgnore.setOnAction(e -> System.out.println(String.format("file %s Ignore", file)));

		menu.getItems().addAll(itemClone, itemPull, itemCommit, itemPush, itemReset, itemIgnore);
		contextMenu.getItems().add(menu);
		return contextMenu;
	}

	public static Menu createMenu(SerializablePair<R, String> pair, MenuItem ... items)
	{
		Menu menu = new Menu(pair.getKey().get());
		if (pair.getValue() != null)
		{
			menu.setGraphic(new ImageView(new Image(pair.getValue())));
		}
		menu.getItems().setAll(items);
		return menu;
	}

	public static Menu createDisabledMenu(SerializablePair<R, String> pair)
	{
		Menu menu = createMenu(pair);
		menu.setDisable(true);
		return menu;
	}

	public static MenuItem createItem(String name, String image, Common.Function fn, String error)
	{
		return createItem(name, image, fn, error, false);
	}

	public static MenuItem createItem(SerializablePair<R, String> pair, Common.Function fn, String error)
	{
		return createItem(pair.getKey().get(), pair.getValue(), fn, error);
	}

	public static MenuItem createDisabledItem(String name, String image)
	{
		return createItem(name, image, null, null, true);
	}

	public static MenuItem createDisabledItem(SerializablePair<R, String> pair)
	{
		return createItem(pair.getKey().get(), pair.getValue(), null, null, true);
	}

	private static MenuItem createItem(String name, String image, Common.Function fn, String error, boolean isDisabled)
	{
		MenuItem menuItem = new MenuItem(name);
		if (image != null)
		{
			menuItem.setGraphic(new ImageView(new Image(image)));
		}
		menuItem.setOnAction(e -> Common.tryCatch(fn, error));
		menuItem.setDisable(isDisabled);
		return menuItem;
	}
}
