////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

import java.util.Arrays;
import java.util.List;

class Helper
{
	private Helper()
	{}

	/**
	 * add to @param <b>list</b> all application names
	 */
	public static void applicationsNames(List<ReadableValue> list, Context context)
	{
		AbstractEvaluator evaluator = context.getEvaluator();
		context.getConfiguration().getApplications()
				.stream()
				.map(evaluator::createString)
				.map(ReadableValue::new)
				.forEach(list::add);
	}
	
	public static IApplicationFactory getFactory(Matrix matrix, Context context, Parameters parameters, String appName, String connectionName) throws Exception
	{
		try
		{
			parameters.evaluateAll(context.getEvaluator());
		}
		catch (Exception e)
		{
			// nothing to do
		}

		if (appName != null)
		{
			try
			{
		 		Object app = parameters.get(appName);
				if (app != null)
				{
					return context.getConfiguration().getApplicationPool().loadApplicationFactory(app.toString());
				}
			}
			catch (Exception e)
			{
				// nothing to do
			}
		}
		
		if (connectionName != null)
		{
			try
			{
		 		Object connection = parameters.get(connectionName);
				if (connection instanceof AppConnection)
				{
					return ((AppConnection)connection).getApplication().getFactory();
				}
			}
			catch (Exception e)
			{
				// nothing to do
			}
		
		}
		
		IApplicationFactory factory = matrix.getDefaultApp();
		if (factory == null)
		{
			throw new Exception("Choose default application at first.");
		}
		
		return factory;
	}

	public static void helpToAddParameters(List<ReadableValue> list, ParametersKind kind, Matrix matrix, Context context, Parameters parameters,
			String idName, String connectionName) throws Exception
	{
		IApplicationFactory factory = getFactory(matrix, context, parameters, idName, connectionName);
		Arrays.stream(factory.wellKnownParameters(kind))
				.map(ReadableValue::new)
				.forEach(list::add);
	}

	public static boolean canFillParameter(Matrix matrix, Context context, Parameters parameters, 
			String idName, String connectionName, String parameterName) throws Exception
	{
		IApplicationFactory factory = getFactory(matrix, context, parameters, idName, connectionName);
		return factory.canFillParameter(parameterName);
	}
	
	public static void fillListForParameter(List<ReadableValue> list, Matrix matrix, Context context, Parameters parameters, 
			String idName, String connectionName, String parameterName) throws Exception
	{
		IApplicationFactory factory = getFactory(matrix, context, parameters, idName, connectionName);
		AbstractEvaluator evaluator = context.getEvaluator();
		Arrays.stream(factory.listForParameter(parameterName))
				.map(evaluator::createString)
				.map(ReadableValue::new)
				.forEach(list::add);
	}
	
}
