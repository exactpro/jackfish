/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
