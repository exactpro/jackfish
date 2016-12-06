////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.text;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import com.exactprosystems.jf.tool.settings.SettingsPanel;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class PlainTextFxController implements Initializable, ContainingParent
{
	public GridPane					grid;
	public TextArea 				textArea;

	private Parent					pane;
	private PlainTextFx				model;
	private CustomTab				tab;

	// ----------------------------------------------------------------------------------------------
	// Event handlers
	// ----------------------------------------------------------------------------------------------

	// ----------------------------------------------------------------------------------------------
	// Interface Initializable
	// ----------------------------------------------------------------------------------------------
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.textArea = new TextArea();
		this.textArea.setEditable(true);
		this.grid.add(this.textArea, 0, 0);
		GridPane.setColumnSpan(this.textArea, 2);
	}

	// ----------------------------------------------------------------------------------------------
	// Interface ContainingParent
	// ----------------------------------------------------------------------------------------------
	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	// ----------------------------------------------------------------------------------------------
	// Public methods
	// ----------------------------------------------------------------------------------------------
	public void init(PlainTextFx model, Settings settings)
	{
		this.model = model;
		SettingsValue value = settings.getValueOrDefault(Settings.GLOBAL_NS, SettingsPanel.SETTINGS, SettingsPanel.FONT, "Monospaced$16");
		this.textArea.setFont(Common.fontFromString(value.getValue()));
		this.tab = CustomTabPane.getInstance().createTab(model);
		this.tab.setContent(this.pane);
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
	public void displayTitle(String title)
	{
		Platform.runLater(() -> this.tab.setTitle(title));
	}

	public void displayText(StringProperty property)
	{
		Platform.runLater(() -> this.textArea.textProperty().bindBidirectional(property));
	}

	// ------------------------------------------------------------------------------------------------------------------


}
