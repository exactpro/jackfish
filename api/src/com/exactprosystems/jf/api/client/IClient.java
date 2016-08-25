////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import java.net.Socket;
import java.util.Map;

import com.exactprosystems.jf.api.common.IContext;

public interface IClient
{
	void 		init(IClientsPool pool, IClientFactory factory, int limit, Map<String, String> parameters) throws Exception;
	boolean 	start(IContext context, Map<String, Object> parameters) throws Exception;
	boolean 	connect(IContext context, Socket socket, Map<String, Object> parameters) throws Exception;
	void 		stop() throws Exception;

	void 		setProperties(Map<String, Object> properties);

	String		sendMessage(String messageType, Map<String, Object> parameters, boolean check) throws Exception;
	String		sendMessage(byte[] bytes, boolean check) throws Exception;
	
	MapMessage 	getMessage(Map<String, Object> parameters, String messageType, ICondition[] conditions, int timeout, boolean remove) throws Exception;
	int 		countMessages(Map<String, Object> parameters, String messageType, ICondition[] conditions) throws Exception;
	int 		totalMessages() throws Exception;
	void 		clearMessages();

	ICodec 		getCodec();
	IClientsPool	getPool();
	IClientFactory	getFactory();
}
