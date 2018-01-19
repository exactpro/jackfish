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

	private static final SerializablePair<R, String> REFRESH_MATRIX = new SerializablePair<>(R.MATRIX_TREE_NODE_REFRESH, CssVariables.Icons.REFRESH);
	private static final SerializablePair<R, String> RENAME_MATRIX = new SerializablePair<>(R.MATRIX_TREE_NODE_RENAME, null);

	private static final SerializablePair<R, String> OPEN_MATRIX = new SerializablePair<>(R.MATRIX_TREE_NODE_OPEN_MATRIX, CssVariables.Icons.MATRIX_ICON);
	private static final SerializablePair<R, String> OPEN_AS_TEXT = new SerializablePair<>(R.MATRIX_TREE_NODE_OPEN_AS_TEXT, CssVariables.Icons.MATRIX_ICON);
	private static final SerializablePair<R, String> ADD_NEW_MATRIX = new SerializablePair<>(R.MATRIX_TREE_NODE_ADD_NEW_MATRIX, CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<R, String> REMOVE_MATRIX = new SerializablePair<>(R.MATRIX_TREE_NODE_REMOVE, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> ADD_TO_TOOLBAR = new SerializablePair<>(R.MATRIX_TREE_NODE_ADD_TO_TOOLBAR, CssVariables.Icons.ADD_PARAMETER_ICON);


	private static final SerializablePair<R, String> REMOVE_MATRIX_FOLDER = new SerializablePair<>(R.MATRIX_TREE_NODE_REMOVE_FOLDER, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> EXCLUDE_MATRIX_FOLDER = new SerializablePair<>(R.MATRIX_TREE_NODE_EXCLUDE_FOLDER, CssVariables.Icons.REMOVE_PARAMETER_ICON);

	public MatrixTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
	{
		this.model = model;
		this.treeItem = treeItem;
		BuildTree.addListenerToExpandChild(this.treeItem);
	}

	@Override
	public Node getView()
	{
		return new Text(R.MATRIX_TN_VIEW.get());
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
				ConfigurationTreeView.createDisabledItem(OPEN_AS_TEXT),
				ConfigurationTreeView.createDisabledItem(ADD_NEW_MATRIX),
				ConfigurationTreeView.createDisabledItem(REMOVE_MATRIX),
				ConfigurationTreeView.createDisabledItem(ADD_TO_TOOLBAR),
				ConfigurationTreeView.createDisabledItem(REMOVE_MATRIX_FOLDER),
				ConfigurationTreeView.createDisabledItem(EXCLUDE_MATRIX_FOLDER),
				ConfigurationTreeView.createItem(REFRESH_MATRIX, () -> this.model.updateMatrices(), R.MATRIX_TN_ERROR_ON_REFRESH.get()),
				ConfigurationTreeView.createDisabledItem(RENAME_MATRIX),
				new SeparatorMenuItem(),
				ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
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
					ConfigurationTreeView.createDisabledItem(OPEN_AS_TEXT),
					ConfigurationTreeView.createDisabledItem(REMOVE_MATRIX),
					ConfigurationTreeView.createDisabledItem(ADD_TO_TOOLBAR),
					ConfigurationTreeView.createItem(EXCLUDE_MATRIX_FOLDER, () -> model.excludeMatrixDirectory(file.getName()), R.MATRIX_TN_ERROR_ON_REMOVE_DIR.get()),
					ConfigurationTreeView.createDisabledItem(REFRESH_MATRIX),
					ConfigurationTreeView.createDisabledItem(RENAME_MATRIX)
			);
			return menu;
		};
		Function<File, ContextMenu> menuFiles = file ->
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createItem(OPEN_MATRIX,() -> this.model.openMatrix(file), R.MATRIX_TN_ERROR_ON_OPEN_MATRIX.get()),
					ConfigurationTreeView.createItem(OPEN_AS_TEXT,() -> this.model.openPlainText(file), R.MATRIX_TN_ERROR_ON_OPEN_MATRIX.get()),
					ConfigurationTreeView.createItem(ADD_NEW_MATRIX, () -> DialogsHelper.showInputDialog(R.MATRIX_TN_ENTER_NEW_NAME.get(), "").ifPresent(
							name -> Common.tryCatch(() -> this.model.addNewMatrix(file, name), R.MATRIX_TN_ERROR_ON_CREATE_MATRIX.get())), R.MATRIX_TN_ERROR_ON_ADD_MATRIX.get()),
					ConfigurationTreeView.createItem(REMOVE_MATRIX, () -> this.model.removeMatrix(file), R.MATRIX_TN_ERROR_ON_REMOVE.get()),
					ConfigurationTreeView.createItem(ADD_TO_TOOLBAR, () -> this.model.addToToolbar(Common.getRelativePath(file.getPath())), R.MATRIX_TN_ERROR_ADD_TO_TOOLBAR.get()),
					ConfigurationTreeView.createDisabledItem(REMOVE_MATRIX_FOLDER),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_MATRIX_FOLDER),
					ConfigurationTreeView.createDisabledItem(REFRESH_MATRIX),
					ConfigurationTreeView.createItem(RENAME_MATRIX, () -> this.model.renameMatrix(file), R.MATRIX_TN_ERROR_ON_RENAME.get())
			);
			return menu;
		};
		Function<File, ContextMenu> menuFolders = file ->
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(OPEN_MATRIX),
					ConfigurationTreeView.createDisabledItem(OPEN_AS_TEXT),
					ConfigurationTreeView.createItem(ADD_NEW_MATRIX, () -> DialogsHelper.showInputDialog(R.MATRIX_TN_ENTER_NEW_NAME.get(), "").ifPresent(
							name -> Common.tryCatch(() -> this.model.addNewMatrix(file, name), R.MATRIX_TN_ERROR_ON_CREATE_MATRIX.get())), R.MATRIX_TN_ERROR_ON_ADD_MATRIX.get()),
					ConfigurationTreeView.createDisabledItem(REMOVE_MATRIX),
					ConfigurationTreeView.createDisabledItem(ADD_TO_TOOLBAR),
					ConfigurationTreeView.createItem(REMOVE_MATRIX_FOLDER, () -> this.model.removeMatrix(file), R.MATRIX_TN_ERROR_ON_REMOVE_FOLDER.get()),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_MATRIX_FOLDER),
					ConfigurationTreeView.createDisabledItem(REFRESH_MATRIX),
					ConfigurationTreeView.createDisabledItem(RENAME_MATRIX)
			);
			return menu;
		};
		matricesValue.forEach(file -> new BuildTree(new File(file), this.treeItem,model.getFileComparator())
				.fileFilter(f -> ConfigurationFx.getExtension(f.getAbsolutePath()).equals(Configuration.matrixExt)).menuTopFolder(topFolderMenu)
				.doubleClickEvent(f -> () -> Common.tryCatch(() -> this.model.openMatrix(f), R.MATRIX_TN_ERROR_ON_OPEN_MATRIX_FILE.get())).menuFiles(menuFiles)
				.menuFolder(menuFolders).byPass());
	}

	public void select(File file, Consumer<TreeItem<TreeNode>> consumer)
	{
		selectFile(file, consumer, this.treeItem);
	}
}
