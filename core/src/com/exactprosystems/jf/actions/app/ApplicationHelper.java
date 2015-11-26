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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;

public class ApplicationHelper
{
	public static AppConnection checkConnection(AppConnection connection, Object parameter)
	{
		if (connection != null)
		{
			return connection;
		}
		
		if (parameter instanceof AppConnection)
		{
			return (AppConnection)parameter;
		}
		
		return null;
	}
	
	public static void applicationsNames(List<ReadableValue> list, Context context) throws Exception
	{
		AbstractEvaluator evaluator = context.getConfiguration().getEvaluator();
		Configuration configuration = context.getConfiguration();
		for (String str : configuration.getApplications())
		{
			String quoted = evaluator.createString(str);
			list.add(new ReadableValue(quoted));
		}
	}
	
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters, String idName) throws Exception
	{
		parameters.evaluateAll(context.getConfiguration().getEvaluator());
		for (String str : context.getConfiguration().getApplicationPool().wellKnownStartArgs(Str.asString(parameters.get(idName))))
		{
			list.add(new ReadableValue(str));
		}
	}
	
	
	public static boolean canFillParameter(Parameters parameters, Context context, String idName, String fieldName) throws Exception
	{
		parameters.evaluateAll(context.getConfiguration().getEvaluator());
		return context.getConfiguration().getApplicationPool().canFillParameter(Str.asString(parameters.get(idName)), fieldName);
	}
	
	public static void fillListForParameter(List<ReadableValue> list, Context context, Parameters parameters, String idName,  String parameterName) throws Exception
	{
		AbstractEvaluator evaluator = context.getConfiguration().getEvaluator();
		parameters.evaluateAll(evaluator);
		String[] arr = context.getConfiguration().getApplicationPool().listForParameter(Str.asString(parameters.get(idName)), parameterName);
		for (String str : arr)
		{
			String quoted = evaluator.createString(str);
			list.add(new ReadableValue(quoted));
		}
	}
	

}
