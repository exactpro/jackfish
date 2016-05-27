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
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLCRMM",
		generalDescription 		= "Composes Message over client's connection. ",
		additionFieldsAllowed 	= true,
		outputDescription = "Converted message.", 
		outputType = MapMessage.class
	)
public class ClientCreateMapMessage extends AbstractAction
{
	public final static String connectionName 	= "ClientConnection";
	public final static String messageTypeName 	= "MessageType";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The client connection." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageTypeName, mandatory = true, description = "Message type." )
	protected String	messageType	= null;

	public ClientCreateMapMessage()
	{
	}
	
	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		ActionClientHelper.additionParameters(list, context, super.owner.getMatrix(), parameters, connectionName, messageTypeName);
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
				ActionClientHelper.messageTypes(list, context, super.owner.getMatrix(), parameters, connectionName);
				break;
				
			default:
				ActionClientHelper.messageValues(list, context, super.owner.getMatrix(), parameters, connectionName, messageTypeName, parameterToFill);
				break;
		}
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null");
		}
		else
		{
			IClient client = this.connection.getClient();
			ClientHelper.errorIfDisable(client.getClass(), Possibility.Encoding);			
			MapMessage message = client.getCodec().convert(this.messageType, parameters.select(TypeMandatory.Extra));
			super.setResult(message);
		}
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
		
}
