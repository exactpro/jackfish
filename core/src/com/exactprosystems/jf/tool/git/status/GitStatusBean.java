////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.status;

import com.exactprosystems.jf.tool.CssVariables;

import java.io.File;

public class GitStatusBean
{
	public enum Status
	{
		//TODO add styleclasses
		ADDED		(CssVariables.GIT_ADDED_FILE,	"new file"),
		REMOVED		(CssVariables.GIT_REMOVED_FILE,	"deleted"),
		CHANGED		(CssVariables.GIT_CHANGED_FILE,	"modified");

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

	private final Status status;
	private final File file;
	private boolean isChecked;

	public GitStatusBean(Status status, File file)
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

	public boolean isChecked()
	{
		return this.isChecked;
	}

	public void setChecked(boolean checked)
	{
		this.isChecked = checked;
	}
}
