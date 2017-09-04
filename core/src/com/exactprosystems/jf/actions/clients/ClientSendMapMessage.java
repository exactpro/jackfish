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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLSMM",
		generalDescription 		= "The purpose of the action is to send messages through a made connection."
				+ "The start of the client is mandatory. It is targeted at converting messages for specified type and its sending.",
		additionFieldsAllowed 	= false,
		examples 				= "{{`1. Load the client for FIX.`}}"
				+ "{{`2. Create the message with the help of MessageCreate method.`}}"
				+ "{{`3. Connect to the port #13000.`}}"
				+ "{{`4. Send the created message, test it preliminarily with the indication of CHECK - true in the  parameter.`}}"
				+ "{{#\n" +
				"#Id;#Action;$ClientId\n" +
				"CLLD1;ClientLoad;'FIX'\n" +
				"#Id;#Action;PartyID;$MessageType\n" +
				"MSGCR1;MessageCreate;'qpwoei';'35'\n" +
				"#Id;#Action;Address;Port;$ClientConnection\n" +
				"CLSTRT1;ClientStart;'127.0.0.1';13000;CLLD3.Out\n" +
				"#Id;#Action;$Check;$MapMessage;$ClientConnection\n" +
				"CLSMM1;ClientSendMapMessage;true;MSGCR1.Out;CLLD1.Out#}}"
	)
public class ClientSendMapMessage extends AbstractAction
{
	public final static String connectionName = "ClientConnection";
	public final static String messageName = "MapMessage";
	public final static String checkName = "Check";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageName, mandatory = true, description = "Message that is required to send." )
	protected MapMessage	message	= null;

	@ActionFieldAttribute(name = checkName, mandatory = false, def = DefaultValuePool.True, description = "Validation message check before sending. As a default true." )
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
