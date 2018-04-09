/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.AbstractApplicationFactory;
import com.exactprosystems.jf.api.app.ProxyApplication;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.Str;

import java.util.Arrays;
import java.util.Map;

public class ProxySwingApp extends ProxyApplication
{
    @Override
    public int reconnect(Map<String, String> parameters) throws Exception
    {
        System.out.println("SwingApp.reconnect() params=" + Arrays.toString(parameters.values().toArray()));
        return super.reconnect(parameters);
    }

    @Override
	public SerializablePair<Integer, Integer> connect(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		System.out.println("SwingApp.connect() port=" + startPort + "  jar=" + jar + " work=" + work + " class=" + remoteClassName + " params=" + Arrays.toString(parameters.values().toArray()));
		tune(driverParameters, parameters);
		return super.connect(startPort, jar, work, remoteClassName, driverParameters, parameters);
	}

	@Override
	public SerializablePair<Integer, Integer> start(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		System.out.println("SwingApp.start() port=" + startPort + "  jar=" + jar + " work=" + work + " class=" + remoteClassName + " params=" + Arrays.toString(parameters.values().toArray()));
		tune(driverParameters, parameters);
		return super.start(startPort, jar, work, remoteClassName, driverParameters, parameters);
	}

	@Override
	public void stop(boolean needKill) throws Exception
	{
		System.out.println("SwingApp.stop()");
		super.stop(needKill);
	}

	@Override
	protected void addToClassPath(StringBuilder sb, Map<String, String> parameters)
	{
		String jarName = parameters.get(SwingAppFactory.jarName);
		if (!Str.IsNullOrEmpty(jarName))
		{
			String separator = System.getProperty("path.separator");
			if (!sb.toString().endsWith(separator))
			{
				sb.append(separator);
			}
			sb.append(jarName);
		}
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
		String trimText = driverParameters.get(AbstractApplicationFactory.trimTextName);
		if (!Str.IsNullOrEmpty(trimText))
		{
			parameters.put(AbstractApplicationFactory.trimTextName, Boolean.valueOf(trimText).toString());
		}
	}
}
