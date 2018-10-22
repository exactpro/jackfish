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
