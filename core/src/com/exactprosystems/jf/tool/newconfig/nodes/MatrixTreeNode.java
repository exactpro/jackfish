////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
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

	private static final SerializablePair<String, String> REFRESH_MATRIX = new SerializablePair<>("Refresh", CssVariables.Icons.REFRESH);

	private static final SerializablePair<String, String> OPEN_MATRIX = new SerializablePair<>("Open matrix", CssVariables.Icons.MATRIX_ICON);
	private static final SerializablePair<String, String> ADD_NEW_MATRIX = new SerializablePair<>("Add new matrix", CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<String, String> REMOVE_MATRIX = new SerializablePair<>("Remove matrix", CssVariables.Icons.REMOVE_PARAMETER_ICON);

	private static final SerializablePair<String, String> REMOVE_MATRIX_FOLDER = new SerializablePair<>("Remove folder", CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<String, String> EXCLUDE_MATRIX_FOLDER = new SerializablePair<>("Exclude matrix dir", CssVariables.Icons.REMOVE_PARAMETER_ICON);

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
		menu.getItems().addAll(
				ConfigurationTreeView.createDisabledItem(OPEN_MATRIX),
				ConfigurationTreeView.createDisabledItem(ADD_NEW_MATRIX),
				ConfigurationTreeView.createDisabledItem(REMOVE_MATRIX),
				ConfigurationTreeView.createDisabledItem(REMOVE_MATRIX_FOLDER),
				ConfigurationTreeView.createDisabledItem(EXCLUDE_MATRIX_FOLDER),
				ConfigurationTreeView.createItem(REFRESH_MATRIX, () -> this.model.refreshMatrices(), "Error on refresh matrices"),
				new SeparatorMenuItem(),
				ConfigurationTreeView.createDisabledItem("Git", null)
		);
		return Optional.of(menu);
	}

	public void display(List<String> matricesValue)
	{
		this.treeItem.getChildren().clear();
		Function<File, ContextMenu> topFolderMenu = file ->
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(OPEN_MATRIX),
					ConfigurationTreeView.createDisabledItem(REMOVE_MATRIX),
					ConfigurationTreeView.createItem(EXCLUDE_MATRIX_FOLDER, () -> model.excludeMatrixDirectory(file.getName()), "Error on remove matrix directory"),
					ConfigurationTreeView.createDisabledItem(REFRESH_MATRIX)
			);
			return menu;
		};
		Function<File, ContextMenu> menuFiles = file ->
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createItem(OPEN_MATRIX,() -> this.model.openMatrix(file), "Error on on open matrix"),
					ConfigurationTreeView.createItem(ADD_NEW_MATRIX, () -> ConfigurationTreeView.showInputDialog("Enter new name").ifPresent(
							name -> Common.tryCatch(() -> this.model.addNewMatrix(file, name), "Error on create new matrix")), "Error on add new matrix"),
					ConfigurationTreeView.createItem(REMOVE_MATRIX, () -> this.model.removeMatrix(file), "Error on remove matrix"),
					ConfigurationTreeView.createDisabledItem(REMOVE_MATRIX_FOLDER),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_MATRIX_FOLDER),
					ConfigurationTreeView.createDisabledItem(REFRESH_MATRIX)
			);
			return menu;
		};
		Function<File, ContextMenu> menuFolders = file ->
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(OPEN_MATRIX),
					ConfigurationTreeView.createItem(ADD_NEW_MATRIX, () -> ConfigurationTreeView.showInputDialog("Enter new name").ifPresent(
							name -> Common.tryCatch(() -> this.model.addNewMatrix(file, name), "Error on create new matrix")), "Error on add new matrix"),
					ConfigurationTreeView.createDisabledItem(REMOVE_MATRIX),
					ConfigurationTreeView.createItem(REMOVE_MATRIX_FOLDER, () -> this.model.removeMatrix(file), "Error on remove folder"),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_MATRIX_FOLDER),
					ConfigurationTreeView.createDisabledItem(REFRESH_MATRIX)
			);
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
