////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.common.MutableString;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.TablePair;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LibraryTreeNode extends TreeNode
{
	private ConfigurationFx model;
	private TreeItem<TreeNode> treeItem;

	private static final SerializablePair<String, String> REFRESH_LIBRARY = new SerializablePair<>("Refresh", CssVariables.Icons.REFRESH);

	private static final SerializablePair<String, String> ADD_NEW_LIBRARY = new SerializablePair<>("Add new library to", CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<String, String> EXCLUDE_LIB_FOLDER = new SerializablePair<>("Exclude library folder", CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<String, String> OPEN_LIBRARY = new SerializablePair<>("Open library", CssVariables.Icons.LIBRARY_ICON);
	private static final SerializablePair<String, String> REMOVE_LIBRARY = new SerializablePair<>("Remove library", CssVariables.Icons.REMOVE_PARAMETER_ICON);


	public LibraryTreeNode(ConfigurationFx configuration, TreeItem<TreeNode> treeItem)
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

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = new ContextMenu();

		menu.getItems().addAll(
				ConfigurationTreeView.createDisabledItem(OPEN_LIBRARY),
				ConfigurationTreeView.createDisabledItem(REMOVE_LIBRARY),
				ConfigurationTreeView.createItem(REFRESH_LIBRARY, () -> this.model.updateLibraries(), "Error on refresh libs")
		);

		boolean isLibEmpty = this.model.getLibrariesValue().isEmpty();
		if (!isLibEmpty)
		{
			Menu addLibrary = new Menu("Add new library to", new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
			this.model.getLibrariesValue()
					.stream()
					.map(MutableString::get)
					.map(Common::getRelativePath)
					.map(MenuItem::new)
					.peek(item -> item.setOnAction(
							e -> ConfigurationTreeView.showInputDialog("Enter new name")
									.ifPresent(name -> Common.tryCatch(() -> this.model.addNewLibrary(new File(item.getText()), name),
											"Error on create new library")
									)
							)
					).forEach(addLibrary.getItems()::add);

			Menu excludeLibrary = new Menu("Exclude library folder", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			this.model.getLibrariesValue()
					.stream()
					.map(MutableString::get)
					.map(Common::getRelativePath)
					.map(MenuItem::new)
					.peek(item -> item.setOnAction(
							e -> Common.tryCatch(() -> this.model.excludeLibraryDirectory(item.getText()), "Error on exclude library folder")
						)
					).forEach(excludeLibrary.getItems()::add);

			menu.getItems().addAll(addLibrary, excludeLibrary);
		}

		menu.getItems().addAll(
				new SeparatorMenuItem(),
				ConfigurationTreeView.createDisabledItem("Git", null)
		);

		return Optional.of(menu);
	}

	public void display(Map<String, Matrix> map)
	{
		this.treeItem.getChildren().clear();
		map.entrySet().stream().map(entry -> new TreeNodeLib(entry.getValue(), entry.getKey(), ConfigurationFx.path(entry.getValue().getName()))).map(lib -> {
			TreeItem<TreeNode> treeItem = new TreeItem<>();
			treeItem.setValue(lib);
			return treeItem;
		}).forEach(this.treeItem.getChildren()::add);
	}

	private class TreeNodeLib extends TreeNode
	{
		private Matrix lib;
		private String namespace;
		private String fullPath;

		public TreeNodeLib(Matrix lib, String namespace, String fullPath)
		{
			this.lib = lib;
			this.namespace = namespace;
			this.fullPath = fullPath;
		}

		@Override
		public Node getView()
		{
			HBox box = new HBox();
			Text textNamespace = new Text("<" + this.namespace + "> ");
			Text textName = new Text(new File(this.lib.getName()).getName());
			box.getChildren().addAll(textNamespace, textName);
			return box;
		}

		@Override
		public Optional<Image> icon()
		{
			return Optional.empty();
		}

		@Override
		public Common.Function onDoubleClickEvent()
		{
			return () -> model.openLibrary(this.fullPath);
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createItem(OPEN_LIBRARY, () -> model.openLibrary(this.fullPath), "Error on open library file"),
					ConfigurationTreeView.createItem(REMOVE_LIBRARY, () -> model.removeLibrary(this.namespace), "Error on remove library"),
					ConfigurationTreeView.createDisabledItem(ADD_NEW_LIBRARY),
					ConfigurationTreeView.createDisabledItem(EXCLUDE_LIB_FOLDER),
					ConfigurationTreeView.createDisabledItem(REFRESH_LIBRARY),
					new SeparatorMenuItem(),
					ConfigurationTreeView.createDisabledItem("Git", null)
			);
			MenuItem itemRemove = new MenuItem("Remove library", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			itemRemove.setOnAction(e -> Common.tryCatch(() -> model.removeLibrary(this.namespace), "Error on remove library"));
			return Optional.of(menu);
		}

		@Override
		public List<TablePair> getParameters()
		{
			List<TablePair> list = new ArrayList<>();
			list.add(TablePair.TablePairBuilder.create("path", this.fullPath).edit(false).build());
			return list;
		}
	}
}