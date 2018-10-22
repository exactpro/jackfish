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
package com.exactprosystems.jf.tool.newconfig.wizard;

import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.main.Main;

import java.io.File;

public class WizardConfiguration
{
	private File folderDir;
	private String newProjectName;
	private Main model;
	private WizardConfigurationController controller;

	private boolean createMatrixDir = true;
	private boolean createLibraryDir = true;
	private boolean createAppDicDir = true;
	private boolean createClientDicDir = true;
	private boolean createVarsDir = true;
	private boolean createReportDir = true;

	public WizardConfiguration(Main model)
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("WizardConfiguration.fxml"));
		this.controller.init(this);
	}

	public String display() throws Exception
	{
		Boolean needCreate = this.controller.display();
		if (needCreate)
		{
			File folder = new File(this.folderDir.getAbsolutePath() + File.separator + this.newProjectName);
			this.model.createFolder(this.folderDir, this.newProjectName);
			this.model.createFile(folder, newProjectName+".xml");
			if (createMatrixDir) this.model.createFolder(folder, Configuration.MATRIX_FOLDER);
			if (createLibraryDir) this.model.createFolder(folder, Configuration.LIBRARY_FOLDER);
			if (createAppDicDir) this.model.createFolder(folder, Configuration.APP_DIC_FOLDER);
			if (createClientDicDir) this.model.createFolder(folder, Configuration.CLIENT_DIC_FOLDER);
			if (createVarsDir)
			{
				this.model.createFolder(folder, Configuration.USER_VARS_FOLDER);
				this.model.createFile(new File(folder + File.separator + Configuration.USER_VARS_FOLDER), Configuration.USER_VARS_FILE);
			}
			if (createReportDir) this.model.createFolder(folder, Configuration.REPORTS_FOLDER);

			return this.folderDir.getAbsolutePath() + File.separator + this.newProjectName;
		}
		return null;
	}

	void setFolderDir(File folderDir)
	{
		this.folderDir = folderDir;
	}

	void setNewProjectName(String newProjectName)
	{
		this.newProjectName = newProjectName;
	}

	void setCreateMatrixDir(boolean createMatrixDir)
	{
		this.createMatrixDir = createMatrixDir;
	}

	void setCreateLibraryDir(boolean createLibraryDir)
	{
		this.createLibraryDir = createLibraryDir;
	}

	void setCreateAppDicDir(boolean createAppDicDir)
	{
		this.createAppDicDir = createAppDicDir;
	}

	void setCreateClientDicDir(boolean createClientDicDir)
	{
		this.createClientDicDir = createClientDicDir;
	}

	void setCreateVarsDir(boolean createVarsDir)
	{
		this.createVarsDir = createVarsDir;
	}

	void setCreateReportDir(boolean createReportDir)
	{
		this.createReportDir = createReportDir;
	}
}
