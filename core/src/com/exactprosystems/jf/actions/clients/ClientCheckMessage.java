/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.clients;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.client.ClientConnection;
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
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
import java.util.Optional;

@ActionAttribute(
		group					      = ActionGroups.Clients,
		suffix					      = "CLMSGCHK",
		constantGeneralDescription    = R.CLIENT_CHECK_MESSAGE_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.CLIENT_CHECK_MESSAGE_ADDITIONAL_DESC,
		constantExamples              = R.CLIENT_CHECK_MESSAGE_EXAMPLE
	)
public class ClientCheckMessage extends AbstractAction 
{
	public static final String connectionName          = "ClientConnection";
	public static final String actualName              = "ActualMessage";
	public static final String expectedMessageTypeName = "ExpectedMessageType";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.CLIENT_CHECK_MESSAGE_CONNECTION)
	protected ClientConnection connection;

	@ActionFieldAttribute(name = actualName, mandatory = true, constantDescription = R.CLIENT_CHECK_MESSAGE_ACTUAL)
	protected MapMessage actual;

	@ActionFieldAttribute(name = expectedMessageTypeName, mandatory = true, constantDescription = R.CLIENT_CHECK_MESSAGE_MESSAGE_TYPE)
	protected String messageType;

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
		return Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName) ? HelpKind.ChooseFromList : null;
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
			Optional.ofNullable(diff).ifPresent(d -> d.forEach(table::addValues));

			super.setError("The message does not match.", ErrorKind.NOT_EQUAL);
		}
	}

}
