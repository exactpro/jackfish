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
import com.exactprosystems.jf.api.client.*;
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

import java.util.List;

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
	public static final String connectionName  = "ClientConnection";
	public static final String conditionsName  = "Conditions";
	public static final String messageTypeName = "MessageType";
	public static final String timeoutName     = "MessageTimeout";
	public static final String removeName      = "Remove";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_GET_MESSAGE_CONNECTION)
	protected ClientConnection connection;

	@ActionFieldAttribute(name = messageTypeName, mandatory = true, constantDescription = R.CLIENT_GET_MESSAGE_MESSAGE_TYPE)
	protected String messageType;

	@ActionFieldAttribute(name = conditionsName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.CLIENT_GET_MESSAGE_CONDITIONS)
	protected Condition[] conditions;

	@ActionFieldAttribute(name = timeoutName, mandatory = false, def = DefaultValuePool.Int20000, constantDescription = R.CLIENT_GET_MESSAGE_TIMEOUT)
	protected Integer timeout;

	@ActionFieldAttribute(name = removeName, mandatory = false, def = DefaultValuePool.True, constantDescription = R.CLIENT_GET_MESSAGE_REMOVE)
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
		return Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName) ? HelpKind.ChooseFromList : null;
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
