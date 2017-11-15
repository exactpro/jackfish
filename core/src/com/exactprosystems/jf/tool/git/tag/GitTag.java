////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.git.tag;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import javafx.concurrent.Task;

public class GitTag
{
	private Main model;
	private final CredentialBean credentialBean;
	private GitTagController controller;

	public GitTag(Main model) throws Exception
	{
		this.model = model;
		this.credentialBean = this.model.getCredential();
		this.controller = Common.loadController(this.getClass().getResource("GitTag.fxml"));
		this.controller.init(this);
		this.controller.updateTags(GitUtil.getTags(this.credentialBean));
	}

	public void display()
	{
		this.controller.show();
	}

	void deleteTag(String tagName) throws Exception
	{
		GitUtil.deleteTag(this.credentialBean, tagName);
		this.controller.updateTags(GitUtil.getTags(this.credentialBean));
	}

	void newTag(String version, String msg) throws Exception
	{
		GitUtil.newTag(this.credentialBean, msg, version);
		this.controller.updateTags(GitUtil.getTags(this.credentialBean));
	}

	void pushTag() throws Exception
	{
		DialogsHelper.showInfo("Start pushing tags...");
		this.controller.setDisable(true);
		Task<Void> task = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				GitUtil.pushTag(credentialBean);
				return null;
			}
		};
		task.setOnFailed(e -> {
			this.controller.setDisable(false);
			DialogsHelper.showError("Error on push tags");
		});
		task.setOnSucceeded(e -> {
			this.controller.setDisable(false);
			DialogsHelper.showSuccess("All tags was pushed");
			Common.tryCatch(() -> this.controller.updateTags(GitUtil.getTags(this.credentialBean)), "Error on updateTask");
		});
		new Thread(task).start();
	}
}
