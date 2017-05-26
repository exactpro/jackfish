////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.systemvars;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import com.exactprosystems.jf.tool.custom.table.CustomTable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class SystemVarsFxController implements Initializable, ContainingParent
{
	public GridPane grid;
	public CustomTable<Parameter> tableView;

	private Parent pane;
	private SystemVarsFx model;
	private CustomTab tab;


	//----------------------------------------------------------------------------------------------
	// Event handlers
	//----------------------------------------------------------------------------------------------
	public void addNewVar(ActionEvent event)
	{
		tryCatch(this.model::addNewVariable, "Error on adding new var");
	}


	//----------------------------------------------------------------------------------------------
	// Interface Initializable
	//----------------------------------------------------------------------------------------------
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.tableView = new CustomTable<>(true);
		MenuItem itemAdd = new MenuItem("Add new variable");
		itemAdd.setOnAction(this::addNewVar);
		this.tableView.getContextMenu().getItems().add(0,itemAdd);
		this.grid.add(this.tableView, 0, 0);
	}

	//----------------------------------------------------------------------------------------------
	// Interface ContainingParent
	//----------------------------------------------------------------------------------------------
	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	//----------------------------------------------------------------------------------------------
	// Public methods
	//----------------------------------------------------------------------------------------------
	public void init(SystemVarsFx model, Settings settings)
	{
		this.model = model;
		this.tab = CustomTabPane.getInstance().createTab(model);
		this.tab.setContent(this.pane);
		this.tableView.setListener(this.model::removeParameters);
		createTable();
		CustomTabPane.getInstance().addTab(this.tab);
		CustomTabPane.getInstance().selectTab(this.tab);
	}
	
	public void saved(String name)
	{
		this.tab.saved(name);
	}

	public void close() throws Exception
	{
		this.tab.close();
		CustomTabPane.getInstance().removeTab(this.tab);
	}

	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayNewParameters(List<Parameter> data)
	{
		Platform.runLater(() -> {
			this.tableView.setItems(FXCollections.observableList(data));
			this.tableView.update();
		});
	}

	public void displayTitle(String title)
	{
		Platform.runLater(() -> this.tab.setTitle(title));
	}

	// ------------------------------------------------------------------------------------------------------------------
	private void createTable()
	{
		this.tableView.completeFirstColumn("Name", "name", true, false);
		this.tableView.completeSecondColumn("Expression", "expression", true, false);
		this.tableView.completeThirdColumn("Value", "valueAsString", false, true);
		this.tableView.completeFourthColumn("Description", "description", true, true);
		this.tableView.onFinishEditFirstColumn((par, value) -> this.model.updateNameRow(current(), value));
		this.tableView.onFinishEditSecondColumn((par, value) -> this.model.updateExpressionRow(current(), value));
		this.tableView.onFinishEditFourthColumn((par, varlue) -> this.model.updateDescriptionRow(current(), varlue));
		this.tableView.setRowFactory((v) -> new ColorRow());
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
