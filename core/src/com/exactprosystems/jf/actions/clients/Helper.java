////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.clients;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.client.*;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

import java.util.List;

public class Helper
{
	private Helper()
	{}

	public static void clientsNames(List<ReadableValue> list, Context context)
	{
		context.getConfiguration().getClientPool().clientNames()
				.stream()
				.map(context.getEvaluator()::createString)
				.map(ReadableValue::new)
				.forEach(list::add);
	}
	
	public static IClientFactory getFactory(Matrix matrix, Context context, Parameters parameters, String clientName, String connectionName) throws Exception
	{
		try
		{
			parameters.evaluateAll(context.getEvaluator());
		}
		catch (Exception e)
		{
			// nothing to do
		}

		if (clientName != null)
		{
			try
			{
		 		Object client = parameters.get(clientName);
				if (client != null)
				{
					return context.getConfiguration().getClientPool().loadClientFactory(client.toString());
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
				if (connection instanceof ClientConnection)
				{
					return ((ClientConnection)connection).getClient().getFactory();
				}
			}
			catch (Exception e)
			{
				// nothing to do
			}
		
		}
		
		IClientFactory factory = matrix.getDefaultClient();
		if (factory == null)
		{
			throw new Exception("Choose default client at first.");
		}
		
		return factory;
	}
	
	public static void messageTypes(List<ReadableValue> list, Matrix matrix, Context context, Parameters parameters, String clientName, String connectionName) throws Exception
	{
		IClientFactory factory = getFactory(matrix, context, parameters, clientName, connectionName);
		AbstractEvaluator evaluator = context.getEvaluator();
		IMessageDictionary dic = factory.getDictionary();
		dic.getMessages().stream()
				.map(message -> new ReadableValue(evaluator.createString(message.getName())))
				.forEach(list::add);
	}

	public static void messageValues(List<ReadableValue> list, Context context, Matrix matrix, Parameters parameters, String clientName, String connectionName, String messageTypeName, String fieldName) throws Exception
	{
		IClientFactory factory = getFactory(matrix, context, parameters, clientName, connectionName);
		AbstractEvaluator evaluator = context.getEvaluator();
		IMessageDictionary dic = factory.getDictionary();
		String messageType = Str.asString(parameters.get(messageTypeName));
		IMessage message = dic.getMessage(messageType);
		if (message == null)
		{
			throw new Exception("The message with message type='" + messageType + "' is unknown.");
		}
		IField field = message.getDeepField(fieldName);
		if (field == null)
		{
			throw new Exception("The field with name='" + fieldName + "' is unknown.");
		}
		
		Class<?> type = field.getType().getJavaClass();
		boolean needQuotes = type != null && (type == String.class || type == Character.class);
		if (type == Boolean.class)
		{
			list.add(ReadableValue.TRUE);
			list.add(ReadableValue.FALSE);
		}
		for(IAttribute attr : field.getValues())
		{
			String str = attr.getValue();
			if (needQuotes)
			{
				str = evaluator.createString(str);
			}

			if (!Str.IsNullOrEmpty(str))
			{
				list.add(new ReadableValue(str, attr.getName()));
			}
		}
	}

	public static void helpToAddParameters(List<ReadableValue> list, ParametersKind kind, Context context, Matrix matrix, Parameters parameters, String clientName, String connectionName, String messageTypeName) throws Exception
	{
		IClientFactory factory = getFactory(matrix, context, parameters, clientName, connectionName);
		IMessageDictionary dic = factory.getDictionary();
		
		for (String arg : factory.wellKnownParameters(kind))
		{
			list.add(new ReadableValue(arg));
		}
		
		if (messageTypeName != null)
		{
			String messageType = Str.asString(parameters.get(messageTypeName));
			if (!Str.areEqual(messageType, "*"))
			{
				IMessage message = dic.getMessage(messageType);
				if (message == null)
				{
					throw new Exception("The message with message type='" + messageType + "' is unknown.");
				}
				for (IField field : message.getFields())
				{
					list.add(new ReadableValue(field.getName()));
				}
			}
		}
	}
	
	public static boolean canFillParameter(Matrix matrix, Context context, Parameters parameters, String idName, String connectionName, String parameterName) throws Exception
	{
		IClientFactory factory = getFactory(matrix, context, parameters, idName, connectionName);
		IMessageDictionary dic = factory.getDictionary();
		IField field = dic.getField(parameterName);
		if (field == null)
		{
			return false;
		}
		return  field.getType().getJavaClass() == Boolean.class || field.getValues() != null && !field.getValues().isEmpty();
	}

}
