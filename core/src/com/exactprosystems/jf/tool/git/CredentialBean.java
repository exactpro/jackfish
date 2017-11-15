////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git;

public class CredentialBean
{
	private String username;
	private String password;
	private String pathToRsa;
	private String pathToHosts;

	public CredentialBean(String username, String password, String pathToRsa, String pathToHosts)
	{
		this.username = username;
		this.password = password;
		this.pathToRsa = pathToRsa;
		this.pathToHosts = pathToHosts;
	}

	public CredentialBean()
	{
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getPathToRsa()
	{
		return pathToRsa;
	}

	public void setPathToRsa(String pathToRsa)
	{
		this.pathToRsa = pathToRsa;
	}

	public String getPathToHosts()
	{
		return pathToHosts;
	}

	public void setPathToHosts(String pathToHosts)
	{
		this.pathToHosts = pathToHosts;
	}
}
