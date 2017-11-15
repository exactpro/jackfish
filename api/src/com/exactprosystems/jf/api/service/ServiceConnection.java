////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.service;

public class ServiceConnection implements AutoCloseable
{
	public ServiceConnection(IService connection, String id)
	{
		this.service = connection;
		this.id = id;
	}

	@Override
	public void close() throws Exception
	{
		if (this.service != null)
		{
			this.service.stop();;
		}
		this.service = null;
	}
	
	@Override
	public String toString()
	{
		return ServiceConnection.class.getSimpleName() + "{" + this.id + ":" +hashCode() + "}";
	}
	
	public boolean isStopped()
	{
		return this.service == null;
	}
	
	public IService getService()
	{
		return this.service;
	}

	public String getId()
	{
		return id;
	}

	private IService service;
	
	private String id;
}
