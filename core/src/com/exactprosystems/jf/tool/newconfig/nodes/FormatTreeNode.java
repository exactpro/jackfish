////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.TablePair;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FormatTreeNode extends TreeNode
{
	private ConfigurationFx model;
	private TreeItem<TreeNode> formatTreeItem;

	private String timeFormat;
	private String dateFormat;
	private String dateTimeFormat;

	private static final SerializablePair<R, String> ADD_FORMAT = new SerializablePair<>(R.FORMAT_TREE_NODE_ADD_FORMAT, CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<R, String> REMOVE_FORMAT = new SerializablePair<>(R.FORMAT_TREE_NODE_REMOVE, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> REPLACE_FORMAT = new SerializablePair<>(R.FORMAT_TREE_NODE_REPLACE, null);

	public FormatTreeNode(ConfigurationFx configuration, TreeItem<TreeNode> treeItem)
	{
		this.model = configuration;
		this.formatTreeItem = treeItem;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu contextMenu = ConfigurationTreeView.add(R.FORMAT_TN_ADD_FORMAT.get(),
				e -> DialogsHelper.showInputDialog(R.FORMAT_TN_ENTER_NEW.get(), "").ifPresent(
						res -> Common.tryCatch(() -> this.model.addNewAdditionalFormat(res), R.FORMAT_TN_ERROR_ON_ADD.get())
				));
		contextMenu.getItems().addAll(
				ConfigurationTreeView.createDisabledItem(REMOVE_FORMAT),
				ConfigurationTreeView.createDisabledItem(REPLACE_FORMAT)
		);
		return Optional.of(contextMenu);
	}

	@Override
	public Node getView()
	{
		return new Text(R.FORMAT_TN_VIEW.get());
	}

	@Override
	public List<TablePair> getParameters()
	{
		List<TablePair> list = new ArrayList<>();
		list.add(TablePair.TablePairBuilder.create(Configuration.time, this.timeFormat).build());
		list.add(TablePair.TablePairBuilder.create(Configuration.date, this.dateFormat).build());
		list.add(TablePair.TablePairBuilder.create(Configuration.dateTime, this.dateTimeFormat).build());
		return list;
	}

	@Override
	public void updateParameter(String key, String value)
	{
		Common.tryCatch(() -> this.model.changeFormat(key, value), R.FORMAT_TN_ERROR_ON_CHANGE.get());
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.FORMAT_ICON));
	}

	public void display(String timeFormat, String dateFormat, String dateTimeFormat, List<String> additionFormats)
	{
		this.timeFormat = timeFormat;
		this.dateFormat = dateFormat;
		this.dateTimeFormat = dateTimeFormat;

		this.formatTreeItem.getChildren().clear();

		additionFormats.stream().map(format ->
		{
			TreeItem<TreeNode> treeItem = new TreeItem<>();
			TreeNode formatNode = new TreeNodeFormat(format);
			treeItem.setValue(formatNode);
			return treeItem;
		}).forEach(this.formatTreeItem.getChildren()::add);
	}

	private class TreeNodeFormat extends TreeNode
	{
		private String name;

		public TreeNodeFormat(String name)
		{
			this.name = name;
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(ADD_FORMAT),
					ConfigurationTreeView.createItem(REMOVE_FORMAT, () -> model.removeAdditionalFormat(this.name), R.FORMAT_TN_ERROR_ON_REMOVE.get()),
					ConfigurationTreeView.createItem(REPLACE_FORMAT, this::replaceFormat, R.FORMAT_TN_ERROR_ON_REPLACE.get())
			);
			return Optional.of(menu);
		}

		@Override
		public Common.Function onDoubleClickEvent()
		{
			return this::replaceFormat;
		}

		@Override
		public Node getView()
		{
			return new Text(this.name);
		}

		@Override
		public Optional<Image> icon()
		{
			return Optional.empty();
		}

		private void replaceFormat()
		{
			Dialog<String> dialog = new TextInputDialog(this.name);
			dialog.setResizable(true);
			dialog.setTitle(R.FORMAT_TN_REPLACE.get());
			dialog.setHeaderText(R.FORMAT_TN_ENTER_NEW.get());
			dialog.showAndWait().ifPresent(str -> Common.tryCatch(() -> model.replaceAdditionalFormat(this.name, str), R.FORMAT_TB_ERROR_REPLACE_FORMAT.get()));
		}

	}
}
