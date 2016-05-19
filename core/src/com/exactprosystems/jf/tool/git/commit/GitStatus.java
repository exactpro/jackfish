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

import java.io.File;
import java.util.List;

public class GitStatus
{
	private Main model;
	private final GitStatusController controller;

	public GitStatus(Main model)
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitStatus.fxml"));
		this.controller.init(this);
	}

	public void display(List<GitStatusBean> list)
	{
		this.controller.display(list);
	}

	public void revert(List<File> collect) throws Exception
	{
		this.model.revertFiles(collect);
	}
}
