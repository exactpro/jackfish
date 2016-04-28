////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
