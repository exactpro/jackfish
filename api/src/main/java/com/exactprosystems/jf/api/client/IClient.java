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

import java.net.Socket;
import java.util.Map;

import com.exactprosystems.jf.api.common.IContext;

public interface IClient
{
	void 		init(IClientsPool pool, IClientFactory factory, int limit, Map<String, String> parameters) throws Exception;
	boolean 	start(IContext context, Map<String, Object> parameters) throws Exception;
	boolean 	connect(IContext context, Socket socket, Map<String, Object> parameters) throws Exception;
	void 		stop() throws Exception;
	
	boolean		isOpen();

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
