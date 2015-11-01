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
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;

@ActionAttribute(
		group = ActionGroups.Clients, 
		suffix					= "CLDEC",
		generalDescription = "Convert given array of bytes to a MapMessage. ", 
		additionFieldsAllowed = false, 
		outputDescription = "Converted message.", 
		outputType = MapMessage.class
		)
public class ClientDecode extends AbstractAction
{
	public final static String connectionName 	= "ClientConnection";
	public final static String arrayName 		= "Array";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The client connection." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = arrayName, mandatory = true, description = "Array of bytes.")
	protected Byte[]	array	= null;

	public ClientDecode()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null");
		}
		else
		{
			IClient client = this.connection.getClient();
			ClientHelper.errorIfDisable(client.getClass(), Possibility.Decoding);			
			MapMessage res = client.getCodec().decode(Converter.convertToByteArray(this.array));
	
			super.setResult(res);
		}
	}
}
