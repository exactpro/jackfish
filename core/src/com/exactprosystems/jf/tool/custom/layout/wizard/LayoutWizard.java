////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.layout.wizard;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;

public class LayoutWizard
{
	private LayoutWizardController controller;
	private IGuiDictionary dictionary;
	private AppConnection appConnection;
	private Table table;

	public LayoutWizard(Table table, AppConnection appConnection)
	{
		this.table = table;
		this.dictionary = appConnection.getDictionary();
		this.controller = Common.loadController(this.getClass().getResource("LayoutWizard.fxml"));
		this.controller.init(this, table);
		this.controller.displayDialogs(dictionary.getWindows());
	}

	public void show()
	{
		this.controller.show();
	}

	void close()
	{
		this.controller.hide();
	}

	void accept()
	{

	}

	void changeScale(double currentZoom)
	{

	}

	void changeDialog(IWindow window)
	{
		if (window == null)
		{
			return;
		}

		this.controller.displayControls(window.getControls(IWindow.SectionKind.Run));
	}
}
