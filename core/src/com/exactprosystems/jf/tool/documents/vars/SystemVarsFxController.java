////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents.vars;

import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import com.exactprosystems.jf.tool.custom.table.CustomTable;
import com.exactprosystems.jf.tool.documents.AbstactDocumentController;
import com.exactprosystems.jf.tool.documents.ControllerInfo;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.layout.GridPane;

import static com.exactprosystems.jf.tool.Common.tryCatch;

import java.net.URL;
import java.util.ResourceBundle;


@ControllerInfo (resourceName = "SystemVarsFx.fxml")
public class SystemVarsFxController extends AbstactDocumentController<SystemVarsFx>
{
    public GridPane               grid;
    public CustomTable<Parameter> tableView;

    private Parent                pane;
    private CustomTab             tab;

	//----------------------------------------------------------------------------------------------
	// Event handlers
	//----------------------------------------------------------------------------------------------
	public void addNewVar(ActionEvent event)
	{
		tryCatch(this.model::addNewVariable, "Error on adding new var");
	}

	//----------------------------------------------------------------------------------------------
	// Public methods
	//----------------------------------------------------------------------------------------------
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        System.err.println(">> initalize " + location + " " + resources);
        
        super.initialize(location, resources);
        
        this.tableView = new CustomTable<>(true);
        MenuItem itemAdd = new MenuItem("Add new variable");
        itemAdd.setOnAction(this::addNewVar);
        this.tableView.getContextMenu().getItems().add(0, itemAdd);
        this.grid.add(this.tableView, 0, 0);

        System.err.println(">> initalize done!!!");
    }
	
	@Override
	public void init(Document model)
	{
        System.err.println(">> init " + model);

        super.init(model);

        this.tab = CustomTabPane.getInstance().createTab(model);
		this.tab.setContent(this.pane);
		this.tab.setTitle(this.model.getNameProperty().get());
		this.tableView.setListener(this.model::removeParameters);
		createTable();
		CustomTabPane.getInstance().addTab(this.tab);
		CustomTabPane.getInstance().selectTab(this.tab);
		
        this.model.getNameProperty().setOnChangeListener((o, n) ->
        {
            Platform.runLater(() ->
            {
                this.tab.setTitle(n);
                this.tab.saved(n);
            });
        });
        
        this.model.getParameters().setOnChangeListener((o,n) ->
        {
            Platform.runLater(() -> 
            {
                System.err.println(">>>> data changed");
                this.tableView.setItems(FXCollections.observableList(this.model.getParameters()));
                this.tableView.update();
            });
        });
        
        System.err.println(">> init done!!! ");
	}
	
	protected void close()
	{
		this.tab.close();
		CustomTabPane.getInstance().removeTab(this.tab);
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
