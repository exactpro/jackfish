/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.AbstractApplicationFactory;
import com.exactprosystems.jf.api.app.ConnectionConfiguration;
import com.exactprosystems.jf.api.app.ProxyApplication;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;

import java.util.Arrays;
import java.util.Map;

public class ProxyWinGuiApp extends ProxyApplication
{
	public ProxyWinGuiApp() throws Exception
	{
		if (!isWindows())
		{
			throw new Exception(R.PROXY_WIN_GUI_APP_NEED_WINDOWS.get());
		}
	}
	
	@Override
	public int reconnect(Map<String, String> parameters) throws Exception
	{
		System.out.println("WinGuiApp.reconnect() parameters = " + Arrays.toString(parameters.values().toArray()));
		return super.reconnect(parameters);
	}

	@Override
	public SerializablePair<Integer, Integer> connect(ConnectionConfiguration configuration, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		System.out.println("WinGuiApp.connect() " + configuration + "  " + Arrays.toString(parameters.values().toArray()));
		tune(driverParameters, parameters);
		return super.connect(configuration, driverParameters, parameters);
	}

	@Override
	public SerializablePair<Integer, Integer> start(ConnectionConfiguration configuration, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception
	{
		System.out.println("WinGuiApp.start() " + configuration + "  " + Arrays.toString(parameters.values().toArray()));
		tune(driverParameters, parameters);
		return super.start(configuration, driverParameters, parameters);
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
