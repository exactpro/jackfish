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

package com.exactprosystems.jf.service;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.MatrixConnection;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.service.AbstractTcpServer;
import com.exactprosystems.jf.api.service.Connection;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

public class MatrixService  extends AbstractTcpServer
{
	@Override
	protected void beforeStart(Map<String, Object> parameters)
	{
		this.port 			= Integer.parseInt(Str.asString(parameters.get(MatrixServiceFactory.portName)));
		this.onConnected 	= Str.asString(parameters.get(MatrixServiceFactory.onConnectedName));
	}

	@Override
	protected void connected(IContext context, Socket clientSocket, OutputStream out, InputStream in)
	{
		System.out.println("Connected: " + clientSocket + " " + this.onConnected);

		
		if (Str.IsNullOrEmpty(this.onConnected))
		{
			return;
		}
		
		logger.info("connection accepted: " + clientSocket + " " + this.onConnected);
		
		Connection connection = new Connection(clientSocket, 
				new BufferedWriter(new OutputStreamWriter(out, Charset.forName("UTF-8"))), 
				new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8"))));

		try (	Reader matrixReader 	= new FileReader(this.onConnected) )
		{
            MatrixConnection matrixConnection = context.startMatrix(this.onConnected, matrixReader, connection);
            matrixConnection.join(0);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			try
			{
				clientSocket.close();
			}
			catch (IOException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	protected void disconnected(IContext context)
	{
		logger.info("disconnected");
	}

	@Override
	protected int getPort()
	{
		return this.port;
	}

	private int 			port;
	private String 			onConnected;

	protected static final Logger logger = Logger.getLogger(MatrixService.class);
}
