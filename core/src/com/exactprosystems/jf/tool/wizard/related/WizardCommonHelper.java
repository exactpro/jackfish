/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.documents.config.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WizardCommonHelper
{
	private WizardCommonHelper()
	{

	}

	public static List<ConnectionBean> getAllConnections(Configuration config)
	{
		Map<String, Object> storeMap = config.getStoreMap();
		//get stored connection
		List<ConnectionBean> list = storeMap.entrySet().stream()
				.filter(entry -> entry.getValue() instanceof AppConnection)
				.map(entry -> new ConnectionBean(entry.getKey(), ((AppConnection) entry.getValue())))
				.collect(Collectors.toList());

		return list;
	}

	public static void shutdownExec(ExecutorService exec)
	{
		if (exec != null)
		{
			exec.shutdown();
			try
			{
				if (!exec.awaitTermination(800, TimeUnit.MILLISECONDS))
				{
					exec.shutdownNow();
				}
			}
			catch (InterruptedException e)
			{
				exec.shutdownNow();
			}
		}
	}

}
