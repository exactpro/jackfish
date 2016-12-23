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
