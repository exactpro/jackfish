////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.clone;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;

import java.io.File;

public class GitClone
{
	private Main model;
	private GitCloneController controller;

	private String userName;
	private String password;
	private String projectLocation;
	private String remotePath;
	private String projectName;
	private boolean needOpenProject;

	public GitClone(Main model)
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitCloneWindow.fxml"));
		this.controller.init(this);
	}

	public String display() throws Exception
	{
		boolean display = this.controller.display();
		if (display)
		{
			File projectFolder = new File(projectLocation + File.separator + this.projectName);
			this.model.cloneRepository(projectFolder, this.remotePath, this.userName, this.password);
			DialogsHelper.showSuccess("Successful cloning repo " + this.remotePath);
			if (this.needOpenProject)
			{
				return projectFolder.getAbsolutePath() + File.separator + this.projectName + ".xml";
			}
			return null;
//			DisplayableTask<Void> task = new DisplayableTask<>(() -> {
//				this.model.cloneRepository(this.projectLocation, this.remotePath, this.userName, this.password);
//				return null;
//			});
//
//			task.onSuccess(rep -> {
//				DialogsHelper.showSuccess("Success clone from " + this.remotePath);
//			});
//			this.model.displayableTask(task, "Cloning repository ... ", "Error on clone repository");
		}
		return null;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setProjectLocation(String projectLocation)
	{
		this.projectLocation = projectLocation;
	}

	public void setRemotePath(String remotePath)
	{
		this.remotePath = remotePath;
	}

	public void setProjectName(String projectName)
	{
		this.projectName = projectName;
	}

	public void setOpenProject(boolean flag)
	{
		this.needOpenProject = flag;
	}
}
