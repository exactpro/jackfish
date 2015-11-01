////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.message;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameter;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.TypeMandatory;
import com.exactprosystems.jf.common.report.ReportBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group					= ActionGroups.Messages,
		suffix					= "MSGCR",
		generalDescription 		= "Assemble a message from given data.",
		additionFieldsAllowed 	= true,
		outputDescription 		= "Message object.",
		outputType				= MapMessage.class
	)
public class MessageCreate extends AbstractAction 
{
	public final static String messageTypeName = "MessageType";
	public final static String sourceName = "Source";
	public final static String fieldsName = "Fields";

	@ActionFieldAttribute(name = messageTypeName, mandatory = false, description = "Message type." )
	protected String 		messageType	= null;

	@ActionFieldAttribute(name = sourceName, mandatory = false, description = "Source of message." )
	protected String 		source	= null;

	@ActionFieldAttribute(name = fieldsName, mandatory = false, description = "Collection of fields." )
	protected Map<String, Object> 	fields	= null;

	public MessageCreate()
	{
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Map<String, Object> map = null;
		
		if (this.fields == null)
		{
			map = new HashMap<String, Object>();
			
			for (Parameter parameter : parameters.select(TypeMandatory.Extra))
			{
				String name = parameter.getName();
				Object value = parameter.getValue();
				
				if (value == null)
				{
					continue;
				}
				
				if (value.getClass().isArray())
				{
					if (value.getClass().getComponentType().isAssignableFrom(Map.class))
					{
						map.put(name, value);
					}
					else
					{
						throw new Exception(String.format("For field '%s' value is an array, but not of type Map", name));
					}
					
				}
				else if (value instanceof Map)
				{
					map.put(name, new Object[] { value });
				}
				else if (value instanceof List)
				{
					map.put(name, ((List)value).toArray() );
				}
				else
				{
					map.put(name, value);
				}
			}
		}
		else
		{
			map = this.fields;
		}
		
		MapMessage ret = new MapMessage(this.messageType, map, this.source);
		
		if (ret != null)
		{
			super.setResult(ret);
			return;
		}
	}
}

