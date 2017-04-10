////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.service;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.exception.EmptyParameterException;
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
import java.util.stream.Collectors;

public class ServicePool implements IServicesPool
{
	public ServicePool(DocumentFactory factory)
	{
		this.factory = factory;
		this.serviceFactories = new HashMap<>();
		this.serviceStatusMap = new HashMap<>();
		this.connections = new HashSet<>();
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
	public IServiceFactory loadServiceFactory(String serviceId) throws Exception
	{
		ServiceEntry entry = parametersEntry(serviceId);
		return loadServiceFactory(serviceId, entry);
	}

	@Override
	public synchronized ServiceConnection loadService(String serviceId) throws Exception
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
			throw new Exception(t.getMessage(), t);
		}
	}
	
	@Override
	public synchronized void startService(IContext context, ServiceConnection connection, Map<String, Object> params) throws Exception
	{
		if (connection == null || connection.isStopped())
		{
			throw new Exception("The service " + connection + " is not loaded or already stopped.");
		}
		if (this.serviceStatusMap.get(connection) == ServiceStatus.StartSuccessful)
		{
			throw new Exception(String.format("The service '%s' already started.", connection.getId()));
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
			throw e;
		}
	}

	@Override
	public synchronized void stopService(ServiceConnection connection) throws Exception
	{
		try
		{
			if (connection == null || connection.isStopped())
			{
				throw new Exception("The service " + connection + " is not loaded or already stopped.");
			}
			
			connection.close();
			this.serviceStatusMap.remove(connection);
			this.connections.remove(connection);
		}
		catch (Exception e)
		{
			logger.error(String.format("Error in stopService(%s)", connection));
			logger.error(e.getMessage(), e);
			throw e;
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
	
	private ServiceEntry parametersEntry(String serviceId) throws Exception
	{
		ServiceEntry entry = this.factory.getConfiguration().getServiceEntry(serviceId);
		if (entry == null)
		{
			throw new Exception("'" + serviceId + "' is not found.");
		}
		
		return entry;
	}
	

	private IServiceFactory loadServiceFactory(String serviceId, ServiceEntry entry) throws Exception
	{
		IServiceFactory serviceFactory = this.serviceFactories.get(serviceId);
		if (serviceFactory == null)
		{
			String jarName	= MainRunner.makeDirWithSubstitutions(entry.get(Configuration.serviceJar));
			
			List<URL> urls = new ArrayList<URL>();
			urls.add(new URL("file:" + jarName));
			
			ClassLoader parent = getClass().getClassLoader();
			URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[] {}), parent);
			
			ServiceLoader<IServiceFactory> loader = ServiceLoader.load(IServiceFactory.class, classLoader);
			Iterator<IServiceFactory> iterator = loader.iterator();
			if(iterator.hasNext())
			{
				serviceFactory = iterator.next();
				this.serviceFactories.put(serviceId, serviceFactory);
			}
			if (serviceFactory == null)
			{
				throw new Exception("The service factory with serviceId '" + serviceId + "' is not found");
			}
			
			this.serviceFactories.put(serviceId, serviceFactory);
		}
		
		return serviceFactory;
	}

	private Map<ServiceConnection, ServiceStatus> serviceStatusMap;
	private DocumentFactory factory;
	private Map<String, IServiceFactory> serviceFactories;
	private static final Logger logger = Logger.getLogger(ServicePool.class);
	private Set<ServiceConnection> connections;

}
