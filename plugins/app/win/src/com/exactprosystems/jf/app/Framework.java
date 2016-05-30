package com.exactprosystems.jf.app;

public enum Framework
{
	WIN_FORM("WinForm"),
	SILVER_LIGHT("Silverlight"),
	WFP("WPF");

	private String frameworkId;

	Framework(String frameworkId)
	{
		this.frameworkId = frameworkId;
	}

	public static Framework byId(String frameworkId)
	{
		for (Framework framework : Framework.values())
		{
			if (framework.frameworkId.toLowerCase().contains(frameworkId.toLowerCase()))
			{
				return framework;
			}
		}
		return null;
	}
}
