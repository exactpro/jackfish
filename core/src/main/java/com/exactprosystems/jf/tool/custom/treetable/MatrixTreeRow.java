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
		this.getStyleClass().removeAll(CssVariables.ITEM_PASSED, CssVariables.ITEM_FAILED, CssVariables.ITEM_EXECUTING);
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
			switch (item.getExecutingState())
			{
				case Failed: this.getStyleClass().add(CssVariables.ITEM_FAILED); break;
				case Passed: this.getStyleClass().add(CssVariables.ITEM_PASSED); break;
				case Executing: this.getStyleClass().add(CssVariables.ITEM_EXECUTING); break;
				case None : break;
			}
			this.getStyleClass().add(CssVariables.SIMPLE_ITEM);
			if (!item.canExecute())
			{
				this.getStyleClass().add(CssVariables.ITEM_OFF_TRUE);
			}
		}
	}

	void showExpressionsResults()
	{
		List<ExpressionField> list = new ArrayList<>();
		find((GridPane) this.getTreeTableView().getSelectionModel().getSelectedItem().getValue().getLayout(), list);
		list.forEach(ExpressionField::showShadowText);
	}

	void hideExpressionsResults()
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