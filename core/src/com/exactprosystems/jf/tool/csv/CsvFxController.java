////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.csv;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.SpreadsheetView;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CsvFxController implements Initializable, ContainingParent
{
//	public BorderPane				borderPane;
	public SpreadsheetView 			view;
	public ToolBar					toolBar;
	public Button					btnReloadCsv;
	public TextField				tfDelimiter;

	private BorderPane				pane;
	private CsvFx					model;
	private CustomTab				tab;
	private DataProvider<String>	provider;

	// ----------------------------------------------------------------------------------------------
	// Event handlers
	// ----------------------------------------------------------------------------------------------

	// ----------------------------------------------------------------------------------------------
	// Interface Initializable
	// ----------------------------------------------------------------------------------------------
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		Platform.runLater(() -> {
			btnReloadCsv.setTooltip(new Tooltip("Reload Csv"));
			Common.customizeLabeled(btnReloadCsv, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.REFRESH);
		});
		listeners();
	}

	//============================================================
	// events methods
	//============================================================
	public void reloadCsv(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::reloadCsv, "Error on reload csv");
	}

	// ----------------------------------------------------------------------------------------------
	// Interface ContainingParent
	// ----------------------------------------------------------------------------------------------
	@Override
	public void setParent(Parent parent)
	{
		this.pane = (BorderPane) parent;
	}

	// ----------------------------------------------------------------------------------------------
	// Public methods
	// ----------------------------------------------------------------------------------------------
	public void init(CsvFx model, Settings settings)
	{
		this.model = model;
		
		this.tab = Common.createTab(model);
		this.tab.setContent(this.pane);

		Platform.runLater(() ->
		{
			Common.getTabPane().getTabs().add(this.tab);
			Common.getTabPane().getSelectionModel().select(this.tab);
		});
	}

	public void saved(String name)
	{
		this.tab.saved(name);
	}

	public void close() throws Exception
	{
		this.tab.close();
		Common.getTabPane().getTabs().remove(this.tab);
	}

	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayTitle(String title)
	{
		Platform.runLater(() -> this.tab.setTitle(title));
	}

	public void displayTable(DataProvider<String> provider)
	{
		Platform.runLater(() -> 
		{
			this.provider = provider;
			this.view = new SpreadsheetView(this.provider);
			this.pane.setCenter(this.view);
		});
	}

	//============================================================
	// private methods
	//============================================================
	private void listeners()
	{
		this.tfDelimiter.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue && !newValue)
			{
				setDelimiter();
			}
		});
		this.tfDelimiter.setOnAction(event -> {
			setDelimiter();
			this.reloadCsv(event);
		});
	}

	private void setDelimiter()
	{
		if (tfDelimiter.getText().length() != 0)
		{
			this.model.setDelimiter(tfDelimiter.getText().charAt(0));
		}
	}
}
