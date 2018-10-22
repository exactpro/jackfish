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

package com.exactprosystems.jf.actions.clients;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;
import java.util.Map;

@ActionAttribute(
		group					      = ActionGroups.Clients,
		suffix						  = "CLCNT",
		constantGeneralDescription 	  = R.CLIENT_COUNT_MESSAGES_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantOutputDescription 	  = R.CLIENT_COUNT_MESSAGES_OUTPUT_DESC,
		outputType					  = Integer.class,
		constantAdditionalDescription = R.CLIENT_COUNT_MESSAGES_ADDITIONAL_DESC,
		constantExamples 			  = R.CLIENT_COUNT_MESSAGES_EXAMPLE
	)
public class ClientCountMessages extends AbstractAction
{
	public static final String connectionName  = "ClientConnection";
	public static final String messageTypeName = "MessageType";
	public static final String conditionsName  = "Conditions";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_COUNT_MESSAGES_CONNECTION)
	protected ClientConnection connection;

	@ActionFieldAttribute(name = messageTypeName, mandatory = true, constantDescription = R.CLIENT_COUNT_MESSAGES_MESSAGE_TYPE)
	protected String messageType;

	@ActionFieldAttribute(name = conditionsName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.CLIENT_COUNT_MESSAGES_CONDITIONS)
	protected Condition[] conditions;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.ENCODE, context, this.owner.getMatrix(), parameters, null, connectionName, messageTypeName);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (messageTypeName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case messageTypeName:
				list.add(new ReadableValue(context.getEvaluator().createString("*"), "Any messages"));
				Helper.messageTypes(list, this.owner.getMatrix(), context, parameters, null, connectionName);
				break;

			default:
				Helper.messageValues(list, context, this.owner.getMatrix(), parameters, null, connectionName, messageTypeName, parameterToFill);
				break;
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator)  throws Exception
	{
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Receiving);
		int countMessages;
		Map<String, Object> additional = parameters.select(TypeMandatory.Extra).makeCopy();

		if (!Str.areEqual(this.messageType, "*") || this.conditions != null || additional != null)
		{
			countMessages = client.countMessages(additional, this.messageType, this.conditions);
		}
		else
		{
			countMessages = client.totalMessages();
		}
		super.setResult(countMessages);
	}
}
