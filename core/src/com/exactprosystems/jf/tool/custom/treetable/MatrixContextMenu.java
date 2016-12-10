////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.report.ContextHelpFactory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.MatrixFx.PlaceToInsert;
import com.exactprosystems.jf.tool.settings.SettingsPanel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.Date;
import java.util.Optional;

public class MatrixContextMenu extends ContextMenu
{
	public MatrixContextMenu(Context context, MatrixFx matrix, MatrixTreeView tree, Settings settings)
	{
		super();

		setAutoHide(true);

		MenuItem breakPoint = new MenuItem("Breakpoint" + "\t" + SettingsPanel.getShortcutName(settings, SettingsPanel.BREAK_POINT));
		breakPoint.setGraphic(new ImageView(new Image(CssVariables.Icons.BREAK_POINT_ICON)));
		breakPoint.setOnAction(event -> breakPoint(matrix, tree));

		MenuItem addBefore = new MenuItem("Add before >>" + SettingsPanel.getShortcutName(settings, SettingsPanel.ADD_BEFORE));
		addBefore.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_BEFORE_ICON)));
		addBefore.setOnAction(event -> addBefore(tree, matrix));

		MenuItem addAfter = new MenuItem("Add after >>" + SettingsPanel.getShortcutName(settings, SettingsPanel.ADD_AFTER));
		addAfter.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_AFTER_ICON)));
		addAfter.setOnAction(event -> addAfter(matrix, tree));

		MenuItem addChild = new MenuItem("Add child >>" + SettingsPanel.getShortcutName(settings, SettingsPanel.ADD_CHILD));
		addChild.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_CHILD_ICON)));
		addChild.setOnAction(event -> Common.tryCatch(() -> matrix.insertNew(tree.currentItem(), PlaceToInsert.Child, Tokens.TempItem.get(), null), "Error on add child"));

		MenuItem deleteItem = new MenuItem("Delete", createShortcut(SettingsPanel.DELETE_ITEM, settings));
		deleteItem.setGraphic(new ImageView(new Image(CssVariables.Icons.DELETE_ICON)));
		deleteItem.setOnAction(event -> deleteCurrentItems(matrix, tree));

		MenuItem copy = new MenuItem("Copy" + SettingsPanel.getShortcutName(settings, SettingsPanel.COPY_ITEMS));
		copy.setGraphic(new ImageView(new Image(CssVariables.Icons.COPY_ICON)));
		copy.setOnAction(event -> copyItems(matrix, tree));

		MenuItem pasteAfter = new MenuItem("Paste after" + SettingsPanel.getShortcutName(settings, SettingsPanel.PASTE_ITEMS_AFTER));
		pasteAfter.setGraphic(new ImageView(new Image(CssVariables.Icons.PASTE_ICON)));
		pasteAfter.setOnAction(event -> pasteItems(PlaceToInsert.After, matrix, tree));

		MenuItem pasteChild = new MenuItem("Paste child" + SettingsPanel.getShortcutName(settings, SettingsPanel.PASTE_ITEMS_CHILD));
		pasteChild.setGraphic(new ImageView(new Image(CssVariables.Icons.PASTE_ICON)));
		pasteChild.setOnAction(event -> pasteItems(PlaceToInsert.Child, matrix, tree));

		MenuItem pasteBefore = new MenuItem("Paste before" + SettingsPanel.getShortcutName(settings, SettingsPanel.PASTE_ITEMS_BEFORE));
		pasteBefore.setGraphic(new ImageView(new Image(CssVariables.Icons.PASTE_ICON)));
		pasteBefore.setOnAction(event -> pasteItems(PlaceToInsert.Before, matrix, tree));

		MenuItem gotoItem = new MenuItem("Go to line ..." + SettingsPanel.getShortcutName(settings, SettingsPanel.GO_TO_LINE));
		gotoItem.setGraphic(new ImageView(new Image(CssVariables.Icons.GO_TO_LINE_ICON)));
		gotoItem.setOnAction(event -> gotoLine(tree));

		MenuItem help = new MenuItem("Help" + SettingsPanel.getShortcutName(settings, SettingsPanel.HELP));
		help.setGraphic(new ImageView(new Image(CssVariables.Icons.HELP_ICON)));
		help.setOnAction(showHelp(context, tree));

		MenuItem parAdd = new MenuItem("Add param to end" + SettingsPanel.getShortcutName(settings, SettingsPanel.ADD_PARAMETER));
		parAdd.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
		parAdd.setOnAction(event -> addParameter(matrix, tree));

		getItems().addAll(breakPoint, new SeparatorMenuItem(), parAdd, new SeparatorMenuItem(), copy, pasteBefore, pasteChild, pasteAfter, new SeparatorMenuItem(), addBefore, addChild, addAfter, deleteItem, gotoItem, new SeparatorMenuItem(), help);
	}

	public void initShortcuts(Settings settings, MatrixTreeView treeView, MatrixFx matrix, Context context)
	{
		treeView.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
			if (keyEvent.getCode() == KeyCode.UNDEFINED)
			{
				return;
			}
			if (!(keyEvent.getTarget() instanceof MatrixTreeView))
			{
				return;
			}
			if (SettingsPanel.match(settings, keyEvent, SettingsPanel.BREAK_POINT))
			{
				breakPoint(matrix, treeView);
				keyEvent.consume();
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.ADD_BEFORE))
			{
				addBefore(treeView, matrix);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.ADD_AFTER))
			{
				addAfter(matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.ADD_CHILD))
			{
				addChild(treeView, matrix);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.DELETE_ITEM))
			{
				deleteCurrentItems(matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.COPY_ITEMS))
			{
				copyItems(matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.PASTE_ITEMS_AFTER))
			{
				pasteItems(PlaceToInsert.After, matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.PASTE_ITEMS_BEFORE))
			{
				pasteItems(PlaceToInsert.Before, matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.PASTE_ITEMS_CHILD))
			{
				pasteItems(PlaceToInsert.Child, matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.GO_TO_LINE))
			{
				gotoLine(treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.HELP))
			{
				showHelp(context, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.ADD_PARAMETER))
			{
				addParameter(matrix, treeView);
			}
		});
	}

	private ActionHelp showHelp(Context context, MatrixTreeView tree)
	{
		return new ActionHelp(context, tree);
	}

	private void addChild(MatrixTreeView tree, MatrixFx matrix)
	{
		Common.tryCatch(() -> matrix.insertNew(tree.currentItem(), PlaceToInsert.Child, Tokens.TempItem.get(), null), "Error on add child");
	}

	private void addBefore(MatrixTreeView treeView, MatrixFx matrix)
	{
		Common.tryCatch(() -> matrix.insertNew(treeView.currentItem(), PlaceToInsert.Before, Tokens.TempItem.get(), null), "Error on add before");
	}

	private void addAfter(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> matrix.insertNew(tree.currentItem(), PlaceToInsert.After, Tokens.TempItem.get(), null), "Error on add after");
	}

	private void breakPoint(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> matrix.breakPoint(tree.currentItems()), "Error on breakpoint");
	}

	private void deleteCurrentItems(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> matrix.remove(tree.currentItems()), "Error on delete item");
	}

	private void copyItems(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> matrix.copy(tree.currentItems()), "Error on copy");
	}

	private void pasteItems(PlaceToInsert placeToInsert, MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> matrix.paste(placeToInsert, tree.currentItem()), "Error on paste");
	}

	private void gotoLine(MatrixTreeView tree)
	{
		TextInputDialog dialog = new TextInputDialog();
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		dialog.getDialogPane().setHeader(new Pane());
		dialog.setTitle("Enter line number");
		dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.isEmpty())
			{
				if (!newValue.matches(Common.UINT_REGEXP))
				{
					dialog.getEditor().setText(oldValue);
				}
			}
		});
		Optional<String> string = dialog.showAndWait();
		if (string.isPresent())
		{
			try
			{
				int index = Integer.parseInt(string.get());

				TreeItem<MatrixItem> treeItem = tree.find(matrixItem -> matrixItem.getNumber() == index);
				if (treeItem == null)
				{
					DialogsHelper.showError(String.format("Matrix item with number '%d' not found", index));
				}
				else
				{
					tree.setCurrent(treeItem);
				}
			}
			catch (NumberFormatException e)
			{
				//
			}
		}
	}

	private void addParameter(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> {
			MatrixItem value = tree.getSelectionModel().getSelectedItem().getValue();
			matrix.parameterInsert(value, value.getParameters().size() - 1);
		}, "Error on add new parameter");
	}

	private Node createShortcut(String shortcutName, Settings settings)
	{
		String shortcut = SettingsPanel.getShortcutName(settings, shortcutName);
		Text text = new Text(shortcut);
		text.setOpacity(0.5);
		return text;
	}

	private class ActionHelp implements EventHandler<ActionEvent>
	{
		private MatrixTreeView tree;
		private Context context;

		public ActionHelp(Context context, MatrixTreeView tree)
		{
			this.tree = tree;
			this.context = context;
		}

		@Override
		public void handle(ActionEvent actionEvent)
		{
			Common.tryCatch(() -> {
				MatrixItem item = this.tree.currentItem();
				if (item != null)
				{
					ReportBuilder report = new ContextHelpFactory().createReportBuilder(null, null, new Date());

					item.documentationOnlyThis(this.context, report);

					WebView browser = new WebView();
					WebEngine engine = browser.getEngine();
					String str = report.getContent();
					engine.loadContent(str);

					Dialog<?> dialog = new Alert(Alert.AlertType.INFORMATION);
					dialog.getDialogPane().setContent(browser);
					dialog.getDialogPane().setPrefWidth(1024);
					dialog.getDialogPane().setPrefHeight(768);
					dialog.setResizable(true);
					dialog.setHeaderText("Help for " + item.getItemName());
					dialog.setTitle("HELP");
					dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
					dialog.show();
				}
			}, "Error on show result");
		}
	}
}
