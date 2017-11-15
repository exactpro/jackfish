////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.message;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.actions.clients.Helper;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group					      = ActionGroups.Messages,
		suffix						  = "MSGCR",
		constantGeneralDescription 	  = R.MESSAGE_CREATE_GENERAL_DESC,
		constantAdditionalDescription = R.MESSAGE_CREATE_ADDITIONAL_DESC,
		additionFieldsAllowed 	      = true,
		constantOutputDescription 	  = R.MESSAGE_CREATE_OUTPUT_DESC,
		outputType					  = MapMessage.class,
		constantExamples 			  = R.MESSAGE_CREATE_EXAMPLE
	)
public class MessageCreate extends AbstractAction 
{
	public final static String messageTypeName = "MessageType";
	public final static String sourceName = "Source";
	public final static String fieldsName = "Fields";

	@ActionFieldAttribute(name = messageTypeName, mandatory = true, constantDescription = R.MESSAGE_CREATE_MESSAGE_TYPE )
	protected String 				messageType;

	@ActionFieldAttribute(name = sourceName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.MESSAGE_CREATE_SOURCE )
	protected String 				source;

	@ActionFieldAttribute(name = fieldsName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.MESSAGE_CREATE_FIELDS )
	protected Map<String, Object> 	fields;

    @Override
    protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters)
            throws Exception
    {
        Helper.helpToAddParameters(list, ParametersKind.ENCODE, context, this.owner.getMatrix(), parameters, null, null, messageTypeName);
    }

    @Override
    protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
            throws Exception
    {
        if (messageTypeName.equals(fieldName))
        {
            return HelpKind.ChooseFromList;
        }
        boolean res = Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, null, fieldName);
        return res ? HelpKind.ChooseFromList : null;
    }

    @Override
    protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill,
            Parameters parameters) throws Exception
    {
        switch (parameterToFill)
        {
        case messageTypeName:
            Helper.messageTypes(list, this.owner.getMatrix(), context, parameters, null, null);
            break;

        default:
            Helper.messageValues(list, context, this.owner.getMatrix(), parameters, null, null, messageTypeName, parameterToFill);
            break;
        }
    }
	
	@SuppressWarnings("rawtypes")
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Map<String, Object> map = null;
		
		if (this.fields == null)
		{
			map = new LinkedHashMap<String, Object>();
			
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
		super.setResult(ret);
	}
}

