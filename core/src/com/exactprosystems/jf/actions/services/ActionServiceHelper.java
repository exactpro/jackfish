////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.services;

import java.util.List;

import org.apache.log4j.Logger;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Configuration;

public class ActionServiceHelper
{
	public static ServiceConnection checkConnection(Object parameter) throws Exception
	{
		if (parameter == null)
		{
			throw new Exception("Connection is null.");
		}
		
		if (parameter instanceof ServiceConnection)
		{
			return (ServiceConnection)parameter;
		}
		
		throw new Exception("Connection is wrong");
	}

	public static void serviceNames(List<ReadableValue> list, AbstractEvaluator evaluator, Configuration configuration) throws Exception
	{
		for (String str : configuration.getServices())
		{
			String quoted = evaluator.createString(str);
			list.add(new ReadableValue(quoted));
		}
	}


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ActionServiceHelper.class);
}
