////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.clients;

import java.net.Socket;
import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;

@ActionAttribute(
		group = ActionGroups.Clients, 
		suffix = "CLCNCT", 
		generalDescription = "Connect the client to the given socket", 
		additionFieldsAllowed = true, 
		outputDescription = "True, if client connects successful."
)
public class ClientConnect extends AbstractAction
{
	public final static String	connectionName		= "ClientConnection";
	public final static String	socketName			= "Socket";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The client connection.")
	protected ClientConnection	connection = null;

	@ActionFieldAttribute(name = socketName, mandatory = true, description = "Socket to which this client has to connect.")
	protected Socket socket = null;

	public ClientConnect()
	{
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return ActionClientHelper.canHelpWithParameters(context, parameters, connectionName, fieldName);
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		ActionClientHelper.listToFillParameterDerived(list, context, parameters, connectionName, parameterToFill);
	}

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		parameters.evaluateAll(context.getEvaluator());
		Object value = parameters.get(connectionName);
		if (value instanceof ClientConnection)
		{
			for (String str : ((ClientConnection)value).getClient().getFactory().wellKnownConnectArgs())
			{
				list.add(new ReadableValue(str));
			}
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null", ErrorKind.EMPTY_PARAMETER);
		}
		else
		{
			IClient client = this.connection.getClient();
			boolean res = client.connect(context, this.socket, parameters.select(TypeMandatory.Extra));
			if (res)
			{
				super.setResult(null);
			}
			else
			{
				super.setError("Connection can not be established.", ErrorKind.CLIENT_ERROR);
			}
		}
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}

}
