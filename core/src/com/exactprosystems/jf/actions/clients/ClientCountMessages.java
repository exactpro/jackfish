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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;
import java.util.Map;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLCNT",
		generalDescription 		= "The purpose of the action is to count the messages, which field set responds to the reported condition."
				+ " Simple comparison conditions are passed in additional parameters. Complicated comparison conditions are indicated in the parameter Conditions.",
		additionFieldsAllowed 	= true,
		outputDescription 		= "The number of messages which meet the requirements.",
		outputType				= Integer.class,
		additionalDescription 	= "In the name of the parameter the key is indicated, in the option setting the predicted value is indicated.",
		examples 				= "{{`1. Load the client for FIX.`}}"
				+ "{{`2. Count the number of messages with the field name Name and  the field value Value.`}} "
				+ "{{#\n" +
				"#Id;#Action;#ClientId\n"
				+ "CLLD1;ClientLoad;'FIX'\n"
				+ "\n"
				+ "#Id;#Action;#ClientConnection;#MessageType;#Name\n"
				+ "CLCNT1;ClientCountMessages;CLLD1.Out;'35';'Value'#}}"
	)
public class ClientCountMessages extends AbstractAction 
{
	public final static String connectionName = "ClientConnection";
	public final static String messageTypeName = "MessageType";
	public final static String conditionsName = "Conditions";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad.")
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = messageTypeName, mandatory = true, description = "Message type. Use * for any type of messages." )
	protected String	messageType	= null;

	@ActionFieldAttribute(name = conditionsName, mandatory = false, def = DefaultValuePool.Null, description = "The conditions in which the comparison will be carried out.")
	protected Condition[] conditions;
	
	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.ENCODE, context, this.owner.getMatrix(), parameters, null, connectionName, messageTypeName);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (messageTypeName.equals(fieldName))
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
				
			default:
				Helper.messageValues(list, context, this.owner.getMatrix(), parameters, null, connectionName, messageTypeName, parameterToFill);
				break;
		}
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator)  throws Exception
	{
		IClient client = this.connection.getClient();
		ClientHelper.errorIfDisable(client.getClass(), Possibility.Receiving);
		Integer ret = null;
		Map<String, Object> additional = parameters.select(TypeMandatory.Extra).makeCopy();

		if (!Str.areEqual(this.messageType, "*") || this.conditions != null || additional != null)
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
