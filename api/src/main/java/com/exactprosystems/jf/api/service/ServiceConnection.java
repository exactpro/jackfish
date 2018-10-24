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

package com.exactprosystems.jf.api.service;

public class ServiceConnection implements AutoCloseable
{
	public ServiceConnection(IService connection, String id)
	{
		this.service = connection;
		this.id = id;
	}

	@Override
	public void close() throws Exception
	{
		if (this.service != null)
		{
			this.service.stop();;
		}
		this.service = null;
	}
	
	@Override
	public String toString()
	{
		return ServiceConnection.class.getSimpleName() + "{" + this.id + ":" +hashCode() + "}";
	}
	
	public boolean isStopped()
	{
		return this.service == null;
	}
	
	public IService getService()
	{
		return this.service;
	}

	public String getId()
	{
		return id;
	}

	private IService service;
	
	private String id;
}
