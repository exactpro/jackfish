////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool;

public class SupportedEntry
{
	private boolean isSupported;
	private int requaredMajorVersion;
	private int requaredMinorVersion;

	public SupportedEntry(boolean isSupported, int requredMajorVersion, int requredMinorVersion)
	{
		this.isSupported = isSupported;
		this.requaredMajorVersion = requredMajorVersion;
		this.requaredMinorVersion = requredMinorVersion;
	}

	public boolean isSupported()
	{
		return isSupported;
	}

	public void setIsSupported(boolean isSupported)
	{
		this.isSupported = isSupported;
	}

	public int getRequaredMajorVersion()
	{
		return requaredMajorVersion;
	}

	public void setRequaredMajorVersion(int requaredMajorVersion)
	{
		this.requaredMajorVersion = requaredMajorVersion;
	}

	public int getRequaredMinorVersion()
	{
		return requaredMinorVersion;
	}

	public void setRequaredMinorVersion(int requaredMinorVersion)
	{
		this.requaredMinorVersion = requaredMinorVersion;
	}

	@Override
	public String toString()
	{
		return "SupportedEntry{" +
				"isSupported=" + isSupported +
				", requaredMajorVersion=" + requaredMajorVersion +
				", requaredMinorVersion=" + requaredMinorVersion +
				'}';
	}
}
