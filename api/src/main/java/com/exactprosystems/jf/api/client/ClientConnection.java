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

package com.exactprosystems.jf.api.client;

public class ClientConnection implements AutoCloseable
{
	public ClientConnection(IClient connection, String id, IMessageDictionary dictionary)
	{
		this.client = connection;
		this.id = id;
		this.dictionary = dictionary;
	}

	@Override
	public void close() throws Exception
	{
		if (this.client != null)
		{
			this.client.stop();;
		}
		this.client = null;
	}
	
	@Override
	public String toString()
	{
		return ClientConnection.class.getSimpleName() + "{" + this.id + ":" +hashCode() + "}";
	}
	
	public boolean isOpen()
	{
		return this.client != null && this.client.isOpen();
	}
	
	public boolean isBad()
	{
		return this.client == null;
	}

	public String getId()
	{
		return id;
	}

	public IClient getClient()
	{
		return this.client;
	}

	public IMessageDictionary getDictionary()
	{
		return this.dictionary;
	}

	private IClient client;
	
	private String id;
	
	private IMessageDictionary dictionary;

}
