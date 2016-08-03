////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git;

import com.exactprosystems.jf.tool.CssVariables;

import java.io.File;

public class GitBean
{
	public enum Status
	{
		//TODO add styleclasses
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
