////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.reset;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.main.Main;

import java.util.List;

public class GitReset
{
	private final Main model;
	private final GitResetController controller;

	public GitReset(Main model, List<String> list) throws Exception
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitReset.fxml"));
		this.controller.init(this, list);

	}

	public void select(String item)
	{

	}

	public void display()
	{
		this.controller.show();
	}

	public void hide()
	{
		this.controller.hide();
	}
}
