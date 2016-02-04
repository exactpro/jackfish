////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.parser.items.ActionItem;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.matrix.params.ParametersPane;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeTableRow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatrixTreeRow extends TreeTableRow<MatrixItem>
{
	private Settings settings;

	public MatrixTreeRow(ContextMenu contextMenu, Settings settings) throws Exception
	{
		setContextMenu(contextMenu);
		this.settings = settings;
	}

	@Override
	protected void updateItem(MatrixItem item, boolean empty)
	{
		super.updateItem(item, empty);
		this.setStyle(null);
		this.getStyleClass().removeAll(CssVariables.SIMPLE_ITEM, CssVariables.ITEM_OFF_TRUE);
		if (item != null)
		{
			this.getStyleClass().add(CssVariables.SIMPLE_ITEM);
			if (!item.canExecute())
			{
				this.getStyleClass().add(CssVariables.ITEM_OFF_TRUE);
			}
			else
			{
				String key;
				if (item instanceof ActionItem)
				{
					key = ((ActionItem) item).getActionName();
				}
				else
				{
					key = item.getClass().getSimpleName();
				}
				Settings.SettingsValue value = settings.getValue(Settings.GLOBAL_NS, SettingsPanel.MATRIX_COLORS, key);
				Optional.ofNullable(value).ifPresent(v -> this.setStyle("-fx-background-color : " + v.getValue()));
			}
		}
	}

	public void showExpressionsResults()
	{
		List<ExpressionField> list = new ArrayList<>();
		find((GridPane) this.getTreeTableView().getSelectionModel().getSelectedItem().getValue().getLayout(), list);
		list.stream().forEach(ExpressionField::showShadowText);
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