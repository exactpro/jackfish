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

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.api.common.i18n.R;
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
import com.exactprosystems.jf.tool.settings.Theme;
import com.exactprosystems.jf.tool.wizard.WizardButton;
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
import org.fxmisc.richtext.StyledTextArea;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatrixContextMenu extends ContextMenu
{
	private boolean fold = false;

	private Menu menuWizard = WizardButton.createMenu();
	private Context context;

	public MatrixContextMenu(Context context, MatrixFx matrix, MatrixTreeView tree, Settings settings)
	{
		super();
		this.context = context;

		SettingsValue foldSetting = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_FOLD_ITEMS);
		this.fold = Boolean.parseBoolean(foldSetting.getValue());

		setAutoHide(true);

		MenuItem breakPoint = new MenuItem(R.MATRIX_CM_BREAKPOINT.get(), new ImageView(new Image(CssVariables.Icons.BREAK_POINT_ICON)));
		breakPoint.setAccelerator(Common.getShortcut(settings, Settings.BREAK_POINT));
		breakPoint.setOnAction(event -> breakPoint(matrix, tree));

		MenuItem addItem = new MenuItem(R.MATRIX_CM_ADD_ITEM.get(), new ImageView(new Image(CssVariables.Icons.ADD_BEFORE_ICON)));
		addItem.setAccelerator(Common.getShortcut(settings, Settings.ADD_ITEMS));
		addItem.setOnAction(event -> addItem(tree, matrix));

		MenuItem deleteItem = new MenuItem(R.MATRIX_CM_DELETE.get(), new ImageView(new Image(CssVariables.Icons.DELETE_ICON)));
		deleteItem.setAccelerator(Common.getShortcut(settings, Settings.DELETE_ITEM));
		deleteItem.setOnAction(event -> deleteCurrentItems(matrix, tree));

		MenuItem copy = new MenuItem(R.MATRIX_CM_COPY.get(), new ImageView(new Image(CssVariables.Icons.COPY_ICON)));
		copy.setAccelerator(Common.getShortcut(settings, Settings.COPY_ITEMS));
		copy.setOnAction(event -> copyItems(matrix, tree));

		MenuItem cut = new MenuItem(R.MATRIX_CM_CUT.get(), new ImageView(new Image(CssVariables.Icons.CUT_ICON)));
		cut.setAccelerator(Common.getShortcut(settings, Settings.CUT_ITEMS));
		cut.setOnAction(event -> cutItems(matrix, tree));

		MenuItem pasteBefore = new MenuItem(R.MATRIX_CM_PASTE.get(), new ImageView(new Image(CssVariables.Icons.PASTE_ICON)));
		pasteBefore.setAccelerator(Common.getShortcut(settings, Settings.PASTE_ITEMS));
		pasteBefore.setOnAction(event -> pasteItems(matrix, tree));

		MenuItem gotoItem = new MenuItem(R.MATRIX_CM_GO_TO_LINE.get(), new ImageView(new Image(CssVariables.Icons.GO_TO_LINE_ICON)));
		gotoItem.setAccelerator(Common.getShortcut(settings, Settings.GO_TO_LINE));
		gotoItem.setOnAction(event -> gotoLine(tree));

		MenuItem help = new MenuItem(R.MATRIX_CM_HELP.get(), new ImageView(new Image(CssVariables.Icons.HELP_ICON)));
		help.setAccelerator(Common.getShortcut(settings, Settings.HELP));
		help.setOnAction(showHelp(context, tree));

		MenuItem addParameterToEnd = new MenuItem(R.MATRIX_CM_ADD_PARAM.get(), new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
		addParameterToEnd.setAccelerator(Common.getShortcut(settings, Settings.ADD_PARAMETER));
		addParameterToEnd.setOnAction(event -> addParameter(matrix, tree));

		getItems().addAll(
				breakPoint,
				new SeparatorMenuItem(),
				addParameterToEnd,
				new SeparatorMenuItem(),
				copy,
				cut,
				pasteBefore,
				new SeparatorMenuItem(),
				addItem,
				deleteItem,
				gotoItem,
				new SeparatorMenuItem(),
				this.menuWizard,
				new SeparatorMenuItem(), help);
		this.setOnShown(event ->
		{
			TreeItem<MatrixItem> selectedItem = tree.getSelectionModel().getSelectedItem();
			if (selectedItem != null)
			{
				boolean b = selectedItem.getValue() instanceof End;
				breakPoint.setDisable(b);
				addParameterToEnd.setDisable(b);
				copy.setDisable(b);
				deleteItem.setDisable(b);
				help.setDisable(b);
				addParameterToEnd.setDisable(!AbstractAction.additionFieldsAllow(selectedItem.getValue()));
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
				addItem(treeView, matrix);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.DELETE_ITEM))
			{
				deleteCurrentItems(matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.COPY_ITEMS))
			{
				copyItems(matrix, treeView);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.CUT_ITEMS))
			{
				cutItems(matrix, treeView);
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

	void addWizards(Object... criteries)
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

	//region private methods
	private void addItem(MatrixTreeView treeView, MatrixFx matrix)
	{
		Common.tryCatch(() ->
		{
			MatrixItem item = treeView.currentItem();
			matrix.insertNew(item, Tokens.TempItem.get(), null);
		}, R.MATRIX_CM_ERROR_ON_ADD.get());
	}

	private void breakPoint(MatrixFx matrix, MatrixTreeView tree)
	{
		matrix.breakPoint(tree.currentItems()
				.stream()
				.filter(item -> !item.getClass().equals(End.class))
				.collect(Collectors.toList())
		);
	}

	private void deleteCurrentItems(MatrixFx matrix, MatrixTreeView tree)
	{
		matrix.remove(tree.currentItems()
				.stream()
				.filter(item -> !item.getClass().equals(End.class))
				.collect(Collectors.toList())
		);
	}

	private void copyItems(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() -> matrix.copy(tree.currentItems()), R.MATRIX_CM_ERROR_ON_COPY.get());
	}

	private void cutItems(MatrixFx matrix, MatrixTreeView tree)
	{
		this.copyItems(matrix, tree);
		this.deleteCurrentItems(matrix, tree);
	}

	private void pasteItems(MatrixFx matrix, MatrixTreeView tree)
	{
		Common.tryCatch(() ->
		{
			MatrixItem item = tree.currentItem();
			MatrixItem[] inserted = matrix.paste(item);
			Common.runLater(() -> Arrays.stream(inserted).map(tree::find).forEach(treeItem -> tree.expand(treeItem, !this.fold)));
		}, R.MATRIX_CM_ERROR_ON_PASTE.get());
	}

	private void gotoLine(MatrixTreeView tree)
	{
		TextInputDialog dialog = new TextInputDialog();
		DialogsHelper.centreDialog(dialog);
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		dialog.getDialogPane().setHeader(new Pane());
		dialog.setTitle(R.MATRIX_CM_ENTER_NUMBER.get());
		dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue.isEmpty() && !newValue.matches(Common.UINT_REGEXP))
			{
				dialog.getEditor().setText(oldValue);
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
					DialogsHelper.showError(String.format(R.MATRIX_CM_ERROR_ON_MATRIX_NUMBER.get(), index));
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
		MatrixItem value = tree.getSelectionModel().getSelectedItem().getValue();
		if (!(value instanceof End))
		{
			matrix.parameterInsert(value, value.getParameters().size() - 1);
		}
	}
	//endregion

	private static class ActionHelp implements EventHandler<ActionEvent>
	{
		private MatrixTreeView tree;
		private Context        context;

		ActionHelp(Context context, MatrixTreeView tree)
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
					DialogsHelper.showHelpDialog(context, item.getClass().getSimpleName(), report, help);
				}
			}, R.MATRIX_CM_ERROR_ON_SHOW_HELP.get());
		}
	}
}
