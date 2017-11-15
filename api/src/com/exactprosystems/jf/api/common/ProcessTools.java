////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
