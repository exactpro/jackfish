/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
