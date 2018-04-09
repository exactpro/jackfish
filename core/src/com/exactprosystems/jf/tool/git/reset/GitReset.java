/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.git.reset;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;

import java.util.List;

public class GitReset
{
	private final Main model;
	private final GitResetController controller;

	public GitReset(Main model, List<GitResetBean> list) throws Exception
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitReset.fxml"));
		this.controller.init(this, list);

	}

	public void select(GitResetBean item) throws Exception
	{
		this.controller.displayMessage(item.getMessage());
		this.controller.displayFiles(item.getFiles());
	}

	public void reset(GitResetBean bean) throws Exception
	{
		String commitId = bean.getCommitId();
		GitUtil.gitReset(this.model.getCredential(), commitId);
		DialogsHelper.showSuccess(String.format(R.GIT_RESET_TO.get(), commitId));
		this.controller.hide();
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
