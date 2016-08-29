////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.client;

import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.IClientFactory;
import com.exactprosystems.jf.api.client.IClientsPool;
import com.exactprosystems.jf.api.common.ApiVersionInfo;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.config.ClientEntry;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Parameter;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;

import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientsPool implements IClientsPool
{
	public ClientsPool(DocumentFactory factory)
	{
		this.factory = factory;
		this.clientFactories = new ConcurrentHashMap<>();
	}

	//----------------------------------------------------------------------------------------------
	// PoolVersionSupported
	//----------------------------------------------------------------------------------------------
	@Override
	public int requiredMajorVersion(String id)
	{
		try
		{
			ClientEntry entry = parametersEntry(id);
			IClientFactory clientFactory = loadClientFactory(id, entry);
			return clientFactory.requiredMajorVersion();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return -1;
	}

	@Override
	public int requiredMinorVersion(String id)
	{
		try
		{
			ClientEntry entry = parametersEntry(id);
			IClientFactory clientFactory = loadClientFactory(id, entry);
			return clientFactory.requiredMinorVersion();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return -1;
	}

	@Override
	public boolean isSupported(String id)
	{
		try
		{
			ClientEntry entry = parametersEntry(id);
			IClientFactory clientFactory = loadClientFactory(id, entry);
			boolean ret = clientFactory.isSupported(ApiVersionInfo.majorVersion(), ApiVersionInfo.minorVersion());
			return ret;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return false;
	}

	//----------------------------------------------------------------------------------------------
	// IClientPool
	//----------------------------------------------------------------------------------------------
	@Override
	public List<String> clientNames()
	{
		List<String> result = new ArrayList<String>();
		for (ClientEntry entry : this.factory.getConfiguration().getClientEntries())
		{
			String name = null; 
			try
			{
				name = entry.toString();
				result.add(name);
			}
			catch (Exception e)
			{
				logger.error("Error in clientNames() name = " + name);
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}	

	@Override
	public IClientFactory loadClientFactory(String id) throws Exception
	{
		ClientEntry entry = parametersEntry(id);
		IClientFactory clientFactory = loadClientFactory(id, entry);
		if (!clientFactory.isSupported(ApiVersionInfo.majorVersion(), ApiVersionInfo.minorVersion()))
		{
			throwException(id, clientFactory);
		}
		return clientFactory;
	}

	@Override
	public ClientConnection loadClient(String id) throws Exception
	{
		try
		{
			if (id == null)
			{
				throw new Exception("id");
			}
			
			ClientEntry entry = parametersEntry(id);
			IClientFactory clientFactory = loadClientFactory(id, entry);
			if (!clientFactory.isSupported(ApiVersionInfo.majorVersion(), ApiVersionInfo.minorVersion()))
			{
				throwException(id, clientFactory);
			}

			List<Parameter> list = entry.getParameters();
			Map<String, String> map = new HashMap<String, String>();
			for (Parameter param : list)
			{
				map.put(param.getKey(), param.getValue());
			}

			int limit = Integer.parseInt(entry.get(Configuration.clientLimit));
			
			IClient client = clientFactory.createClient();
			client.init(this, clientFactory, limit, map);
			return new ClientConnection(client, id, clientFactory.getDictionary());
		}
		catch (Throwable t)
		{
			logger.error(String.format("Error in loadClient(%s)", id));
			logger.error(t.getMessage(), t);
			throw new Exception(t.getMessage(), t);
		}
	}
	
	@Override
	public void startClient(IContext context, ClientConnection connection, Map<String, Object> params) throws Exception
	{
		try
		{
			if (connection == null || connection.isBad())
			{
				throw new Exception("The client " + connection + " is not loaded.");
			}
			
			IClient client = connection.getClient();
			client.start(context, params);
		}
		catch (Exception e)
		{
			logger.error(String.format("Error in startClient(%s)", connection));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void stopClient(ClientConnection connection) throws Exception
	{
		try
		{
			if (connection == null || connection.isBad())
			{
				throw new Exception("The client " + connection + " is not loaded.");
			}
			
			IClient client = connection.getClient();
			client.stop();
		}
		catch (Exception e)
		{
			logger.error(String.format("Error in stopClient(%s)", connection));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	//----------------------------------------------------------------------------------------------
	private MessageDictionary getDictionary(ClientEntry entry) throws Exception
	{
		String dictionaryName = entry.get(Configuration.clientDictionary);
		MessageDictionary dictionary = null;
		if (!Str.IsNullOrEmpty(dictionaryName))
		{
			dictionary = this.factory.createClientDictionary(dictionaryName);
	    	try (Reader reader = new FileReader(dictionaryName))
	    	{
	    		dictionary.load(reader);
	    	}
		}
		return dictionary;
	}
	
	private ClientEntry parametersEntry(String id) throws Exception
	{
		ClientEntry entry = this.factory.getConfiguration().getClientEntry(id);
		if (entry == null)
		{
			throw new Exception("'" + id + "' is not found.");
		}
		
		return entry;
	}

	private IClientFactory loadClientFactory(String id, ClientEntry entry) throws Exception
	{
		IClientFactory clientFactory = this.clientFactories.get(id);
		if (clientFactory == null)
		{
			String jarName	= MainRunner.makeDirWithSubstitutions(entry.get(Configuration.clientJar)); 
			
			
			List<URL> urls = new ArrayList<URL>();
			urls.add(new URL("file:" + jarName));
			
			ClassLoader parent = getClass().getClassLoader();
			URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[] {}), parent);
			
			ServiceLoader<IClientFactory> loader = ServiceLoader.load(IClientFactory.class, classLoader);
			Iterator<IClientFactory> iterator = loader.iterator();
			if(iterator.hasNext())
			{
				clientFactory = iterator.next();
				this.clientFactories.put(id, clientFactory);
			}
			if (clientFactory == null)
			{
				throw new Exception("The client factory with id '" + id + "' is not found");
			}
			MessageDictionary dictionary = getDictionary(entry);
			clientFactory.init(dictionary);
			this.clientFactories.put(id, clientFactory);
		}
		return clientFactory;
	}
	
	private void throwException(String id, IClientFactory clientFactory) throws Exception
	{
		throw new Exception("Application '" + id + "' needs API no less than " 
				+ clientFactory.requiredMajorVersion() + "." + clientFactory.requiredMinorVersion());
	}
	
	private DocumentFactory factory;

	private Map<String, IClientFactory> clientFactories;
	
	private static final Logger logger = Logger.getLogger(ClientsPool.class);
}
