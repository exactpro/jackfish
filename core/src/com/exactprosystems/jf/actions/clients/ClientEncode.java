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

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_ENCODE_CONNECTION )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageName, mandatory = true, constantDescription = R.CLIENT_ENCODE_MESSAGE )
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
