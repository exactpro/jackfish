package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.api.app.AppConnection;

public class ConnectionBean
{
	private String        name;
	private AppConnection connection;

	public ConnectionBean(String name, AppConnection connection)
	{
		this.name = name;
		this.connection = connection;
	}

	public String getName()
	{
		return name;
	}

	public AppConnection getConnection()
	{
		return connection;
	}

	@Override
	public String toString()
	{
		return this.name + " [ " + this.connection.toString() + " ]";
	}

}
