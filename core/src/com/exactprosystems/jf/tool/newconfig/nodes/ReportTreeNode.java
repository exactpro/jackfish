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

	private static final SerializablePair<R, String> CLEAR_FOLDER = new SerializablePair<>(R.REPORT_TREE_NODE_CLEAR_FOLDER, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> REFRESH = new SerializablePair<>(R.REPORT_TREE_NODE_REFRESH, CssVariables.Icons.REFRESH);
	private static final SerializablePair<R, String> OPEN_REPORT = new SerializablePair<>(R.REPORT_TREE_NODE_OPEN_REPORT, CssVariables.Icons.REPORT_ICON);
	private static final SerializablePair<R, String> REMOVE_REPORT = new SerializablePair<>(R.REPORT_TREE_NODE_REMOVE_REPORT, CssVariables.Icons.REMOVE_PARAMETER_ICON);

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
				ConfigurationTreeView.createDisabledItem(OPEN_REPORT),
				ConfigurationTreeView.createDisabledItem(REMOVE_REPORT),
				ConfigurationTreeView.createItem(CLEAR_FOLDER, () -> this.model.clearReportFolder(), R.REPORT_TN_ERROR_ON_CLEAR.get()),
				ConfigurationTreeView.createItem(REFRESH, () -> this.model.updateReport(), R.REPORT_TN_ERROR_ON_REFRESH.get()),
				new SeparatorMenuItem(),
				ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
		);
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		return new Text(R.REPORT_TN_VIEW.get());
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
						ConfigurationTreeView.createItem(OPEN_REPORT, () -> this.model.openReport(file), R.REPORT_TN_ERROR_ON_OPEN.get()),
						ConfigurationTreeView.createItem(REMOVE_REPORT, () -> this.model.removeReport(file), R.REPORT_TN_ERROR_ON_REMOVE.get()),
						ConfigurationTreeView.createDisabledItem(CLEAR_FOLDER),
						ConfigurationTreeView.createDisabledItem(REFRESH)
				);
				return menu;
			};
			Arrays.stream(files)
					.sorted(model.getFileComparator())
					.forEach(initFile -> new BuildTree(initFile, this.treeItem,model.getFileComparator())
							.doubleClickEvent(file -> () -> this.model.openReport(file))
							.menuFiles(menuFiles)
							.byPass()
					);
		}
	}
}
