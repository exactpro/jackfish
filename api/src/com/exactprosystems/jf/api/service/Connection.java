////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
