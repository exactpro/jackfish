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
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLCHM",
		generalDescription 		= "The purpose of the action is to check message fields against the field set dictionary of the client."
				+ " Start of the client is not mandatory.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "True, if the message is correct.",
		outputType				= Boolean.class,
		examples 				= "{{`1.Load the client for FIX.`}}"
				+ "{{`2.Create a message type FIX with a set key-value - FirstName:FirstValue.`}}"
				+ "{{`3.Check the message.`}}"
				+ "{{##Id;#Action;#ClientId\n"
				+ "CLLD1;ClientLoad;'FIX'\n"
				+ "#Id;#Action;#ClientConnection;#MessageType;#Name\n"
				+ "CLCRMM1;ClientCreateMapMessage;CLLD1.Out;'35';'Value'\n"
				+ "#Id;#Action;#Message;#ClientConnection\n"
				+ "CLCHM1;ClientCheckFields;CLCRMM1.Out;CLLD1.Out#}}"
	)
public class ClientCheckFields extends AbstractAction
{
	public final static String connectionName 	= "ClientConnection";
	public final static String messageName 		= "MapMessage";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageName, mandatory = true, description = "The message that is required to check." )
	protected MapMessage	message	= null; 


	public ClientCheckFields()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null", ErrorKind.EMPTY_PARAMETER);
		}
		else
		{
			IClient client = this.connection.getClient();
			MapMessage map = client.getCodec().convert(this.message.getMessageType(), this.message);
			
			super.setResult(map.isCorrect());
		}
	}

	@Override
	public void initDefaultValues() 
	{
	}
}
