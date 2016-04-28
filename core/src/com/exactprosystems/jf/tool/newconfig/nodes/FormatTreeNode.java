////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.MutableString;
import com.exactprosystems.jf.common.parser.items.MutableArrayList;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFxNew;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.TablePair;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FormatTreeNode extends TreeNode
{
	private ConfigurationFxNew model;
	private TreeItem<TreeNode> formatTreeItem;

	private String timeFormat;
	private String dateFormat;
	private String dateTimeFormat;

	public FormatTreeNode(ConfigurationFxNew configuration, TreeItem<TreeNode> treeItem)
	{
		this.model = configuration;
		this.formatTreeItem = treeItem;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		return Optional.of(ConfigurationTreeView.add("Add format", e ->
				ConfigurationTreeView.showInputDialog("Enter new format")
						.ifPresent(res -> Common.tryCatch(() -> this.model.addNewAdditionalFormat(res), "Error on add new format"))
		));
	}

	@Override
	public Node getView()
	{
		return new Text("format");
	}

	@Override
	public List<TablePair> getParameters()
	{
		List<TablePair> list = new ArrayList<>();
		list.add(TablePair.TablePairBuilder.create().key(Configuration.time).value(this.timeFormat).build());
		list.add(TablePair.TablePairBuilder.create().key(Configuration.date).value(this.dateFormat).build());
		list.add(TablePair.TablePairBuilder.create().key(Configuration.dateTime).value(this.dateTimeFormat).build());
		return list;
	}

	@Override
	public void updateParameter(String key, String value)
	{
		Common.tryCatch(() -> this.model.changeFormat(key, value), "Error on change format");
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.FORMAT_ICON));
	}

	public void display(String timeFormat, String dateFormat, String dateTimeFormat, MutableArrayList<MutableString> additionFormats)
	{
		this.timeFormat = timeFormat;
		this.dateFormat = dateFormat;
		this.dateTimeFormat = dateTimeFormat;
		
		this.formatTreeItem.getChildren().clear();
		
		additionFormats.stream().map(format ->
		{
			TreeItem<TreeNode> treeItem = new TreeItem<>();
			TreeNode formatNode = new TreeNodeFormat(format.get());
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

			MenuItem removeItem = new MenuItem("Remove", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			removeItem.setOnAction(e -> Common.tryCatch(() -> model.removeAdditionalFormat(this.name), "Error on remove format"));

			MenuItem replaceItem = new MenuItem("Replace");
			replaceItem.setOnAction(e -> this.replaceFormat());

			menu.getItems().addAll(removeItem, replaceItem);
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
			dialog.setTitle("Replace");
			dialog.setHeaderText("Enter new format");
			dialog.showAndWait().ifPresent(str -> Common.tryCatch(() -> model.replaceAdditionalFormat(this.name, str), "Error on change evaluator import"));
		}

	}

}
