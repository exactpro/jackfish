////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.sql;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Configuration.SqlEntry;
import com.exactprosystems.jf.functions.Table;

public class DataBasePool
{
	public DataBasePool(Configuration configuration)
	{
		this.configuration = configuration;
		this.drivers = new HashMap<String, Driver>();
	}
	
	public SqlConnection connect(String sql, String server, String base, String user, String password) throws Exception
	{
		Driver driver = getDriver(sql);
		
		if (driver == null)
		{
			throw new Exception("The driver '" + sql + "' was not found.");
		}
		
		Connection connection = connection(sql, driver, server, base, user, password);
		
		return new SqlConnection(connection, sql);
	}
	
	public Table select(SqlConnection connection, String text, Object[] objs) throws Exception
	{
		if (connection.isClosed())
		{
			throw new Exception(connection.toString() + " is not established." );
		}
		
		PreparedStatement query = connection.getConnection().prepareStatement(text);

		int index = 1;
		int limit = query.getParameterMetaData().getParameterCount();
		for (Object obj : objs)
		{
			if (index <= limit)
			{
				
				query.setObject(index, obj);
			}
			index++;
		}

		ResultSet result = query.executeQuery();
		
		return new Table(result);
	}
	
	public boolean execute(SqlConnection connection, String text, Object[] objs) throws Exception
	{
		if (connection.isClosed())
		{
			throw new Exception(connection.toString() + " is not established." );
		}
		
		PreparedStatement query = connection.getConnection().prepareStatement(text);
		int index = 1;
		int limit = query.getParameterMetaData().getParameterCount();
		for (Object obj : objs)
		{
			if (index <= limit)
			{
				query.setObject(index, obj);
			}
			index++;
		}
		
		return query.execute();
	}

	public void disconnect(SqlConnection connection) throws Exception
	{
		connection.close();
	}

	private Connection connection(String sql, Driver driver, String server, String base, String user, String password) throws Exception
	{
		SqlEntry entry = this.configuration.getSqlEntry(sql);
		String url 	 = entry.get(Configuration.sqlConnection);
		String connectionString = url.replace("${SERVER}", server).replace("${BASE}", base);
		Properties prop = new Properties();
		prop.put("user", user);
		prop.put("password", password);
		return driver.connect(connectionString, prop);
	}
	
	private Driver getDriver(String sql) throws Exception
	{
		if (sql == null)
		{
			throw new Exception("sql");
		}
		
		Driver driver = this.drivers.get(sql);
		if (driver == null)
		{
			SqlEntry entry = this.configuration.getSqlEntry(sql);
			if (entry == null)
			{
				throw new Exception("'" + sql + "' is not found.");
			}
			
			String jarName 	 = entry.get(Configuration.sqlJar);
			URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL("file:" + jarName) });
			
			ServiceLoader<Driver> loader = ServiceLoader.load(Driver.class, classLoader);
			Iterator<Driver> iterator = loader.iterator();
			while(iterator.hasNext())
			{
				Driver next = iterator.next();
				if (next.getClass().getSimpleName().startsWith("Driver"))
				{
					driver = next;
				}
			}
			
			if (driver == null)
			{
				throw new Exception("Jar file '" + jarName + "' does not contain SQL Driver.");
			}
			
			this.drivers.put(sql, driver);
			DriverManager.registerDriver(driver);
		}
		return driver;
	}
	
	private Configuration configuration = null;
	
	private Map<String, Driver> drivers = null;
}
