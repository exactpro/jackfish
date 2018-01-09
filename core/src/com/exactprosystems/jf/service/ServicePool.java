////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.service;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.exception.EmptyParameterException;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.service.ServiceException;
import com.exactprosystems.jf.api.service.*;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Entry;
import com.exactprosystems.jf.documents.config.Parameter;
import com.exactprosystems.jf.documents.config.ServiceEntry;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class ServicePool implements IServicesPool
{
	private static final Logger logger = Logger.getLogger(ServicePool.class);

	private final DocumentFactory                       factory;
	/**
	 * A map of service by service status
	 */
	private final Map<ServiceConnection, ServiceStatus> serviceStatusMap;
	/**
	 * The cache for loaded services
	 */
	private final Map<String, IServiceFactory>          serviceFactories;
	private final Set<ServiceConnection>                connections;

	public ServicePool(DocumentFactory factory)
	{
		this.factory = factory;
		this.serviceFactories = new HashMap<>();
		this.serviceStatusMap = new HashMap<>();
		this.connections = new ConcurrentSkipListSet<>(Comparator.comparingInt(ServiceConnection::hashCode));
	}

	//region IPool interface
	@Override
	public boolean isSupported(String serviceId)
	{
		try
		{
			ServiceEntry entry = this.getEntryById(serviceId);
			IServiceFactory serviceFactory = this.loadServiceFactory(serviceId, entry);
			return serviceFactory != null;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}

		return false;
	}
	//endregion

	//region IServicePool interface
	@Override
	public List<String> servicesNames()
	{
		return this.factory.getConfiguration().getServiceEntries()
				.stream()
				.map(Entry::toString)
				.collect(Collectors.toList());
	}

	@Override
	public List<ServiceConnection> getConnections()
	{
		return new ArrayList<>(this.connections);
	}

	@Override
	public IServiceFactory loadServiceFactory(String serviceId) throws ServiceException
	{
		ServiceEntry entry = this.getEntryById(serviceId);
		return this.loadServiceFactory(serviceId, entry);
	}

	@Override
	public synchronized ServiceConnection loadService(String serviceId) throws ServiceException
	{
		try
		{
			if (serviceId == null)
			{
				throw new EmptyParameterException("serviceId");
			}
			
			ServiceEntry entry = this.getEntryById(serviceId);
			IServiceFactory serviceFactory = this.loadServiceFactory(serviceId, entry);
			List<Parameter> list = entry.getParameters();
			Map<String, String> map = list.stream().collect(Collectors.toMap(Parameter::getKey, parameter -> MainRunner.makeDirWithSubstitutions(parameter.getValue())));
			IService service = serviceFactory.createService();
			service.init(this, serviceFactory, map);
			ServiceConnection connection = new ServiceConnection(service, serviceId);
			this.connections.add(connection);

			return connection;
		}
		catch (Exception t)
		{
			logger.error(String.format("Error in loadService(%s)", serviceId));
			logger.error(t.getMessage(), t);
			throw new ServiceException(t.getMessage(), t);
		}
	}
	
	@Override
	public synchronized void startService(IContext context, ServiceConnection connection, Map<String, Object> params) throws ServiceException
	{
		if (connection == null || connection.isStopped())
		{
			throw new ServiceException(String.format(R.SERVICE_POOL_SERVICE_IS_NOT_LOADED.get(), "" + connection));
		}
		if (this.serviceStatusMap.get(connection) == ServiceStatus.StartSuccessful)
		{
			throw new ServiceException(String.format(R.SERVICE_POOL_SERVICE_ALREADY_STARTED.get(), connection.getId()));
		}
		try
		{
			IService service = connection.getService();
			this.serviceStatusMap.remove(connection);
			this.serviceStatusMap.put(connection, ServiceStatus.StartSuccessful);
			service.start(context, params);
		}
		catch (Exception e)
		{
			ServiceStatus startFailed = ServiceStatus.StartFailed;
			startFailed.setMsg(e.getMessage());
			this.serviceStatusMap.replace(connection, startFailed);
			logger.error(String.format("Error in startService(%s)", connection));
			logger.error(e.getMessage(), e);
			throw new ServiceException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void stopService(ServiceConnection connection) throws ServiceException
	{
		try
		{
			if (connection == null || connection.isStopped())
			{
				throw new ServiceException(String.format(R.SERVICE_POOL_SERVICE_IS_NOT_LOADED.get(), "" + connection));
			}
			
			connection.close();
			this.serviceStatusMap.remove(connection);
			this.connections.remove(connection);
		}
		catch (Exception e)
		{
			logger.error(String.format("Error in stopService(%s)", connection));
			logger.error(e.getMessage(), e);
			throw new ServiceException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void stopAllServices()
	{
		for (ServiceConnection connection : this.connections)
		{
			try
			{
				stopService(connection);
			}
			catch (Exception e)
			{
				logger.error(String.format("Error in stopAllServices()"));
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public ServiceStatus getStatus(String serviceId)
	{
		return this.serviceStatusMap.entrySet()
				.stream()
				.filter(e -> e.getKey().getId().equals(serviceId))
				.map(Map.Entry::getValue)
				.findFirst()
				.orElse(ServiceStatus.NotStarted);
	}

	//endregion

	//region private methods
	/**
	 * Get entry by the passed id. If entry not found, will throw {@link ServiceException}
	 */
	private ServiceEntry getEntryById(String serviceId) throws ServiceException
	{
		ServiceEntry entry = this.factory.getConfiguration().getServiceEntry(serviceId);
		if (entry == null)
		{
			throw new ServiceException(serviceId + R.COMMON_IS_NOT_FOUND.get());
		}
		
		return entry;
	}

	private IServiceFactory loadServiceFactory(String serviceId, ServiceEntry entry) throws ServiceException
	{
		IServiceFactory serviceFactory = this.serviceFactories.get(serviceId);
		if (serviceFactory == null)
		{
			try
			{
				String jarName = entry.get(Configuration.serviceJar);
				jarName = MainRunner.makeDirWithSubstitutions(jarName);

				serviceFactory = CommonHelper.loadFactory(this.getClass()
						, IServiceFactory.class
						, jarName
						, () -> new Exception(String.format(R.SERVICE_POOL_NOT_FOUND.get(), serviceId))
						, logger);

				this.serviceFactories.put(serviceId, serviceFactory);
			}
			catch (Exception e)
			{
				throw new ServiceException(e.getMessage(), e);
			}
		}
		
		return serviceFactory;
	}
	//endregion
}
