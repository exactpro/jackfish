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
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.SqlEntry;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.ConfigurationTreeView;
import com.exactprosystems.jf.tool.newconfig.TablePair;
import com.exactprosystems.jf.tool.newconfig.testing.TestingConnectionFxController;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlTreeNode extends TreeNode
{
	private TreeItem<TreeNode> treeItem;
	private ConfigurationFx model;
	private TestingConnectionFxController	testSqlController;

	private static final SerializablePair<R, String> ADD_NEW_SQL = new SerializablePair<>(R.SQL_TREE_NODE_ADD_NEW, CssVariables.Icons.ADD_PARAMETER_ICON);
	private static final SerializablePair<R, String> REMOVE_SQL = new SerializablePair<>(R.SQL_TREE_NODE_REMOVE, CssVariables.Icons.REMOVE_PARAMETER_ICON);
	private static final SerializablePair<R, String> TEST = new SerializablePair<>(R.SQL_TREE_NODE_TEST, null);

	public SqlTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
	{
		this.treeItem = treeItem;
		this.model = model;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		ContextMenu menu = ConfigurationTreeView.add(R.SQL_TN_ADD_NEW.get(),
				e -> DialogsHelper.showInputDialog(R.SQL_TN_ENTER_NEW_NAME.get(), "").ifPresent(
						res -> Common.tryCatch(() -> this.model.addNewSqlEntry(res), R.SQL_TN_ERROR_ON_ADD_IMPORT.get()))
		);
		menu.getItems().addAll(
				ConfigurationTreeView.createDisabledItem(REMOVE_SQL),
				ConfigurationTreeView.createDisabledItem(TEST)
		);
		return Optional.of(menu);
	}

	@Override
	public Node getView()
	{
		return new Text(R.SQL_TN_VIEW.get());
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(CssVariables.Icons.SQL_ICON));
	}

	public void display(List<SqlEntry> entries)
	{
		this.treeItem.getChildren().clear();
		entries.stream()
				.map(sqlEntry -> new SqlEntryNode(model, sqlEntry))
				.map(e -> new TreeItem<TreeNode>(e))
				.forEach(i -> this.treeItem.getChildren().add(i));
	}

	public void testSqlEntry(SqlEntry entry) throws Exception
	{
		Settings settings = this.model.getFactory().getSettings();
		String s = entry.get(Configuration.entryName);
		List<Settings.SettingsValue> values = settings.getValues(Settings.GLOBAL_NS, Settings.SQL + s);
		Common.tryCatchThrow(() -> this.showTestSqlPanel(entry, values), R.SQL_TN_ERROR_ON_SHOW_TESTING_PANEL.get());
	}

	private void showTestSqlPanel(SqlEntry entry, List<Settings.SettingsValue> values)
	{
		Common.tryCatch(() ->
		{
			this.testSqlController = Common.loadController(TestingConnectionFxController.class.getResource("TestingConnectionFx.fxml"));
			this.testSqlController.init(model, entry.toString(), values);
			this.testSqlController.display();
		}, R.SQL_TN_ERROR_ON_SHOW_TEST_PANEL.get());
	}


	private class SqlEntryNode extends AbstractEntryNode<SqlEntry>
	{
		public SqlEntryNode(ConfigurationFx model, SqlEntry sqlEntry)
		{
			super(model, sqlEntry);
		}

		@Override
		public Optional<ContextMenu> contextMenu()
		{
			ContextMenu menu = new ContextMenu();
			menu.getItems().addAll(
					ConfigurationTreeView.createDisabledItem(ADD_NEW_SQL),
					ConfigurationTreeView.createItem(REMOVE_SQL, () -> model.removeSqlEntry(getEntry()), R.SQL_TN_ERROR_ON_REMOVE_ENTRY.get()),
					ConfigurationTreeView.createItem(TEST, () -> testSqlEntry(getEntry()), R.SQL_TN_ERROR_ON_TEST_ENTRY.get())
			);
			return Optional.of(menu);
		}

		@Override
		public List<TablePair> getParameters()
		{
			try
			{
				List<TablePair> list = new ArrayList<>();
				list.add(TablePair.TablePairBuilder.create(Configuration.sqlJar, getEntry().get(Configuration.sqlJar)).required().edit(true).pathFunc(
						() -> DialogsHelper.showOpenSaveDialog(R.SQL_TN_CHOOSE_SQL.get(), R.COMMON_JAR_FILTER.get(), "*.jar", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
				list.add(TablePair.TablePairBuilder.create(Configuration.sqlConnection, getEntry().get(Configuration.sqlConnection)).required().edit(true).build());
				return list;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected boolean needSupport()
		{
			return false;
		}
	}
}
