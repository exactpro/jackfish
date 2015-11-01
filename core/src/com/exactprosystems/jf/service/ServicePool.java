////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.service;

import com.exactprosystems.jf.api.common.ApiVersionInfo;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.service.IService;
import com.exactprosystems.jf.api.service.IServiceFactory;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Configuration.Parameter;
import com.exactprosystems.jf.common.Configuration.ServiceEntry;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;

public class ServicePool implements IServicesPool
{
	public ServicePool(Configuration configuration)
	{
		this.configuration = configuration;
		this.serviceFactories = new HashMap<String, IServiceFactory>();
		this.connections = new HashSet<ServiceConnection>();
	}

	//----------------------------------------------------------------------------------------------
	// PoolVersionSupported
	//----------------------------------------------------------------------------------------------
	@Override
	public int requiredMajorVersion(String id)
	{
		try
		{
			Configuration.ServiceEntry entry = parametersEntry(id);
			IServiceFactory serviceFactory = loadServiceFactory(id, entry);
			return serviceFactory.requiredMajorVersion();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}

		return -1;
	}

	@Override
	public int requiredMinorVersion(String id)
	{
		try
		{
			Configuration.ServiceEntry entry = parametersEntry(id);
			IServiceFactory serviceFactory = loadServiceFactory(id, entry);
			return serviceFactory.requiredMinorVersion();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}

		return -1;
	}

	@Override
	public boolean isSupported(String id)
	{
		try
		{
			Configuration.ServiceEntry entry = parametersEntry(id);
			IServiceFactory serviceFactory = loadServiceFactory(id, entry);
			return serviceFactory.isSupported(ApiVersionInfo.majorVersion(), ApiVersionInfo.minorVersion());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}

		return false;
	}

	//----------------------------------------------------------------------------------------------
	// IServicePool
	//----------------------------------------------------------------------------------------------
	@Override
	public boolean canFillParameter(String id, String parameterToFill) throws Exception
	{
		Configuration.ServiceEntry entry = parametersEntry(id);
		IServiceFactory serviceFactory = loadServiceFactory(id, entry);

		return serviceFactory.canFillParameter(parameterToFill);
	}

	@Override
	public String[] listForParameter(String id, String parameterToFill) throws Exception
	{
		Configuration.ServiceEntry entry = parametersEntry(id);
		IServiceFactory serviceFactory = loadServiceFactory(id, entry);

		return serviceFactory.listForParameter(parameterToFill);
	}

	@Override
	public List<String> servicesNames()
	{
		List<String> result = new ArrayList<String>();
		for (ServiceEntry entry : this.configuration.getServiceEntries())
		{
			String name = null; 
			try
			{
				name = entry.toString();
				result.add(name);
			}
			catch (Exception e)
			{
				logger.error("Error in serviceNames() name = " + name);
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}	

	@Override
	public String[] wellKnownParameters(String id) throws Exception
	{
		Configuration.ServiceEntry entry = parametersEntry(id);
		IServiceFactory serviceFactory = loadServiceFactory(id, entry);

		return serviceFactory.wellKnownParameters();
	}


	@Override
	public String[] wellKnownStartArgs(String id) throws Exception
	{
		Configuration.ServiceEntry entry = parametersEntry(id);
		IServiceFactory serviceFactory = loadServiceFactory(id, entry);

		return serviceFactory.wellKnownStartArgs();
	}

	

	@Override
	public IServiceFactory loadServiceFactory(String id) throws Exception
	{
		ServiceEntry entry = parametersEntry(id);
		IServiceFactory serviceFactory = loadServiceFactory(id, entry);
		if (!serviceFactory.isSupported(ApiVersionInfo.majorVersion(), ApiVersionInfo.minorVersion()))
		{
			throwException(id, serviceFactory);
		}
		return serviceFactory;
	}

	@Override
	public synchronized ServiceConnection loadService(String id) throws Exception
	{
		try
		{
			if (id == null)
			{
				throw new Exception("id");
			}
			
			ServiceEntry entry = parametersEntry(id);
			IServiceFactory serviceFactory = loadServiceFactory(id, entry);
			if (!serviceFactory.isSupported(ApiVersionInfo.majorVersion(), ApiVersionInfo.minorVersion()))
			{
				throwException(id, serviceFactory);
			}
			List<Parameter> list = entry.getParameters();
			Map<String, String> map = new HashMap<String, String>();
			for (Parameter param : list)
			{
				map.put(param.get(Configuration.parametersKey), param.get(Configuration.parametersValue));
			}

			IService service = serviceFactory.createService();
			service.init(this, serviceFactory, map);
			ServiceConnection connection = new ServiceConnection(service, id);
			this.connections.add(connection);

			return connection;
		}
		catch (Throwable t)
		{
			logger.error(String.format("Error in loadService(%s)", id));
			logger.error(t.getMessage(), t);
			throw new Exception(t.getMessage(), t);
		}
	}
	
	@Override
	public synchronized void startService(IContext context, ServiceConnection connection, Map<String, Object> params) throws Exception
	{
		try
		{
			if (connection == null || connection.isBad())
			{
				throw new Exception("The service " + connection + " is not loaded.");
			}
			
			IService service = connection.getService();
			service.start(context, params);
		}
		catch (Exception e)
		{
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
			if (connection == null || connection.isBad())
			{
				throw new Exception("The service " + connection + " is not loaded.");
			}
			
			IService service = connection.getService();
			service.stop();
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
	//----------------------------------------------------------------------------------------------
	
	private Configuration.ServiceEntry parametersEntry(String id) throws Exception
	{
		ServiceEntry entry = this.configuration.getServiceEntry(id);
		if (entry == null)
		{
			throw new Exception("'" + id + "' is not found.");
		}
		
		return entry;
	}
	

	private IServiceFactory loadServiceFactory(String id, Configuration.ServiceEntry entry) throws Exception
	{
		IServiceFactory serviceFactory = this.serviceFactories.get(id);
		if (serviceFactory == null)
		{
			String jarName	= entry.get(Configuration.serviceJar);
			
			List<URL> urls = new ArrayList<URL>();
			urls.add(new URL("file:" + jarName));
			
			ClassLoader parent = getClass().getClassLoader();
			URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[] {}), parent);
			
			ServiceLoader<IServiceFactory> loader = ServiceLoader.load(IServiceFactory.class, classLoader);
			Iterator<IServiceFactory> iterator = loader.iterator();
			if(iterator.hasNext())
			{
				serviceFactory = iterator.next();
				this.serviceFactories.put(id, serviceFactory);
			}
			if (serviceFactory == null)
			{
				throw new Exception("The service factory with id '" + id + "' is not found");
			}
			
			this.serviceFactories.put(id, serviceFactory);
		}
		
		return serviceFactory;
	}
	
	private void throwException(String id, IServiceFactory serviceFactory) throws Exception
	{
		throw new Exception("Application '" + id + "' needs API no less than " 
				+ serviceFactory.requiredMajorVersion() + "." + serviceFactory.requiredMinorVersion());
	}
	
	private Configuration configuration;

	private Map<String, IServiceFactory> serviceFactories;
	
	private Set<ServiceConnection> connections;

	private static final Logger logger = Logger.getLogger(ServicePool.class);

}
