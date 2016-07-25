////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;


import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.error.app.ProxyException;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.params.ParameterGridPane;
import com.exactprosystems.jf.tool.matrix.params.ShowAllParams;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

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
		
		this.parRemove = new MenuItem("Remove");
		this.parRemove.setGraphic(new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));
		this.parRemove.setOnAction(event -> changeParameters(event, matrix::parameterRemove));

		this.parMoveLeft = new MenuItem("Move to left");
		this.parMoveLeft.setGraphic(new ImageView(new Image(CssVariables.Icons.MOVE_LEFT_ICON)));
		this.parMoveLeft.setOnAction(event -> changeParameters(event, matrix::parameterMoveLeft));

		this.parMoveRight = new MenuItem("Move to right");
		this.parMoveRight.setGraphic(new ImageView(new Image(CssVariables.Icons.MOVE_RIGHT_ICON)));
		this.parMoveRight.setOnAction(event -> changeParameters(event, matrix::parameterMoveRight));

		this.parAdd = new MenuItem("Add param after");
		this.parAdd.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
		this.parAdd.setOnAction(event -> changeParameters(event, matrix::parameterInsert));


		this.parShowAll = new MenuItem("All parameters ...");
		this.parShowAll.setGraphic(new ImageView(new Image(CssVariables.Icons.ALL_PARAMETERS_ICON)));
		this.parShowAll.setOnAction(event -> Common.tryCatch(() ->
		{
			if (this.row != null && this.row.getItem() instanceof ActionItem)
			{
				ActionItem actionItem = (ActionItem) this.row.getItem();
				Map<ReadableValue, TypeMandatory> map = actionItem.helpToAddParameters(context);
				ShowAllParams params = new ShowAllParams(map, actionItem.getParameters(), actionItem.getItemName());
				ArrayList<Pair<ReadableValue, TypeMandatory>> result = params.show();
				matrix.parameterInsert(actionItem, this.index, result);
			}

		}, "Error on show all parameters"));

		getItems().add(0, this.parRemove);
		getItems().add(1, this.parMoveLeft);
		getItems().add(2, this.parMoveRight);
		getItems().add(3, this.parAdd);
		getItems().add(4, this.parShowAll);
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
				
				Point location = MouseInfo.getPointerInfo().getLocation();
				super.show(this.row, location.getX(), location.getY());
			}
		});
	}

	
	@FunctionalInterface
	public static interface Function
	{
		void call(MatrixItem item, int index) throws ProxyException, Exception;
	}

	private void changeParameters(Event event, Function func)
	{
		Common.tryCatch(() -> 
		{
			if (this.row != null)
			{
				func.call(this.row.getItem(), this.index);
			}
		}, "Error on change parameters");
	}


	protected MatrixTreeRow matrixTreeRow(Event event)
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

	protected int parameterIndex(Event event)
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


}
