////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.api.common.SerializablePair;
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
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class ReportTreeNode extends TreeNode
{
	private ConfigurationFx model;
	private TreeItem<TreeNode> treeItem;

	private static final SerializablePair<String, String> CLEAR_FOLDER = new SerializablePair<>("Clear folder", CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<String, String> REFRESH = new SerializablePair<>("Refresh", CssVariables.Icons.REFRESH);
	private static final SerializablePair<String, String> OPER_REPORT = new SerializablePair<>("Open report", CssVariables.Icons.REPORT_ICON);
	private static final SerializablePair<String, String> REMOVE_REPORT = new SerializablePair<>("Remove report", CssVariables.Icons.REMOVE_PARAMETER_ICON);

	public ReportTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
	{
		this.model = model;
		this.treeItem = treeItem;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = new ContextMenu();

		menu.getItems().addAll(
				ConfigurationTreeView.createDisabledItem(OPER_REPORT),
				ConfigurationTreeView.createDisabledItem(REMOVE_REPORT),
				ConfigurationTreeView.createItem(CLEAR_FOLDER, () -> this.model.clearReportFolder(), "Error on clear folder"),
				ConfigurationTreeView.createItem(REFRESH, () -> this.model.refreshReport(), "Error on refresh report folder"),
				new SeparatorMenuItem(),
				ConfigurationTreeView.createDisabledItem("Git", null)
		);
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		return new Text("Report folder");
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.REPORT_ICON));
	}

	public void display(String reportFolder)
	{
		this.treeItem.getChildren().clear();
		File[] files = new File(reportFolder).listFiles();
		if (files != null)
		{
			Function<File, ContextMenu> menuFiles = file -> {
				ContextMenu menu = new ContextMenu();

				menu.getItems().addAll(
						ConfigurationTreeView.createItem(OPER_REPORT, () -> this.model.openReport(file), "Error on open report"),
						ConfigurationTreeView.createItem(REMOVE_REPORT, () -> this.model.removeReport(file), "Error on remove report"),
						ConfigurationTreeView.createDisabledItem(CLEAR_FOLDER),
						ConfigurationTreeView.createDisabledItem(REFRESH)
				);
				return menu;
			};
			Arrays.stream(files)
					.sorted(ConfigurationTreeView.comparator)
					.forEach(initFile -> new BuildTree(initFile, this.treeItem)
						.doubleClickEvent(file -> () -> this.model.openReport(file))
						.menuFiles(menuFiles)
						.byPass()
			);
		}
	}
}
