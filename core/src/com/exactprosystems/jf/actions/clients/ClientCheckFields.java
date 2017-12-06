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
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					   = ActionGroups.Clients,
		suffix					   = "CLCHM",
		constantGeneralDescription = R.CLIENT_CHECK_FIELDS_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.CLIENT_CHECK_FIELDS_OUTPUT_DESC,
		outputType				   = Boolean.class,
		constantExamples     	   = R.CLIENT_CHECK_FIELDS_EXAMPLE
	)
public class ClientCheckFields extends AbstractAction
{
	public static final String connectionName = "ClientConnection";
	public static final String messageName    = "MapMessage";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_CHECK_FIELDS_CONNECTION)
	protected ClientConnection connection;

	@ActionFieldAttribute(name = messageName, mandatory = true, constantDescription = R.CLIENT_CHECK_FIELDS_MESSAGE)
	protected MapMessage message;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();
		MapMessage map = client.getCodec().convert(this.message.getMessageType(), this.message);
		super.setResult(map.isCorrect());
	}
}
