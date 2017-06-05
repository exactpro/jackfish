////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.clients;

import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLSTRT",
		generalDescription 		= "The purpose of the action is to start the client. It establishes the connection with "
				+ "the remote service, by using the mentioned address and the port, receives messages and adds them in the queue. "
				+ "If in the customer setting the parameter UseHeartBeat is shown as true, then in the queueing the client will "
				+ "automatically send  the heart beat with the interval, which is also mentioned in the parameters of the client.",
		additionFieldsAllowed 	= true,
		outputType 				= Boolean.class,
		outputDescription 		= "True, if the client started successfully.",
		additionalDescription 	= "As additional parameters, parameters with the names Address and Port are indicated, "
				+ "in the value of which, correspondingly the address and the service port are indicated, with which the connection is required.",
		examples 				= "{{`1. Load the client for FIX.`}}"
				+ "{{`2. Start the client.`}}"
				+ "{{##Id;#Action;#ClientId\n"
				+ "CLLD1;ClientLoad;'FIX'\n"
				+ "#Id;#Action;#ClientConnection\n"
				+ "CLSTRT1;ClientStart;CLLD1.Out#}}"
	)
public class ClientStart extends AbstractAction 
{
	public final static String connectionName = "ClientConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad" )
	protected ClientConnection	connection	= null;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.START, context, this.owner.getMatrix(), parameters, null, connectionName, null);
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		boolean res = Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName);
		return res ? HelpKind.ChooseFromList : null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		Helper.messageValues(list, context, this.owner.getMatrix(), parameters, null, connectionName, null, parameterToFill);
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();
		boolean res = client.start(context, parameters.select(TypeMandatory.Extra));
		if (res)
		{
			super.setResult(res);
		}
		else
		{
			super.setError("Connection can not be established.", ErrorKind.CLIENT_ERROR);
		}

	}

}
