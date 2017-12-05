////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.services;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Configuration;

import java.util.List;

public class ActionServiceHelper
{
	private ActionServiceHelper()
	{

	}

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
		
		throw new Exception("Connection is wrong. Service not loaded");
	}

	public static void serviceNames(List<ReadableValue> list, AbstractEvaluator evaluator, Configuration configuration)
	{
		configuration.getServices().stream()
				.map(evaluator::createString)
				.map(ReadableValue::new)
				.forEach(list::add);
	}
}
