////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.clone;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.io.File;

public class GitClone
{
	private GitCloneController controller;

	private String locationToCloningConfig = null;

	private Service<Void> service;

	public GitClone()
	{
		this.controller = Common.loadController(this.getClass().getResource("GitCloneWindow.fxml"));
		this.controller.init(this);
	}

	public String display() throws Exception
	{
		this.controller.show();
		return locationToCloningConfig;
	}

	public void cancel() throws Exception
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

	public void cloneProject(String projectLocation, String uri, String projectName, String userName, String password, boolean openProject, ProgressMonitor monitor) throws Exception
	{
		this.locationToCloningConfig = null;
		this.controller.setDisable(true);
		File projectFolder = new File(projectLocation + File.separator + projectName);
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
						GitUtil.getInstance().gitClone(uri, projectFolder, userName, password, monitor);
						return null;
					}
				};
			}
		};
		service.start();
		service.setOnSucceeded(e -> {
			this.controller.setDisable(false);
			DialogsHelper.showSuccess("Successful cloning repo " + uri);
			if (openProject)
			{
				locationToCloningConfig = projectFolder.getAbsolutePath();
			}
			this.controller.hide();
		});
		service.setOnCancelled(e -> {
			DialogsHelper.showInfo("Task canceled by user");
			this.controller.setDisable(false);
		});
		service.setOnFailed(e -> {
			String asd;
			Throwable exception = e.getSource().getException();
			DialogsHelper.showError("ERROR " + exception.getMessage());
		});
	}
}
