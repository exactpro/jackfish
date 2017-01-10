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
		generalDescription 		= "Compares the given message to a set of name-value pairs.",
		additionFieldsAllowed 	= true
	)
public class ClientCheckMessage extends AbstractAction 
{
	public final static String connectionName 	= "ClientConnection";
	public final static String actualName = "ActualMessage";
	public final static String expectedMessageTypeName 	= "ExpectedMessageType";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The client connection." )
	protected ClientConnection	connection	= null;

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "Actual value. It is Message object that was found by GetMessage action. For example FoundOrder.Out")
	protected MapMessage actual = null;

	@ActionFieldAttribute(name = expectedMessageTypeName, mandatory = true, description = "Expected message type." )
	protected String	messageType	= null;

	public ClientCheckMessage()
	{
	}

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
	public void initDefaultValues() 
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.actual == null)
		{
			super.setError("Actual object is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		
		boolean sameTypes = Str.areEqual(this.messageType, this.actual.getMessageType());
		
		Map<String, String> diff = ClientHelper.difference(this.actual, Condition.convertToCondition(parameters.select(TypeMandatory.Extra))); 
		
		if (diff == null && sameTypes)
		{
			super.setResult(null);
		}
		else
		{
			ReportTable table = report.addTable("Mismatched fields:", null, true, 1, new int[] { 20, 80 }, "Name", "Expected & Actual");
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
