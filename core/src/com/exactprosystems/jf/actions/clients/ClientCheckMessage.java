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
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@ActionAttribute(
		group					= ActionGroups.Clients,
		suffix					= "CLMSGCHK",
		generalDescription 		= "The purpose of the action is to compare the message type MapMessage with the field set type key-value."
				+ " MapMessage is the output value of actions: {{@ClientCreateMapMessage@}}, {{@ClientDecode@}}, {{@ClientGetMessage@}}, {{@ClientSendMessage@}}."
				+ " In the report a chart will be formed with the headline the Mismatched fields: consisting of columns {{$Name$}} and {{$Expected$}} + {{$Actual$}}. "
				+ "In the case of inequality in the compared fields the MapMessage action fails."
				+ " Start of the client is not mandatory.",
		additionFieldsAllowed 	= true,
		additionalDescription   = "In additional parameters, the names and values which should be compared with MapMessage, which is passed in parameter {{$ActualMessage$}}, are pointed out.",
		examples 				= "{{`1. Load the client for FIX`}}"
				+ "{{`2. Create a message type FIX with a set key-value.`}}"
				+ "{{`3. Check the message.`}} "
				+ "{{#\n" +
				"#Id;#Action;$ClientId\n"
				+ "CLLD1;ClientLoad;'TestClient'\n"
				+ "\n"
				+ "#Id;#Action;Address;Port;$ClientConnection\n"
				+ "CLSTRT1;ClientStart;'127.0.0.1';13000;CLLD3.Out\n"
				+ "\n"
				+ "#Id;#Action;PartyID;PartyIDSource;PartyRole;$MessageType\n"
				+ "MSGCR1;MessageCreate;'test';'1';3;'35'\n"
				+ "\n"
				+ "#Id;#Action;PartyID;$ActualMessage;$ExpectedMessageType;$ClientConnection\n"
				+ "CLMSGCHK1;ClientCheckMessage;'test';MSGCR1.Out;'35';CLLD1.Out#}}"
	)
public class ClientCheckMessage extends AbstractAction 
{
	public final static String connectionName 	= "ClientConnection";
	public final static String actualName = "ActualMessage";
	public final static String expectedMessageTypeName 	= "ExpectedMessageType";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The connection with the client, which is derived from the action ClientLoad." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "The message that is required to compare.")
	protected MapMessage actual = null;

	@ActionFieldAttribute(name = expectedMessageTypeName, mandatory = true, description = "The message type that is expected." )
	protected String	messageType	= null;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.ENCODE, context, this.owner.getMatrix(), parameters, null, connectionName, expectedMessageTypeName);
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (expectedMessageTypeName.equals(fieldName))
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
			case expectedMessageTypeName:
				Helper.messageTypes(list, this.owner.getMatrix(), context, parameters, null, connectionName);
				break;
				
			default:
				Helper.messageValues(list, context, this.owner.getMatrix(), parameters, null, connectionName, expectedMessageTypeName, parameterToFill);
				break;
		}
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		boolean sameTypes = Str.areEqual(this.messageType, this.actual.getMessageType());
		
		Map<String, String> diff = ClientHelper.difference(this.actual, Condition.convertToCondition(parameters.select(TypeMandatory.Extra).makeCopy())); 
		
		if (diff == null && sameTypes)
		{
			super.setResult(null);
		}
		else
		{
			ReportTable table = report.addTable("Mismatched fields:", null, true, true, new int[] { 20, 80 }, "Name", "Expected & Actual");
			if (!sameTypes)
			{
				table.addValues(MapMessage.messageTypeName, "" + this.messageType + " is not " + this.actual.getMessageType());
			}
			
			for (Entry<String, String> entry : diff.entrySet())
			{
				table.addValues(entry.getKey(), entry.getValue());
			}
			
			super.setError("The message does not match.", ErrorKind.NOT_EQUAL);
		}
	}

}
