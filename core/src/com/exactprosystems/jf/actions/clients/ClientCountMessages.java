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
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.TypeMandatory;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;

import java.util.List;
import java.util.Map;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLCNT",
		generalDescription 		= "Count messages whose fields are equal to the given fields or messages witch match given criteria.",
		additionFieldsAllowed 	= true,
		outputDescription 		= "Number of matched messages.",
		outputType				= Integer.class
	)
public class ClientCountMessages extends AbstractAction 
{
	public final static String connectionName = "ClientConnection";
	public final static String messageTypeName = "MessageType";
	public final static String conditionsName = "Conditions";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The client connection." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageTypeName, mandatory = true, description = "Message type." )
	protected String	messageType	= null;

	@ActionFieldAttribute(name = conditionsName, mandatory = false, description = "Conditions.")
	protected Condition[] conditions = null;

	public ClientCountMessages()
	{
	}
	
	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		ActionClientHelper.additionParameters(list, context, parameters, connectionName, messageTypeName);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (messageTypeName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		
		return ActionClientHelper.canHelpWithParameters(context, parameters, connectionName, fieldName);
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case messageTypeName:
				ActionClientHelper.messageTypes(list, context, parameters, connectionName);
				break;
				
			default:
				ActionClientHelper.messageValues(list, context, parameters, connectionName, messageTypeName, parameterToFill);
				break;
		}
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator)  throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null");
		}
		else
		{
			IClient client = this.connection.getClient();
			ClientHelper.errorIfDisable(client.getClass(), Possibility.Receiving);			
			Integer ret = null;
			Map<String, Object> additional = parameters.select(TypeMandatory.Extra);
			
			if (this.conditions != null || additional != null)
			{
				ret = client.countMessages(additional, this.messageType, this.conditions);
			}
			else
			{
				ret = client.totalMessages();
			}
			super.setResult(ret);
		}
	}
}
