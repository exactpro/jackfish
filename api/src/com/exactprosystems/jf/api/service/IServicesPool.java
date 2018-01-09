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
	/**
	 * @return a list, which contains all names of all services, which was registered into a configuration
	 */
	List<String> servicesNames();

	/**
	 * @return a copy list of all service connection, which was started
	 */
	List<ServiceConnection> getConnections();

	/**
	 * Load the service factory by passed service id
	 *
	 * @param id the id of service, which should be loaded
	 *
	 * @return a instance of {@link IServiceFactory}
	 *
	 * @throws ServiceException if loaded was failed
	 */
	IServiceFactory loadServiceFactory(String id) throws ServiceException;

	/**
	 * Load a service, which has the passed id.
	 *
	 * @param id the id of service, which should be loaded
	 *
	 * @return a {@link ServiceConnection} object, which will used for manipulate with the loaded service
	 *
	 * @throws ServiceException if loading was failed
	 */
	ServiceConnection loadService(String id) throws ServiceException;

	/**
	 * Start the service
	 *
	 * @param context the context for service
	 * @param connection the service connection, which was produced by {@link IServicesPool#loadService(String)}
	 * @param params the parameter for starting the service
	 *
	 * @throws ServiceException if starting was failed
	 */
	void startService(IContext context, ServiceConnection connection, Map<String, Object> params) throws ServiceException;

	/**
	 * Stop the service
	 *
	 * @param connection the service connection, which was produced by {@link IServicesPool#loadService(String)}
	 *
	 * @throws ServiceException if stopping was failed
	 */
	void stopService(ServiceConnection connection) throws ServiceException;
	/**
	 * Stop all services, which was started and not stopped yet
	 */
	void stopAllServices();

	/**
	 * @return a {@link ServiceStatus} object, which show status of a service, which has the passed id
	 */
	ServiceStatus getStatus(String id);
}

