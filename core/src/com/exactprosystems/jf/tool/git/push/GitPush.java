////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.git.push;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class GitPush
{
	private static final Logger logger = Logger.getLogger(GitPush.class);
	private final CredentialBean credential;

	private Main model;
	private GitPushController controller;

	private Service<Void> service;

	public GitPush(Main model) throws Exception
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitPush.fxml"));
		this.controller.init(this);
		this.credential = this.model.getCredential();
		this.controller.displayUnpushingCommits(GitUtil.gitUnpushingCommits(this.credential));
		this.controller.displayCurrentBranch(GitUtil.currentBranch(this.credential));
		String remoteBranch = GitUtil.getRemoteBranch(this.credential, GitUtil.currentBranch(credential));
		List<String> list = GitUtil.getBranches(this.credential)
				.stream()
				.filter(b -> !b.isLocal())
				.map(GitUtil.Branch::getSimpleName)
				.collect(Collectors.toList());
		this.controller.displayRemoteBranch(list, remoteBranch);
	}

	public void display()
	{
		this.controller.show();
	}

	public void close() throws Exception
	{
		if (this.service == null || !this.service.isRunning())
		{
			this.controller.hide();
		}
		if (this.service != null && this.service.isRunning())
		{
			this.service.cancel();
			this.service = null;
		}
	}

	void push(String remoteBranchName)
	{
		this.controller.setDisable(true);
		CredentialBean credential = model.getCredential();
		this.service = new Service<Void>()
		{
			@Override
			protected Task<Void> createTask()
			{
				return new Task<Void>()
				{
					@Override
					protected Void call() throws Exception
					{
						DialogsHelper.showInfo("Start pushing");
						GitUtil.gitPush(credential, remoteBranchName);
						return null;
					}
				};
			}
		};
		service.start();
		service.setOnSucceeded(e -> {
			this.controller.setDisable(false);
			DialogsHelper.showSuccess("Successful pushing");
			this.controller.hide();
		});
		service.setOnCancelled(e -> {
			DialogsHelper.showInfo("Task canceled by user");
			this.controller.setDisable(false);
		});
		service.setOnFailed(e -> {
			Throwable exception = e.getSource().getException();
			logger.error(exception.getMessage(), exception);
			DialogsHelper.showError("Error on pushing\n" + exception.getMessage());
			this.controller.setDisable(false);
		});
	}
}
