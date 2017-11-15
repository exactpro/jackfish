////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.api.service;

public enum ServiceStatus
{
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
