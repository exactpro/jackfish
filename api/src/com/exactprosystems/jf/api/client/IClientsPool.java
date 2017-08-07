////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import java.util.List;
import java.util.Map;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.IPool;

public interface IClientsPool  extends IPool
{
	List<String> clientNames();
	
	IClientFactory 		loadClientFactory(String id) throws Exception;
	ClientConnection 	loadClient(String id) throws Exception;
	void 				startClient(IContext context, ClientConnection connection, Map<String, Object> params) throws Exception;
	void 				stopClient(ClientConnection connection) throws Exception;
}

