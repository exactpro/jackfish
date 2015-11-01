////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.sql;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlConnection implements AutoCloseable 
{
	protected SqlConnection(Connection connection, String sql)
	{
		this.connection = connection;
		this.sql = sql;
	}
	
	@Override
	public void close() throws Exception
	{
		if (!isClosed())
		{
			this.connection.close();
		}
		this.connection = null;
	}
	
	@Override
	public String toString()
	{
		return SqlConnection.class.getSimpleName() + "{" + this.sql + ":" + hashCode() + ", closed=" + isClosed() + "}";
	}
	
	public boolean isClosed()
	{
		try
		{
			return (this.connection == null || this.connection.isClosed());
		}
		catch (SQLException e)
		{
			return true;
		}
	}
	
	public Connection getConnection()
	{
		return this.connection;
	}

	private Connection connection;
	
	private String sql;
}
