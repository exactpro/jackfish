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
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

@ActionAttribute(
		group 				  = ActionGroups.Clients,
		suffix 				  = "CLCNCT",
		generalDescription 	  = "The purpose of the action is to connect the client to the specific socket. Usually the number of the socket comes from the service.\n" +
				"Later the client will bring in and send messages through this socket.",
		additionFieldsAllowed = true, 
		outputDescription 	  = "True, if the connection was successful.",
		additionalDescription = "Parameters for  the client run are indicated.",
		examples 			  = "{{`1. Load the client for FIX.`}}"
				+ "{{`2. Connect the client to the port №10506.`}}"
				+ "{{##Id;#Action;#ClientId\n"
				+ "CLLD1;ClientLoad;'FIX'\n"
				+ "#Id;#Action;#ClientConnection;#Socket\n"
				+ "CLCNCT1;ClientConnect;CLLD1.Out;10506#}}"
)
public class ClientConnect extends AbstractAction
{
	public final static String	connectionName		= "ClientConnection";
	public final static String	socketName			= "Socket";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad.")
	protected ClientConnection	connection = null;

	@ActionFieldAttribute(name = socketName, mandatory = true, description = "The port that will use the client.")
	protected Socket socket = null;

	public ClientConnect()
	{
	}

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.CONNECT, context, this.owner.getMatrix(), parameters, null, connectionName, null);
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
	public void initDefaultValues() 
	{
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
}
