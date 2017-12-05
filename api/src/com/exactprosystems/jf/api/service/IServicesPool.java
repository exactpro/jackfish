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
import com.exactprosystems.jf.api.error.service.ServiceException;

import java.util.List;
import java.util.Map;

public interface IServicesPool extends IPool
{
	List<String> servicesNames();
	List<ServiceConnection> getConnections();
	IServiceFactory loadServiceFactory(String id) throws ServiceException;
	ServiceConnection loadService(String id) throws ServiceException;
	void startService(IContext context, ServiceConnection connection, Map<String, Object> params) throws ServiceException;
	void stopService(ServiceConnection connection) throws ServiceException;
	void stopAllServices();

	ServiceStatus getStatus(String id);
}

