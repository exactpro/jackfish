////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.service;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.IPool;

import java.util.List;
import java.util.Map;

public interface IServicesPool  extends IPool
{
	public List<String> servicesNames();
	
	public String[] 			wellKnownStartArgs(String id) throws Exception;
	
	public IServiceFactory loadServiceFactory(String id) throws Exception;
	public ServiceConnection loadService(String id) throws Exception;
	public void startService(IContext context, ServiceConnection connection, Map<String, Object> params) throws Exception;
	public void stopService(ServiceConnection connection) throws Exception;
	public void stopAllServices();

	public ServiceStatus getStatus(String id);
}

