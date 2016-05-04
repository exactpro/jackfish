////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

public class ConfigurationToolBar extends BorderPane
{
	private ConfigurationFx model;

	public ConfigurationToolBar(ConfigurationFx model)
	{
		this.model = model;
		init();
	}

	private void init()
	{
		ToolBar toolBar = new ToolBar();

		Button reload = new Button();
		Common.customizeLabeled(reload, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.REFRESH);
		reload.setTooltip(new Tooltip("Reload configuration"));
		reload.setOnAction(e -> Common.tryCatch(() -> this.model.refresh(), "Error on refresh configuration"));
		toolBar.getItems().add(reload);
		this.setCenter(toolBar);

	}
}
