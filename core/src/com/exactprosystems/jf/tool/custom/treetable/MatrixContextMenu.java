////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.help.ActionsList;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.parser.Tokens;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.report.ContextHelpFactory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.xml.control.Table;
import com.exactprosystems.jf.functions.Text;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.MatrixFx.PlaceToInsert;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;

import java.util.*;

public class MatrixContextMenu extends ContextMenu
{
	private ContextMenu addBeforeMenu;
	private ContextMenu addAfterMenu;
	private ContextMenu addChildMenu;
	
	public MatrixContextMenu(Context context, MatrixFx matrix, MatrixTreeView tree, Settings settings)
	{
		super();
		
		setAutoHide(true);

		this.addBeforeMenu = createInsertMenu(matrix, tree, PlaceToInsert.Before);
		this.addAfterMenu = createInsertMenu(matrix, tree, PlaceToInsert.After);
		this.addChildMenu = createInsertMenu(matrix, tree, PlaceToInsert.Child);
		
		MenuItem breakPoint = new MenuItem("Breakpoint");
		breakPoint.setGraphic(new ImageView(new Image(CssVariables.Icons.BREAK_POINT_ICON)));
		breakPoint.setAccelerator(SettingsPanel.shortCut(settings, SettingsPanel.BREAK_POINT));
		breakPoint.setOnAction(event -> breakPoint(matrix, tree));

		MenuItem addBefore = new MenuItem("Add before >>");
		addBefore.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_BEFORE_ICON)));
		addBefore.setAccelerator(SettingsPanel.shortCut(settings, SettingsPanel.ADD_BEFORE));
		addBefore.setOnAction(event -> Common.tryCatch(() -> showAddMenu(this.addBeforeMenu), "Error on add before"));

		MenuItem addAfter = new MenuItem("Add after >>");
		addAfter.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_AFTER_ICON)));
		addAfter.setAccelerator(SettingsPanel.shortCut(settings, SettingsPanel.ADD_AFTER));
		addAfter.setOnAction(event -> Common.tryCatch(() -> showAddMenu(this.addAfterMenu), "Error on add after"));

		MenuItem addChild = new MenuItem("Add child >>");
		addChild.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_CHILD_ICON)));
		addChild.setAccelerator(SettingsPanel.shortCut(settings, SettingsPanel.ADD_CHILD));
		addChild.setOnAction(event -> Common.tryCatch(() -> showAddMenu(this.addChildMenu), "Error on add child"));

		MenuItem deleteItem = new MenuItem("Delete");
		deleteItem.setGraphic(new ImageView(new Image(CssVariables.Icons.DELETE_ICON)));
		deleteItem.setAccelerator(SettingsPanel.shortCut(settings, SettingsPanel.DELETE_ITEM));
		deleteItem.setOnAction(event -> deleteCurrentItems(matrix, tree));

		MenuItem copy = new MenuItem("Copy");
		copy.setGraphic(new ImageView(new Image(CssVariables.Icons.COPY_ICON)));
		copy.setOnAction(event -> copyItems(matrix, tree));
		
		MenuItem paste = new MenuItem("Paste");
		paste.setGraphic(new ImageView(new Image(CssVariables.Icons.PASTE_ICON)));
		paste.setOnAction(event -> pasteItems(matrix, tree));

		MenuItem gotoItem = new MenuItem("Go to line ...");
		gotoItem.setGraphic(new ImageView(new Image(CssVariables.Icons.GO_TO_LINE_ICON)));
		gotoItem.setAccelerator(SettingsPanel.shortCut(settings, SettingsPanel.GO_TO_LINE));
		gotoItem.setOnAction(event -> gotoLine(tree));

		MenuItem help = new MenuItem("Help");
		help.setGraphic(new ImageView(new Image(CssVariables.Icons.HELP_ICON)));
		help.setAccelerator(SettingsPanel.shortCut(settings, SettingsPanel.HELP));
		help.setOnAction(new ActionHelp(context, tree));

		getItems().addAll(
				breakPoint,
				new SeparatorMenuItem(),
				copy,
				paste,
				new SeparatorMenuItem(),
				addAfter,
				addChild,
				addBefore,
				deleteItem,
				gotoItem,
				new SeparatorMenuItem(),
				help
			);
	}
	
	private ContextMenu createInsertMenu(MatrixFx matrix, MatrixTreeView tree, PlaceToInsert placeToInsert)
	{
		ContextMenu insertMenu = new ContextMenu();
		insertMenu.setHideOnEscape(true);
		insertMenu.setAutoHide(true);

		Menu actionItemMenu = new Menu("ActionItem");
		
		Map<ActionGroups, Menu> map = new HashMap<>();
		Arrays.asList(ActionGroups.values()).stream().forEach(group ->
		{
			Menu menu = new Menu(group.name());
			actionItemMenu.getItems().add(menu);
			map.put(group, menu);
		});
		
		Arrays.asList(ActionsList.actions).forEach(clazz ->
		{
			MenuItem menuItem = new MenuItem(clazz.getSimpleName());
			menuItem.setOnAction(event -> Common.tryCatch(() -> 
			{
				MatrixItem item = tree.currentItem();
				if (item != null)
				{
					matrix.insertNew(item, placeToInsert, Tokens.Action.get(), clazz.getSimpleName());
					if (placeToInsert.equals(PlaceToInsert.Child))
					{
						tree.find(item).setExpanded(true);
					}
				}
			}, "Error on create item"));
			ActionGroups aClassGroup = clazz.getAnnotation(ActionAttribute.class).group();
			map.get(aClassGroup).getItems().add(menuItem);
		});
		
		Menu dataItemMenu = new Menu("Raw data");
		MenuItem insertDataTable = new MenuItem(Table.class.getSimpleName());
		insertDataTable.setOnAction(event -> Common.tryCatch(() -> 
		{
			MatrixItem item = tree.currentItem();
			if (item != null)
			{
				matrix.insertNew(item, placeToInsert, Tokens.RawTable.get(), Table.class.getSimpleName());
				if (placeToInsert.equals(PlaceToInsert.Child))
				{
					tree.find(item).setExpanded(true);
				}
			}
		}, "Error on insert") );
		
		MenuItem insertDataMessage = new MenuItem(MapMessage.class.getSimpleName());
		insertDataMessage.setOnAction(event -> Common.tryCatch(() -> 
		{
			MatrixItem item = tree.currentItem();
			if (item != null)
			{
				matrix.insertNew(item, placeToInsert, Tokens.RawMessage.get(), "none");
				if (placeToInsert.equals(PlaceToInsert.Child))
				{
					tree.find(item).setExpanded(true);
				}
			}
		}, "Error on insert") );
		
		MenuItem insertDataText = new MenuItem(Text.class.getSimpleName());
		insertDataText.setOnAction(event -> Common.tryCatch(() -> 
		{
			MatrixItem item = tree.currentItem();
			if (item != null)
			{
				matrix.insertNew(item, placeToInsert, Tokens.RawText.get(), null);
				if (placeToInsert.equals(PlaceToInsert.Child))
				{
					tree.find(item).setExpanded(true);
				}
			}
		}, "Error on insert") );
		
		dataItemMenu.getItems().addAll(
				insertDataTable,
				insertDataMessage,
				insertDataText
			);
		
		insertMenu.getItems().addAll(
				actionItemMenu, 
				dataItemMenu,
				new MenuItem(Tokens.TestCase.get()), 
				new MenuItem(Tokens.SubCase.get()), 
				new MenuItem(Tokens.Return.get()), 
				new MenuItem(Tokens.Call.get()), 
				new MenuItem(Tokens.If.get()), 
				new MenuItem(Tokens.Else.get()), 
				new MenuItem(Tokens.For.get()), 
				new MenuItem(Tokens.ForEach.get()), 
				new MenuItem(Tokens.While.get()), 
				new MenuItem(Tokens.Continue.get()), 
				new MenuItem(Tokens.Break.get()), 
				new MenuItem(Tokens.OnError.get()), 
				new MenuItem(Tokens.Switch.get()), 
				new MenuItem(Tokens.Case.get()), 
				new MenuItem(Tokens.Default.get()), 
				new MenuItem(Tokens.ReportOn.get()), 
				new MenuItem(Tokens.ReportOff.get()), 
				new MenuItem(Tokens.Fail.get())
			);

		insertMenu.getItems()
			.stream()
			.filter(menuItem -> !(menuItem instanceof Menu))
			.forEach(menuItem -> menuItem.setOnAction(event -> Common.tryCatch(() -> 
			{
				MatrixItem item = tree.currentItem();
				if (item != null)
				{
					matrix.insertNew(item, placeToInsert, menuItem.getText(), null);
					if (placeToInsert.equals(PlaceToInsert.Child))
					{
						tree.find(item).setExpanded(true);
					}
				}
			}, "Error on add matrix item")));

		return insertMenu;
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

	private void pasteItems(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> matrix.paste(tree.currentItem()), "Error on paste");
	}

	private void gotoLine(MatrixTreeView tree)
	{
		TextInputDialog dialog = new TextInputDialog();
		dialog.getDialogPane().getStylesheets()
				.add(Common.currentTheme().getPath());
		dialog.getDialogPane().setHeader(new Pane());
		dialog.setTitle("Enter line number");
		dialog.getEditor()
				.textProperty()
				.addListener(
						(observable, oldValue, newValue) ->
						{
							if (!newValue.isEmpty())
							{
								if (!newValue.matches(Common.intPositiveNumberMatcher))
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
				int index =  Integer.parseInt(string.get());
			
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
	
	private void showAddMenu(ContextMenu menu)
	{
		Window window = Common.getTabPane().getScene().getWindow();
		final double x = window.getX() + window.getWidth() / 2;
		final double y = window.getY() + window.getHeight() / 2;
		menu.show(Common.node, x, y);
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
			Common.tryCatch(() ->
			{
				MatrixItem item = this.tree.currentItem();
				if (item != null)
				{
					ReportBuilder report = new ContextHelpFactory().createBuilder(null, null, new Date());

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
					dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
					dialog.show();
				}
			}, "Error on show result");
		}
	}
}
