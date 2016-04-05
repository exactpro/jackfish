////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;


public abstract class ProxyApplication implements IApplication
{
	public static final String JREpathName 			= "JREpath";
	public static final String JVMparametersName 	= "JVMparameters";
	
	public static final String remoteLogName  		= "remoteLog";
	public static final String remoteLogLevelName	= "remoteLogLevel";
	public static final String remoteLogPatternName= "remoteLogPattern";

	public ProxyApplication()
	{
	}

	@Override
	public void init(IApplicationPool pool, IApplicationFactory factory) throws Exception
	{
		this.pool = pool;
		this.factory = factory;
	}

	@Override
	public boolean connect(int port, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		return startOrConnect(false, port, jar, work, remoteClassName, driverParameters, parameters);
	}

	@Override
	public boolean start(int port, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		return startOrConnect(true, port, jar, work, remoteClassName, driverParameters, parameters);
	}

	public boolean startOrConnect(boolean start, int port, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		String fileSeparator 	= System.getProperty("file.separator");
		
		String javaRuntime  	= driverParameters.get(JREpathName);
		if (javaRuntime == null || javaRuntime.isEmpty())
		{
			javaRuntime  			= System.getProperty("java.home") + fileSeparator + "bin" + fileSeparator + "java";
		}
		
		// find the working directory
		File workDir = null;
		if (work != null)
		{
			workDir = new File(work);
		}

		// compose all command-line parameters to launch another process
		List<String> commandLine = new ArrayList<String>();
		add(commandLine, javaRuntime);
		
		String jvmParameters = driverParameters.get(JVMparametersName);
		StringBuilder classPath = new StringBuilder();
		String separator = System.getProperty("path.separator");
		classPath.append(jar).append(separator);

		if (jvmParameters != null)
		{
			String[] split = jvmParameters.trim().split(" ");
			List<String> additionalParameters = Arrays.asList(split);
			Iterator<String> iterator = additionalParameters.iterator();
			while (iterator.hasNext())
			{
				String next = iterator.next();
				if (next.equals("-cp") || next.equals("-classpath"))
				{
					classPath.append(iterator.next());
				}
				else
				{
					add(commandLine, next);
				}
			}
		}
		add(commandLine, "-cp");
		add(commandLine, classPath.toString());
		add(commandLine, RemoteApplication.class.getName());
		add(commandLine, remoteClassName);
		add(commandLine, String.valueOf(port));
		
		System.err.println(commandLine);

		//command need be like this
		//java -cp jar.jar:another1.jar:another2.jar com.exactprosystems.jf.api.app.RemoteApplication remoteClassName port
		//		classpath								mainclass										arguments
		// launch the process
		Redirect output = Redirect.appendTo(new File("remote_out.txt"));
		
		ProcessBuilder builder = new ProcessBuilder(commandLine);
		builder
			.redirectOutput(output)
			.redirectError(output)
			.directory(workDir);
		
		
		this.process = builder.start();
		
	    Thread.sleep(1000);

		String remoteLog 		= driverParameters.get(remoteLogName);
		String remoteLogLevel 	= driverParameters.get(remoteLogLevelName);
		String remoteLogPattern	= driverParameters.get(remoteLogPatternName);

		remoteLog 			= remoteLog == null ? "remote.log" : remoteLog;
		remoteLogLevel 		= remoteLogLevel == null? "ALL" : remoteLogLevel;
		remoteLogPattern	= remoteLogPattern == null ? "%-5p %d{yyyy-MM-dd HH:mm:ss.SSS} %c{1}:%-3L-%m%n" : remoteLogPattern;
		
	    // connect to the service
	    Exception lastException = null;
	    for (int attempt = 0; attempt < 10; attempt++)
	    {
	    	try
	    	{
				Registry registry = LocateRegistry.getRegistry("127.0.0.1", port);
				this.service = (IRemoteApplication) registry.lookup(serviceName);
						
				if (this.service != null)
				{
					break;
				}
	    	}
	    	catch (Exception e)
	    	{ 
	    		lastException = e;
	    	}
	    	
    		try
    		{
    			this.process.exitValue();
	    		break;
    		}
    		catch (IllegalThreadStateException e)
    		{
    			// nothing to do
    		}
	    	
	    	Thread.sleep(1000 + attempt * 1000);
	    }
        if (this.service != null)
	    {
	    	try
	    	{
				this.service.createLogger(remoteLog, remoteLogLevel, remoteLogPattern);
                if (start)
				{
					this.service.run(parameters);
				}
				else
				{
					this.service.connect(parameters);
				}
                return true;
	    	}
	    	catch (Throwable t)
	    	{
                t.printStackTrace(System.err);
		    	stop();
	    		throw t;
	    	}
	    }
	    else
	    {
	    	stop();
	    	if (lastException != null)
	    	{
	    		throw lastException;
	    	}
	    	throw new Exception("The service can not start.");
	    }
	}

	
	@Override
	public void stop() throws Exception
	{
		if (this.service != null)
		{
			this.service.stop();
			this.service = null;
		}

		Thread.sleep(500);

		if (this.process != null)
		{
			this.process.destroy();
			this.process = null;
		}
	}
	
	@Override
	public IRemoteApplication service()
	{
		return this.service;
	}
	
	@Override
	public IApplicationPool getPool()
	{
		return this.pool;
	}
	
	@Override
	public IApplicationFactory	getFactory()
	{
		return this.factory;
	}

	
	private static void add(List<String> list, String str)
	{
		if (str != null)
		{
			list.add(str);
		}
	}

	private Process process;
	private IApplicationPool pool;
	private IApplicationFactory factory;
	private IRemoteApplication service;
}
