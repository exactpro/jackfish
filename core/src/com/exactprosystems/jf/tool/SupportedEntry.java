////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool;

public class SupportedEntry
{
	private boolean isSupported;

	public SupportedEntry(boolean isSupported)
	{
		this.isSupported = isSupported;
	}

	public boolean isSupported()
	{
		return isSupported;
	}

	public void setIsSupported(boolean isSupported)
	{
		this.isSupported = isSupported;
	}

	@Override
	public String toString()
	{
		return "SupportedEntry{ isSupported=" + isSupported + " }";
	}
}
