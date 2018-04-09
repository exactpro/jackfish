/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.client;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.IPool;

import java.util.List;
import java.util.Map;

public interface IClientsPool  extends IPool
{
	List<String> clientNames();
	
	IClientFactory 		loadClientFactory(String id) throws Exception;
	ClientConnection 	loadClient(String id) throws Exception;
	boolean				startClient(IContext context, ClientConnection connection, Map<String, Object> params) throws Exception;
	void 				stopClient(ClientConnection connection) throws Exception;
}

