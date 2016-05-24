////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.commit;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.main.Main;

public class GitCommit
{
	private Main model;
	private GitCommitController controller;

	public GitCommit(Main model) throws Exception
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitCommit.fxml"));
		this.controller.init(this);
	}

	public void display()
	{
		this.controller.show();
	}
}
