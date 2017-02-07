////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

public class AppConnection implements AutoCloseable
{
	public AppConnection(IApplication connection, String id, int port, IApplicationFactory factory, int pid)
	{
		this.app = connection;
		this.id = id;
		this.port = port;
		this.factory = factory;
		this.pid = pid;
	}

	@Override
	public void close() throws Exception
	{
		if (this.app != null)
		{
			this.app.stop(false);
		}
		this.app = null;
	}

	@Override
	public String toString()
	{
		return AppConnection.class.getSimpleName() + "{" + this.id + ":" + hashCode() + "}";
	}

	public String getId()
	{
		return this.id;
	}

	public boolean isGood()
	{
		return this.app != null;
	}

	public IApplication getApplication()
	{
		return this.app;
	}

	public int getPort()
	{
		return this.port;
	}

	public IGuiDictionary getDictionary()
	{
		return this.factory.getDictionary();
	}

	public int getProcessId()
	{
		return this.pid;
	}

	private IApplication app;

	private String id;

	private int port;

	private IApplicationFactory factory;
	
	private int pid;
}
