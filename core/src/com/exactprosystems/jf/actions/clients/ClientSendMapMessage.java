////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.clients;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.client.Possibility;
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
	public final static String connectionName = "ClientConnection";
	public final static String messageName = "MapMessage";
	public final static String checkName = "Check";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_SEND_MAP_MESSAGE_CONNECTION )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageName, mandatory = true, constantDescription = R.CLIENT_SEND_MAP_MESSAGE_MESSAGE )
	protected MapMessage	message	= null;

	@ActionFieldAttribute(name = checkName, mandatory = false, def = DefaultValuePool.True, constantDescription= R.CLIENT_SEND_MAP_MESSAGE_CHECK )
	protected Boolean	check;


	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (checkName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		boolean res = Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName);
		return res ? HelpKind.ChooseFromList : null;
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
