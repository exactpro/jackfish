/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.api.service;

public enum ServiceStatus
{
	//TODO need refactor
	NotStarted(null),

	StartSuccessful(null),
	StartFailed(null);

	private String msg;

	ServiceStatus(String msg)
	{
		this.msg = msg;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(String msg)
	{
		if (this == StartFailed)
		{
			this.msg = msg;
		}
	}
}
