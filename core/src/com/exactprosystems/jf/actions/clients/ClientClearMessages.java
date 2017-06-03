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
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLCLM",
		generalDescription 		= "The purpose of the action is to clear the list of all accepted client messages."
				+ " Start of the client is not mandatory.",
		additionFieldsAllowed 	= false,
		examples 				= "{{`1. Load the client for FIX.`}}"
				+ "{{`2. Clear the list of the client messages.`}} "
				+ "{{##Id;#Action;#ClientId\n"
				+ "CLLD1;ClientLoad;'FIX'\n"
				+ "\n"
				+ "#Id;#Action;#ClientConnection\n"
				+ "CLCLM1;ClientClearMessages;CLLD1.Out#}}"
	)
public class ClientClearMessages extends AbstractAction 
{
	public final static String connectionName = "ClientConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action {{@ClientLoad@}}." )
	protected ClientConnection	connection	= null;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Receiving);
		client.clearMessages();

		super.setResult(null);
	}
}
