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
import com.exactprosystems.jf.tool.newconfig.TablePair;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigurationTreeNode extends TreeNode
{
	private ConfigurationFxNew model;

	public ConfigurationTreeNode(ConfigurationFxNew model)
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
		return new Text("Configuration_elite");
	}

	@Override
	public List<TablePair> getParameters()
	{
		List<TablePair> list = new ArrayList<>();
		list.add(TablePair.TablePairBuilder.create().key("version").value("2.0").edit(false).build());
		list.add(TablePair.TablePairBuilder.create().key("description").value("some Description").build());
		list.add(TablePair.TablePairBuilder.create().key("matrix").value(this.model.matrixToString()).tooltipSeparator(ConfigurationFxNew.SEPARATOR).edit(false).build());
		list.add(TablePair.TablePairBuilder.create().key("libs").value(this.model.libraryToString()).tooltipSeparator(ConfigurationFxNew.SEPARATOR).edit(false).build());
		list.add(TablePair.TablePairBuilder.create().key("gitRemotePath").value(this.model.gitRemotePath()).edit(true).build());
		list.add(TablePair.TablePairBuilder.create().key("reportPath").value(this.model.getReportPath()).edit(false).build());
		list.add(TablePair.TablePairBuilder.create().key("appDictionaries").value(this.model.getAppDictionaries()).tooltipSeparator(ConfigurationFxNew.SEPARATOR).edit(false).build());
		list.add(TablePair.TablePairBuilder.create().key("clientDictionaries").value(this.model.getClientDictionaries()).tooltipSeparator(ConfigurationFxNew.SEPARATOR).edit(false).build());
		return list;
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.CONFIGURATION_ICON));
	}

}
