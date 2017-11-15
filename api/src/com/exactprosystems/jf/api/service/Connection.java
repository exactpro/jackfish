////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.service;

import java.net.Socket;
import java.io.*;

public class Connection
{
	public Connection(Socket socket, Writer writer, Reader reader)
	{
		super();
		this.Socket = socket;
		this.Writer = writer;
		this.Reader = reader;
	}
	
	@Override
	public String toString()
	{
		return Connection.class.getSimpleName() + "{" + this.Socket + ":" +hashCode() + "}";
	}
	
	public Socket Socket;
	public Writer Writer;
	public Reader Reader;
}
