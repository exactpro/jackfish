////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.error.app.InternalErrorException;

public enum CSharpLogLevel
{
	All("All"),
	Error("Error");

	private String name;

	CSharpLogLevel(String name)
	{
		this.name = name;
	}

	public String logLevel()
	{
		return this.name;
	}

	public static CSharpLogLevel logLevelFromStr(String str) throws InternalErrorException
	{
		if (str == null)
		{
			return All;
		}
		switch (str.toUpperCase())
		{
			case "ALL":
				return All;
			case "ERROR":
				return Error;
			default:
				return All;
		}
	}
}
