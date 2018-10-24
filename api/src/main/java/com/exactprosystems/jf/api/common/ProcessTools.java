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

package com.exactprosystems.jf.api.common;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;

public class ProcessTools
{

	public static int processId(Process process)
	{
		try
		{
			Method pidMethod = process.getClass().getDeclaredMethod("getProcessId");
			pidMethod.setAccessible(true);
			return (Integer) pidMethod.invoke(process);
		} 
		catch (Exception e)
		{
			return -1;
		}
	}
	
	public static int currentProcessId()
	{
		try
		{
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
			jvm.setAccessible(true);
			Object mgmt = jvm.get(runtime); // recheck it
			Method pidMethod = mgmt.getClass().getDeclaredMethod("getProcessId");
			pidMethod.setAccessible(true);
			return (Integer) pidMethod.invoke(mgmt);
		} 
		catch (Exception e)
		{
			return -1;
		}
	}
}
