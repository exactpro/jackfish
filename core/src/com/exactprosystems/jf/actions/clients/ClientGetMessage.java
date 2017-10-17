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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

@ActionAttribute(
		group						  = ActionGroups.Clients,
		suffix						  = "CLGM",
		constantGeneralDescription 	  = R.CLIENT_GET_MESSAGE_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.CLIENT_GET_MESSAGE_ADDITIONAL_DESC,
		constantOutputDescription 	  = R.CLIENT_GET_MESSAGE_OUTPUT_DESC,
		outputType					  = MapMessage.class,
		constantExamples 			  = R.CLIENT_GET_MESSAGE_EXAMPLE
	)
public class ClientGetMessage extends AbstractAction 
{
	public final static String connectionName	= "ClientConnection";
	public final static String conditionsName 	= "Conditions";
	public final static String messageTypeName = "MessageType";
	public final static String timeoutName 		= "MessageTimeout";
	public final static String removeName 		= "Remove";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageTypeName, mandatory = true, description = "The type of the message. Use * for any type of messages." )
	protected String	messageType	= null;

	@ActionFieldAttribute(name = conditionsName, mandatory = false, def = DefaultValuePool.Null, description = "The conditions upon which the message will be checked.")
	protected Condition[] conditions;

	@ActionFieldAttribute(name = timeoutName, mandatory = false, def = DefaultValuePool.Int20000, description = "The time which is given to find the acceptable message.")
	protected Integer timeout;
	
	@ActionFieldAttribute(name = removeName, mandatory = false, def = DefaultValuePool.True, description = "Delete the found message.")
	protected Boolean remove;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.ENCODE, context, this.owner.getMatrix(), parameters, null, connectionName, messageTypeName);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (messageTypeName.equals(fieldName) || removeName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		boolean res = Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName);
		return res ? HelpKind.ChooseFromList : null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case messageTypeName:
                list.add(new ReadableValue(context.getEvaluator().createString("*"), "Any messages"));
				Helper.messageTypes(list, this.owner.getMatrix(), context, parameters, null, connectionName);
				break;
				
			case removeName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;

			default:
				Helper.messageValues(list, context, this.owner.getMatrix(), parameters, null, connectionName, messageTypeName, parameterToFill);
				break;
		}
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Receiving);

		MapMessage ret = client.getMessage(parameters.select(TypeMandatory.Extra).makeCopy(), this.messageType, this.conditions, this.timeout, this.remove);

		if (ret != null)
		{
			super.setResult(ret);
			return;
		}
		super.setError("Timeout", ErrorKind.TIMEOUT);
	}
}
