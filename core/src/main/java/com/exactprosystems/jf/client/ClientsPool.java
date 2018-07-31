/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.client;

import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.IClientFactory;
import com.exactprosystems.jf.api.client.IClientsPool;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.exception.EmptyParameterException;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.ClientEntry;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Parameter;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;
import com.exactprosystems.jf.exceptions.client.ClientNotLoadedException;
import org.apache.log4j.Logger;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClientsPool implements IClientsPool
{
	private static final Logger logger = Logger.getLogger(ClientsPool.class);
	private final DocumentFactory             factory;
	private final Map<String, IClientFactory> clientFactories;

	public ClientsPool(DocumentFactory factory)
	{
		this.factory = factory;
		this.clientFactories = new ConcurrentHashMap<>();
	}

	//region PoolVersionSupported
	@Override
	public boolean isSupported(String id)
	{
		try
		{
			return this.loadClientFactory(id) != null;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return false;
	}

	//endregion

	//region IClientPool
	@Override
	public List<String> clientNames()
	{
		return this.factory.getConfiguration().getClientEntries()
				.stream()
				.map(ClientEntry::toString)
				.collect(Collectors.toList());
	}

	@Override
	public IClientFactory loadClientFactory(String id) throws Exception
	{
		ClientEntry entry = this.getEntryById(id);
		return this.loadClientFactory(id, entry);
	}

	@Override
	public ClientConnection loadClient(String id) throws Exception
	{
		try
		{
			if (id == null)
			{
				throw new EmptyParameterException("id");
			}
			
			ClientEntry entry = this.getEntryById(id);
			IClientFactory clientFactory = this.loadClientFactory(id, entry);

			Map<String, String> map = entry.getParameters()
					.stream()
					.collect(Collectors.toMap(Parameter::getKey, p -> MainRunner.makeDirWithSubstitutions(p.getValue())));

			int limit = Integer.parseInt(entry.get(Configuration.clientLimit));
			
			IClient client = clientFactory.createClient();
			client.init(this, clientFactory, limit, map);
			return new ClientConnection(client, id, clientFactory.getDictionary());
		}
		catch (Exception t)
		{
			logger.error(String.format("Error in loadClient(%s)", id));
			logger.error(t.getMessage(), t);
			throw new Exception(t.getMessage(), t);
		}
	}
	
	@Override
	public boolean startClient(IContext context, ClientConnection connection, Map<String, Object> params) throws Exception
	{
		try
		{
			if (connection == null || connection.isBad())
			{
				throw new ClientNotLoadedException(connection);
			}
			
			IClient client = connection.getClient();
			return client.start(context, params);
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
				throw new ClientNotLoadedException(connection);
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

	//endregion

	//region private methods
	private MessageDictionary getDictionary(ClientEntry entry) throws Exception
	{
		String dictionaryName = entry.get(Configuration.clientDictionary);
		MessageDictionary dictionary = null;
		if (!Str.IsNullOrEmpty(dictionaryName))
		{
            dictionary = (MessageDictionary) this.factory.createDocument(DocumentKind.MESSAGE_DICTIONARY, dictionaryName);
	    	try (Reader reader = CommonHelper.readerFromFileName(dictionaryName))
	    	{
	    		dictionary.load(reader);
	    	}
		}
		return dictionary;
	}
	
	private ClientEntry getEntryById(String id) throws Exception
	{
		return Optional.ofNullable(this.factory.getConfiguration().getClientEntry(id))
				.orElseThrow(() -> new Exception(id + R.COMMON_IS_NOT_FOUND.get()));
	}

	private IClientFactory loadClientFactory(String id, ClientEntry entry) throws Exception
	{
		IClientFactory clientFactory = this.clientFactories.get(id);
		if (clientFactory == null)
		{
			String jarName	= MainRunner.makeDirWithSubstitutions(entry.get(Configuration.clientJar)); 
			clientFactory = CommonHelper.loadFactory(this.getClass()
					,IClientFactory.class
					, jarName
					, () -> new Exception(String.format(R.COMMON_CLIENT_FACTORY_IS_NOT_FOUND.get(), id))
					, logger);
			
			MessageDictionary dictionary = getDictionary(entry);
			clientFactory.init(dictionary);

			this.clientFactories.put(id, clientFactory);
		}
		return clientFactory;
	}

	//endregion
}
