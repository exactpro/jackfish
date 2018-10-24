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

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.exceptions.app.ApplicationWasClosedException;

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
			throw new Exception(R.ACTIONS_HELPER_CHOOSE_APP_ERROR.get());
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

	/**
	 * @return IApplication instance from passed AppConnection, if connection is alive
	 * @throws ApplicationWasClosedException if connection was closed ( via ApplicationStop action)
	 */
	public static IApplication getApplication(AppConnection connection) throws ApplicationWasClosedException
	{
		IApplication app = connection.getApplication();
		String id = connection.getId();
		IRemoteApplication service = app.service();
		if (service == null)
		{
			throw new ApplicationWasClosedException(id);
		}
		return app;
	}
}
