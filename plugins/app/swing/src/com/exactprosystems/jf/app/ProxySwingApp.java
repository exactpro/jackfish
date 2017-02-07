////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.ProxyApplication;
import com.exactprosystems.jf.api.common.Str;

import java.util.Arrays;
import java.util.Map;

public class ProxySwingApp extends ProxyApplication
{
	@Override
	public int connect(int port, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		System.out.println("SwingApp.connect() port=" + port + "  jar=" + jar + " work=" + work + " class=" + remoteClassName + " params=" + Arrays.toString(parameters.values().toArray()));
		tune(driverParameters, parameters);
		return super.connect(port, jar, work, remoteClassName, driverParameters, parameters);
	}

	@Override
	public int start(int port, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		System.out.println("SwingApp.start() port=" + port + "  jar=" + jar + " work=" + work + " class=" + remoteClassName + " params=" + Arrays.toString(parameters.values().toArray()));
		tune(driverParameters, parameters);
		return super.start(port, jar, work, remoteClassName, driverParameters, parameters);
	}

	@Override
	public void stop(boolean needKill) throws Exception
	{
		System.out.println("SwingApp.stop()");
		super.stop(needKill);
	}

	private void tune(Map<String, String> driverParameters, Map<String, String> parameters)
	{
		String jreExec 	= driverParameters.get(SwingAppFactory.jreExecName);
		if (jreExec != null && !jreExec.isEmpty())
		{
			System.out.println(SwingAppFactory.jreExecName + "=" + jreExec);
			driverParameters.put(ProxyApplication.JREpathName, jreExec);
		}
		String jreArgs 		= driverParameters.get(SwingAppFactory.jreArgsName);
		if (jreArgs != null && !jreArgs.isEmpty())
		{
			System.out.println(SwingAppFactory.jreArgsName + "=" + jreArgs);
			driverParameters.put(ProxyApplication.JVMparametersName, jreArgs);
		}
		String logLevel = driverParameters.get(SwingAppFactory.logLevel);
		if (!Str.IsNullOrEmpty(logLevel))
		{
			System.out.println(SwingAppFactory.logLevel + "=" + logLevel);
			driverParameters.put(ProxyApplication.remoteLogLevelName, logLevel);
		}
	}
}
