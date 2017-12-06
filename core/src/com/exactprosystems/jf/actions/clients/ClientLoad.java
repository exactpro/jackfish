////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.clients;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.Clients,
		suffix					   = "CLLD",
		constantGeneralDescription = R.CLIENT_LOAD_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.CLIENT_LOAD_OUTPUT_DESC,
		outputType 				   = ClientConnection.class,
		constantExamples 	       = R.CLIENT_LOAD_EXAMPLE
	)
public class ClientLoad extends AbstractAction 
{
	public static final String idName = "ClientId";

	@ActionFieldAttribute(name = idName, mandatory = true, constantDescription = R.CLIENT_LOAD_ID)
	protected String id;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return idName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (idName.equals(parameterToFill))
		{
			Helper.clientsNames(list, context);
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		ClientConnection connection = context.getConfiguration().getClientPool().loadClient(this.id);
		super.setResult(connection);
	}
}
