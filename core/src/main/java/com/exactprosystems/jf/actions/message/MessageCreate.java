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

package com.exactprosystems.jf.actions.message;

import com.exactprosystems.jf.actions.*;
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
	public static final String messageTypeName = "MessageType";
	public static final String sourceName      = "Source";
	public static final String fieldsName      = "Fields";

	@ActionFieldAttribute(name = messageTypeName, mandatory = true, constantDescription = R.MESSAGE_CREATE_MESSAGE_TYPE)
	protected String messageType;

	@ActionFieldAttribute(name = sourceName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.MESSAGE_CREATE_SOURCE)
	protected String source;

	@ActionFieldAttribute(name = fieldsName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.MESSAGE_CREATE_FIELDS)
	protected Map<String, Object> fields;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.ENCODE, context, this.owner.getMatrix(), parameters, null, null, messageTypeName);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (messageTypeName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, null, fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
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

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Map<String, Object> map = null;

		if (this.fields == null)
		{
			map = new LinkedHashMap<>();

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
						throw new Exception(String.format(R.MESSAGE_CREATE_VALUE_ERROR.get(), name));
					}

				}
				else if (value instanceof Map)
				{
					map.put(name, new Object[]{value});
				}
				else if (value instanceof List)
				{
					map.put(name, ((List) value).toArray());
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

