////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.pull;

public class GitPullBean
{
	private final String fileName;
	private final boolean needMerge;

	public GitPullBean(String fileName, boolean needMerge)
	{
		this.fileName = fileName;
		this.needMerge = needMerge;
	}

	public String getFileName()
	{
		return fileName;
	}

	public boolean isNeedMerge()
	{
		return needMerge;
	}
}
