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
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class ReportTreeNode extends TreeNode
{
	private ConfigurationFxNew model;
	private TreeItem<TreeNode> treeItem;
	private ContextMenu contextMenu;

	public ReportTreeNode(ConfigurationFxNew model, TreeItem<TreeNode> treeItem)
	{
		this.model = model;
		this.treeItem = treeItem;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = new ContextMenu();

		MenuItem itemClear = new MenuItem("Clear folder", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
		itemClear.setOnAction(e -> Common.tryCatch(() -> this.model.clearReportFolder(), "Error on clear folder"));

		menu.getItems().addAll(itemClear);

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

	public void display(File reportFolder)
	{
		this.treeItem.getChildren().clear();
		File[] files = reportFolder.listFiles();
		if (files != null)
		{
			Function<File, ContextMenu> menuFiles = file -> {
				ContextMenu menu = new ContextMenu();

				MenuItem openReport = new MenuItem("Open report", new ImageView(new Image(CssVariables.Icons.REPORT_ICON)));
				openReport.setOnAction(e -> Common.tryCatch(() -> this.model.openReport(file), "Error on open report"));

				MenuItem removeReport = new MenuItem("Remove report", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
				removeReport.setOnAction(e -> Common.tryCatch(() -> this.model.removeReport(file), "Error on remove report"));

				menu.getItems().addAll(openReport, removeReport);
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
