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
import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Entry;
import com.exactprosystems.jf.documents.config.Parameter;
import com.exactprosystems.jf.documents.config.ServiceEntry;
import org.apache.log4j.Logger;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class ServicePool implements IServicesPool
{
	public ServicePool(DocumentFactory factory)
	{
		this.factory = factory;
		this.serviceFactories = new HashMap<>();
		this.serviceStatusMap = new HashMap<>();
		this.connections = new ConcurrentSkipListSet<>(Comparator.comparingInt(ServiceConnection::hashCode));
	}

	@Override
	public boolean isSupported(String serviceId)
	{
		try
		{
			ServiceEntry entry = parametersEntry(serviceId);
			IServiceFactory serviceFactory = loadServiceFactory(serviceId, entry);
			return serviceFactory != null;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}

		return false;
	}

	@Override
	public List<String> servicesNames()
	{
		return this.factory.getConfiguration()
				.getServiceEntries()
				.stream()
				.map(Entry::toString)
				.filter(Objects::nonNull)
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
		ServiceEntry entry = parametersEntry(serviceId);
		return loadServiceFactory(serviceId, entry);
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
			
			ServiceEntry entry = parametersEntry(serviceId);
			IServiceFactory serviceFactory = loadServiceFactory(serviceId, entry);
			List<Parameter> list = entry.getParameters();
			Map<String, String> map = list.stream().collect(Collectors.toMap(Parameter::getKey, parameter -> MainRunner.makeDirWithSubstitutions(parameter.getValue())));
			IService service = serviceFactory.createService();
			service.init(this, serviceFactory, map);
			ServiceConnection connection = new ServiceConnection(service, serviceId);
			this.connections.add(connection);

			return connection;
		}
		catch (Throwable t)
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

	//----------------------------------------------------------------------------------------------
	
	private ServiceEntry parametersEntry(String serviceId) throws ServiceException
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
				String jarName = MainRunner.makeDirWithSubstitutions(entry.get(Configuration.serviceJar));

				List<URL> urls = new ArrayList<>();
				urls.add(new URL("file:" + jarName));

				ClassLoader parent = getClass().getClassLoader();
				URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[]{}), parent);

				ServiceLoader<IServiceFactory> loader = ServiceLoader.load(IServiceFactory.class, classLoader);
				Iterator<IServiceFactory> iterator = loader.iterator();
				if (iterator.hasNext())
				{
					serviceFactory = iterator.next();
					this.serviceFactories.put(serviceId, serviceFactory);
				}
				if (serviceFactory == null)
				{
					throw new ServiceException(String.format(R.SERVICE_POOL_NOT_FOUND.get(), serviceId));
				}

				this.serviceFactories.put(serviceId, serviceFactory);
			}
			catch (ServiceException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new ServiceException(e.getMessage(), e);
			}
		}
		
		return serviceFactory;
	}

	private Map<ServiceConnection, ServiceStatus> serviceStatusMap;
	private DocumentFactory factory;
	private Map<String, IServiceFactory> serviceFactories;
	private static final Logger logger = Logger.getLogger(ServicePool.class);
	private Set<ServiceConnection> connections;

}
