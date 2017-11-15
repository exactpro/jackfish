////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Configuration;
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
import java.util.Optional;

public class ConfigurationTreeNode extends TreeNode
{
	private ConfigurationFx model;

	public ConfigurationTreeNode(ConfigurationFx model)
	{
		this.model = model;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = new ContextMenu();

		MenuItem refresh = new MenuItem("Refresh", new ImageView(new Image(CssVariables.Icons.REFRESH)));
		refresh.setOnAction(e -> Common.tryCatch(() -> model.refresh(), "Error on refresh configuration"));

		Menu menuNew = new Menu("New");
		menuNew.getItems().addAll(
				createNewDocumentMenuItem("Dictionary", DocumentKind.GUI_DICTIONARY),
				createNewDocumentMenuItem("System variables", DocumentKind.SYSTEM_VARS),
				createNewDocumentMenuItem("Matrix", DocumentKind.MATRIX),
				createNewDocumentMenuItem("Library", () -> this.model.newLibrary()),
				createNewDocumentMenuItem("Plain text", DocumentKind.PLAIN_TEXT),
				createNewDocumentMenuItem("CSV", DocumentKind.CSV)
		);
		MenuItem newDictionary = new MenuItem("Dictionary");
		newDictionary.setOnAction(e -> Common.tryCatch(() -> this.model.newDocument(DocumentKind.GUI_DICTIONARY), "Error on create new dictionary"));

		menu.getItems().add(menuNew);

		menu.getItems().addAll(refresh, new SeparatorMenuItem());
		menu.getItems().addAll(ConfigurationTreeView.gitContextMenu(new File(".")).getItems());
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		HBox box = new HBox();
		String name = this.model.getNameProperty().get();
		String fullPath = ConfigurationFx.path(name);
		if (name.equals(fullPath))
		{
			name = new File(name).getName();
		}
		Text configName = new Text();
		configName.setText(name);
		box.getChildren().add(configName);
		Label lblFullPath = new Label(" (" + fullPath + ")");
		lblFullPath.setTooltip(new Tooltip(fullPath));
		lblFullPath.getStyleClass().add(CssVariables.FULL_PATH_LABEL);
		box.getChildren().add(lblFullPath);
		return box;
	}

	@Override
	public List<TablePair> getParameters()
	{
		List<TablePair> list = new ArrayList<>();
		list.add(TablePair.TablePairBuilder.create(Configuration.version, this.model.getVersionStr()).edit(true).build());
		list.add(TablePair.TablePairBuilder.create(Configuration.matrix, this.model.matrixToString()).tooltipSeparator(ConfigurationFx.SEPARATOR).edit(false).build());
		list.add(TablePair.TablePairBuilder.create(Configuration.library, this.model.libraryToString()).tooltipSeparator(ConfigurationFx.SEPARATOR).edit(false).build());
		list.add(TablePair.TablePairBuilder.create("appDictionaries", this.model.getAppDictionaries()).tooltipSeparator(ConfigurationFx.SEPARATOR).edit(false).build());
		list.add(TablePair.TablePairBuilder.create("clientDictionaries", this.model.getClientDictionaries()).tooltipSeparator(ConfigurationFx.SEPARATOR).edit(false).build());
		return list;
	}

	@Override
	public void updateParameter(String key, String value)
	{
		if (key.equals(Configuration.version))
		{
			this.model.getVersion().set(value);
		}
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.CONFIGURATION_ICON));
	}

	private MenuItem createNewDocumentMenuItem(String name, DocumentKind kind)
	{
		MenuItem menuItem = new MenuItem(name);
		menuItem.setOnAction(e -> Common.tryCatch(() -> this.model.newDocument(kind), "Error on create new " + name.toLowerCase()));
		return menuItem;
	}

	private MenuItem createNewDocumentMenuItem(String name, Common.Function fn)
	{
		MenuItem menuItem = new MenuItem(name);
		menuItem.setOnAction(e -> Common.tryCatch(fn, "Error on create new " + name.toLowerCase()));
		return menuItem;
	}
}
