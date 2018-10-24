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

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileSystemTreeNode extends TreeNode
{
	private ConfigurationFx	model;
	private TreeItem<TreeNode>	treeItem;

	public FileSystemTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
	{
		this.model = model;
		this.treeItem = treeItem;
	}

	@Override
	public Node getView()
	{
		return new Text(R.FILE_SYSTEM_TN_VIEW.get());
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.FILE_SYSTEM_ICON));
	}

	public void display(File[] initialFiles, List<String> ignoreFiles)
	{
		Iterator<TreeItem<TreeNode>> iterator = this.treeItem.getChildren().iterator();
		boolean needRemove = false;
		while (iterator.hasNext())
		{
			TreeItem<TreeNode> next = iterator.next();
			if (needRemove)
			{
				iterator.remove();
			}
			if (next.getValue() instanceof SeparatorTreeNode)
			{
				needRemove = true;
			}
		}
		if (initialFiles != null)
		{
			Function<File, ContextMenu> menuTopFolder = f ->
			{
				ContextMenu menu = new ContextMenu();

				MenuItem itemAddAsMatrix = new MenuItem(R.FILE_SYSTEM_TN_USE_AS_MATRIX.get(), new ImageView(new Image(CssVariables.Icons.MATRIX_ICON)));
				itemAddAsMatrix.setOnAction(e -> Common.tryCatch(() -> model.useAsMatrix(ConfigurationFx.path(f)), R.FILE_SYSTEM_TN_ERROR_AS_MATRIX.get()));

				MenuItem itemAddAsLibrary = new MenuItem(R.FILE_SYSTEM_TN_USE_AS_LIBRARY.get(), new ImageView(new Image(CssVariables.Icons.LIBRARY_ICON)));
				itemAddAsLibrary.setOnAction(e -> Common.tryCatch(() -> model.useAsLibrary(ConfigurationFx.path(f)), R.FILE_SYSTEM_TN_ERROR_AS_LIBRARY.get()));

				MenuItem itemAddAsAppDic = new MenuItem(R.FILE_SYSTEM_TN_USE_AS_APP.get(), new ImageView(new Image(CssVariables.Icons.APP_DICTIONARY_ICON)));
				itemAddAsAppDic.setOnAction(e -> Common.tryCatch(() -> model.useAsAppDictionaryFolder(ConfigurationFx.path(f)), R.FILE_SYSTEM_TN_ERROR_AS_APP.get()));

				MenuItem itemAddAsClientDic = new MenuItem(R.FILE_SYSTEM_TN_USE_AS_CLIENT.get(), new ImageView(new Image(CssVariables.Icons.CLIENT_DICTIONARY_ICON)));
				itemAddAsClientDic.setOnAction(e -> Common.tryCatch(() -> model.useAsClientDictionaryFolder(ConfigurationFx.path(f)), R.FILE_SYSTEM_TN_ERROR_AS_CLIENT.get()));

				MenuItem itemSetReportDir = new MenuItem(R.FILE_SYSTEM_TN_SET_DIR.get(), new ImageView(new Image(CssVariables.Icons.REPORT_ICON)));
				itemSetReportDir.setOnAction(e -> Common.tryCatch(() -> model.setReportFolder(f.getName()), R.FILE_SYSTEM_TN_ERROR_SET.get()));

				menu.getItems().addAll(itemAddAsMatrix, itemAddAsLibrary, itemAddAsAppDic, itemAddAsClientDic, itemSetReportDir);
				return menu;
			};

			Function<File, ContextMenu> menuFiles = f ->
			{
				ContextMenu menu = new ContextMenu();

				if (f.getName().toLowerCase().endsWith("." + SystemVars.class.getAnnotation(DocumentInfo.class).extension()))
				{
					MenuItem addCsv = new MenuItem(R.FILE_SYSTEM_TN_ADD_VAR.get());
					addCsv.setOnAction(e-> Common.tryCatch(() -> this.model.addUserVarsFile(f), R.FILE_SYSTEM_TN_ERROR_ADD_CSV.get()));
					menu.getItems().add(addCsv);
				}

				return menu;
			};

			Function<File, Common.Function> doubleClickFunction = f -> () ->
			{
				if (f.getName().toLowerCase().endsWith("." + Csv.class.getAnnotation(DocumentInfo.class).extension()))
				{
					this.model.openCsv(f);
				}
			};

			Arrays.stream(initialFiles)
					.sorted(model.getFileComparator())
					.forEach(
							file -> new BuildTree(file, this.treeItem,model.getFileComparator())
									.ignoredFiles(ignoreFiles.stream().map(ConfigurationFx::path).collect(Collectors.toList()))
									.menuTopFolder(menuTopFolder).doubleClickEvent(doubleClickFunction)
									.menuFiles(menuFiles)
									.byPass());
		}
	}
}