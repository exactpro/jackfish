////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

public class ClientConnection implements AutoCloseable
{
	public ClientConnection(IClient connection, String id, IMessageDictionary dictionary)
	{
		this.client = connection;
		this.id = id;
		this.dictionary = dictionary;
	}

	@Override
	public void close() throws Exception
	{
		if (this.client != null)
		{
			this.client.stop();;
		}
		this.client = null;
	}
	
	@Override
	public String toString()
	{
		return ClientConnection.class.getSimpleName() + "{" + this.id + ":" +hashCode() + "}";
	}
	
	public boolean isOpen()
	{
		return this.client != null && this.client.isOpen();
	}
	
	public boolean isBad()
	{
		return this.client == null;
	}

	public String getId()
	{
		return id;
	}

	public IClient getClient()
	{
		return this.client;
	}

	public IMessageDictionary getDictionary()
	{
		return this.dictionary;
	}

	private IClient client;
	
	private String id;
	
	private IMessageDictionary dictionary;

}
