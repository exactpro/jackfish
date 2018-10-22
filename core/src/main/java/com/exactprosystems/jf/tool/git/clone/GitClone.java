/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.tool.git.clone;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.io.File;

public class GitClone
{
	private static final Logger logger = Logger.getLogger(GitClone.class);

	private GitCloneController controller;
	private String locationToCloningConfig = null;
	private Task<Void> task;
	private CredentialBean credentialBean;

	public GitClone(CredentialBean bean)
	{
		this.credentialBean = bean;
		this.controller = Common.loadController(this.getClass());
		this.controller.init(this);
	}

	public String display()
	{
		this.controller.show();
		return locationToCloningConfig;
	}

	public void cancel()
	{
		if (this.task == null || !this.task.isRunning())
		{
			this.controller.hide();
		}
		if (this.task != null && this.task.isRunning())
		{
			this.task.cancel();
			this.task = null;
		}
	}

	void cloneProject(String projectLocation, String uri, String projectName, boolean openProject, ProgressMonitor monitor)
	{
		this.locationToCloningConfig = null;
		this.controller.setDisable(true);
		File projectFolder = new File(projectLocation + File.separator + projectName);
		this.task = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				DialogsHelper.showInfo(R.GIT_CLONE_START.get());
				GitUtil.gitClone(uri, projectFolder, credentialBean, monitor);
				return null;
			}
		};
		this.task.setOnSucceeded(e -> {
			this.controller.setDisable(false);
			DialogsHelper.showSuccess(String.format(R.GIT_CLONE_SUCCESS.get(), uri));
			if (openProject)
			{
				locationToCloningConfig = projectFolder.getAbsolutePath();
			}
			this.controller.hide();
		});
		this.task.setOnCancelled(e -> {
			DialogsHelper.showInfo(R.GIT_CLONE_CANCELED_BY_USER.get());
			this.controller.setDisable(false);
		});
		this.task.setOnFailed(e -> {
			Throwable exception = e.getSource().getException();
			logger.error(exception.getMessage(), exception);
			DialogsHelper.showError(String.format(R.GIT_CLONE_ERROR.get(), exception.getMessage()));
			this.controller.setDisable(false);
		});
		new Thread(this.task).start();
	}
}
