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
import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.TablePair;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VariablesTreeNode extends TreeNode
{
	private ConfigurationFx		model;
	private TreeItem<TreeNode>	variablesTreeNode;

	private static final SerializablePair<R, String> REMOVE_VARS_FILE = new SerializablePair<>(R.VARIABLES_TREE_NODE_REMOVE_VARS, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> OPEN_VARS_FILE = new SerializablePair<>(R.VARIABLES_TREE_NODE_OPEN_VARS_FILE, CssVariables.Icons.VARS_ICON);

	public VariablesTreeNode(ConfigurationFx model, TreeItem<TreeNode> variablesTreeNode)
	{
		this.model = model;
		this.variablesTreeNode = variablesTreeNode;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = new ContextMenu();
		menu.getItems().addAll(
				ConfigurationTreeView.createDisabledItem(REMOVE_VARS_FILE),
				ConfigurationTreeView.createDisabledItem(OPEN_VARS_FILE),
				ConfigurationTreeView.createDisabledItem(R.COMMON_GIT.get(), null)
		);
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		return new Text(R.VARS_TN_VIEW.get());
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.VARS_ICON));
	}

	public void display(List<String> files)
	{
		this.variablesTreeNode.getChildren().clear();
		files.stream().sorted(String::compareTo).map(f -> MainRunner.makeDirWithSubstitutions(f)).map(file -> new FileTreeNode(new File(file))
		{
			@Override
			public Optional<ContextMenu> contextMenu()
			{
				ContextMenu menu = new ContextMenu();
				menu.getItems().addAll(
						ConfigurationTreeView.createItem(REMOVE_VARS_FILE, () -> model.excludeVarsFile(file), R.VARS_TN_ERROR_ON_REMOVE.get()),
						ConfigurationTreeView.createItem(OPEN_VARS_FILE, () -> model.openVariableFile(new File(file)), R.VARS_TN_ERROR_ON_SAVE.get())
				);
				menu.getItems().addAll(super.contextMenu().orElse(new ContextMenu()).getItems());
				return Optional.of(menu);
			}

			@Override
			public Common.Function onDoubleClickEvent()
			{
				return () -> model.openVariableFile(new File(file));
			}

			@Override
			public List<TablePair> getParameters()
			{
				List<TablePair> list = new ArrayList<>();
				list.add(TablePair.TablePairBuilder.create("path", ConfigurationFx.path(file)).edit(false).build());
				return list;
			}
		}).map(e -> new TreeItem<TreeNode>(e)).forEach(i -> this.variablesTreeNode.getChildren().add(i));
	}
}
