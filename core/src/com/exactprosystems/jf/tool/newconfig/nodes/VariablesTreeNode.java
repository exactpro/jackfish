////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
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

public class VariablesTreeNode extends TreeNode
{
	private ConfigurationFx		model;
	private TreeItem<TreeNode>	variablesTreeNode;

	private static final SerializablePair<String, String> REMOVE_VARS_FILE = new SerializablePair<>("Refresh", CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<String, String> OPEN_VARS_FILE = new SerializablePair<>("Open matrix", CssVariables.Icons.VARS_ICON);


	public VariablesTreeNode(ConfigurationFx model, TreeItem<TreeNode> variablesTreeNode)
	{
		this.model = model;
		this.variablesTreeNode = variablesTreeNode;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = new ContextMenu();
		menu.getItems().addAll(
				ConfigurationTreeView.createDisabledItem(REMOVE_VARS_FILE),
				ConfigurationTreeView.createDisabledItem(OPEN_VARS_FILE),
				ConfigurationTreeView.createDisabledItem("Git", null)
		);
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		return new Text("vars");
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.VARS_ICON));
	}

	public void display(List<String> files)
	{
		this.variablesTreeNode.getChildren().clear();
		files.stream().sorted(String::compareTo).map(f -> MainRunner.makeDirWithSubstitutions(f)).map(file -> new FileTreeNode(new File(file))
		{
			@Override
			public Optional<ContextMenu> contextMenu()
			{
				ContextMenu menu = new ContextMenu();
				menu.getItems().addAll(
						ConfigurationTreeView.createItem(REMOVE_VARS_FILE, () -> model.excludeVarsFile(file), "Error on remove vars file"),
						ConfigurationTreeView.createItem(OPEN_VARS_FILE, () -> model.openVariableFile(new File(file)), "Error on load system variable")
				);
				menu.getItems().addAll(super.contextMenu().orElse(new ContextMenu()).getItems());
				return Optional.of(menu);
			}

			@Override
			public Common.Function onDoubleClickEvent()
			{
				return () -> model.openVariableFile(new File(file));
			}

			@Override
			public List<TablePair> getParameters()
			{
				List<TablePair> list = new ArrayList<>();
				list.add(TablePair.TablePairBuilder.create("path", ConfigurationFx.path(file)).edit(false).build());
				return list;
			}
		}).map(e -> new TreeItem<TreeNode>(e)).forEach(i -> this.variablesTreeNode.getChildren().add(i));
	}
}
