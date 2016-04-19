////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;

public class ConfigurationToolBar extends BorderPane
{
	private ConfigurationFxNew model;

	public ConfigurationToolBar(ConfigurationFxNew model)
	{
		this.model = model;
		init();
	}

	private void init()
	{
		ToolBar toolBar = new ToolBar();
		Button btnFilter = new Button("Filter");
		toolBar.getItems().addAll(btnFilter);
		this.setCenter(toolBar);

	}
}
