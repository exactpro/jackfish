////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.documents.matrix.parser.items.End;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.matrix.params.ParametersPane;
import com.sun.javafx.css.PseudoClassState;
import javafx.css.PseudoClass;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeTableRow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MatrixTreeRow extends TreeTableRow<MatrixItem>
{
	private static final PseudoClass customSelected = PseudoClassState.getPseudoClass("customSelectedState");
	private static final PseudoClass selected = PseudoClassState.getPseudoClass("selected");

	public MatrixTreeRow(ContextMenu contextMenu)
	{
		setContextMenu(contextMenu);
		this.selectedProperty().addListener((observable, oldValue, newValue) -> {
			this.pseudoClassStateChanged(customSelected, newValue);
			this.pseudoClassStateChanged(selected, false); // remove selected pseudostate, cause this state change text color
		});
	}

	@Override
	protected void updateItem(MatrixItem item, boolean empty)
	{
		super.updateItem(item, empty);
		this.getStyleClass().removeAll(CssVariables.SIMPLE_ITEM, CssVariables.ITEM_OFF_TRUE);
		if (item != null)
		{
			if (item instanceof End)
			{
				this.setPrefHeight(18);
				this.setMaxHeight(18);
				this.setMinHeight(18);
				((GridPane)item.getLayout()).setPrefHeight(10);
				((GridPane)item.getLayout()).setMaxHeight(10);
				((GridPane)item.getLayout()).setMinHeight(10);
			}
			else
			{
				this.setPrefHeight(-1);
				this.setMaxHeight(-1);
				this.setMinHeight(-1);
			}
			this.getStyleClass().add(CssVariables.SIMPLE_ITEM);
			if (!item.canExecute())
			{
				this.getStyleClass().add(CssVariables.ITEM_OFF_TRUE);
			}
		}
	}

	public void showExpressionsResults()
	{
		List<ExpressionField> list = new ArrayList<>();
		find((GridPane) this.getTreeTableView().getSelectionModel().getSelectedItem().getValue().getLayout(), list);
		list.forEach(ExpressionField::showShadowText);
	}

	public void hideExpressionsResults()
	{
		List<ExpressionField> list = new ArrayList<>();
		find((GridPane) this.getTreeTableView().getSelectionModel().getSelectedItem().getValue().getLayout(), list);
		list.forEach(ExpressionField::hideShadowText);
	}

	private void find(Pane parent, List<ExpressionField> fields)
	{
		fields.addAll(parent.getChildren().stream().filter(n -> n instanceof ExpressionField).map(n -> ((ExpressionField) n)).collect(Collectors.toList()));
		parent.getChildren().stream().filter(n -> n instanceof Pane).forEach(pane -> find(((Pane) pane), fields));
		parent.getChildren().stream().filter(n -> n instanceof ParametersPane).map(n -> ((GridPane) ((ParametersPane) n).getContent())).forEach(gp -> find(gp, fields));
	}

}