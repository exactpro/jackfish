////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFxNew;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.Optional;

public class EvaluatorTreeNode extends TreeNode
{
	private ConfigurationFxNew model;
	private TreeItem<TreeNode> evaluatorTreeItem;

	public EvaluatorTreeNode(ConfigurationFxNew configuration, TreeItem<TreeNode> treeItem) throws Exception
	{
		this.model = configuration;
		this.evaluatorTreeItem = treeItem;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		return Optional.of(ConfigurationTreeView.add("Add import", e ->
				ConfigurationTreeView.showInputDialog("Enter new import")
						.ifPresent(res -> Common.tryCatch(() -> this.model.addNewEvaluatorImport(res), "Error on add new import"))
		));
	}

	@Override
	public Node getView()
	{
		return new Text("evaluator");
	}

	@Override
	public Optional<Image> icon()
	{
		Image image = new Image(CssVariables.Icons.EVALUATOR_ICON);
		return Optional.of(image);
	}

	public void display(String evaluatorImports) throws Exception
	{
		this.evaluatorTreeItem.getChildren().clear();
		String[] imports = evaluatorImports.split(",");
		Arrays.stream(imports).map(evaluatorImport -> {
			TreeItem<TreeNode> remove = new TreeItem<>();
			TreeNode importNode = new TreeNodeImport(evaluatorImport);
			remove.setValue(importNode);
			return remove;
		}).forEach(this.evaluatorTreeItem.getChildren()::add);
	}

	private void remove(String evaluatorImport) throws Exception
	{
		this.model.removeImport(evaluatorImport);
	}

	private class TreeNodeImport extends TreeNode
	{
		private String evaluatorImport;

		public TreeNodeImport(String evaluatorImport)
		{
			this.evaluatorImport = evaluatorImport;
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			MenuItem remove = new MenuItem("Remove", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			remove.setOnAction(e -> Common.tryCatch(() -> remove(this.evaluatorImport), "Error on remove import"));

			MenuItem replace = new MenuItem("Replace");
			replace.setOnAction(e -> this.replaceEvaluator());

			menu.getItems().addAll(remove, replace);
			return Optional.of(menu);
		}

		@Override
		public Node getView()
		{
			return new Text(this.evaluatorImport);
		}

		@Override
		public Common.Function onDoubleClickEvent()
		{
			return this::replaceEvaluator;
		}

		@Override
		public Optional<Image> icon()
		{
			return Optional.empty();
		}

		private void replaceEvaluator()
		{
			Dialog<String> dialog = new TextInputDialog(this.evaluatorImport);
			dialog.setResizable(true);
			dialog.setTitle("Replace");
			dialog.setHeaderText("Enter new evaluator");
			dialog.showAndWait().ifPresent(str -> Common.tryCatch(() -> model.replaceEvaluatorImport(this.evaluatorImport, str), "Error on change evaluator import"));
		}
	}
}
