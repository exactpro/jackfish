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
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Optional;

public class EvaluatorTreeNode extends TreeNode
{
	private ConfigurationFx model;
	private TreeItem<TreeNode> evaluatorTreeItem;

	private static final SerializablePair<R, String> ADD_IMPORT = new SerializablePair<>(R.EVALUATOR_TREE_NODE_ADD_IMPORT, CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<R, String> REMOVE_IMPORT = new SerializablePair<>(R.EVALUATOR_TREE_NODE_REMOVE, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> REPLACE_IMPORT = new SerializablePair<>(R.EVALUATOR_TREE_NODE_REPLACE, null);

	public EvaluatorTreeNode(ConfigurationFx configuration, TreeItem<TreeNode> treeItem)
	{
		this.model = configuration;
		this.evaluatorTreeItem = treeItem;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu contextMenu = ConfigurationTreeView.add(R.EVALUATOR_TREE_NODE_ADD_IMPORT.get(),
				e -> DialogsHelper.showInputDialog(R.EVALUATOR_TREE_NODE_ENTER_IMPORT.get(), "").ifPresent(
						res -> Common.tryCatch(() -> this.model.addNewEvaluatorImport(res), R.EVALUATOR_TREE_NODE_ERROR_ON_ADD.get())
				));
		contextMenu.getItems().addAll(
				ConfigurationTreeView.createDisabledItem(REMOVE_IMPORT),
				ConfigurationTreeView.createDisabledItem(REPLACE_IMPORT)
		);

		return Optional.of(contextMenu);
	}

	@Override
	public Node getView()
	{
		return new Text(R.EVALUATOR_TREE_NODE_EVALUATOR.get());
	}

	@Override
	public Optional<Image> icon()
	{
		Image image = new Image(CssVariables.Icons.EVALUATOR_ICON);
		return Optional.of(image);
	}

	public void display(List<String> imports2) throws Exception
	{
		this.evaluatorTreeItem.getChildren().clear();
		imports2.stream().map(evaluatorImport ->
		{
			TreeItem<TreeNode> remove = new TreeItem<>();
			TreeNode importNode = new TreeNodeImport(evaluatorImport);
			remove.setValue(importNode);
			return remove;
		}).forEach(this.evaluatorTreeItem.getChildren()::add);
	}

	private void remove(String evaluatorImport) throws Exception
	{
		this.model.removeImport(evaluatorImport);
	}

	private class TreeNodeImport extends TreeNode
	{
		private String evaluatorImport;

		public TreeNodeImport(String evaluatorImport)
		{
			this.evaluatorImport = evaluatorImport;
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(ADD_IMPORT),
					ConfigurationTreeView.createItem(REMOVE_IMPORT, () -> remove(this.evaluatorImport), R.EVALUATOR_TREE_NODE_ERROR_REMOVE.get()),
					ConfigurationTreeView.createItem(REPLACE_IMPORT, this::replaceEvaluator, R.EVALUATOR_TREE_NODE_ERROR_REMOVE.get())
			);
			return Optional.of(menu);
		}

		@Override
		public Node getView()
		{
			return new Text(this.evaluatorImport);
		}

		@Override
		public Common.Function onDoubleClickEvent()
		{
			return this::replaceEvaluator;
		}

		@Override
		public Optional<Image> icon()
		{
			return Optional.empty();
		}

		private void replaceEvaluator()
		{
			Dialog<String> dialog = new TextInputDialog(this.evaluatorImport);
			dialog.setResizable(true);
			dialog.setTitle(R.EVALUATOR_TREE_NODE_REPLACE.get());
			dialog.setHeaderText(R.EVALUATOR_TREE_NODE_ENTER_NEW.get());
			dialog.showAndWait().ifPresent(str -> Common.tryCatch(() -> model.replaceEvaluatorImport(this.evaluatorImport, str), R.EVALUATOR_TREE_NODE_ERROR_CHANGE.get()));
		}
	}
}
