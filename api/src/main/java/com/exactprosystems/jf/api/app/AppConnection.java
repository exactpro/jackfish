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

package com.exactprosystems.jf.api.app;

public class AppConnection implements AutoCloseable
{
	public AppConnection(IApplication connection, String id, int port, IApplicationFactory factory, int pid)
	{
		this.app = connection;
		this.id = id;
		this.port = port;
		this.factory = factory;
		this.pid = pid;
	}

	@Override
	public void close() throws Exception
	{
		if (this.app != null)
		{
			this.app.stop(false);
		}
		this.app = null;
	}

	@Override
	public String toString()
	{
		return AppConnection.class.getSimpleName() + "{" + this.id + ":" + hashCode() + "}";
	}

	public String getId()
	{
		return this.id;
	}

	public boolean isGood()
	{
		return this.app != null;
	}

	public IApplication getApplication()
	{
		return this.app;
	}

	public int getPort()
	{
		return this.port;
	}

	public IGuiDictionary getDictionary()
	{
		return this.factory.getDictionary();
	}

	public int getProcessId()
	{
		return this.pid;
	}

    public void setProcessId(int pid)
    {
        this.pid = pid;
    }

	private IApplication app;

	private String id;

	private int port;

	private IApplicationFactory factory;
	
	private int pid;
}
