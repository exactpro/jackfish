////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.service;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.IPool;

import java.util.List;
import java.util.Map;

public interface IServicesPool  extends IPool
{
	List<String> servicesNames();
	List<ServiceConnection> getConnections();
	IServiceFactory loadServiceFactory(String id) throws Exception;
	ServiceConnection loadService(String id) throws Exception;
	void startService(IContext context, ServiceConnection connection, Map<String, Object> params) throws Exception;
	void stopService(ServiceConnection connection) throws Exception;
	void stopAllServices();

	ServiceStatus getStatus(String id);
}

