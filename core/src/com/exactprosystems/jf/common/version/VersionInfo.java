////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.version;

public class VersionInfo
{
	private VersionInfo()
	{

	}

	private static final String LOCAL_BUILD = "LocalBuild";

	public static String getVersion()
	{
		Package pkg = VersionInfo.class.getPackage();
		String version = pkg.getImplementationVersion();

		return version == null ? LOCAL_BUILD : version;
	}

	public static boolean isDevVersion()
	{
		return getVersion().equals(LOCAL_BUILD);
	}
}
