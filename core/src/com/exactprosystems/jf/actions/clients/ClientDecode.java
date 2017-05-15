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
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group = ActionGroups.Clients, 
		suffix					= "CLDEC",
		generalDescription 		= "The purpose of the action is for  the message decode. Converts the massive byte in the message."
				+ " Start of the client is not mandatory.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Converted message.",
		outputType 				= MapMessage.class,
		examples 				= "{{`1. Load the client for FIX.`}}"
				+ "{{`2. Create the message.`}}"
				+ "{{`3. Encode the message.`}}"
				+ "{{`4. Decode the message.`}} "
				+ "{{##Id;#Action;$ClientId\n" +
				"CLLD1;ClientLoad;'FIX'\n" +
				"#Id;#Action;PartyID;$MessageType\n" +
				"MSGCR1;MessageCreate;'test';'35'\n" +
				"#Id;#Action;$MapMessage;$ClientConnection\n" +
				"CLENC1;ClientEncode;MSGCR1.Out;CLLD1.Out\n" +
				"#Id;#Action;$Array;$ClientConnection\n" +
				"CLDEC1;ClientDecode;CLENC1.Out;CLLD4.Out#}}"
		)
public class ClientDecode extends AbstractAction
{
	public final static String connectionName 	= "ClientConnection";
	public final static String arrayName 		= "Array";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = arrayName, mandatory = true, description = "A massive byte which is required to convert in MapMessage.")
	protected Byte[]	array	= null;

	public ClientDecode()
	{
	}

	@Override
	public void initDefaultValues() 
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Decoding);
		MapMessage res = client.getCodec().decode(Converter.convertToByteArray(this.array));

		super.setResult(res);

	}
}
