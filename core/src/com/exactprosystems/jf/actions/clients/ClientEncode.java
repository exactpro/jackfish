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
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					   = ActionGroups.Clients,
		suffix					   = "CLENC",
		constantGeneralDescription = R.CLIENT_ENCODE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.CLIENT_ENCODE_OUTPUT_DESC,
		outputType				   = byte[].class,
		constantExamples 		   = R.CLIENT_ENCODE_EXAMPLE
	)
public class ClientEncode extends AbstractAction
{
	public final static String connectionName 	= "ClientConnection";
	public final static String messageName 		= "MapMessage";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageName, mandatory = true, description = "The object type MapMessage, which is required to convert in a massive byte." )
	protected MapMessage	message	= null;
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Encoding);
		byte[] res = client.getCodec().encode(this.message.getMessageType(), this.message);

		super.setResult(res);
	}
}
