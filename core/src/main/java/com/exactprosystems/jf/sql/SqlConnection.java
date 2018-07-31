/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.sql;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlConnection implements AutoCloseable
{
	private final String     sql;
	private       Connection connection;

	protected SqlConnection(Connection connection, String sql)
	{
		this.connection = connection;
		this.sql = sql;
	}

	@Override
	public void close() throws SQLException
	{
		if (!this.isClosed())
		{
			this.connection.close();
		}
		this.connection = null;
	}

	@Override
	public String toString()
	{
		return SqlConnection.class.getSimpleName() + "{" + this.sql + ":" + this.hashCode() + ", closed=" + this.isClosed() + "}";
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
}
