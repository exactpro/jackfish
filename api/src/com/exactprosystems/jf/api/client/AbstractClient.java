////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.client.ClientException;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractClient implements IClient
{
	
	public AbstractClient()
	{
	} 

	@Override
	public void init(IClientsPool pool, IClientFactory factory, int limit, Map<String, String> parameters) throws Exception
	{
		if (limit > 0)
		{
			this.messages = Collections.synchronizedList(new LimitedArrayList<MapMessage>(limit));
		}
		else
		{
			this.messages = Collections.synchronizedList(new ArrayList<MapMessage>());
		}
		this.pool = pool;
		this.factory = factory;
	}

	
	@Override
	public boolean start(IContext context, Map<String, Object> parameters) throws Exception
	{
		beforeStart(context, parameters);
		
		return mainLoop();
	}

	@Override
	public	boolean connect(IContext context, Socket socket, Map<String, Object> parameters) throws Exception
	{
		beforeConnect(context, socket, parameters);
		
		return mainLoop();
	}

	@Override
	public void stop()  throws Exception
	{
		this.running = false;
		beforeStop();
		if (this.receiveThread != null)
		{
			try
			{
				this.receiveThread.join(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		if (this.heartBeatThread != null)
		{
			try
			{
				this.heartBeatThread.join(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		afterStop();
	}
	
	@Override
	public void setProperties(Map<String, Object> properties)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized String sendMessage(String messageType, Map<String, Object> parameters, boolean check) throws Exception
	{
		this.lastTime = 0;
		ICodec codec = getCodec();
//		codec.tune(messageType, parameters); // TODO posible it is extra
		byte[] data = codec.encode(messageType, parameters);
		return sendMessage(data, check);
	}

	@Override
	public MapMessage getMessage(Map<String, Object> parameters, String messageType, ICondition[] conditions, int timeout, boolean remove) throws Exception
	{
		if (timeout < 0)
		{
			throw new Exception(R.ABSTRACT_CLIENT_TIMEOUT_MUST_BE_POSITIVE.get());
		}

		Condition[] parametersConditions = Condition.convertToCondition(parameters);
		
		long currentTime = System.currentTimeMillis(); 
		long endTime = currentTime + timeout;
		
		while (currentTime <= endTime)
		{
			int size = this.messages.size();
			
			if (size > 0)
			{
				Iterator<MapMessage> iter = this.messages.iterator();
				while(iter.hasNext())
				{
					MapMessage message = iter.next();
					if (ClientHelper.isMatched(message, messageType, conditions) && ClientHelper.isMatched(message, messageType, parametersConditions))
					{
						if (remove)
						{
							iter.remove();
						}
						return message;
					}
				}
			}
			Thread.sleep(100);
			currentTime = System.currentTimeMillis();
		}
		
		return null;
	}

	@Override
	public int countMessages(Map<String, Object> parameters, String messageType, ICondition[] conditions) throws Exception
	{
		Condition[] parametersConditions = Condition.convertToCondition(parameters);

		int count = 0;
		int size = this.messages.size();
		if (size > 0)
		{
			MapMessage[] list = new MapMessage[size];
			for (MapMessage message : this.messages.toArray(list))
			{
				if (ClientHelper.isMatched(message, messageType, conditions) && ClientHelper.isMatched(message, messageType, parametersConditions))
				{
					count++;
				}
			}
		}
		return count;
	}

	@Override
	public int totalMessages() throws Exception
	{
		return this.messages.size();
	}

	@Override
	public void clearMessages()
	{
		this.messages.clear();
	}
	
	@Override
	public IClientsPool 	getPool()
	{
		return this.pool;
	}
	
	@Override
	public IClientFactory 	getFactory()
	{
		return this.factory;
	}

	protected abstract boolean isOk();
	
	protected abstract void beforeStart(IContext context, Map<String, Object> parameters);

	protected abstract void beforeConnect(IContext context, Socket socket, Map<String, Object> parameters);

	protected abstract MapMessage receiveMessage();
	
	protected abstract boolean useHeartBeat();
	
	protected abstract long heartBeatTimeOut();
	
	protected abstract String makeHeartBeat(Map<String, Object> message);

	protected abstract void beforeStop();

	protected abstract void afterStop();

	private boolean mainLoop()
	{
		boolean res = isOk();
		if (res)
		{
			this.running = true;
			this.receiveThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while (running && isOk())
					{
						MapMessage message = receiveMessage();
						if (message != null)
						{
							messages.add(message);
						}
						else
						{
							try
							{
								Thread.sleep(1);
							}
							catch (InterruptedException e)
							{
							}
						}
					}
				}
			});
			this.receiveThread.start();
			
			if (useHeartBeat())
			{
				this.heartBeatThread = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						long timeout = heartBeatTimeOut(); 
						
						while (running && isOk())
						{
							if (lastTime > timeout)
							{
								
								
								Map<String, Object> message = new HashMap<String, Object>();
								String messageType = makeHeartBeat(message);
								try
								{
									sendMessage(messageType, message, false);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
							else
							{
								lastTime++;
								try
								{
									Thread.sleep(1);
								}
								catch (InterruptedException e)
								{
									// nothing to do
								}
							}
						}
					}
				});
				this.heartBeatThread.setDaemon(true);
				this.heartBeatThread.start();
			}
		}
		return res;
	}



	private IClientsPool pool;
	
	private IClientFactory factory;
	
	private volatile long lastTime = 0;
	
	private volatile boolean running = false;
	
	private Thread receiveThread;
	
	private Thread heartBeatThread;

	private volatile List<MapMessage> messages;
}
