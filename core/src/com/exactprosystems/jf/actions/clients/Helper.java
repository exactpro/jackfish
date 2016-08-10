////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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

class Helper
{
	public static void clientsNames(List<ReadableValue> list, Context context) throws Exception
	{
		for (String str : context.getConfiguration().getClientPool().clientNames())
		{
			String quoted = context.getEvaluator().createString(str);
			list.add(new ReadableValue(quoted));
		}
	}
	
	public static IClientFactory getFactory(Matrix matrix, Context context, Parameters parameters, 
			String clientName, String connectionName) throws Exception
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
			throw new Exception("Choose default application at first.");
		}
		
		return factory;
	}
	
	public static void messageTypes(List<ReadableValue> list, Matrix matrix, Context context, Parameters parameters, 
			String clientName, String connectionName) throws Exception
	{
		IClientFactory factory = getFactory(matrix, context, parameters, clientName, connectionName);
		AbstractEvaluator evaluator = context.getEvaluator();
		IMessageDictionary dic = factory.getDictionary();
		for(IMessage message : dic.getMessages())
		{
			IAttribute attr = message.getAttribute("MessageType");
			if (attr != null)
			{
				String quoted = evaluator.createString(attr.getValue());
				list.add(new ReadableValue(quoted, message.getName()));
			}
		}
	}

	public static void messageValues(List<ReadableValue> list, Context context, Matrix matrix, Parameters parameters, 
			String clientName, String connectionName, String messageTypeName, String fieldName) throws Exception
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
		String fieldType = Str.asString(parameters.get(fieldName));
		IField field = message.getField(fieldType);
		if (field == null)
		{
			throw new Exception("The field with name='" + fieldName + "' is unknown.");
		}
		
		Object ref = field.getReference();
		if (ref != null && ref instanceof IField)
		{
			field = (IField)ref;
		}

		Class<?> type = field.getType().getJavaClass();
		
		boolean needQuotes = type != null && (type == String.class || type == Character.class);
		
		for (IAttribute value : field.getValues())
		{
			String str = value.getValue();
			if (needQuotes)
			{
				str = evaluator.createString(str);
			}

			if (!Str.IsNullOrEmpty(str))
			{
				list.add(new ReadableValue(str, value.getName()));
			}
		}
	}

	public static void helpToAddParameters(List<ReadableValue> list, ParametersKind kind, Context context, Matrix matrix, Parameters parameters, 
			String clientName, String connectionName, String messageTypeName) throws Exception
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
	
	public static boolean canFillParameter(Matrix matrix, Context context, Parameters parameters, 
			String idName, String connectionName, String parameterName) throws Exception
	{
		IClientFactory factory = getFactory(matrix, context, parameters, idName, connectionName);
		return factory.canFillParameter(parameterName);
	}
	

}
