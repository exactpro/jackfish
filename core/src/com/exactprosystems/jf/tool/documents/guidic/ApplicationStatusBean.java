////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.documents.guidic;

import com.exactprosystems.jf.api.app.AppConnection;

class ApplicationStatusBean
{
	private ApplicationStatus status;
	private AppConnection appConnection;
	private Throwable throwable;

	public ApplicationStatusBean(ApplicationStatus status, AppConnection appConnection, Throwable throwable)
	{
		this.status = status;
		this.appConnection = appConnection;
		this.throwable = throwable;
	}

	public ApplicationStatus getStatus()
	{
		return status;
	}

	public AppConnection getAppConnection()
	{
		return appConnection;
	}

	public Throwable getThrowable()
	{
		return throwable;
	}
}
