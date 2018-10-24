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
import com.exactprosystems.jf.api.client.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.Clients,
		suffix					   = "CLSMM",
		constantGeneralDescription = R.CLIENT_SEND_MAP_MESSAGE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.CLIENT_SEND_MAP_MESSAGE_EXAMPLE
	)
public class ClientSendMapMessage extends AbstractAction
{
	public static final String connectionName = "ClientConnection";
	public static final String messageName    = "MapMessage";
	public static final String checkName      = "Check";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_SEND_MAP_MESSAGE_CONNECTION)
	protected ClientConnection connection;

	@ActionFieldAttribute(name = messageName, mandatory = true, constantDescription = R.CLIENT_SEND_MAP_MESSAGE_MESSAGE)
	protected MapMessage message;

	@ActionFieldAttribute(name = checkName, mandatory = false, def = DefaultValuePool.True, constantDescription = R.CLIENT_SEND_MAP_MESSAGE_CHECK)
	protected Boolean check;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (checkName.equals(fieldName))
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
			case checkName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
				
			case messageName:
				Helper.messageTypes(list, this.owner.getMatrix(), context, parameters, null, connectionName);
				break;

			default:
				break;
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Sending);

		if (this.check)
		{
			if (this.message.isCorrect())
			{
				client.sendMessage(this.message.getMessageType(), this.message, true);
				super.setResult(null);
			}
			else
			{
				super.setError("Message is failed.", ErrorKind.CLIENT_ERROR);
			}
		}
		else
		{
			client.sendMessage(this.message.getMessageType(), this.message, false);
			super.setResult(null);
		}
	}
}
