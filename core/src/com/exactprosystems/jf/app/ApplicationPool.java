////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.ApiVersionInfo;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Configuration.Parameter;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.xml.gui.GuiDictionary;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ApplicationPool implements IApplicationPool
{
	public ApplicationPool(Configuration configuration)
	{
		this.configuration = configuration;
		this.appFactories = new ConcurrentHashMap<>();
		this.connections = new ConcurrentSkipListSet<>((o1, o2) -> Integer.compare(o1.getPort(), o2.getPort()));
	}

	//----------------------------------------------------------------------------------------------
	// PoolVersionSupported
	//----------------------------------------------------------------------------------------------
	@Override
	public int requiredMajorVersion(String id)
	{
		try
		{
			IApplicationFactory applicationFactory = loadFactory(id);
			return applicationFactory.requiredMajorVersion();
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
			IApplicationFactory applicationFactory = loadFactory(id);
			return applicationFactory.requiredMinorVersion();
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
			IApplicationFactory applicationFactory = loadFactory(id);
			return applicationFactory.isSupported(ApiVersionInfo.majorVersion(), ApiVersionInfo.minorVersion());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}

		return false;
	}

	//----------------------------------------------------------------------------------------------
	// IApplicationPool
	//----------------------------------------------------------------------------------------------
	@Override
	public String[] wellKnownParameters(String id) throws Exception
	{
		IApplicationFactory applicationFactory = loadFactory(id);

		return applicationFactory.wellKnownParameters();
	}

	@Override
	public String[] wellKnownStartArgs(String id) throws Exception
	{
		IApplicationFactory applicationFactory = loadFactory(id);

		return applicationFactory.wellKnownStartArgs();
	}

	@Override
	public String[] wellKnownConnectArgs(String id) throws Exception
	{
		IApplicationFactory applicationFactory = loadFactory(id);

		return applicationFactory.wellKnownConnectArgs();
	}

	@Override
	public ControlKind[] supportedControlKinds(String id) throws Exception
	{
		IApplicationFactory applicationFactory = loadFactory(id);

		return applicationFactory.supportedControlKinds();
	}

	@Override
	public boolean canFillParameter(String id, String parameterToFill) throws Exception
	{
		IApplicationFactory applicationFactory = loadFactory(id);
		return applicationFactory.canFillParameter(parameterToFill);
	}

	@Override
	public String[] listForParameter(String id, String parameterToFill) throws Exception
	{
		IApplicationFactory applicationFactory = loadFactory(id);
		return applicationFactory.listForParameter(parameterToFill);
	}

	@Override
	public List<String> appNames()
	{
		List<String> result = new ArrayList<String>();
		for (Configuration.AppEntry entry : this.configuration.getAppEntries())
		{
			String name = null; 
			try
			{
				name = entry.toString();
				result.add(name);
			}
			catch (Exception e)
			{
				logger.error("Error in appNames() name = " + name);
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}	

	@Override
	public IApplicationFactory loadApplicationFactory(String id) throws Exception
	{
		IApplicationFactory applicationFactory = loadFactory(id);
		if (!applicationFactory.isSupported(ApiVersionInfo.majorVersion(), ApiVersionInfo.minorVersion()))
		{
			throwException(id, applicationFactory);
		}
		
		return applicationFactory;
	}

	@Override
	public synchronized AppConnection connectToApplication(String id, Map<String, String> parameters) throws Exception
	{
		try
		{
			if (id == null)
			{
				throw new Exception("id");
			}

			// prepare initial parameters
			Configuration.AppEntry entry = parametersEntry(id);
			int port = firstFreePort(entry);

			Map<String, String> driverParameters = getDriverParameters(entry);
			IApplicationFactory applicationFactory = loadFactory(id, entry);
			if (!applicationFactory.isSupported(ApiVersionInfo.majorVersion(), ApiVersionInfo.minorVersion()))
			{
				throwException(id, applicationFactory);
			}
			IApplication application = applicationFactory.createApplication();
			application.init(this, applicationFactory);
			String remoteClassName = applicationFactory.getRemoteClassName();
			String jarPath = entry.get(Configuration.appJar);
			String work = entry.get(Configuration.appWorkDir);
			jarPath = new File(jarPath).getName();

			application.connect(port, jarPath, work, remoteClassName, driverParameters, parameters);

			return new AppConnection(application, id, port, applicationFactory.getDictionary());
		}
		catch (InterruptedException e)
		{
			throw new InterruptedException(e.getMessage());
		}
		catch (Throwable t)
		{
			logger.error(String.format("Error in connectToApplication(%s)", id));
			logger.error(t.getMessage(), t);
			throw new Exception(t.getMessage(), t);
		}
	}
	
	
	@Override
	public synchronized AppConnection 	startApplication(String id, Map<String, String> parameters) throws Exception
	{
		try
		{
			if (id == null)
			{
				throw new Exception("id");
			}
			
			// prepare initial parameters
			Configuration.AppEntry entry = parametersEntry(id);
			int port = firstFreePort(entry);
			
			Map<String, String> driverParameters = getDriverParameters(entry);
			IApplicationFactory applicationFactory = loadFactory(id, entry);
			if (!applicationFactory.isSupported(ApiVersionInfo.majorVersion(), ApiVersionInfo.minorVersion()))
			{
				throwException(id, applicationFactory);
			}
			IApplication application = applicationFactory.createApplication();
			application.init(this, applicationFactory);
			String remoteClassName = applicationFactory.getRemoteClassName();
			String jarPath = entry.get(Configuration.appJar);
			String work = entry.get(Configuration.appWorkDir);
			jarPath = new File(jarPath).getName();
			
			application.start(port, jarPath, work, remoteClassName, driverParameters, parameters);
			AppConnection connection = new AppConnection(application, id, port, applicationFactory.getDictionary());
			this.connections.add(connection);
			
			return connection;
		}
		catch (InterruptedException e)
		{
			throw new InterruptedException(e.getMessage());
		}
		catch (Throwable t)
		{
			logger.error(String.format("Error in loadApplication(%s)", id));
			logger.error(t.getMessage(), t);
			throw new Exception(t.getMessage(), t);
		}
	}
	
	@Override
	public synchronized void stopApplication(AppConnection connection) throws Exception
	{
		try
		{
			if (connection == null || !connection.isGood())
			{
				throw new Exception("The application " + connection + " is not loaded.");
			}
			
			IApplication app = connection.getApplication();
			app.stop();
			this.connections.remove(connection);
		}
		catch (Exception e)
		{
			logger.error(String.format("Error in stopApplication(%s)", connection));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public synchronized void stopAllApplications() throws Exception
	{
		//java.util.ConcurrentModificationException
		for (AppConnection connection : this.connections)
		{
			try
			{
				stopApplication(connection);
			}
			catch (Exception e)
			{
				logger.error(String.format("Error in stopAllApplications()"));
				logger.error(e.getMessage(), e);
			}
		}
	}
	//----------------------------------------------------------------------------------------------

	private IApplicationFactory loadFactory(String id) throws Exception
	{
		Configuration.AppEntry entry = parametersEntry(id);
		return loadFactory(id, entry);
	}

	private Configuration.AppEntry parametersEntry(String id) throws Exception
	{
		Configuration.AppEntry entry = this.configuration.getAppEntry(id);
		if (entry == null)
		{
			throw new Exception("'" + id + "' is not found.");
		}
		
		return entry;
	}
	
	private IApplicationFactory loadFactory(String id, Configuration.AppEntry entry) throws Exception
	{
		IApplicationFactory applicationFactory = this.appFactories.get(id);
		if (applicationFactory == null)
		{
			String jarName	= entry.get(Configuration.appJar);
			
			List<URL> urls = new ArrayList<URL>();
			urls.add(new URL("file:" + jarName));

			ClassLoader parent = getClass().getClassLoader();
			URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[] {}), parent);
			
			ServiceLoader<IApplicationFactory> loader = ServiceLoader.load(IApplicationFactory.class, classLoader);
			Iterator<IApplicationFactory> iterator = loader.iterator();
			if(iterator.hasNext())
			{
				applicationFactory = iterator.next();
			}
			if (applicationFactory == null)
			{
				throw new Exception("The application factory with id '" + id + "' is not found");
			}
			
			GuiDictionary dictionary = getDictionary(entry);
			applicationFactory.init(dictionary);
			this.appFactories.put(id, applicationFactory);
		}
		
		return applicationFactory;
	}
	private int firstFreePort(Configuration.AppEntry entry) throws Exception
	{
		String startPortStr = entry.get(Configuration.appStartPort);
		int startPort = Integer.parseInt(startPortStr);
		int port = 0;
		boolean ok = false;
		for (int count = 0; count <= 1000; count++)
		{
			port = startPort + count;
			if (available(port))
			{
				ok = true;
				break;
			}
		}
		if (!ok)
		{
			throw new Exception("No one free port in range " + startPort + "-" + (startPort + 1000));
		}
		
		return port;
	}
	
	private GuiDictionary getDictionary(Configuration.AppEntry entry) throws Exception
	{
		String dictionaryName = entry.get(Configuration.appDicPath);
		GuiDictionary dictionary = null;
		if (!Str.IsNullOrEmpty(dictionaryName))
		{
			dictionary = new GuiDictionary(dictionaryName);
	    	try (Reader reader = new FileReader(dictionaryName))
	    	{
	    		dictionary.load(reader);
	    	}
			
			AbstractEvaluator evaluator = this.configuration.createEvaluator();
			dictionary.evaluateAll(evaluator);
		}
		return dictionary;
	}
	
	private Map<String, String> getDriverParameters(Configuration.AppEntry entry) throws Exception
	{
		List<Parameter> list = entry.getParameters();
		Map<String, String> driverParameters = new HashMap<String, String>();
		for (Parameter param : list)
		{
			driverParameters.put(param.get(Configuration.parametersKey), param.get(Configuration.parametersValue));
		}
		return driverParameters;
	}

	private static boolean available(int port)
	{
		try (ServerSocket ss = new ServerSocket(port))
		{
			ss.setReuseAddress(true);
		}
		catch (IOException e)
		{
			return false;
		}

		try (DatagramSocket ds = new DatagramSocket(port))
		{
			ds.setReuseAddress(true);
		}
		catch (IOException e)
		{
			return false;
		}

		return true;
	}
	
	private void throwException(String id, IApplicationFactory applicationFactory) throws Exception
	{
		throw new Exception("Application '" + id + "' needs API no less than " 
				+ applicationFactory.requiredMajorVersion() + "." + applicationFactory.requiredMinorVersion());
	}


	private Configuration configuration;

	private Map<String, IApplicationFactory> appFactories;

	private Set<AppConnection> connections;
	
	private static final Logger logger = Logger.getLogger(ApplicationPool.class);
}
