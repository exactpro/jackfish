////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.SerializablePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.rmi.RemoteException;
import java.rmi.ServerException;
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
    public int reconnect(Map<String, String> parameters) throws Exception
    {
        this.process = null;
        int pid = this.service.connect(parameters);
		this.service.setPluginInfo(this.factory.getInfo());
		return pid;
    }

	@Override
	public SerializablePair<Integer, Integer> connect(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		return startOrConnect(false, startPort, jar, work, remoteClassName, driverParameters, parameters);
	}

	@Override
	public SerializablePair<Integer, Integer> start(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		return startOrConnect(true, startPort, jar, work, remoteClassName, driverParameters, parameters);
	}

	public SerializablePair<Integer,Integer> startOrConnect(boolean start, int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
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

		this.addToClassPath(classPath, parameters);

		add(commandLine, "-cp");
		add(commandLine, classPath.toString());
		add(commandLine, RemoteApplication.class.getName());
		add(commandLine, remoteClassName);
		add(commandLine, String.valueOf(startPort));
		
		System.out.println(commandLine);

		//command need be like this
		//java -cp jar.jar:another1.jar:another2.jar com.exactprosystems.jf.api.app.RemoteApplication remoteClassName port
		//		classpath								mainclass										arguments
		// launch the process
		Redirect errOutput = Redirect.appendTo(new File("remote_out.txt"));

		ProcessBuilder builder = new ProcessBuilder(commandLine);
		builder
			.redirectError(errOutput)
			.directory(workDir);


		this.process = builder.start();

		int port = 0;
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(this.process.getInputStream())))
		{
			String outPort;
			while ((outPort = reader.readLine()) != null)
			{
				if (outPort.startsWith(RemoteApplication.CONNECTED_PORT))
				{
					port = Integer.parseInt(outPort.split(RemoteApplication.CONNECTED_PORT_DELIMITER)[1]);
					break;
				}
			}
		}


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
	    	
	    	Thread.sleep(1000 + attempt * 100);
	    }
        if (this.service != null)
	    {
	    	try
	    	{
				this.service.createLogger(remoteLog, remoteLogLevel, remoteLogPattern);
                int pid = start ? this.service.run(parameters) : this.service.connect(parameters);
                this.service.setPluginInfo(this.factory.getInfo());
                return new SerializablePair<>(pid, port);
	    	}
	    	catch (ServerException se) {
				tryStop();
				throw new Exception((se.getCause().getMessage()), se.getCause());
			}
	    	catch (Throwable t)
	    	{
				tryStop();
				throw t;
	    	}
	    }
	    else
	    {
	    	stop(false);
	    	if (lastException != null)
	    	{
	    		throw lastException;
	    	}
	    	throw new Exception("The service can not start.");
	    }
	}

	private void tryStop(){
		try
		{
			stop(false);
		}
		catch (Exception e)
		{

		}
	}

	
	@Override
	public void stop(boolean needKill) throws Exception
	{
		if (this.service != null)
		{
			try
			{
				this.service.stop(needKill);
			}
			catch(RemoteException e)
			{
				// if app was closed earlier, go to finally
			}
			finally
			{
				this.service = null;
			}
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

	protected void addToClassPath(StringBuilder sb, Map<String, String> parameters)
	{

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
