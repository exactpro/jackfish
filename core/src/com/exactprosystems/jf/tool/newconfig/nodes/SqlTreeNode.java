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
import com.exactprosystems.jf.documents.config.Configuration.SqlEntry;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFxNew;
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
	private ConfigurationFxNew model;

	public SqlTreeNode(ConfigurationFxNew model, TreeItem<TreeNode> treeItem)
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

	public void display(List<Configuration.SqlEntry> entries)
	{
		this.treeItem.getChildren().clear();
		entries.stream()
				.map(sqlEntry -> new SqlEntryNode(model, sqlEntry))
				.map(e -> new TreeItem<TreeNode>(e))
				.forEach(i -> this.treeItem.getChildren().add(i));
	}

	public void testSqlEntry(SqlEntry entry) throws Exception
	{
//		String s = entry.get(Configuration.entryName);
//		List<Settings.SettingsValue> values = settings.getValues(Settings.GLOBAL_NS, Settings.SQL + s);
//		Common.tryCatchThrow(() -> this.showTestSqlPanel(entry, values), "Error on show testing panel");
	}

	public void testSqlConnection(String sql, String server, String base, String user, String password) throws Exception
	{
//		Common.tryCatch(() ->
//		{
//			settings.removeAll(Settings.GLOBAL_NS, Settings.SQL + sql);
//			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.SERVER_NAME, server);
//			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.USER, user);
//			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.DATABASE_NAME, base);
//			settings.saveIfNeeded();
//			SqlConnection connect = getDataBasesPool().connect(sql, server, base, user, password);
//			if (connect != null && !connect.isClosed() && connect.getConnection().isValid(1))
//			{
//				this.testSqlController.displayConnectionGood();
//			}
//			else
//			{
//				this.testSqlController.displayConnectionBad(null);
//			}
//		}, "Error on test sql connection");
	}

	private void showTestSqlPanel(SqlEntry entry, List<Settings.SettingsValue> values)
	{
//		Common.tryCatch(() ->
//		{
//			testSqlController = Common.loadController(TestingConnectionFxController.class.getResource("TestingConnectionFx.fxml"));
//			// TODO remake TestingConnectionFxController to ConfigurationFxNew
//			// testSqlController.init(model, entry.toString(), values);
//				testSqlController.display();
//			}, "Error on show test sql panel");
	}

	
	private class SqlEntryNode extends AbstractEntryNode<Configuration.SqlEntry>
	{
		public SqlEntryNode(ConfigurationFxNew model, Configuration.SqlEntry sqlEntry)
		{
			super(model, sqlEntry);
		}

		@Override
		protected SupportedEntry getSupportedEntry()
		{
			return null;
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
				list.add(TablePair.TablePairBuilder.create().key(Configuration.sqlJar).value(getEntry().get(Configuration.sqlJar)).edit(true).isPath(true).build());
				list.add(TablePair.TablePairBuilder.create().key(Configuration.sqlConnection).value(getEntry().get(Configuration.sqlConnection)).edit(true).build());
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
