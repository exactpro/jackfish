////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.status;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitBean;
import com.exactprosystems.jf.tool.git.GitUtil;
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

	public void display(List<GitBean> list, String state)
	{
		this.controller.display(list, state);
	}

	public void revertFiles(List<File> collect) throws Exception
	{
		CredentialBean credential = this.model.getCredential();
		GitUtil.revertFiles(credential, collect);
		this.controller.updateFiles(GitUtil.gitStatus(credential));
	}

	public void ignoreFiles(List<File> collect) throws Exception
	{
		GitUtil.ignoreFiles(collect);
		this.controller.updateFiles(GitUtil.gitStatus(this.model.getCredential()));
	}
}
