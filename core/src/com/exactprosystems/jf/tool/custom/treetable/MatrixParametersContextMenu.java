/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.custom.treetable;


import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.grideditor.SpreadsheetGridView;
import com.exactprosystems.jf.tool.custom.grideditor.SpreadsheetView;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.params.ParameterGridPane;
import com.exactprosystems.jf.tool.matrix.params.ShowAllParams;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.fxmisc.richtext.StyledTextArea;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class MatrixParametersContextMenu extends MatrixContextMenu
{
	private MenuItem parRemove;
	private MenuItem parMoveLeft;
	private MenuItem parMoveRight;
	private MenuItem parAdd;
	private MenuItem parShowAll;

	private MatrixTreeRow row;
	private int index;

	public MatrixParametersContextMenu(Context context, MatrixFx matrix, MatrixTreeView tree, Settings settings)
	{
		super(context, matrix, tree, settings);
		super.setAutoHide(true);
		super.setHideOnEscape(true);
		
		this.parRemove = new MenuItem(R.MATRIX_PCM_REMOVE.get());
		this.parRemove.setGraphic(new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
		this.parRemove.setOnAction(event -> changeParameters(event, matrix::parameterRemove));

		this.parMoveLeft = new MenuItem(R.MATRIX_PCM_MOVE_LEFT.get());
		this.parMoveLeft.setGraphic(new ImageView(new Image(CssVariables.Icons.MOVE_LEFT_ICON)));
		this.parMoveLeft.setOnAction(event -> changeParameters(event, matrix::parameterMoveLeft));

		this.parMoveRight = new MenuItem(R.MATRIX_PCM_MOVE_RIGHT.get());
		this.parMoveRight.setGraphic(new ImageView(new Image(CssVariables.Icons.MOVE_RIGHT_ICON)));
		this.parMoveRight.setOnAction(event -> changeParameters(event, matrix::parameterMoveRight));

		this.parAdd = new MenuItem(R.MATRIX_PCM_ADD.get());
		this.parAdd.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
		this.parAdd.setOnAction(event -> changeParameters(event, matrix::parameterInsert));

		this.parShowAll = new MenuItem(R.MATRIX_PCM_ALL.get(),new ImageView(new Image(CssVariables.Icons.ALL_PARAMETERS_ICON)));
		this.parShowAll.setAccelerator(Common.getShortcut(settings, Settings.ALL_PARAMETERS));
		this.parShowAll.setOnAction(event -> allParameters(context, matrix, event));

		//TODO add icon
		MenuItem parameterWizard = new MenuItem(R.MATRIX_PCM_FOR.get());
		parameterWizard.setOnAction(event ->
		{
			WizardManager manager = context.getFactory().getWizardManager();
			Object[] criteries = new Object[] {
					matrix,
					tree.getSelectionModel().getSelectedItem().getValue(),
					row.getItem().getParameters().getByIndex(this.index)
			};
            
			List<Class<? extends Wizard>> suitable  = manager.suitableWizards(criteries);
			if (suitable.isEmpty())
			{
				DialogsHelper.showInfo(R.MATRIX_PCM_WIZARD_ERROR.get());
				return;
			}
			Class<? extends Wizard> wizardClass = DialogsHelper.selectFromList(R.MATRIX_PCM_CHOOSE_WIZARD.get(), null, suitable, manager::nameOf);
			manager.runWizard(wizardClass, context, criteries);
		});

		getItems().add(0, this.parRemove);
		getItems().add(1, this.parMoveLeft);
		getItems().add(2, this.parMoveRight);
		getItems().add(3, this.parAdd);
		getItems().add(4, this.parShowAll);
	}

	@Override
	public void initShortcuts(Settings settings, MatrixTreeView treeView, MatrixFx matrix, Context context)
	{
		super.initShortcuts(settings, treeView, matrix, context);
		treeView.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
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
			if (SettingsPanel.match(settings, keyEvent, Settings.ALL_PARAMETERS))
			{
				allParameters(context, matrix, keyEvent);
			}
		});
	}

	public EventHandler<ContextMenuEvent> createContextMenuHandler()
	{
		return (event ->
		{
			event.consume();
			this.row = matrixTreeRow(event);
			this.index = parameterIndex(event);
					
			if (this.row != null)
			{
				MatrixItem matrixItem = row.getItem();
				Parameters parameters = matrixItem.getParameters();
				boolean canMove = parameters.canMove(this.index);
				this.parMoveRight.setDisable(!canMove);
				this.parMoveLeft.setDisable(!canMove);
				this.parRemove.setDisable(!parameters.canRemove(this.index));
				this.parShowAll.setDisable(!(matrixItem instanceof ActionItem));
				this.parAdd.setDisable(!AbstractAction.additionFieldsAllow(matrixItem));

				super.addWizards(matrixItem.getMatrix(), matrixItem, this.index == -1 ? null : row.getItem().getParameters().getByIndex(this.index));

				Point location = MouseInfo.getPointerInfo().getLocation();
				super.show(this.row, location.getX(), location.getY());
			}
		});
	}

	//region private methods
	private void changeParameters(Event event, BiConsumer<MatrixItem, Integer> func)
	{
		Optional.ofNullable(this.row)
				.map(MatrixTreeRow::getItem)
				.ifPresent(item -> func.accept(item, this.index));
	}

	private void allParameters(Context context, MatrixFx matrix, Event event)
	{
		Common.tryCatch(() ->
		{
			MatrixItem item = getActionItem(event);
			if (item instanceof ActionItem)
			{
				ActionItem actionItem = (ActionItem) item;
				Map<ReadableValue, TypeMandatory> map = actionItem.helpToAddParameters(context);
				ShowAllParams params = new ShowAllParams(map, actionItem.getParameters(), actionItem.getItemName());
				ArrayList<Pair<ReadableValue, TypeMandatory>> result = params.show();
				matrix.parameterInsert(actionItem, this.index, result);
			}

		}, R.MATRIX_PCM_ERROR_SHOW_ALL.get());
	}

	private MatrixItem getActionItem(Event event)
	{
		if (this.row != null && !(event instanceof KeyEvent))
		{
			return this.row.getItem();
		}
		if (event.getSource() instanceof MatrixTreeView)
		{
			TreeItem<MatrixItem> selectedItem = ((MatrixTreeView) event.getSource()).getSelectionModel().getSelectedItem();
			if (selectedItem != null)
			{
				return selectedItem.getValue();
			}
		}
		return null;
	}

	private MatrixTreeRow matrixTreeRow(Event event)
	{
		Node parent = (Node)event.getSource();
		while (parent != null)
		{
			if (parent instanceof MatrixTreeRow)
			{
				return (MatrixTreeRow)parent;
			}
			
			parent = parent.getParent();
		}

		return null;
	}

	private int parameterIndex(Event event)
	{
		Node selectedPane = null;
		Node item = (Node)event.getSource();

		while (item != null)
		{
			if (item instanceof ParameterGridPane)
			{
				selectedPane = item;
				break;
			}
			item = item.getParent();
		}

		if (selectedPane == null)
		{
			return -1;
		}
		else
		{
			return ((GridPane)selectedPane.getParent()).getChildren().indexOf(selectedPane);
		}
	}
	//endregion
}
