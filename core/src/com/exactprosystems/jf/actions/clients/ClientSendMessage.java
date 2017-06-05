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
import com.exactprosystems.jf.api.client.*;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLSM",
		generalDescription 		= "The purpose of the action is to create and send the message."
				+ " The start of the client is mandatory.",
		additionFieldsAllowed 	= true,
		outputDescription = "The message which was created and sent.",
		outputType = MapMessage.class,
		additionalDescription 	= "In additional parameters name and values set is indicated which will be converted into the message type which is typical for the client.",
		examples 				= "{{`1. Load the client for FIX.`}}"
				+ "{{`2. Connect to the port №10555.`}}"
				+ "{{`3. Create and send the message, check it beforehand with the indication of CHECK - true in the  parameter.`}}"
				+ "{{##Id;#Action;$ClientId\n" +
				"CLLD1;ClientLoad;'TestClient'\n" +
				"#Id;#Action;Address;Port;$ClientConnection\n" +
				"CLSTRT1;ClientStart;'127.0.0.1';10555;CLLD1.Out\n" +
				"#Id;#Action;$Check;PartyID;$ClientConnection;$MessageType\n" +
				"CLSM1;ClientSendMessage;true;'test';CLLD1.Out;'35'#}}"
	)
public class ClientSendMessage extends AbstractAction
{
	public final static String connectionName = "ClientConnection";
	public final static String messageTypeName = "MessageType";
	public final static String checkName = "Check";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageTypeName, mandatory = true, description = "The type of the created message." )
	protected String	messageType	= null;

	@ActionFieldAttribute(name = checkName, mandatory = false, def = DefaultValuePool.True, description = "Checks the validation before the message sending. As a default true.")
	protected Boolean	check;

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
			case messageTypeName:
				Helper.messageTypes(list, this.owner.getMatrix(), context, parameters, null, connectionName);
				break;
				
			case checkName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;

			default:
				Helper.messageValues(list, context, this.owner.getMatrix(), parameters, null, connectionName, messageTypeName, parameterToFill);
				break;
		}
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Parameters additional = parameters.select(TypeMandatory.Extra);
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Sending);

		if (this.check)
		{
			IMessage mes = client.getFactory().getDictionary().getMessage(this.messageType);
			if (mes != null)
			{
				client.sendMessage(this.messageType, additional.makeCopy(), true);
				super.setResult(null);
			}
			else
			{
				super.setError("Message is failed.", ErrorKind.CLIENT_ERROR);
			}
		}
		else
		{
			client.sendMessage(this.messageType, additional.makeCopy(), false);
			super.setResult(null);
		}
	}


}
