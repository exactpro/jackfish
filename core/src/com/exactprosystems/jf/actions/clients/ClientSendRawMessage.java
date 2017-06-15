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
		suffix					= "CLSRM",
		generalDescription 		= "The purpose of the action is to send an array of bytes through a made connection without any preprocessing."
				+ "The start of the client is mandatory.",
		additionFieldsAllowed 	= false,
		examples 				= "{{`1. Load the client for FIX.`}}"
				+ "{{`2. Connect to the port #10555.`}}"
				+ "{{`3. Create and send the raw message.`}} "
				+ "{{#\n" +
				"#Id;#Let\n" +
				"str;'8=FIXT.1.1|9=91|35=A|34=1|49=SenderCompID|52=20170426-08:25:00.002'\n" +
				"#Id;#Let\n" +
				"str;str + '|56=TargetCompID|98=0|108=1|141=Y|1137=9|10=131|'\n" +
				"#Id;#Let\n" +
				"bytes;str.replace('|', '\\001').getBytes()\n" +
				"#Id;#Action;$ClientId\n" +
				"CLLD1;ClientLoad;'FIX'\n" +
				"#Id;#Action;$ClientConnection;$Socket\n" +
				"CLCNCT1;ClientConnect;CLLD1.Out;10555\n" +
				"#Id;#Action;$ClientConnection;$Data\n" +
				"CLSRM1;ClientSendRawMessage;CLLD1.Out;bytes#}}"
	)
public class ClientSendRawMessage extends AbstractAction
{
	public final static String connectionName = "ClientConnection";
	public final static String dataName = "Data";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = dataName, mandatory = true, description = "Array of bytes that will be sent 'as is'." )
	protected byte[]	data	= null;



	public ClientSendRawMessage()
	{
	}


	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		boolean res = Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName);
		return res ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case dataName:
				Helper.messageTypes(list, this.owner.getMatrix(), context, parameters, null, connectionName);
				break;
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Sending);

		client.sendMessage(this.data, false);
		super.setResult(null);
	}


}
