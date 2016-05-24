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

		menu.getItems().addAll(refresh, new SeparatorMenuItem());
		menu.getItems().addAll(ConfigurationTreeView.gitContextMenu(new File(".")).getItems());
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		HBox box = new HBox();
		String name = this.model.getName();
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
		list.add(TablePair.TablePairBuilder.create("matrix", this.model.matrixToString()).tooltipSeparator(ConfigurationFx.SEPARATOR).edit(false).build());
		list.add(TablePair.TablePairBuilder.create("libs", this.model.libraryToString()).tooltipSeparator(ConfigurationFx.SEPARATOR).edit(false).build());
		list.add(TablePair.TablePairBuilder.create("gitRemotePath", this.model.gitRemotePath()).edit(true).build());
		list.add(TablePair.TablePairBuilder.create("appDictionaries", this.model.getAppDictionaries()).tooltipSeparator(ConfigurationFx.SEPARATOR).edit(false).build());
		list.add(TablePair.TablePairBuilder.create("clientDictionaries", this.model.getClientDictionaries()).tooltipSeparator(ConfigurationFx.SEPARATOR).edit(false).build());
		return list;
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.CONFIGURATION_ICON));
	}

}
