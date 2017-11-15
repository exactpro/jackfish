////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.AbstractApplicationFactory;
import com.exactprosystems.jf.api.app.ProxyApplication;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.Str;

import java.util.Arrays;
import java.util.Map;

public class ProxyWinGuiApp extends ProxyApplication
{
	public ProxyWinGuiApp() throws Exception
	{
		if (!isWindows())
		{
			throw new Exception("This adapter needs Windows.");
		}
	}
	
	@Override
	public int reconnect(Map<String, String> parameters) throws Exception
	{
		System.out.println("WinGuiApp.reconnect() parameters = " + Arrays.toString(parameters.values().toArray()));
		return super.reconnect(parameters);
	}

	@Override
	public SerializablePair<Integer, Integer> connect(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		System.out.println("WinGuiApp.connect() " + startPort + "  " + Arrays.toString(parameters.values().toArray()));
		tune(driverParameters, parameters);
		return super.connect(startPort, jar, work, remoteClassName, driverParameters, parameters);
	}

	@Override
	public SerializablePair<Integer, Integer> start(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		System.out.println("WinGuiApp.start() " + startPort + "  " + Arrays.toString(parameters.values().toArray()));
		tune(driverParameters, parameters);
		return super.start(startPort, jar, work, remoteClassName, driverParameters, parameters);
	}

	@Override
	public void stop(boolean needKill) throws Exception
	{
		System.out.println("WinGuiApp.stop()");
		super.stop(needKill);
	}

	private void tune(Map<String, String> driverParameters, Map<String, String> parameters)
	{
		String jreExec 	= driverParameters.get(WinAppFactory.jreExecName);
		if (jreExec != null && !jreExec.isEmpty())
		{
			System.out.println(WinAppFactory.jreExecName + "=" + jreExec);
			driverParameters.put(ProxyApplication.JREpathName, jreExec);
		}
		String jreArgs 		= driverParameters.get(WinAppFactory.jreArgsName);
		if (jreArgs != null && !jreArgs.isEmpty())
		{
			System.out.println(WinAppFactory.jreArgsName + "=" + jreArgs);
			driverParameters.put(ProxyApplication.JVMparametersName, jreArgs);
		}
		String maxTimeout = driverParameters.get(WinAppFactory.maxTimeout);
		if (maxTimeout != null && !maxTimeout.isEmpty())
		{
			System.out.println(WinAppFactory.maxTimeout + " = " + maxTimeout);
			parameters.put(WinAppFactory.maxTimeout, maxTimeout);
		}
		String logLevel = driverParameters.get(WinAppFactory.logLevel);
		if (!Str.IsNullOrEmpty(logLevel))
		{
			System.out.println(WinAppFactory.logLevel + "=" + logLevel);
			driverParameters.put(ProxyApplication.remoteLogLevelName, logLevel);
			parameters.put(WinAppFactory.logLevel, logLevel);
		}

		String trimText = driverParameters.get(AbstractApplicationFactory.trimTextName);
		if (!Str.IsNullOrEmpty(trimText))
		{
			parameters.put(AbstractApplicationFactory.trimTextName, Boolean.valueOf(trimText).toString());
		}
	}

	private boolean isWindows()
	{
		String OS = System.getProperty("os.name");
		return OS != null && OS.startsWith("Windows");

	}
}
