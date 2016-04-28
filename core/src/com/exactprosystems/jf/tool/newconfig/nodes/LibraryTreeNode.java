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

public class LibraryTreeNode extends TreeNode
{
	private ConfigurationFxNew model;
	private TreeItem<TreeNode> treeItem;

	public LibraryTreeNode(ConfigurationFxNew configuration, TreeItem<TreeNode> treeItem)
	{
		this.model = configuration;
		this.treeItem = treeItem;
	}

	@Override
	public Node getView()
	{
		return new Text("library");
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.LIBRARY_ICON));
	}

	public void display(List<String> librariesValue)
	{
		this.treeItem.getChildren().clear();
		Function<File, ContextMenu> menuTopFolder = file -> {
			ContextMenu menu = new ContextMenu();
			MenuItem itemRemove = new MenuItem("Remove library dir", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			itemRemove.setOnAction(e -> Common.tryCatch(() -> model.removeLibraryDirectory(file.getName()), "Error on remove library directory"));
			menu.getItems().addAll(itemRemove);
			return menu;
		};
		Function<File, ContextMenu> menuFiles = file -> {
			ContextMenu menu = new ContextMenu();
			MenuItem itemOpen = new MenuItem("Open library", new ImageView(new Image(CssVariables.Icons.LIBRARY_ICON)));
			itemOpen.setOnAction(e -> Common.tryCatch(() -> this.model.openLibrary(file), "Error on open library file"));

			MenuItem addNewLibrary = new MenuItem("Add new library", new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
			addNewLibrary.setOnAction(e ->
					Common.tryCatch(() ->
									ConfigurationTreeView.showInputDialog("Enter new name").ifPresent(name ->Common.tryCatch(() -> this.model.addNewLibrary(file, name), "Error on create new " +
											"library")),
							"Error on add new library"));

			MenuItem removeLibrary = new MenuItem("Remove library", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			removeLibrary.setOnAction(e -> Common.tryCatch(() -> this.model.removeLibrary(file), "Error on remove library"));
			
			menu.getItems().addAll(itemOpen, addNewLibrary, removeLibrary);
			return menu;
		};
		Function<File, ContextMenu> menuFolders = file -> {
			ContextMenu menu = new ContextMenu();
			MenuItem addNewLibrary = new MenuItem("Add new library", new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
			addNewLibrary.setOnAction(e ->
					Common.tryCatch(() ->
									ConfigurationTreeView.showInputDialog("Enter new name").ifPresent(name -> Common.tryCatch(() -> this.model.addNewLibrary(file, name), "Error on create new " +
											"library")),
							"Error on add new library"));

			MenuItem removeFolder = new MenuItem("Remove folder", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			removeFolder.setOnAction(e -> Common.tryCatch(() -> this.model.removeLibrary(file), "Error on remove folder"));

			menu.getItems().addAll(addNewLibrary, removeFolder);
			return menu;
		};
		
		librariesValue.forEach(file -> new BuildTree(new File(file), this.treeItem)
				.doubleClickEvent(f -> () -> this.model.openLibrary(f))
				.fileFilter(f -> ConfigurationFxNew.getExtension(f.getAbsolutePath()).equals(ConfigurationFxNew.matrixExt))
				.menuTopFolder(menuTopFolder)
				.menuFiles(menuFiles)
				.menuFolder(menuFolders)
				.byPass()
		);
	}

	public void select(File file, Consumer<TreeItem<TreeNode>> consumer)
	{
		selectFile(file, consumer, this.treeItem);
	}
}