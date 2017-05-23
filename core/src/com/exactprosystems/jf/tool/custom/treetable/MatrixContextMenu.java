////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;
import com.exactprosystems.jf.common.documentation.DocumentationBuilder;
import com.exactprosystems.jf.common.report.ContextHelpFactory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.End;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.grideditor.SpreadsheetGridView;
import com.exactprosystems.jf.tool.custom.grideditor.SpreadsheetView;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyledTextArea;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatrixContextMenu extends ContextMenu
{
	private boolean fold = false;

	private Menu menuWizard = new Menu("Wizard");
	private Context context;

	public MatrixContextMenu(Context context, MatrixFx matrix, MatrixTreeView tree, Settings settings)
	{
		super();
		this.context = context;

		SettingsValue foldSetting = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_FOLD_ITEMS, "false");
		this.fold = Boolean.parseBoolean(foldSetting.getValue());

		setAutoHide(true);

		MenuItem breakPoint = new MenuItem("Breakpoint", new ImageView(new Image(CssVariables.Icons.BREAK_POINT_ICON)));
		breakPoint.setAccelerator(Common.getShortcut(settings, Settings.BREAK_POINT));
		breakPoint.setOnAction(event -> breakPoint(matrix, tree));

		MenuItem addBefore = new MenuItem("Add item", new ImageView(new Image(CssVariables.Icons.ADD_BEFORE_ICON)));
		addBefore.setAccelerator(Common.getShortcut(settings, Settings.ADD_ITEMS));
		addBefore.setOnAction(event -> addBefore(tree, matrix));

		MenuItem deleteItem = new MenuItem("Delete", new ImageView(new Image(CssVariables.Icons.DELETE_ICON)));
		deleteItem.setAccelerator(Common.getShortcut(settings, Settings.DELETE_ITEM));
		deleteItem.setOnAction(event -> deleteCurrentItems(matrix, tree));

		MenuItem copy = new MenuItem("Copy", new ImageView(new Image(CssVariables.Icons.COPY_ICON)));
		copy.setAccelerator(Common.getShortcut(settings, Settings.COPY_ITEMS));
		copy.setOnAction(event -> copyItems(matrix, tree));

		MenuItem pasteBefore = new MenuItem("Paste", new ImageView(new Image(CssVariables.Icons.PASTE_ICON)));
		pasteBefore.setAccelerator(Common.getShortcut(settings, Settings.PASTE_ITEMS));
		pasteBefore.setOnAction(event -> pasteItems(matrix, tree));

		MenuItem gotoItem = new MenuItem("Go to line ...", new ImageView(new Image(CssVariables.Icons.GO_TO_LINE_ICON)));
		gotoItem.setAccelerator(Common.getShortcut(settings, Settings.GO_TO_LINE));
		gotoItem.setOnAction(event -> gotoLine(tree));

		MenuItem help = new MenuItem("Help", new ImageView(new Image(CssVariables.Icons.HELP_ICON)));
		help.setAccelerator(Common.getShortcut(settings, Settings.HELP));
		help.setOnAction(showHelp(context, tree));

		MenuItem parAdd = new MenuItem("Add param to end", new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
		parAdd.setAccelerator(Common.getShortcut(settings, Settings.ADD_PARAMETER));
		parAdd.setOnAction(event -> addParameter(matrix, tree));

		getItems().addAll(breakPoint, new SeparatorMenuItem(), parAdd, new SeparatorMenuItem(), copy, pasteBefore, new SeparatorMenuItem(), addBefore,
				deleteItem, gotoItem, new SeparatorMenuItem(), menuWizard, new SeparatorMenuItem(), help);
		this.setOnShown(event ->
		{
			TreeItem<MatrixItem> selectedItem = tree.getSelectionModel().getSelectedItem();
			if (selectedItem != null)
			{
				boolean b = selectedItem.getValue() instanceof End;
				breakPoint.setDisable(b);
				parAdd.setDisable(b);
				copy.setDisable(b);
				deleteItem.setDisable(b);
				help.setDisable(b);
				parAdd.setDisable(!AbstractAction.additionFieldsAllow(selectedItem.getValue()));
				menuWizard.setDisable(b);
				this.addWizards(matrix, tree.getSelectionModel().getSelectedItem().getValue());
			}
		});
	}

	public void initShortcuts(Settings settings, MatrixTreeView treeView, MatrixFx matrix, Context context)
	{
		treeView.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.UNDEFINED)
			{
				return;
			}
			//check that control placed inside matrixTreeView
			EventTarget parent = keyEvent.getTarget();
			if (!(parent instanceof Node))
			{
				return;
			}
			if (parent instanceof TextInputControl || parent instanceof SpreadsheetView || parent instanceof SpreadsheetGridView || parent instanceof StyledTextArea)
			{
				return;
			}
			boolean inside = parent instanceof MatrixTreeView;
			while (!inside && parent != null)
			{
				parent = ((Node) parent).getParent();
				inside = parent instanceof MatrixTreeView;
			}
			if (!inside)
			{
				return;
			}
			if (SettingsPanel.match(settings, keyEvent, Settings.BREAK_POINT))
			{
				breakPoint(matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.ADD_ITEMS))
			{
				addBefore(treeView, matrix);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.DELETE_ITEM))
			{
				deleteCurrentItems(matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.COPY_ITEMS))
			{
				copyItems(matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.PASTE_ITEMS))
			{
				pasteItems(matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.GO_TO_LINE))
			{
				gotoLine(treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.HELP))
			{
				showHelp(context, treeView).handle(null);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.ADD_PARAMETER))
			{
				addParameter(matrix, treeView);
			}
		});
	}

	protected void addWizards(Object ... criteries)
	{
		WizardManager manager = context.getFactory().getWizardManager();
		java.util.List<Class<? extends Wizard>> suitableWizards = manager.suitableWizards(criteries);
		menuWizard.getItems().clear();
		menuWizard.getItems().addAll(suitableWizards.stream()
				.map(wizardClass -> {
					MenuItem menuItem = new MenuItem(manager.nameOf(wizardClass));
					menuItem.setOnAction(e -> manager.runWizard(wizardClass, context, criteries));
					return menuItem;
				})
				.collect(Collectors.toList())
		);
	}

	private ActionHelp showHelp(Context context, MatrixTreeView tree)
	{
		return new ActionHelp(context, tree);
	}

	private void addBefore(MatrixTreeView treeView, MatrixFx matrix)
	{
		Common.tryCatch(() ->
		{
			MatrixItem item = treeView.currentItem();
			matrix.insertNew(item, Tokens.TempItem.get(), null);
		}, "Error on add before");
	}

	private void breakPoint(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> matrix.breakPoint(tree.currentItems().stream().filter(item -> !item.getClass().equals(End.class)).collect(Collectors.toList())), "Error on breakpoint");
	}

	private void deleteCurrentItems(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> matrix.remove(tree.currentItems().stream().filter(item -> !item.getClass().equals(End.class)).collect(Collectors.toList())), "Error on delete item");
	}

	private void copyItems(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> matrix.copy(tree.currentItems()), "Error on copy");
	}

	private void pasteItems(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() ->
		{
			MatrixItem item = tree.currentItem();
			MatrixItem[] inserted = matrix.paste(item);
			Platform.runLater(() -> Arrays.stream(inserted).map(tree::find).forEach(treeItem -> tree.expand(treeItem, !this.fold)));
		}, "Error on paste");
	}

	private void gotoLine(MatrixTreeView tree)
	{
		TextInputDialog dialog = new TextInputDialog();
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		dialog.getDialogPane().setHeader(new Pane());
		dialog.setTitle("Enter line number");
		dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) ->
		{
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
					tree.setCurrent(treeItem, false);
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
		Common.tryCatch(() ->
		{
			MatrixItem value = tree.getSelectionModel().getSelectedItem().getValue();
			if (!(value instanceof End))
			{
				matrix.parameterInsert(value, value.getParameters().size() - 1);
			}
		}, "Error on add new parameter");
	}

	private class ActionHelp implements EventHandler<ActionEvent>
	{
		private MatrixTreeView tree;
		private Context        context;

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
				if (item != null && !(item instanceof End))
				{
					ReportBuilder report = new ContextHelpFactory().createReportBuilder(null, null, new Date());
					MatrixItem help = DocumentationBuilder.createHelpForItem(report, context, item);
					report.reportStarted(null, "");
					help.execute(context, context.getMatrixListener(), context.getEvaluator(), report);
					report.reportFinished(0, 0, null, null);

					WebView browser = new WebView();
					WebEngine engine = browser.getEngine();
					String str = report.getContent();
					engine.loadContent(str);

					Dialog<?> dialog = new Alert(Alert.AlertType.INFORMATION);
					Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
					dialog.getDialogPane().setContent(browser);
					dialog.getDialogPane().setPrefWidth(1024);
					dialog.getDialogPane().setPrefHeight(768);
					dialog.setResizable(true);
					dialog.getDialogPane().setHeader(new Label());
					dialog.setTitle("Help for " + item.getItemName());
					dialog.setHeaderText(null);
					dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
					dialog.show();
				}
			}, "Error on show result");
		}
	}
}
