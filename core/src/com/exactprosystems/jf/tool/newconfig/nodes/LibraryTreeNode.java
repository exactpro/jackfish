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
import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.common.MutableString;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
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

	public LibraryTreeNode(ConfigurationFx configuration, TreeItem<TreeNode> treeItem)
	{
		this.model = configuration;
		this.treeItem = treeItem;
	}

	@Override
	public Node getView()
	{
		return new Text(R.LIBRARY_TN_VIEW.get());
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
				ConfigurationTreeView.createDisabledItem(openLibrary()),
				ConfigurationTreeView.createDisabledItem(openAsText()),
				ConfigurationTreeView.createDisabledItem(remove()),
				ConfigurationTreeView.createItem(refresh(), () -> this.model.updateLibraries(), R.LIBRARY_TN_REFRESH_LIBS.get())
		);

		boolean isLibEmpty = this.model.getLibrariesValue().isEmpty();
		if (!isLibEmpty)
		{
			Menu addLibrary = new Menu(R.LIBRARY_TN_ADD_NEW_TO.get(), new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
			this.model.getLibrariesValue()
					.stream()
					.map(mutableString -> MainRunner.makeDirWithSubstitutions(mutableString.get()))
					.map(Common::getRelativePath)
					.map(MenuItem::new)
					.peek(item -> item.setOnAction(
							e -> DialogsHelper.showInputDialog(R.LIBRARY_TN_ENTER_NAME.get(), "")
									.ifPresent(name -> Common.tryCatch(() -> this.model.addNewLibrary(new File(item.getText()), name),
											R.LIBRARY_TN_ERROR_ON_CREATE.get())
									)
							)
					).forEach(addLibrary.getItems()::add);

			Menu excludeLibrary = new Menu(R.LIBRARY_TN_EXCLUDE_FOLDER.get(), new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			this.model.getLibrariesValue()
					.stream()
					.map(MutableString::get)
					.map(Common::getRelativePath)
					.map(MenuItem::new)
					.peek(item -> item.setOnAction(
							e -> Common.tryCatch(() -> this.model.excludeLibraryDirectory(item.getText()), R.LIBRARY_TN_ERROR_ON_EXCLUDE.get())
						)
					).forEach(excludeLibrary.getItems()::add);

			menu.getItems().addAll(addLibrary, excludeLibrary);
		}

		menu.getItems().addAll(
				new SeparatorMenuItem(),
				ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
		);

		return Optional.of(menu);
	}

	public void display(Map<String, Matrix> map)
	{
		this.treeItem.getChildren().clear();
		map.entrySet().stream().map(entry -> new TreeNodeLib(entry.getValue(), entry.getKey(), ConfigurationFx.path(entry.getValue().getNameProperty().get()))).map(lib -> {
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
			Text textName = new Text(new File(this.lib.getNameProperty().get()).getName());
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
					ConfigurationTreeView.createItem(openLibrary(), () -> model.openLibrary(this.fullPath), R.LIBRARY_TN_ERROR_ON_OPEN_FILE.get()),
					ConfigurationTreeView.createItem(openAsText(), () -> model.openPlainText(new File(this.fullPath)), R.LIBRARY_TN_ERROR_ON_OPEN_FILE.get()),
					ConfigurationTreeView.createItem(remove(), () -> model.removeLibrary(this.namespace), R.LIBRARY_TN_ERROR_ON_REMOVE.get()),
					ConfigurationTreeView.createDisabledItem(addNew()),
					ConfigurationTreeView.createDisabledItem(excludeFolder()),
					ConfigurationTreeView.createDisabledItem(refresh()),
					new SeparatorMenuItem(),
					ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
			);
			MenuItem itemRemove = new MenuItem(R.LIBRARY_TN_REMOVE_LIBRARY.get(), new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			itemRemove.setOnAction(e -> Common.tryCatch(() -> model.removeLibrary(this.namespace), R.LIBRARY_TN_ERROR_ON_REMOVE.get()));
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

	private SerializablePair<String, String> refresh()
	{
		return new SerializablePair<>(R.LIBRARY_TREE_NODE_REFRESH.get(), CssVariables.Icons.REFRESH);
	}

	private SerializablePair<String, String> addNew()
	{
		return new SerializablePair<>(R.LIBRARY_TREE_NODE_ADD_NEW.get(), CssVariables.Icons.ADD_PARAMETER_ICON);
	}

	private SerializablePair<String, String> excludeFolder()
	{
		return new SerializablePair<>(R.LIBRARY_TREE_NODE_EXCLUDE_FOLDER.get(), CssVariables.Icons.REMOVE_PARAMETER_ICON);
	}

	private SerializablePair<String, String> openLibrary()
	{
		return new SerializablePair<>(R.LIBRARY_TREE_NODE_OPEN.get(), CssVariables.Icons.LIBRARY_ICON);
	}

	private SerializablePair<String, String> openAsText()
	{
		return new SerializablePair<>(R.LIBRARY_TREE_NODE_OPEN_AS_TEXT.get(), CssVariables.Icons.LIBRARY_ICON);
	}

	private SerializablePair<String, String> remove()
	{
		return new SerializablePair<>(R.LIBRARY_TREE_NODE_REMOVE.get(), CssVariables.Icons.REMOVE_PARAMETER_ICON);
	}
}