////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.pull;

import com.exactprosystems.jf.api.common.i18n.R;

public class GitPullBean
{
	private final String fileName;
	private boolean needMerge;

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

	public void resolve()
	{
		this.needMerge = false;
	}

	@Override
	public String toString()
	{
		return String.format(R.GIT_PULL_BEAN_NEED_MERGE.get(), this.fileName, this.needMerge);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		GitPullBean that = (GitPullBean) o;

		return fileName != null ? fileName.equals(that.fileName) : that.fileName == null;

	}

	@Override
	public int hashCode()
	{
		return fileName != null ? fileName.hashCode() : 0;
	}
}
