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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;

import org.apache.log4j.Logger;

import java.util.List;

public class ActionClientHelper
{
	public static HelpKind canHelpWithParameters(Context context, Parameters parameters, String connectionName,  String fieldName)
	{
		parameters.evaluateAll(context.getEvaluator());
		Object value = parameters.get(connectionName);
		if (value instanceof ClientConnection)
		{
			return ((ClientConnection)value).getClient().getFactory().canFillParameter(fieldName) ? HelpKind.ChooseFromList : null;
		}
		return null;
	}
	
	
	public static void listToFillParameterDerived(List<ReadableValue> list, Context context, Parameters parameters, String connectionName, String parameterToFill) 
	{
		parameters.evaluateAll(context.getEvaluator());
		Object value = parameters.get(connectionName);
		if (value instanceof ClientConnection)
		{
			for (String str : ((ClientConnection)value).getClient().getFactory().listForParameter(parameterToFill))
			{
				list.add(new ReadableValue(str));
			}
		}
	}


	
	public static String checkMessageType(String messageType, Object parameter) throws Exception
	{
		if (messageType != null)
		{
			return messageType;
		}
		
		if (parameter != null && parameter instanceof String)
		{
			return (String)parameter;
		}
		
		return null;
	}
		
	public static void messageTypes(List<ReadableValue> list, Context context, Matrix matrix, Parameters parameters, String connectionName) throws Exception
	{
		AbstractEvaluator evaluator = context.getEvaluator();
		parameters.evaluateAll(evaluator);
		Object obj = parameters.get(connectionName);
		IMessageDictionary dic = getMessageDictionary(matrix, (obj instanceof ClientConnection) ? (ClientConnection)obj : null);
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

	public static void messageValues(List<ReadableValue> list, Context context, Matrix matrix, Parameters parameters, String connectionName, String messageTypeName, String fieldName) throws Exception
	{
		AbstractEvaluator evaluator = context.getEvaluator();
		parameters.evaluateAll(evaluator);
		Object obj = parameters.get(connectionName);
		IMessageDictionary dic = getMessageDictionary(matrix, (obj instanceof ClientConnection) ? (ClientConnection)obj : null);
		String messageType = Str.asString(parameters.get(messageTypeName));
		IMessage message = dic.getMessage(messageType);
		if (message == null)
		{
			throw new Exception("The message with message type='" + messageType + "' is unknown.");
		}
		IField field = message.getField(fieldName);
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

	public static void additionParameters(List<ReadableValue> list, Context context, Matrix matrix, Parameters parameters, String connectionName, String messageTypeName) throws Exception
	{
		AbstractEvaluator evaluator = context.getEvaluator();
		parameters.evaluateAll(evaluator);
		Object obj = parameters.get(connectionName);
		IMessageDictionary dic = getMessageDictionary(matrix, (obj instanceof ClientConnection) ? (ClientConnection)obj : null);
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

	private static IMessageDictionary getMessageDictionary(Matrix matrix, ClientConnection connection) throws Exception
	{
		IMessageDictionary dictionary = null;
		if (connection != null)
		{
			 dictionary = connection.getDictionary();
		}
		if (dictionary == null)
		{
			dictionary = matrix.getDefaultClient() == null ? null : matrix.getDefaultClient().getDictionary();
		}

		if (dictionary == null)
		{
			throw new Exception("You need to set up default client");
		}
		return dictionary;
	}

	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ActionClientHelper.class);


}
