////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

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
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlTreeNode extends TreeNode
{
	private TreeItem<TreeNode> treeItem;
	private ConfigurationFx model;
	private TestingConnectionFxController	testSqlController;

	public SqlTreeNode(ConfigurationFx model, TreeItem<TreeNode> treeItem)
	{
		this.treeItem = treeItem;
		this.model = model;
	}

	@Override
	public Optional<ContextMenu> contextMenu()
	{
		return Optional.of(ConfigurationTreeView.add("Add new sql", e ->
				ConfigurationTreeView.showInputDialog("Enter new sql name")
						.ifPresent(res -> Common.tryCatch(() -> this.model.addNewSqlEntry(res), "Error on add new import"))
		));
	}

	@Override
	public Node getView()
	{
		return new Text("Sql entries");
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
		Settings settings = this.model.getSettings();
		String s = entry.get(Configuration.entryName);
		List<Settings.SettingsValue> values = settings.getValues(Settings.GLOBAL_NS, Settings.SQL + s);
		Common.tryCatchThrow(() -> this.showTestSqlPanel(entry, values), "Error on show testing panel");
	}

	private void showTestSqlPanel(SqlEntry entry, List<Settings.SettingsValue> values)
	{
		Common.tryCatch(() ->
		{
			this.testSqlController = Common.loadController(TestingConnectionFxController.class.getResource("TestingConnectionFx.fxml"));
			this.testSqlController.init(model, entry.toString(), values);
			this.testSqlController.display();
			}, "Error on show test sql panel");
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
			MenuItem removeItem = new MenuItem("Remove", new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
			removeItem.setOnAction(e -> Common.tryCatch(() -> model.removeSqlEntry(getEntry()), "Error on remove sql entry"));
			MenuItem testItem = new MenuItem("Test");
			testItem.setOnAction(e -> Common.tryCatch(() -> testSqlEntry(getEntry()), "Error on test sql entry"));
			menu.getItems().addAll(removeItem, testItem);
			return Optional.of(menu);
		}

		@Override
		public List<TablePair> getParameters()
		{
			try
			{
				List<TablePair> list = new ArrayList<>();
				list.add(TablePair.TablePairBuilder.create(Configuration.sqlJar, getEntry().get(Configuration.sqlJar)).edit(true).pathFunc(
						() -> DialogsHelper.showOpenSaveDialog("Choose sql", "Jar files(*.jar)", "*.jar", DialogsHelper.OpenSaveMode.OpenFile))
						.build());
				list.add(TablePair.TablePairBuilder.create(Configuration.sqlConnection, getEntry().get(Configuration.sqlConnection)).edit(true).build());
				return list;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}
}
