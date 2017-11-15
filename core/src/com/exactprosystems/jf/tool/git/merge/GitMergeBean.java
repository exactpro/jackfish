////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.merge;

public class GitMergeBean
{
	private final String fileName;

	public GitMergeBean(String fileName)
	{
		this.fileName = fileName;
	}

	public String getFileName()
	{
		return this.fileName;
	}
}
