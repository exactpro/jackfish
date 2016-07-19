////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.clients;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.documents.matrix.parser.items.ErrorKind;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLSMM",
		generalDescription 		= "Sends MapMessage over client's connection. ",
		additionFieldsAllowed 	= false
	)
public class ClientSendMapMessage extends AbstractAction
{
	public final static String connectionName = "ClientConnection";
	public final static String messageName = "Message";
	public final static String checkName = "Check";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The client connection." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageName, mandatory = true, description = "The message that will be sent." )
	protected MapMessage	message	= null;

	@ActionFieldAttribute(name = checkName, mandatory = false, description = "Check the message before sending." )
	protected boolean	check;

	@Override
	public void initDefaultValues() 
	{
		check	= true;
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		if (checkName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return ActionClientHelper.canHelpWithParameters(context, parameters, connectionName, fieldName);
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
				
			default:
					ActionClientHelper.listToFillParameterDerived(list, context, parameters, connectionName, parameterToFill);
		}
	}

	public ClientSendMapMessage()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null",  ErrorKind.EMPTY_PARAMETER);
		}
		else
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


}
