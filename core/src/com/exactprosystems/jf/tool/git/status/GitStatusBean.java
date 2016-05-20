////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.status;

import java.io.File;

public class GitStatusBean
{
	public enum Status
	{
		//TODO add styleclasses
		ADDED(""),
		REMOVED(""),
		CHANGED("");

		private Status(String styleClass)
		{

		}

		private String styleClass;

		public String getStyleClass()
		{
			return styleClass;
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
