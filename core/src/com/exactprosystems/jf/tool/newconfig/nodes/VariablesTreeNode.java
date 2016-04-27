////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.common.MutableString;
import com.exactprosystems.jf.common.parser.items.MutableArrayList;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFxNew;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class VariablesTreeNode extends TreeNode
{
	private ConfigurationFxNew model;
	private TreeItem<TreeNode> variablesTreeNode;

	public VariablesTreeNode(ConfigurationFxNew model, TreeItem<TreeNode> variablesTreeNode)
	{
		this.model = model;
		this.variablesTreeNode = variablesTreeNode;
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

	public void display(MutableArrayList<MutableString>  files)
	{
		this.variablesTreeNode.getChildren().clear();
		files.stream().sorted((f1, f2) -> f1.get().compareTo(f2.get())).map(file -> new FileTreeNode(new File(file.get()))
		{
			@Override
			public Optional<ContextMenu> contextMenu()
			{
				ContextMenu menu = super.contextMenu().orElse(new ContextMenu());

				MenuItem itemRemoveVars = new MenuItem("Remove vars file", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
				itemRemoveVars.setOnAction(e -> Common.tryCatch(() -> model.removeVarsFile(file.get()), "Error on remove vars file"));

				MenuItem itemOpenVars = new MenuItem("Open vars file", new ImageView(new Image(CssVariables.Icons.VARS_ICON)));
				itemOpenVars.setOnAction(event -> model.openVariableFile(new File(file.get())));

				menu.getItems().addAll(itemRemoveVars, itemOpenVars);
				return Optional.of(menu);
			}

			@Override
			public Common.Function onDoubleClickEvent()
			{
				return () -> model.openVariableFile(new File(file.get()));
			}
		}).map(e -> new TreeItem<TreeNode>(e)).forEach(i -> this.variablesTreeNode.getChildren().add(i));
	}
}
