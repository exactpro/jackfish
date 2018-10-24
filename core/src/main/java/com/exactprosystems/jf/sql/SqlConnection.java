/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
