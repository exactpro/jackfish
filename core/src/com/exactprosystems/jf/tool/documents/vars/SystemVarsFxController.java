////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents.vars;

import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.table.CustomTable;
import com.exactprosystems.jf.tool.documents.AbstractDocumentController;
import com.exactprosystems.jf.tool.documents.ControllerInfo;
import javafx.collections.FXCollections;
import javafx.scene.control.TableRow;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.tryCatch;
import static com.exactprosystems.jf.tool.custom.table.CustomTable.*;


@ControllerInfo(resourceName = "SystemVarsFx.fxml")
public class SystemVarsFxController extends AbstractDocumentController<SystemVarsFx>
{
	public GridPane               grid;
	public CustomTable<Parameter> tableView;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		super.initialize(location, resources);
		this.tableView = new CustomTable<>(true);
		this.grid.add(this.tableView, 0, 0);
	}

	@Override
	public void init(Document model, CustomTab customTab)
	{
		super.init(model, customTab);
		this.tableView.setAddListener(() -> tryCatch(SystemVarsFxController.this.model::addNewVariable, "Error on adding new var"));
		this.tableView.setDeleteListener(this.model::removeParameters);
		createTable();

		this.model.getParameters().setOnAddListener((o, n) -> updateVars());
		this.model.getParameters().setOnRemoveListener((o, n) -> updateVars());
		this.model.getParameters().setOnChangeListener((o, n) -> updateVars());
	}

	private void updateVars()
	{
		SystemVarsFxController.this.tableView.setItems(FXCollections.observableList(SystemVarsFxController.this.model.getParameters()));
		SystemVarsFxController.this.tableView.update();
	}

	private void createTable()
	{
		this.tableView.completeFirstColumn("Name", "name", EditState.TEXTFIELD, false);
		this.tableView.completeSecondColumn("Expression", "expression", EditState.TEXTFIELD, false);
		this.tableView.completeThirdColumn("Value", "valueAsString", EditState.TEXTFIELD_READONLY, true);
		this.tableView.completeFourthColumn("Description", "description", EditState.TEXTFIELD, true);
		this.tableView.onFinishEditFirstColumn((par, value) -> this.model.updateNameRow(current(), value));
		this.tableView.onFinishEditSecondColumn((par, value) -> this.model.updateExpressionRow(current(), value));
		this.tableView.onFinishEditFourthColumn((par, value) -> this.model.updateDescriptionRow(current(), value));
		this.tableView.setRowFactory(v -> new ColorRow());
		this.tableView.setSortable(false);
	}

	private int current()
	{
		return this.tableView.getSelectionModel().getSelectedIndex();
	}

	private class ColorRow extends TableRow<Parameter>
	{
		@Override
		protected void updateItem(Parameter vars, boolean b)
		{
			super.updateItem(vars, b);
			this.getStyleClass().remove(CssVariables.CORRECT_ROW);
			this.getStyleClass().remove(CssVariables.INCORRECT_ROW);
			if (vars == null)
			{
				return;
			}
			if (!vars.isValid())
			{
				this.getStyleClass().add(CssVariables.INCORRECT_ROW);
			}
			else
			{
				this.getStyleClass().add(CssVariables.CORRECT_ROW);
			}
		}

		@Override
		public void cancelEdit()
		{
			super.cancelEdit();
			Parameter item = getItem();
			if (!item.isValid())
			{
				this.getStyleClass().remove(CssVariables.CORRECT_ROW);
				this.getStyleClass().add(CssVariables.INCORRECT_ROW);
			}
			else
			{
				this.getStyleClass().remove(CssVariables.INCORRECT_ROW);
				this.getStyleClass().add(CssVariables.CORRECT_ROW);
			}
			tableView.update();
		}

		public ColorRow()
		{

		}
	}

}
