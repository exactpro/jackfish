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
import com.exactprosystems.jf.client.ClientsPool;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLLD",
		generalDescription 		= "Loads desired client and inits it. ",
		additionFieldsAllowed 	= false,
		outputDescription = "Connection to the client.", 
		outputType = ClientConnection.class
	)
public class ClientLoad extends AbstractAction 
{
	public final static String idName = "ClientId";

	@ActionFieldAttribute(name = idName, mandatory = true, description = "The client id." )
	protected String 		id	= null;

	public ClientLoad()
	{
	}

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
				for (String str : context.getClients().clientNames())
				{
					String quoted = context.getEvaluator().createString(str);
					list.add(new ReadableValue(quoted));
				}
				break;

			default:
		}
	}

	@Override
	public void doRealDocumetation(Context context, ReportBuilder report)
	{
		ReportTable info = report.addTable("Available clients", 100, new int[] { 100 }, "Client name");
		for (String protocol : context.getClients().clientNames())
		{
			info.addValues(protocol);
		}
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		ClientsPool client = context.getClients();
		ClientConnection connection = client.loadClient(this.id);
		
		super.setResult(connection);
	}

}
