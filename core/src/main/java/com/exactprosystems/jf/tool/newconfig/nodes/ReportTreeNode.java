/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
				ConfigurationTreeView.createItem(REFRESH, () -> this.model.refreshReport(), R.REPORT_TN_ERROR_ON_REFRESH.get()),
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
