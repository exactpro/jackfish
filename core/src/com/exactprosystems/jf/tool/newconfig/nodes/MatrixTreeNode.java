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
				ConfigurationTreeView.createDisabledItem(open()),
				ConfigurationTreeView.createDisabledItem(openAsText()),
				ConfigurationTreeView.createDisabledItem(addNew()),
				ConfigurationTreeView.createDisabledItem(remove()),
				ConfigurationTreeView.createDisabledItem(addToToolbar()),
				ConfigurationTreeView.createDisabledItem(removeMatrixFolder()),
				ConfigurationTreeView.createDisabledItem(excludeMatrixFolder()),
				ConfigurationTreeView.createItem(refresh(), () -> this.model.updateMatrices(), R.MATRIX_TN_ERROR_ON_REFRESH.get()),
				ConfigurationTreeView.createDisabledItem(rename()),
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
					ConfigurationTreeView.createDisabledItem(open()),
					ConfigurationTreeView.createDisabledItem(openAsText()),
					ConfigurationTreeView.createDisabledItem(remove()),
					ConfigurationTreeView.createDisabledItem(addToToolbar()),
					ConfigurationTreeView.createItem(excludeMatrixFolder(), () -> model.excludeMatrixDirectory(file.getName()), R.MATRIX_TN_ERROR_ON_REMOVE_DIR.get()),
					ConfigurationTreeView.createDisabledItem(refresh()),
					ConfigurationTreeView.createDisabledItem(rename())
			);
			return menu;
		};
		Function<File, ContextMenu> menuFiles = file ->
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createItem(open(),() -> this.model.openMatrix(file), R.MATRIX_TN_ERROR_ON_OPEN_MATRIX.get()),
					ConfigurationTreeView.createItem(openAsText(),() -> this.model.openPlainText(file), R.MATRIX_TN_ERROR_ON_OPEN_MATRIX.get()),
					ConfigurationTreeView.createItem(addNew(), () -> DialogsHelper.showInputDialog(R.MATRIX_TN_ENTER_NEW_NAME.get(), "").ifPresent(
							name -> Common.tryCatch(() -> this.model.addNewMatrix(file, name), R.MATRIX_TN_ERROR_ON_CREATE_MATRIX.get())), R.MATRIX_TN_ERROR_ON_ADD_MATRIX.get()),
					ConfigurationTreeView.createItem(remove(), () -> this.model.removeMatrix(file), R.MATRIX_TN_ERROR_ON_REMOVE.get()),
					ConfigurationTreeView.createItem(addToToolbar(), () -> this.model.addToToolbar(Common.getRelativePath(file.getPath())), R.MATRIX_TN_ERROR_ADD_TO_TOOLBAR.get()),
					ConfigurationTreeView.createDisabledItem(removeMatrixFolder()),
					ConfigurationTreeView.createDisabledItem(excludeMatrixFolder()),
					ConfigurationTreeView.createDisabledItem(refresh()),
					ConfigurationTreeView.createItem(rename(), () -> this.model.renameMatrix(file), R.MATRIX_TN_ERROR_ON_RENAME.get())
			);
			return menu;
		};
		Function<File, ContextMenu> menuFolders = file ->
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(open()),
					ConfigurationTreeView.createDisabledItem(openAsText()),
					ConfigurationTreeView.createItem(addNew(), () -> DialogsHelper.showInputDialog(R.MATRIX_TN_ENTER_NEW_NAME.get(), "").ifPresent(
							name -> Common.tryCatch(() -> this.model.addNewMatrix(file, name), R.MATRIX_TN_ERROR_ON_CREATE_MATRIX.get())), R.MATRIX_TN_ERROR_ON_ADD_MATRIX.get()),
					ConfigurationTreeView.createDisabledItem(remove()),
					ConfigurationTreeView.createDisabledItem(addToToolbar()),
					ConfigurationTreeView.createItem(removeMatrixFolder(), () -> this.model.removeMatrix(file), R.MATRIX_TN_ERROR_ON_REMOVE_FOLDER.get()),
					ConfigurationTreeView.createDisabledItem(excludeMatrixFolder()),
					ConfigurationTreeView.createDisabledItem(refresh()),
					ConfigurationTreeView.createDisabledItem(rename())
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

	private SerializablePair<String, String> refresh()
	{
		return new SerializablePair<>(R.MATRIX_TREE_NODE_REFRESH.get(), CssVariables.Icons.REFRESH);
	}

	private SerializablePair<String, String> rename()
	{
		return new SerializablePair<>(R.MATRIX_TREE_NODE_REMOVE.get(), null);
	}

	private SerializablePair<String, String> open()
	{
		return new SerializablePair<>(R.MATRIX_TREE_NODE_OPEN_MATRIX.get(), CssVariables.Icons.MATRIX_ICON);
	}

	private SerializablePair<String, String> openAsText()
	{
		return new SerializablePair<>(R.MATRIX_TREE_NODE_OPEN_AS_TEXT.get(), CssVariables.Icons.MATRIX_ICON);
	}

	private SerializablePair<String, String> addNew()
	{
		return new SerializablePair<>(R.MATRIX_TREE_NODE_ADD_NEW_MATRIX.get(), CssVariables.Icons.ADD_PARAMETER_ICON);
	}

	private SerializablePair<String, String> remove()
	{
		return new SerializablePair<>(R.MATRIX_TREE_NODE_REMOVE.get(), CssVariables.Icons.REMOVE_PARAMETER_ICON);
	}

	private SerializablePair<String, String> addToToolbar()
	{
		return new SerializablePair<>(R.MATRIX_TREE_NODE_ADD_TO_TOOLBAR.get(), CssVariables.Icons.ADD_PARAMETER_ICON);
	}

	private SerializablePair<String, String> removeMatrixFolder()
	{
		return new SerializablePair<>(R.MATRIX_TREE_NODE_REMOVE_FOLDER.get(), CssVariables.Icons.REMOVE_PARAMETER_ICON);
	}

	private SerializablePair<String, String> excludeMatrixFolder()
	{
		return new SerializablePair<>(R.MATRIX_TREE_NODE_EXCLUDE_FOLDER.get(), CssVariables.Icons.REMOVE_PARAMETER_ICON);
	}
}
