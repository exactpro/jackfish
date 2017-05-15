////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.clients;

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

import java.util.List;

@ActionAttribute(
		group 					= ActionGroups.Clients, 
		suffix 					= "CLSP", 
		generalDescription 		= "The purpose of the action is to set properties to the client."
				+ " The following properties are available: {{$SenderCompID$}}, {{$TargetCompID$}}, {{$MsgSeqNum$}}.",
		additionFieldsAllowed 	= true,
		additionalDescription 	= "In the value of the additional parameter the name of the property is indicated, in the parameter  value the property value is pointed out.",
		examples 				= "{{`1. Load the client for FIX`}}"
				+ "{{`2. Set the propertyName the value Value`}} "
				+ "{{##Id;#Action;#ClientId\n"
				+ "CLLD1;ClientLoad;'FIX'\n"
				+ "#Id;#Action;#ClientConnection;#Name\n"
				+ "CLSP1;ClientSetProperties;CLLD1.Out;'Value'#}}"
	)
public class ClientSetProperties extends AbstractAction
{
	public final static String	connectionName	= "ClientConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad.")
	protected ClientConnection	connection		= null;

	public ClientSetProperties()
	{
	}

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.PROPS, context, this.owner.getMatrix(), parameters, null, connectionName, null);
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
		switch (parameterToFill)
		{
			default:
				Helper.messageValues(list, context, this.owner.getMatrix(), parameters, null, connectionName, null, parameterToFill);
				break;
		}
	}

	@Override
	public void initDefaultValues()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Parameters additional = parameters.select(TypeMandatory.Extra);
		IClient client = this.connection.getClient();

		client.setProperties(additional.makeCopy());
		super.setResult(null);
	}

}
