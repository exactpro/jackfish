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

import com.exactprosystems.jf.api.common.IContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractTcpServer implements IService
{

	@Override
	public void init(IServicesPool pool, IServiceFactory factory, Map<String, String> parameters) throws Exception
	{
		this.list = new ArrayList<SocketThread>();
		this.pool = pool;
		this.factory = factory;
	}

	@Override
	public boolean start(final IContext context, Map<String, Object> parameters) throws Exception
	{
		this.running = true;

		beforeStart(parameters);

		try
		{
			this.serverSocket = new ServerSocket(getPort());
			this.serviceThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while (running)
					{
						try
						{
							final Socket clientSocket = serverSocket.accept();
							final OutputStream out 	= clientSocket.getOutputStream();
							final InputStream in 	= clientSocket.getInputStream();

							Thread clientThread = new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									connected(context.createCopy(), clientSocket, out, in);
								}
							});
							synchronized (monitor)
							{
								list.add(new SocketThread(clientSocket, clientThread));
							}
							clientThread.setDaemon(true);
							clientThread.start();
						}
						catch (IOException e)
						{
							running = false;
							break;
						}
					}
					
					disconnected(context);
				}

			});
			serviceThread.setDaemon(true);
			serviceThread.start();
		}
		catch (BindException e)
		{
			return false;
		}
		return true;
	}

	@Override
	public void stop() throws Exception
	{
		this.running = false;
		
		if (this.serverSocket != null)
		{
			this.serverSocket.close();
			this.serverSocket = null;
		}
		synchronized (monitor)
		{
			for (SocketThread st : this.list)
			{
				st.socket.close();
				st.thread.join(1000);
			}
		}

		if (this.serviceThread != null)
		{
			this.serviceThread.join();
			this.serviceThread = null;
		}
	}

	@Override
	public final IServicesPool getPool()
	{
		return this.pool;
	}

	@Override
	public final IServiceFactory getFactory()
	{
		return this.factory;
	}

	protected synchronized void onFinished()
	{
		Thread current = Thread.currentThread();
		Iterator<SocketThread> iter = this.list.iterator();
		while(iter.hasNext())
		{
			SocketThread element = iter.next();

			if (current.equals(element.thread))
			{
				iter.remove();
				try
				{
					element.socket.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	protected boolean isRunning()
	{
		return this.running;
	}
	
	protected abstract void connected(IContext context, Socket socket, OutputStream out, InputStream in);

	protected void disconnected(IContext context) {}

	protected abstract void beforeStart(Map<String,Object> parameters);

	protected abstract int getPort();
	
	

	private class SocketThread
	{
		public SocketThread(Socket socket, Thread thread)
		{
			this.socket = socket;
			this.thread = thread;
		}

		public Socket socket;
		public Thread thread;
	}

	final private Object monitor = new Object();
	private IServicesPool pool;
	private IServiceFactory factory;
	private List<SocketThread> list;
	private ServerSocket serverSocket = null;
	private Thread serviceThread = null;
	private volatile boolean running = false;
}
