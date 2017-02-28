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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group					= ActionGroups.Messages,
		suffix					= "MSGCR",
		generalDescription 		= "The purpose of the action is to create an object of MapMessage type.\n" +
				"The object of MapMessage type is a collection which stores data in the form of key/value pairs.\n" +
				"It is used in such actions as ClientCheckFields, ClientCheckMessage, ClientEncode, ClientSendMapMessage, MessageCheck, MessageCompareTwo.",
		additionalDescription = "Names and values are passed to fill MapMessage. This parameter is used only if the parameter Fiels is not set.",
		additionFieldsAllowed 	= true,
		outputDescription 		= "Message object.",
		outputType				= MapMessage.class,
		examples = "{{##Id;#Action;#Fields\n" +
				"MSGCR1;MessageCreate;{'First item' : 'First Value', 'Second Item' : 'Second Value'}#}}"
	)
public class MessageCreate extends AbstractAction 
{
	public final static String messageTypeName = "MessageType";
	public final static String sourceName = "Source";
	public final static String fieldsName = "Fields";

	@ActionFieldAttribute(name = messageTypeName, mandatory = false, description = "The type of created MapMessage should be specified." )
	protected String 				messageType;

	@ActionFieldAttribute(name = sourceName, mandatory = false, description = "The source of created MapMessage should be specified." )
	protected String 				source;

	@ActionFieldAttribute(name = fieldsName, mandatory = false, description = "The collection of Map type values is specified." )
	protected Map<String, Object> 	fields;

	public MessageCreate()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		messageType	= null;
		source	= null;
		fields	= null;
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

