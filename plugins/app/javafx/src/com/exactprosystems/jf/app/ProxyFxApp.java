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

public class ProxyFxApp extends ProxyApplication
{
	@Override
	public SerializablePair<Integer, Integer> start(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		tune(driverParameters, parameters);
		SerializablePair<Integer, Integer> start = super.start(startPort, jar, work, remoteClassName, driverParameters, parameters);
		System.out.println("FxApp.start() " + start.getValue() + "  " + Arrays.toString(parameters.values().toArray()));
		return start;
	}

	@Override
	public void stop(boolean needKill) throws Exception
	{
		System.out.println("FxApp.stop()");
		super.stop(needKill);
	}

	@Override
	protected void addToClassPath(StringBuilder sb, Map<String, String> parameters)
	{
		String jarName = parameters.get(FxAppFactory.jarName);
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
		String jreExec = driverParameters.get(FxAppFactory.jreExecName);
		if (!Str.IsNullOrEmpty(jreExec))
		{
			driverParameters.put(ProxyApplication.JREpathName, jreExec);
		}

		String jreArgs = driverParameters.get(FxAppFactory.jreArgsName);
		if (!Str.IsNullOrEmpty(jreArgs))
		{
			driverParameters.put(ProxyApplication.JVMparametersName, jreArgs);
		}

		String logLevel = driverParameters.get(FxAppFactory.logLevel);
		if (!Str.IsNullOrEmpty(logLevel))
		{
			driverParameters.put(ProxyApplication.remoteLogLevelName, logLevel);
		}

		String trimText = driverParameters.get(AbstractApplicationFactory.trimTextName);
		if (!Str.IsNullOrEmpty(trimText))
		{
			parameters.put(AbstractApplicationFactory.trimTextName, Boolean.valueOf(trimText).toString());
		}
	}
}
