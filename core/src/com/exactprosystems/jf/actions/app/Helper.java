////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import java.util.List;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

public class Helper
{
	public static void applicationsNames(List<ReadableValue> list, Context context) throws Exception
	{
		AbstractEvaluator evaluator = context.getEvaluator();
		Configuration configuration = context.getConfiguration();
		for (String str : configuration.getApplications())
		{
			list.add(new ReadableValue(evaluator.createString(str)));
		}
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

	public static void helpToAddParameters(List<ReadableValue> list, Matrix matrix, Context context, Parameters parameters,
			String idName, String connectionName) throws Exception
	{
		IApplicationFactory factory = getFactory(matrix, context, parameters, idName, connectionName);
		for (String arg : factory.wellKnownProperties())
		{
			list.add(new ReadableValue(arg));
		}
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
		AbstractEvaluator evaluator = context.getEvaluator();
		IApplicationFactory factory = getFactory(matrix, context, parameters, idName, connectionName);
		for (String str : factory.listForParameter(parameterName))
		{
			list.add(new ReadableValue(evaluator.createString(str)));
		}
	}
	
}
