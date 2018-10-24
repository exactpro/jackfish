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
package com.exactprosystems.jf.tool.git;

import com.exactprosystems.jf.tool.CssVariables;

import java.io.File;

public class GitBean
{
	public enum Status
	{
		//TODO add styleclasses
		EMPTY		("",""),
		ADDED		(CssVariables.GIT_ADDED_FILE,		"new file"),
		REMOVED		(CssVariables.GIT_REMOVED_FILE,		"deleted"),
		CHANGED		(CssVariables.GIT_CHANGED_FILE,		"modified"),
		UNTRACKED	(CssVariables.GIT_UNTRACKED_FILE,	"untracked"),
		UNSTAGED	(CssVariables.GIT_UNSTAGED_FILE,	"unstaged"),
		CONFLICTING	(CssVariables.GIT_UNSTAGED_FILE,	"conflicting");
		;

		Status(String styleClass, String preffix)
		{
			this.styleClass = styleClass;
			this.preffix = preffix;
		}

		private String styleClass;
		private String preffix;

		public String getStyleClass()
		{
			return this.styleClass;
		}

		public String getPreffix()
		{
			return this.preffix;
		}
	}

	private Status status;
	private final File file;
	private boolean isChecked;
	private boolean isMatch;

	public GitBean(Status status, File file)
	{
		this.status = status;
		this.file = file;
	}

	public Status getStatus()
	{
		return status;
	}

	public File getFile()
	{
		return file;
	}

	public void updateStatus(Status newStatus)
	{
		this.status = newStatus;
	}

	public boolean isChecked()
	{
		return this.isChecked;
	}

	public void setChecked(boolean checked)
	{
		this.isChecked = checked;
	}

	public boolean isMatch()
	{
		return isMatch;
	}

	public void setMatch(boolean match)
	{
		isMatch = match;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		GitBean gitBean = (GitBean) o;

		return file != null ? file.equals(gitBean.file) : gitBean.file == null;

	}

	@Override
	public int hashCode()
	{
		return file != null ? file.hashCode() : 0;
	}
}
