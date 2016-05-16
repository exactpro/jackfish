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
	private char[] password;
	private String projectLocation;
	private String remotePath;

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
			String projectName = remotePath.substring(remotePath.lastIndexOf('/'), remotePath.lastIndexOf('.'));
			File projectFolder = new File(projectLocation + File.separator + projectName);

			this.model.cloneRepository(projectFolder, this.remotePath, this.userName, this.password);
			DialogsHelper.showSuccess("Successful cloning repo " + this.remotePath);
			return projectFolder.getAbsolutePath() + File.separator + projectName + ".xml";
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

	public void setPassword(char[] password)
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
}
