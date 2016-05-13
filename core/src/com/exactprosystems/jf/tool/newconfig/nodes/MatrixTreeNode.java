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
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
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
import java.util.function.Consumer;
import java.util.function.Function;

public class MatrixTreeNode extends TreeNode
{
	private ConfigurationFx		model;
	private TreeItem<TreeNode>	treeItem;

	public MatrixTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
	{
		this.model = model;
		this.treeItem = treeItem;
		BuildTree.addListenerToExpandChild(this.treeItem);
	}

	@Override
	public Node getView()
	{
		return new Text("matrix");
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.MATRIX_ICON));
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = new ContextMenu();
		// TODO think about implementation this method
		menu.getItems().add(new MenuItem("Git"));

		MenuItem refresh = new MenuItem("Refresh", new ImageView(new Image(CssVariables.Icons.REFRESH)));
		refresh.setOnAction(e -> Common.tryCatch(() -> this.model.refreshMatrices(), "Error on refresh matrices"));
		menu.getItems().add(0, refresh);
		return Optional.of(menu);
	}

	public void display(List<String> matricesValue)
	{
		this.treeItem.getChildren().clear();
		Function<File, ContextMenu> topFolderMenu = file ->
		{
			ContextMenu menu = new ContextMenu();
			MenuItem itemRemove = new MenuItem("Exclude matrix dir", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			itemRemove.setOnAction(e -> Common.tryCatch(() -> model.excludeMatrixDirectory(file.getName()), "Error on remove matrix directory"));
			menu.getItems().addAll(itemRemove);
			return menu;
		};
		Function<File, ContextMenu> menuFiles = file ->
		{
			ContextMenu menu = new ContextMenu();

			MenuItem itemOpenMatrix = new MenuItem("Open matrix", new ImageView(new Image(CssVariables.Icons.MATRIX_ICON)));
			itemOpenMatrix.setOnAction(e -> Common.tryCatch(() -> this.model.openMatrix(file), "Error on on open matrix"));

			MenuItem addNewMatrix = new MenuItem("Add new matrix", new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
			addNewMatrix.setOnAction(e -> Common.tryCatch(
					() -> ConfigurationTreeView.showInputDialog("Enter new name").ifPresent(
							name -> Common.tryCatch(() -> this.model.addNewMatrix(file, name), "Error on create new matrix")), "Error on add new matrix"));

			MenuItem removeMatrix = new MenuItem("Remove matrix", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			removeMatrix.setOnAction(e -> Common.tryCatch(() -> this.model.removeMatrix(file), "Error on remove matrix"));

			menu.getItems().addAll(itemOpenMatrix, addNewMatrix, removeMatrix);
			return menu;
		};
		Function<File, ContextMenu> menuFolders = file ->
		{
			ContextMenu menu = new ContextMenu();
			MenuItem addNewMatrix = new MenuItem("Add new matrix", new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
			addNewMatrix.setOnAction(e -> Common.tryCatch(
					() -> ConfigurationTreeView.showInputDialog("Enter new name").ifPresent(
							name -> Common.tryCatch(() -> this.model.addNewMatrix(file, name), "Error on create new matrix")), "Error on add new matrix"));

			MenuItem removeFolder = new MenuItem("Remove folder", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			removeFolder.setOnAction(e -> Common.tryCatch(() -> this.model.removeMatrix(file), "Error on remove folder"));

			menu.getItems().addAll(addNewMatrix, removeFolder);
			return menu;
		};
		matricesValue.forEach(file -> new BuildTree(new File(file), this.treeItem)
				.fileFilter(f -> ConfigurationFx.getExtension(f.getAbsolutePath()).equals(Configuration.matrixExt)).menuTopFolder(topFolderMenu)
				.doubleClickEvent(f -> () -> Common.tryCatch(() -> this.model.openMatrix(f), "Error on open matrix file")).menuFiles(menuFiles)
				.menuFolder(menuFolders).byPass());
	}

	public void select(File file, Consumer<TreeItem<TreeNode>> consumer)
	{
		selectFile(file, consumer, this.treeItem);
	}
}
