////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

public class ApiVersionInfo
{
	private static final int majorVersion = 2;
	private static final int minorVersion = 9;
	
	public static int majorVersion() 
	{
		return majorVersion;
	}
	
	public static int minorVersion() 
	{
		return minorVersion;
	}
}
