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
import com.exactprosystems.jf.api.client.IClientsPool;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLLD",
		generalDescription 		= "The purpose of the action is to load,initialize and get the connection with the Client."
				+ "Later, the received connection is used in such actions as {{@ClientStart@}}, {{@ClientStop@}}, {{@ClientDecode@}} and etc."
				+ " Clients represent the entity which performs functions in respect of client- server."
				+ " The client can send and get messages through received TCP connection.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "The connection with the Client.",
		outputType 				= ClientConnection.class,
		examples 				= "{{`Load the client for FIX.`}} "
				+ "{{##Id;#Action;#ClientId\n"
				+ "CLLD1;ClientLoad;'FIX'#}}"
	)
public class ClientLoad extends AbstractAction 
{
	public final static String idName = "ClientId";

	@ActionFieldAttribute(name = idName, mandatory = true, description = "Id of the Client, with which the connection should be made." )
	protected String 		id	= null;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return idName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case idName:
				Helper.clientsNames(list, context);
				break;

			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClientsPool client = context.getConfiguration().getClientPool();
		ClientConnection connection = client.loadClient(this.id);
		
		super.setResult(connection);
	}
}
