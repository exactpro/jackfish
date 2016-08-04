////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.pull;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.util.List;

import static com.exactprosystems.jf.tool.Common.logger;

public class GitPull
{
	private final Main model;

	private Service<List<GitPullBean>> service;
	private GitPullController controller;

	public GitPull(Main model)
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitPull.fxml"));
		this.controller.init(this, this.model.getCredential());
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

	public void pull(ProgressMonitor monitor) throws Exception
	{
		this.controller.startPulling();
		CredentialBean credential = this.model.getCredential();
		this.service = new Service<List<GitPullBean>>()
		{
			@Override
			protected Task<List<GitPullBean>> createTask()
			{
				return new Task<List<GitPullBean>>()
				{
					@Override
					protected List<GitPullBean> call() throws Exception
					{
						DialogsHelper.showInfo("Start pulling");
						return GitUtil.gitPull(credential, monitor);
					}
				};
			}
		};
		service.start();
		service.setOnSucceeded(e -> Common.tryCatch(() -> {
			DialogsHelper.showSuccess("Successful pulling");
			this.displayFiles(((List<GitPullBean>) e.getSource().getValue()));
			this.controller.endPulling("Pull done. All files up to date");
		}, "Error on display files"));
		service.setOnCancelled(e -> {
			DialogsHelper.showInfo("Task canceled by user");
			this.controller.endPulling("");
		});
		service.setOnFailed(e -> {
			Throwable exception = e.getSource().getException();
			logger.error(exception.getMessage(), exception);
			DialogsHelper.showError("Error on pulling" + exception.getMessage());
			this.controller.endPulling("Error on pulling");
		});
	}

	public void display()
	{
		this.controller.show();
	}

	private void displayFiles(List<GitPullBean> list) throws Exception
	{
		if (list.stream().anyMatch(GitPullBean::isNeedMerge))
		{
			List<String> strings = this.model.gitMerge();
			list.stream().filter(b -> strings.contains(b.getFileName())).forEach(GitPullBean::resolve);
		}
		this.controller.displayFiles(list);
	}
}
